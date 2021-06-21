package com.backbase.oss.blimp.lint.rules;

import static java.util.stream.Collectors.toSet;

import com.backbase.oss.blimp.lint.LintRuleViolation;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import liquibase.changelog.ChangeSet;

public class LabelsRule extends AbstractRule<LabelsConfiguration> {

    public static final String NAME = "labels";

    public LabelsRule() {
        super(NAME);
    }

    @Override
    protected Stream<LintRuleViolation.Builder> validate(LabelsConfiguration cf, ChangeSet cs) {
        final Set<String> labels = Stream
            .concat(
                cs.getLabels().getLabels().stream(),
                cs.getInheritableLabels().stream().flatMap(x -> x.getLabels().stream()))
            .filter(Objects::nonNull)
            .collect(toSet());

        return labels.isEmpty()
            ? validateInput(cf, cs, null)
            : labels.stream().flatMap(label -> validateInput(cf, cs, label));
    }
}
