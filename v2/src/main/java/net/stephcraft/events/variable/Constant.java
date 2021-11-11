package net.stephcraft.events.variable;

public class Constant<T> {
	protected T value;
	
	protected Constant(T value) {
	    this.value = value;
	}
	
	public static <T> Constant<T> of(T value) {
		return new Constant<>(value);
	}
	
	public boolean isNull() {
		 return value == null;
	}
	 
	public T get() {
		 return value;
	}
}
