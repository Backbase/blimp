package com.backbase.oss.blimp.lint.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.backbase.oss.blimp.lint.LintRuleViolation;
import java.util.List;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.Test;

class LabelsRuleTest extends AbstractRuleTest {

    LabelsRuleTest() {
        super(LabelsRule.NAME, LabelsRule.class);
    }

    @Test
    void required() throws LiquibaseException {
        setLintProperty(AbstractRuleConfiguration.REQUIRED, "true");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(1);
    }

    @Test
    void notRequired() throws LiquibaseException {
        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(0);
    }

    @Test
    void equalsNotAvailable() throws LiquibaseException {
        setLintProperty(AbstractRuleConfiguration.EQUALS, "ana");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(0);
    }

    @Test
    void matches() throws LiquibaseException {
        setLintProperty(AbstractRuleConfiguration.REQUIRED, "true");
        setLintProperty(AbstractRuleConfiguration.MATCHES, "\\d{4}\\.\\d{2}");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(2);
    }

    @Test
    void matchesNotRequired() throws LiquibaseException {
        setLintProperty(AbstractRuleConfiguration.MATCHES, "\\d{4}\\.\\d{2}");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(1);
    }

}
