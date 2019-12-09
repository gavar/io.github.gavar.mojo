package dev.gavar.mojo.release.phase;

import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseFailureException;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.env.ReleaseEnvironment;
import org.apache.maven.shared.release.phase.ReleasePhase;
import org.codehaus.plexus.component.annotations.Component;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static dev.gavar.mojo.release.util.ProjectUtils.versionlessKey;
import static java.lang.Boolean.parseBoolean;

@Component(role = ReleasePhase.class, hint = "scm-tag-projects")
public class TagProjectsPhase extends AbstractSemanticPhase {

    @Override
    public ReleaseResult execute(final ReleaseDescriptor descriptor,
                                 final ReleaseEnvironment environment,
                                 final List<MavenProject> projects) throws ReleaseExecutionException, ReleaseFailureException {
        try {
            return process(false, descriptor, environment, projects);
        } catch (ScmException e) {
            throw new ReleaseExecutionException(e.getMessage(), e);
        }
    }

    @Override
    public ReleaseResult simulate(final ReleaseDescriptor descriptor,
                                  final ReleaseEnvironment environment,
                                  final List<MavenProject> projects) throws ReleaseExecutionException, ReleaseFailureException {
        try {
            return process(true, descriptor, environment, projects);
        } catch (ScmException e) {
            throw new ReleaseExecutionException(e.getMessage(), e);
        }
    }

    public ReleaseResult process(final boolean simulate,
                                 final ReleaseDescriptor descriptor,
                                 final ReleaseEnvironment environment,
                                 final List<MavenProject> mavenProjects) throws ReleaseExecutionException, ScmException {
        ReleaseResult result = new ReleaseResult();
        result.setResultCode(ReleaseResult.SUCCESS);

        final File root = new File(descriptor.getWorkingDirectory());
        final ScmRepository repository = getScmRepository(descriptor, environment, root.getPath());
        final ScmProvider provider = getScmProvider(repository);

        for (MavenProject mavenProject : mavenProjects) {
            final String key = versionlessKey(mavenProject);
            final Map releaseVersions = descriptor.getReleaseVersions();
            final String tagName = Objects.toString(releaseVersions.get(key + ".tag"), mavenProject.getScm().getTag());
            final boolean skipTag = parseBoolean(Objects.toString(releaseVersions.get(key + ".tag.skip"), "false"));

            if (skipTag || tagName == null) {
                logInfo(result, "Skipping tag for project: " + key);
            } else if (simulate) {
                logInfo(result, "Full run would create tag: " + tagName);
            } else {
                final ScmFileSet fileSet = new ScmFileSet(root);
                ScmTagParameters scmTagParameters = new ScmTagParameters();
                scmTagParameters.setRemoteTagging(descriptor.isRemoteTagging());
                scmTagParameters.setScmRevision(descriptor.getScmReleasedPomRevision());
                final TagScmResult tag = provider.tag(repository, fileSet, tagName, scmTagParameters);
                if (tag.isSuccess()) logInfo(result, "Tag created: " + tagName);
                else logError(result, tag.getProviderMessage());
            }
        }

        return result;
    }
}
