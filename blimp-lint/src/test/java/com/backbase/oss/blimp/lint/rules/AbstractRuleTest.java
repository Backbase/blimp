package com.backbase.oss.blimp.lint.rules;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import com.backbase.oss.blimp.core.AbstractBlimpConfiguration;
import com.backbase.oss.blimp.lint.BlimpLinter;
import com.backbase.oss.blimp.lint.LintRule;
import com.backbase.oss.blimp.lint.LintRuleFinder;
import com.backbase.oss.blimp.lint.LintRuleViolation;
import java.util.List;
import java.util.Properties;
import liquibase.configuration.ConfigurationProperty;
import liquibase.configuration.ConfigurationValueProvider;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
abstract class AbstractRuleTest implements ConfigurationValueProvider {

    private final String rule;
    private final Class<? extends LintRule>[] types;

    public AbstractRuleTest(String rule, Class<? extends LintRule>... types) {
        this.rule = rule;
        this.types = types;
    }

    private final Properties properties = new Properties();

    @BeforeEach
    void setUp() throws Exception {
        LiquibaseConfiguration.getInstance().init(this);
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
        final BlimpLinter linter = BlimpLinter.builder()
            .changeLogFile(file)
            .build();

        final List<LintRuleViolation> violations = linter.run();

        violations.forEach(System.out::println);

        return violations;
    }

    @Override
    public Object getValue(String namespace, String property) {
        return this.properties.get(namespace + "." + property);
    }

    @Override
    public String describeValueLookupLogic(ConfigurationProperty property) {
        return format("[test] '%s.%s'", property.getNamespace(), property.getName());
    }
}
