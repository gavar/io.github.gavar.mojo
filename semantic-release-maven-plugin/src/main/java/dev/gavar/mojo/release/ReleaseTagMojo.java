package dev.gavar.mojo.release;

import org.apache.maven.model.Model;
import org.apache.maven.model.Scm;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.mojo.versions.AbstractVersionsUpdaterMojo;
import org.codehaus.mojo.versions.api.PomHelper;
import org.codehaus.mojo.versions.rewriting.ModifiedPomXMLEventReader;

import java.io.File;
import java.util.Objects;

import static org.apache.maven.shared.utils.StringUtils.defaultString;

@Mojo(name = "release-tag")
public class ReleaseTagMojo extends AbstractVersionsUpdaterMojo {

    @Component
    private ScmManager manager;

    @Parameter(defaultValue = "false", property = "dryRun")
    private boolean dryRun;

    @Parameter(property = "generateBackupPoms", defaultValue = "false")
    private boolean generateBackupPoms;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        process(new File(project.getBasedir(), "pom.xml.tag"));
    }

    @Override
    protected void update(ModifiedPomXMLEventReader pom) throws MojoExecutionException {
        try {
            process(pom);
        } catch (MojoExecutionException e) {
            getLog().error(e);
            throw e;
        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected void process(ModifiedPomXMLEventReader pom) throws Exception {
        final Log log = getLog();

        Model model = PomHelper.getRawModel(pom);
        Scm scm = model.getScm();
        if (scm == null)
            throw new MojoFailureException("No <scm> was present");

        final String prevTag = scm.getTag();
        if (prevTag == null)
            throw new MojoFailureException("No <scm.tag> was present");

        // load properties
        final File cwd = new File(session.getExecutionRootDirectory());
        final File file = new File(cwd, "semantic.release.properties");
        final ReleaseProperties properties = new ReleaseProperties();
        properties.load(file, false);

        final String newTag = properties.getTag(project);
        final boolean deploy = !Objects.equals(prevTag, newTag);

        if (deploy) {
            log.info("Updating project tag " + prevTag + " > " + newTag);
            boolean success = PomHelper.setProjectValue(pom, "/project/scm/tag", newTag);
            if (!success) throw new MojoFailureException("Could not update the SCM tag");
        }

        if (deploy) {
            final ScmRepository scmRepository = getScmRepository();
            final ScmProvider scmProvider = manager.getProviderByRepository(scmRepository);
            final ScmFileSet scmFileSet = new ScmFileSet(cwd);
            ScmTagParameters scmTagParameters = new ScmTagParameters();
            scmTagParameters.setRemoteTagging(false);

            // create Tag in Scm
            if (dryRun) {
                log.info("Full run would create SCM tag: " + newTag);
            } else {
                final TagScmResult tsr = scmProvider.tag(scmRepository, scmFileSet, newTag, scmTagParameters);
                checkResult(tsr);
            }
        }
    }

    public ScmRepository getScmRepository() throws ScmException {
        ScmRepository repository = manager.makeScmRepository(getConnectionUrl());
        ScmProviderRepository provider = repository.getProviderRepository();
        provider.setPushChanges(false);
        return repository;
    }

    public String getConnectionUrl() {
        for (MavenProject p = project; p != null; p = p.getParent()) {
            final Scm scm = p.getScm();
            if (scm != null) {
                String connection = scm.getConnection();
                if (connection != null) return connection;
                connection = scm.getDeveloperConnection();
                if (connection != null) return connection;
            }

            if (p.isExecutionRoot())
                break;
        }

        throw new NullPointerException("You need to define a <scm.connection> or <scm.developerConnection>");
    }

    public void checkResult(ScmResult result) throws MojoExecutionException {
        if (!result.isSuccess()) {
            final Log log = getLog();
            final String message = defaultString(result.getProviderMessage());
            final String output = defaultString(result.getCommandOutput());
            log.error("Provider message:");
            log.error(message);
            log.error("Command output:");
            log.error(output);
            throw new MojoExecutionException("Command failed." + message);
        }
    }
}
