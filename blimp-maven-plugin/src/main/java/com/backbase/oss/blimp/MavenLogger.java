package com.backbase.oss.blimp;

import static java.lang.String.format;

import liquibase.logging.LogType;
import liquibase.logging.core.AbstractLogger;
import lombok.RequiredArgsConstructor;
import org.apache.maven.plugin.logging.Log;

@RequiredArgsConstructor
class MavenLogger extends AbstractLogger {

    private final Log log;
    private final String cat;

    @Override
    public void severe(LogType target, String message) {
        if (this.log.isErrorEnabled()) {
            this.log.error(format("[%s] %s: %s", this.cat, target, message));
        }
    }

    @Override
    public void severe(LogType target, String message, Throwable e) {
        if (this.log.isErrorEnabled()) {
            this.log.error(format("[%s] %s: %s", this.cat, target, message), e);
        }
    }

    @Override
    public void warning(LogType target, String message) {
        if (this.log.isWarnEnabled()) {
            this.log.warn(format("[%s] %s: %s", this.cat, target, message));
        }
    }

    @Override
    public void warning(LogType target, String message, Throwable e) {
        if (this.log.isWarnEnabled()) {
            this.log.warn(format("[%s] %s: %s", this.cat, target, message), e);
        }
    }

    @Override
    public void info(LogType target, String message) {
        if (this.log.isInfoEnabled()) {
            this.log.info(format("[%s] %s: %s", this.cat, target, message));
        }
    }

    @Override
    public void info(LogType target, String message, Throwable e) {
        if (this.log.isInfoEnabled()) {
            this.log.info(format("[%s] %s: %s", this.cat, target, message, e));
        }
    }

    @Override
    public void debug(LogType target, String message) {
        if (this.log.isDebugEnabled()) {
            this.log.debug(format("[%s] %s: %s", this.cat, target, message));
        }
    }

    @Override
    public void debug(LogType target, String message, Throwable e) {
        if (this.log.isDebugEnabled()) {
            this.log.debug(format("[%s] %s: %s", this.cat, target, message), e);
        }
    }
}

