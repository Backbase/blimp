package com.backbase.oss.blimp;

import liquibase.logging.Logger;
import liquibase.logging.LoggerContext;
import liquibase.logging.LoggerFactory;
import liquibase.logging.core.NoOpLoggerContext;
import lombok.RequiredArgsConstructor;
import org.apache.maven.plugin.logging.Log;

@RequiredArgsConstructor
class MavenLoggerFactory implements LoggerFactory {

    private final Log log;

    @Override
    public Logger getLog(Class clazz) {
        return new MavenLogger(this.log, clazz.getName());
    }

    @Override
    public LoggerContext pushContext(String key, Object object) {
        return new NoOpLoggerContext();
    }

    @Override
    public void close() {}
}


