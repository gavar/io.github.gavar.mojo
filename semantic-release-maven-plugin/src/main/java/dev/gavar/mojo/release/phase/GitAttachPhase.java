package dev.gavar.mojo.release.phase;

import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseFailureException;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.env.ReleaseEnvironment;
import org.apache.maven.shared.release.phase.ReleasePhase;
import org.codehaus.plexus.component.annotations.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component(role = ReleasePhase.class, hint = "git-attach")
public class GitAttachPhase extends AbstractScmPhase {

    @Override
    public ReleaseResult execute(ReleaseDescriptor releaseDescriptor, ReleaseEnvironment releaseEnvironment, List<MavenProject> reactorProjects) throws ReleaseExecutionException, ReleaseFailureException {
        try {
            // TODO: read branch from descriptor
            final String commonBaseDir = commonDir(reactorProjects);
            new ProcessBuilder("git", "checkout", "master")
                    .directory(new File(commonBaseDir))
                    .redirectErrorStream(true)
                    .start()
                    .waitFor();
        } catch (IOException | InterruptedException e) {
            throw new ReleaseExecutionException(e.getMessage(), e);
        }
        final ReleaseResult result = new ReleaseResult();
        result.setResultCode(ReleaseResult.SUCCESS);
        return result;
    }

    @Override
    public ReleaseResult simulate(ReleaseDescriptor releaseDescriptor, ReleaseEnvironment releaseEnvironment, List<MavenProject> reactorProjects) throws ReleaseExecutionException, ReleaseFailureException {
        final ReleaseResult result = new ReleaseResult();
        logInfo(result, "Full run would checkout to master branch");
        result.setResultCode(ReleaseResult.SUCCESS);
        return result;
    }
}
