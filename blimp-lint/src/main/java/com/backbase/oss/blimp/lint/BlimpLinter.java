package com.backbase.oss.blimp.lint;

import com.backbase.oss.blimp.core.NormalizedResourceAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import liquibase.RuntimeEnvironment;
import liquibase.changelog.ChangeLogIterator;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.changelog.visitor.ChangeSetVisitor;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.resource.ResourceAccessor;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

@Builder
public class BlimpLinter implements ChangeSetVisitor {

    private final List<LintRuleViolation> violations = new ArrayList<>();

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final List<LintRule> rules = rules();
    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final DatabaseChangeLog changeLog = parse();

    @Builder.Default
    private final ResourceAccessor accessor = new NormalizedResourceAccessor();
    @NonNull
    private final String changeLogFile;

    private final ChangeLogParameters parameters = new ChangeLogParameters();

    public BlimpLinter withProperties(Properties properties) {
        properties.forEach((k, v) -> {
            this.parameters.set((String) k, v);
        });

        return this;
    }

    public List<LintRuleViolation> run() throws LiquibaseException {
        this.violations.clear();

        getRules()
            .stream()
            .filter(rule -> rule.supports(getChangeLog(), null))
            .forEach(rule -> {
                this.violations.addAll(rule.validate(getChangeLog(), null));
            });

        new ChangeLogIterator(getChangeLog())
            .run(this, new RuntimeEnvironment(null, null, null));

        return this.violations;
    }

    @Override
    public Direction getDirection() {
        return Direction.FORWARD;
    }

    @Override
    public void visit(ChangeSet changeSet, DatabaseChangeLog changeLog, Database database,
        Set<ChangeSetFilterResult> results) throws LiquibaseException {

        getRules()
            .stream()
            .filter(rule -> rule.supports(changeLog, changeSet))
            .forEach(rule -> {
                this.violations.addAll(rule.validate(changeLog, changeSet));
            });
    }

    private List<LintRule> rules() {
        return LintRuleFinder.getInstance().getRules();
    }

    @SneakyThrows
    private DatabaseChangeLog parse() {
        return new XMLChangeLogSAXParser()
            .parse(this.changeLogFile, this.parameters, this.accessor);
    }
}
