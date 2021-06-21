package com.backbase.oss.blimp.lint.rules;

import static java.util.Arrays.asList;

import com.backbase.oss.blimp.lint.LintRuleViolation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import liquibase.change.Change;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.CreateTableChange;

public abstract class AbstractColumnRule<C extends AbstractRuleConfiguration> extends AbstractRule<C> {
    private static final List<Class<? extends Change>> SUPPORTED = asList(
        CreateTableChange.class,
        AddColumnChange.class);

    private final Collection<Predicate<ColumnConfig>> columnFilter = new ArrayList<>();

    public AbstractColumnRule(String name) {
        super(name);

        filterChange(ch -> SUPPORTED.stream().anyMatch(t -> t.isInstance(ch)));
    }

    protected void filterColumn(Predicate<ColumnConfig> f) {
        this.columnFilter.add(f);
    }

    private boolean isFiltered(ColumnConfig cc) {
        return this.columnFilter.isEmpty() || this.columnFilter.stream().allMatch(f -> f.test(cc));
    }

    @Override
    protected Stream<LintRuleViolation.Builder> validate(C cf, Change ch) {
        final List<ColumnConfig> columns = ((ChangeWithColumns) ch).getColumns();

        return columns.stream()
            .filter(this::isFiltered)
            .flatMap(cc -> validate(cf, ch, cc));
    }

    protected Stream<LintRuleViolation.Builder> validate(C cf, Change ch, ColumnConfig cc) {
        return Stream.empty();
    }

}
