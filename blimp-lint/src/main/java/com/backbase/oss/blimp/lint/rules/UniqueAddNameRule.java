package com.backbase.oss.blimp.lint.rules;

import com.backbase.oss.blimp.lint.LintRuleViolation.Builder;
import java.util.stream.Stream;
import liquibase.change.Change;
import liquibase.change.core.AddUniqueConstraintChange;

public class UniqueAddNameRule extends AbstractRule<UniqueNameConfiguration> {

    public UniqueAddNameRule() {
        super(UniqueNameRule.NAME);

        filterChange(ch -> ch instanceof AddUniqueConstraintChange);
    }

    @Override
    protected Stream<Builder> validate(UniqueNameConfiguration cf, Change ch) {
        final AddUniqueConstraintChange uqch = (AddUniqueConstraintChange) ch;

        return validateInput(cf, ch.getChangeSet(), uqch.getConstraintName());
    }
}

