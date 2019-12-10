package io.github.gavar.mojo.util;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class ArrayUtils {

    public static <T> void forEach(T[] array, Consumer<T> action) {
        requireNonNull(array);
        requireNonNull(action);
        for (T item : array)
            action.accept(item);
    }
}
