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
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@MavenJupiterExtension
@SystemProperty(value = "changelog.location", content = "src/test/resources")
@MavenGoal("install")
@MavenOption("-B")
@MavenOption("-X")
@MavenRepository
@EnabledIfSystemProperty(named = "blimp-internal-test", matches = "^true$")
class BackbaseIT {

    @MavenTest
    void liquibaseExtension(MavenExecutionResult result) {
        final MavenProjectResultAssert target = assertThat(result).isSuccessful()
            .project()
            .hasTarget();

        target.withFile(format("backbase-liquibase-ext-sql.zip")).exists().isNotEmpty();
        assertThat(installedArchive(result, null, "sql", "zip")).exists().isNotEmpty();
    }

    @MavenTest
    void liquibaseExtensionLint(MavenExecutionResult result) {
        assertThat(result).isFailure();
    }
}
