package com.rbc.payments.crossborder.publish;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Test/local publisher that records messages instead of sending them to Kafka. */
public final class InMemoryPublisher implements MessagePublisher {

    public record Published(String topic, String key, String payload) {}

    private final List<Published> messages = new ArrayList<>();

    @Override
    public synchronized void publish(String topic, String key, String payload) {
        messages.add(new Published(topic, key, payload));
    }

    public synchronized List<Published> messages() {
        return Collections.unmodifiableList(new ArrayList<>(messages));
    }
}
