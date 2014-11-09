package com.github.rowanhill.boneidle;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

public class LoaderMethodResolver {
    public Method getLoaderFor(Method targetMethod) throws NoSuchMethodException {
        LazyLoadWith lazyLoadWith = getLoaderAnnotation(targetMethod);

        if (lazyLoadWith == null) {
            return null;
        }

        String loaderMethodName = lazyLoadWith.value();
        return targetMethod.getDeclaringClass().getDeclaredMethod(loaderMethodName);
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
