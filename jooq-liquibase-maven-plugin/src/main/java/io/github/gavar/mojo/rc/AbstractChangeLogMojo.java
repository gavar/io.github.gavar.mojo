package io.github.gavar.mojo.rc;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import java.sql.Connection;

public abstract class AbstractChangeLogMojo extends AbstractTestContainerMojo {

    /** Specifies the change log file to use for Liquibase. */
    @Parameter(property = "liquibase.changeLogFile")
    protected String changeLogFile;

    /**
     * The Liquibase contexts to execute, which can be "," separated if multiple contexts
     * are required. If no context is specified then ALL contexts will be executed.
     */
    @Parameter(property = "liquibase.contexts", defaultValue = "")
    protected String contexts;

    /**
     * The Liquibase labels to execute, which can be "," separated if multiple labels
     * are required or a more complex expression. If no label is specified then ALL all will be executed.
     */
    @Parameter(property = "liquibase.labels", defaultValue = "")
    protected String labels;

    protected Liquibase createLiquibase(Connection connection) throws MojoExecutionException {
        try {
            final JdbcConnection jdbcConnection = new JdbcConnection(connection);
            final DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
            final Database database = databaseFactory.findCorrectDatabaseImplementation(jdbcConnection);
            final ResourceAccessor resourceAccessor = new FileSystemResourceAccessor();
            return new Liquibase(changeLogFile, resourceAccessor, database);
        } catch (DatabaseException e) {
            throw new MojoExecutionException("liquibase initialization error", e);
        }
    }

    protected void liquibaseUpdate(Liquibase liquibase) throws MojoExecutionException {
        final Contexts contexts = new Contexts(this.contexts);
        final LabelExpression labelExpression = new LabelExpression(labels);
        try {
            liquibase.update(contexts, labelExpression);
        } catch (LiquibaseException cause) {
            throw new MojoExecutionException("unable to run liquibase update", cause);
        }
    }
}
