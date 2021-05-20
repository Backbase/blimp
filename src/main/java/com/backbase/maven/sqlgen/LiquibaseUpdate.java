package com.backbase.maven.sqlgen;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.Optional.ofNullable;

import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.function.Consumer;
import liquibase.ContextExpression;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.ServiceLocator;
import lombok.Builder;
import org.apache.maven.plugin.MojoExecutionException;

@Builder
final class LiquibaseUpdate {
    interface Action<T> {
        T call(ResourceAccessor accessor) throws MojoExecutionException;
    }

    private final String database;
    private final String changeLogFile;
    private final Path changeLogCache;

    @Builder.Default
    private final ResourceAccessor baseAccessor = new FileSystemResourceAccessor();
    @Builder.Default
    private final Consumer<String> log = System.out::println;

    private final Path output;
    private final String context;
    private final ClassLoader classLoader;

    LiquibaseUpdateBuilder newBuilder() {
        return builder()
            .baseAccessor(this.baseAccessor)
            .changeLogFile(this.changeLogFile)
            .changeLogCache(this.changeLogCache)
            .classLoader(this.classLoader)
            .log(this.log);
    }

    Collection<String> contexts() throws MojoExecutionException {
        Objects.requireNonNull(this.changeLogFile, "The member 'changeLogFile' is required");

        return withClassLoader(accessor -> {
            final DatabaseConnection conn = new OfflineConnection("offline:h2", accessor);

            try (final Liquibase liquibase = new Liquibase(this.changeLogFile, accessor, conn)) {
                return collectContexts(new LinkedHashSet<>(), liquibase.getDatabaseChangeLog());
            } catch (final Exception e) {
                throw new MojoExecutionException("contexts", e);
            }
        });
    }

    void generateSQL() throws MojoExecutionException {
        Objects.requireNonNull(this.changeLogFile, "The member 'changeLogFile' is required");
        Objects.requireNonNull(this.database, "The member 'database' is required");
        Objects.requireNonNull(this.output, "The member 'output' is required");
        Objects.requireNonNull(this.changeLogCache, "The member 'changeLogCache' is required");

        withClassLoader(accessor -> {
            this.log.accept("Generating " + this.output);

            final String url = format("offline:%s?changeLogFile=%s", this.database, this.changeLogCache);
            final DatabaseConnection conn = new OfflineConnection(url, accessor);

            try (final Liquibase liquibase = new Liquibase(this.changeLogFile, accessor, conn)) {
                Files.createDirectories(this.output.getParent());

                try (Writer out = new FileWriter(this.output.toFile())) {
                    final Contexts contexts = this.context != null ? new Contexts(this.context) : new Contexts();

                    ServiceLocator.getInstance().setResourceAccessor(accessor);
                    liquibase.update(contexts, new LabelExpression(), out);
                }
            } catch (final Exception e) {
                throw new MojoExecutionException(this.output.toString(), e);
            }

            return null;
        });
    }

    private <T> T withClassLoader(Action<T> action) throws MojoExecutionException {
        if (this.classLoader == null) {
            return action.call(this.baseAccessor);
        }

        final ClassLoader old = currentThread().getContextClassLoader();

        currentThread().setContextClassLoader(this.classLoader);

        try {
            return action.call(
                new CompositeResourceAccessor(this.baseAccessor,
                    new ClassLoaderResourceAccessor(this.classLoader)));
        } finally {
            currentThread().setContextClassLoader(old);
        }

    }

    private Collection<String> collectContexts(Collection<String> contexts, DatabaseChangeLog changeLog) {
        ofNullable(changeLog.getIncludeContexts())
            .map(ContextExpression::getContexts)
            .ifPresent(contexts::addAll);

        for (final ChangeSet cs : changeLog.getChangeSets()) {
            final DatabaseChangeLog cl = cs.getChangeLog();

            if (cl == changeLog) {
                continue;
            }

            collectContexts(contexts, cl);
        }

        return contexts;
    }
}
