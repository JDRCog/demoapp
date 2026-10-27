package com.rbc.payments.crossborder.model;

import java.util.Objects;

/** Debtor or creditor of a wire. */
public record Party(String name, String account, PostalAddress address) {

    public Party {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(account, "account");
        Objects.requireNonNull(address, "address");
    }
}
