package com.backbase.oss.blimp.lint.rules;

public class ContextConfiguration extends AbstractRuleConfiguration {

    public ContextConfiguration() {
        super(ContextRule.NAME, "The 'context' attribute");

        addRequired();
        addMatches();
    }

}
