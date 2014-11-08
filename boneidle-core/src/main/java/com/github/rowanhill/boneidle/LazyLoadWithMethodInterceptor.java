package com.github.rowanhill.boneidle;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * CGLib interceptor that invokes defined lazy loaded methods before invoking the target method, if necessary
 *
 * @param <T> The type of the object being proxied
 */
final class LazyLoadWithMethodInterceptor<T> implements MethodInterceptor {
    private final LoaderMethodResolver loaderMethodResolver;
    private final T original;
    private final Set<String> calledLoaders = new HashSet<String>();

    LazyLoadWithMethodInterceptor(LoaderMethodResolver loaderMethodResolver, T original) {
        this.loaderMethodResolver = loaderMethodResolver;
        this.original = original;
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy)
            throws Throwable
    {
        Method loaderMethod = loaderMethodResolver.getLoaderFor(method);

        if (loaderMethod != null) {
            callLazyLoaderIfNeeded(loaderMethod);
        }

        return methodProxy.invoke(original, args);
    }

    private void callLazyLoaderIfNeeded(Method loaderMethod)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        if (!calledLoaders.contains(loaderMethod.getName())) {
            callLazyLoader(loaderMethod);
        }
    }

    private void callLazyLoader(Method loaderMethod)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        loaderMethod.setAccessible(true);
        loaderMethod.invoke(original);
        calledLoaders.add(loaderMethod.getName());
    }
}
