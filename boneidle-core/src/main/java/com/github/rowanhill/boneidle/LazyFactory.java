package com.github.rowanhill.boneidle;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import org.objenesis.ObjenesisHelper;

public class LazyFactory {
    private static LazyFactory instance = null;

    private final LoaderMethodResolver loaderMethodResolver;

    /**
     * Creates a proxy wrapping the given object, adding lazy-loading logic according to annotations
     *
     * @param original The object to proxy
     * @param <T> The type of the object to proxy
     * @return A lazy-loading proxy wrapping original
     */
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
}
