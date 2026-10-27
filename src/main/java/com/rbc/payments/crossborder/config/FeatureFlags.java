package com.rbc.payments.crossborder.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * Single point of access for feature flags and service configuration. System properties override
 * values from {@code application.properties}.
 */
public final class FeatureFlags {

    private final Properties properties;

    public FeatureFlags(Properties properties) {
        this.properties = properties;
    }

    public static FeatureFlags fromClasspath() {
        Properties props = new Properties();
        try (InputStream in = FeatureFlags.class.getResourceAsStream("/application.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to load application.properties", e);
        }
        props.putAll(System.getProperties());
        return new FeatureFlags(props);
    }

    public boolean isEnabled(String flag, boolean defaultValue) {
        String value = properties.getProperty(flag);
        return value == null ? defaultValue : Boolean.parseBoolean(value.trim());
    }

    public String get(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalStateException("Missing required configuration: " + key);
        }
        return value;
    }
}
