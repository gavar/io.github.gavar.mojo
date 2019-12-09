package dev.gavar.mojo.rc;

import liquibase.Liquibase;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.*;
import org.jooq.util.jaxb.tools.MiniJAXB;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import static java.util.Collections.emptyList;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GenerateMojo extends AbstractChangeLogMojo {

    /**
     * An external configuration file that is appended to anything from the
     * Maven configuration, using Maven's <code>combine.children="append"</code>
     * semantics.
     */
    @Parameter(property = "jooq.codegen.configurationFile")
    private File configurationFile;

    /**
     * An external set of configuration files that is appended to anything from
     * the Maven configuration, using Maven's
     * <code>combine.children="append"</code> semantics.
     */
    @Parameter(property = "jooq.codegen.configurationFiles")
    private List<File> configurationFiles;

    /** Whether to skip the execution of the Maven Plugin for this module. */
    @Parameter(property = "jooq.codegen.skip")
    private boolean skip;

    /** Logging threshold. */
    @Parameter(property = "jooq.codegen.logging")
    private Logging logging;

    /** On-error behavior. */
    @Parameter(property = "jooq.codegen.onError")
    private OnError onError;

    /** The generator settings. */
    @Parameter
    protected Generator generator;

    @Override
    public void execute() throws MojoExecutionException {
        final Log log = getLog();

        // load configuration files
        for (File file : getConfigurationFiles())
            read(file);

        Connection connection = null;
        try (JdbcDatabaseContainer container = createJdbcContainer()) {
            log.info(">> starting JDBC container");
            container.start();
            connection = connect(container);
            execute(connection);
        } finally {
            close(connection);
        }
    }

    private void execute(Connection connection) throws MojoExecutionException {
        final Log log = getLog();

        // apply liquibase migrations
        log.info(">> liquibase.update");
        final Liquibase liquibase = createLiquibase(connection);
        liquibaseUpdate(liquibase);

        // initialize code generation tool
        log.info(">> initializing codegen");
        Configuration configuration = createConfiguration();
        GenerationTool codegen = new GenerationTool();
        codegen.setConnection(connection);

        // generate classes
        log.info(">> run codegen");
        try {
            codegen.run(configuration);
        } catch (Exception cause) {
            throw new MojoExecutionException("error while JOOQ code generation", cause);
        }

        // add generated code as source code
        project.addCompileSourceRoot(generator.getTarget().getDirectory());
    }

    private Configuration createConfiguration() {
        if (generator == null)
            generator = new Generator();

        if (generator.getDatabase() == null)
            generator.setDatabase(new Database());

        if (generator.getTarget() == null)
            generator.setTarget(new Target());

        Target target = generator.getTarget();

        // resolve path to target
        File directory = new File(target.getDirectory());
        if (!directory.isAbsolute()) {
            directory = new File(project.getBasedir(), target.getDirectory());
            target.setDirectory(directory.getAbsolutePath());
        }

        return new Configuration()
            .withGenerator(generator);
    }

    private List<File> getConfigurationFiles() {
        if (configurationFiles != null && !configurationFiles.isEmpty())
            return configurationFiles;

        if (configurationFile != null)
            return List.of(configurationFile);

        return emptyList();
    }

    private void read(File file) throws MojoExecutionException {
        getLog().info("Reading external configuration: " + file);

        if (!file.isAbsolute())
            file = new File(project.getBasedir(), file.getPath());

        try (FileInputStream in = new FileInputStream(file);) {
            Configuration configuration = GenerationTool.load(in);
            generator = MiniJAXB.append(generator, configuration.getGenerator());
        } catch (IOException cause) {
            throw new MojoExecutionException("error while reading configuration file", cause);
        }
    }
}
