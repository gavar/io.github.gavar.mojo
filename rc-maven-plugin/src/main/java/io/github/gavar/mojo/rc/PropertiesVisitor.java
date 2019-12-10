package io.github.gavar.mojo.rc;

import io.github.gavar.mojo.io.ConfigurationLoader;
import io.github.gavar.mojo.io.PathInfo;
import io.github.gavar.mojo.io.StringInterpolator;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.maven.plugin.logging.Log;

import javax.annotation.Nullable;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkArgument;
import static io.github.gavar.mojo.util.GenericUtils.valueOrDefault;
import static org.apache.commons.configuration2.ConfigurationConverter.getProperties;

/**
 * Property visitor that accumulates properties of the visited files in a list.
 * Newly discovered files are added to the end of the list, so it's up to an end user to decide property precedence.
 * Properties are merged in a reverse order via {@link PropertiesVisitor#toProperties},
 * so that properties sources earlier in this list override later ones.
 */
public class PropertiesVisitor {

    private static final String[] EMPTY = new String[0];

    private final Log log;
    private final ConfigurationLoader loader;
    private final Map<String, Configuration> visits;
    private final CompositeConfiguration configurations;

    public PropertiesVisitor(ConfigurationLoader loader, Log log) {
        this.log = log;
        this.loader = loader;
        this.visits = new HashMap<>();
        this.configurations = new CompositeConfiguration();
        this.configurations.setInterpolator(new StringInterpolator(this.configurations::getProperty));
    }

    public int visit(String path) throws ConfigurationException {
        return visit(Path.of(path).normalize().toString(), loader.load(path));
    }

    public int visit(String path, @Nullable String[] variants, @Nullable String[] extensions) throws ConfigurationException {
        return visit(new PathInfo(path), variants, extensions);
    }

    public int visit(File file) throws ConfigurationException {
        return visit(file.toPath().normalize().toString(), loader.load(file));
    }

    public int visit(Path path) throws ConfigurationException {
        return visit(path.normalize().toString(), loader.load(path));
    }

    public int visit(Path path, @Nullable String[] variants, @Nullable String[] extensions) throws ConfigurationException {
        return visit(new PathInfo(path), variants, extensions);
    }

    public int visit(URL url) throws ConfigurationException {
        return visit(url.toString(), loader.load(url));
    }

    public int visit(URL url, @Nullable String[] variants, @Nullable String[] extensions) throws ConfigurationException {
        checkArgument(variants == null || variants.length == 0, "URL does not supports variants yet");
        checkArgument(extensions == null || extensions.length == 0, "URL does not supports extensions yet");
        return visit(url.toString(), loader.load(url));
    }

    public int visit(PathInfo info) throws ConfigurationException {
        return visit(info.getPathName());
    }

    public int visit(PathInfo info, @Nullable String variant, @Nullable String extension) throws ConfigurationException {
        String path = info.getStem();
        path += valueOrDefault(variant, "");
        path += valueOrDefault(extension, info.getExtension());
        path = info.getDirectory() + "/" + path;
        return visit(path);
    }

    /**
     * Visit every variant of the file at particular path.
     * <p>
     * Visiting order:
     * <ul>
     *   <li>{@code ${path} ${filename}}</li>
     *   <li>{@code ${path} ${filename} ${extension}} - for every extension</li>
     *   <li>{@code ${path} ${filename} ${variant}} - for every variant</li>
     *   <li>{@code ${path} ${filename} ${variant} ${extension}} - for every extension of every variant</li>
     * </ul>
     * @param info       - object containing components of the file path to visit.
     * @param variants   - file variants to load along the way up.
     * @param extensions - list of file types (including dot) to search for.
     * @throws ConfigurationException - when properties loading failed.
     */
    public int visit(PathInfo info, @Nullable String[] variants, @Nullable String[] extensions) throws ConfigurationException {
        if (variants == null) variants = EMPTY;
        if (extensions == null) extensions = EMPTY;

        // visit path itself
        int v = visit(info);

        // visit every extension
        for (String extension : extensions)
            v += visit(info, null, extension);

        // visit every variant
        for (String variant : variants) {
            v += visit(info, variant, null);
            // visit every variant extension
            for (String extension : extensions)
                v += visit(info, variant, extension);
        }

        return v;
    }

    private int visit(String key, Configuration configuration) {
        if (visits.containsKey(key))
            throw new IllegalStateException("duplicate visit of: " + key);

        visits.put(key, configuration);
        if (configuration != null) {
            log.info("[+] " + key);
            configurations.addConfiguration(configuration);
            return 1;
        }

        return 0;
    }

    /**
     * Flatten accumulated properties sources to a single object.
     * @return {@code properties} object when not null; otherwise a newly created object.
     */
    public Properties toProperties() {
        return getProperties(configurations);
    }
}
