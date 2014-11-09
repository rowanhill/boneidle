package com.github.rowanhill.boneidle;

import com.github.rowanhill.boneidle.exception.CannotInvokeLazyLoaderRuntimeException;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

class LoaderMethodResolver {
    /**
     * Finds the loader method for a given target method
     *
     * @param targetMethod The method for which to find the loader method
     * @return The loader method for the given targetMethod, or null if no loader method is defined.
     * @throws CannotInvokeLazyLoaderRuntimeException Thrown if the loader method is missing or parameterised
     */
    Method getLoaderFor(Method targetMethod) {
        LazyLoadWith lazyLoadWith = getLoaderAnnotation(targetMethod);

        if (lazyLoadWith == null) {
            return null;
        }

        String loaderMethodName = lazyLoadWith.value();
        Method loaderMethod;
        try {
            loaderMethod = targetMethod.getDeclaringClass().getDeclaredMethod(loaderMethodName);
        } catch (NoSuchMethodException e) {
            throw CannotInvokeLazyLoaderRuntimeException.create(loaderMethodName, e);
        }
        return loaderMethod;
    }

    private LazyLoadWith getLoaderAnnotation(Method targetMethod) {
        if (targetMethod.isAnnotationPresent(ExcludeFromLazyLoading.class)) {
            return null;
        }

        LazyLoadWith annotation = getMethodAnnotation(targetMethod);
        if (annotation != null) {
            return annotation;
        }

        return getClassAnnotation(targetMethod);
    }

    private LazyLoadWith getMethodAnnotation(Method targetMethod) {
         return getAnnotation(targetMethod);
    }

    private LazyLoadWith getClassAnnotation(Method targetMethod) {
        Class<?> declaringClass = targetMethod.getDeclaringClass();
        return getAnnotation(declaringClass);
    }

    private LazyLoadWith getAnnotation(AnnotatedElement element) {
        return element.isAnnotationPresent(LazyLoadWith.class) ? element.getAnnotation(LazyLoadWith.class) : null;
    }
}
