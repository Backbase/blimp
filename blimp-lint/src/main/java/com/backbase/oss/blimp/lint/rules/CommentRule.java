package com.backbase.oss.blimp.lint.rules;

import com.backbase.oss.blimp.lint.LintRuleViolation;
import java.util.stream.Stream;
import liquibase.changelog.ChangeSet;

public class CommentRule extends AbstractRule<CommentConfiguration> {

    public static final String NAME = "comment";

    public CommentRule() {
        super(NAME);
    }

    @Override
    @SuppressWarnings("unused")
    protected Stream<LintRuleViolation.Builder> validate(CommentConfiguration cf, ChangeSet cs) {
        return validateInput(cf, cs, cs.getComments());
    }
}
