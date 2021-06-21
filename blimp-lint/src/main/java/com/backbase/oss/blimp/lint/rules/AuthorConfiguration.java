package com.backbase.oss.blimp.lint.rules;

public class AuthorConfiguration extends AbstractRuleConfiguration {

    public AuthorConfiguration() {
        super(AuthorRule.NAME, "The attribute 'author'");

        addRequired();
        addEquals();
        addMatches();
    }
}
