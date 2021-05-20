package com.backbase.maven.sqlgen;


import static java.lang.String.format;
import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;
import liquibase.resource.FileSystemResourceAccessor;
import lombok.SneakyThrows;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(name = "generate", requiresProject = true, defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class SqlGenMojo extends AbstractMojo {
    @Parameter(property = "project", readonly = true)
    private MavenProject project;

    @Parameter(property = "sqlgen.skip")
    boolean skip;

    @Parameter(property = "sqlgen.changeLogFile", defaultValue = "db.changelog-persistence.xml", required = true)
    String changeLogFile;

    @Parameter(property = "sqlgen.inputDirectory",
        defaultValue = "${project.basedir}/src/main/resources",
        required = true)
    File inputDirectory;

    @Parameter(property = "sqlgen.outputDirectory",
        defaultValue = "${project.build.directory}/generated-resources/liquibase",
        required = true)
    File outputDirectory;

    @Parameter(property = "sqlgen.outputPrefix")
    String outputPrefix;

    @Parameter(property = "sqlgen.serviceName",
        defaultValue = "${project.artifactId}",
        required = true)
    String serviceName;

    @Parameter(property = "sqlgen.databases",
        defaultValue = "mysql",
        required = true)
    List<String> databases = new ArrayList<>();

    @Parameter(property = "sqlgen.addResource")
    boolean addResource = false;

    @Override
    public void execute() throws MojoExecutionException {
        if (isExecutable()) {
            try {
                generateSQL();
            } catch (final MojoExecutionException e) {
                getLog().error(e);

                throw e;
            }

            if (this.addResource) {
                final Resource resource = new Resource();

                resource.setDirectory(this.outputDirectory.getPath());
                resource.setIncludes(asList("**/*.sql"));

                this.project.addResource(resource);
            }
        }
    }

    private boolean isExecutable() {
        if (this.skip) {
            getLog().info("SQL generation is skipped.");

            return false;
        }

        if (!this.inputDirectory.exists()) {
            getLog().info("SQL generation is skipped, input directory doesnt exist.");

            return false;
        }

        if (this.databases.isEmpty()) {
            getLog().info("SQL generation is skipped, no database specified.");

            return false;
        }

        return true;
    }

    private void generateSQL() throws MojoExecutionException {
        final LiquibaseUpdate update = LiquibaseUpdate.builder()
            .baseAccessor(new FileSystemResourceAccessor(this.inputDirectory.getPath()))
            .changeLogFile(this.changeLogFile)
            .classLoader(classLoader())
            .build();

        final Collection<String> contexts = update.contexts();

        if (contexts.isEmpty()) {
            throw new MojoExecutionException("No context could be found in the changelog");
        }

        final FileTime youngest = findYoungest();

        for (final String database : this.databases) {
            final Path createCSV = changeLogCSV(database, "create");

            if (isOlder(createCSV, youngest)) {
                update.newBuilder()
                    .database(database)
                    .changeLogCache(createCSV)
                    .output(constructOUT(database, "create"))
                    .build()
                    .generateSQL();
            }

            final Path updateCSV = changeLogCSV(database, "update");

            if (isOlder(updateCSV, youngest)) {
                for (final String context : contexts) {
                    update.newBuilder()
                        .database(database)
                        .changeLogCache(updateCSV)
                        .output(constructOUT(database, context))
                        .context(context)
                        .build()
                        .generateSQL();
                }
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

    @SneakyThrows
    private URL toURL(URI uri) {
        return uri.toURL();
    }

    private FileTime findYoungest() throws MojoExecutionException {
        final FileTime youngest[] = {
            FileTime.fromMillis(0)
        };

        try {
            Files.walkFileTree(this.inputDirectory.toPath(), EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                Integer.MAX_VALUE,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        youngest[0] = Collections.max(asList(youngest[0], attrs.lastModifiedTime()));

                        return FileVisitResult.CONTINUE;
                    }

                });


            return youngest[0];

        } catch (final IOException e) {
            throw new MojoExecutionException(this.inputDirectory.getAbsolutePath(), e);
        }
    }

    private boolean isOlder(final Path file, final FileTime youngest) throws MojoExecutionException {
        try {
            return !Files.exists(file) || Files.getLastModifiedTime(file).compareTo(youngest) < 0;
        } catch (final IOException e) {
            throw new MojoExecutionException(file.toString(), e);
        }
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


    private Path changeLogCSV(String database, String kind) {
        return Paths.get(this.outputDirectory.getPath(), format("%s-%s.csv", database, kind));
    }
}
