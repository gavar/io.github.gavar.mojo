package io.github.gavar.mojo.release.util;

import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.project.MavenProject;

public class ProjectUtils {

    public static String versionlessKey(MavenProject project) {
        return ArtifactUtils.versionlessKey(project.getGroupId(), project.getArtifactId());
    }
}
