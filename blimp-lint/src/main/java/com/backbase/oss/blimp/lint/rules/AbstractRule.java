package com.backbase.oss.blimp.lint.rules;

import static java.util.Optional.ofNullable;
import static io.leangen.geantyref.GenericTypeReflector.getTypeParameter;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import com.backbase.oss.blimp.lint.LintRule;
import com.backbase.oss.blimp.lint.LintRuleViolation;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.configuration.LiquibaseConfiguration;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractRule<C extends AbstractRuleConfiguration> implements LintRule {

    private static final TypeVariable<? extends Class<?>> CONFIG_TYPE = AbstractRule.class.getTypeParameters()[0];

    private final String name;

    private final Collection<Predicate<Change>> changeFilter = new ArrayList<>();

    @Override
    public final String getName() {
        return this.name;
    }

    protected void filterChange(Predicate<Change> f) {
        this.changeFilter.add(f);
    }

    private boolean isFiltered(Change ch) {
        return this.changeFilter.isEmpty() || this.changeFilter.stream().allMatch(f -> f.test(ch));
    }

    /* {@inheritDoc} */
    @Override
    public boolean supports(DatabaseChangeLog cl, ChangeSet cs) {
        return true;
    }

    /* {@inheritDoc} */
    @Override
    public final List<LintRuleViolation> validate(DatabaseChangeLog cl, ChangeSet cs) {
        final C cf = getConfiguration();

        if (!cf.isEnabled()) {
            return emptyList();
        }

        final Collection<Stream<LintRuleViolation.Builder>> builders = new ArrayList<>();

        builders.add(validate(cf, cl));

        if (cs != null) {
            builders.add(validate(cf, cs));
            builders.add(cs.getChanges().stream()
                .filter(ch -> isFiltered(ch))
                .flatMap(ch -> validate(cf, ch)));
        }

        return builders.stream()
            .flatMap(UnaryOperator.identity())
            .map(b -> b.id(ofNullable(cs).map(ChangeSet::getId).orElse("<" + this.name + ">")))
            .map(b -> b.rule(getName()))
            .map(b -> b.severity(cf.getSeverity()))
            .map(b -> b.database(cl.getRuntimeEnvironment().getTargetDatabase().getShortName()))
            .map(LintRuleViolation.Builder::build)
            .collect(toList());
    }

    @SuppressWarnings("unused")
    protected Stream<LintRuleViolation.Builder> validate(C cf, DatabaseChangeLog cl) {
        return Stream.empty();
    }

    @SuppressWarnings("unused")
    protected Stream<LintRuleViolation.Builder> validate(C cf, ChangeSet cs) {
        return Stream.empty();
    }

    @SuppressWarnings("unused")
    protected Stream<LintRuleViolation.Builder> validate(C cf, Change ch) {
        return Stream.empty();
    }

    protected final Stream<LintRuleViolation.Builder> validateInput(C cf, ChangeSet cs, String input) {
        return cf.validate(input)
            .map(Stream::of)
            .orElse(Stream.empty());
    }

    private C getConfiguration() {
        final Class<C> type = (Class<C>) getTypeParameter(getClass(), AbstractRule.CONFIG_TYPE);

        return LiquibaseConfiguration.getInstance()
            .getConfiguration(type);
    }
}
