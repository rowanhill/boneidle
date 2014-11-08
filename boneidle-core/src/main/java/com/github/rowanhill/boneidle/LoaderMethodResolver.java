package com.github.rowanhill.boneidle;

import java.lang.reflect.Method;

public class LoaderMethodResolver {
    public Method getLoaderFor(Method targetMethod) throws NoSuchMethodException {
        boolean isLazyLoaded = targetMethod.isAnnotationPresent(LazyLoadWith.class);

        if (isLazyLoaded) {
            LazyLoadWith lazyLoadWith = targetMethod.getAnnotation(LazyLoadWith.class);
            String loaderMethodName = lazyLoadWith.value();
            return targetMethod.getDeclaringClass().getDeclaredMethod(loaderMethodName);
        }

        return null;
    }
}
