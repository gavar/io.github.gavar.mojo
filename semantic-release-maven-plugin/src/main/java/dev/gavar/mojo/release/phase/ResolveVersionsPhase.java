package dev.gavar.mojo.release.phase;

import dev.gavar.mojo.release.model.ReleaseProject;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseFailureException;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.env.ReleaseEnvironment;
import org.apache.maven.shared.release.phase.ReleasePhase;
import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static dev.gavar.mojo.release.util.ProjectUtils.versionlessKey;
import static org.apache.maven.shared.release.ReleaseResult.SUCCESS;

@Component(role = ReleasePhase.class, hint = "resolve-semantic-versions")
public class ResolveVersionsPhase extends AbstractSemanticPhase {

    @Override
    public ReleaseResult execute(final ReleaseDescriptor descriptor,
                                 final ReleaseEnvironment environment,
                                 final List<MavenProject> projects) throws ReleaseExecutionException, ReleaseFailureException {
        try {
            return process(descriptor, projects);
        } catch (IOException e) {
            throw new ReleaseExecutionException(e.getMessage(), e);
        }
    }

    @Override
    public ReleaseResult simulate(final ReleaseDescriptor descriptor,
                                  final ReleaseEnvironment environment,
                                  final List<MavenProject> projects) throws ReleaseExecutionException, ReleaseFailureException {
        return execute(descriptor, environment, projects);
    }

    public ReleaseResult process(ReleaseDescriptor descriptor, List<MavenProject> reactorProjects) throws IOException, ReleaseExecutionException {
        final File dir = new File(descriptor.getWorkingDirectory());

        final Git git = Git.open(dir);
        final Repository repository = git.getRepository();
        final RevWalk walk = new RevWalk(repository);

        // analyze projects
        for (MavenProject project : reactorProjects) {
            final String key = versionlessKey(project);
            final ReleaseProject release = toReleaseProject(project, dir).analyze(git, walk);
            descriptor.addReleaseVersion(key, release.getNextRelVersion().toString());
            descriptor.addDevelopmentVersion(key, release.getNextDevVersion().toString());
            descriptor.addReleaseVersion(key + ".tag", release.getReleaseTag());
            descriptor.addReleaseVersion(key + ".tag.skip", Boolean.toString(release.shouldSkipTag()));
        }

        final ReleaseResult result = new ReleaseResult();
        result.setResultCode(SUCCESS);
        return result;
    }
}
