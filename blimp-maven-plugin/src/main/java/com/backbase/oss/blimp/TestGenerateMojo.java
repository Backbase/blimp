package com.backbase.oss.blimp;

import java.io.File;
import java.util.List;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * The equivalent of {@code generate} goal, but for testing purposes.
 */
@Mojo(name = "test-generate", requiresProject = true, defaultPhase = LifecyclePhase.GENERATE_TEST_RESOURCES,
    requiresDependencyResolution = ResolutionScope.TEST)
public class TestGenerateMojo extends AbstractGenerateMojo {

    /**
     * Whether to add the SQL scripts as a test resource of the project.
     */
    @Parameter(property = "blimp.addTestResource", defaultValue = "false")
    private boolean addTestResource;

    /**
     * The location of the <i>changelog</i> to execute.
     * <p>
     * Usually a file name relative to the input directory but it can also point to a classpath
     * resource.
     * </p>
     */
    @Parameter(property = "blimp.testChangeLogFile", defaultValue = "db.changelog-test.xml", required = true)
    private String testChangeLogFile;

    /**
     * The base directory of the <i>changelog</i> files.
     */
    @Parameter(property = "blimp.testChangeLogDirectory",
        defaultValue = "${project.basedir}/src/test/resources",
        required = true)
    private File testChangeLogDirectory;

    /**
     * The location of the test output directory.
     */
    @Parameter(property = "blimp.testScriptsDirectory",
        defaultValue = "${project.build.directory}/generated-test-resources/blimp",
        required = true)
    private File testScriptsDirectory;

    @Override
    protected void addOutputResource() {
        if (this.addTestResource) {
            this.project.addTestResource(createResource());
        }
    }

    @Override
    protected File changeLogDirectory() {
        return this.testChangeLogDirectory;
    }

    @Override
    protected String changeLogFile() {
        return this.testChangeLogFile;
    }

    @Override
    protected File scriptsDirectory() {
        return this.testScriptsDirectory;
    }

    @Override
    protected List<String> classpathElements() throws DependencyResolutionRequiredException {
        return this.project.getTestClasspathElements();
    }
}

