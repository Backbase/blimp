package com.backbase.oss.blimp.lint.rules;

public class IndexNameConfiguration extends AbstractRuleConfiguration {

    public IndexNameConfiguration() {
        super(IndexNameRule.NAME);

        addRequired();
        addMatches();
    }
}
