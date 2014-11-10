package io.boneidle;

import io.boneidle.exception.CannotCreateLazyProxyRuntimeException;
import io.boneidle.exception.CannotCreateObjectToProxyRuntimeException;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import org.objenesis.ObjenesisHelper;

import java.lang.reflect.Constructor;

public class LazyFactory {
    private static LazyFactory instance = null;

    private final LoaderMethodResolver loaderMethodResolver;

    /**
     * Creates a proxy wrapping the given object, adding lazy-loading logic according to annotations
     *
     * @param original The object to proxy
     * @param <T> The type of the object to proxy
     * @return A lazy-loading proxy wrapping original
     *
     * @throws CannotCreateLazyProxyRuntimeException if the object to proxy cannot be proxied by CGLIB
     */
    public static <T> T proxy(final T original) {
        return getFactory().createProxy(original);
    }

    /**
     * Convenience method to create a proxy for a given class.
     *
     * @see LazyFactory#proxy(Object)
     */
    public static <T> T proxy(final Class<T> originalClass)
    {
        try {
            Constructor<T> constructor = originalClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            T original = constructor.newInstance();
            return proxy(original);
        } catch (ReflectiveOperationException e) {
            throw CannotCreateObjectToProxyRuntimeException.create(originalClass, e);
        }
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

        Class<T> proxyClass = tryCreateClass(enhancer, original);

        return createProxyInstanceWithoutCallingConstructor(proxyClass, original);
    }

    private <T> Class<T> tryCreateClass(Enhancer enhancer, T original) {
        try {
            //noinspection unchecked
            return (Class<T>)enhancer.createClass();
        } catch (IllegalArgumentException e) {
            throw CannotCreateLazyProxyRuntimeException.create(original, e);
        }
    }

    private <T> T createProxyInstanceWithoutCallingConstructor(Class<T> proxyClass, T original) {
        LazyLoadWithMethodInterceptor<T> interceptor =
                new LazyLoadWithMethodInterceptor<T>(loaderMethodResolver, original);
        Enhancer.registerCallbacks(proxyClass, new Callback[] { interceptor });
        return ObjenesisHelper.newInstance(proxyClass);
    }
}
