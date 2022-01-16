package com.backbase.oss.blimp.format;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.backbase.oss.blimp.core.AbstractBlimpConfiguration;
import com.backbase.oss.blimp.core.NormalizedResourceAccessor;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.ServiceLocator;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Slf4j
class BlimpFormatterTest {

    private final ResourceAccessor accessor = new NormalizedResourceAccessor();
    private final StringWriter output = new StringWriter();
    private Database database;

    @SneakyThrows
    static String loadResource(String resource) {
        if (!resource.startsWith("/")) {
            resource = "/" + resource;
        }

        try (InputStream is = BlimpFormatterTest.class.getResourceAsStream(resource)) {
            assertThat(is).as(resource).isNotNull();

            return IOUtils.toString(is);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        final Path changeLogFile = Files.createTempFile("changeLogFile-", ".csv");

        changeLogFile.toFile().deleteOnExit();

        LiquibaseConfiguration.getInstance().reset();
        ServiceLocator.getInstance()
            .addPackageToScan(getClass().getPackage().getName());

        this.database = DatabaseFactory.getInstance().openDatabase(
            "offline:mysql?outputLiquibaseSql=none&changeLogFile=" + changeLogFile,
            "", "", "", "", "", "", this.accessor);

        SqlGeneratorFactory.reset();
    }

    @Test
    void formatedSQL() throws Exception {
        LiquibaseConfiguration.getInstance().getConfiguration(FormatterConfiguration.class)
            .setValue(AbstractBlimpConfiguration.ENABLED, true);

        try (final Liquibase liquibase = new Liquibase("product-db/changelog/db.changelog-persistence.xml",
            this.accessor, this.database)) {

            liquibase.update("", new Contexts(""), new LabelExpression(""), this.output);
        }

        LOG.info("output is\n{}", this.output);

        assertThat(this.output.toString()).contains(loadResource("product-db/formatted.sql"));
    }

    @Test
    void unformatedSQL() throws Exception {
        LiquibaseConfiguration.getInstance().getConfiguration(FormatterConfiguration.class)
            .setValue(AbstractBlimpConfiguration.ENABLED, false);

        try (final Liquibase liquibase = new Liquibase("product-db/changelog/db.changelog-persistence.xml",
            this.accessor, this.database)) {

            liquibase.update("", new Contexts(""), new LabelExpression(""), this.output);
        }

        LOG.info("output is\n{}", this.output);

        assertThat(this.output.toString()).contains(loadResource("product-db/unformatted.sql"));
    }

    @ParameterizedTest
    @CsvSource({
        "with-details-1,true",
        "with-details-1,false",
        "with-details-2,true",
        "with-details-2,false",
    })
    void mainWithDetails(String label, boolean formatted) throws Exception {
        LiquibaseConfiguration.getInstance().getConfiguration(FormatterConfiguration.class)
            .setValue(AbstractBlimpConfiguration.ENABLED, formatted);

        try (final Liquibase liquibase = new Liquibase("main-with-details/db.changelog-main.xml",
            this.accessor, this.database)) {

            liquibase.update("", new Contexts(""), new LabelExpression(label), this.output);
        }

        LOG.info("output is\n{}", this.output);

        final String fmt = formatted ? "formatted" : "unformatted";
        final String sql = this.output.toString();

        assertAll(
            () -> assertThat(sql).as("foreign key")
                .contains(loadResource(format("main-with-details/%s-%s.sql", label, fmt))),
            () -> assertThat(sql).as("insert")
                .contains(loadResource(format("main-with-details/insert-%s.sql", fmt))));
    }

}
