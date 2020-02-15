package com.github.vlmap.spring.loadbalancer.util;


import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.env.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EnvironmentUtils {

    public static List<String> getKeys(ConfigurableEnvironment environment) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, PropertySource<?>> entry : getPropertySources(environment).entrySet()) {
            PropertySource<?> source = entry.getValue();
            if (source instanceof EnumerablePropertySource) {
                EnumerablePropertySource<?> enumerable = (EnumerablePropertySource<?>) source;
                for (String name : enumerable.getPropertyNames()) {
                    result.add(name);
                }
            }
        }
        return result;
    }

    public static ConfigurationPropertySource getSubsetConfigurationPropertySource(ConfigurableEnvironment environment, String prefix) {
        Map<String, Object> map = new ConcurrentHashMap<>();
        MapConfigurationPropertySource propertySource = new MapConfigurationPropertySource();
        List<String> keys = getKeys(environment);
        String delimiter = ".";
        for (String key : keys) {
            String childKey = toSubsetKey(key, prefix, delimiter);
            if (childKey != null) {
                map.put(childKey, environment.getProperty(key));
            }
        }
        return propertySource;

    }

    public static String toSubsetKey(String key, String prefix, String delimiter) {
        if (!key.startsWith(prefix)) {
            return null;
        } else {
            String modifiedKey = null;
            if (key.length() == prefix.length()) {
                modifiedKey = "";
            } else {
                int i = prefix.length() + (delimiter != null ? delimiter.length() : 0);
                modifiedKey = key.substring(i);
            }

            return modifiedKey;
        }

    }


    private static Map<String, PropertySource<?>> getPropertySources(ConfigurableEnvironment environment) {
        Map<String, PropertySource<?>> map = new LinkedHashMap<>();
        MutablePropertySources sources = (environment != null
                ? environment.getPropertySources()
                : new StandardEnvironment().getPropertySources());
        for (PropertySource<?> source : sources) {
            extract("", map, source);
        }
        return map;
    }

    private static void extract(String root, Map<String, PropertySource<?>> map,
                                PropertySource<?> source) {
        if (source instanceof CompositePropertySource) {
            for (PropertySource<?> nest : ((CompositePropertySource) source)
                    .getPropertySources()) {
                extract(source.getName() + ":", map, nest);
            }
        } else {
            map.put(root + source.getName(), source);
        }
    }


}
