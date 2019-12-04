package dev.gavar.mojo.validate;

import java.util.Collection;
import java.util.Iterator;

import static dev.gavar.mojo.validate.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class Validate {

    /**
     * Ensure provided argument value is not {@code null}.
     * @param value - argument value to check.
     * @param name  - name of the argument.
     * @throws IllegalArgumentException when argument is {@literal null}.
     */
    public static <T> void argumentNotNull(T value, String name) {
        final boolean NotNull = value != null;
        checkArgument(NotNull, "argument '%s' cannot be null", name);
    }

    /**
     * Ensure provided array contains at least one element.
     * @param array - array to check.
     * @param name  - name of the argument.
     * @throws IllegalArgumentException when is null or empty.
     */
    public static <T> void argumentNotEmpty(T[] array, String name) {
        final boolean NotEmpty = array != null && array.length > 0;
        checkArgument(NotEmpty, "argument '%s' cannot be empty array", name);
    }

    /**
     * Ensure provided array contains at least one element.
     * @param array - array to check.
     * @param name  - name of the argument.
     * @throws IllegalArgumentException when is null or empty.
     */
    public static void argumentNotEmpty(byte[] array, String name) {
        final boolean NotEmpty = array != null && array.length > 0;
        checkArgument(NotEmpty, "argument '%s' cannot be empty array", name);
    }

    /**
     * Ensure provided collection contains at least one element.
     * @param collection - collection to check.
     * @param name       - name of the argument.
     * @throws IllegalArgumentException when is null or empty.
     */
    public static void argumentNotEmpty(Collection collection, String name) {
        final boolean NotEmpty = collection != null && !collection.isEmpty();
        checkArgument(NotEmpty, "argument '%s' cannot be empty collection", name);
    }

    /**
     * Ensure provided iterator contains at least one element.
     * @param iterator - iterator to check.
     * @param name     - name of the argument.
     * @throws IllegalArgumentException when is null or empty.
     */
    public static void argumentNotEmpty(Iterator iterator, String name) {
        final boolean NotEmpty = iterator != null && iterator.hasNext();
        checkArgument(NotEmpty, "argument '%s' cannot be empty iterator", name);
    }

    /**
     * Ensure provided char sequence contains at least one character.
     * @param chars - char sequence to check.
     * @param name  - name of the argument.
     * @throws IllegalArgumentException when character sequence is null or empty.
     */
    public static void argumentNotEmpty(CharSequence chars, String name) {
        final boolean NotEmpty = chars != null && chars.length() > 0;
        checkArgument(NotEmpty, "argument '%s' cannot be empty char sequence", name);
    }

    /**
     * Ensure provided char sequence contains at least one non-whitespace character.
     * @param chars - char sequence to check.
     * @param name  - name of the argument.
     * @throws IllegalArgumentException when character sequence is null, empty or blank.
     */
    public static void argumentNotBlank(CharSequence chars, String name) {
        checkArgument(!isBlank(chars), "argument '%s' cannot be empty char sequence", name);
    }

    /**
     * Ensure provided char sequence is not empty and contains only ASCII characters.
     * @param chars - char sequence to check.
     * @param name  - name of the argument.
     * @throws IllegalArgumentException when character sequence is empty or contains non-ASCII characters.
     */
    public static void argumentAscii(CharSequence chars, String name) {
        checkArgument(isAscii(chars), "argument '%s' should contain only ASCII characters", name);
    }

    /**
     * Ensure provided argument value is {@code null}.
     * @param value - argument value to check.
     * @param name  - name of the argument.
     * @throws IllegalArgumentException when argument is not {@literal null}.
     */
    public static <T> void argumentIsNull(T value, String name) {
        final boolean IsNull = value == null;
        checkArgument(IsNull, "argument '%s' must be null", name);
    }

    /**
     * Ensure provided argument value is {@code true}.
     * @param value - argument value to check.
     * @param name  - name of the argument.
     * @throws IllegalArgumentException when argument is {@literal false}.
     */
    public static void argumentIsTrue(boolean value, String name) {
        checkArgument(value, "argument '%s' must be true", name, value);
    }

    /**
     * Ensure provided argument value is {@code false}.
     * @param value - argument value to check.
     * @param name  - name of the argument.
     * @throws IllegalArgumentException when argument is {@literal true}.
     */
    public static void argumentIsFalse(boolean value, String name) {
        checkArgument(!value, "argument '%s' must be false", name);
    }

    /**
     * Ensure provided argument value is equal to other value.
     * @param value - argument value to check.
     * @param other - value to compare argument against.
     * @param name  - name of the argument.
     * @throws IllegalArgumentException when argument is not equal to other value.
     */
    public static <T> void argumentIsEqualTo(T value, Comparable<T> other, String name) {
        checkArgument(areEqual(value, other), "argument '%s' has to be equal to '%s', but was '%s'", name, other, value);
    }

    /**
     * Ensure provided argument value is not equal to other value.
     * @param value - argument value to check.
     * @param other - value to compare argument against.
     * @param name  - name of the argument.
     * @throws IllegalArgumentException when argument is equal to other value.
     */
    public static <T> void argumentNotEqualTo(T value, Comparable<T> other, String name) {
        checkArgument(!areEqual(value, other), "argument '%s' cannot be equal to '%s'", name, other);
    }

    /**
     * Ensure provided argument value is positive number.
     * @param value - argument value to check.
     * @param name  - name of the argument.
     * @throws IllegalArgumentException when argument is negative.
     */
    public static void argumentIsPositive(long value, String name) {
        final boolean IsPositive = value >= 0;
        checkArgument(IsPositive, "argument '%s' has to be positive, but was '%s'", name, value);
    }

    /**
     * Ensure provided argument value is negative number.
     * @param value - argument value to check.
     * @param name  - name of the argument.
     * @throws IllegalArgumentException when argument is positive.
     */
    public static void argumentIsNegative(long value, String name) {
        final boolean IsNegative = value < 0;
        checkArgument(IsNegative, "argument '%s' has to be positive, but was '%s'", name, value);
    }

    /**
     * Ensure provided argument value is instance of particular type.
     * @param value - argument value to check.
     * @param type  - type to check against.
     * @param name  - name of the argument.
     * @throws IllegalArgumentException when argument does not implement provided type.
     */
    public static <T> void argumentOfType(Object value, Class<T> type, String name) {
        final boolean IsOfType = type.isInstance(value);
        final Class valueType = value != null ? value.getClass() : null;
        checkArgument(IsOfType, "argument '%s' has to be instance of '%s', but was '%s'", name, type, valueType);
    }

    private static boolean isAscii(CharSequence cs) {
        if (cs == null)
            return false;

        final int size = cs.length();
        for (int i = 0; i < size; i++)
            if (cs.charAt(i) > Byte.MAX_VALUE)
                return false;

        return true;
    }

    private static <T> boolean areEqual(T a, Comparable<T> b) {
        if (a == null) return b == null;
        if (b == null) return false;
        return b.compareTo(a) == 0;
    }
}
