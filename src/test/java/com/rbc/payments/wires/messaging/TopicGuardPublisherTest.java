package com.rbc.payments.wires.messaging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TopicGuardPublisherTest {

    @Test
    void forwardsApprovedTopics() {
        InMemoryMessagePublisher sink = new InMemoryMessagePublisher();
        new TopicGuardPublisher(sink).publish(KafkaTopics.CROSSBORDER_MT103_OUTBOUND, "k", "v");
        assertThat(sink.messages()).containsExactly(
                new InMemoryMessagePublisher.Published(KafkaTopics.CROSSBORDER_MT103_OUTBOUND, "k", "v"));
    }

    @Test
    void rejectsUnapprovedTopics() {
        InMemoryMessagePublisher sink = new InMemoryMessagePublisher();
        assertThatThrownBy(() -> new TopicGuardPublisher(sink).publish("payments.scratch", "k", "v"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(sink.messages()).isEmpty();
    }
}
