package dev.gavar.mojo.rc;

import dev.gavar.mojo.util.MojoUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.PluginParameterExpressionEvaluator;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.nio.file.Path;
import java.util.Properties;

import static dev.gavar.mojo.util.GenericUtils.arrify;
import static dev.gavar.mojo.util.MojoUtils.findProjectsRoot;

/**
 * Maven config that configures project properties by reading `.properties` files.
 * Plugin allow flexible configuration in a hierarchical manner.
 * "RC" stands for "run configuration".
 */
@Execute(phase = LifecyclePhase.INITIALIZE)
@Mojo(name = "properties", defaultPhase = LifecyclePhase.INITIALIZE)
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

    /**
     * List of outputs where to write accumulated properties.
     * {@code ${project.build.directory}/rc.properties} by default.
     */
    @Parameter
    protected OutputFiles[] outputs = {
            OutputFiles.files("${project.build.directory}/rc.properties")
    };

    /** List of containers where to inject accumulated properties. */
    @Parameter
    protected OutputInjection[] injections = {};
    public void setInjections(OutputInjection[] injections) { this.injections = injections; }
    public void setInjection(OutputInjection injection) { this.injections = arrify(injection, OutputInjection[]::new); }

    /** Whether {@link #sources} should be modular by default. */
    @Parameter(defaultValue = "true")
    protected Boolean modular;
    public Boolean getModular() { return modular; }
    public void setModular(Boolean modular) { this.modular = modular; }

    @Override
    public void execute() throws MojoExecutionException {
        Properties properties = collect();
        output(properties);
    }

    public Properties collect() throws MojoExecutionException {
        SourceProcessor processor = new SourceProcessor();
        processor.setLog(getLog());
        processor.setRoot(root != null ? root : findProjectsRoot(session.getAllProjects()));
        processor.setBase(session.getCurrentProject().getBasedir().toPath());
        processor.setModular(modular);
        processor.setVariants(variants);
        processor.setExtensions(extensions);

        try {
            return processor.process(this.sources);
        } catch (Throwable cause) {
            getLog().error(cause);
            throw new MojoExecutionException("error collecting properties", cause);
        }
    }

    public void output(Properties properties) throws MojoExecutionException {
        try {
            OutputProcessor processor = new OutputProcessor();
            processor.setSession(session);
            processor.setEvaluator(new PluginParameterExpressionEvaluator(session, execution));
            processor.process(properties, outputs, injections);
        } catch (Throwable cause) {
            getLog().error(cause);
            throw new MojoExecutionException("error writing properties", cause);
        }
    }
}
