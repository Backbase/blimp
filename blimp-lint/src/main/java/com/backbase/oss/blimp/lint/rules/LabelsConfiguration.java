package com.backbase.oss.blimp.lint.rules;

public class LabelsConfiguration extends AbstractRuleConfiguration {

    public LabelsConfiguration() {
        super(LabelsRule.NAME, "The attribute 'labels'");

        addRequired();
        addMatches();
    }

}
