package com.backbase.oss.blimp.lint.rules;

public class PrimaryKeyNameConfiguration extends AbstractRuleConfiguration {

    public PrimaryKeyNameConfiguration() {
        super(PrimaryKeyNameRule.NAME, "The primary key name");

        addRequired();
        addMatches();
    }
}
