package com.backbase.oss.blimp.lint.rules;

public class ForeignKeyNameConfiguration extends AbstractRuleConfiguration {

    public ForeignKeyNameConfiguration() {
        super(ForeignKeyNameRule.NAME, "The foreign key name");

        addRequired();
        addMatches();
    }
}
