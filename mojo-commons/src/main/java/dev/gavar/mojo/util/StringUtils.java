package dev.gavar.mojo.util;

public class StringUtils {

    public static int indexOf(final CharSequence cs, final int from, final int to, final char value) {
        for (int i = from; i < to; i++)
            if (cs.charAt(i) == value)
                return i;
        return -1;
    }

    public static int indexOf(final CharSequence chars, int from, final int to, final CharSequence value) {
        final char c = value.charAt(0);
        final int length = value.length();
        final int last = to - length;

        for (int i = from; i >= 0 && i < last; i = indexOf(chars, i + 1, last, c))
            if (regionMatches(chars, i, value, 0, length))
                return i;

        return -1;
    }

    public static boolean regionMatches(final CharSequence chars, final int from,
                                        final CharSequence value, final int offset, final int length) {
        for (int i = 0; i < length; i++)
            if (chars.charAt(from + i) != value.charAt(offset + i))
                return false;

        return true;
    }
}
