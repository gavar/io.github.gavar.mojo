package dev.gavar.mojo.release;

import dev.gavar.mojo.io.HorizontalTable;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.IOException;

import static dev.gavar.mojo.release.GitUtils.refHash;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@Mojo(name = "analyze", requiresDirectInvocation = true, aggregator = true)
public class AnalyzeMojo extends BaseMojo {

    @Override
    public void execute() throws MojoExecutionException {
        try {
            process();
        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected void process() throws IOException {
        final Log log = getLog();

        log.info("Loading GIT repository: " + root);
        final Git git = openGit();
        final Repository repository = git.getRepository();
        final RevWalk walk = new RevWalk(repository);

        final HorizontalTable table = new HorizontalTable();
        table.header("Name", "Version", "Tag", "Commit", "Deploy", "Release Version", "Dev Version");

        for (MavenProject project : session.getAllProjects()) {
            final ReleaseProject rp = toReleaseProject(project).analyze(git, walk);

            final String name = rp.getName();
            final String version = rp.getVersion();
            final String latestTag = rp.getLatestTagName();
            final String latestCommit = refHash(rp.getLatestRef(), 7);
            final String deploy = deployType(rp.getHasChanges(), rp.isSkipDeploy());
            final String relVersion = rp.getNextRelVersion().toString();
            final String devVersion = rp.getNextDevVersion().toString();

            table.row(name, version, latestTag, latestCommit, deploy, relVersion, devVersion);
        }

        // print summary
        final StringBuilder sb = new StringBuilder();
        sb.append("Projects Release Summary:\n");
        table.print(x -> sb.append(x).append('\n'));
        sb.setLength(sb.length() - 1);
        log.info(sb);
    }

    static String YesNo(Boolean condition) {
        return condition == TRUE ? "Yes"
                : condition == FALSE ? "No"
                : null;
    }

    static String deployType(Boolean changes, boolean skip) {
        return skip == TRUE ? "SKIP"
                : changes == TRUE ? "YES"
                : changes == FALSE ? "NO"
                : "NEW";
    }
}
