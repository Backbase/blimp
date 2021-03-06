package com.backbase.oss.blimp;

import java.io.File;
import java.util.List;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Generates all SQL scripts for all specified databases.
 * <p>
 * This mojo executes the following actions
 * <ol>
 * <li>generates the full creation script</li>
 * <li>collects all groups of changesets</li>
 * <li>for each group, generates one script containing all changes in that group</li>
 * </ol>
 * <br>
 * <b>What is a group?</b><br>
 * A group is a collection of changesets that are supposed to included in a release; they can be
 * either the labels of the changes or the contexts depending on the {@link #groupingStrategy}
 * configuration.
 */
@Mojo(name = "generate", requiresProject = true, defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GenerateMojo extends AbstractGenerateMojo {

    /**
     * Whether to add the SQL scripts as a resource of the project.
     */
    @Parameter(property = "blimp.addResource", defaultValue = "false")
    private boolean addResource;

    /**
     * Whether to add the SQL scripts as a test resource of the project.
     *
     * <p>
     * Use it when the generated SQL should be visible to the testing classpath, but not to the artifact
     * classpath.
     * </p>
     */
    @Parameter(property = "blimp.addTestResource", defaultValue = "false")
    protected boolean addTestResource;

    /**
     * The location of the <i>changelog</i> to execute.
     * <p>
     * Usually a file name relative to the input directory but it can also point to a classpath
     * resource.
     * </p>
     */
    @Parameter(property = "blimp.changeLogFile",
        defaultValue = "db.changelog-main.xml",
        required = true)
    private String changeLogFile;

    /**
     * The base directory of the <i>changelog</i> files.
     */
    @Parameter(property = "blimp.changeLogDirectory",
        defaultValue = "${project.basedir}/src/main/resources",
        required = true)
    private File changeLogDirectory;

    /**
     * The location of the output directory.
     */
    @Parameter(property = "blimp.scriptsDirectory",
        defaultValue = "${project.build.directory}/generated-resources/blimp",
        required = true)
    private File scriptsDirectory;

    @Override
    protected void addOutputResource() {
        if (this.addResource) {
            this.project.addResource(createResource());
        } else if (this.addTestResource) {
            this.project.addTestResource(createResource());
        }
    }

    @Override
    protected File changeLogDirectory() {
        return this.changeLogDirectory;
    }

    @Override
    protected String changeLogFile() {
        return this.changeLogFile;
    }

    @Override
    protected File scriptsDirectory() {
        return this.scriptsDirectory;
    }

    @Override
    protected List<String> classpathElements() throws DependencyResolutionRequiredException {
        return this.project.getRuntimeClasspathElements();
    }
}
