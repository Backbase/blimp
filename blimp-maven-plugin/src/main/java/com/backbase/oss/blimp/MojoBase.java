package com.backbase.oss.blimp;

import java.io.File;
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
     * Location of the output directory.
     */
    @Parameter(property = "blimp.outputDirectory",
        defaultValue = "${project.build.directory}/generated-resources/liquibase",
        required = true)
    protected File outputDirectory;

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
        }
    }


    protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;
}
