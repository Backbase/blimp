package com.backbase.oss.blimp.lint.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.backbase.oss.blimp.lint.LintRuleViolation;
import java.util.List;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthorRuleTest extends AbstractRuleTest {

    AuthorRuleTest() {
        super(AuthorRule.NAME, AuthorRule.class);
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
    void equalsRequired() throws LiquibaseException {
        setLintProperty(AbstractRuleConfiguration.REQUIRED, "true");
        setLintProperty(AbstractRuleConfiguration.EQUALS, "bob");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(2);
    }

    @Test
    void equalsNotRequired() throws LiquibaseException {
        setLintProperty(AbstractRuleConfiguration.EQUALS, "bob");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(1);
    }

    @Test
    void bothEqualRequired() throws LiquibaseException {
        setLintProperty(AbstractRuleConfiguration.REQUIRED, "true");
        setLintProperty(AbstractRuleConfiguration.EQUALS, "alice,bob");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(1);
    }

    @Test
    void bothEqualNotRequired() throws LiquibaseException {
        setLintProperty(AbstractRuleConfiguration.EQUALS, "alice,bob");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(0);
    }

    @Test
    void noneEqualsRequired() throws LiquibaseException {
        setLintProperty(AbstractRuleConfiguration.REQUIRED, "true");
        setLintProperty(AbstractRuleConfiguration.EQUALS, "eleanor,rigby");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(3);
    }

    @Test
    void noneEqualsNotRequired() throws LiquibaseException {
        setLintProperty(AbstractRuleConfiguration.EQUALS, "eleanor,rigby");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(2);
    }

    @Test
    void matchesRequired() throws LiquibaseException {
        setLintProperty(AbstractRuleConfiguration.REQUIRED, "true");
        setLintProperty(AbstractRuleConfiguration.MATCHES, "b.+b");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(2);
    }

    @Test
    void matchesNotRequired() throws LiquibaseException {
        setLintProperty(AbstractRuleConfiguration.MATCHES, "b.+b");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(1);
    }

    @Test
    void noneMatchesRequired() throws LiquibaseException {
        setLintProperty(AbstractRuleConfiguration.REQUIRED, "true");
        setLintProperty(AbstractRuleConfiguration.MATCHES, "x+");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(3);
    }

    @Test
    void noneMatchesNotRequired() throws LiquibaseException {
        setLintProperty(AbstractRuleConfiguration.MATCHES, "x+");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(2);
    }

}
