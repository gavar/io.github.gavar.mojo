package dev.gavar.mojo.release.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import java.util.List;

import static dev.gavar.mojo.release.util.ListUtils.*;
import static dev.gavar.mojo.release.util.ReflectionUtils.phasesOf;

@Mojo(name = "prepare", aggregator = true)
public class PrepareReleaseMojo extends org.apache.maven.plugins.release.PrepareReleaseMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            // TODO: allow to tag on detached head
            final List<String> phases = phasesOf(releaseManager, "preparePhases");
            addBefore(phases, "map-release-versions", "resolve-semantic-versions");
            // addBefore(phases, "scm-commit-release", "git-detach"); // work on detached head
            replace(phases, "scm-tag", "scm-tag-projects"); // tag each project
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
}
