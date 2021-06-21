package com.backbase.oss.blimp.lint;

import java.util.List;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;

/**
 * Represents a validation rule of the {@code DatabaseChangeLog}.
 */
public interface LintRule {

    /**
     * Rule name which is also used for property toggle.
     */
    String getName();

    /**
     * Validate {@link DatabaseChangeLog} and {@link ChangeSet}.
     *
     * @param cl the change log to validate
     * @param cs the change set to validate
     *
     * @return list of {@link LintRuleViolation} object(s) in case of failure or empty list
     */
    List<LintRuleViolation> validate(DatabaseChangeLog cl, ChangeSet cs);

    /**
     * Whether this rule supports the specified changeset.
     *
     * @param cl the change log to validate
     * @param cs the change set to validate
     *
     * @return {@code true} if the rule is supported
     */
    boolean supports(DatabaseChangeLog cl, ChangeSet cs);
}
