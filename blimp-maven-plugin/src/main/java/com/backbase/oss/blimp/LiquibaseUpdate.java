package com.backbase.oss.blimp;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.Optional.ofNullable;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import liquibase.ContextExpression;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Labels;
import liquibase.Liquibase;
import liquibase.change.CheckSum;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.ServiceLocator;
import lombok.Builder;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.plugin.MojoExecutionException;

@Builder
final class LiquibaseUpdate {
    interface Action<T> {
        T call(ResourceAccessor accessor) throws MojoExecutionException;
    }

    @Builder.Default
    private final String database = "mysql";
    @Builder.Default
    private final String changeLogFile = "db.changelog-persistence.xml";
    private final Path changeLogCache;

    @Builder.Default
    private final ResourceAccessor accessor = new FileSystemResourceAccessor();
    private final WriterProvider writerProvider;

    @Builder.Default
    private ScriptGroupingStrategy strategy = ScriptGroupingStrategy.AUTO;

    private final Path output;
    private final String select;
    private final ClassLoader classLoader;

    @Builder.Default
    private final Set<String> contexts = new LinkedHashSet<>();
    @Builder.Default
    private final Set<String> labels = new LinkedHashSet<>();
    private Set<String> names;
    private String digest;

    LiquibaseUpdateBuilder newBuilder() {
        return builder()
            .accessor(this.accessor)
            .changeLogFile(this.changeLogFile)
            .changeLogCache(this.changeLogCache)
            .classLoader(this.classLoader)
            .contexts(this.contexts)
            .database(this.database)
            .digest(this.digest)
            .labels(this.labels)
            .names(this.names)
            .strategy(this.strategy)
            .writerProvider(this.writerProvider);
    }

    Set<String> groups() throws MojoExecutionException {
        if (this.names != null) {
            return this.names;
        }

        Objects.requireNonNull(this.changeLogFile, "The attribute 'changeLogFile' is required");
        Objects.requireNonNull(this.strategy, "The attribute 'strategy' is required");
        Objects.requireNonNull(this.database, "The attribute 'database' is required");

        withClassLoader(accessor -> {
            final String url = format("offline:%s", this.database);
            final DatabaseConnection conn = new OfflineConnection(url, accessor);

            try (final Liquibase liquibase = openLiquibase(conn, accessor)) {
                visitChanges(liquibase.getDatabaseChangeLog());
            } catch (final Exception e) {
                throw new MojoExecutionException("Cannot collect groups", e);
            }

            return null;
        });

        switch (this.strategy) {
            case AUTO:
                if (this.contexts.isEmpty()) {
                    this.strategy = ScriptGroupingStrategy.LABELS;
                    this.names = this.labels;
                } else {
                    this.strategy = ScriptGroupingStrategy.CONTEXTS;
                    this.names = this.contexts;
                }
                break;

            case CONTEXTS:
                this.names = this.contexts;
                break;

            case LABELS:
                this.names = this.labels;
                break;

            default:
                throw new AssertionError("supposed to be unreachable");
        }

        return this.names;
    }

    String digest() throws MojoExecutionException {
        if (this.digest != null) {
            return this.digest;
        }

        Objects.requireNonNull(this.changeLogFile, "The attribute 'changeLogFile' is required");
        Objects.requireNonNull(this.database, "The attribute 'database' is required");

        final MessageDigest md = DigestUtils.getMd5Digest();

        groups().forEach(x -> update(md, x));

        withClassLoader(accessor -> {
            final String url = format("offline:%s", this.database);
            final DatabaseConnection conn = new OfflineConnection(url, accessor);

            try (final Liquibase liquibase = openLiquibase(conn, accessor)) {
                updateDigest(md, liquibase.getDatabaseChangeLog());
            } catch (final Exception e) {
                throw new MojoExecutionException("digest", e);
            }

            return this.digest;
        });

        return this.digest = Hex.encodeHexString(md.digest());
    }

    void generateSQL() throws MojoExecutionException {
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

                try (Writer out = this.writerProvider.create(this.output)) {
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
                renameCache();

                throw new MojoExecutionException(this.output.toString(), e);
            }

            return null;
        });
    }

    private Liquibase openLiquibase(final DatabaseConnection conn, ResourceAccessor accessor)
        throws LiquibaseException {
        return new Liquibase(this.changeLogFile, accessor, conn);
    }

    LiquibaseUpdate renameCache() throws MojoExecutionException {
        if (Files.exists(this.changeLogCache)) {
            try {
                final Path failed = this.changeLogCache.getParent()
                    .resolve("old-" + this.changeLogCache.getFileName());

                Files.deleteIfExists(failed);
                Files.move(this.changeLogCache, failed);
            } catch (final IOException e) {
                throw new MojoExecutionException(this.changeLogCache.toString(), e);
            }
        }

        return this;
    }

    private <T> T withClassLoader(Action<T> action) throws MojoExecutionException {
        if (this.classLoader == null) {
            return action.call(this.accessor);
        }

        final ClassLoader old = currentThread().getContextClassLoader();

        currentThread().setContextClassLoader(this.classLoader);

        try {
            return action.call(new ClassLoaderResourceAccessor(this.classLoader));
        } finally {
            currentThread().setContextClassLoader(old);
        }

    }

    private void visitChanges(DatabaseChangeLog changeLog) {
        ofNullable(changeLog.getIncludeContexts())
            .map(ContextExpression::getContexts)
            .ifPresent(this::pickFirstContext);
        ofNullable(changeLog.getIncludeLabels())
            .map(LabelExpression::getLabels)
            .ifPresent(this::pickFirstLabel);

        for (final ChangeSet cs : changeLog.getChangeSets()) {
            final DatabaseChangeLog cl = cs.getChangeLog();

            if (cl == changeLog) {
                continue;
            }

            ofNullable(cs.getContexts())
                .map(ContextExpression::getContexts)
                .ifPresent(this::pickFirstContext);
            ofNullable(cs.getLabels())
                .map(Labels::getLabels)
                .ifPresent(this::pickFirstLabel);

            visitChanges(cl);
        }
    }

    private void pickFirstContext(Collection<String> contexts) {
        if (contexts == null || contexts.isEmpty()) {
            return;
        }

        this.contexts.add(contexts.iterator().next());
    }

    private void pickFirstLabel(Collection<String> labels) {
        if (labels == null || labels.isEmpty()) {
            return;
        }

        this.labels.add(labels.iterator().next());
    }

    private MessageDigest updateDigest(MessageDigest digest, DatabaseChangeLog changeLog) {
        for (final ChangeSet cs : changeLog.getChangeSets()) {
            final DatabaseChangeLog cl = cs.getChangeLog();

            if (cl == changeLog) {
                continue;
            }

            final Set<CheckSum> checksums = cs.getValidCheckSums();

            if (checksums.isEmpty()) {
                update(digest, cs.generateCheckSum().toString());
            } else {
                update(digest, cs.generateCheckSum().toString());
            }
        }

        return digest;
    }

    private void update(MessageDigest digest, String text) {
        digest.update(text.toString().getBytes(StandardCharsets.UTF_8));
    }
}
