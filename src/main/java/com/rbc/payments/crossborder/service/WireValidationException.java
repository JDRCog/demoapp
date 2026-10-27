package com.rbc.payments.crossborder.service;

/** Thrown when a wire cannot be sent. Messages must never contain clear-text PII. */
public class WireValidationException extends RuntimeException {

    public WireValidationException(String message) {
        super(message);
    }
}
