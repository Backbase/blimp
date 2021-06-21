package com.backbase.oss.blimp.lint.rules;

import static liquibase.util.StringUtils.*;
import com.backbase.oss.blimp.lint.LintRuleViolation.Builder;
import java.util.stream.Stream;
import liquibase.change.Change;
import liquibase.change.ConstraintsConfig;

public class ForeignKeyNameRule extends AbstractConstraintRule<ForeignKeyNameConfiguration> {

    public static final String NAME = "foreign-key-name";

    public ForeignKeyNameRule() {
        super(NAME);

        filterConstraints(cc -> isNotEmpty(cc.getReferencedTableName()));
    }

    @Override
    protected Stream<Builder> validate(ForeignKeyNameConfiguration cf, Change ch, ConstraintsConfig cc) {
        return validateInput(cf, ch.getChangeSet(), cc.getForeignKeyName());
    }
}
