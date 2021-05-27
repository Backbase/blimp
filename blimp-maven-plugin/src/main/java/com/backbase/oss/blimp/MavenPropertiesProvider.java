package com.backbase.oss.blimp;

import java.util.Properties;
import liquibase.configuration.ConfigurationProperty;
import liquibase.configuration.ConfigurationValueProvider;

public class MavenPropertiesProvider extends Properties implements ConfigurationValueProvider {

    @Override
    public Object getValue(String namespace, String property) {
        return getProperty(namespace + "." + property);
    }

    @Override
    public String describeValueLookupLogic(ConfigurationProperty property) {
        return "Maven configuration '" + property.getNamespace() + "." + property.getName() + "'";
    }
}


