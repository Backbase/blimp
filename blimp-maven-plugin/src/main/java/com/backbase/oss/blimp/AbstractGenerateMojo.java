package com.backbase.oss.blimp;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

import com.backbase.oss.blimp.core.PropertiesConfigProvider;
import com.backbase.oss.blimp.liquibase.LiquibaseEngine;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.configuration.SystemPropertyProvider;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogService;
import liquibase.resource.FileSystemResourceAccessor;
import lombok.SneakyThrows;
import org.apache.commons.io.output.NullWriter;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.Scanner;

public abstract class AbstractGenerateMojo extends MojoBase {

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
     * Set to {@code true} to remove comments from SQL scripts.
     */
    @Parameter(property = "blimp.stripComments", defaultValue = "false")
    private boolean stripComments;

    /**
     * The list of the databases for which to generate the SQL scripts.
     */
    @Parameter(property = "blimp.databases", defaultValue = "mysql", required = true)
    private List<String> databases;

    /**
     * Generates a script for the initial version when there is more than one group.
     * <p>
     * Having more than one group means a database has been already created for the initial version, so
     * only the upgrade scripts should be generated.
     * </p>
     */
    @Parameter(property = "blimp.withInitialVersion", defaultValue = "false")
    private boolean withInitialVersion;

    /**
     * Specifies a map of properties you want to pass to Liquibase.
     */
    @Parameter(property = "blimp.properties")
    private PropertiesConfigProvider properties;

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

    /**
     * The location of the test cache directory where Liquibase stores the CSV file use to generate the
     * upgrade scripts.
     */
    @Parameter(property = "blimp.testCacheDirectory",
        defaultValue = "${project.build.directory}/blimp-cache",
        required = true, readonly = true)
    private File cacheDirectory;

    @Override
    protected void doExecute() throws Exception {
        LogService.setLoggerFactory(new MavenLoggerFactory(getLog()));

        final LiquibaseEngine update = evaluateChanges();

        if (update == null) {
            return;
        }

        processSystemProperties();
        generateSQL(update);
    }

    private LiquibaseEngine evaluateChanges() throws MojoExecutionException, LiquibaseException {
        if (this.databases.isEmpty()) {
            getLog().info("SQL generation skipped, no database specified.");

            return null;
        }

        final LiquibaseEngine.Builder builder = LiquibaseEngine.builder()
            .changeLogFile(changeLogFile())
            .strategy(this.groupingStrategy)
            .stripComments(this.stripComments)
            .writerProvider(this::createWriter);

        final String[] inputs;

        if (changeLogDirectory().exists()) {
            final Scanner scanner = this.buildContext.newScanner(changeLogDirectory());
            scanner.setIncludes(
                Stream.concat(
                    Stream.of(changeLogFile()), stream(this.inputPatterns))
                    .toArray(String[]::new));
            scanner.scan();
            inputs = scanner.getIncludedFiles();
        } else {
            inputs = null;
        }

        if (inputs != null && inputs.length > 0) {
            builder.accessor(new FileSystemResourceAccessor(changeLogDirectory().getPath()));
        } else {
            builder.classLoader(classLoader());
        }

        final LiquibaseEngine engine = builder.build();
        final GroupsVisitor gv = engine.visit(new GroupsVisitor(this.groupingStrategy));
        final Set<String> groups = gv.groups();

        if (groups.isEmpty()) {
            getLog().info("SQL generation skipped, no change found");

            return null;
        }

        addOutputResource();

        return engine.newBuilder()
            .strategy(gv.strategy())
            .groups(groups)
            .build();
    }

    protected abstract void addOutputResource();

    protected abstract String changeLogFile();

    protected abstract File changeLogDirectory();

    protected abstract File scriptsDirectory();

    protected abstract List<String> classpathElements() throws DependencyResolutionRequiredException;

    protected Resource createResource() {
        final Resource resource = new Resource();

        resource.setDirectory(scriptsDirectory().getPath());
        resource.setIncludes(asList(SQL_FILES));

        return resource;
    }

    private void processSystemProperties() {
        if (this.properties != null) {
            LiquibaseConfiguration.getInstance().init(new SystemPropertyProvider(), this.properties);
        }
    }

    private void generateSQL(LiquibaseEngine engine) throws LiquibaseException {
        for (final String database : this.databases) {
            final LiquibaseEngine create = engine.newBuilder().database(database).build();
            final String digest = create.visit(new DigestVisitor()).digest();
            final File marker = cacheFile(database + "-" + digest);
            final File createCSV = cacheFile(marker.getName() + "-create.csv");

            if (this.buildContext.isUptodate(createCSV, marker)) {
                continue;
            }

            try (Writer out = createWriter(marker.toPath())) {
            } catch (final IOException e) {
                throw new LiquibaseException(database, e);
            }

            create.newBuilder()
                .changeLogCache(createCSV.toPath())
                .output(sqlFileName(database, "create"))
                .build()
                .discardCache()
                .generateSQL();

            final String[] groups = create.groups();
            final File updateCSV = cacheFile(marker.getName() + "-update.csv");

            final LiquibaseEngine update = create.newBuilder()
                .changeLogCache(updateCSV.toPath())
                .build()
                .discardCache();

            for (int g = 0; g < groups.length; g++) {
                update.newBuilder()
                    .select(groups[g])
                    .output(sqlFileName(database, groups[g]))
                    .writerProvider(this.withInitialVersion || g > 0 ? this::createWriter : p -> new NullWriter())
                    .build()
                    .generateSQL();
            }
        }
    }

    private ClassLoader classLoader() throws MojoExecutionException {
        try {
            final List<String> classpath = classpathElements();
            final URL[] urls = Stream.concat(
                Stream.of(changeLogDirectory().toURI().toURL()),
                classpath.stream()
                    .map(Paths::get)
                    .map(Path::toUri)
                    .map(MojoBase::toURL))
                .toArray(URL[]::new);

            return new URLClassLoader(urls, getClass().getClassLoader());
        } catch (final DependencyResolutionRequiredException | MalformedURLException e) {
            throw new MojoExecutionException("Cannot construct Liquibase classpath", e);
        }
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

        return scriptsDirectory().toPath().resolve(sqlFile);
    }

    private Writer createWriter(Path path) throws IOException {
        getLog().info(format("Creating %s", relativePath(path)));

        Files.createDirectories(path.getParent());

        return new OutputStreamWriter(this.buildContext.newFileOutputStream(path.toFile()), this.encoding);
    }

    private Path relativePath(Path path) {
        return this.project.getBasedir().toPath().relativize(path);
    }

    private File cacheFile(String name) {
        final Path output = relativePath(scriptsDirectory().toPath());
        final int nameCount = output.getNameCount();
        final Path relPath = nameCount < 2
            ? output
            : output.getName(nameCount - 2).resolve(output.getName(nameCount - 1));
        return this.cacheDirectory.toPath().resolve(relPath).resolve(name).toFile();
    }
}
