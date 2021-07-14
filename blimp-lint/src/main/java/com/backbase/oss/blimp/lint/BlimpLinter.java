package com.backbase.oss.blimp.lint;

import com.backbase.oss.blimp.core.LiquibaseVisitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import lombok.AccessLevel;
import lombok.Getter;

public class BlimpLinter implements LiquibaseVisitor<List<LintRuleViolation>> {

    @Getter
    private final List<LintRuleViolation> result = new ArrayList<>();

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final List<LintRule> rules = rules();

    private final Set<DatabaseChangeLog> changeLogs = Collections.newSetFromMap(new IdentityHashMap<>());

    @Override
    public void visit(ChangeSet changeSet, DatabaseChangeLog changeLog, Database database,
        Set<ChangeSetFilterResult> results) throws LiquibaseException {
        final List<LintRule> rules = getRules();

        if (this.changeLogs.add(changeLog)) {
            rules
                .stream()
                .filter(rule -> rule.supports(changeLog, null))
                .forEach(rule -> {
                    this.result.addAll(rule.validate(changeLog, null));
                });
        }

        rules
            .stream()
            .filter(rule -> rule.supports(changeLog, changeSet))
            .forEach(rule -> {
                this.result.addAll(rule.validate(changeLog, changeSet));
            });
    }

    private List<LintRule> rules() {
        return LintRuleFinder.getInstance().getRules();
    }
}
