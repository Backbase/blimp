package com.backbase.oss.blimp.lint.rules;

import com.backbase.oss.blimp.lint.LintRuleViolation;
import java.util.stream.Stream;
import liquibase.change.Change;
import liquibase.change.core.AddForeignKeyConstraintChange;

public class ForeignKeyDeferredRule extends AbstractRule<ForeignKeyDeferredConfiguration> {

    public static final String NAME = "foreign-key-deferred";
    public static final String MESSAGE = "The 'deferred' attribute is not supported for foreign key '%4$s'";

    public ForeignKeyDeferredRule() {
        super(NAME);

        filterChange(ch -> ch instanceof AddForeignKeyConstraintChange);
        filterChange(ch -> isDeferrableForeignKeyChange((AddForeignKeyConstraintChange) ch));
    }

    @Override
    @SuppressWarnings("unused")
    protected Stream<LintRuleViolation.Builder> validate(ForeignKeyDeferredConfiguration cf, Change ch) {
        final AddForeignKeyConstraintChange fkch = (AddForeignKeyConstraintChange) ch;

        return Stream.of(LintRuleViolation.builder()
            .property("unsupported")
            .message(MESSAGE)
            .values(fkch.getConstraintName()));
    }

    private boolean isDeferrableForeignKeyChange(AddForeignKeyConstraintChange change) {
        return Boolean.TRUE.equals(change.getDeferrable()) || Boolean.TRUE.equals(change.getInitiallyDeferred());
    }
}
