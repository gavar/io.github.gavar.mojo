package io.github.gavar.mojo.rc;

import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.ConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.composite.ObjectWithFieldsConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkState;
import static io.github.gavar.mojo.io.PathInfo.hasExtension;

/** Represents search location of the properties file. */
public class PropertyFile {

    /**
     * Path to a property file or a search location.
     * May be one of:
     * <ul>
     *     <li>name of the file to search starting from project directory</li>
     *     <li>absolute path to a particular file</li>
     *     <li>{@link URL}</li>
     * </ul>
     */
    @Parameter
    private String path;
    public String getPath() { return path; }
    public void setPath(String path) {
        try {
            this.update(path, new URL(path), modular, true);
        } catch (MalformedURLException e) {
            this.update(path, null, modular, Path.of(path).isAbsolute());
        }
    }

    /**
     * Whether to look for default values in nearest ancestor, or user's home directory.
     * Overrides value of the {@link PropertyFileSet#modular}.
     * Works only for relative paths.
     */
    @Parameter
    protected Boolean modular;
    public Boolean getModular() { return modular; }
    public void setModular(Boolean modular) { this.update(path, url, modular, absolute); }

    /** Whether {@link #path} is an absolute path or URL. */
    private boolean absolute;
    public boolean isAbsolute() { return absolute; }

    /** URL representation of the {@link #path} if applicable. */
    private URL url;
    public URL getUrl() { return url; }

    private void update(String path, URL url, Boolean modular, boolean absolute) {
        if (modular != null && modular) {
            checkState(url == null, "URL cannot be modular");
            checkState(!absolute, "absolute path cannot be modular");
        }

        this.url = url;
        this.path = path;
        this.modular = modular;
        this.absolute = absolute;
    }

    /** Overrides value of the {@link PropertyFileSet#variants}. */
    @Parameter
    protected String[] variants;
    public String[] getVariants() { return variants; }
    public void setVariant(String variant) { this.variants = new String[]{variant}; }
    public void setVariants(String[] variants) { this.variants = variants; }

    /**
     * Whether to try searching for file variants relative to a {@link #path}.
     * Defaults to {@code false} when {@link #path}:
     * <ul>
     *     <li>represents a {@link URL} (includes protocol)</li>
     *     <li>explicitly defines an extension</li>
     * </ul>
     * @see #shouldTryVariants
     */
    @Parameter
    private Boolean tryVariants;
    public Boolean getTryVariants() { return tryVariants; }
    public void setTryVariants(Boolean tryVariants) { this.tryVariants = tryVariants; }

    /** Overrides value of the {@link PropertyFileSet#extensions}. */
    @Parameter
    protected String[] extensions;
    public String[] getExtensions() { return extensions; }
    public void setExtension(String extension) { this.extensions = new String[]{extension}; }
    public void setExtensions(String[] extensions) { this.extensions = extensions; }

    /**
     * Whether to try searching for file other file extensions relative to a {@link #path}.
     * Defaults to {@code false} when {@link #path}:
     * <ul>
     *     <li>explicitly defines an extension</li>
     * </ul>
     * @see #shouldTryExtensions
     */
    @Parameter
    protected Boolean tryExtensions;
    public Boolean getTryExtensions() { return tryExtensions; }
    public void setTryExtensions(Boolean tryExtensions) { this.tryExtensions = tryExtensions; }

    public PropertyFile() {}
    public PropertyFile(String path) {
        this.setPath(path);
    }

    public boolean isModular(Boolean defaultValue) {
        if (modular != null) return modular;
        if (url != null) return false;
        if (absolute) return false;
        if (defaultValue != null) return defaultValue;
        return false;
    }

    public boolean shouldTryVariants() {
        if (tryVariants != null) return tryVariants;
        if (url != null) return false;
        if (hasExtension(this.path)) return false;
        return true;
    }

    public boolean shouldTryExtensions() {
        if (tryExtensions != null) return tryExtensions;
        if (hasExtension(this.path)) return false;
        return true;
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
            if (configuration.getValue() != null) {
                ConfigurationConverter converter = lookup.lookupConverterForType(String.class);
                String path = (String) converter.fromConfiguration(lookup, configuration, String.class, enclosingType, loader, evaluator);
                return new PropertyFile(path);
            }
            return super.fromConfiguration(lookup, configuration, type, enclosingType, loader, evaluator, listener);
        }
    }
}
