package v2;

import java.util.function.Function;

public class Variable<T> {
    private T value;

    private Variable(T value) {
        this.value = value;
    }
    
    public static <T> Variable<T> of(T value) {
    	return new Variable<>(value);
    }
    
    public void apply(Function<T,T> function) {
    	value = function.apply(value);
    }
    
    public boolean isNull() {
    	return value == null;
    }
    
    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }
}
