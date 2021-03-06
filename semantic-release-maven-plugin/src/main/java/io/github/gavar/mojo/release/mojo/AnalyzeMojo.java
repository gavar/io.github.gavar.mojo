package io.github.gavar.mojo.release.mojo;

import io.github.gavar.mojo.io.HorizontalTable;
import io.github.gavar.mojo.release.model.ReleaseProject;
import io.github.gavar.mojo.release.util.GitUtils;
import io.github.gavar.mojo.release.util.ProjectUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.IOException;

import static io.github.gavar.mojo.release.util.ProjectUtils.toReleaseProject;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@Mojo(name = "analyze", requiresDirectInvocation = true, aggregator = true)
public class AnalyzeMojo extends BaseMojo {

    @Override
    protected void process() throws IOException {
        final Log log = getLog();

        final Git git = git();
        final Repository repository = git.getRepository();
        final RevWalk walk = new RevWalk(repository);

        final HorizontalTable table = new HorizontalTable();
        table.header("Name", "Version", "Tag", "Commit", "Deploy", "Release Version", "Dev Version");

        for (MavenProject project : session.getAllProjects()) {
            final ReleaseProject rp = toReleaseProject(project, root).analyze(git, walk);

            final String name = rp.getName();
            final String version = rp.getVersion();
            final String latestTag = rp.getLatestTagName();
            final String latestCommit = GitUtils.refHash(rp.getLatestRef(), 7);
            final String deploy = deployType(rp.getHasChanges(), rp.isDeploySkip());
            final String relVersion = rp.resolveRelVersion().toString();
            final boolean skip = rp.shouldSkip(relVersion);
            final String devVersion = rp.resolveDevVersion(relVersion, skip).toString();

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
