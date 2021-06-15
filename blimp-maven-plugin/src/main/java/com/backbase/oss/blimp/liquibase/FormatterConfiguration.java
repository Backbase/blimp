package com.backbase.oss.blimp.liquibase;

import liquibase.configuration.AbstractConfigurationContainer;
import liquibase.sqlgenerator.SqlGenerator;

public class FormatterConfiguration extends AbstractConfigurationContainer {

    public static final String NAMESPACE = "blimp.formatter";
    public static final String ENABLED = "enabled";
    public static final String PRIORITY = "priority";

    public FormatterConfiguration() {
        super(NAMESPACE);

        getContainer()
            .addProperty(ENABLED, Boolean.class)
            .setDescription("Wether to apply Blimp formatter to the generated SQL statements")
            .setDefaultValue(true);

        getContainer()
            .addProperty(PRIORITY, Integer.class)
            .setDescription("The priority of the Blimp formatter")
            .setDefaultValue(SqlGenerator.PRIORITY_DATABASE + 1);
    }
}
