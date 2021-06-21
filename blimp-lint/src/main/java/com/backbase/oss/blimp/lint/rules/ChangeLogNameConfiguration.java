package com.backbase.oss.blimp.lint.rules;

public class ChangeLogNameConfiguration extends AbstractRuleConfiguration {
    public ChangeLogNameConfiguration() {
        super(ChangeLogNameRule.NAME, "The changelog name");

        addEquals();
        addMatches("(.+/)?db.changelog-(main|test)\\.(x|y(a)?)ml");
    }
}
