package com.backbase.oss.blimp.lint.rules;

public class RemarksConfiguration extends AbstractRuleConfiguration {

    public RemarksConfiguration() {
        super(RemarksRule.NAME);

        addRequired();
    }
}
