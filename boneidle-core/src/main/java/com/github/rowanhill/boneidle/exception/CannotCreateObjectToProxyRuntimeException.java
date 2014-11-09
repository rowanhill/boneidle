package com.github.rowanhill.boneidle.exception;

public class CannotCreateObjectToProxyRuntimeException extends LazyLoadingMisconfigurationRuntimeException {
    private CannotCreateObjectToProxyRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public static CannotCreateObjectToProxyRuntimeException create(Class<?> classToProxy, Throwable cause) {
        String msg = String.format("Could not create object to proxy for class %s", classToProxy.getCanonicalName());
        return new CannotCreateObjectToProxyRuntimeException(msg, cause);
    }
}
