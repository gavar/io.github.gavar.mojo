package io.github.gavar.mojo.release.phase;

import io.github.gavar.mojo.release.util.RewritePomForReleaseUtils;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.phase.RewritePomsForReleasePhase;
import org.jdom.Element;
import org.jdom.Namespace;

public class RewritePomsForSemanticReleasePhase extends RewritePomsForReleasePhase {

    @Override
    protected void transformScm(MavenProject project, Element root, Namespace namespace,
                                ReleaseDescriptor descriptor, String key, ScmRepository repository,
                                ReleaseResult result, String commonBasedir) throws ReleaseExecutionException {
        RewritePomForReleaseUtils.transformScm(
            key, root, descriptor,
            () -> super.transformScm(project, root, namespace, descriptor, key, repository, result, commonBasedir));
    }
}
