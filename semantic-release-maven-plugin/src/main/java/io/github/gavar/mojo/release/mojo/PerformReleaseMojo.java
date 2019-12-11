package io.github.gavar.mojo.release.mojo;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.shared.release.config.ReleaseDescriptor;

import static io.github.gavar.mojo.release.util.ReleaseMojoUtils.initializeDescriptorArguments;

@Mojo(name = "perform", aggregator = true, requiresProject = false)
public class PerformReleaseMojo extends org.apache.maven.plugins.release.PerformReleaseMojo {

    @Override
    protected ReleaseDescriptor createReleaseDescriptor() {
        final ReleaseDescriptor descriptor = super.createReleaseDescriptor();
        initializeDescriptorArguments(descriptor, session);
        return descriptor;
    }
}
