package dev.gavar.mojo.release;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static java.lang.Boolean.parseBoolean;

@Mojo(name = "properties", requiresDirectInvocation = true, aggregator = true)
public class PropertiesMojo extends BaseMojo {

    @Override
    public void execute() throws MojoExecutionException {
        try {
            process();
        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected void process() throws IOException {
        final Log log = getLog();
        final Properties userProperties = session.getUserProperties();

        log.info("Loading GIT repository: " + root);
        final Git git = openGit();
        final Repository repository = git.getRepository();
        final RevWalk walk = new RevWalk(repository);

        final File file = new File(project.getBasedir(), "release.properties");
        final ReleaseProperties properties = new ReleaseProperties();

        // load properties
        boolean resume = bool(userProperties.getOrDefault("resume", true));
        if (resume) properties.load(file);

        // analyze projects
        for (MavenProject project : session.getAllProjects()) {
            final ReleaseProject release = toReleaseProject(project).analyze(git, walk);
            properties.write(release.analyze(git, walk));
        }

        // save properties
        properties.store(file);

        // always resume release after this plugin, in order to pick up properties
        userProperties.put("resume", true);
    }

    static boolean bool(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        return parseBoolean(value.toString());
    }
}
