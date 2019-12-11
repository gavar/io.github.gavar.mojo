package io.github.gavar.mojo.release.mojo;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.shared.release.config.ReleaseDescriptor;

import static io.github.gavar.mojo.release.util.ReleaseMojoUtils.initializeDescriptorArguments;

@Mojo(name = "clean", threadSafe = true, defaultPhase = LifecyclePhase.CLEAN)
public class CleanReleaseMojo extends org.apache.maven.plugins.release.CleanReleaseMojo {

    @Override
    protected ReleaseDescriptor createReleaseDescriptor() {
        final ReleaseDescriptor descriptor = super.createReleaseDescriptor();
        initializeDescriptorArguments(descriptor, session);
        return descriptor;
    }
}
