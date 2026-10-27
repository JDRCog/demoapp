package com.rbc.payments.crossborder.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;
import java.util.Optional;

/** A customer cross-border credit transfer instruction. Source of truth for outbound messages. */
public record WireTransfer(
        String reference,
        String uetr,
        LocalDate valueDate,
        BigDecimal amount,
        Currency currency,
        Party debtor,
        String debtorAgentBic,
        Party creditor,
        String creditorAgentBic,
        String intermediaryAgentBic,
        String remittanceInformation,
        ChargeBearer chargeBearer) {

    public WireTransfer {
        Objects.requireNonNull(reference, "reference");
        Objects.requireNonNull(uetr, "uetr");
        Objects.requireNonNull(valueDate, "valueDate");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        Objects.requireNonNull(debtor, "debtor");
        Objects.requireNonNull(debtorAgentBic, "debtorAgentBic");
        Objects.requireNonNull(creditor, "creditor");
        Objects.requireNonNull(creditorAgentBic, "creditorAgentBic");
        Objects.requireNonNull(chargeBearer, "chargeBearer");
    }

    public Optional<String> intermediary() {
        return Optional.ofNullable(intermediaryAgentBic);
    }

    public Optional<String> remittance() {
        return Optional.ofNullable(remittanceInformation);
    }
}
