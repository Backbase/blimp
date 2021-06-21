package com.backbase.oss.blimp.lint.rules;

import com.backbase.oss.blimp.lint.LintRuleViolation.Builder;
import java.util.stream.Stream;
import liquibase.change.Change;
import liquibase.change.ColumnConfig;

public class RemarksRule extends AbstractColumnRule<RemarksConfiguration> {

    public static final String NAME = "remarks";
    private static final String MESSAGE = "The column '%4$s' is missing remarks";

    public RemarksRule() {
        super(NAME);
    }

    @Override
    protected Stream<Builder> validate(RemarksConfiguration cf, Change ch, ColumnConfig cc) {
        return validateInput(cf, ch.getChangeSet(), cc.getRemarks())
            .map(b -> b.message(MESSAGE).values(cc.getName()));
    }
}
