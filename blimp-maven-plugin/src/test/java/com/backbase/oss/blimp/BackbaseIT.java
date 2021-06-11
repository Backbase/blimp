package com.backbase.oss.blimp;

import static com.backbase.oss.blimp.TestUtils.installedArchive;
import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;
import static com.soebes.itf.jupiter.extension.MavenCLIOptions.BATCH_MODE;
import static com.soebes.itf.jupiter.extension.MavenCLIOptions.FAIL_AT_END;
import static com.soebes.itf.jupiter.extension.MavenCLIOptions.SETTINGS;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Files.contentOf;

import com.soebes.itf.extension.assertj.MavenProjectResultAssert;
import com.soebes.itf.jupiter.extension.MavenGoal;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenOption;
import com.soebes.itf.jupiter.extension.MavenRepository;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.extension.SystemProperty;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@MavenJupiterExtension
@SystemProperty(value = "changelog.location", content = "src/test/resources")
@MavenGoal({"clean", "install"})
@MavenOption(BATCH_MODE)
@MavenOption(FAIL_AT_END)
@MavenOption(SETTINGS)
@MavenOption("settings.xml")
@MavenOption("-Dproducts.version=1.4.17")
@MavenRepository
@EnabledIfSystemProperty(named = "blimp-internal-test", matches = "^true$")
class BackbaseIT {

    @MavenTest
    void liquibaseLint(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("generated-resources/blimp/mysql/create/liquibase-lint.sql")
            .satisfies(file -> {
                assertThat(contentOf(file, StandardCharsets.UTF_8))
                    .contains("\nCREATE TABLE product (\n ");
            });

        target.withFile(format("liquibase-lint-sql.zip")).exists().isNotEmpty();
        assertThat(installedArchive(result, null, "sql", "zip")).exists().isNotEmpty();
    }

    @MavenTest
    void liquibaseLintBlimpFormat(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("generated-resources/blimp/mysql/create/liquibase-lint-blimp-format.sql")
            .satisfies(file -> {
                assertThat(contentOf(file, StandardCharsets.UTF_8))
                    .contains("\nCREATE TABLE product (\n   id");
            });
    }

    @MavenTest
    void liquibaseLintFailure(MavenExecutionResult result) {
        assertThat(result).isFailure();

        final File stdout = result.getMavenLog().getStdout().toFile();

        assertThat(contentOf(stdout, Charset.defaultCharset()))
            .matches("(?s)"
                + ".+db.changelog-persistence.xml"
                + ".+failed"
                + ".+validation"
                + ".+LintingChangeLogParser.+");
    }

    @MavenTest
    void productsDirectory(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("generated-resources/blimp/mysql/create/products-directory.sql")
            .exists().isNotEmpty();
        target.withFile("generated-resources/blimp/mysql/create_2_19_0/products-directory.sql")
            .exists().isNotEmpty();
        target.withFile("generated-resources/blimp/mysql/upgrade_2_20_5_to_2_21_0/products-directory.sql")
            .exists().isNotEmpty();
    }

    @MavenTest
    void productsClasspath(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("generated-resources/blimp/mysql/create/products-classpath.sql")
            .exists().isNotEmpty();
        target.withFile("generated-resources/blimp/mysql/create_2_19_0/products-classpath.sql")
            .exists().isNotEmpty();
        target.withFile("generated-resources/blimp/mysql/upgrade_2_20_5_to_2_21_0/products-classpath.sql")
            .exists().isNotEmpty();
    }
}
