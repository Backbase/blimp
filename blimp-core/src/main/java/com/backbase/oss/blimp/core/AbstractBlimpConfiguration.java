package com.backbase.oss.blimp.core;

import static java.lang.String.format;

import liquibase.configuration.AbstractConfigurationContainer;

public abstract class AbstractBlimpConfiguration extends AbstractConfigurationContainer {
    public static final String ENABLED = "enabled";
    public static final String PRIORITY = "priority";

    protected final String name;

    public AbstractBlimpConfiguration(String namespace) {
        super("blimp." + namespace);

        final int lastDot = namespace.lastIndexOf('.');

        this.name = lastDot < 0 ? namespace : namespace.substring(lastDot + 1);

        getContainer()
            .addProperty(ENABLED, Boolean.class)
            .setDescription(format("Whether or not %s is enabled", this.name))
            .setDefaultValue(true);
    }

    protected void addPriority(int priority) {
        getContainer()
            .addProperty(PRIORITY, Integer.class)
            .setDescription(format("The priority of %s", this.name))
            .setDefaultValue(priority);
    }

    public boolean isEnabled() {
        return getValue(ENABLED, Boolean.class);
    }

    public Integer getPriority() {
        return getValue(PRIORITY, Integer.class);
    }
}
