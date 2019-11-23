package dev.gavar.mojo.io;

import org.apache.commons.configuration2.*;
import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.builder.DefaultParametersManager;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.FileBasedBuilderParameters;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Configuration loader that handles different file types by path pattern matching.
 * <p>
 * {@link DefaultParametersManager} should be used to configure properties for the particular file types
 * via {@link DefaultParametersManager#registerDefaultsHandler}.
 * <p>
 * List of file types supported by default:
 * <ul>
 *     <li>{@code .json}</li>
 *     <li>{@code .yml / yaml}</li>
 *     <li>{@code .xml}</li>
 *     <li>{@code .ini}</li>
 *     <li>{@code .properties}</li>
 * </ul>
 */
public class ConfigurableConfigurationLoader implements ConfigurationLoader {

    private final Parameters parameters;
    private List<Pair<PathMatcher, Class>> bindings;
    public ConfigurableConfigurationLoader() {
        this(null);
    }

    public ConfigurableConfigurationLoader(@Nullable final DefaultParametersManager manager) {
        this.bindings = new ArrayList<>();
        this.parameters = new Parameters(manager);

        // well known extensions
        glob("**.json", JSONConfiguration.class);
        glob("**.{yml,yaml}", YAMLConfiguration.class);
        glob("**.xml", XMLConfiguration.class);
        glob("**.ini", INIConfiguration.class);
        glob("**.properties", PropertiesConfiguration.class);
    }

    @Override
    public Configuration load(String path) throws ConfigurationException {
        return file(Path.of(path), path, FileBasedBuilderParameters::setPath);
    }

    @Override
    public Configuration load(File file) throws ConfigurationException {
        return file(file.toPath(), file, FileBasedBuilderParameters::setFile);
    }

    @Override
    public Configuration load(Path path) throws ConfigurationException {
        return file(path, path.toFile(), FileBasedBuilderParameters::setFile);
    }

    @Override
    public Configuration load(URI uri) throws ConfigurationException {
        return file(Path.of(uri.getPath()), new File(uri), FileBasedBuilderParameters::setFile);
    }

    @Override
    public Configuration load(URL url) throws ConfigurationException {
        return file(Path.of(url.getPath()), url, FileBasedBuilderParameters::setURL);
    }

    /**
     * Configure loader to use configuration of particular type,
     * when file path matches the provided Glob pattern.
     * @param pattern - glob pattern to check path against.
     * @param type    - class of the configuration to use for reading properties.
     * @see <a href="https://mincong-h.github.io/2019/04/16/glob-expression-understanding/">Glob Expression Understanding</a>
     */
    public <T extends FileBasedConfiguration> void glob(final String pattern, final Class<T> type) {
        binding("glob:" + pattern, type);
    }

    /**
     * Configure loader to use configuration of particular type,
     * when file path matches the provided Glob pattern.
     * @param pattern - regular expresion defining match criteria.
     * @param type    - class of the configuration to use for reading properties.
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html#getPathMatcher-java.lang.String-">PathMatcher</a>
     */
    public <T extends FileBasedConfiguration> void regex(final String pattern, final Class<T> type) {
        binding("regex:" + pattern, type);
    }

    private <T> Configuration file(Path path) throws ConfigurationException {
        return load(path, parameters.fileBased());
    }

    private <T> Configuration file(Path path, T param, BiConsumer<FileBasedBuilderParameters, T> consumer) throws ConfigurationException {
        FileBasedBuilderParameters params = parameters.fileBased();
        consumer.accept(params, param);
        return load(path, params);
    }

    private <T extends FileBasedConfiguration> void binding(final String syntaxAndPattern, final Class<T> type) {
        FileSystem fileSystem = FileSystems.getDefault();
        PathMatcher matcher = fileSystem.getPathMatcher(syntaxAndPattern);
        bindings.add(Pair.of(matcher, type));
    }

    private Class findType(Path path, Class defaultType) {
        for (int i = bindings.size() - 1; i >= 0; i--)
            if (bindings.get(i).getLeft().matches(path))
                return bindings.get(i).getRight();
        return defaultType;
    }

    @SuppressWarnings("unchecked")
    private Configuration load(Path path, BuilderParameters params) throws ConfigurationException {
        // fallback to properties file type
        Class type = findType(path, PropertiesConfiguration.class);

        // initialize
        FileBasedConfigurationBuilder<?> builder = new FileBasedConfigurationBuilder(type);
        builder.configure(params);

        // locate
        FileHandler file = builder.getFileHandler();
        if (canLocate(file) && !file.locate())
            return null;

        return builder.getConfiguration();
    }

    private static boolean canLocate(FileHandler file) {
        URL url = file.getURL();
        if (url == null) return true;
        if ("file".equalsIgnoreCase(url.getProtocol())) return true;
        return false;
    }
}
