package com.backbase.oss.blimp;

import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;
import static com.soebes.itf.jupiter.extension.MavenCLIOptions.BATCH_MODE;
import static com.soebes.itf.jupiter.extension.MavenCLIOptions.FAIL_AT_END;
import static com.soebes.itf.jupiter.extension.MavenCLIOptions.SETTINGS;
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
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@MavenJupiterExtension
@SystemProperty(value = "changelog.location", content = "target/dependency")
@MavenGoal({"clean", "install"})
@MavenOption(BATCH_MODE)
@MavenOption(FAIL_AT_END)
@MavenOption(SETTINGS)
@MavenOption("settings.xml")
@MavenOption("-Dproducts.version=1.7.21")
@MavenOption("-Dblimp-lint-rules.version=0.1.0")
@MavenRepository
@EnabledIfSystemProperty(named = "blimp-internal-test", matches = "^true$")
class BackbaseIT {

    @MavenTest
    void lint(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("site/blimp.csv")
            .exists().isNotEmpty();
    }

    @MavenTest
    void lintFail(MavenExecutionResult result) {
        assertThat(result).isFailure();

        final MavenProjectResultAssert target = assertThat(result)
            .project()
            .hasTarget();

        target.withFile("site/blimp.csv")
            .exists().isNotEmpty();

        final File stdout = result.getMavenLog().getStdout().toFile();

        assertThat(contentOf(stdout, Charset.defaultCharset()))
            .matches("(?s)"
                + ".*\\[ERROR\\]"
                + ".*db.changelog-persistence.xml"
                + ".*doesn't"
                + ".*equal.+");
    }

    @MavenTest
    void productsDirectory(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("generated-resources/blimp/mysql/create/products-directory.sql")
            .exists().isNotEmpty();
        target.withFile("generated-resources/blimp/mysql/upgrade_2021_09_to_2021_11/products-directory.sql")
            .exists().isNotEmpty();
    }

    @MavenTest
    void productsClasspath(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile("generated-resources/blimp/mysql/create/products-classpath.sql")
            .exists().isNotEmpty();
        target.withFile("generated-resources/blimp/mysql/upgrade_2021_09_to_2021_11/products-classpath.sql")
            .exists().isNotEmpty();
    }
}
