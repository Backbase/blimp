package com.backbase.oss.blimp.lint.rules;

import com.backbase.oss.blimp.lint.LintRuleViolation.Builder;
import java.util.stream.Stream;
import liquibase.change.Change;
import liquibase.change.core.AddPrimaryKeyChange;

public class PrimaryKeyAddNameRule extends AbstractRule<PrimaryKeyNameConfiguration> {

    public PrimaryKeyAddNameRule() {
        super(PrimaryKeyNameRule.NAME);

        filterChange(ch -> ch instanceof AddPrimaryKeyChange);
    }

    @Override
    protected Stream<Builder> validate(PrimaryKeyNameConfiguration cf, Change ch) {
        final AddPrimaryKeyChange pkch = (AddPrimaryKeyChange) ch;

        return validateInput(cf, ch.getChangeSet(), pkch.getConstraintName());
    }
}

