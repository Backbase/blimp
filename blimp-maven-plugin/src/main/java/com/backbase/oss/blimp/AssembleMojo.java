package com.backbase.oss.blimp;

import static java.util.Optional.ofNullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.FileSet;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.archiver.util.DefaultFileSet;

/**
 * Creates an archive containing the generated SQL files.
 */
@Mojo(name = "assemble", requiresProject = true, defaultPhase = LifecyclePhase.PACKAGE)
public class AssembleMojo extends MojoBase {
    @Component
    private ArchiverManager archiverManager;

    /**
     * Specifies the formats of the archive.
     * <p>
     * Multiple formats can be supplied and the goal {@code assemble} will generate an archive for each
     * desired formats.
     * </p>
     * <p>
     * A format is specified by supplying one of the following values in a &lt;format&gt; subelement:
     * <ul>
     * <li><b>zip</b> creates a ZIP file format</li>
     * <li><b>tar</b> creates a TAR file format</li>
     * <li><b>tar.gz</b> creates a Gzip TAR file format</li>
     * <li><b>tar.xz</b> creates a Xz TAR file format</li>
     * <li><b>tar.bz2</b> creates a Bzip2 TAR file format</li>
     * </p>
     */
    @Parameter(property = "blimp.formats", defaultValue = "zip")
    private List<String> formats;

    /**
     * The classifier of the archive artifact.
     */
    @Parameter(property = "blimp.classifier", defaultValue = "sql")
    private String classifier;

    /**
     * Whether to attach the produced archives as artifacts.
     */
    @Parameter(property = "blimp.attach", defaultValue = "true")
    private boolean attach;

    @Override
    protected void doExecute() throws MojoExecutionException {
        for (final String format : this.formats) {
            try {
                createAssembly(format);
            } catch (final NoSuchArchiverException | ArchiverException | IOException e) {
                throw new MojoExecutionException(format, e);
            }
        }
    }

    private void createAssembly(String format) throws NoSuchArchiverException, ArchiverException, IOException {
        final String archiveName = this.project.getBuild().getFinalName()
            + ofNullable(this.classifier).map(c -> "-" + c).orElse("")
            + "." + format;
        final File archive = new File(this.project.getBuild().getDirectory(), archiveName);
        final Archiver archiver = this.archiverManager.getArchiver(archive);
        final FileSet fileSet = DefaultFileSet
            .fileSet(this.scriptsDirectory)
            .prefixed(this.serviceName + "/")
            .include(new String[] {SQL_FILES})
            .includeEmptyDirs(false);

        archiver.setDestFile(archive);
        archiver.addFileSet(fileSet);
        archiver.createArchive();

        if (this.attach) {
            this.projectHelper.attachArtifact(this.project, format, this.classifier, archive);
        }

        this.buildContext.refresh(archive);
    }
}
