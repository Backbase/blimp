package com.backbase.oss.blimp.lint.rules;

import com.backbase.oss.blimp.lint.LintRuleViolation;
import com.backbase.oss.blimp.lint.LintRuleViolation.Builder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;
import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;

public abstract class AbstractConstraintRule<C extends AbstractRuleConfiguration> extends AbstractColumnRule<C> {

    private final Collection<Predicate<ConstraintsConfig>> constraintsFilter = new ArrayList<>();

    public AbstractConstraintRule(String name) {
        super(name);

        filterColumn(cc -> cc.getConstraints() != null);
        filterColumn(cc -> isFiltered(cc.getConstraints()));
    }

    protected void filterConstraints(Predicate<ConstraintsConfig> f) {
        this.constraintsFilter.add(f);
    }

    private boolean isFiltered(ConstraintsConfig cc) {
        return this.constraintsFilter.isEmpty() || this.constraintsFilter.stream().allMatch(f -> f.test(cc));
    }

    @Override
    protected Stream<LintRuleViolation.Builder> validate(C cf, Change ch, ColumnConfig cc) {
        return validate(cf, ch, cc.getConstraints());
    }

    protected abstract Stream<Builder> validate(C cf, Change ch, ConstraintsConfig cc);
}
