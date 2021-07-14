package com.backbase.oss.blimp;

import static org.assertj.core.api.Assertions.assertThat;

import com.backbase.oss.blimp.core.NormalizedResourceAccessor;
import com.backbase.oss.blimp.liquibase.LiquibaseGenerator;
import java.util.Set;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LiquibaseGroupsTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "product-db",
        "review-db",
    })
    void run(String changes) throws LiquibaseException {
        final LiquibaseGenerator engine = LiquibaseGenerator.builder()
            .accessor(new NormalizedResourceAccessor())
            .changeLogFile(changes + "/changelog/db.changelog-persistence.xml")
            .build();

        final GroupsVisitor gv = new GroupsVisitor();
        final Set<String> groups = engine.visit(gv);

        assertThat(groups).hasSize(3);
        assertThat(gv.getStrategy()).isEqualTo(ScriptGroupingStrategy.CONTEXTS);

        // test propagation
        final LiquibaseGenerator newEngine =
            engine.toBuilder()
                .groups(groups)
                .strategy(gv.getStrategy())
                .build()
                .toBuilder()
                .build();

        assertThat(newEngine.getGroups())
            .containsExactlyElementsOf(groups);
        assertThat(newEngine.getStrategy())
            .isEqualTo(gv.getStrategy());
    }

}
