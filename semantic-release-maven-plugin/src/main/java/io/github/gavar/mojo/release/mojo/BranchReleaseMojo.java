package io.github.gavar.mojo.release.mojo;

import io.github.gavar.mojo.release.util.ListUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.shared.release.config.ReleaseDescriptor;

import java.util.List;

import static io.github.gavar.mojo.release.util.ReflectionUtils.phasesOf;
import static io.github.gavar.mojo.release.util.ReleaseMojoUtils.initializeDescriptorArguments;

@Mojo(name = "branch", aggregator = true)
public class BranchReleaseMojo extends org.apache.maven.plugins.release.BranchReleaseMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            // TODO: allow to branch on detached head
            final List<String> phases = phasesOf(releaseManager, "branchPhases");
            ListUtils.addBefore(phases, "map-branch-versions", "resolve-semantic-versions");
            ListUtils.replace(phases, "rewrite-poms-for-branch", "rewrite-poms-for-semantic-branch");
            // addBefore(phases, "scm-commit-branch", "git-detach");  // work on detached head
            ListUtils.replace(phases, "scm-branch", "scm-tag-projects"); // tag each project
            // addAfter(phases, "scm-commit-branch", "git-attach"); // back to master branch
            super.execute();
        } catch (ReflectiveOperationException e) {
            getLog().error(e);
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (MojoExecutionException e) {
            getLog().error(e);
            throw e;
        }
    }

    @Override
    protected ReleaseDescriptor createReleaseDescriptor() {
        final ReleaseDescriptor descriptor = super.createReleaseDescriptor();
        initializeDescriptorArguments(descriptor, session);
        return descriptor;
    }
}
