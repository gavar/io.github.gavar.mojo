package dev.gavar.mojo.rc;

import dev.gavar.mojo.core.PropertySourceType;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.ConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.composite.ObjectWithFieldsConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

import static dev.gavar.mojo.util.GenericUtils.arrify;

public class PropertyOutputSet {

    public static PropertyOutputSet files(Path... paths) {
        return new PropertyOutputSet(Arrays.stream(paths).map(Path::toFile).toArray(File[]::new));
    }

    public static PropertyOutputSet files(String... paths) {
        return new PropertyOutputSet(Arrays.stream(paths).map(File::new).toArray(File[]::new));
    }

    public PropertyOutputSet(File... files) {
        this.files = files;
    }

    public PropertyOutputSet(PropertySourceType... targets) {
        this.targets = targets;
    }

    @Parameter
    private File[] files = {};
    public File[] getFiles() { return files; }
    public void setFiles(File[] files) { this.files = files; }
    public void setFile(File file) { this.files = arrify(file, File[]::new); }

    @Parameter
    private PropertySourceType[] targets = {};
    public PropertySourceType[] getTargets() { return targets; }
    public void setTargets(PropertySourceType[] targets) { this.targets = targets; }
    public void setTarget(PropertySourceType target) { this.targets = arrify(target, PropertySourceType[]::new); }

    /**
     * Rules for property inclusion when exporting to a target destination.
     * Includes all properties when empty.
     * Included properties may be further excluded by {@link #excludes}.
     */
    @Parameter
    private Pattern[] includes = {};
    public Pattern[] getIncludes() { return includes; }
    public void setIncludes(Pattern[] includes) { this.includes = includes; }
    public void setInclude(Pattern include) { this.includes = arrify(include, Pattern[]::new); }

    /**
     * Rules for property exclusion when exporting to a target destination.
     * Does not exclude any property when empty.
     */
    @Parameter
    private Pattern[] excludes = {};
    public Pattern[] getExcludes() { return excludes; }
    public void setExcludes(Pattern[] excludes) { this.excludes = excludes; }
    public void setExclude(Pattern exclude) { this.excludes = arrify(exclude, Pattern[]::new); }

    public Properties filter(Properties source) {
        Properties target = source;

        if (includes.length > 0 || excludes.length > 0) {
            target = new Properties();
            for (String name : source.stringPropertyNames())
                if (include(name, includes) && !exclude(name, excludes))
                    target.put(name, source.get(name));
        }

        return target;
    }

    static boolean include(String name, Pattern[] patterns) {
        if (patterns == null || patterns.length == 0)
            return true;

        for (Pattern pattern : patterns)
            if (pattern.matcher(name).matches())
                return true;

        return false;
    }

    static boolean exclude(String name, Pattern[] patterns) {
        if (patterns != null)
            for (Pattern pattern : patterns)
                if (pattern.matcher(name).matches())
                    return true;

        return false;
    }

    public static class Converter extends ObjectWithFieldsConverter {

        @Override
        public boolean canConvert(Class<?> type) {
            return PropertyFile.class.equals(type);
        }

        @Override
        public Object fromConfiguration(final ConverterLookup lookup, final PlexusConfiguration configuration,
                                        final Class<?> type, final Class<?> enclosingType, final ClassLoader loader,
                                        final ExpressionEvaluator evaluator, final ConfigurationListener listener) throws ComponentConfigurationException {
            String value = configuration.getValue();
            if (value != null) {
                ConfigurationConverter converter = lookup.lookupConverterForType(File.class);
                File file = (File) converter.fromConfiguration(lookup, configuration, File.class, enclosingType, loader, evaluator);
                return new PropertyOutputSet(file);
            }
            return super.fromConfiguration(lookup, configuration, type, enclosingType, loader, evaluator, listener);
        }
    }
}
