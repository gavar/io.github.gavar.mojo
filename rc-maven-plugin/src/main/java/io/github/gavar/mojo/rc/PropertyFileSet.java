package io.github.gavar.mojo.rc;

import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.ConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.composite.ObjectWithFieldsConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class PropertyFileSet {

    static PropertyFileSet files(String... files) {
        return new PropertyFileSet(Arrays.stream(files)
            .map(PropertyFile::new)
            .toArray(PropertyFile[]::new));
    }

    /** List of properties files locations. */
    protected PropertyFile[] files = {};
    public PropertyFile[] getFiles() { return files; }
    public void setFiles(PropertyFile[] files) { this.files = files; }
    public void setLocation(PropertyFile location) { this.files = new PropertyFile[]{location}; }

    /**
     * Whether {@link #files} should be modular by default.
     * Overrides value of the {@link RCPropertiesMojo#modular}.
     */
    @Parameter
    protected Boolean modular;
    public Boolean getModular() { return modular; }
    public void setModular(Boolean modular) { this.modular = modular; }

    /** Overrides value of the {@link RCPropertiesMojo#variants}. */
    protected String[] variants;
    public String[] getVariants() { return variants; }
    public void setVariant(String variant) { this.variants = new String[]{variant}; }
    public void setVariants(String[] variants) { this.variants = variants; }

    /** Overrides value of the {@link RCPropertiesMojo#extensions}. */
    protected String[] extensions;
    public String[] getExtensions() { return extensions; }
    public void setExtension(String extension) { this.extensions = new String[]{extension}; }
    public void setExtensions(String[] extensions) { this.extensions = extensions; }

    public PropertyFileSet() { }
    public PropertyFileSet(PropertyFile[] files) {
        this(files, null, null);
    }
    public PropertyFileSet(PropertyFile[] files, String[] extensions) {
        this(files, extensions, null);
    }
    public PropertyFileSet(@Nonnull PropertyFile[] files, @Nullable String[] extensions, @Nullable String[] variants) {
        this.files = files;
        this.variants = variants;
        this.extensions = extensions;
    }

    public static class Converter extends ObjectWithFieldsConverter {

        @Override
        public boolean canConvert(Class<?> type) {
            return PropertyFileSet.class.equals(type);
        }

        @Override
        public Object fromConfiguration(final ConverterLookup lookup, final PlexusConfiguration configuration,
                                        final Class<?> type, final Class<?> enclosingType, final ClassLoader loader,
                                        final ExpressionEvaluator evaluator, final ConfigurationListener listener) throws ComponentConfigurationException {
            if (configuration.getValue() != null) {
                ConfigurationConverter converter = lookup.lookupConverterForType(String.class);
                String value = (String) converter.fromConfiguration(lookup, configuration, String.class, enclosingType, loader, evaluator);
                return files(value);
            }
            return super.fromConfiguration(lookup, configuration, type, enclosingType, loader, evaluator, listener);
        }
    }
}
