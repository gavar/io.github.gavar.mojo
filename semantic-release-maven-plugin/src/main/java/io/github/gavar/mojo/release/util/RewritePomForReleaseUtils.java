package io.github.gavar.mojo.release.util;

import io.github.gavar.mojo.release.function.UnsafeProcedure;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.Map;
import java.util.Objects;

import static io.github.gavar.mojo.release.Constants.MAVEN_DEPLOY_SKIP;
import static java.lang.Boolean.parseBoolean;

public class RewritePomForReleaseUtils {

    public static void transformScm(String key, Element root, ReleaseDescriptor descriptor, UnsafeProcedure procedure) throws ReleaseExecutionException {
        final Map releaseVersions = descriptor.getReleaseVersions();
        final String scmReleaseLabel = descriptor.getScmReleaseLabel();
        final String tagName = releaseVersions.get(key + ".tag").toString();
        final boolean skip = parseBoolean(releaseVersions.get(key + ".skip").toString());

        try {
            descriptor.setScmReleaseLabel(tagName);
            // mark projects that should not be deployed
            setDeploySkip(root, skip);
            procedure.perform();
        } catch (Exception e) {
            throw new ReleaseExecutionException(e.getMessage(), e);
        } finally {
            // rollback
            if (Objects.equals(descriptor.getScmReleaseLabel(), tagName))
                descriptor.setScmReleaseLabel(scmReleaseLabel);
        }
    }

    private static void setDeploySkip(final Element project, final boolean value) {
        final Namespace ns = project.getNamespace();
        Element properties = project.getChild("properties", ns);
        if (properties == null) {
            properties = new Element("properties", ns);
            project.addContent(properties);
        }

        Element property = properties.getChild(MAVEN_DEPLOY_SKIP, ns);
        if (property == null) {
            property = new Element(MAVEN_DEPLOY_SKIP, ns);
            properties.addContent(property);
        }

        property.setText(Boolean.toString(value));
    }
}
