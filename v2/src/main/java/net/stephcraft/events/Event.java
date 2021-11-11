package net.stephcraft.events;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.function.Consumer;

import net.stephcraft.events.settings.EventFlag;
import net.stephcraft.events.settings.EventState;

public abstract class Event <L, A,B,C,D,E,F,G,H,I> {
	
	private static class ListenerProperties<L> {
		private L listener;
		private int priority;
		private boolean ignoreCancelled;
		
		private ListenerProperties(L listener, int priority, boolean ignoreCancelled) {
			this.listener = listener;
			this.priority = priority;
			this.ignoreCancelled = ignoreCancelled;
		}
		
		private static <L> ListenerProperties<L> of(L listener, int priority, boolean ignoreCancelled) {
			return new ListenerProperties<>(listener, priority, ignoreCancelled);
		}
	}
	
	public static class EventStackElement {
		private EventState state;
		public boolean cancelled;
		
		private EventStackElement(EventState state) {
			this.state = state;
		}
	}
	
	private final Comparator<ListenerProperties<L>> comparator;
	private final Map<L, ListenerProperties<L>> listeners;
	private final Map<Event<?,?, A,B,C,D,E,F,G,H>, Set<L>> redirects;
	private final Map<EventState, PriorityQueue<ListenerProperties<L>>> queues;
	protected final EnumSet<EventFlag> flags;
	
	private EventStackElement cachedEventStackElement;
	private Stack<EventStackElement> eventStack;
	
	public Event(EventFlag... flags) {
		switch(flags.length) {
			case 0: this.flags = EnumSet.noneOf(EventFlag.class); break;
			case 1: this.flags = EnumSet.of(flags[0]); break;
			default: this.flags = EnumSet.of(flags[0], flags); break;
		}
		
		this.comparator = (a, b) -> a.priority - b.priority;
		this.listeners = new HashMap<>();
		this.redirects = new HashMap<>();
		this.eventStack = new Stack<>();
		this.cachedEventStackElement = new EventStackElement(null);
		
		if(this.flags.contains(EventFlag.CHRONICLED)) {
			this.queues = Map.of(
				EventState.PRE,  new PriorityQueue<>(comparator),
				EventState.POST, new PriorityQueue<>(comparator)
			);
		}
		else {
			this.queues = Map.of(EventState.DEFAULT, new PriorityQueue<>(comparator));
		}
	}
	
	// --- binding ---
	
	public void bind(EventState state, L listener, int priority, boolean ignoreCancelled) {
		failsafeChronicled(state);
		ListenerProperties<L> properties = ListenerProperties.of(listener, priority, ignoreCancelled);
		listeners.put(listener, properties);
		queues.get(state).add(properties);
	}
	
	public void bind(EventState state, L listener, int priority) {
		bind(state, listener, priority, false);
	}
	
	public void bind(EventState state, L listener) {
		bind(state, listener, 0, false);
	}
	
	public void bind(L listener, int priority, boolean ignoreCancelled) {
		bind(EventState.DEFAULT, listener, priority, ignoreCancelled);
	}
	
	public void bind(L listener, int priority) {
		bind(EventState.DEFAULT, listener, priority);
	}
	
	public void bind(L listener) {
		bind(EventState.DEFAULT, listener);
	}
	
	public void unbind(L listener) {
		queues.values().forEach((queue) -> queue.remove(listeners.remove(listener)));
	}
	
	public void unbind() {
		listeners.clear();
		redirects.clear();
	}
	
	public boolean bound(L listener) {
		return listeners.containsKey(listener);
	}
	
	// --- redirecting ---
	
	protected void redirect(Event<?,?, A,B,C,D,E,F,G,H> event, L listener, L pre, L post) {
		if(flags.contains(EventFlag.CHRONICLED)) {
			bind(EventState.PRE, pre, Integer.MAX_VALUE, false);
			bind(EventState.POST, post, Integer.MAX_VALUE, false);
			redirects.put(event, Set.of(pre, post));
		}
		else {
			bind(EventState.DEFAULT, listener, Integer.MAX_VALUE, false);
			redirects.put(event, Set.of(listener));
		}
	}
	
	protected void unredirect(Event<?,?, A,B,C,D,E,F,G,H> event) {
		if(!redirects.containsKey(event)) return;
		Set<L> listeners = redirects.get(event);
		listeners.forEach((listener) -> unbind(listener));
	}
	
	protected boolean redirected(Event<?,?, A,B,C,D,E,F,G,H> event) {
		return redirects.containsKey(event);
	}
		
	// --- flush ---
	
	@SuppressWarnings("rawtypes")
	public static void flush(Object object) {
		for(Field field : object.getClass().getFields()) {
			if(Modifier.isStatic(field.getModifiers()) || field.canAccess(object)) {
				if(field.getType().isAssignableFrom(Event.class)) {
					Object value = null;
					
					try {
						value = field.get(object);
					}
					catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
					
					if(value instanceof Event) {
						Event event = (Event)value;
						event.unbind();
					}
				}
			}
		}
	}
	
	// --- triggering ---
	
	protected void trigger(Consumer<L> consumer) {
		trigger(EventState.DEFAULT, consumer);
	}
	
	protected void trigger(Consumer<L> consumer, Runnable runnable) {
		
		if(flags.contains(EventFlag.CHRONICLED)) {
			
			// chronicled schedule
			trigger(EventState.PRE, consumer);
			if(!isCancelled()) {
				runnable.run();
				trigger(EventState.POST, consumer);
			}
		}
		else if(flags.contains(EventFlag.CANCELLABLE)) {
			
			// cancellable schedule
			trigger(consumer);
			if(!isCancelled())
				runnable.run();
		}
		else throw new UnsupportedOperationException("Event must be either cancellable or chronicled to trigger with a runnable");
	}
	
	protected void trigger(EventState state, Consumer<L> consumer) {
		Objects.requireNonNull(state);
		failsafeNotChronicled(state);
		
		if(state == EventState.DEFAULT && flags.contains(EventFlag.CHRONICLED)) {
			trigger(EventState.PRE, consumer);
			trigger(EventState.POST, consumer);
			return;
		}
		
		// active, state set, cancelled = false
		eventStack.push(cachedEventStackElement = new EventStackElement(state));
		queues.get(state).forEach((properties) -> {
			final L listener = properties.listener;
			final boolean ignore = properties.ignoreCancelled && cancelled();
			
			if(ignore) return;
			consumer.accept(listener);
		});
		cachedEventStackElement = eventStack.pop();
	}
	
	public void reflectionTrigger(EventState state, Object... params) {
		Objects.requireNonNull(state);
		// failsafeInactive();
		failsafeNotChronicled(state);
		
		if(state == EventState.DEFAULT && flags.contains(EventFlag.CHRONICLED)) {
			reflectionTrigger(EventState.PRE, params);
			reflectionTrigger(EventState.POST, params);
			return;
		}
		
		// active, state set, cancelled = false
		eventStack.push(cachedEventStackElement = new EventStackElement(state));
		queues.get(state).forEach((properties) -> {
			final L listener = properties.listener;
			final boolean ignore = properties.ignoreCancelled && cancelled();
			
			if(ignore) return;
			
			try {
				listener.getClass().getMethods()[0].invoke(listener, params);
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
				e.printStackTrace();
			}
		});
		cachedEventStackElement = eventStack.pop();
	}
	
	@Deprecated
	protected void failsafeInactive() {
		if(active())
			throw new UnsupportedOperationException("Event cannot be triggered while being triggered");
	}
	
	protected void failsafeActive() {
		if(!active())
			throw new UnsupportedOperationException("Event is not active");
	}
	
	protected void failsafeChronicled() {
		if(!flags.contains(EventFlag.CHRONICLED))
			throw new UnsupportedOperationException("Event is not chronicled");
	}
	
	protected void failsafeNotChronicled(EventState state) {
		if(state != EventState.DEFAULT && !flags.contains(EventFlag.CHRONICLED))
			throw new UnsupportedOperationException("Event is not chronicled");
	}
	
	protected void failsafeChronicled(EventState state) {
		failsafeNotChronicled(state);
		
		if(state == EventState.DEFAULT && flags.contains(EventFlag.CHRONICLED))
			throw new UnsupportedOperationException("Event is chronicled, you must bind to either PRE or POST");
	}
	
	protected void failsafeCancellable() {
		if(!active())
			throw new UnsupportedOperationException("Event is not active at this time");
		if(!flags.contains(EventFlag.CANCELLABLE))
			throw new UnsupportedOperationException("Event is not cancellable");
		if(getStackElement().state == EventState.POST)
			throw new UnsupportedOperationException("POST Events cannot be cancelled");
	}
	
	protected EventStackElement getStackElement() {
		return eventStack.peek();
	}
	
	public void cancel() {
		setCancelled(true);
	}
	
	public void setCancelled(boolean cancelled) {
		failsafeCancellable();
		cachedEventStackElement.cancelled = cancelled;
	}
	
	/**
	 * Use outside of a listener to determine if the event got cancelled
	 */
	public boolean isCancelled() {
		return cachedEventStackElement.cancelled;
	}
	
	/**
	 * Use inside of a listener to know if the event is set to be cancelled
	 */
	public boolean cancelled() {
		return getStackElement().cancelled;
	}
	
	public int depth() {
		return eventStack.size();
	}
	
	public boolean active() {
		return !eventStack.empty();
	}
	
	public static final class $ {}

	// --- generated ---

	public static class P0 extends Event <Listener.P0, $,$,$,$,$,$,$,$,$> {

		public static  P0  create(EventFlag... flags) {
			return new P0(flags);
		}

		public P0(EventFlag... flags) {
			super(flags);
		}

		public void trigger() {
			super.trigger((listener) -> listener.execute());
		}
		
		public void trigger(Runnable runnable) {
			super.trigger((listener) -> listener.execute(), runnable);
		}
		
		public void trigger(EventState state) {
			super.trigger(state, (listener) -> listener.execute());
		}
		
		public <EE> void redirect(Event.P1<EE> event, EE emitter) {
			super.redirect(event,
				getRedirectListener(EventState.DEFAULT, event, emitter),
				getRedirectListener(EventState.PRE, event, emitter),
				getRedirectListener(EventState.POST, event, emitter)
			);
		}
		
		protected <EE> Listener.P0 getRedirectListener(EventState state, Event.P1<EE> event, EE emitter) {
			return () -> {
				event.trigger(state, emitter);
				if(flags.contains(EventFlag.CANCELLABLE) && this.isCancelled() != event.isCancelled())
					this.setCancelled(event.isCancelled());
			};
		}
	}

	public static class P1<A> extends Event <Listener.P1<A>, A,$,$,$,$,$,$,$,$> {

		public static <A> P1 <A> create(EventFlag... flags) {
			return new P1<>(flags);
		}

		public P1(EventFlag... flags) {
			super(flags);
		}

		public void trigger(A a) {
			super.trigger((listener) -> listener.execute(a));
		}
		
		public void trigger(A a, Runnable runnable) {
			super.trigger((listener) -> listener.execute(a), runnable);
		}
		
		public void trigger(EventState state, A a) {
			super.trigger(state, (listener) -> listener.execute(a));
		}
		
		public <EE> void redirect(Event.P2<EE, A> event, EE emitter) {
			super.redirect(event,
				getRedirectListener(EventState.DEFAULT, event, emitter),
				getRedirectListener(EventState.PRE, event, emitter),
				getRedirectListener(EventState.POST, event, emitter)
			);
		}
		
		protected <EE> Listener.P1<A> getRedirectListener(EventState state, Event.P2<EE, A> event, EE emitter) {
			return (a) -> {
				event.trigger(state, emitter, a);
				if(flags.contains(EventFlag.CANCELLABLE) && this.isCancelled() != event.isCancelled())
					this.setCancelled(event.isCancelled());
			};
		}
	}

	public static class P2<A,B> extends Event <Listener.P2<A,B>, A,B,$,$,$,$,$,$,$> {

		public static <A,B> P2 <A,B> create(EventFlag... flags) {
			return new P2<>(flags);
		}

		public P2(EventFlag... flags) {
			super(flags);
		}

		public void trigger(A a, B b) {
			super.trigger((listener) -> listener.execute(a, b));
		}
		
		public void trigger(A a, B b, Runnable runnable) {
			super.trigger((listener) -> listener.execute(a, b), runnable);
		}
		
		public void trigger(EventState state, A a, B b) {
			super.trigger(state, (listener) -> listener.execute(a, b));
		}
		
		public <EE> void redirect(Event.P3<EE, A,B> event, EE emitter) {
			super.redirect(event,
				getRedirectListener(EventState.DEFAULT, event, emitter),
				getRedirectListener(EventState.PRE, event, emitter),
				getRedirectListener(EventState.POST, event, emitter)
			);
		}
		
		protected <EE> Listener.P2<A,B> getRedirectListener(EventState state, Event.P3<EE, A,B> event, EE emitter) {
			return (a, b) -> {
				event.trigger(state, emitter, a, b);
				if(flags.contains(EventFlag.CANCELLABLE) && this.isCancelled() != event.isCancelled())
					this.setCancelled(event.isCancelled());
			};
		}
	}

	public static class P3<A,B,C> extends Event <Listener.P3<A,B,C>, A,B,C,$,$,$,$,$,$> {

		public static <A,B,C> P3 <A,B,C> create(EventFlag... flags) {
			return new P3<>(flags);
		}

		public P3(EventFlag... flags) {
			super(flags);
		}

		public void trigger(A a, B b, C c) {
			super.trigger((listener) -> listener.execute(a, b, c));
		}
		
		public void trigger(A a, B b, C c, Runnable runnable) {
			super.trigger((listener) -> listener.execute(a, b, c), runnable);
		}
		
		public void trigger(EventState state, A a, B b, C c) {
			super.trigger(state, (listener) -> listener.execute(a, b, c));
		}
		
		public <EE> void redirect(Event.P4<EE, A,B,C> event, EE emitter) {
			super.redirect(event,
				getRedirectListener(EventState.DEFAULT, event, emitter),
				getRedirectListener(EventState.PRE, event, emitter),
				getRedirectListener(EventState.POST, event, emitter)
			);
		}
		
		protected <EE> Listener.P3<A,B,C> getRedirectListener(EventState state, Event.P4<EE, A,B,C> event, EE emitter) {
			return (a, b, c) -> {
				event.trigger(state, emitter, a, b, c);
				if(flags.contains(EventFlag.CANCELLABLE) && this.isCancelled() != event.isCancelled())
					this.setCancelled(event.isCancelled());
			};
		}
	}

	public static class P4<A,B,C,D> extends Event <Listener.P4<A,B,C,D>, A,B,C,D,$,$,$,$,$> {

		public static <A,B,C,D> P4 <A,B,C,D> create(EventFlag... flags) {
			return new P4<>(flags);
		}

		public P4(EventFlag... flags) {
			super(flags);
		}

		public void trigger(A a, B b, C c, D d) {
			super.trigger((listener) -> listener.execute(a, b, c, d));
		}
		
		public void trigger(A a, B b, C c, D d, Runnable runnable) {
			super.trigger((listener) -> listener.execute(a, b, c, d), runnable);
		}
		
		public void trigger(EventState state, A a, B b, C c, D d) {
			super.trigger(state, (listener) -> listener.execute(a, b, c, d));
		}
		
		public <EE> void redirect(Event.P5<EE, A,B,C,D> event, EE emitter) {
			super.redirect(event,
				getRedirectListener(EventState.DEFAULT, event, emitter),
				getRedirectListener(EventState.PRE, event, emitter),
				getRedirectListener(EventState.POST, event, emitter)
			);
		}
		
		protected <EE> Listener.P4<A,B,C,D> getRedirectListener(EventState state, Event.P5<EE, A,B,C,D> event, EE emitter) {
			return (a, b, c, d) -> {
				event.trigger(state, emitter, a, b, c, d);
				if(flags.contains(EventFlag.CANCELLABLE) && this.isCancelled() != event.isCancelled())
					this.setCancelled(event.isCancelled());
			};
		}
	}

	public static class P5<A,B,C,D,E> extends Event <Listener.P5<A,B,C,D,E>, A,B,C,D,E,$,$,$,$> {

		public static <A,B,C,D,E> P5 <A,B,C,D,E> create(EventFlag... flags) {
			return new P5<>(flags);
		}

		public P5(EventFlag... flags) {
			super(flags);
		}

		public void trigger(A a, B b, C c, D d, E e) {
			super.trigger((listener) -> listener.execute(a, b, c, d, e));
		}
		
		public void trigger(A a, B b, C c, D d, E e, Runnable runnable) {
			super.trigger((listener) -> listener.execute(a, b, c, d, e), runnable);
		}
		
		public void trigger(EventState state, A a, B b, C c, D d, E e) {
			super.trigger(state, (listener) -> listener.execute(a, b, c, d, e));
		}
		
		public <EE> void redirect(Event.P6<EE, A,B,C,D,E> event, EE emitter) {
			super.redirect(event,
				getRedirectListener(EventState.DEFAULT, event, emitter),
				getRedirectListener(EventState.PRE, event, emitter),
				getRedirectListener(EventState.POST, event, emitter)
			);
		}
		
		protected <EE> Listener.P5<A,B,C,D,E> getRedirectListener(EventState state, Event.P6<EE, A,B,C,D,E> event, EE emitter) {
			return (a, b, c, d, e) -> {
				event.trigger(state, emitter, a, b, c, d, e);
				if(flags.contains(EventFlag.CANCELLABLE) && this.isCancelled() != event.isCancelled())
					this.setCancelled(event.isCancelled());
			};
		}
	}

	public static class P6<A,B,C,D,E,F> extends Event <Listener.P6<A,B,C,D,E,F>, A,B,C,D,E,F,$,$,$> {

		public static <A,B,C,D,E,F> P6 <A,B,C,D,E,F> create(EventFlag... flags) {
			return new P6<>(flags);
		}

		public P6(EventFlag... flags) {
			super(flags);
		}

		public void trigger(A a, B b, C c, D d, E e, F f) {
			super.trigger((listener) -> listener.execute(a, b, c, d, e, f));
		}
		
		public void trigger(A a, B b, C c, D d, E e, F f, Runnable runnable) {
			super.trigger((listener) -> listener.execute(a, b, c, d, e, f), runnable);
		}
		
		public void trigger(EventState state, A a, B b, C c, D d, E e, F f) {
			super.trigger(state, (listener) -> listener.execute(a, b, c, d, e, f));
		}
		
		public <EE> void redirect(Event.P7<EE, A,B,C,D,E,F> event, EE emitter) {
			super.redirect(event,
				getRedirectListener(EventState.DEFAULT, event, emitter),
				getRedirectListener(EventState.PRE, event, emitter),
				getRedirectListener(EventState.POST, event, emitter)
			);
		}
		
		protected <EE> Listener.P6<A,B,C,D,E,F> getRedirectListener(EventState state, Event.P7<EE, A,B,C,D,E,F> event, EE emitter) {
			return (a, b, c, d, e, f) -> {
				event.trigger(state, emitter, a, b, c, d, e, f);
				if(flags.contains(EventFlag.CANCELLABLE) && this.isCancelled() != event.isCancelled())
					this.setCancelled(event.isCancelled());
			};
		}
	}

	public static class P7<A,B,C,D,E,F,G> extends Event <Listener.P7<A,B,C,D,E,F,G>, A,B,C,D,E,F,G,$,$> {

		public static <A,B,C,D,E,F,G> P7 <A,B,C,D,E,F,G> create(EventFlag... flags) {
			return new P7<>(flags);
		}

		public P7(EventFlag... flags) {
			super(flags);
		}

		public void trigger(A a, B b, C c, D d, E e, F f, G g) {
			super.trigger((listener) -> listener.execute(a, b, c, d, e, f, g));
		}
		
		public void trigger(A a, B b, C c, D d, E e, F f, G g, Runnable runnable) {
			super.trigger((listener) -> listener.execute(a, b, c, d, e, f, g), runnable);
		}
		
		public void trigger(EventState state, A a, B b, C c, D d, E e, F f, G g) {
			super.trigger(state, (listener) -> listener.execute(a, b, c, d, e, f, g));
		}
		
		public <EE> void redirect(Event.P8<EE, A,B,C,D,E,F,G> event, EE emitter) {
			super.redirect(event,
				getRedirectListener(EventState.DEFAULT, event, emitter),
				getRedirectListener(EventState.PRE, event, emitter),
				getRedirectListener(EventState.POST, event, emitter)
			);
		}
		
		protected <EE> Listener.P7<A,B,C,D,E,F,G> getRedirectListener(EventState state, Event.P8<EE, A,B,C,D,E,F,G> event, EE emitter) {
			return (a, b, c, d, e, f, g) -> {
				event.trigger(state, emitter, a, b, c, d, e, f, g);
				if(flags.contains(EventFlag.CANCELLABLE) && this.isCancelled() != event.isCancelled())
					this.setCancelled(event.isCancelled());
			};
		}
	}

	public static class P8<A,B,C,D,E,F,G,H> extends Event <Listener.P8<A,B,C,D,E,F,G,H>, A,B,C,D,E,F,G,H,$> {

		public static <A,B,C,D,E,F,G,H> P8 <A,B,C,D,E,F,G,H> create(EventFlag... flags) {
			return new P8<>(flags);
		}

		public P8(EventFlag... flags) {
			super(flags);
		}

		public void trigger(A a, B b, C c, D d, E e, F f, G g, H h) {
			super.trigger((listener) -> listener.execute(a, b, c, d, e, f, g, h));
		}
		
		public void trigger(A a, B b, C c, D d, E e, F f, G g, H h, Runnable runnable) {
			super.trigger((listener) -> listener.execute(a, b, c, d, e, f, g, h), runnable);
		}
		
		public void trigger(EventState state, A a, B b, C c, D d, E e, F f, G g, H h) {
			super.trigger(state, (listener) -> listener.execute(a, b, c, d, e, f, g, h));
		}
		
		public <EE> void redirect(Event.P9<EE, A,B,C,D,E,F,G,H> event, EE emitter) {
			super.redirect(event,
				getRedirectListener(EventState.DEFAULT, event, emitter),
				getRedirectListener(EventState.PRE, event, emitter),
				getRedirectListener(EventState.POST, event, emitter)
			);
		}
		
		protected <EE> Listener.P8<A,B,C,D,E,F,G,H> getRedirectListener(EventState state, Event.P9<EE, A,B,C,D,E,F,G,H> event, EE emitter) {
			return (a, b, c, d, e, f, g, h) -> {
				event.trigger(state, emitter, a, b, c, d, e, f, g, h);
				if(flags.contains(EventFlag.CANCELLABLE) && this.isCancelled() != event.isCancelled())
					this.setCancelled(event.isCancelled());
			};
		}
	}

	public static class P9<A,B,C,D,E,F,G,H,I> extends Event <Listener.P9<A,B,C,D,E,F,G,H,I>, A,B,C,D,E,F,G,H,I> {

		public static <A,B,C,D,E,F,G,H,I> P9 <A,B,C,D,E,F,G,H,I> create(EventFlag... flags) {
			return new P9<>(flags);
		}

		public P9(EventFlag... flags) {
			super(flags);
		}

		public void trigger(A a, B b, C c, D d, E e, F f, G g, H h, I i) {
			super.trigger((listener) -> listener.execute(a, b, c, d, e, f, g, h, i));
		}
		
		public void trigger(A a, B b, C c, D d, E e, F f, G g, H h, I i, Runnable runnable) {
			super.trigger((listener) -> listener.execute(a, b, c, d, e, f, g, h, i), runnable);
		}
		
		public void trigger(EventState state, A a, B b, C c, D d, E e, F f, G g, H h, I i) {
			super.trigger(state, (listener) -> listener.execute(a, b, c, d, e, f, g, h, i));
		}
	}
}

