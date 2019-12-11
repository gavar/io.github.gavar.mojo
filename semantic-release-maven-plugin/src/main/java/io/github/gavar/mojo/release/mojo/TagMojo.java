package io.github.gavar.mojo.release.mojo;

import org.apache.maven.model.Scm;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;

@Mojo(name = "tag")
public class TagMojo extends AbstractScmMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(property = "message")
    protected String message;

    @Parameter(property = "remoteTagging", defaultValue = "true")
    protected boolean remoteTagging;

    protected void process() throws Exception {
        // do not tag if not SCM
        final Scm scm = project.getScm();
        if (scm == null) return;

        // do not create default tag
        final String tag = scm.getTag();
        if (tag == null || "HEAD".equalsIgnoreCase(tag)) return;
        getLog().info("Final Tag Name: '" + tag + "'");

        ScmRepository repository = getScmRepository();
        ScmProvider provider = manager.getProviderByRepository(repository);

        ScmTagParameters scmTagParameters = new ScmTagParameters(message);
        scmTagParameters.setRemoteTagging(remoteTagging);

        TagScmResult result = provider.tag(repository, getFileSet(), tag, scmTagParameters);
        if (!result.isSuccess())
            throw new MojoExecutionException(result.getProviderMessage());
    }
}
