package com.backbase.oss.blimp.lint.rules;

import com.backbase.oss.blimp.lint.LintRuleViolation.Builder;
import java.util.stream.Stream;
import liquibase.change.Change;
import liquibase.change.ConstraintsConfig;

public class UniqueNameRule extends AbstractConstraintRule<UniqueNameConfiguration> {

    public static final String NAME = "unique-constraint-name";

    public UniqueNameRule() {
        super(NAME);

        filterConstraints(cc -> Boolean.TRUE.equals(cc.isUnique()));
    }

    @Override
    protected Stream<Builder> validate(UniqueNameConfiguration cf, Change ch, ConstraintsConfig cc) {
        return validateInput(cf, ch.getChangeSet(), cc.getUniqueConstraintName());
    }

}
