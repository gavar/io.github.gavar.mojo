package io.github.gavar.mojo.release.phase;

import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseFailureException;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.env.ReleaseEnvironment;
import org.apache.maven.shared.release.phase.AbstractReleasePhase;
import org.apache.maven.shared.release.phase.ReleasePhase;
import org.codehaus.plexus.component.annotations.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.github.gavar.mojo.release.util.ProjectUtils.versionlessKey;
import static java.lang.Boolean.parseBoolean;

@Component(role = ReleasePhase.class, hint = "set-project-list")
public class SetProjectListPhase extends AbstractReleasePhase {

    @Override
    public ReleaseResult simulate(final ReleaseDescriptor descriptor,
                                  final ReleaseEnvironment environment,
                                  final List<MavenProject> projects) throws ReleaseExecutionException, ReleaseFailureException {
        return execute(descriptor, environment, projects);
    }

    @Override
    public ReleaseResult execute(final ReleaseDescriptor descriptor,
                                 final ReleaseEnvironment environment,
                                 final List<MavenProject> projects) throws ReleaseExecutionException, ReleaseFailureException {

        final List<String> artifacts = new ArrayList<>();
        final Map releaseVersions = descriptor.getReleaseVersions();
        for (MavenProject project : projects) {
            final String key = versionlessKey(project);
            final boolean skip = parseBoolean(Objects.toString(releaseVersions.get(key + ".skip")));
            if (!skip) artifacts.add(project.getArtifactId());
        }

        if (artifacts.isEmpty())
            throw new ReleaseFailureException("no projects to release!");

        String args = descriptor.getAdditionalArguments();
        args += " --projects " + String.join(",", artifacts);
        descriptor.setAdditionalArguments(args);

        final ReleaseResult result = new ReleaseResult();
        result.setResultCode(ReleaseResult.SUCCESS);
        return result;
    }
}
