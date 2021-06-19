package com.backbase.oss.blimp;

import static java.util.Optional.ofNullable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import liquibase.ContextExpression;
import liquibase.LabelExpression;
import liquibase.Labels;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.changelog.visitor.ChangeSetVisitor;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;

class GroupsVisitor implements ChangeSetVisitor {

    private final Set<String> contexts = new LinkedHashSet<>();
    private final Set<String> labels = new LinkedHashSet<>();

    private ScriptGroupingStrategy strategy;

    GroupsVisitor() {
        this(ScriptGroupingStrategy.AUTO);
    }

    GroupsVisitor(ScriptGroupingStrategy strategy) {
        this.strategy = strategy;
    }

    Set<String> groups() {
        switch (this.strategy) {
            case AUTO:
                if (this.contexts.isEmpty()) {
                    this.strategy = ScriptGroupingStrategy.LABELS;
                    return this.labels;
                } else {
                    this.strategy = ScriptGroupingStrategy.CONTEXTS;
                    return this.contexts;
                }

            case CONTEXTS:
                return this.contexts;

            case LABELS:
                return this.labels;

            default:
                throw new AssertionError("supposed to be unreachable");
        }
    }

    ScriptGroupingStrategy strategy() {
        return this.strategy;
    }

    @Override
    public Direction getDirection() {
        return Direction.FORWARD;
    }

    @SuppressWarnings("unused")
    @Override
    public void visit(ChangeSet changeSet, DatabaseChangeLog changeLog, Database database,
        Set<ChangeSetFilterResult> results) throws LiquibaseException {

        collect(changeLog);
        collect(changeSet.getChangeLog());
        collect(changeSet);
    }

    private void collect(ChangeSet changeSet) {
        ofNullable(changeSet.getContexts())
            .map(ContextExpression::getContexts)
            .ifPresent(this::pickFirstContext);
        ofNullable(changeSet.getLabels())
            .map(Labels::getLabels)
            .ifPresent(this::pickFirstLabel);
    }

    private void collect(DatabaseChangeLog changeLog) {
        ofNullable(changeLog.getIncludeContexts())
            .map(ContextExpression::getContexts)
            .ifPresent(this::pickFirstContext);
        ofNullable(changeLog.getIncludeLabels())
            .map(LabelExpression::getLabels)
            .ifPresent(this::pickFirstLabel);
    }

    private void pickFirstContext(Collection<String> contexts) {
        if (contexts == null || contexts.isEmpty()) {
            return;
        }

        this.contexts.add(contexts.iterator().next());
    }

    private void pickFirstLabel(Collection<String> labels) {
        if (labels == null || labels.isEmpty()) {
            return;
        }

        this.labels.add(labels.iterator().next());
    }

}
