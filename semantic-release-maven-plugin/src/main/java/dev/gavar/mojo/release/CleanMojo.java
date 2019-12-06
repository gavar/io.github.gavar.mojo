package dev.gavar.mojo.release;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;

@Mojo(name = "clean", threadSafe = true, defaultPhase = LifecyclePhase.CLEAN)
public class CleanMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Override
    public void execute() {
        final File basedir = project.getBasedir();
        final String[] fileNames = {
                "semantic.release.properties",
        };

        for (String fileName : fileNames) {
            final File file = new File(basedir, fileName);
            if (file.exists() && file.isFile())
                file.delete();
        }
    }
}
