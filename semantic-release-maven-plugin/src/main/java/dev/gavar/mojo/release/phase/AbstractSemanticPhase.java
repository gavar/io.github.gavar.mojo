package dev.gavar.mojo.release.phase;

import dev.gavar.mojo.release.model.ProjectConfig;
import dev.gavar.mojo.release.model.ReleaseProject;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.Properties;

import static dev.gavar.mojo.release.model.ProjectConfig.SCM_TAG_PREFIX;

public abstract class AbstractSemanticPhase extends AbstractScmPhase {

    protected ReleaseProject toReleaseProject(MavenProject project, File root) {
        final Properties props = new Properties();
        // defaults
        props.put(SCM_TAG_PREFIX, "v/" + project.getArtifactId() + "/");

        // per-project configuration
        props.putAll(project.getProperties());

        final ProjectConfig config = new ProjectConfig(props);
        return new ReleaseProject(root, project, config);
    }
}
