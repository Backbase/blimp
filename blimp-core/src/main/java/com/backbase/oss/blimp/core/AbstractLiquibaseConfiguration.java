package com.backbase.oss.blimp.core;

import static java.lang.String.format;

import liquibase.configuration.AbstractConfigurationContainer;
import liquibase.sqlgenerator.SqlGenerator;

public abstract class AbstractLiquibaseConfiguration extends AbstractConfigurationContainer {
    public static final String ENABLED = "enabled";
    public static final String PRIORITY = "priority";

    public AbstractLiquibaseConfiguration(String namespace, String generatorName) {
        super(namespace);

        getContainer()
            .addProperty(ENABLED, Boolean.class)
            .setDescription(format("Controls wether or not %s generator is enabled", generatorName))
            .setDefaultValue(true);

        getContainer()
            .addProperty(PRIORITY, Integer.class)
            .setDescription(format("The priority of %s generator", generatorName))
            .setDefaultValue(SqlGenerator.PRIORITY_DEFAULT + 50);
    }
}


