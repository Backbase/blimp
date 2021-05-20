package com.backbase.maven.sqlgen;

import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;
import static java.lang.String.format;

import com.soebes.itf.extension.assertj.MavenProjectResultAssert;
import com.soebes.itf.jupiter.extension.MavenGoal;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenOption;
import com.soebes.itf.jupiter.extension.MavenRepository;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.extension.SystemProperty;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;
import java.io.File;
import org.apache.maven.model.Model;

@MavenJupiterExtension
@SystemProperty(value = "changelog.location", content = "src/test/resources")
@MavenGoal("install")
@MavenOption("-s")
@MavenOption("settings.xml")
@MavenRepository
class SqlGenMojoIT {
    @MavenTest
    void arrangementManager(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("arrangement-manager-sql.zip")
            .exists();
        target.withFile("generated-resources/liquibase/mssql/create/arrangement-manager.sql")
            .exists();
        target.withFile("generated-resources/liquibase/mssql/create_2_19_0/arrangement-manager.sql")
            .exists();
        target.withFile("generated-resources/liquibase/mssql/upgrade_2_20_5_to_2_21_0/arrangement-manager.sql")
            .exists();

        assertThat(installedArchive(result, "sql", "zip")).doesNotExist();
    }

    @MavenTest
    void product(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("product-db-scripts.zip")
            .exists();
        target.withFile("generated-resources/liquibase/mysql/create/product-db.sql")
            .exists();
        target.withFile("generated-resources/liquibase/mysql/initial_2021.07/product-db.sql")
            .exists();
        target.withFile("generated-resources/liquibase/mysql/upgrade_2021.07_to_2021.08/product-db.sql")
            .exists();

        assertThat(installedArchive(result, "scripts", "zip")).exists();
    }

    @MavenTest
    void review(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("review-db-sql.tar")
            .exists();
        target.withFile("generated-resources/liquibase/mysql/create/review-db.sql")
            .exists();
        target.withFile("generated-resources/liquibase/mysql/initial_2021.07/review-db.sql")
            .exists();
        target.withFile("generated-resources/liquibase/mysql/upgrade_2021.07_to_2021.08/review-db.sql")
            .exists();

        assertThat(installedArchive(result, "sql", "tar")).exists();
    }

    private File installedArchive(MavenExecutionResult result, final String classifier, final String format) {
        final Model model = result.getMavenProjectResult().getModel();
        final File cache = result.getMavenProjectResult().getTargetCacheDirectory();
        final File installed = new File(cache, format("%1$s/%2$s/%3$s/%2$s-%3$s-%4$s.%5$s",
            model.getGroupId().replace('.', File.separatorChar),
            model.getArtifactId(), model.getVersion(),
            classifier, format));

        return installed;
    }
}
