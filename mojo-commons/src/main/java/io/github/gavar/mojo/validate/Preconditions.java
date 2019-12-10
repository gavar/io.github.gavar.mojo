package io.github.gavar.mojo.validate;

/**
 * Assertion utility providing set of methods for validating program input.
 * These methods generally accept a{@code boolean} expression which is expected to be {@code true}.
 */
public class Preconditions {

    private static ThreadLocal<StringBuilder> threadLocalStringBuilder = ThreadLocal.withInitial(StringBuilder::new);

    /**
     * Ensures the truth of an expression.
     * @param expression - expression to check.
     * @throws IllegalArgumentException when expression evaluates to {@literal false}.
     */
    public static void checkArgument(boolean expression) {
        if (expression) return;
        throw new IllegalArgumentException();
    }

    /**
     * Ensures the truth of an expression.
     * @param expression - expression to check.
     * @param error      - exception message to use if the check fails.
     * @throws IllegalArgumentException when expression evaluates to {@literal false}.
     */
    public static void checkArgument(boolean expression, Object error) {
        if (expression) return;
        String message = error != null ? error.toString() : null;
        throw new IllegalArgumentException(message);
    }

    /**
     * Ensures the truth of an expression.
     * @param expression - expression to check.
     * @param format     - exception message format string.
     * @throws IllegalArgumentException when expression evaluates to {@literal false}.
     */
    public static void checkArgument(boolean expression, String format, Object $1) {
        if (expression) return;
        String message = format(format, $1);
        throw new IllegalArgumentException(message);
    }

    /**
     * Ensures the truth of an expression.
     * @param expression - expression to check.
     * @param format     - exception message format string.
     * @throws IllegalArgumentException when expression evaluates to {@literal false}.
     */
    public static void checkArgument(boolean expression, String format, Object $1, Object $2) {
        if (expression) return;
        String message = format(format, $1, $2);
        throw new IllegalArgumentException(message);
    }

    /**
     * Ensures the truth of an expression.
     * @param expression - expression to check.
     * @param format     - exception message format string.
     * @throws IllegalArgumentException when expression evaluates to {@literal false}.
     */
    public static void checkArgument(boolean expression, String format, Object $1, Object $2, Object $3) {
        if (expression) return;
        String message = format(format, $1, $2, $3);
        throw new IllegalArgumentException(message);
    }

    /**
     * Ensures the truth of an expression.
     * @param expression - expression to check.
     * @param format     - exception message format string.
     * @param args       - arguments to use for substituting literals in format message.
     * @throws IllegalArgumentException when expression evaluates to {@literal false}.
     */
    public static void checkArgument(boolean expression, String format, Object... args) {
        if (expression) return;
        String message = format(format, args);
        throw new IllegalArgumentException(message);
    }

    public static String format(String message, Object... args) {
        if (message == null) return null;
        if (args == null || args.length < 1) return message;
        StringBuilder sb = threadLocalStringBuilder.get();
        try {
            sb.ensureCapacity(message.length() + 1);
            int offset = 0;
            for (int i = 0; i < args.length; i++) {
                int index = message.indexOf("%s", offset);
                if (index == -1) break;
                sb.append(message, offset, index);
                sb.append(args[i++]);
                offset = index + 2;
            }
            sb.append(message, offset, message.length());
            return sb.toString();
        } finally {
            sb.setLength(0);
        }
    }
}
