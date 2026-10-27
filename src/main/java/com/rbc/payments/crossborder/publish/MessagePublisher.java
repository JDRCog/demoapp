package com.rbc.payments.crossborder.publish;

/** Abstraction over the Kafka producer so the service can be tested without a broker. */
public interface MessagePublisher {

    void publish(String topic, String key, String payload);
}
