package com.backbase.oss.blimp.lint;

import com.backbase.oss.blimp.core.LiquibaseEngine;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BlimpLinterTest {

    @ParameterizedTest
    @ValueSource(strings = {"mssql", "mysql", "oracle"})
    void run(String db) throws LiquibaseException {
        final LiquibaseEngine engine = LiquibaseEngine.builder()
            .changeLogFile("product-db/changelog/db.changelog-persistence.xml")
            .classLoader(getClass().getClassLoader())
            .database(db)
            .build();

        engine.visit(new BlimpLinter());
    }

}
