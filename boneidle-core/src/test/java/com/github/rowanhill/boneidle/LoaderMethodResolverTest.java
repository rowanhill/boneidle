package com.github.rowanhill.boneidle;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

public class LoaderMethodResolverTest {
    private LoaderMethodResolver resolver;

    @Before
    public void setUp() {
        resolver = new LoaderMethodResolver();
    }

    @Test
    public void unannotatedMethodHasNoLoader() throws Exception {
        // given
        Method unannotatedMethod = SimpleClass.class.getDeclaredMethod("getUnannotatedString");

        // when
        Method loaderMethod = resolver.getLoaderFor(unannotatedMethod);

        // then
        assertThat(loaderMethod).isNull();
    }

    @Test
    public void annotatedMethodHasSpecifiedLoader() throws Exception {
        // given
        Method annotatedMethod = SimpleClass.class.getDeclaredMethod("getAnnotatedString");

        // when
        Method loaderMethod = resolver.getLoaderFor(annotatedMethod);

        // then
        assertThat(loaderMethod).isNotNull();
        assertThat(loaderMethod.getName()).isEqualTo("load");
    }

    private static class SimpleClass {
        String getUnannotatedString() { return null; }

        @LazyLoadWith("load") String getAnnotatedString() { return null; }

        @SuppressWarnings("UnusedDeclaration")
        private void load() {}
    }
}