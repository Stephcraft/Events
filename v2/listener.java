interface PI<GENERICS> extends Listener {
	
	public default int count() {
		return INDEX;
	}
	
	public void execute(PARAMS);
}