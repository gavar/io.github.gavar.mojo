package io.github.gavar.mojo.rc;

import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public abstract class AbstractOutput {
    /**
     * Rules for property inclusion when exporting to a target destination.
     * Includes all properties when empty.
     * Included properties may be further excluded by {@link #excludes}.
     */
    @Parameter
    private Pattern[] includes = {};
    public Pattern[] getIncludes() { return includes; }
    public void setIncludes(String[] includes) { this.includes = toPatterns(includes); }

    /**
     * Rules for property exclusion when exporting to a target destination.
     * Does not exclude any property when empty.
     */
    @Parameter
    private Pattern[] excludes = {};
    public Pattern[] getExcludes() { return excludes; }
    public void setExcludes(String[] excludes) { this.excludes = toPatterns(excludes); }

    /**
     * Property aliases to include into the output, where:
     * <ul>
     *   <li>key - name of the property to write to output</li>
     *   <li>value - name of the source key containing the value</li>
     * </ul>
     * To the output will be written only those properties that:
     * <ul>
     *   <li>present in source properties</li>
     *   <li>satisfy {@link #excludes} filtering</li>
     * </ul>
     */
    @Parameter
    protected Map<String, String> aliases;

    public Properties filter(Properties source) {
        Properties target = source;

        if (includes.length > 0 || excludes.length > 0) {
            target = new Properties();
            for (String name : source.stringPropertyNames())
                if (include(name, includes) && !exclude(name, excludes))
                    target.put(name, source.get(name));
        }

        if (aliases != null && !aliases.isEmpty()) {
            target = lazyClone(source, target);
            for (String key : aliases.keySet()) {
                String name = aliases.get(key);
                Object value = target.get(name);
                if (value != null && !exclude(name, excludes))
                    target.put(key, value);
            }
        }

        return target;
    }

    private static boolean include(String name, Pattern[] patterns) {
        return patterns == null
            || patterns.length == 0
            || find(name, patterns);
    }

    private static boolean exclude(String name, Pattern[] patterns) {
        return patterns != null
            && patterns.length > 0
            && find(name, patterns);
    }

    private static boolean find(String value, Pattern[] patterns) {
        for (Pattern pattern : patterns)
            if (pattern.matcher(value).find())
                return true;
        return false;
    }

    private static Properties lazyClone(Properties source, Properties target) {
        if (target == source) {
            target = new Properties();
            target.putAll(source);
        }
        return target;
    }

    private static Pattern[] toPatterns(String... strings) {
        return Arrays.stream(strings)
            .filter(StringUtils::isNotBlank)
            .map(Pattern::compile)
            .toArray(Pattern[]::new)
            ;
    }
}
