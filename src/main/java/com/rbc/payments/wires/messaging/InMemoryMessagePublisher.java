package com.rbc.payments.wires.messaging;

import java.util.ArrayList;
import java.util.List;

/** Publisher that records messages in memory. Used for local runs and tests. */
public class InMemoryMessagePublisher implements MessagePublisher {

    public record Published(String topic, String key, String payload) {
    }

    private final List<Published> messages = new ArrayList<>();

    @Override
    public synchronized void publish(String topic, String key, String payload) {
        messages.add(new Published(topic, key, payload));
    }

    public synchronized List<Published> messages() {
        return List.copyOf(messages);
    }
}
