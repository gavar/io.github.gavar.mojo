package dev.gavar.mojo.release;

import java.util.Properties;

import static java.lang.Boolean.TRUE;

public class ProjectConfig {

    public final static String RELEASE_TAG_PREFIX = "release.tag.prefix";
    public final static String MAVEN_DEPLOY_SKIP = "maven.deploy.skip";

    private String tagPrefix;
    public String getTagPrefix() { return tagPrefix; }
    public void setTagPrefix(String tagPrefix) { this.tagPrefix = tagPrefix; }

    private Boolean skipDeploy;
    public boolean isSkipDeploy() { return skipDeploy == TRUE; }
    public void setSkipDeploy(Boolean skipDeploy) { this.skipDeploy = skipDeploy; }

    public ProjectConfig() {}
    public ProjectConfig(Properties properties) {
        this.load(properties);
    }

    public void load(Properties properties) {
        tagPrefix = properties.getProperty(RELEASE_TAG_PREFIX);
        skipDeploy = Boolean.valueOf(properties.getProperty(MAVEN_DEPLOY_SKIP));
    }
}
