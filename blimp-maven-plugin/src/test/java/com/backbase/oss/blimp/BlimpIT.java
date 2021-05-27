package com.backbase.oss.blimp;

import static com.backbase.oss.blimp.TestUtils.installedArchive;
import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;
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

@MavenJupiterExtension
@SystemProperty(value = "changelog.location", content = "src/test/resources")
@MavenGoal("install")
@MavenOption("-B")
@MavenOption("-s")
@MavenOption("settings.xml")
@MavenRepository
class BlimpIT {
    @MavenTest
    void arrangementManager(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .withModule("service")
            .hasTarget();

        target.withFile("arrangement-manager-sql.zip")
            .exists().isNotEmpty();

        target.withFile("generated-resources/liquibase/mssql/create/arrangement-manager.sql")
            .exists().isNotEmpty();
        target.withFile("generated-resources/liquibase/mssql/create_2_19_0/arrangement-manager.sql")
            .exists().isNotEmpty();
        target.withFile("generated-resources/liquibase/mssql/upgrade_2_20_5_to_2_21_0/arrangement-manager.sql")
            .exists().isNotEmpty();

        target.withFile("generated-resources/liquibase/mysql/create/arrangement-manager.sql")
            .exists().isNotEmpty();
        target.withFile("generated-resources/liquibase/mysql/create_2_19_0/arrangement-manager.sql")
            .exists().isNotEmpty();
        target.withFile("generated-resources/liquibase/mysql/upgrade_2_20_5_to_2_21_0/arrangement-manager.sql")
            .exists().isNotEmpty();

        assertThat(installedArchive(result, "arrangement-manager", "sql", "zip")).exists().isNotEmpty();
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
