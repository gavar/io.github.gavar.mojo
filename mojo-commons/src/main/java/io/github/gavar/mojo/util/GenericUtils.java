package io.github.gavar.mojo.util;

import java.util.Properties;
import java.util.function.IntFunction;

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

    /**
     * Create array having a single value.
     * @param value     - value to wrap as array.
     * @param generator - array constructor.
     * @return array containing a value when it's not null; otherwise an empty array.
     */
    public static <T> T[] arrify(T value, IntFunction<T[]> generator) {
        if (value == null)
            return generator.apply(0);

        T[] array = generator.apply(1);
        array[0] = value;
        return array;
    }

    public static void putNonNull(Properties properties, Object key, Object value) {
        if (value != null)
            properties.put(key, value);
    }
}
