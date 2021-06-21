package com.backbase.oss.blimp.lint.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.backbase.oss.blimp.lint.LintRuleViolation;
import java.util.List;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.Test;

class OneChangeRuleTest extends AbstractRuleTest {

    OneChangeRuleTest() {
        super(OneChangeRule.NAME, OneChangeRule.class);
    }

    @Test
    void enabled() throws LiquibaseException {
        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(2);
    }

}
