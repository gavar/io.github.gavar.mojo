package dev.gavar.mojo.io;

import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static dev.gavar.mojo.util.StringUtils.indexOf;
import static org.apache.commons.text.StringSubstitutor.*;

public class StringInterpolator extends ConfigurationInterpolator {

    private static final String END = DEFAULT_VAR_END;
    private static final String START = DEFAULT_VAR_START;
    private static final String DELIMITER = DEFAULT_VAR_DEFAULT;

    private static final int END_LENGTH = END.length();
    private static final int START_LENGTH = START.length();
    private static final int DELIMITER_LENGTH = DELIMITER.length();

    private final Function<String, Object> getProperty;

    public StringInterpolator(Function<String, Object> getProperty) {
        this.getProperty = getProperty;
    }

    private String lookup(String key, String fallback) {
        Object value = getProperty.apply(key);
        return Objects.toString(value, fallback);
    }

    @Override
    public Object interpolate(Object value) {
        return value instanceof String
                ? interpolate((String) value)
                : value;
    }

    private String interpolate(String value) {
        return interpolate(value, value.indexOf(START), value.lastIndexOf(END) + 1);
    }

    private String interpolate(String value, int from, int to) {
        if (from >= 0 && to > from + START_LENGTH) {
            StringBuilder sb = new StringBuilder();
            sb.append(value, 0, to);
            interpolate(sb, from);
            sb.append(value, to, value.length());
            value = sb.toString();
        }
        return value;
    }

    private void interpolate(final StringBuilder sb, int from) {
        // TODO: detect infinite loop
        String variable, fallback;
        for (int to = sb.indexOf(END, from);
             from >= 0 && to > from;
             to = sb.indexOf(END, from)) {

            // parse
            int s = indexOf(sb, from + START_LENGTH, to, DELIMITER);
            if (s > 0) {
                variable = sb.substring(from + START_LENGTH, s);
                fallback = sb.substring(s + DELIMITER_LENGTH, to);
            } else {
                variable = sb.substring(from + START_LENGTH, to);
                fallback = null;
            }

            // resolve
            String value = lookup(variable, fallback);
            if (value == null || equals(variable, fallback, value))
                value = fallback;

            // replace
            if (value == null) {
                from = to + END_LENGTH;
            } else {
                sb.replace(from, to + END_LENGTH, value);
                from = sb.indexOf(START, from);
            }
        }
    }

    private static boolean equals(String variable, String fallback, String value) {
        return value != null
                && isVariable(value)
                && sizeMatches(variable, fallback, value)
                && variableMatches(variable, value)
                && fallbackMatches(variable.length(), fallback, value);
    }

    private static boolean isVariable(String value) {
        return value.startsWith(START)
                && value.endsWith(END);
    }

    private static boolean sizeMatches(String variable, String fallback, String value) {
        return value.length() == START_LENGTH
                + variable.length()
                + (fallback != null ? fallback.length() + DELIMITER_LENGTH : 0)
                + END_LENGTH;
    }

    private static boolean variableMatches(String variable, String value) {
        return value.regionMatches(START_LENGTH, variable, 0, variable.length());
    }

    private static boolean fallbackMatches(int from, String fallback, String value) {
        return value.regionMatches(START_LENGTH + from, DELIMITER, 0, DELIMITER_LENGTH)
                && value.regionMatches(START_LENGTH + from + DELIMITER_LENGTH, fallback, 0, fallback.length());
    }
}
