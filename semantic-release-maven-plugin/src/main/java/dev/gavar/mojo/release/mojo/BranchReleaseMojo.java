package dev.gavar.mojo.release.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import java.util.List;

import static dev.gavar.mojo.release.util.ListUtils.addBefore;
import static dev.gavar.mojo.release.util.ListUtils.replace;
import static dev.gavar.mojo.release.util.ReflectionUtils.phasesOf;

@Mojo(name = "branch", aggregator = true)
public class BranchReleaseMojo extends org.apache.maven.plugins.release.BranchReleaseMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            // TODO: allow to branch on detached head
            final List<String> phases = phasesOf(releaseManager, "branchPhases");
            addBefore(phases, "map-branch-versions", "resolve-semantic-versions");
            // addBefore(phases, "scm-commit-branch", "git-detach");  // work on detached head
            replace(phases, "scm-branch", "scm-tag-projects"); // tag each project
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
}
