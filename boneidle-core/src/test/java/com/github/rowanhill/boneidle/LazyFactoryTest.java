package com.github.rowanhill.boneidle;

import com.github.rowanhill.boneidle.exception.CannotCreateLazyProxyRuntimeException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import pl.wkr.fluentrule.api.FluentExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class LazyFactoryTest {
    @Rule
    public final FluentExpectedException expectedException = FluentExpectedException.none();

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
        assertThat(original.lazyLoadStringCount).isEqualTo(1);
    }

    @Test
    public void loaderMethodIsOnlyInvokedOnceWhenMultipleLazyMethodsWithSameLoaderAreInvoked() {
        // when
        proxy.getLazyLoadedString();
        proxy.getOtherLazyLoadedString();

        // then
        assertThat(original.lazyLoadStringCount).isEqualTo(1);
    }

    @Test
    public void secondLoaderMethodNotInvokedForUnassociatedMethods() {
        // when
        proxy.getLazyLoadedString();

        // then
        assertThat(original.lazyLoadIntegerCount).isEqualTo(0);
    }

    @Test
    public void multipleLoaderMethodsInvokedOnceEachByMultipleUnassociatedLazyLoadedMethodInvocations() {
        // when
        proxy.getLazyLoadedString();
        proxy.getLazyLoadedString();
        proxy.getLazyLoadedInteger();
        proxy.getLazyLoadedInteger();

        // then
        assertThat(original.lazyLoadStringCount).isEqualTo(1);
        assertThat(original.lazyLoadIntegerCount).isEqualTo(1);
    }

    @Test
    public void proxiesForClassesWithNonTrivialConstructorsCanBeMade() {
        // given
        AwkwardConstructorClass awkwardOriginal = new AwkwardConstructorClass("dummy");

        // when
        LazyFactory.proxy(awkwardOriginal);
    }

    @Test
    public void makingProxiesForFinalClassesThrowsHelpfulException() {
        // given
        FinalClass finalClass = new FinalClass();

        // expect
        expectedException
                .expect(CannotCreateLazyProxyRuntimeException.class)
                .hasMessageContaining("Could not create a lazy-loading proxy")
                .hasMessageContaining("FinalClass");

        // when
        LazyFactory.proxy(finalClass);
    }

    private static class MyClass {
        public static final String LAZY_LOADED_STRING_CONTENT = "This string was loaded lazily";
        public static final Integer LAZY_LOADED_INTEGER_CONTENT = 123;

        private String lazyLoadedString = null;
        private String otherLazyLoadedString = null;

        private Integer lazyLoadedInteger = null;

        private int lazyLoadStringCount = 0;
        private int lazyLoadIntegerCount = 0;

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

        @LazyLoadWith("loadLazyLoadedInteger")
        Integer getLazyLoadedInteger() {
            return lazyLoadedInteger;
        }

        @SuppressWarnings("UnusedDeclaration")
        private void loadLazyLoadedString() {
            lazyLoadStringCount++;
            lazyLoadedString = LAZY_LOADED_STRING_CONTENT;
            otherLazyLoadedString = "Other lazy loaded string";
        }

        @SuppressWarnings("UnusedDeclaration")
        private void loadLazyLoadedInteger() {
            lazyLoadIntegerCount++;
            lazyLoadedInteger = LAZY_LOADED_INTEGER_CONTENT;
        }
    }

    private static class AwkwardConstructorClass {
        private AwkwardConstructorClass(@SuppressWarnings("UnusedParameters") String dummy) {}
    }

    private static final class FinalClass {}
}
