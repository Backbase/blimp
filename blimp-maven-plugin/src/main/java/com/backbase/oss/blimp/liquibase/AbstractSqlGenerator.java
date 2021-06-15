package com.backbase.oss.blimp.liquibase;



import static java.lang.String.format;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.logging.Logger;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.SqlStatement;
import lombok.RequiredArgsConstructor;

@SuppressWarnings({
    "rawtypes",
    "unused",
})
@RequiredArgsConstructor
public abstract class AbstractSqlGenerator implements SqlGenerator {
    private final String namespace;
    private boolean disabledLogged;

    @Override
    public final boolean supports(SqlStatement statement, Database database) {
        final boolean enabled = isEnabled();

        if (!enabled) {
            if (!this.disabledLogged) {
                getLogger().debug("generator is disabled");

                this.disabledLogged = true;
            }

            return false;
        }

        if (!this.disabledLogged) {
            getLogger().debug("generator is enabled");

            this.disabledLogged = true;
        }

        final boolean supported = isSupported(statement, database);

        if (supported) {
            getLogger().debug(format("supported %s", statement.getClass()));
        } else {
            getLogger().debug(format("not supported %s", statement.getClass()));
        }

        return supported;
    }

    protected abstract boolean isEnabled();

    @Override
    public ValidationErrors validate(SqlStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return sqlGeneratorChain.validate(statement, database);
    }

    @Override
    public Warnings warn(SqlStatement statementType, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return sqlGeneratorChain.warn(statementType, database);
    }

    @Override
    public boolean generateStatementsIsVolatile(Database database) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsIsVolatile(Database database) {
        return false;
    }

    protected abstract boolean isSupported(SqlStatement statement, Database database);

    protected abstract Logger getLogger();
}

