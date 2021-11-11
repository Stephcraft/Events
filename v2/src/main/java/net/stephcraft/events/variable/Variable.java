package net.stephcraft.events.variable;

import java.util.function.Function;

public class Variable<T> extends Constant<T> {
	protected final T defaultValue;
	
	protected Variable(T value) {
        super(value);
        this.defaultValue = value;
    }
    
    public static <T> Variable<T> of(T value) {
    	return new Variable<>(value);
    }
    
    public void apply(Function<T,T> function) {
    	value = function.apply(value);
    }
    
    public void set(T value) {
        this.value = value;
    }
    
    public boolean mutated() {
    	return value != defaultValue;
    }
    
    public T getDefault() {
    	return defaultValue;
    }
}