package com.backbase.oss.blimp;

import static java.util.Optional.ofNullable;

import com.backbase.oss.blimp.core.LiquibaseVisitor;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;
import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

class DigestVisitor implements LiquibaseVisitor<String> {
    private final MessageDigest digest = DigestUtils.getMd5Digest();

    @Override
    public String getResult() {
        return Hex.encodeHexString(this.digest.digest());
    }

    @SuppressWarnings("unused")
    @Override
    public void visit(ChangeSet changeSet, DatabaseChangeLog changeLog, Database database,
        Set<ChangeSetFilterResult> results) throws LiquibaseException {

        ofNullable(changeSet.getContexts())
            .map(ContextExpression::getContexts)
            .ifPresent(set -> set.forEach(this::update));
        ofNullable(changeSet.getLabels())
            .map(Labels::getLabels)
            .ifPresent(set -> set.forEach(this::update));

        update(changeSet.generateCheckSum().toString());
    }

    private void update(String text) {
        this.digest.update(text.toString().getBytes(StandardCharsets.UTF_8));
    }

}
