package dev.gavar.mojo.util;

public class GenericUtils {
    /**
     * Use default value when provided value is null.
     * @param value        - value to check.
     * @param defaultValue - default value to use if provided value is null.
     */
    public static <T> T valueOrDefault(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    /** From the provided array pick first that is not empty. */
    @SafeVarargs
    public static <T> T[] firstNonEmpty(T[]... arrays) {
        for (T[] array : arrays)
            if (array != null && array.length > 0)
                return array;
        return null;
    }
}
