package io.github.gavar.mojo.release.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.utils.StringUtils;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;

import java.io.File;
import java.io.IOException;

public abstract class AbstractScmMojo extends AbstractMojo {

    @Parameter(property = "connectionUrl", defaultValue = "${project.scm.connection}")
    protected String connectionUrl;

    @Parameter(property = "workingDirectory")
    protected File workingDirectory;

    @Parameter(property = "username")
    protected String username;

    @Parameter(property = "password")
    protected String password;

    @Component
    protected ScmManager manager;

    @Component(hint = "mng-4384")
    protected SecDispatcher secDispatcher;

    @Parameter(property = "basedir", required = true)
    private File basedir;

    @Parameter(defaultValue = "${settings}", readonly = true)
    protected Settings settings;

    @Parameter(property = "pushChanges", defaultValue = "true")
    protected boolean pushChanges;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            process();
        } catch (MojoExecutionException e) {
            getLog().error(e);
            throw e;
        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected abstract void process() throws Exception;

    public File getWorkingDirectory() {
        return workingDirectory == null ? basedir : workingDirectory;
    }

    public ScmFileSet getFileSet() {
        return new ScmFileSet(getWorkingDirectory());
    }

    public ScmRepository getScmRepository() throws ScmException {
        ScmRepository repository = manager.makeScmRepository(connectionUrl);
        ScmProviderRepository providerRepo = repository.getProviderRepository();

        providerRepo.setPushChanges(pushChanges);

        if (repository.getProviderRepository() instanceof ScmProviderRepositoryWithHost) {
            ScmProviderRepositoryWithHost repo = (ScmProviderRepositoryWithHost) repository.getProviderRepository();
            loadInfosFromSettings(repo);

            if (!StringUtils.isEmpty(username))
                repo.setUser(username);

            if (!StringUtils.isEmpty(password))
                repo.setPassword(password);
        }

        return repository;
    }

    private void loadInfosFromSettings(ScmProviderRepositoryWithHost repo) {
        if (username == null || password == null) {
            String host = repo.getHost();
            int port = repo.getPort();
            if (port > 0) host += ":" + port;

            Server server = this.settings.getServer(host);
            if (server != null) {
                if (username == null) username = server.getUsername();
                if (password == null) password = decrypt(server.getPassword(), host);
            }
        }
    }

    private String decrypt(String str, String server) {
        try {
            return secDispatcher.decrypt(str);
        } catch (SecDispatcherException e) {
            getLog().warn("Failed to decrypt password/passphrase for server " + server + ", using auth token as is");
            return str;
        }
    }
}
