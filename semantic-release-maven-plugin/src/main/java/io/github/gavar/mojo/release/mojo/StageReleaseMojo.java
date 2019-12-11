package io.github.gavar.mojo.release.mojo;

import io.github.gavar.mojo.release.util.ReflectionUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import java.util.List;

@Mojo(name = "stage", aggregator = true, requiresProject = false)
public class StageReleaseMojo extends org.apache.maven.plugins.release.BranchReleaseMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            final List<String> phases = ReflectionUtils.phasesOf(releaseManager, "branchPhases");
            phases.clear();
            phases.add("check-poms");
            phases.add("scm-check-modifications");
            phases.add("create-backup-poms");
            phases.add("resolve-semantic-versions");
            phases.add("git-detach");
            phases.add("rewrite-poms-for-semantic-branch");
            phases.add("scm-commit-branch");
            phases.add("end-release");
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
