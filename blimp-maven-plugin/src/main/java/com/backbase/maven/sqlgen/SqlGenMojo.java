package com.backbase.maven.sqlgen;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;

import com.backbase.maven.sqlgen.LiquibaseUpdate.LiquibaseUpdateBuilder;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import liquibase.resource.FileSystemResourceAccessor;
import lombok.SneakyThrows;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.FileSet;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.archiver.util.DefaultFileSet;
import org.codehaus.plexus.util.Scanner;
import org.sonatype.plexus.build.incremental.BuildContext;

@Mojo(name = "generate", requiresProject = true, defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class SqlGenMojo extends AbstractMojo {
    private static final String SQL_FILES = "**/*.sql";

    @Parameter(property = "project", readonly = true)
    private MavenProject project;

    @Component
    private ArchiverManager archiverManager;
    @Component
    private MavenProjectHelper projectHelper;
    @Component
    private BuildContext buildContext;

    @Parameter(property = "sqlgen.skip")
    boolean skip;

    @Parameter(property = "sqlgen.changeLogFile", defaultValue = "db.changelog-persistence.xml", required = true)
    String changeLogFile;

    @Parameter(property = "sqlgen.inputDirectory",
        defaultValue = "${project.basedir}/src/main/resources",
        required = true)
    File inputDirectory;

    @Parameter(property = "sqlgen.inputPatterns",
        defaultValue = "**/*.sql,**/db.changelog*.xml,**/db.changelog*.yml",
        required = true)
    String[] inputPatterns;

    @Parameter(property = "sqlgen.outputDirectory",
        defaultValue = "${project.build.directory}/generated-resources/liquibase",
        required = true)
    File outputDirectory;

    @Parameter(property = "sqlgen.outputPrefix")
    String outputPrefix;

    @Parameter(property = "sqlgen.encoding", defaultValue = "UTF-8")
    private String encoding;

    @Parameter(property = "sqlgen.serviceName",
        defaultValue = "${project.artifactId}",
        required = true)
    String serviceName;

    @Parameter(property = "sqlgen.databases", defaultValue = "mysql", required = true)
    List<String> databases = new ArrayList<>();

    @Parameter(property = "sqlgen.formats", defaultValue = "zip")
    List<String> formats = asList("zip");

    @Parameter(property = "sqlgen.attach")
    boolean attach = true;

    @Parameter(property = "sqlgen.classifier", defaultValue = "sql")
    String classifier;

    @Parameter(property = "sqlgen.addResource")
    boolean addResource = false;

    @Parameter(property = "sqlgen.addTestResource")
    boolean addTestResource = false;

    @Parameter(property = "sqlgen.namingStrategy", defaultValue = "AUTO")
    ScriptNamingStrategy namingStrategy;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            final LiquibaseUpdate update = evaluateChanges();

            if (update == null) {
                return;
            }

            generateSQL(update);
        } catch (final MojoExecutionException e) {
            getLog().error(e);

            throw e;
        }

        this.buildContext.refresh(this.outputDirectory);

        for (final String format : this.formats) {
            try {
                createAssembly(format);
            } catch (final NoSuchArchiverException | ArchiverException | IOException e) {
                getLog().error(e);

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
            .fileSet(this.outputDirectory)
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

    private LiquibaseUpdate evaluateChanges() throws MojoExecutionException {
        if (this.skip) {
            getLog().info("SQL generation is skipped.");

            return null;
        }
        if (this.databases.isEmpty()) {
            getLog().info("SQL generation skipped, no database specified.");

            return null;
        }

        final LiquibaseUpdateBuilder builder = LiquibaseUpdate.builder()
            .changeLogFile(this.changeLogFile)
            .strategy(this.namingStrategy)
            .writerProvider(this::createWriter);

        final String[] inputs;

        if (this.inputDirectory.exists()) {
            final Scanner scanner = this.buildContext.newScanner(this.inputDirectory);
            scanner.setIncludes(
                Stream.concat(
                    Stream.of(this.changeLogFile), stream(this.inputPatterns))
                    .toArray(String[]::new));
            scanner.scan();
            inputs = scanner.getIncludedFiles();
        } else {
            inputs = null;
        }

        if (inputs != null && inputs.length > 0) {
            builder.accessor(new FileSystemResourceAccessor(this.inputDirectory.getPath()));
        } else {
            builder.classLoader(classLoader());
        }

        final LiquibaseUpdate update = builder.strategy(this.namingStrategy).build();

        if (update.scriptNames().isEmpty()) {
            getLog().info("SQL generation skipped, no change found");

            return null;
        }

        if (this.addResource) {
            this.project.addResource(createResource());
        } else if (this.addTestResource) {
            this.project.addTestResource(createResource());
        }

        return update;
    }

    private void generateSQL(LiquibaseUpdate changes) throws MojoExecutionException {
        for (final String database : this.databases) {
            final LiquibaseUpdate create = changes.newBuilder().database(database).build();
            final File marker = new File(this.outputDirectory, "." + create.digest());
            final File createCSV = changeLogCSV(database, "create");

            if (this.buildContext.isUptodate(createCSV, marker)) {
                continue;
            }

            create.newBuilder()
                .changeLogCache(createCSV.toPath())
                .output(constructOUT(database, "create"))
                .build()
                .renameCache()
                .generateSQL();

            final File updateCSV = changeLogCSV(database, "update");

            final LiquibaseUpdate update = changes.newBuilder()
                .changeLogCache(updateCSV.toPath())
                .build()
                .renameCache();

            for (final String name : create.scriptNames()) {
                update.newBuilder()
                    .select(name)
                    .output(constructOUT(database, name))
                    .build()
                    .generateSQL();
            }

            try {
                Files.write(marker.toPath(), new byte[0]);
            } catch (final IOException e) {
                throw new MojoExecutionException(database, e);
            }
        }
    }

    private ClassLoader classLoader() throws MojoExecutionException {
        try {
            final URL[] urls = this.project.getRuntimeClasspathElements().stream()
                .map(Paths::get)
                .map(Path::toUri)
                .map(this::toURL)
                .toArray(URL[]::new);

            return new URLClassLoader(urls, null);
        } catch (final DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("Cannot get runtime classpath", e);
        }

    }

    private Resource createResource() {
        final Resource resource = new Resource();

        resource.setDirectory(this.outputDirectory.getPath());
        resource.setIncludes(asList(SQL_FILES));

        return resource;
    }

    @SneakyThrows
    private URL toURL(URI uri) {
        return uri.toURL();
    }

    @SneakyThrows
    private FileTime fileTime(Path path) {
        return Files.getLastModifiedTime(path);
    }

    private Path constructOUT(String database, String context) {
        return Paths.get(this.outputDirectory.getPath(),
            Stream.of(this.outputPrefix, database, context, this.serviceName + ".sql")
                .filter(s -> s != null)
                .map(String::trim)
                .filter(s -> s.length() > 0)
                .map(s -> s.replace('/', File.separatorChar))
                .toArray(String[]::new));
    }

    private Writer createWriter(Path path) throws IOException {
        getLog().info(format("Creating %s", this.project.getBasedir().toPath().relativize(path)));

        return new OutputStreamWriter(this.buildContext.newFileOutputStream(path.toFile()), this.encoding);
    }

    private File changeLogCSV(String database, String kind) {
        return new File(this.outputDirectory.getPath(), format("%s-%s.csv", database, kind));
    }
}
