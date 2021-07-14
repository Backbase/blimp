package com.backbase.oss.blimp.core;

import static java.lang.Thread.currentThread;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.RuntimeEnvironment;
import liquibase.changelog.ChangeLogIterator;
import liquibase.configuration.ConfigurationValueProvider;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.configuration.SystemPropertyProvider;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.ServiceLocator;
import lombok.Builder;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
public class LiquibaseEngine {
    static {
        ServiceLocator.getInstance().addPackageToScan("com.backbase.oss.blimp");
    }

    private interface Action<T> {
        T call(ResourceAccessor accessor) throws LiquibaseException;
    }

    public interface LiquibaseAction<T> {
        T call(Liquibase liquibase) throws LiquibaseException;
    }

    @lombok.Builder.Default
    protected final String changeLogFile = "db.changelog-main.xml";
    protected final String changeLogCache;

    @Builder.Default
    protected final String database = "mysql";

    @lombok.Builder.Default
    private final ResourceAccessor accessor = new FileSystemResourceAccessor();

    @Singular
    private Map<String, String> connectionParams;

    @Singular
    private List<ConfigurationValueProvider> configProviders;

    private ClassLoader classLoader;
    private Contexts contexts;
    private LabelExpression labels;

    public <T> T visit(LiquibaseVisitor<T> visitor) throws LiquibaseException {
        return run(liquibase -> {
            final RuntimeEnvironment env = new RuntimeEnvironment(liquibase.getDatabase(),
                this.contexts, this.labels);

            new ChangeLogIterator(liquibase.getDatabaseChangeLog())
                .run(visitor, env);

            return visitor.getResult();
        });
    }

    public <T> T run(LiquibaseAction<T> action) throws LiquibaseException {
        return withClassLoader(accessor -> {
            final ConfigurationValueProvider[] cvps = Stream.concat(
                Stream.of(new SystemPropertyProvider()),
                ofNullable(this.configProviders)
                    .map(Collection::stream)
                    .orElse(Stream.empty()))
                .filter(Objects::nonNull)
                .toArray(ConfigurationValueProvider[]::new);

            LiquibaseConfiguration.getInstance().init(cvps);

            final String params = Stream.concat(
                ofNullable(this.changeLogCache).map(clc -> "changeLogFile=" + clc).map(Stream::of)
                    .orElse(Stream.empty()),
                ofNullable(this.connectionParams)
                    .map(Map::entrySet)
                    .map(Collection::stream)
                    .orElse(Stream.empty())
                    .map(Object::toString))
                .collect(joining("&", "?", ""));

            final String url = "offline:" + this.database + params;
            final DatabaseConnection conn = new OfflineConnection(url, accessor);

            try (final Liquibase liquibase = openLiquibase(conn, accessor)) {
                return action.call(liquibase);
            } catch (final LiquibaseException e) {
                throw e;
            } catch (final Exception e) {
                throw new LiquibaseException(url, e);
            }
        });
    }

    private <T> T withClassLoader(Action<T> action) throws LiquibaseException {
        if (this.classLoader == null) {
            return action.call(this.accessor);
        }

        final ClassLoader old = currentThread().getContextClassLoader();

        if (old != this.classLoader) {
            currentThread().setContextClassLoader(this.classLoader);
        }

        try {
            return action.call(new NormalizedResourceAccessor(this.classLoader));
        } finally {
            currentThread().setContextClassLoader(old);
        }

    }

    private Liquibase openLiquibase(DatabaseConnection conn, ResourceAccessor accessor) throws LiquibaseException {
        return new Liquibase(this.changeLogFile, accessor, conn);
    }
}
