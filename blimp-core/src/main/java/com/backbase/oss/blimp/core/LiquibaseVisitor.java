package com.backbase.oss.blimp.core;

import liquibase.changelog.visitor.ChangeSetVisitor;

public interface LiquibaseVisitor<T> extends ChangeSetVisitor {

    @Override
    default Direction getDirection() {
        return Direction.FORWARD;
    }

    T getResult();
}
