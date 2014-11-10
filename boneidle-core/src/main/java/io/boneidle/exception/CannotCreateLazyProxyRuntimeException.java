package io.boneidle.exception;

public class CannotCreateLazyProxyRuntimeException extends LazyLoadingMisconfigurationRuntimeException {
    private CannotCreateLazyProxyRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public static CannotCreateLazyProxyRuntimeException create(Object original, Throwable cause) {
        String className = original.getClass().getCanonicalName();
        String msg = String.format("Could not create a lazy-loading proxy for class %s", className);
        return new CannotCreateLazyProxyRuntimeException(msg, cause);
    }
}
