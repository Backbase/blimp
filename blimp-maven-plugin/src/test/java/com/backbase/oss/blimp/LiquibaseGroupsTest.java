package com.backbase.oss.blimp;

import static org.assertj.core.api.Assertions.assertThat;

import liquibase.resource.FileSystemResourceAccessor;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;

class LiquibaseGroupsTest {

    @Test
    void product() throws MojoExecutionException {
        final LiquibaseUpdate liquibase = LiquibaseUpdate.builder()
            .accessor(new FileSystemResourceAccessor("src/test/resources/product-db"))
            .changeLogFile("changelog/db.changelog-persistence.xml")
            .build();

        assertThat(liquibase.groups()).hasSize(3);
        assertThat(liquibase.newBuilder().build().groups()).hasSize(3);
        assertThat(liquibase.digest()).isNotEmpty();
    }

    @Test
    void review() throws MojoExecutionException {
        final LiquibaseUpdate liquibase = LiquibaseUpdate.builder()
            .accessor(new FileSystemResourceAccessor("src/test/resources/review-db"))
            .changeLogFile("changelog/db.changelog-persistence.xml")
            .build();

        assertThat(liquibase.groups()).hasSize(3);
        assertThat(liquibase.newBuilder().build().groups()).hasSize(3);
        assertThat(liquibase.digest()).isNotEmpty();
    }

}

