package com.backbase.oss.blimp.lint.rules;

public class CommentConfiguration extends AbstractRuleConfiguration {

    public CommentConfiguration() {
        super(CommentRule.NAME, "The element 'comment'");

        addRequired();
    }
}


