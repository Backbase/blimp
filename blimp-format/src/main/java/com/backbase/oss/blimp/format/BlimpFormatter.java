package com.backbase.oss.blimp.format;


import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

import com.backbase.oss.blimp.core.AbstractSqlGenerator;
import java.util.List;
import liquibase.database.Database;
import liquibase.logging.LogService;
import liquibase.logging.Logger;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertSetStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.statement.core.RawSqlStatement;

/**
 * The formatter implemented as an SQL generator.
 */
public class BlimpFormatter extends AbstractSqlGenerator {
    private static final Logger LOG = LogService.getLog(BlimpFormatter.class);
    private static final List<Class<? extends SqlStatement>> UNSUPPORTED = asList(
        InsertStatement.class,
        InsertSetStatement.class,
        RawSqlStatement.class);

    public BlimpFormatter() {
        super(FormatterConfiguration.class);
    }

    @Override
    public Sql[] generateSql(SqlStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return stream(sqlGeneratorChain.generateSql(statement, database))
            .map(this::formatSQL)
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

    private Sql formatSQL(Sql sql) {
        if (sql instanceof FormattedSql) {
            return sql;
        }

        LOG.debug(format("Formatting SQL: %s", sql));

        final String formatted = apply(sql.toSql());

        return new FormattedSql(formatted, sql.getEndDelimiter(), sql.getAffectedDatabaseObjects());
    }

    private String apply(String statement) {
        return DDLFormatter.INSTANCE.format(statement.trim()).trim();
    }
}

