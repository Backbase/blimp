package com.backbase.oss.blimp.format;


import com.backbase.oss.blimp.core.AbstractBlimpConfiguration;
import liquibase.sqlgenerator.SqlGenerator;

public class FormatterConfiguration extends AbstractBlimpConfiguration {

    public FormatterConfiguration() {
        super("formatter");

        addPriority(SqlGenerator.PRIORITY_DEFAULT + 50);
    }
}
