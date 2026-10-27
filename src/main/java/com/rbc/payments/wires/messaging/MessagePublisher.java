package com.rbc.payments.wires.messaging;

/** Port for publishing outbound payment messages. Production binding is the platform Kafka producer. */
public interface MessagePublisher {

    void publish(String topic, String key, String payload);
}
