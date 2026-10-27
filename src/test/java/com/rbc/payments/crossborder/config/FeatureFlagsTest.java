package com.rbc.payments.crossborder.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;
import org.junit.jupiter.api.Test;

class FeatureFlagsTest {

    @Test
    void readsFlagsWithDefaults() {
        Properties p = new Properties();
        p.setProperty("a", " true ");
        FeatureFlags flags = new FeatureFlags(p);
        assertTrue(flags.isEnabled("a", false));
        assertFalse(flags.isEnabled("missing", false));
    }

    @Test
    void loadsClasspathConfig() {
        assertEquals("payments.crossborder.mt103.outbound.v1",
                FeatureFlags.fromClasspath().get("crossborder.topic.mt103"));
    }

    @Test
    void failsOnMissingRequiredConfig() {
        assertThrows(IllegalStateException.class, () -> new FeatureFlags(new Properties()).get("nope"));
    }
}
