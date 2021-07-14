package com.backbase.oss.blimp.lint.rules;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import com.backbase.oss.blimp.core.AbstractBlimpConfiguration;
import com.backbase.oss.blimp.core.LiquibaseEngine;
import com.backbase.oss.blimp.core.PropertiesConfigProvider;
import com.backbase.oss.blimp.lint.BlimpLinter;
import com.backbase.oss.blimp.lint.LintRule;
import com.backbase.oss.blimp.lint.LintRuleFinder;
import com.backbase.oss.blimp.lint.LintRuleViolation;
import java.util.List;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
abstract class AbstractRuleTest {

    private final String rule;
    private final Class<? extends LintRule>[] types;

    public AbstractRuleTest(String rule, Class<? extends LintRule>... types) {
        this.rule = rule;
        this.types = types;
    }

    private final PropertiesConfigProvider properties = new PropertiesConfigProvider();

    @BeforeEach
    void setUp() throws Exception {
        setLintProperty(AbstractBlimpConfiguration.ENABLED, "true");
    }

    @Test
    @Order(-100)
    void disabled() throws LiquibaseException {
        setLintProperty(AbstractBlimpConfiguration.ENABLED, "false");

        final List<LintRuleViolation> violations = runLint();

        assertThat(violations).hasSize(0);
    }

    void setLintProperty(String property, String value) {
        this.properties.setProperty(format("blimp.lint.%s.%s", this.rule, property), value);
    }

    List<LintRuleViolation> runLint() throws LiquibaseException {
        LintRuleFinder.getInstance().setTypes(this.types);

        final String file = this.rule + "/db.changelog-main.xml";
        final LiquibaseEngine engine = LiquibaseEngine.builder()
            .changeLogFile(file)
            .classLoader(getClass().getClassLoader())
            .configProvider(this.properties)
            .build();
        final List<LintRuleViolation> violations = engine.visit(new BlimpLinter());

        violations.forEach(System.out::println);

        return violations;
    }
}
