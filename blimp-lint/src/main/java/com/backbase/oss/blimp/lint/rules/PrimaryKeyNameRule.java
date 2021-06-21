package com.backbase.oss.blimp.lint.rules;

import com.backbase.oss.blimp.lint.LintRuleViolation.Builder;
import java.util.stream.Stream;
import liquibase.change.Change;
import liquibase.change.ConstraintsConfig;

public class PrimaryKeyNameRule extends AbstractConstraintRule<PrimaryKeyNameConfiguration> {

    public static final String NAME = "primary-key-name";

    public PrimaryKeyNameRule() {
        super(NAME);

        filterConstraints(cc -> Boolean.TRUE.equals(cc.isPrimaryKey()));
    }

    @Override
    protected Stream<Builder> validate(PrimaryKeyNameConfiguration cf, Change ch, ConstraintsConfig cc) {
        return validateInput(cf, ch.getChangeSet(), cc.getPrimaryKeyName());
    }
}
