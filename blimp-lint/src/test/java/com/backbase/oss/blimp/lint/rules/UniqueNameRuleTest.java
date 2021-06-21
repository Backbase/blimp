package com.backbase.oss.blimp.lint.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.backbase.oss.blimp.lint.LintRuleViolation;
import java.util.List;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.Test;

class UniqueNameRuleTest extends AbstractRuleTest {

    UniqueNameRuleTest() {
        super(UniqueNameRule.NAME, UniqueNameRule.class, UniqueAddNameRule.class);
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

    @Test
    void matchesRequired() throws LiquibaseException {
        setLintProperty(AbstractRuleConfiguration.REQUIRED, "true");
        setLintProperty(AbstractRuleConfiguration.MATCHES, "uq_.+");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(3);
    }

    @Test
    void matchesNotRequired() throws LiquibaseException {
        setLintProperty(AbstractRuleConfiguration.MATCHES, "uq_.+");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(0);
    }

    @Test
    void noneMatchesRequired() throws LiquibaseException {
        setLintProperty(AbstractRuleConfiguration.REQUIRED, "true");
        setLintProperty(AbstractRuleConfiguration.MATCHES, "x+");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(6);
    }

    @Test
    void noneMatchesNotRequired() throws LiquibaseException {
        setLintProperty(AbstractRuleConfiguration.MATCHES, "x+");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(3);
    }

    @Test
    void equalsNotAvailable() throws LiquibaseException {
        setLintProperty(AbstractRuleConfiguration.EQUALS, "xxx");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(0);
    }

}
