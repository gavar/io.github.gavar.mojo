package dev.gavar.mojo.release.mojo;

import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.deploy.ArtifactDeployerException;
import org.apache.maven.shared.transfer.project.NoFileAssignedException;
import org.apache.maven.shared.transfer.project.deploy.ProjectDeployer;
import org.apache.maven.shared.transfer.project.deploy.ProjectDeployerRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mojo(name = "deploy", defaultPhase = LifecyclePhase.DEPLOY, threadSafe = true)
public class DeployMojo extends AbstractMojo {

    protected static final Pattern ALT_REPO_SYNTAX_PATTERN = Pattern.compile("(.+)::(.+)");
    protected static final AtomicInteger READYPROJECTSCOUNTER = new AtomicInteger();
    protected static final List<ProjectDeployerRequest> DEPLOYREQUESTS = Collections.synchronizedList(new ArrayList<>());

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;

    @Parameter(defaultValue = "${reactorProjects}", required = true, readonly = true)
    protected List<MavenProject> reactorProjects;

    /** Flag whether Maven is currently in online/offline mode. */
    @Parameter(defaultValue = "${settings.offline}", readonly = true)
    protected boolean offline;

    /**
     * Parameter used to control how many times a failed deployment will be retried before giving up and failing. If a
     * value outside the range 1-10 is specified it will be pulled to the nearest value within the range 1-10.
     */
    @Parameter(property = "retryFailedDeploymentCount", defaultValue = "1")
    protected int retryFailedDeploymentCount;

    /**
     * Whether every project should be deployed during its own deploy-phase or at the end of the multimodule build. If
     * set to {@code true} and the build fails, none of the reactor projects is deployed.
     * <strong>(experimental)</strong>
     * @since 2.8
     */
    @Parameter(defaultValue = "false", property = "deployAtEnd")
    protected boolean deployAtEnd;

    /**
     * Specifies an alternative repository to which the project artifacts should be deployed ( other than those
     * specified in &lt;distributionManagement&gt; ). <br/>
     * Format: id::layout::url
     * <dl>
     * <dt>id</dt>
     * <dd>The id can be used to pick up the correct credentials from the settings.xml</dd>
     * <dt>url</dt>
     * <dd>The location of the repository</dd>
     * </dl>
     */
    @Parameter(property = "altDeploymentRepository")
    protected String altDeploymentRepository;

    /**
     * The alternative repository to use when the project has a snapshot version.
     * @see #altDeploymentRepository
     */
    @Parameter(property = "altSnapshotDeploymentRepository")
    protected String altSnapshotDeploymentRepository;

    /**
     * The alternative repository to use when the project has a final version.
     * @see #altDeploymentRepository
     */
    @Parameter(property = "altReleaseDeploymentRepository")
    protected String altReleaseDeploymentRepository;

    /** Set this to 'true' to bypass artifact deploy */
    @Parameter(property = "maven.deploy.skip", defaultValue = "false")
    protected boolean skip;

    /** Component used to deploy project. */
    @Component
    protected ProjectDeployer projectDeployer;

    public void execute() throws MojoExecutionException, MojoFailureException {
        boolean addedDeployRequest = false;
        if (skip) {
            getLog().info("Skipping artifact deployment");
        } else {
            failIfOffline();

            ProjectDeployerRequest pdr = new ProjectDeployerRequest()
                .setProject(project)
                .setRetryFailedDeploymentCount(retryFailedDeploymentCount)
                .setAltReleaseDeploymentRepository(altReleaseDeploymentRepository)
                .setAltSnapshotDeploymentRepository(altSnapshotDeploymentRepository)
                .setAltDeploymentRepository(altDeploymentRepository);

            ArtifactRepository repo = getDeploymentRepository(pdr);

            if (!deployAtEnd) {
                deployProject(session.getProjectBuildingRequest(), pdr, repo);
            } else {
                DEPLOYREQUESTS.add(pdr);
                addedDeployRequest = true;
            }
        }

        boolean projectsReady = READYPROJECTSCOUNTER.incrementAndGet() == reactorProjects.size();
        if (projectsReady) {
            synchronized (DEPLOYREQUESTS) {
                while (!DEPLOYREQUESTS.isEmpty()) {
                    ArtifactRepository repo = getDeploymentRepository(DEPLOYREQUESTS.get(0));

                    deployProject(session.getProjectBuildingRequest(), DEPLOYREQUESTS.remove(0), repo);
                }
            }
        } else if (addedDeployRequest) {
            getLog().info("Deploying " + project.getGroupId() + ":" + project.getArtifactId() + ":"
                + project.getVersion() + " at end");
        }
    }

    protected void deployProject(final ProjectBuildingRequest pbr,
                                 final ProjectDeployerRequest pir,
                                 final ArtifactRepository repo) throws MojoExecutionException {
        try {
            projectDeployer.deploy(pbr, pir, repo);
        } catch (NoFileAssignedException | ArtifactDeployerException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected ArtifactRepository getDeploymentRepository(ProjectDeployerRequest pdr)
        throws MojoExecutionException, MojoFailureException {
        final MavenProject project = pdr.getProject();
        final String altDeploymentRepository = pdr.getAltDeploymentRepository();
        final String altReleaseDeploymentRepository = pdr.getAltReleaseDeploymentRepository();
        final String altSnapshotDeploymentRepository = pdr.getAltSnapshotDeploymentRepository();

        ArtifactRepository repo = null;

        final String altDeploymentRepo;
        if (ArtifactUtils.isSnapshot(project.getVersion()) && altSnapshotDeploymentRepository != null)
            altDeploymentRepo = altSnapshotDeploymentRepository;
        else if (!ArtifactUtils.isSnapshot(project.getVersion()) && altReleaseDeploymentRepository != null)
            altDeploymentRepo = altReleaseDeploymentRepository;
        else
            altDeploymentRepo = altDeploymentRepository;

        if (altDeploymentRepo != null) {
            getLog().info("Using alternate deployment repository " + altDeploymentRepo);

            Matcher matcher = ALT_REPO_SYNTAX_PATTERN.matcher(altDeploymentRepo);

            if (matcher.matches()) {
                String id = matcher.group(1).trim();
                String url = matcher.group(2).trim();
                repo = createDeploymentArtifactRepository(id, url);
            } else {
                throw new MojoFailureException(altDeploymentRepo, "Invalid syntax for repository.",
                    "Invalid syntax for alternative repository. Use \"id::url\".");
            }
        }

        if (repo == null)
            repo = project.getDistributionManagementArtifactRepository();

        if (repo == null)
            throw new MojoExecutionException(""
                + "Deployment failed: repository element was not specified in the POM inside "
                + "distributionManagement element or in -DaltDeploymentRepository=id::layout::url parameter"
            );

        return repo;
    }

    protected void failIfOffline() throws MojoFailureException {
        if (offline)
            throw new MojoFailureException("Cannot deploy artifacts when Maven is in offline mode");
    }

    protected ArtifactRepository createDeploymentArtifactRepository(String id, String url) {
        return new MavenArtifactRepository(id, url, new DefaultRepositoryLayout(), new ArtifactRepositoryPolicy(),
            new ArtifactRepositoryPolicy());
    }
}
