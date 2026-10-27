package com.rbc.payments.wires.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * An outbound customer credit transfer, independent of the wire format used to send it.
 *
 * @param reference                 sender's reference, unique per transfer (MT103 :20:)
 * @param uetr                      unique end-to-end transaction reference (gpi UETR)
 * @param valueDate                 interbank settlement date
 * @param currency                  ISO 4217 currency code
 * @param amount                    settlement amount
 * @param senderBic                 BIC of the sending institution (RBC)
 * @param receiverBic               BIC of the next institution in the chain
 * @param orderingInstitutionBic    BIC of the debtor agent
 * @param intermediaryBic           optional BIC of an intermediary agent, may be {@code null}
 * @param beneficiaryInstitutionBic BIC of the creditor agent
 * @param orderingCustomer          debtor
 * @param beneficiary               creditor
 * @param remittanceInfo            optional unstructured remittance information, may be {@code null}
 * @param chargeBearer              charge bearer
 */
public record WireTransfer(
        String reference,
        UUID uetr,
        LocalDate valueDate,
        String currency,
        BigDecimal amount,
        String senderBic,
        String receiverBic,
        String orderingInstitutionBic,
        String intermediaryBic,
        String beneficiaryInstitutionBic,
        Party orderingCustomer,
        Party beneficiary,
        String remittanceInfo,
        ChargeBearer chargeBearer) {

    public WireTransfer {
        Objects.requireNonNull(reference, "reference");
        Objects.requireNonNull(uetr, "uetr");
        Objects.requireNonNull(valueDate, "valueDate");
        Objects.requireNonNull(currency, "currency");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(orderingCustomer, "orderingCustomer");
        Objects.requireNonNull(beneficiary, "beneficiary");
        Objects.requireNonNull(chargeBearer, "chargeBearer");
    }
}
