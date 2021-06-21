package com.backbase.oss.blimp.lint.rules;

import com.backbase.oss.blimp.lint.LintRuleViolation.Builder;
import java.util.stream.Stream;
import liquibase.change.Change;
import liquibase.change.core.CreateIndexChange;

public class IndexNameRule extends AbstractRule<IndexNameConfiguration> {

    public static final String NAME = "index-name";

    public IndexNameRule() {
        super(NAME);

        filterChange(ch -> ch instanceof CreateIndexChange);
    }

    @Override
    protected Stream<Builder> validate(IndexNameConfiguration cf, Change ch) {
        final CreateIndexChange cxch = (CreateIndexChange) ch;

        return validateInput(cf, ch.getChangeSet(), cxch.getIndexName());
    }
}
