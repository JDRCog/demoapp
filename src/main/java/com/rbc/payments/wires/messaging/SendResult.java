package com.rbc.payments.wires.messaging;

/** Outcome of sending a transfer: which format and topic were used, and the rendered payload. */
public record SendResult(MessageFormat format, String topic, String payload) {
}
