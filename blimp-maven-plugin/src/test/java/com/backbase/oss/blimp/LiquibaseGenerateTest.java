package com.backbase.oss.blimp;

import com.backbase.oss.blimp.liquibase.HibernateFormatter;
import com.backbase.oss.blimp.liquibase.LiquibaseUpdate;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;

@Slf4j
class LiquibaseGenerateTest {

    @Test
    void generate() throws MojoExecutionException {
        final Path output = Paths.get("target", "test-output", "generate");
        final Path cache = output.resolve("cache.csv");

        SqlGeneratorFactory.getInstance().register(new HibernateFormatter());

        final LiquibaseUpdate liquibase = LiquibaseUpdate.builder()
            .stripComments(false)
            .accessor(new FileSystemResourceAccessor("src/test/resources/review-db"))
            .changeLogFile("changelog/db.changelog-persistence.xml")
            .changeLogCache(cache)
            .database("mysql")
            .writerProvider(path -> {
                LOG.info("Path: {}", path);

                return new PrintWriter(System.out);
            })
            .output(output)
            .build();

        liquibase.renameCache().generateSQL();
    }

    @Test
    void stripComments() throws MojoExecutionException {
        final Path output = Paths.get("target", "test-output", "generate");
        final Path cache = output.resolve("cache.csv");

        SqlGeneratorFactory.getInstance().register(new HibernateFormatter());

        final LiquibaseUpdate liquibase = LiquibaseUpdate.builder()
            .stripComments(true)
            .accessor(new FileSystemResourceAccessor("src/test/resources/review-db"))
            .changeLogFile("changelog/db.changelog-persistence.xml")
            .changeLogCache(cache)
            .database("mysql")
            .writerProvider(path -> {
                LOG.info("Path: {}", path);

                return new PrintWriter(System.out);
            })
            .output(output)
            .build();

        liquibase.renameCache().generateSQL();
    }
}

