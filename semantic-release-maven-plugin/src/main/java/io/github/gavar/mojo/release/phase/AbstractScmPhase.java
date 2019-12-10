package io.github.gavar.mojo.release.phase;

import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.env.ReleaseEnvironment;
import org.apache.maven.shared.release.phase.AbstractReleasePhase;
import org.apache.maven.shared.release.scm.ScmRepositoryConfigurator;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;

import java.io.IOException;
import java.util.List;

import static org.apache.maven.shared.release.util.ReleaseUtil.*;

public abstract class AbstractScmPhase extends AbstractReleasePhase {

    /** Tool that gets a configured SCM repository from release configuration. */
    @Requirement
    private ScmRepositoryConfigurator scmRepositoryConfigurator;

    public String commonDir(List<MavenProject> projects) throws ReleaseExecutionException {
        try {
            return getCommonBasedir(projects);
        } catch (IOException e) {
            throw new ReleaseExecutionException(e.getMessage(), e);
        }
    }

    public ScmRepository getScmRepository(final ReleaseDescriptor descriptor,
                                          final ReleaseEnvironment environment,
                                          final List<MavenProject> reactorProjects) throws ReleaseExecutionException {
        return getScmRepository(descriptor, environment, descriptor.getWorkingDirectory());
    }

    public ScmRepository getScmRepository(final ReleaseDescriptor descriptor,
                                          final ReleaseEnvironment environment,
                                          final String dir) throws ReleaseExecutionException {
        try {
            final String workingDirectory = FileUtils.normalize(descriptor.getWorkingDirectory());
            final int parentCount = getBaseWorkingDirectoryParentCount(dir, workingDirectory);
            final String scmSourceUrl = realignScmUrl(parentCount, descriptor.getScmSourceUrl());
            final ScmRepository repository = scmRepositoryConfigurator.getConfiguredRepository(scmSourceUrl, descriptor, environment.getSettings());
            final ScmProviderRepository providerRepository = repository.getProviderRepository();
            providerRepository.setPushChanges(descriptor.isPushChanges());
            return repository;
        } catch (ScmException e) {
            throw new ReleaseExecutionException(e.getMessage(), e);
        }
    }

    public ScmProvider getScmProvider(final ScmRepository repository) throws ReleaseExecutionException {
        try {
            return scmRepositoryConfigurator.getRepositoryProvider(repository);
        } catch (ScmException e) {
            throw new ReleaseExecutionException(e.getMessage(), e);
        }
    }

    protected void logError(ReleaseResult result, String message) {
        result.appendError(message);
        getLogger().error(message);
    }
}
