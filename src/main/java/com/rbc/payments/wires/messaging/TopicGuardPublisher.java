package com.rbc.payments.wires.messaging;

import java.util.Objects;

/** Decorator that refuses to publish to topics not listed in {@link KafkaTopics#APPROVED}. */
public class TopicGuardPublisher implements MessagePublisher {

    private final MessagePublisher delegate;

    public TopicGuardPublisher(MessagePublisher delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public void publish(String topic, String key, String payload) {
        if (!KafkaTopics.APPROVED.contains(topic)) {
            throw new IllegalArgumentException("Topic not approved for this service: " + topic);
        }
        delegate.publish(topic, key, payload);
    }
}
