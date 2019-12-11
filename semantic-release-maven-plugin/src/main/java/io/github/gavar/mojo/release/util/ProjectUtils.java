package io.github.gavar.mojo.release.util;

import io.github.gavar.mojo.release.model.ProjectConfig;
import io.github.gavar.mojo.release.model.ReleaseProject;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.Properties;

import static io.github.gavar.mojo.release.Constants.MAVEN_DEPLOY_SKIP;
import static java.lang.Boolean.parseBoolean;

public class ProjectUtils {

    public static String versionlessKey(MavenProject project) {
        return ArtifactUtils.versionlessKey(project.getGroupId(), project.getArtifactId());
    }

    public static boolean isDeploySkip(MavenProject project) {
        return parseBoolean(project.getProperties().getProperty(MAVEN_DEPLOY_SKIP));
    }

    public static ReleaseProject toReleaseProject(MavenProject project, File root) {
        final Properties props = new Properties();
        props.putAll(project.getProperties());

        final ProjectConfig config = new ProjectConfig(props);
        config.setTagPrefix("v/" + project.getArtifactId() + "/");

        return new ReleaseProject(root, project, config);
    }
}
