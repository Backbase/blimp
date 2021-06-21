package com.backbase.oss.blimp.lint.rules;

import com.backbase.oss.blimp.lint.LintRuleViolation;
import java.util.stream.Stream;
import liquibase.changelog.ChangeSet;

public class AuthorRule extends AbstractRule<AuthorConfiguration> {

    public static final String NAME = "author";

    public AuthorRule() {
        super(NAME);
    }

    @Override
    protected Stream<LintRuleViolation.Builder> validate(AuthorConfiguration cf, ChangeSet cs) {
        return validateInput(cf, cs, cs.getAuthor());
    }

}
