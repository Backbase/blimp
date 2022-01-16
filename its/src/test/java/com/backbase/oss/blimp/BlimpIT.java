package com.backbase.oss.blimp;

import static com.backbase.oss.blimp.TestUtils.installedArchive;
import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;
import static com.soebes.itf.jupiter.extension.MavenCLIOptions.BATCH_MODE;
import static com.soebes.itf.jupiter.extension.MavenCLIOptions.FAIL_AT_END;
import static com.soebes.itf.jupiter.extension.MavenCLIOptions.SETTINGS;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import com.soebes.itf.extension.assertj.MavenProjectResultAssert;
import com.soebes.itf.jupiter.extension.MavenGoal;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenOption;
import com.soebes.itf.jupiter.extension.MavenRepository;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.extension.SystemProperty;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;
import java.nio.charset.StandardCharsets;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Disabled;

@MavenJupiterExtension
@SystemProperty(value = "changelog.location", content = "target/dependency")
@MavenGoal({"clean", "install"})
@MavenOption(BATCH_MODE)
@MavenOption(FAIL_AT_END)
@MavenOption(SETTINGS)
@MavenOption("settings.xml")
@MavenRepository
class BlimpIT {

    @MavenTest
    void defaults(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("blimp-cache/generated-resources/blimp/mysql-acfc64159e9f681fc2e4ff6a4f2918a3")
            .exists().isEmpty();
        target.withFile("blimp-cache/generated-resources/blimp/mysql-acfc64159e9f681fc2e4ff6a4f2918a3-create.csv")
            .exists().isNotEmpty();
        target.withFile("blimp-cache/generated-resources/blimp/mysql-acfc64159e9f681fc2e4ff6a4f2918a3-update.csv")
            .exists().isNotEmpty();

        target.withFile("generated-resources/blimp/mysql/create/default-values.sql")
            .exists().isNotEmpty();
        target.withFile("generated-resources/blimp/mysql/initial_2021.07/default-values.sql")
            .doesNotExist();
        target.withFile("generated-resources/blimp/mysql/upgrade_2021.07_to_2021.08/default-values.sql")
            .exists().isNotEmpty();

        target.withFile("blimp-cache/generated-test-resources/blimp/mysql-535d9b3ce7d6e7948c7036adbfd973cb")
            .exists().isEmpty();
        target.withFile("blimp-cache/generated-test-resources/blimp/mysql-535d9b3ce7d6e7948c7036adbfd973cb-create.csv")
            .exists().isNotEmpty();
        target.withFile("blimp-cache/generated-test-resources/blimp/mysql-535d9b3ce7d6e7948c7036adbfd973cb-update.csv")
            .exists().isNotEmpty();

        target.withFile("generated-test-resources/blimp/mysql/create/default-values.sql")
            .exists().isNotEmpty();
        target.withFile("generated-test-resources/blimp/mysql/initial_2021.07/default-values.sql")
            .doesNotExist();
        target.withFile("generated-test-resources/blimp/mysql/upgrade_2021.07_to_2021.08/default-values.sql")
            .exists().isNotEmpty();

        target.withFile("default-values-sql.zip")
            .exists().isNotEmpty();

        assertThat(installedArchive(result, null, "sql", "zip")).exists().isNotEmpty();
    }

    @MavenTest
    void dontStripComments(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("generated-resources/blimp/mysql/create/dont-strip-comments.sql")
            .satisfies(file -> {
                assertThat(Files.contentOf(file, StandardCharsets.UTF_8))
                    .contains("--  ");
            });
    }

    @MavenTest
    void formatSQL(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("generated-resources/blimp/mysql/create/format-sql.sql")
            .satisfies(file -> {
                assertThat(Files.contentOf(file, StandardCharsets.UTF_8))
                    .contains("\nCREATE TABLE product (\n    id");
                assertThat(Files.contentOf(file, StandardCharsets.UTF_8))
                    .doesNotMatch(".*--\\s+\\*+.*");
            });
    }

    @MavenTest
    @Disabled
    void liquibase3DotDot(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .withModule("service")
            .hasTarget();

        target.withFile("liquibase4-dot-dot-service-sql.zip")
            .exists().isNotEmpty();

        assertThat(installedArchive(result, "liquibase-dot-dot-service", "sql", "zip")).exists().isNotEmpty();
    }

    @MavenTest
    @Disabled
    void liquibase4DotDot(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .withModule("service")
            .hasTarget();

        target.withFile("liquibase4-dot-dot-service-sql.zip")
            .exists().isNotEmpty();

        assertThat(installedArchive(result, "liquibase4-dot-dot-service", "sql", "zip")).exists().isNotEmpty();
    }

    @MavenTest
    void multiModuleDotDot(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .withModule("service")
            .hasTarget();

        target.withFile("multi-module-dot-dot-service.jar")
            .exists().isNotEmpty();
    }

    @MavenTest
    @Disabled
    void multiModuleNoDotDot(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .withModule("service")
            .hasTarget();

        target.withFile("multi-module-no-dot-dot-service-sql.zip")
            .exists().isNotEmpty();

        assertThat(installedArchive(result, "multi-module-no-dot-dot-service", "sql", "zip")).exists().isNotEmpty();
    }

    @MavenTest
    void noAttach(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile(format("no-attach-sql.zip")).exists().isNotEmpty();
        target.withFile(format("no-attach-sql.tar")).exists().isNotEmpty();
        assertThat(installedArchive(result, null, "sql", "tar")).doesNotExist();
        assertThat(installedArchive(result, null, "sql", "zip")).doesNotExist();
    }

    @MavenTest
    void noUpdateScript(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("no-update-script-sql.zip")
            .exists().isNotEmpty();
        target.withFile("generated-resources/blimp/mysql/create/no-update-script.sql")
            .exists().isNotEmpty();

        assertThat(installedArchive(result, null, "sql", "zip")).exists().isNotEmpty();
    }

    @MavenTest
    void product(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("product-db-scripts.zip")
            .exists().isNotEmpty();
        target.withFile("generated-resources/blimp/mysql/create/product-db.sql")
            .exists().isNotEmpty();
        target.withFile("generated-resources/blimp/mysql/initial_2021.07/product-db.sql")
            .doesNotExist();
        target.withFile("generated-resources/blimp/mysql/upgrade_2021.07_to_2021.08/product-db.sql")
            .exists().isNotEmpty();

        assertThat(installedArchive(result, null, "scripts", "zip")).exists().isNotEmpty();
    }

    @MavenTest
    void review(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("review-db-sql.tar")
            .exists().isNotEmpty();
        target.withFile("generated-resources/blimp/mysql/create/review-db.sql")
            .exists().isNotEmpty();
        target.withFile("generated-resources/blimp/mysql/initial_2021.07/review-db.sql")
            .doesNotExist();
        target.withFile("generated-resources/blimp/mysql/upgrade_2021.07_to_2021.08/review-db.sql")
            .exists().isNotEmpty();

        assertThat(installedArchive(result, null, "sql", "tar")).exists().isNotEmpty();
    }

    @MavenTest
    void stripComments(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("generated-resources/blimp/mysql/create/strip-comments.sql")
            .satisfies(file -> {
                assertThat(Files.contentOf(file, StandardCharsets.UTF_8))
                    .doesNotContain("--  ");
            });
    }

    @MavenTest
    void testGenerate(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("generated-test-resources/blimp/mysql/create/product-db.sql")
            .exists().isNotEmpty();
        target.withFile("generated-test-resources/blimp/mysql/initial_2021.07/product-db.sql")
            .doesNotExist();
        target.withFile("generated-test-resources/blimp/mysql/upgrade_2021.07_to_2021.08/product-db.sql")
            .exists().isNotEmpty();
    }

    @MavenTest
    void unformatted(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("generated-resources/blimp/mysql/create/unformatted.sql")
            .satisfies(file -> {
                assertThat(Files.contentOf(file, StandardCharsets.UTF_8))
                    .contains("\nCREATE TABLE product (id ");
            });
    }

    @MavenTest
    void withInitialVersion(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("generated-resources/blimp/mysql/create/with-initial-version.sql")
            .exists().isNotEmpty();
        target.withFile("generated-resources/blimp/mysql/initial_2021.07/with-initial-version.sql")
            .exists().isNotEmpty();
    }

    @MavenTest
    void withoutInitialVersion(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("generated-resources/blimp/mysql/create/without-initial-version.sql")
            .exists().isNotEmpty();
        target.withFile("generated-resources/blimp/mysql/initial_2021.07/without-initial-version.sql")
            .doesNotExist();
        target.withFile("generated-resources/blimp/mysql/upgrade_2021.07_to_2021.08/without-initial-version.sql")
            .exists().isNotEmpty();
        target.withFile("generated-resources/blimp/mysql/upgrade_2021.08_to_2022.01/without-initial-version.sql")
            .exists().isNotEmpty();
    }
}
