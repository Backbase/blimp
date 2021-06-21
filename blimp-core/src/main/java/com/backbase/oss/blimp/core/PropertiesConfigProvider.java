package com.backbase.oss.blimp.core;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;
import liquibase.configuration.ConfigurationProperty;
import liquibase.configuration.ConfigurationValueProvider;
import liquibase.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.yaml.snakeyaml.Yaml;

@RequiredArgsConstructor
public class PropertiesConfigProvider extends Properties implements ConfigurationValueProvider {
    static private String encode(Stream<?> stream) {
        return stream
            .map(Object::toString)
            .map(String::trim)
            .filter(s -> s.length() > 0)
            .map(s -> s.replace(",", "\\,"))
            .collect(joining(","));
    }

    static private String path(String... names) {
        return Stream.of(names).filter(StringUtils::isNotEmpty).collect(joining("."));
    }

    private final String name;

    public PropertiesConfigProvider() {
        this("properties");
    }

    public void loadYaml(InputStream input) {
        final Yaml yaml = new Yaml();

        yaml.loadAll(input).forEach(o -> add("", o));
    }

    @Override
    public Object getValue(String namespace, String property) {
        return getProperty(namespace + "." + property);
    }

    @Override
    public String describeValueLookupLogic(ConfigurationProperty property) {
        return format("[%s] '%s.%s'", this.name, property.getNamespace(), property.getName());
    }

    private void add(String prefix, Object value) {
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> ms = (Map<String, Object>) value;

            ms.forEach((k, s) -> {
                add(path(prefix, k), s);
            });
        } else if (value instanceof Collection) {
            add(prefix, encode(((Collection<?>) value).stream()));
        } else if (value instanceof Object[]) {
            add(prefix, encode(stream((Object[]) value)));
        } else if (value != null) {
            setProperty(prefix, Objects.toString(value));
        }
    }
}
