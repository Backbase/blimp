package com.backbase.oss.blimp;

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

        SqlGeneratorFactory.getInstance().register(new HibernateSqlGenerator());

        final LiquibaseUpdate liquibase = LiquibaseUpdate.builder()
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

