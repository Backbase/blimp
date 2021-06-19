package com.backbase.oss.blimp;

import static org.assertj.core.api.Assertions.assertThat;

import com.backbase.oss.blimp.liquibase.LiquibaseEngine;
import com.backbase.oss.blimp.liquibase.NormalizedResourceAccessor;
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
        final LiquibaseEngine engine = LiquibaseEngine.builder()
            .accessor(new NormalizedResourceAccessor())
            .changeLogFile(changes + "/changelog/db.changelog-persistence.xml")
            .build();

        final GroupsVisitor gv = engine.visit(new GroupsVisitor());

        assertThat(gv.groups()).hasSize(3);
        assertThat(gv.strategy()).isEqualTo(ScriptGroupingStrategy.CONTEXTS);

        // test propagation
        final LiquibaseEngine newEngine =
            engine.newBuilder()
                .groups(gv.groups())
                .strategy(gv.strategy())
                .build()
                .newBuilder()
                .build();

        assertThat(newEngine.getGroups())
            .containsExactlyElementsOf(gv.groups());
        assertThat(newEngine.getStrategy())
            .isEqualTo(gv.strategy());
    }

}
