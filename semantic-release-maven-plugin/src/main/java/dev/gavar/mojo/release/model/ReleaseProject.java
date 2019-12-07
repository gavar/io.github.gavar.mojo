package dev.gavar.mojo.release.model;

import com.github.zafarkhaja.semver.Version;
import dev.gavar.mojo.release.util.GitUtils;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefDatabase;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static dev.gavar.mojo.release.util.SemanticGitLogUtils.AFFECTS_CODE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.unmodifiableList;
import static org.eclipse.jgit.lib.Constants.R_TAGS;

public class ReleaseProject {

    private final TreeFilter treeFilter;

    private final MavenProject mavenProject;
    public MavenProject getMavenProject() { return mavenProject; }

    private final ProjectConfig config;
    public ProjectConfig getConfig() { return config; }

    public ReleaseProject(File root, MavenProject mavenProject, ProjectConfig config) {
        this.config = config;
        this.mavenProject = mavenProject;
        this.treeFilter = createChangesFiler(root, mavenProject);
    }

    /** Reference of the latest release. */
    private Ref latestRef;
    public Ref getLatestRef() { return latestRef; }

    private String latestTagName;
    public String getLatestTagName() { return latestTagName; }

    private Boolean hasChanges;
    public boolean isChanged() { return hasChanges == TRUE; }
    public boolean isPristine() { return hasChanges != TRUE; }
    public Boolean getHasChanges() { return hasChanges; }
    public void setHasChanges(Boolean hasChanges) { this.hasChanges = hasChanges; }

    private RevCommit latestChange;
    public RevCommit getLatestChange() { return latestChange; }

    private Version lastReleaseVersion;
    public Version getLastReleaseVersion() {
        return lastReleaseVersion;
    }

    private Version nextRelVersion;
    public Version getNextRelVersion() {
        return nextRelVersion;
    }

    private Version nextDevVersion;
    public Version getNextDevVersion() {
        return nextDevVersion;
    }

    private List<Ref> tagRefs = List.of();
    public List<Ref> getTagRefs() { return tagRefs; }

    public String getNextRelTag() {
        return isDeploySkip()
                ? this.mavenProject.getScm().getTag()
                : tagNameFor(nextRelVersion);
    }

    public void setTagRefs(List<Ref> tagRefs) {
        this.hasChanges = null;
        this.tagRefs = tagRefs != null ? unmodifiableList(tagRefs) : List.of();

        if (this.tagRefs.isEmpty()) {
            latestRef = null;
            latestTagName = null;
            lastReleaseVersion = null;
        } else {
            final String prefix = config.getTagPrefix();
            latestRef = tagRefs.get(this.tagRefs.size() - 1);
            latestTagName = latestRef.getName().substring(R_TAGS.length());
            lastReleaseVersion = versionOf(latestTagName.substring(prefix.length()));
        }
    }

    public String getName() {
        return this.mavenProject.getName();
    }

    public String getVersion() {
        return this.mavenProject.getVersion();
    }

    public boolean isDeploySkip() {
        return this.config.isSkipDeploy();
    }

    public boolean shouldSkipTag() {
        return this.isDeploySkip() || this.isPristine();
    }

    public String tagNameFor(Version version) {
        return tagNameFor(version.toString());
    }

    public String tagNameFor(String version) {
        return this.config.getTagPrefix() + version;
    }

    public ReleaseProject analyze(Git git, RevWalk walk) throws IOException {
        final Repository repository = git.getRepository();
        final RefDatabase database = repository.getRefDatabase();
        final Ref head = database.exactRef("HEAD");

        analyzeTags(git);
        analyzeChanges(walk, head);
        resolveVersions();
        return this;
    }

    private void analyzeTags(Git git) throws IOException {
        final String prefix = config.getTagPrefix();
        final RefDatabase database = git.getRepository().getRefDatabase();
        setTagRefs(database.getRefsByPrefix(R_TAGS + prefix));
    }

    private void analyzeChanges(RevWalk walk, Ref head) throws IOException {
        if (!tagRefs.isEmpty()) {
            walk.reset();
            walk.setTreeFilter(treeFilter);
            walk.setRevFilter(AFFECTS_CODE.clone());
            latestChange = GitUtils.find(walk, tagRefs, head);
            hasChanges = latestChange != null;
        }
    }

    private void resolveVersions() {
        nextRelVersion = resolveNextRelVersion();
        nextDevVersion = resolveNextDevVersion();
    }

    private Version resolveNextRelVersion() {
        return isDeploySkip() ? normalVersionOf(getVersion())
                : tagRefs.isEmpty() ? versionOf("0.0.1")
                : hasChanges ? lastReleaseVersion.incrementPatchVersion()
                : lastReleaseVersion;
    }

    private Version resolveNextDevVersion() {
        final Version version = this.isDeploySkip() || this.isPristine()
                ? versionOf(getVersion())
                : nextRelVersion;

        return version.setPreReleaseVersion("SNAPSHOT");
    }

    private static TreeFilter createChangesFiler(final File root, final MavenProject mavenProject) {
        final Path rootPath = root.toPath();
        final Path projectPath = rootPath.relativize(mavenProject.getBasedir().toPath());

        List<TreeFilter> filters = new ArrayList<>();
        filters.add(TreeFilter.ANY_DIFF);
        add(filters, projectPath);

        List<PathFilter> excludes = new ArrayList<>();
        for (String module : mavenProject.getModel().getModules())
            add(excludes, projectPath.resolve(module));

        add(filters, not(group(excludes)));
        return and(filters);
    }

    static Version versionOf(String value) {
        return Version.valueOf(value);
    }
    static Version normalVersionOf(String value) {
        final Version version = versionOf(value);
        return versionOf(version.getNormalVersion());
    }

    static String patch(String version) {
        return Version.valueOf(version)
                .incrementPatchVersion()
                .toString();
    }

    static TreeFilter group(Collection<PathFilter> paths) {
        return paths == null || paths.isEmpty() ? null
                : PathFilterGroup.create(paths);
    }

    static TreeFilter not(TreeFilter filter) {
        return filter != null ? filter.negate() : null;
    }

    static TreeFilter and(TreeFilter a, TreeFilter b) {
        if (a == null) return b;
        if (b == null) return a;
        return AndTreeFilter.create(a, b);
    }

    static TreeFilter and(List<TreeFilter> list) {
        if (list.isEmpty()) return null;
        if (list.size() == 1) return list.get(0);
        return AndTreeFilter.create(list);
    }

    static <T extends TreeFilter> void add(Collection<T> collection, T filter) {
        if (filter != null)
            collection.add(filter);
    }

    @SuppressWarnings("unchecked")
    static <T extends TreeFilter> void add(Collection<T> collection, Path path) {
        add(collection, (T) toPathFilter(path));
    }

    static PathFilter toPathFilter(Path path) {
        final String s = path.toString();
        return s != null && s.length() > 0
                ? PathFilter.create(s)
                : null;
    }
}
