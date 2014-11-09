package com.github.rowanhill.boneidle.exception;

public class CannotInvokeLazyLoaderRuntimeException extends LazyLoadingMisconfigurationRuntimeException {
    private CannotInvokeLazyLoaderRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public static CannotInvokeLazyLoaderRuntimeException create(String loaderMethodName, Throwable cause) {
        String msg = String.format(
                "Cannot use the specified lazy loader method '%s'. Ensure it exists and takes no parameters.",
                loaderMethodName
        );
        return new CannotInvokeLazyLoaderRuntimeException(msg, cause);
    }
}
