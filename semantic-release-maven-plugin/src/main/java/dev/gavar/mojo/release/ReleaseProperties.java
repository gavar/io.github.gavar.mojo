package dev.gavar.mojo.release;

import org.apache.maven.project.MavenProject;

import java.io.*;
import java.util.Objects;
import java.util.Properties;

public class ReleaseProperties {

    private final Properties properties;
    public Properties getProperties() { return properties; }

    public ReleaseProperties() {
        this.properties = new Properties();
    }

    public ReleaseProperties(Properties properties) {
        this.properties = properties;
    }

    public void load(File file) throws IOException {
        try (final FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
        } catch (FileNotFoundException ignored) {
            // ignore if file not exists
        }
    }

    public void store(File file) throws IOException {
        if (!file.exists())
            file.createNewFile();

        try (final FileOutputStream output = new FileOutputStream(file)) {
            properties.store(output, "provided by dev.gavar.mojo:semantic-release-maven-plugin");
        }
    }

    public void write(ReleaseProject release) {
        final String key = keyOf(release.getMavenProject());
        write("project.rel." + key, release.getNextRelVersion());
        write("project.dev." + key, release.getNextDevVersion());
        write("project.scm.tag" + key, release.tagNameFor(release.getNextRelVersion()));
    }

    private void write(String key, Object value) {
        write(key, Objects.toString(value, null));
    }

    private void write(String key, String value) {
        if (value != null)
            properties.put(key, value);
    }

    private static String keyOf(MavenProject project) {
        return String.join(":",
                project.getGroupId(),
                project.getArtifactId()
        );
    }
}
