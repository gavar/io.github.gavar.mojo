package io.github.gavar.mojo.release.phase;

import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.Version;
import io.github.gavar.mojo.release.model.ReleaseProject;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseFailureException;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.env.ReleaseEnvironment;
import org.apache.maven.shared.release.phase.ReleasePhase;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import static io.github.gavar.mojo.release.util.ProjectUtils.toReleaseProject;
import static io.github.gavar.mojo.release.util.ProjectUtils.versionlessKey;
import static org.apache.maven.shared.release.ReleaseResult.SUCCESS;

@Component(role = ReleasePhase.class, hint = "resolve-semantic-versions")
public class ResolveVersionsPhase extends AbstractScmPhase {

    @Requirement(role = Prompter.class, hint = "default")
    protected Prompter prompter;

    @Override
    public ReleaseResult execute(final ReleaseDescriptor descriptor,
                                 final ReleaseEnvironment environment,
                                 final List<MavenProject> projects) throws ReleaseExecutionException, ReleaseFailureException {
        try {
            return process(descriptor, environment, projects);
        } catch (IOException | PrompterException e) {
            throw new ReleaseExecutionException(e.getMessage(), e);
        }
    }

    @Override
    public ReleaseResult simulate(final ReleaseDescriptor descriptor,
                                  final ReleaseEnvironment environment,
                                  final List<MavenProject> projects) throws ReleaseExecutionException, ReleaseFailureException {
        return execute(descriptor, environment, projects);
    }

    public ReleaseResult process(final ReleaseDescriptor descriptor,
                                 final ReleaseEnvironment environment,
                                 final List<MavenProject> reactorProjects) throws IOException, ReleaseExecutionException, PrompterException {
        final File dir = new File(descriptor.getWorkingDirectory());

        final Git git = Git.open(dir);
        final Repository repository = git.getRepository();
        final RevWalk walk = new RevWalk(repository);

        // analyze projects
        for (MavenProject project : reactorProjects) {
            final String key = versionlessKey(project);
            final ReleaseProject release = toReleaseProject(project, dir).analyze(git, walk);
            final Version suggestVersion = release.resolveRelVersion();

            Version relVersion = null;
            if (descriptor.isInteractive() && !release.isDeploySkip()) {
                while (relVersion == null) {
                    final String pattern = "What is the release version for \"{0}\"? {1} ->";
                    final String deploy = release.shouldSkip(suggestVersion) ? "skip"
                        : release.isNew() ? "new"
                        : release.getLastReleaseVersion().toString();
                    final String message = MessageFormat.format(pattern, project.getName(), deploy);
                    final String input = prompter.prompt(message, suggestVersion.toString());
                    try {
                        relVersion = Version.valueOf(input);
                    } catch (ParseException e) {
                        relVersion = null;
                        prompter.showMessage("invalid version format, should be a SemVer: x.x.x\n");
                    }
                }

                if (release.shouldSkip(relVersion))
                    prompter.showMessage(MessageFormat.format("\"{0}\" will be skipped\n", project.getName()));

            } else {
                relVersion = suggestVersion;
            }

            final boolean skip = release.shouldSkip(relVersion);
            final Version devVersion = release.resolveDevVersion(relVersion, skip);
            final String releaseTag = release.getReleaseTag(relVersion);

            descriptor.addReleaseVersion(key, relVersion.toString());
            descriptor.addDevelopmentVersion(key, devVersion.toString());
            descriptor.addReleaseVersion(key + ".tag", releaseTag);
            descriptor.addReleaseVersion(key + ".skip", Boolean.toString(skip));
        }

        final ReleaseResult result = new ReleaseResult();
        result.setResultCode(SUCCESS);
        return result;
    }
}
