package io.github.gavar.mojo.release.util;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.shared.release.config.ReleaseDescriptor;

import java.util.Properties;

public class ReleaseMojoUtils {

    public static void initializeDescriptorArguments(ReleaseDescriptor descriptor, MavenSession session) {
        final Properties userProperties = session.getUserProperties();
        final StringBuilder arguments = new StringBuilder(descriptor.getAdditionalArguments());
        consumeUserProperty("skipTests", userProperties, arguments);
        consumeUserProperty("dryRun", userProperties, arguments);
    }

    private static void consumeUserProperty(String key, Properties userProperties, StringBuilder arguments) {
        final String value = userProperties.getProperty(key);
        if (value != null) arguments.append(" -D")
            .append(key)
            .append('=')
            .append(value)
            ;
    }
}
