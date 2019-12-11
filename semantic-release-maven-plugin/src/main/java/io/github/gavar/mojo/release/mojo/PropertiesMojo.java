package io.github.gavar.mojo.release.mojo;

import io.github.gavar.mojo.release.model.ReleaseProject;
import io.github.gavar.mojo.release.model.ReleaseProperties;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.File;

import static io.github.gavar.mojo.release.util.ProjectUtils.toReleaseProject;

@Mojo(name = "properties", requiresDirectInvocation = true, aggregator = true)
public class PropertiesMojo extends BaseMojo {

    @Override
    protected void process() throws Exception {
        final Git git = git();
        final Repository repository = git.getRepository();
        final RevWalk walk = new RevWalk(repository);

        // load properties
        final ReleaseProperties properties = new ReleaseProperties();

        // analyze projects
        for (MavenProject project : session.getAllProjects()) {
            final ReleaseProject release = toReleaseProject(project, root).analyze(git, walk);
            properties.write(release.analyze(git, walk));
        }

        // save properties
        properties.store(new File(root, "release.properties"));
    }
}
