package com.rbc.payments.wires.model;

import java.util.List;
import java.util.Objects;

/**
 * A customer party (ordering customer or beneficiary).
 *
 * <p>All fields except {@code addressLines} are PII and must never be logged unmasked.
 */
public record Party(String name, String account, List<String> addressLines, String townName, String countryCode) {

    public Party {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(account, "account");
        addressLines = addressLines == null ? List.of() : List.copyOf(addressLines);
    }

    @Override
    public String toString() {
        return "Party[name=***, account=***, countryCode=" + countryCode + "]";
    }
}
