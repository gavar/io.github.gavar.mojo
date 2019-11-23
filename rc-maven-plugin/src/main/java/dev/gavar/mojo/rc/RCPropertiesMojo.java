package dev.gavar.mojo.rc;

import dev.gavar.mojo.io.ConfigurableConfigurationLoader;
import dev.gavar.mojo.util.MojoUtils;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkArgument;
import static dev.gavar.mojo.util.GenericUtils.valueOrDefault;
import static dev.gavar.mojo.util.MojoUtils.findProjectsRoot;

/**
 * Maven config that configures project properties by reading `.properties` files.
 * Plugin allow flexible configuration in a hierarchical manner.
 * "RC" stands for "run configuration".
 */
@Execute(phase = LifecyclePhase.INITIALIZE)
@Mojo(name = "rc-properties", defaultPhase = LifecyclePhase.INITIALIZE)
public class RCPropertiesMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;

    @Parameter(defaultValue = "${mojoExecution}", readonly = true, required = true)
    protected MojoExecution execution;

    /**
     * Path to the directory where to stop searching for a configuration files.
     * {@link MojoUtils#findProjectsRoot} by default.
     */
    @Parameter
    protected Path root;

    /**
     * List of variants of the configuration file names.
     * All variants found will be flattened into one object,
     * so that variants earlier in this list override later ones.
     * May be overridden by explicitly setting {@link PropertyFileSet#variants}.
     */
    @Parameter
    protected String[] variants = {};
    public void setVariant(String variant) { this.variants = new String[]{variant}; }
    public void setVariants(String[] variants) { this.variants = variants; }

    /**
     * List of the file extensions to consider as configuration files.
     * All variants found will be flattened into one object,
     * so that variants earlier in this list override later ones.
     * May be overridden by explicitly setting {@link PropertyFileSet#extensions}.
     */
    @Parameter
    protected String[] extensions = {
            ".rc.json",
            ".rc.xml",
            ".rc.yml",
            ".rc.yaml",
            ".rc.ini",
            ".rc.properties",
    };
    public void setExtension(String extension) { this.extensions = new String[]{extension}; }
    public void setExtensions(String[] extensions) { this.extensions = extensions; }

    /**
     * List of sources defining a files to search for.
     * All sources found will be flattened into one object,
     * so that variants earlier in this list override later ones.
     */
    @Parameter
    protected PropertyFileSet[] sources = {
            PropertyFileSet.files("env", "rc/env"),
            PropertyFileSet.files("default", "rc/default"),
    };

    /** Whether {@link #sources} should be modular by default. */
    @Parameter(defaultValue = "true")
    protected Boolean modular;
    public Boolean getModular() { return modular; }
    public void setModular(Boolean modular) { this.modular = modular; }

    @Override
    public void execute() throws MojoExecutionException {
        Properties properties;
        try {
            properties = this.process();
        } catch (Throwable cause) {
            getLog().error(cause);
            throw new MojoExecutionException("execution error", cause);
        }

        // write to project properties
        project.getProperties().putAll(properties);
    }

    protected Properties process() throws ConfigurationException {
        // configure
        ConfigurableConfigurationLoader loader = new ConfigurableConfigurationLoader();
        PropertiesVisitor visitor = new PropertiesVisitor(loader, getLog());
        Path root = this.root != null ? this.root : findProjectsRoot(session.getAllProjects());
        Path base = session.getCurrentProject().getBasedir().toPath();

        List<Entry> items = new ArrayList<>();
        for (PropertyFileSet source : sources) {
            // collect items to visit
            items.clear();
            items = collect(items, source);
            visitEach(base, visitor, items);
            visitModularOnly(root, base.getParent(), visitor, items);
        }

        // merge properties
        return visitor.toProperties();
    }

    protected List<Entry> collect(List<Entry> items) {
        for (PropertyFileSet source : sources)
            items = collect(items, source);
        return items;
    }

    protected List<Entry> collect(List<Entry> items, PropertyFileSet source) {
        return collect(items, source,
                valueOrDefault(source.variants, this.variants),
                valueOrDefault(source.extensions, this.extensions),
                valueOrDefault(source.modular, this.modular)
        );
    }

    protected List<Entry> collect(List<Entry> items, PropertyFileSet source,
                                  String[] variants, String[] extensions, Boolean modular) {
        for (PropertyFile file : source.files)
            items = collect(items, file,
                    valueOrDefault(file.variants, variants),
                    valueOrDefault(file.extensions, extensions),
                    valueOrDefault(file.modular, modular)
            );
        return items;
    }

    protected List<Entry> collect(List<Entry> items, PropertyFile file,
                                  String[] variants, String[] extensions, Boolean modular) {
        Entry item = new Entry();

        item.file = file;
        item.variants = file.shouldTryVariants() ? variants : null;
        item.extensions = file.shouldTryExtensions() ? extensions : null;
        item.modular = file.isModular(modular);
        item.warn = file.getUrl() != null || file.isAbsolute();

        items.add(item);
        return items;
    }

    private void visitEach(Path base, PropertiesVisitor visitor, List<Entry> items) throws ConfigurationException {
        for (Entry item : items)
            visit(item, base, visitor);
    }

    private void visitModularOnly(Path root, Path base, PropertiesVisitor visitor, List<Entry> items) throws ConfigurationException {
        checkArgument(Files.isDirectory(root), "root should be a directory");
        checkArgument(Files.isDirectory(base), "base should be a directory");

        // visit every path until root
        for (; base.startsWith(root); base = base.getParent())
            for (Entry item : items) {
                if (item.modular) {
                    String rel = item.file.getPath();
                    Path absolute = base.resolve(rel);
                    visitor.visit(absolute, item.variants, item.extensions);
                }
            }
    }

    private void visit(Entry item, Path base, PropertiesVisitor visitor) throws ConfigurationException {
        final URL url = item.file.getUrl();
        final String path = item.file.getPath();
        final boolean absolute = item.file.isAbsolute();

        if (url != null) visit(url, visitor, item);
        else if (absolute) visit(path, visitor, item);
        else visit(base.resolve(path), visitor, item);
    }

    private void visit(String path, PropertiesVisitor visitor, Entry item) throws ConfigurationException {
        boolean success = visitor.visit(path, item.variants, item.extensions) > 0;
        track(path, success, item);
    }

    private void visit(Path path, PropertiesVisitor visitor, Entry item) throws ConfigurationException {
        boolean success = visitor.visit(path, item.variants, item.extensions) > 0;
        track(path, success, item);
    }

    private void visit(URL url, PropertiesVisitor visitor, Entry item) throws ConfigurationException {
        boolean success = visitor.visit(url, item.variants, item.extensions) > 0;
        track(url, success, item);
    }

    private void track(Object path, boolean ok, Entry item) {
        if (!ok && item.warn)
            getLog().info("[-] " + path);
    }

    private static class Entry {
        public PropertyFile file;
        public String[] variants;
        public String[] extensions;
        public boolean modular;
        public boolean warn;
    }
}
