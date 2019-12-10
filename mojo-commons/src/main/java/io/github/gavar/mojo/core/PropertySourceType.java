package io.github.gavar.mojo.core;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

import java.util.Properties;
import java.util.function.Function;

public enum PropertySourceType {

    /**
     * Maven project properties.
     * @see MavenProject#getProperties()
     */
    PROJECT(s -> s.getCurrentProject().getProperties()),

    /**
     * Maven session user properties.
     * @see MavenSession#getUserProperties()
     */
    USER(MavenSession::getUserProperties),

    /**
     * Maven session system properties.
     * @see MavenSession#getSystemProperties()
     */
    SYSTEM(MavenSession::getSystemProperties);

    private final Function<MavenSession, Properties> resolver;

    public Properties resolve(MavenSession session) {
        return this.resolver.apply(session);
    }

    PropertySourceType(Function<MavenSession, Properties> resolver) {
        this.resolver = resolver;
    }
}
