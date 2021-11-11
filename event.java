public static class PI<GENERICS> extends Event <Listener.PI<GENERICS>, GENERICDOLLARS> {

	public static <GENERICS> PI <GENERICS> create(EventFlag... flags) {
		return new PI<>(flags);
	}

	public PI(EventFlag... flags) {
		super(flags);
	}

	public void trigger(PARAMS) {
		super.trigger((listener) -> listener.execute(INPUTS));
	}
	
	public void trigger(PARAMS, Runnable runnable) {
		super.trigger((listener) -> listener.execute(INPUTS), runnable);
	}
	
	public void trigger(EventState state, PARAMS) {
		super.trigger(state, (listener) -> listener.execute(INPUTS));
	}
	
	public <EE> void redirect(Event.PII<EE, GENERICS> event, EE emitter) {
		super.redirect(event,
			getRedirectListener(EventState.DEFAULT, event, emitter),
			getRedirectListener(EventState.PRE, event, emitter),
			getRedirectListener(EventState.POST, event, emitter)
		);
	}
	
	protected <EE> Listener.PI<GENERICS> getRedirectListener(EventState state, Event.PII<EE, GENERICS> event, EE emitter) {
		return (INPUTS) -> {
			event.trigger(state, emitter, INPUTS);
			if(flags.contains(EventFlag.CANCELLABLE) && this.isCancelled() != event.isCancelled())
				this.setCancelled(event.isCancelled());
		};
	}
}