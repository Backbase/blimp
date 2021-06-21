package com.backbase.oss.blimp.lint.rules;

import com.backbase.oss.blimp.lint.LintRuleViolation.Builder;
import java.util.stream.Stream;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;

public class ChangeLogNameRule extends AbstractRule<ChangeLogNameConfiguration> {

    public static final String NAME = "change-log-name";

    public ChangeLogNameRule() {
        super(NAME);
    }

    @Override
    public boolean supports(DatabaseChangeLog cl, ChangeSet cs) {
        return cs == null;
    }

    @Override
    protected Stream<Builder> validate(ChangeLogNameConfiguration cf, DatabaseChangeLog cl) {
        return validateInput(cf, null, cl.getPhysicalFilePath());
    }
}
