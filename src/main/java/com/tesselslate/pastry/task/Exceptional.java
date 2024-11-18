package com.tesselslate.pastry.task;

public class Exceptional<T> {
    private final T value;
    private final Exception error;

    public Exceptional(T value) {
        this.value = value;
        this.error = null;
    }

    public Exceptional(Exception error) {
        this.value = null;
        this.error = error;
    }

    public Exception getError() {
        return this.error;
    }

    public T getValue() {
        return this.value;
    }

    public boolean hasError() {
        return this.error != null;
    }

    public boolean hasValue() {
        return this.value != null;
    }
}
