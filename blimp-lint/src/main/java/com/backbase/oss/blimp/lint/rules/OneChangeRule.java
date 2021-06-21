package com.backbase.oss.blimp.lint.rules;

import com.backbase.oss.blimp.lint.LintRuleViolation;
import java.util.stream.Stream;
import liquibase.changelog.ChangeSet;

public class OneChangeRule extends AbstractRule<OneChangeConfiguration> {

    public static final String NAME = "one-change";
    public static final String MESSAGE = "Only one change is allowed in change set '%1$s', found %4$d";

    public OneChangeRule() {
        super(NAME);
    }

    @Override
    @SuppressWarnings("unused")
    protected Stream<LintRuleViolation.Builder> validate(OneChangeConfiguration cf, ChangeSet cs) {
        if (cs.getChanges().size() == 1) {
            return Stream.empty();
        }

        return Stream.of(LintRuleViolation.builder()
            .id(cs.getId())
            .property("")
            .message(MESSAGE)
            .values(cs.getChanges().size()));
    }
}
