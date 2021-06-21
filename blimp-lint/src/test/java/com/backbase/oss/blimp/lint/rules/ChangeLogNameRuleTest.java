package com.backbase.oss.blimp.lint.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.backbase.oss.blimp.lint.LintRuleViolation;
import java.util.List;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.Test;

class ChangeLogNameRuleTest extends AbstractRuleTest {

    ChangeLogNameRuleTest() {
        super(ChangeLogNameRule.NAME, ChangeLogNameRule.class);
    }

    @Test
    void matches0() throws LiquibaseException {
        setLintProperty(AbstractRuleConfiguration.MATCHES, "(.+/)?db.change-log\\.xml");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(1);
    }

    @Test
    void matches1() throws LiquibaseException {
        setLintProperty(AbstractRuleConfiguration.MATCHES, "db.change-log\\.xml");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(1);
    }

}
