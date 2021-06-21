package com.backbase.oss.blimp.lint.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.backbase.oss.blimp.lint.LintRuleViolation;
import java.util.List;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.Test;

class RemarksRuleTest extends AbstractRuleTest {

    RemarksRuleTest() {
        super(RemarksRule.NAME, RemarksRule.class);
    }

    @Test
    void required() throws LiquibaseException {
        setLintProperty(AbstractRuleConfiguration.REQUIRED, "true");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(3);
    }

    @Test
    void notRequired() throws LiquibaseException {
        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(0);
    }
}
