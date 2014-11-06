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
    public static <T> T proxy(final T original) {
        Enhancer enhancer = new Enhancer();

        enhancer.setSuperclass(original.getClass());
        enhancer.setCallbackType(LazyLoadWithMethodInterceptor.class);

        //noinspection unchecked
        return createProxyInstanceWithoutCallingConstructor((Class<T>) enhancer.createClass(), original);
    }

    private static <T> T createProxyInstanceWithoutCallingConstructor(Class<T> proxyClass, T original) {
        LazyLoadWithMethodInterceptor<T> interceptor = new LazyLoadWithMethodInterceptor<T>(original);
        Enhancer.registerCallbacks(proxyClass, new Callback[] { interceptor });
        return ObjenesisHelper.newInstance(proxyClass);
    }

    private static final class LazyLoadWithMethodInterceptor<T> implements MethodInterceptor {
        private final T original;
        private final Set<String> calledLoaders = new HashSet<String>();

        private LazyLoadWithMethodInterceptor(T original) {
            this.original = original;
        }

        @Override
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy)
                throws Throwable
        {
            boolean isLazyLoaded = method.isAnnotationPresent(LazyLoadWith.class);

            if (isLazyLoaded) {
                LazyLoadWith lazyLoadWith = method.getAnnotation(LazyLoadWith.class);
                String loaderMethodName = lazyLoadWith.value();
                callLazyLoaderIfNeeded(loaderMethodName);
            }

            return methodProxy.invoke(original, args);
        }

        private void callLazyLoaderIfNeeded(String loaderMethodName)
                throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
        {
            if (!calledLoaders.contains(loaderMethodName)) {
                callLazyLoader(loaderMethodName);
            }
        }

        private void callLazyLoader(String loaderMethodName)
                throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
        {
            Method loaderMethod = original.getClass().getDeclaredMethod(loaderMethodName);
            loaderMethod.setAccessible(true);
            loaderMethod.invoke(original);
            calledLoaders.add(loaderMethodName);
        }
    }
}
