package com.rbc.payments.wires.messaging;

import java.util.Set;

/**
 * Kafka topics this service is registered to produce to. Keep in sync with docs/kafka-topics.md.
 * Publishing anywhere else is rejected by {@link TopicGuardPublisher}.
 */
public final class KafkaTopics {

    public static final String CROSSBORDER_MT103_OUTBOUND = "payments.crossborder.mt103.outbound.v1";
    public static final String CROSSBORDER_PACS008_OUTBOUND = "payments.crossborder.pacs008.outbound.v1";
    public static final String LYNX_MT103_OUTBOUND = "payments.domestic.lynx.mt103.outbound.v1";
    public static final String LYNX_PACS008_OUTBOUND = "payments.domestic.lynx.pacs008.outbound.v1";

    public static final Set<String> APPROVED = Set.of(
            CROSSBORDER_MT103_OUTBOUND,
            CROSSBORDER_PACS008_OUTBOUND,
            LYNX_MT103_OUTBOUND,
            LYNX_PACS008_OUTBOUND);

    private KafkaTopics() {
    }
}
