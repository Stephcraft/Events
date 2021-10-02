package v2;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.function.Consumer;

public abstract class Event <L, A,B,C,D,E,F,G,H,I> {
	
	private boolean cancelled;
	private boolean cancellable;
	private boolean active;
	
	private static class ListenerProperties <L> {
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
	
	private Comparator<ListenerProperties<L>> comparator = new Comparator<ListenerProperties<L>>() {
		@Override
		public int compare(ListenerProperties<L> a, ListenerProperties<L> b) {
			return a.priority - b.priority;
		}
	};
	
	private Map<L, ListenerProperties<L>> listeners = new HashMap<>();
	private PriorityQueue<ListenerProperties<L>> queue = new PriorityQueue<>(comparator);
	
	public Event() {}
	
	public Event(boolean cancellable) {
		this.cancellable = cancellable;
	}
	
	public void bind(L listener, int priority, boolean ignoreCancelled) {
		ListenerProperties<L> properties = ListenerProperties.of(listener, priority, ignoreCancelled);
		listeners.put(listener, properties);
		queue.add(properties);
	}
	
	public void bind(L listener, int priority) {
		bind(listener, priority, false);
	}
	
	public void bind(L listener) {
		bind(listener, 0, false);
	}
	
	public void unbind(L listener) {
		queue.remove(listeners.remove(listener));
	}
	
	public void unbind() {
		listeners.clear();
	}
	
	public boolean bound(L listener) {
		return listeners.containsKey(listener);
	}
	
	public void reflectionTrigger(Object... params) {
		cancelled = false;
		
		active = true;
		queue.forEach((properties) -> {
			final L listener = properties.listener;
			final boolean ignore = properties.ignoreCancelled && cancelled;
			
			if(ignore) return;
			
			try {
				listener.getClass().getMethods()[0].invoke(listener, params);
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
				e.printStackTrace();
			}
		});
		active = false;
	}
	
	@SuppressWarnings("rawtypes")
	public static void flush(Object object) {
		for(Field field : object.getClass().getFields()) {
			if(field.canAccess(object)) {
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
	
	protected void trigger(Consumer<L> consumer) {
		cancelled = false;
		
		active = true;
		queue.forEach((properties) -> {
			final L listener = properties.listener;
			final boolean ignore = properties.ignoreCancelled && cancelled;
			
			if(ignore) return;
			consumer.accept(listener);
		});
		active = false;
	}
	
	protected void failsafeCancellable() {
		if(!active) throw new UnsupportedOperationException("Event is not active at this time");
		if(!cancellable) throw new UnsupportedOperationException("Event is not cancellable");
	}
	
	public void cancel() {
		failsafeCancellable();
		cancelled = true;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	public static final class $ {}

	// --- generated ---
	
	public static class P0 extends Event <Listener.P0, $,$,$,$,$,$,$,$,$> {
	
		public static P0 create() {
			return new P0();
		}
	
		public static P0 create(boolean cancellable) {
			return new P0(cancellable);
		}
	
		public P0() {}
	
		public P0(boolean cancellable) {
			super(cancellable);
		}
	
		public void trigger() {
			super.trigger((listener) -> listener.execute());
		}
	}
	
	public static class P1<A> extends Event <Listener.P1<A>, A,$,$,$,$,$,$,$,$> {
	
		public static <A> P1 <A> create() {
			return new P1<>();
		}
	
		public static <A> P1 <A> create(boolean cancellable) {
			return new P1<>(cancellable);
		}
	
		public P1() {}
	
		public P1(boolean cancellable) {
			super(cancellable);
		}
	
		public void trigger(A a) {
			super.trigger((listener) -> listener.execute(a));
		}
	}
	
	public static class P2<A,B> extends Event <Listener.P2<A,B>, A,B,$,$,$,$,$,$,$> {
	
		public static <A,B> P2 <A,B> create() {
			return new P2<>();
		}
	
		public static <A,B> P2 <A,B> create(boolean cancellable) {
			return new P2<>(cancellable);
		}
	
		public P2() {}
	
		public P2(boolean cancellable) {
			super(cancellable);
		}
	
		public void trigger(A a, B b) {
			super.trigger((listener) -> listener.execute(a, b));
		}
	}
	
	public static class P3<A,B,C> extends Event <Listener.P3<A,B,C>, A,B,C,$,$,$,$,$,$> {
	
		public static <A,B,C> P3 <A,B,C> create() {
			return new P3<>();
		}
	
		public static <A,B,C> P3 <A,B,C> create(boolean cancellable) {
			return new P3<>(cancellable);
		}
	
		public P3() {}
	
		public P3(boolean cancellable) {
			super(cancellable);
		}
	
		public void trigger(A a, B b, C c) {
			super.trigger((listener) -> listener.execute(a, b, c));
		}
	}
	
	public static class P4<A,B,C,D> extends Event <Listener.P4<A,B,C,D>, A,B,C,D,$,$,$,$,$> {
	
		public static <A,B,C,D> P4 <A,B,C,D> create() {
			return new P4<>();
		}
	
		public static <A,B,C,D> P4 <A,B,C,D> create(boolean cancellable) {
			return new P4<>(cancellable);
		}
	
		public P4() {}
	
		public P4(boolean cancellable) {
			super(cancellable);
		}
	
		public void trigger(A a, B b, C c, D d) {
			super.trigger((listener) -> listener.execute(a, b, c, d));
		}
	}
	
	public static class P5<A,B,C,D,E> extends Event <Listener.P5<A,B,C,D,E>, A,B,C,D,E,$,$,$,$> {
	
		public static <A,B,C,D,E> P5 <A,B,C,D,E> create() {
			return new P5<>();
		}
	
		public static <A,B,C,D,E> P5 <A,B,C,D,E> create(boolean cancellable) {
			return new P5<>(cancellable);
		}
	
		public P5() {}
	
		public P5(boolean cancellable) {
			super(cancellable);
		}
	
		public void trigger(A a, B b, C c, D d, E e) {
			super.trigger((listener) -> listener.execute(a, b, c, d, e));
		}
	}
	
	public static class P6<A,B,C,D,E,F> extends Event <Listener.P6<A,B,C,D,E,F>, A,B,C,D,E,F,$,$,$> {
	
		public static <A,B,C,D,E,F> P6 <A,B,C,D,E,F> create() {
			return new P6<>();
		}
	
		public static <A,B,C,D,E,F> P6 <A,B,C,D,E,F> create(boolean cancellable) {
			return new P6<>(cancellable);
		}
	
		public P6() {}
	
		public P6(boolean cancellable) {
			super(cancellable);
		}
	
		public void trigger(A a, B b, C c, D d, E e, F f) {
			super.trigger((listener) -> listener.execute(a, b, c, d, e, f));
		}
	}
	
	public static class P7<A,B,C,D,E,F,G> extends Event <Listener.P7<A,B,C,D,E,F,G>, A,B,C,D,E,F,G,$,$> {
	
		public static <A,B,C,D,E,F,G> P7 <A,B,C,D,E,F,G> create() {
			return new P7<>();
		}
	
		public static <A,B,C,D,E,F,G> P7 <A,B,C,D,E,F,G> create(boolean cancellable) {
			return new P7<>(cancellable);
		}
	
		public P7() {}
	
		public P7(boolean cancellable) {
			super(cancellable);
		}
	
		public void trigger(A a, B b, C c, D d, E e, F f, G g) {
			super.trigger((listener) -> listener.execute(a, b, c, d, e, f, g));
		}
	}
	
	public static class P8<A,B,C,D,E,F,G,H> extends Event <Listener.P8<A,B,C,D,E,F,G,H>, A,B,C,D,E,F,G,H,$> {
	
		public static <A,B,C,D,E,F,G,H> P8 <A,B,C,D,E,F,G,H> create() {
			return new P8<>();
		}
	
		public static <A,B,C,D,E,F,G,H> P8 <A,B,C,D,E,F,G,H> create(boolean cancellable) {
			return new P8<>(cancellable);
		}
	
		public P8() {}
	
		public P8(boolean cancellable) {
			super(cancellable);
		}
	
		public void trigger(A a, B b, C c, D d, E e, F f, G g, H h) {
			super.trigger((listener) -> listener.execute(a, b, c, d, e, f, g, h));
		}
	}
	
	public static class P9<A,B,C,D,E,F,G,H,I> extends Event <Listener.P9<A,B,C,D,E,F,G,H,I>, A,B,C,D,E,F,G,H,I> {
	
		public static <A,B,C,D,E,F,G,H,I> P9 <A,B,C,D,E,F,G,H,I> create() {
			return new P9<>();
		}
	
		public static <A,B,C,D,E,F,G,H,I> P9 <A,B,C,D,E,F,G,H,I> create(boolean cancellable) {
			return new P9<>(cancellable);
		}
	
		public P9() {}
	
		public P9(boolean cancellable) {
			super(cancellable);
		}
	
		public void trigger(A a, B b, C c, D d, E e, F f, G g, H h, I i) {
			super.trigger((listener) -> listener.execute(a, b, c, d, e, f, g, h, i));
		}
	}

}
