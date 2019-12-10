package io.github.gavar.mojo.validate;

import java.util.Collection;

/**
 * Provides set of validation methods same as {@link Validate},
 * but throwing error in form of describing some action like "unable to .... because ....".
 */
public class UnableTo {

    /**
     * Throw an exception when provided value is null.
     * @param action name of the action which unable to perform on check failure.
     * @param name   name of the argument.
     * @param value  argument value to check.
     * @throws IllegalArgumentException when check fails.
     */
    public static <T> void whenArgumentIsNull(String action, String name, T value) {
        final boolean NotNull = value != null;
        Preconditions.checkArgument(NotNull, "unable to '%s' because argument '%s' is null", action, name);
    }

    /**
     * Throw an exception when provided array is null or empty.
     * @param action name of the action which unable to perform on check failure.
     * @param name   name of the argument.
     * @param array  argument value to check.
     * @throws IllegalArgumentException when check fails.
     */
    public static <T> void whenArgumentIsEmpty(String action, String name, T[] array) {
        final boolean NotEmpty = array != null && array.length > 0;
        Preconditions.checkArgument(NotEmpty, "unable to '%s' because argument '%s' is empty", action, name);
    }

    /**
     * Throw an exception when provided char sequence is null or empty.
     * @param action name of the action which unable to perform on check failure.
     * @param name   name of the argument.
     * @param chars  argument value to check.
     * @throws IllegalArgumentException when check fails.
     */
    public static void whenArgumentIsEmpty(String action, String name, CharSequence chars) {
        final boolean NotEmpty = chars != null && chars.length() > 0;
        Preconditions.checkArgument(NotEmpty, "unable to '%s' because argument '%s' is empty", action, name);
    }

    /**
     * Throw an exception when provided collection is null or empty.
     * @param action     name of the action which unable to perform on check failure.
     * @param name       name of the argument.
     * @param collection argument value to check.
     * @throws IllegalArgumentException when check fails.
     */
    public static void whenArgumentIsEmpty(String action, String name, Collection collection) {
        final boolean NotEmpty = collection != null && !collection.isEmpty();
        Preconditions.checkArgument(NotEmpty, "unable to '%s' because argument '%s' is empty", action, name);
    }
}
