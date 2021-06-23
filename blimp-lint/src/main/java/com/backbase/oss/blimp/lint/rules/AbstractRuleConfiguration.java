package com.backbase.oss.blimp.lint.rules;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static liquibase.util.StringUtils.isEmpty;

import com.backbase.oss.blimp.core.AbstractBlimpConfiguration;
import com.backbase.oss.blimp.lint.LintRuleSeverity;
import com.backbase.oss.blimp.lint.LintRuleViolation;
import java.util.List;
import java.util.Optional;

public abstract class AbstractRuleConfiguration extends AbstractBlimpConfiguration {
    public static final String SEVERITY = "severity";

    public static final String REQUIRED = "required";
    private static final String M_REQUIRED = "%4$s is required";

    public static final String EQUALS = "equals";
    private static final String M_EQUALS = "%4$s '%5$s' doesn't equal '%6$s'";

    public static final String MATCHES = "matches";
    private static final String M_MATCHES = "%4$s '%5$s' doesn't match the expected pattern '%6$s'";

    private boolean hasRequired;
    private boolean hasMatches;
    private boolean hasEquals;

    private final String description;

    public AbstractRuleConfiguration(String rule) {
        this(rule, rule);
    }

    public AbstractRuleConfiguration(String rule, String description) {
        super("lint." + rule);

        this.description = description;

        getContainer()
            .addProperty(SEVERITY, String.class)
            .setDescription(format("The severity of the rule '%s' violation", this.description))
            .setDefaultValue(LintRuleSeverity.values()[0].name());
    }

    public LintRuleSeverity getSeverity() {
        return LintRuleSeverity.valueOf(getValue(SEVERITY, String.class).toUpperCase());
    }

    protected void addRequired() {
        this.hasRequired = true;

        getContainer()
            .addProperty(REQUIRED, Boolean.class)
            .setDescription(format("Whether or not '%s' is required", this.description))
            .setDefaultValue(false);
    }

    protected void addEquals(String... equals) {
        this.hasEquals = true;

        getContainer()
            .addProperty(EQUALS, List.class)
            .setDescription(format("Whether or not '%s' equals '%s'", this.description, equals))
            .setDefaultValue(asList(equals));
    }

    protected void addMatches(String... matches) {
        this.hasMatches = true;

        getContainer()
            .addProperty(MATCHES, List.class)
            .setDescription(format("Whether or not '%s' matches '%s'", this.description, matches))
            .setDefaultValue(asList(matches));
    }

    protected Optional<LintRuleViolation.Builder> validate(String input) {
        if (isEmpty(input)) {
            if (this.hasRequired && getValue(REQUIRED, Boolean.class)) {
                return Optional.of(
                    LintRuleViolation.builder()
                        .property(REQUIRED)
                        .message(M_REQUIRED)
                        .values(this.description, input));
            }

            return Optional.empty();
        }

        if (this.hasEquals) {
            final List<String> exp = getValue(EQUALS, List.class);

            if (!exp.isEmpty()) {
                if (exp.stream().anyMatch(input::equals)) {
                    return Optional.empty();
                }

                return Optional.of(
                    LintRuleViolation.builder()
                        .property(EQUALS)
                        .message(M_EQUALS)
                        .values(this.description, input, exp));
            }
        }

        if (this.hasMatches) {
            final List<String> pat = getValue(MATCHES, List.class);

            if (!pat.isEmpty()) {
                if (pat.stream().anyMatch(input::matches)) {
                    return Optional.empty();
                }

                return Optional.of(
                    LintRuleViolation.builder()
                        .property(MATCHES)
                        .message(M_MATCHES)
                        .values(this.description, input, pat));
            }
        }

        return Optional.empty();
    }
}
