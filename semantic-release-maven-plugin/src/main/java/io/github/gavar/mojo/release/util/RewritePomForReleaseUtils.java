package io.github.gavar.mojo.release.util;

import io.github.gavar.mojo.release.function.UnsafeProcedure;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.Map;
import java.util.Objects;

import static io.github.gavar.mojo.release.Constants.MAVEN_DEPLOY_SKIP;
import static io.github.gavar.mojo.release.Constants.MAVEN_TEST_SKIP;
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
            setProjectProperty(root, MAVEN_DEPLOY_SKIP, skip);
            // do not run tests when deploy skipped for quick deploys
            if (skip) setProjectProperty(root, MAVEN_TEST_SKIP, true);
            procedure.perform();
        } catch (Exception e) {
            throw new ReleaseExecutionException(e.getMessage(), e);
        } finally {
            // rollback
            if (Objects.equals(descriptor.getScmReleaseLabel(), tagName))
                descriptor.setScmReleaseLabel(scmReleaseLabel);
        }
    }

    private static void setProjectProperty(final Element root, final String name, final boolean value) {
        setProjectProperty(root, name, Boolean.toString(value));
    }

    private static void setProjectProperty(final Element root, final String name, final String value) {
        final Namespace ns = root.getNamespace();
        Element properties = root.getChild("properties", ns);
        if (properties == null) {
            properties = new Element("properties", ns)
                .addContent("\n  ");

            root.addContent("\n  ")
                .addContent(properties);
        }

        Element property = properties.getChild(name, ns);
        if (property == null) {
            property = new Element(name, ns);
            properties.addContent("  ")
                .addContent(property)
                .addContent("\n  ");
        }

        property.setText(value);
    }
}
