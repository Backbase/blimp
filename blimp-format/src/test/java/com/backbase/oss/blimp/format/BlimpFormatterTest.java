package com.backbase.oss.blimp.format;

import static org.assertj.core.api.Assertions.assertThat;

import com.backbase.oss.blimp.core.AbstractBlimpConfiguration;
import com.backbase.oss.blimp.core.NormalizedResourceAccessor;
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
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BlimpFormatterTest {

    private final ResourceAccessor accessor = new NormalizedResourceAccessor();
    private final StringWriter output = new StringWriter();

    private Database database;
    private String expected;

    @BeforeEach
    void setUp() throws Exception {
        final Path changeLogFile = Files.createTempFile("changeLogFile-", ".csv");

        changeLogFile.toFile().deleteOnExit();

        ServiceLocator.getInstance()
            .addPackageToScan(getClass().getPackage().getName());

        this.database = DatabaseFactory.getInstance().openDatabase(
            "offline:mysql?outputLiquibaseSql=none&changeLogFile=" + changeLogFile,
            "", "", "", "", "", "", this.accessor);

        this.expected = IOUtils.toString(getClass().getResourceAsStream("/generate/expected.sql"));

        SqlGeneratorFactory.reset();
    }

    @Test
    void formatSQL() throws Exception {
        LiquibaseConfiguration.getInstance().getConfiguration(FormatterConfiguration.class)
            .setValue(AbstractBlimpConfiguration.ENABLED, true);

        try (final Liquibase liquibase = new Liquibase("product-db/changelog/db.changelog-persistence.xml",
            this.accessor, this.database)) {

            liquibase.update("", new Contexts(""), new LabelExpression(""), this.output);
        }

        assertThat(this.output.toString()).contains(this.expected);
    }

    @Test
    void formatterDisabled() throws Exception {
        LiquibaseConfiguration.getInstance().getConfiguration(FormatterConfiguration.class)
            .setValue(AbstractBlimpConfiguration.ENABLED, false);

        try (final Liquibase liquibase = new Liquibase("product-db/changelog/db.changelog-persistence.xml",
            this.accessor, this.database)) {

            liquibase.update("", new Contexts(""), new LabelExpression(""), this.output);
        }

        assertThat(this.output.toString()).doesNotContain(this.expected);
    }

    @Test
    void loadData() throws Exception {
        LiquibaseConfiguration.getInstance().getConfiguration(FormatterConfiguration.class)
            .setValue(AbstractBlimpConfiguration.ENABLED, true);

        try (final Liquibase liquibase = new Liquibase("load-data/db.changelog-main.xml",
            this.accessor, this.database)) {

            liquibase.update("", new Contexts(""), new LabelExpression(""), this.output);
        }

        assertThat(this.output.toString()).doesNotContain(this.expected);
    }

}


