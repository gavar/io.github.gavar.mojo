package dev.gavar.mojo.core;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

public enum PropertySourceType {

    /**
     * Maven project properties.
     * @see MavenProject#getProperties()
     */
    PROJECT,

    /**
     * Maven session user properties.
     * @see MavenSession#getUserProperties()
     */
    USER,

    /**
     * Maven session system properties.
     * @see MavenSession#getSystemProperties()
     */
    SYSTEM,
}
