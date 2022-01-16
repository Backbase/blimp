package com.backbase.oss.blimp.format;

import java.util.Collection;
import liquibase.sql.Sql;
import liquibase.structure.DatabaseObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class FormattedSql implements Sql {

    private final String sql;

    @Getter
    private final String endDelimiter;
    @Getter
    private final Collection<? extends DatabaseObject> affectedDatabaseObjects;

    @Override
    public String toSql() {
        return this.sql;
    }
}
