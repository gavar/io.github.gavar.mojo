package io.github.gavar.mojo.rc;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;

import static io.github.gavar.mojo.validate.Preconditions.checkArgument;
import static io.github.gavar.mojo.validate.Validate.argumentNotBlank;
import static io.github.gavar.mojo.validate.Validate.argumentOfType;

public abstract class AbstractTestContainerMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    /** Name of the class implementing {@link JdbcDatabaseContainer}. */
    @Parameter(property = "jdbc.container", required = true)
    protected String jdbcContainer;

    /** Name of the docker image containing JDBC container to run. */
    @Parameter(property = "jdbc.docker-image-name", required = true)
    protected String dockerImageName;

    protected JdbcDatabaseContainer createJdbcContainer() throws MojoExecutionException {
        argumentNotBlank(jdbcContainer, "jdbcContainer");
        argumentNotBlank(dockerImageName, "dockerImageName");
        try {
            // resolve constructor
            final ClassLoader classLoader = this.getClass().getClassLoader();
            final Class<?> jdbcContainerClass = classLoader.loadClass(this.jdbcContainer);
            final Constructor<?> constructor = jdbcContainerClass.getConstructor();

            // instantiate
            final Object instance = constructor.newInstance();
            argumentOfType(instance, JdbcDatabaseContainer.class, "jdbcContainer");
            final JdbcDatabaseContainer container = (JdbcDatabaseContainer) instance;

            // configure
            container.setDockerImageName(dockerImageName);
            return container;
        } catch (ReflectiveOperationException cause) {
            getLog().error(cause);
            throw new MojoExecutionException("unable to instantiate JDBC container", cause);
        }
    }

    protected Connection connect(final JdbcDatabaseContainer container) throws MojoExecutionException {
        try {
            checkArgument(container.isRunning(), "JDBC container is not running");
            return container.createConnection("");
        } catch (SQLException cause) {
            getLog().error(cause);
            throw new MojoExecutionException("unable to create JDBC connection", cause);
        }
    }

    protected void close(final Connection connection) throws MojoExecutionException {
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException cause) {
            throw new MojoExecutionException("unable to close JDBC connection", cause);
        }
    }
}
