public static class PI<GENERICS> extends Event <Listener.PI<GENERICS>, GENERICDOLLARS> {

	public static <GENERICS> PI <GENERICS> create() {
		return new PI<>();
	}

	public static <GENERICS> PI <GENERICS> create(boolean cancellable) {
		return new PI<>(cancellable);
	}

	public PI() {}

	public PI(boolean cancellable) {
		super(cancellable);
	}

	public void trigger(PARAMS) {
		super.trigger((listener) -> listener.execute(INPUTS));
	}
}