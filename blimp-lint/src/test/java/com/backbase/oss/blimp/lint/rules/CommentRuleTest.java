package com.backbase.oss.blimp.lint.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.backbase.oss.blimp.lint.LintRuleViolation;
import java.util.List;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.Test;

class CommentRuleTest extends AbstractRuleTest {

    CommentRuleTest() {
        super(CommentRule.NAME, CommentRule.class);
    }

    @Test
    void required() throws LiquibaseException {
        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(0);
    }

    @Test
    void notRequired() throws LiquibaseException {
        setLintProperty(AbstractRuleConfiguration.REQUIRED, "true");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(1);
    }

}
