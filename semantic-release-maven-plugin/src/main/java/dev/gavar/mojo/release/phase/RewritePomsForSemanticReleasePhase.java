package dev.gavar.mojo.release.phase;

import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.phase.RewritePomsForReleasePhase;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.Objects;

public class RewritePomsForSemanticReleasePhase extends RewritePomsForReleasePhase {

    @Override
    @SuppressWarnings("DuplicatedCode")
    protected void transformScm(MavenProject project, Element root, Namespace namespace,
                                ReleaseDescriptor descriptor, String key, ScmRepository repository,
                                ReleaseResult result, String commonBasedir) throws ReleaseExecutionException {
        final String scmReleaseLabel = descriptor.getScmReleaseLabel();
        final String tagName = Objects.toString(descriptor.getReleaseVersions().get(key + ".tag"), null);
        try {
            // rewrite release label to properly update <scm.tag>
            if (tagName != null) descriptor.setScmReleaseLabel(tagName);
            super.transformScm(project, root, namespace, descriptor, key, repository, result, commonBasedir);
        } finally {
            // rollback
            if (Objects.equals(descriptor.getScmReleaseLabel(), tagName))
                descriptor.setScmReleaseLabel(scmReleaseLabel);
        }
    }
}
