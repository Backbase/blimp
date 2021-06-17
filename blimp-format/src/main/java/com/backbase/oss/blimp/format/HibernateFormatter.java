package com.backbase.oss.blimp.format;


import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static liquibase.util.StringUtils.trimRight;

import com.backbase.oss.blimp.core.AbstractSqlGenerator;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.logging.LogService;
import liquibase.logging.Logger;
import liquibase.sql.CallableSql;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.statement.core.InsertSetStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import org.hibernate.engine.jdbc.internal.BasicFormatterImpl;
import org.hibernate.engine.jdbc.internal.DDLFormatterImpl;

public class HibernateFormatter extends AbstractSqlGenerator {
    private static final Logger LOG = LogService.getLog(HibernateFormatter.class);
    private static final boolean ENABLED = checkHibernateFormatters();
    private static final List<Class<? extends SqlStatement>> UNSUPPORTED = asList(
        InsertStatement.class,
        InsertSetStatement.class,
        RawSqlStatement.class);

    private static boolean checkHibernateFormatters() {
        try {
            return DDLFormatterImpl.INSTANCE != null;
        } catch (final NoClassDefFoundError e) {
            LOG.debug("Cannot load Hibernate formatter");

            return false;
        }
    }

    private static final String DDL_LINE = "    ";
    private static final int DDL_LINE_Z = DDL_LINE.length();
    private static final String DDL_HEAD = System.lineSeparator() + DDL_LINE;

    private final Map<String, Sql> visited = new LinkedHashMap<>();

    public HibernateFormatter() {
        super(FormatterConfiguration.class);
    }

    @Override
    public int getPriority() {
        return LiquibaseConfiguration.getInstance()
            .getConfiguration(FormatterConfiguration.class)
            .getValue(FormatterConfiguration.PRIORITY, Integer.class);
    }

    @Override
    protected boolean isEnabled() {
        return ENABLED && super.isEnabled();
    }

    @Override
    public Sql[] generateSql(SqlStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return stream(sqlGeneratorChain.generateSql(statement, database))
            .map(this::cached)
            .toArray(Sql[]::new);
    }

    @Override
    protected boolean isSupported(SqlStatement statement, @SuppressWarnings("unused") Database database) {
        return UNSUPPORTED.stream().noneMatch(t -> t.isInstance(statement));
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    private Sql cached(Sql sql) {
        // we need to cache formatted SQL because even though SSDK formatter is disabled, it still breaks
        // the formatters chain calling other generators twice
        return this.visited.computeIfAbsent(key(sql), k -> formatSQL(sql));
    }

    private String key(Sql sql) {
        return sql.toSql().replaceAll("\\s+", "");
    }

    private Sql formatSQL(Sql sql) {
        LOG.debug(format("Formatting SQL: %s", sql));

        final String formatted = apply(sql.toSql());

        if (sql instanceof CallableSql) {
            sql = new CallableSql(formatted, ((CallableSql) sql).getExpectedStatus());
        }
        if (sql instanceof UnparsedSql) {
            sql = new UnparsedSql(formatted, sql.getEndDelimiter(),
                sql.getAffectedDatabaseObjects().toArray(new DatabaseObject[0]));
        }

        return sql;
    }

    private String apply(String statement) {
        final String ddl = DDLFormatterImpl.INSTANCE.format(statement);

        if (ddl.startsWith(DDL_HEAD)) {
            return new BufferedReader(new StringReader(ddl))
                .lines()
                .map(this::unindent)
                .collect(joining("\n"));
        }

        return new BasicFormatterImpl().format(statement);
    }

    private String unindent(String text) {
        return text.startsWith(DDL_LINE) ? trimRight(text.substring(DDL_LINE_Z)) : trimRight(text);
    }
}

