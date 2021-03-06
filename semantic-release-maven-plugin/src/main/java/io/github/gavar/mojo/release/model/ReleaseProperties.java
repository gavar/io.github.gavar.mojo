package io.github.gavar.mojo.release.model;

import com.github.zafarkhaja.semver.Version;
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

    public void load(File file, boolean optional) throws IOException {
        try (final FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
        } catch (FileNotFoundException e) {
            if (!optional) throw e;
        }
    }

    public void store(File file) throws IOException {
        if (!file.exists())
            file.createNewFile();

        try (final FileOutputStream output = new FileOutputStream(file)) {
            properties.store(output, "provided by io.github.gavar.mojo:semantic-release-maven-plugin");
        }
    }

    public String getTag(MavenProject project) {
        final String KEY = "project.scm." + keyOf(project) + ".tag";
        return properties.getProperty(KEY);
    }

    public void write(ReleaseProject release) {
        final String key = keyOf(release.getMavenProject());
        final Version relVersion = release.resolveRelVersion();
        final boolean skip = release.shouldSkip(relVersion);
        final Version devVersion = release.resolveDevVersion(relVersion, skip);
        final String nextRelTag = release.getReleaseTag(relVersion);

        write("project.rel." + key, relVersion);
        write("project.dev." + key, devVersion);
        write("project.scm." + key + ".tag", nextRelTag);
    }

    public void write(String key, Object value) {
        write(key, Objects.toString(value, null));
    }

    public void write(String key, String value) {
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
