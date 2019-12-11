package io.github.gavar.mojo.release.mojo;

import org.apache.maven.model.Scm;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;

import static io.github.gavar.mojo.release.util.ProjectUtils.isDeploySkip;
import static java.lang.String.format;
import static org.codehaus.plexus.util.StringUtils.isBlank;

@Mojo(name = "tag")
public class TagMojo extends AbstractScmMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(property = "message")
    protected String message;

    @Parameter(property = "remoteTagging", defaultValue = "true")
    protected boolean remoteTagging;

    protected void process() throws Exception {
        final Log log = getLog();
        final String artifactId = project.getArtifactId();

        // do not tag if not SCM
        final Scm scm = project.getScm();
        if (scm == null) {
            log.info(format("'%s': skip creating deploy tag since <scm> is not defined", artifactId));
            return;
        }

        // do not create blank tag
        final String tag = scm.getTag();
        if (isBlank(tag)) {
            log.info(format("'%s': skip creating deploy tag since <scm.tag> is blank", artifactId));
            return;
        }

        // do not create default tag
        if ("HEAD".equalsIgnoreCase(tag)) {
            log.info(format("'%s': skip creating deploy tag since <scm.tag> is: %s", artifactId, tag));
            return;
        }

        // do not create tag when deploy skipped
        if (isDeploySkip(project)) {
            log.info(format("'%s': skip creating deploy tag since deploy is skipped", artifactId));
            return;
        }

        log.info(format("'%s': creating deploy tag: %s", artifactId, tag));
        ScmRepository repository = getScmRepository();
        ScmProvider provider = manager.getProviderByRepository(repository);

        ScmTagParameters scmTagParameters = new ScmTagParameters(message);
        scmTagParameters.setRemoteTagging(remoteTagging);

        TagScmResult result = provider.tag(repository, getFileSet(), tag, scmTagParameters);
        if (!result.isSuccess())
            throw new MojoExecutionException(result.getProviderMessage());
    }
}
