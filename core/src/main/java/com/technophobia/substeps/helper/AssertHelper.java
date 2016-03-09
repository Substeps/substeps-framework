package com.technophobia.substeps.helper;

/**
 * Helper that contains extracted methods from Assert from JUnit.
 */
public class AssertHelper {

    private AssertHelper() {
    }

    public static void fail(final Object message) {
        if (message == null) {
            throw new AssertionError();
        }

        throw new AssertionError(message);
    }

    public static void assertTrue(final String message, final boolean condition) {
        if (!condition) {
            fail(message);
        }
    }

    public static void assertTrue(final boolean condition) {
        assertTrue(null, condition);
    }

    public static void assertFalse(final String message, final boolean condition) {
        assertTrue(message, !condition);
    }

    public static void assertFalse(final boolean condition) {
        assertFalse(null, condition);
    }

    public static void assertNotNull(final String message, final Object object) {
        assertTrue(message, object != null);
    }

    public static void assertNotNull(final Object object) {
        assertNotNull(null, object);
    }
}