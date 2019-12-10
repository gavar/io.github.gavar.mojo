package io.github.gavar.mojo.release.mojo;

import io.github.gavar.mojo.release.model.ProjectConfig;
import io.github.gavar.mojo.release.model.ReleaseProject;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.apache.maven.shared.release.util.ReleaseUtil.getCommonBasedir;

public abstract class BaseMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;

    /** Common root directory of the session projects. */
    protected File root;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            root = new File(getCommonBasedir(session.getProjects()));
            process();
        } catch (MojoExecutionException e) {
            getLog().error(e);
            throw e;
        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected abstract void process() throws Exception;

    protected Git git() throws IOException {
        return Git.open(root);
    }

    protected ReleaseProject toReleaseProject(MavenProject project) {
        final Properties props = new Properties();
        // defaults
        props.put(ProjectConfig.SCM_TAG_PREFIX, "v/" + project.getArtifactId() + "/");

        // per-project configuration
        props.putAll(project.getProperties());

        final ProjectConfig config = new ProjectConfig(props);
        return new ReleaseProject(root, project, config);
    }
}
