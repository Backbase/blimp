package com.backbase.oss.blimp;

import java.io.File;
import java.util.List;
import lombok.Getter;
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
public class GenerateTestMojo extends AbstractGenerateMojo {

    /**
     * The base directory of the <i>changelog</i> files.
     */
    @Parameter(property = "blimp.testChangeLogDirectory",
        defaultValue = "${project.basedir}/src/test/resources",
        required = true)
    @Getter
    private File changeLogDirectory;

    /**
     * The location of the test output directory.
     */
    @Parameter(property = "blimp.testScriptsDirectory",
        defaultValue = "${project.build.directory}/generated-test-resources/blimp",
        required = true)
    @Getter
    private File scriptsDirectory;

    @Override
    protected void addOutputResource() {
        if (this.addTestResource) {
            this.project.addTestResource(createResource());
        }
    }

    @Override
    protected List<String> classpathElements() throws DependencyResolutionRequiredException {
        return this.project.getTestClasspathElements();
    }
}

