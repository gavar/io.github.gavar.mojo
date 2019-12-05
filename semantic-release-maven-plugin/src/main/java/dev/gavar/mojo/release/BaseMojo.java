package dev.gavar.mojo.release;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

import static dev.gavar.mojo.release.ProjectConfig.RELEASE_TAG_PREFIX;
import static dev.gavar.mojo.util.GenericUtils.putNonNull;

public abstract class BaseMojo extends AbstractMojo {

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;

    @Parameter(property = "git.root")
    protected File root;

    @Parameter(property = RELEASE_TAG_PREFIX)
    protected String tagPrefix;

    protected Git openGit() throws IOException {
        if (root == null) {
            final Process process = new ProcessBuilder
                    ("git", "rev-parse", "--show-toplevel")
                    .directory(session.getCurrentProject().getBasedir())
                    .start();
            final Scanner scanner = new Scanner(process.getInputStream());
            root = new File(scanner.nextLine());
        }
        return Git.open(root);
    }

    protected ReleaseProject toReleaseProject(MavenProject project) {
        final Properties props = new Properties();
        // defaults
        props.put(RELEASE_TAG_PREFIX, "v/" + project.getArtifactId());

        // configuration
        putNonNull(props, RELEASE_TAG_PREFIX, tagPrefix);

        // per-project configuration
        props.putAll(project.getProperties());

        final ProjectConfig config = new ProjectConfig(props);
        return new ReleaseProject(root, project, config);
    }
}
