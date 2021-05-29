package com.backbase.oss.blimp;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.io.BufferedReader;
import java.io.StringReader;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.sql.CallableSql;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.SqlStatement;
import liquibase.structure.DatabaseObject;
import org.hibernate.engine.jdbc.internal.BasicFormatterImpl;
import org.hibernate.engine.jdbc.internal.DDLFormatterImpl;

@SuppressWarnings({
    "rawtypes",
    "unused",
})
public class HibernateSqlGenerator implements SqlGenerator {
    private static final boolean ENABLED = checkHibernateFormatters();

    private static boolean checkHibernateFormatters() {
        try {
            return DDLFormatterImpl.INSTANCE != null;
        } catch (final NoClassDefFoundError e) {
            return false;
        }
    }

    private static final String DDL_LINE = "    ";
    private static final String DDL_HEAD = System.lineSeparator() + DDL_LINE;

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT + 15;
    }

    @Override
    public boolean supports(SqlStatement statement, Database database) {
        return ENABLED;
    }

    @Override
    public boolean generateStatementsIsVolatile(Database database) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsIsVolatile(Database database) {
        return false;
    }

    @Override
    public ValidationErrors validate(SqlStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return sqlGeneratorChain.validate(statement, database);
    }

    @Override
    public Warnings warn(SqlStatement statementType, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return sqlGeneratorChain.warn(statementType, database);
    }

    @Override
    public Sql[] generateSql(SqlStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return stream(sqlGeneratorChain.generateSql(statement, database))
            .map(this::format)
            .toArray(Sql[]::new);
    }

    private Sql format(Sql sql) {
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
                .map(line -> unindent(line, DDL_LINE))
                .collect(joining("\n"));
        }

        return new BasicFormatterImpl().format(statement);
    }


    private String unindent(String text, String prefix) {
        return text.startsWith(prefix) ? text.substring(prefix.length()) : text;
    }
}

