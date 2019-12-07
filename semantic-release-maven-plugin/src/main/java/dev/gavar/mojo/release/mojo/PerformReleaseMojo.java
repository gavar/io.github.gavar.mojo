package dev.gavar.mojo.release.mojo;

import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "perform", aggregator = true, requiresProject = false)
public class PerformReleaseMojo extends org.apache.maven.plugins.release.PerformReleaseMojo {

}
