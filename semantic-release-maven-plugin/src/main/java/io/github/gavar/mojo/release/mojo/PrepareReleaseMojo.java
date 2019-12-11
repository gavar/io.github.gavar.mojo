package io.github.gavar.mojo.release.mojo;

import io.github.gavar.mojo.release.util.ListUtils;
import io.github.gavar.mojo.release.util.ReflectionUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.shared.release.config.ReleaseDescriptor;

import java.util.List;

import static io.github.gavar.mojo.release.util.ReleaseMojoUtils.initializeDescriptorArguments;

@Mojo(name = "prepare", aggregator = true)
public class PrepareReleaseMojo extends org.apache.maven.plugins.release.PrepareReleaseMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            // TODO: allow to tag on detached head
            final List<String> phases = ReflectionUtils.phasesOf(releaseManager, "preparePhases");
            ListUtils.addBefore(phases, "map-release-versions", "resolve-semantic-versions");
            ListUtils.replace(phases, "rewrite-poms-for-release", "rewrite-poms-for-semantic-release");
            // addBefore(phases, "scm-commit-release", "git-detach"); // work on detached head
            ListUtils.replace(phases, "scm-tag", "scm-tag-projects"); // tag each project
            // addAfter(phases, "scm-tag-projects", "git-attach"); // back to master branch
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
