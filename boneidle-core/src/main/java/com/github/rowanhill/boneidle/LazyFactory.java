package com.github.rowanhill.boneidle;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.objenesis.ObjenesisHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class LazyFactory {
    private static LazyFactory instance = null;

    private final LoaderMethodResolver loaderMethodResolver;

    public static <T> T proxy(final T original) {
        return getFactory().createProxy(original);
    }

    /**
     * Get a lazy-loaded singleton instance of the factory.
     *
     * This is for two reasons:
     *   a) It allows us to inject dependencies to the factory (with this method as the top level 'container')
     *   b) Lazily loading the lazy-loader factory is pleasingly meta.
     */
    private static LazyFactory getFactory() {
        if (instance == null) {
            instance = new LazyFactory(
                    new LoaderMethodResolver()
            );
        }
        return instance;
    }

    private LazyFactory(LoaderMethodResolver loaderMethodResolver) {
        this.loaderMethodResolver = loaderMethodResolver;
    }

    private <T> T createProxy(final T original) {
        Enhancer enhancer = new Enhancer();

        enhancer.setSuperclass(original.getClass());
        enhancer.setCallbackType(LazyLoadWithMethodInterceptor.class);

        //noinspection unchecked
        return createProxyInstanceWithoutCallingConstructor((Class<T>) enhancer.createClass(), original);
    }

    private <T> T createProxyInstanceWithoutCallingConstructor(Class<T> proxyClass, T original) {
        LazyLoadWithMethodInterceptor<T> interceptor =
                new LazyLoadWithMethodInterceptor<T>(loaderMethodResolver, original);
        Enhancer.registerCallbacks(proxyClass, new Callback[] { interceptor });
        return ObjenesisHelper.newInstance(proxyClass);
    }

    private static final class LazyLoadWithMethodInterceptor<T> implements MethodInterceptor {
        private final LoaderMethodResolver loaderMethodResolver;
        private final T original;
        private final Set<String> calledLoaders = new HashSet<String>();

        private LazyLoadWithMethodInterceptor(LoaderMethodResolver loaderMethodResolver, T original) {
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
}
