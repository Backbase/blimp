package com.backbase.oss.blimp;

import java.net.URI;
import java.net.URL;
import liquibase.configuration.LiquibaseConfiguration;
import lombok.SneakyThrows;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.sonatype.plexus.build.incremental.BuildContext;

public abstract class MojoBase extends AbstractMojo {
    protected static final String SQL_FILES = "**/*.sql";

    @SneakyThrows
    protected static URL toURL(URI uri) {
        return uri.toURL();
    }

    @Parameter(property = "project", readonly = true)
    protected MavenProject project;

    @Component
    protected BuildContext buildContext;

    @Component
    protected MavenProjectHelper projectHelper;

    /**
     * Skip the execution.
     */
    @Parameter(property = "blimp.skip")
    private boolean skip;

    /**
     * The name of the service.
     */
    @Parameter(property = "blimp.serviceName",
        defaultValue = "${project.artifactId}",
        required = true)
    protected String serviceName;


    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        if (this.skip) {
            getLog().info("Execution is skipped.");

            return;
        }

        try {
            doExecute();
        } catch (MojoExecutionException | MojoFailureException e) {
            getLog().error(e);

            throw e;
        } catch (final Exception e) {
            getLog().error(e);

            throw new MojoExecutionException(this.serviceName, e);
        } finally {
            LiquibaseConfiguration.setInstance(null);
        }
    }

    protected abstract void doExecute() throws Exception;
}
