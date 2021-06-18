package com.backbase.oss.blimp;

import com.backbase.oss.blimp.core.NormalizedResourceAccessor;
import com.backbase.oss.blimp.format.BlimpFormatter;
import com.backbase.oss.blimp.liquibase.LiquibaseEngine;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import liquibase.exception.LiquibaseException;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class LiquibaseGenerateTest {

    @Test
    void generate() throws LiquibaseException {
        final Path output = Paths.get("target", "test-output", "generate");
        final Path cache = output.resolve("cache.csv");

        SqlGeneratorFactory.getInstance().register(new BlimpFormatter());

        final LiquibaseEngine engine = LiquibaseEngine.builder()
            .stripComments(false)
            .accessor(new NormalizedResourceAccessor())
            .changeLogFile("review-db/changelog/db.changelog-persistence.xml")
            .changeLogCache(cache)
            .database("mysql")
            .writerProvider(path -> {
                LOG.info("Path: {}", path);

                return new PrintWriter(System.out);
            })
            .output(output)
            .build();

        engine.discardCache().generateSQL();
    }

    @Test
    void stripComments() throws LiquibaseException {
        final Path output = Paths.get("target", "test-output", "generate");
        final Path cache = output.resolve("cache.csv");

        SqlGeneratorFactory.getInstance().register(new BlimpFormatter());

        final LiquibaseEngine engine = LiquibaseEngine.builder()
            .stripComments(true)
            .accessor(new NormalizedResourceAccessor())
            .changeLogFile("review-db/changelog/db.changelog-persistence.xml")
            .changeLogCache(cache)
            .database("mysql")
            .writerProvider(path -> {
                LOG.info("Path: {}", path);

                return new PrintWriter(System.out);
            })
            .output(output)
            .build();

        engine.discardCache().generateSQL();
    }
}

