package com.backbase.oss.blimp.lint.rules;

import static java.util.stream.Collectors.toSet;

import com.backbase.oss.blimp.lint.LintRuleViolation;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import liquibase.changelog.ChangeSet;

public class ContextRule extends AbstractRule<ContextConfiguration> {

    public static final String NAME = "context";

    public ContextRule() {
        super(NAME);
    }

    @Override
    protected Stream<LintRuleViolation.Builder> validate(ContextConfiguration cf, ChangeSet cs) {
        final Set<String> contexts = Stream
            .concat(
                Stream.of(cs.getContexts()),
                cs.getInheritableContexts().stream())
            .filter(Objects::nonNull)
            .flatMap(x -> x.getContexts().stream())
            .collect(toSet());

        return contexts.isEmpty()
            ? validateInput(cf, cs, null)
            : contexts.stream().flatMap(context -> validateInput(cf, cs, context));
    }
}
