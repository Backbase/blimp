package com.backbase.oss.blimp;

import static com.backbase.oss.blimp.TestUtils.installedArchive;
import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;
import static com.soebes.itf.jupiter.extension.MavenCLIOptions.BATCH_MODE;
import static com.soebes.itf.jupiter.extension.MavenCLIOptions.*;
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
@SystemProperty(value = "changelog.location", content = "src/test/resources")
@MavenGoal({"clean", "install"})
@MavenOption(BATCH_MODE)
@MavenOption(FAIL_AT_END)
@MavenOption(SETTINGS)
@MavenOption("settings.xml")
@MavenRepository
class BlimpIT {

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
    void product(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("product-db-scripts.zip")
            .exists().isNotEmpty();
        target.withFile("generated-resources/liquibase/mysql/create/product-db.sql")
            .exists().isNotEmpty();
        target.withFile("generated-resources/liquibase/mysql/initial_2021.07/product-db.sql")
            .exists().isNotEmpty();
        target.withFile("generated-resources/liquibase/mysql/upgrade_2021.07_to_2021.08/product-db.sql")
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
        target.withFile("generated-resources/liquibase/mysql/create/review-db.sql")
            .exists().isNotEmpty();
        target.withFile("generated-resources/liquibase/mysql/initial_2021.07/review-db.sql")
            .exists().isNotEmpty();
        target.withFile("generated-resources/liquibase/mysql/upgrade_2021.07_to_2021.08/review-db.sql")
            .exists().isNotEmpty();

        assertThat(installedArchive(result, null, "sql", "tar")).exists().isNotEmpty();
    }

    @MavenTest
    void noUpdateScript(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("no-update-script-sql.zip")
            .exists().isNotEmpty();
        target.withFile("generated-resources/liquibase/mysql/create/no-update-script.sql")
            .exists().isNotEmpty();
        target.withFile("generated-resources/liquibase/mysql/initial_2021.07/no-update-script.sql")
            .doesNotExist();

        assertThat(installedArchive(result, null, "sql", "zip")).exists().isNotEmpty();
    }

    @MavenTest
    void forceUpdateScript(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("force-update-script-sql.zip")
            .exists().isNotEmpty();
        target.withFile("generated-resources/liquibase/mysql/create/force-update-script.sql")
            .exists().isNotEmpty();
        target.withFile("generated-resources/liquibase/mysql/initial_2021.07/force-update-script.sql")
            .exists().isNotEmpty();

        assertThat(installedArchive(result, null, "sql", "zip")).exists().isNotEmpty();
    }

    @MavenTest
    void formatted(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("generated-resources/liquibase/mysql/create/formatted.sql")
            .satisfies(file -> {
                assertThat(Files.contentOf(file, StandardCharsets.UTF_8))
                    .contains("\nCREATE TABLE product (\n ");
            });
    }

    @MavenTest
    void unformatted(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("generated-resources/liquibase/mysql/create/unformatted.sql")
            .satisfies(file -> {
                assertThat(Files.contentOf(file, StandardCharsets.UTF_8))
                    .contains("\nCREATE TABLE product (id ");
            });
    }

    @MavenTest
    void unformattedWithHibernate(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("generated-resources/liquibase/mysql/create/unformatted-with-hibernate.sql")
            .satisfies(file -> {
                assertThat(Files.contentOf(file, StandardCharsets.UTF_8))
                    .contains("\nCREATE TABLE product (id ");
            });
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
}
