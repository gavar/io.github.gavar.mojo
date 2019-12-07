package dev.gavar.mojo.release.util;

import java.util.List;

import static dev.gavar.mojo.validate.Preconditions.checkArgument;

public class ListUtils {

    public static <T> void addBefore(List<T> list, T before, T element) {
        final int index = list.indexOf(before);
        checkArgument(index >= 0, "unable to find element to insert before: %s", before);
        list.remove(element);
        list.add(index, element);
    }

    public static <T> void addAfter(List<T> list, T after, T element) {
        final int index = list.indexOf(after);
        checkArgument(index >= 0, "unable to find element to insert after: %s", after);
        list.remove(element);
        list.add(index + 1, element);
    }

    public static <T> void replace(List<T> list, T existing, T element) {
        final int index = list.indexOf(existing);
        checkArgument(index >= 0, "unable to find element to replace: %s", existing);
        list.set(index, element);
    }
}
