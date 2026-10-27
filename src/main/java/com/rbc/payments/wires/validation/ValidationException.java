package com.rbc.payments.wires.validation;

import java.util.List;

/** Thrown when a transfer fails validation. Messages never contain customer data. */
public class ValidationException extends RuntimeException {

    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super("Wire transfer failed validation: " + String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> errors() {
        return errors;
    }
}
