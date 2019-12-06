package dev.gavar.mojo.release;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.File;
import java.util.Properties;

import static java.lang.Boolean.parseBoolean;

@Mojo(name = "properties", requiresDirectInvocation = true, aggregator = true)
public class PropertiesMojo extends BaseMojo {

    protected void process() throws Exception {
        final Properties userProperties = session.getUserProperties();
        final String resume = userProperties.getProperty("resume", "true");
        if (!parseBoolean(resume))
            throw new MojoExecutionException(String.join(" ",
                    "there is no reason to run `semantic-release:properties` goal having -Dresume=false",
                    "since `maven-release-plugin` will discard all release.properties modifications"
            ));

        final Git git = openGit();
        final Repository repository = git.getRepository();
        final RevWalk walk = new RevWalk(repository);

        // load properties
        final File basedir = project.getBasedir();
        final ReleaseProperties properties = new ReleaseProperties();

        // analyze projects
        for (MavenProject project : session.getAllProjects()) {
            final ReleaseProject release = toReleaseProject(project).analyze(git, walk);
            properties.write(release.analyze(git, walk));
        }
        properties.write("scm.tag", "");

        // save properties
        properties.store(new File(basedir, "release.properties"));
        properties.store(new File(basedir, "semantic.release.properties"));
    }
}
