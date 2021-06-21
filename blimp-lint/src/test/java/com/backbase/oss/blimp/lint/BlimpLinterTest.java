package com.backbase.oss.blimp.lint;

import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.Test;

class BlimpLinterTest {

    @Test
    void run() throws LiquibaseException {
        System.setProperty("db.type", "mysql");

        BlimpLinter.builder()
            .changeLogFile("product-db/changelog/db.changelog-persistence.xml")
            .build()
            .run();
    }

}
