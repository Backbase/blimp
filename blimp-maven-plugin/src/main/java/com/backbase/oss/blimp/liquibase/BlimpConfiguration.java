package com.backbase.oss.blimp.liquibase;

import liquibase.configuration.AbstractConfigurationContainer;

public class BlimpConfiguration extends AbstractConfigurationContainer {

    public static final String NAMESPACE = "blimp";
    public static final String FORMAT_SQL = "format-sql";

    public BlimpConfiguration() {
        super(NAMESPACE);

        getContainer()
            .addProperty(FORMAT_SQL, Boolean.class)
            .setDescription("Wether to apply Hibernate formatter to the generated SQL statements")
            .setDefaultValue(true);
    }
}
