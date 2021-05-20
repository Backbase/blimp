package com.backbase.maven.sqlgen;

import static org.assertj.core.api.Assertions.assertThat;

import liquibase.resource.FileSystemResourceAccessor;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;

class LiquibaseContextsTest {

    @Test
    void arrangementManager() throws MojoExecutionException {
        final LiquibaseUpdate liquibase = LiquibaseUpdate.builder()
            .baseAccessor(new FileSystemResourceAccessor("src/test/resources/arrangement-manager"))
            .build();

        assertThat(liquibase.contexts())
            .hasSize(7);
    }

    @Test
    void product() throws MojoExecutionException {
        final LiquibaseUpdate liquibase = LiquibaseUpdate.builder()
            .baseAccessor(new FileSystemResourceAccessor("src/test/resources/product-db"))
            .changeLogFile("changelog/db.changelog-persistence.xml")
            .build();

        assertThat(liquibase.contexts())
            .hasSize(3);
    }

    @Test
    void review() throws MojoExecutionException {
        final LiquibaseUpdate liquibase = LiquibaseUpdate.builder()
            .baseAccessor(new FileSystemResourceAccessor("src/test/resources/review-db"))
            .changeLogFile("changelog/db.changelog-persistence.xml")
            .build();

        assertThat(liquibase.contexts())
            .hasSize(3);
    }

}


