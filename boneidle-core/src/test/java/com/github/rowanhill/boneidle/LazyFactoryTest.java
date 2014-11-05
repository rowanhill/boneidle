package com.github.rowanhill.boneidle;

import com.github.rowanhill.boneidle.LazyFactory;
import com.github.rowanhill.boneidle.LazyLoadWith;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LazyFactoryTest {
    private MyClass original;
    private MyClass proxy;

    @Before
    public void setUp() {
        // given
        original = new MyClass();

        // when
        proxy = LazyFactory.proxy(original);
    }

    @Test
    public void lazyProxiesAreAssignableToProxiedType() {
        // (This is a bit redundant, given the compiler is happy with the above assignment)
        assertThat(proxy).isInstanceOf(MyClass.class);
    }

    @Test
    public void unannotatedMethodsAreUnaffected() {
        // when
        String unannotated = proxy.unannotatedMethod();

        // then
        assertThat(unannotated).isEqualTo(original.unannotatedMethod());
    }

    @Test
    public void lazyLoadWithAnnotationCallsLoaderMethodBeforeInvokingOriginalMethod() {
        // when
        String lazyLoadedString = proxy.getLazyLoadedString();

        // then
        assertThat(lazyLoadedString).isEqualTo(MyClass.LAZY_LOADED_STRING_CONTENT);
    }

    @Test
    public void loaderMethodIsOnlyInvokedOnceWhenLazyMethodIsInvokedMultipleTimes() {
        // when
        proxy.getLazyLoadedString();
        proxy.getLazyLoadedString();

        // then
        assertThat(original.lazyLoadCount).isEqualTo(1);
    }

    @Test
    public void loaderMethodIsOnlyInvokedOnceWhenMultipleLazyMethodsWithSameLoaderAreInvoked() {
        // when
        proxy.getLazyLoadedString();
        proxy.getOtherLazyLoadedString();

        // then
        assertThat(original.lazyLoadCount).isEqualTo(1);
    }

    static class MyClass {
        public static final String LAZY_LOADED_STRING_CONTENT = "This string was loaded lazily";
        private String lazyLoadedString = null;
        private String otherLazyLoadedString = null;
        private int lazyLoadCount = 0;

        String unannotatedMethod() {
            return "Unannotated methods should not be affected by proxying";
        }

        @LazyLoadWith("loadLazyLoadedString")
        String getLazyLoadedString() {
            return lazyLoadedString;
        }

        @LazyLoadWith("loadLazyLoadedString")
        String getOtherLazyLoadedString() {
            return otherLazyLoadedString;
        }

        @SuppressWarnings("UnusedDeclaration")
        private void loadLazyLoadedString() {
            lazyLoadCount++;
            lazyLoadedString = LAZY_LOADED_STRING_CONTENT;
            otherLazyLoadedString = "Other lazy loaded string";
        }
    }
}
