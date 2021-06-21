package com.backbase.oss.blimp.lint.rules;

public class UniqueNameConfiguration extends AbstractRuleConfiguration {

    public UniqueNameConfiguration() {
        super(UniqueNameRule.NAME, "The unique constraint name");

        addRequired();
        addMatches();
    }
}
