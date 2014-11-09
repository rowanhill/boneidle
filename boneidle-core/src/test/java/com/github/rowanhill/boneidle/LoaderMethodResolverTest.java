package com.github.rowanhill.boneidle;

import com.github.rowanhill.boneidle.exception.CannotInvokeLazyLoaderRuntimeException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import pl.wkr.fluentrule.api.FluentExpectedException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

public class LoaderMethodResolverTest {
    @Rule
    public final FluentExpectedException expectedException = FluentExpectedException.none();

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

    @Test
    public void unannotatedMethodOnAnnotatedClassHasClassDefaultLoader() throws Exception {
        // given
        Method unannotatedMethod = DefaultedClass.class.getDeclaredMethod("getUnannotatedString");

        // when
        Method loaderMethod = resolver.getLoaderFor(unannotatedMethod);

        // then
        assertThat(loaderMethod).isNotNull();
        assertThat(loaderMethod.getName()).isEqualTo("defaultLoad");
    }

    @Test
    public void annotatedMethodOnAnnotatedClassUsesMethodLoader() throws Exception {
        // given
        Method unannotatedMethod = DefaultedClass.class.getDeclaredMethod("getAnnotatedString");

        // when
        Method loaderMethod = resolver.getLoaderFor(unannotatedMethod);

        // then
        assertThat(loaderMethod).isNotNull();
        assertThat(loaderMethod.getName()).isEqualTo("load");
    }

    @Test
    public void excludedMethodOnDefaultedClassHasNoLoader() throws Exception {
        // given
        Method excludedMethod = DefaultedClass.class.getDeclaredMethod("getExcludedString");

        // when
        Method loaderMethod = resolver.getLoaderFor(excludedMethod);

        // then
        assertThat(loaderMethod).isNull();
    }

    @Test
    public void specifyingMissingLoaderMethodThrowsHelpfulException() throws Exception {
        // given
        Method missingLoaderMethod = MisconfiguredClass.class.getDeclaredMethod("getMissingLoaderString");

        // expect
        expectedException
                .expect(CannotInvokeLazyLoaderRuntimeException.class)
                .hasMessageContaining("missingLoader")
                .hasMessageContaining("Ensure it exists and takes no parameters");

        // when
        resolver.getLoaderFor(missingLoaderMethod);
    }

    @Test
    public void specifyingParameterisedLoaderMethodThrowsHelpfulException() throws Exception {
        // given
        Method missingLoaderMethod = MisconfiguredClass.class.getDeclaredMethod("getParameterisedLoaderString");

        // expect
        expectedException
                .expect(CannotInvokeLazyLoaderRuntimeException.class)
                .hasMessageContaining("loaderWithParameters")
                .hasMessageContaining("Ensure it exists and takes no parameters");

        // when
        resolver.getLoaderFor(missingLoaderMethod);
    }

    private static class SimpleClass {
        String getUnannotatedString() { return null; }

        @LazyLoadWith("load") String getAnnotatedString() { return null; }

        @SuppressWarnings("UnusedDeclaration")
        private void load() {}
    }

    @LazyLoadWith("defaultLoad")
    private static class DefaultedClass {
        String getUnannotatedString() { return null; }

        @LazyLoadWith("load") String getAnnotatedString() { return null; }

        @ExcludeFromLazyLoading String getExcludedString() { return null; }

        @SuppressWarnings("UnusedDeclaration")
        private void defaultLoad() {}

        @SuppressWarnings("UnusedDeclaration")
        private void load() {}
    }

    private static class MisconfiguredClass {
        @LazyLoadWith("missingLoader") String getMissingLoaderString() { return null; }

        @LazyLoadWith("loaderWithParameters") String getParameterisedLoaderString() { return null; }

        @SuppressWarnings("UnusedDeclaration")
        private void loaderWithParameters(String dummy) {}
    }
}