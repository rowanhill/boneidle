package com.github.rowanhill.boneidle.exception;

public abstract class LazyLoadingMisconfigurationRuntimeException extends RuntimeException {
    public LazyLoadingMisconfigurationRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
