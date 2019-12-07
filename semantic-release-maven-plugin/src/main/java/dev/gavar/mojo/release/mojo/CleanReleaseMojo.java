package dev.gavar.mojo.release.mojo;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "clean", threadSafe = true, defaultPhase = LifecyclePhase.CLEAN)
public class CleanReleaseMojo extends org.apache.maven.plugins.release.CleanReleaseMojo {

}
