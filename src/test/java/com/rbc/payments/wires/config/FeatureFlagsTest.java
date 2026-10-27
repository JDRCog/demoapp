package com.rbc.payments.wires.config;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class FeatureFlagsTest {

    private static Properties props(String key, String value) {
        Properties p = new Properties();
        p.setProperty(key, value);
        return p;
    }

    @Test
    void fallsBackToDefault() {
        FeatureFlags flags = new FeatureFlags(new Properties(), new Properties(), Map.of());
        assertThat(flags.isEnabled("some.flag", false)).isFalse();
        assertThat(flags.isEnabled("some.flag", true)).isTrue();
    }

    @Test
    void envBeatsSystemPropertyBeatsFile() {
        Properties file = props("some.flag", "true");
        Properties sys = props("some.flag", "false");

        assertThat(new FeatureFlags(file, new Properties(), Map.of()).isEnabled("some.flag", false)).isTrue();
        assertThat(new FeatureFlags(file, sys, Map.of()).isEnabled("some.flag", true)).isFalse();
        assertThat(new FeatureFlags(file, sys, Map.of("SOME_FLAG", " TRUE ")).isEnabled("some.flag", false))
                .isTrue();
    }

    @Test
    void loadsFromEnvironment() {
        assertThat(FeatureFlags.fromEnvironment().isEnabled("does.not.exist", true)).isTrue();
    }
}
