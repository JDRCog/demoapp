package com.rbc.payments.wires.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Boolean feature flags. Resolution order: environment variable, then system property,
 * then {@code application.properties}, then the supplied default.
 */
public class FeatureFlags {

    private final Properties fileProperties;
    private final Properties systemProperties;
    private final Map<String, String> env;

    public FeatureFlags(Properties fileProperties, Properties systemProperties, Map<String, String> env) {
        this.fileProperties = fileProperties;
        this.systemProperties = systemProperties;
        this.env = env;
    }

    public static FeatureFlags fromEnvironment() {
        Properties file = new Properties();
        try (InputStream in = FeatureFlags.class.getResourceAsStream("/application.properties")) {
            if (in != null) {
                file.load(in);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read application.properties", e);
        }
        return new FeatureFlags(file, System.getProperties(), System.getenv());
    }

    public boolean isEnabled(String key, boolean defaultValue) {
        String envKey = key.toUpperCase(Locale.ROOT).replace('.', '_');
        String value = env.get(envKey);
        if (value == null) {
            value = systemProperties.getProperty(key);
        }
        if (value == null) {
            value = fileProperties.getProperty(key);
        }
        return value == null ? defaultValue : Boolean.parseBoolean(value.trim());
    }
}
