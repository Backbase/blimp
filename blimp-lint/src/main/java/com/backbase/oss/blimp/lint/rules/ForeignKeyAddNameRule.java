package com.backbase.oss.blimp.lint.rules;

import com.backbase.oss.blimp.lint.LintRuleViolation.Builder;
import java.util.stream.Stream;
import liquibase.change.Change;
import liquibase.change.core.AddForeignKeyConstraintChange;

public class ForeignKeyAddNameRule extends AbstractRule<ForeignKeyNameConfiguration> {

    public ForeignKeyAddNameRule() {
        super(ForeignKeyNameRule.NAME);

        filterChange(ch -> ch instanceof AddForeignKeyConstraintChange);
    }

    @Override
    protected Stream<Builder> validate(ForeignKeyNameConfiguration cf, Change ch) {
        final AddForeignKeyConstraintChange fkch = (AddForeignKeyConstraintChange) ch;

        return validateInput(cf, ch.getChangeSet(), fkch.getConstraintName());
    }

}
