package dev.gavar.mojo.io;

import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkState;
import static dev.gavar.mojo.util.StringUtils.indexOf;
import static dev.gavar.mojo.util.StringUtils.regionMatches;
import static org.apache.commons.text.StringSubstitutor.*;

public class StringInterpolator extends ConfigurationInterpolator {

    private static final String END = DEFAULT_VAR_END;
    private static final String START = DEFAULT_VAR_START;
    private static final String DELIMITER = DEFAULT_VAR_DEFAULT;

    private static final int END_LENGTH = END.length();
    private static final int START_LENGTH = START.length();
    private static final int DELIMITER_LENGTH = DELIMITER.length();

    private boolean interpolating;
    private final StringBuilder sb = new StringBuilder();
    private final Map<String, String> visits = new LinkedHashMap<>();
    private final Map<String, String> cache = new HashMap<>();
    private final Function<String, Object> getProperty;

    public StringInterpolator(Function<String, Object> getProperty) {
        this.getProperty = getProperty;
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
            checkState(!interpolating, "recursive interpolation is not supported!");

            sb.setLength(0);
            sb.append(value, 0, to);

            try {
                interpolating = true;
                interpolate(sb, from);
            } finally {
                cache.clear();
                visits.clear();
                interpolating = false;
            }

            sb.append(value, to, value.length());
            value = sb.toString();
        }
        return value;
    }

    private void interpolate(final StringBuilder sb, int from) {
        // TODO: detect infinite loop
        for (int to = sb.indexOf(END, from);
             from >= 0 && to > from;
             to = sb.indexOf(END, from)) {

            // parse
            final String variable, fallback;
            int s = indexOf(sb, from + START_LENGTH, to, DELIMITER);
            if (s > 0) {
                variable = sb.substring(from + START_LENGTH, s);
                fallback = sb.substring(s + DELIMITER_LENGTH, to);
            } else {
                variable = sb.substring(from + START_LENGTH, to);
                fallback = null;
            }

            // resolve
            String value = resolve(variable, fallback);

            // fallback when resolves to self
            if (regionMatches(sb, from, value))
                value = fallback;

            // track variable visit
            visit(variable, value);

            // replace
            final int next;
            if (value == null) {
                next = to + END_LENGTH;
            } else {
                sb.replace(from, to + END_LENGTH, value);
                next = sb.indexOf(START, from);
            }

            // advance
            if (from != next) {
                // shorten future lookups
                if (next >= 0)
                    for (String visit : visits.keySet())
                        cache.put(visit, value);

                visits.clear();
                from = next;
            }
        }
    }

    private void visit(String variable, String value) {
        if (Objects.equals(visits.put(variable, value), value)) {
            final String path = String.join(" -> ", visits.keySet());
            final String text = "infinite interpolation loop: " + path + " -> " + variable;
            throw new IllegalArgumentException(text);
        }

        // update value
        cache.put(variable, value);
    }

    private String resolve(String variable, String fallback) {
        String value = cache.get(variable);
        if (value == null) {
            final Object property = getProperty.apply(variable);
            value = Objects.toString(property, fallback != null ? fallback : "");
            cache.put(variable, value);
        }
        return value;
    }
}
