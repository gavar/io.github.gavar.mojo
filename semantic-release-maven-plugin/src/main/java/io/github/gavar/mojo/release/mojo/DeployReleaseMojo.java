package io.github.gavar.mojo.release.mojo;

import io.github.gavar.mojo.release.util.ReflectionUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import java.util.List;

@Mojo(name = "deploy", aggregator = true, requiresProject = false)
public class DeployReleaseMojo extends org.apache.maven.plugins.release.PerformReleaseMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            final List<String> phases = ReflectionUtils.phasesOf(releaseManager, "performPhases");
            phases.clear();
            phases.add("set-project-list");
            phases.add("run-perform-goals");
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
