package com.backbase.oss.blimp.liquibase;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.Collections.emptySet;

import com.backbase.oss.blimp.ScriptGroupingStrategy;
import com.backbase.oss.blimp.core.NormalizedResourceAccessor;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.RuntimeEnvironment;
import liquibase.changelog.ChangeLogIterator;
import liquibase.changelog.visitor.ChangeSetVisitor;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.ServiceLocator;
import lombok.Getter;

@lombok.Builder(builderClassName = "Builder")
public final class LiquibaseEngine {
    interface Action<T> {
        T call(ResourceAccessor accessor) throws LiquibaseException;
    }

    static {
        ServiceLocator.getInstance().addPackageToScan("com.backbase.oss.blimp");
    }

    @lombok.Builder.Default
    private final String database = "mysql";
    @lombok.Builder.Default
    private final String changeLogFile = "db.changelog-persistence.xml";
    private final Path changeLogCache;

    @lombok.Builder.Default
    private final ResourceAccessor accessor = new FileSystemResourceAccessor();
    private final WriterProvider writerProvider;

    @lombok.Builder.Default
    @Getter
    private final ScriptGroupingStrategy strategy = ScriptGroupingStrategy.AUTO;
    @lombok.Builder.Default
    @Getter
    private final Set<String> groups = emptySet();

    private final Path output;
    private final String select;
    private final ClassLoader classLoader;

    private final boolean stripComments;

    public LiquibaseEngine.Builder newBuilder() {
        return builder()
            .accessor(this.accessor)
            .changeLogFile(this.changeLogFile)
            .changeLogCache(this.changeLogCache)
            .classLoader(this.classLoader)
            .database(this.database)
            .groups(this.groups)
            .strategy(this.strategy)
            .writerProvider(this.writerProvider)
            .stripComments(this.stripComments);
    }

    public void generateSQL() throws LiquibaseException {
        Objects.requireNonNull(this.changeLogFile, "The attribute 'changeLogFile' is required");
        Objects.requireNonNull(this.database, "The attribute 'database' is required");
        Objects.requireNonNull(this.output, "The attribute 'output' is required");
        Objects.requireNonNull(this.changeLogCache, "The attribute 'changeLogCache' is required");
        Objects.requireNonNull(this.writerProvider, "The attribute 'writerProvider' is required");
        Objects.requireNonNull(this.strategy, "The attribute 'strategy' is required");

        withClassLoader(accessor -> {
            final String url = format("offline:%s?changeLogFile=%s", this.database, this.changeLogCache);
            final DatabaseConnection conn = new OfflineConnection(url, accessor);

            try (final Liquibase liquibase = openLiquibase(conn, accessor)) {
                Files.createDirectories(this.output.getParent());

                try (Writer out = createOutput()) {
                    ServiceLocator.getInstance().setResourceAccessor(accessor);

                    final Contexts contexts;
                    final LabelExpression labels;

                    if (this.select == null) {
                        contexts = new Contexts();
                        labels = new LabelExpression();
                    } else {
                        switch (this.strategy) {
                            case CONTEXTS:
                                contexts = new Contexts(this.select);
                                labels = new LabelExpression();

                                break;

                            case LABELS:
                                contexts = new Contexts();
                                labels = new LabelExpression(this.select);

                                break;

                            default:
                                throw new AssertionError("unreachable code");
                        }
                    }

                    liquibase.update(contexts, labels, out);
                }
            } catch (final Exception e) {
                discardCache();

                throw new LiquibaseException(this.output.toString(), e);
            }

            return null;
        });
    }

    public LiquibaseEngine discardCache() throws LiquibaseException {
        if (Files.exists(this.changeLogCache)) {
            try {
                final Path failed = this.changeLogCache.getParent()
                    .resolve("old-" + this.changeLogCache.getFileName());

                Files.deleteIfExists(failed);
                Files.move(this.changeLogCache, failed);
            } catch (final IOException e) {
                throw new LiquibaseException(this.changeLogCache.toString(), e);
            }
        }

        return this;
    }

    public <V extends ChangeSetVisitor> V visit(V visitor) throws LiquibaseException {
        return withClassLoader(accessor -> {
            final String url = format("offline:%s", this.database);
            final DatabaseConnection conn = new OfflineConnection(url, accessor);

            try (final Liquibase liquibase = openLiquibase(conn, accessor)) {
                final RuntimeEnvironment env = new RuntimeEnvironment(liquibase.getDatabase(), null, null);

                new ChangeLogIterator(liquibase.getDatabaseChangeLog())
                    .run(visitor, env);
            } catch (final Exception e) {
                e.printStackTrace();
            }

            return visitor;
        });
    }

    public String[] groups() {
        return this.groups.toArray(new String[0]);
    }

    private Writer createOutput() throws IOException {
        final Writer writer = this.writerProvider.create(this.output);

        return this.stripComments ? new StripCommentsWriter(writer) : writer;
    }

    private Liquibase openLiquibase(DatabaseConnection conn, ResourceAccessor accessor) throws LiquibaseException {
        return new Liquibase(this.changeLogFile, accessor, conn);
    }

    private <T> T withClassLoader(Action<T> action) throws LiquibaseException {
        if (this.classLoader == null) {
            return action.call(this.accessor);
        }

        final ClassLoader old = currentThread().getContextClassLoader();

        if (old == this.classLoader) {
            return action.call(this.accessor);
        }

        currentThread().setContextClassLoader(this.classLoader);

        try {
            return action.call(new NormalizedResourceAccessor(this.classLoader));
        } finally {
            currentThread().setContextClassLoader(old);
        }

    }
}
