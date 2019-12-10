package io.github.gavar.mojo.io;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

public interface ConfigurationLoader {
    /**
     * Load configuration file by the given path.
     * @param path - path to a file.
     * @return configuration for the file if such location is resolved; otherwise null.
     * @throws ConfigurationException if error occurs while loading.
     */
    Configuration load(Path path) throws ConfigurationException;
    Configuration load(String path) throws ConfigurationException;
    Configuration load(File file) throws ConfigurationException;
    Configuration load(URI uri) throws ConfigurationException;
    Configuration load(URL url) throws ConfigurationException;
}
