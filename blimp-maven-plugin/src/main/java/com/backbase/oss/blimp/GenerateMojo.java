package com.backbase.oss.blimp;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

import com.backbase.oss.blimp.LiquibaseUpdate.LiquibaseUpdateBuilder;
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
import java.util.List;
import java.util.stream.Stream;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.configuration.SystemPropertyProvider;
import liquibase.resource.FileSystemResourceAccessor;
import lombok.SneakyThrows;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.Scanner;

/**
 * Generate all SQL scripts for all specified databases.
 * <p>
 * This mojo executes the following actions
 * <ol>
 * <li>generates the full creation script</li>
 * <li>collects all groups of changesets</li>
 * <li>for each group, generates one script containing all changes in that group</li>
 * </ol>
 * <br/>
 * <b>What is a group?</b><br/>
 * A group is a collection of changesets that are supposed to included in a release they can be
 * either the labels of the changes or the contexts.
 * </p>
 */
@Mojo(name = "generate", requiresProject = true, defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GenerateMojo extends MojoBase {

    /**
     * The location of the <i>changelog</i> to execute.
     * <p>
     * Usually a file name relative to the input directory but it can also point to a classpath
     * resource.
     * </p>
     */
    @Parameter(property = "blimp.changeLogFile", defaultValue = "db.changelog-persistence.xml", required = true)
    private String changeLogFile;

    /**
     * The base directory of the <i>changelog</i> files.
     */
    @Parameter(property = "blimp.inputDirectory",
        defaultValue = "${project.basedir}/src/main/resources",
        required = true)
    private File inputDirectory;

    /**
     * List of glob patterns specifing the changelog files.
     * <p>
     * Not needed by Liquibase, but used by the plugin to avoid unnecessary executions of the goal.
     * </p>
     */
    @Parameter(property = "blimp.inputPatterns",
        defaultValue = "**/*.sql,**/db.changelog*.xml,**/db.changelog*.yml",
        required = true)
    private String[] inputPatterns;

    /**
     * The destination directory of the generated SQL files.
     */
    @Parameter(property = "blimp.outputDirectory",
        defaultValue = "${project.build.directory}/generated-resources/liquibase",
        required = true)
    private File outputDirectory;

    /**
     * Specifies how to generate the name of SQL script.
     * <p>
     * The following placeholders are available:
     * <ul>
     * <li>database: the database type</li>
     * <li>group: the name of the group for which the goal generates the SQL script.</li>
     * <li>service: the service name taken from the {@link MojoBase#serviceName}.</li>
     * </p>
     * For full creation SQL scripts, the group name is set as {@code create}.
     */
    @Parameter(property = "blimp.sqlFileNameFormat", defaultValue = "@{database}/@{group}/@{service}.sql")
    private String sqlFileNameFormat;

    /**
     * The file encoding used for SQL files.
     */
    @Parameter(property = "blimp.encoding", defaultValue = "UTF-8")
    private String encoding;

    /**
     * The list of the databases for which to generate the SQL scripts.
     */
    @Parameter(property = "blimp.databases", defaultValue = "mysql", required = true)
    private List<String> databases;

    /**
     * Whether to add the SQL scripts as a resource of the project.
     */
    @Parameter(property = "blimp.addResource", defaultValue = "false")
    private boolean addResource;

    /**
     * Whether to add the SQL scripts as a resource of the project.
     */
    @Parameter(property = "blimp.addTestResource", defaultValue = "false")
    private boolean addTestResource;

    /**
     * Specifies a map of properties you want to pass to Liquibase.
     */
    @Parameter(property = "blimp.properties")
    private MavenPropertiesProvider properties;

    /**
     * Controls how to group the changesets to generate one SQL script for a given context or label.
     * <p>
     * The following options are available
     * <ul>
     * <li><b>CONTEXTS</b>: use the changeset context to group changes.
     * <li><b>LABELS</b>: use the changeset label to group changes.
     * <li><b>AUTO</b>: tries to identify if the changes use contexts or labels; if both are present,
     * then contexts is preferred.
     * </ul>
     * Note that when a context or label contains multiple values, only the first one is considered.
     * </p>
     */
    @Parameter(property = "blimp.groupingStrategy", defaultValue = "AUTO")
    private ScriptGroupingStrategy groupingStrategy;

    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        final LiquibaseUpdate update = evaluateChanges();

        if (update == null) {
            return;
        }

        processSystemProperties();
        generateSQL(update);
    }

    private LiquibaseUpdate evaluateChanges() throws MojoExecutionException {
        if (this.databases.isEmpty()) {
            getLog().info("SQL generation skipped, no database specified.");

            return null;
        }

        final LiquibaseUpdateBuilder builder = LiquibaseUpdate.builder()
            .changeLogFile(this.changeLogFile)
            .strategy(this.groupingStrategy)
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

        final LiquibaseUpdate update = builder.strategy(this.groupingStrategy).build();

        if (update.groups().isEmpty()) {
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

    private void processSystemProperties() {
        if (this.properties != null) {
            LiquibaseConfiguration.getInstance().init(new SystemPropertyProvider(), this.properties);
        }
    }

    private void generateSQL(LiquibaseUpdate changes) throws MojoExecutionException {
        for (final String database : this.databases) {
            final LiquibaseUpdate create = changes.newBuilder().database(database).build();
            final File marker = new File(this.outputDirectory, database + "." + create.digest());
            final File createCSV = changeLogCSV(database, "create");

            if (this.buildContext.isUptodate(createCSV, marker)) {
                continue;
            }

            try {
                Files.createDirectories(marker.toPath().getParent());
                Files.write(marker.toPath(), new byte[0]);
            } catch (final IOException e) {
                throw new MojoExecutionException(database, e);
            }

            create.newBuilder()
                .changeLogCache(createCSV.toPath())
                .output(sqlFileName(database, "create"))
                .build()
                .renameCache()
                .generateSQL();

            final File updateCSV = changeLogCSV(database, "update");

            final LiquibaseUpdate update = create.newBuilder()
                .changeLogCache(updateCSV.toPath())
                .build()
                .renameCache();

            for (final String name : create.groups()) {
                update.newBuilder()
                    .select(name)
                    .output(sqlFileName(database, name))
                    .build()
                    .generateSQL();
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

    private Path sqlFileName(String database, String context) {
        final String sqlFile = this.sqlFileNameFormat
            .replace("@{group}", context)
            .replace("@{database}", database)
            .replace("@{service}", this.serviceName)
            .replace('/', File.separatorChar);

        return Paths.get(this.outputDirectory.getPath(), sqlFile);
    }

    private Writer createWriter(Path path) throws IOException {
        getLog().info(format("Creating %s", this.project.getBasedir().toPath().relativize(path)));

        return new OutputStreamWriter(this.buildContext.newFileOutputStream(path.toFile()), this.encoding);
    }

    private File changeLogCSV(String database, String kind) {
        return new File(this.outputDirectory.getPath(), format("%s-%s.csv", database, kind));
    }
}
