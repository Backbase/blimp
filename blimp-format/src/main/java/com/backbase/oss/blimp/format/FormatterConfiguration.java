package com.backbase.oss.blimp.format;


import com.backbase.oss.blimp.core.AbstractLiquibaseConfiguration;

public class FormatterConfiguration extends AbstractLiquibaseConfiguration {

    public static final String NAMESPACE = "blimp.formatter";

    public FormatterConfiguration() {
        super(NAMESPACE, "Blimp Formatter");
    }
}
