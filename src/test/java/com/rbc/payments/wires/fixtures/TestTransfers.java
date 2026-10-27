package com.rbc.payments.wires.fixtures;

import com.rbc.payments.wires.model.ChargeBearer;
import com.rbc.payments.wires.model.Party;
import com.rbc.payments.wires.model.WireTransfer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Synthetic transfers only. Never copy production data into fixtures. */
public final class TestTransfers {

    public static final UUID UETR = UUID.fromString("8a562c67-ca16-48ba-b074-65581be6f001");

    private TestTransfers() {
    }

    public static Party debtor() {
        return new Party("TEST DEBTOR ONE", "00012345678", List.of("100 TEST STREET", "SUITE 200"),
                "TORONTO", "CA");
    }

    public static Party creditor() {
        return new Party("TEST CREDITOR TWO", "GB33BUKB20201555555555", List.of("1 SAMPLE ROAD"),
                "LONDON", "GB");
    }

    public static WireTransfer usdToGb() {
        return new WireTransfer(
                "RBC2410270001",
                UETR,
                LocalDate.of(2026, 10, 28),
                "USD",
                new BigDecimal("12500.50"),
                "ROYCCAT2",
                "CHASUS33XXX",
                "ROYCCAT2",
                "CHASUS33XXX",
                "BUKBGB22",
                debtor(),
                creditor(),
                "INVOICE 2026-118 SAMPLE SERVICES",
                ChargeBearer.SHA);
    }

    public static WireTransfer jpyNoIntermediary() {
        return new WireTransfer(
                "RBC2410270002",
                UUID.fromString("1f0c5e0a-7d0b-4c43-9a47-2f8c0a9b0002"),
                LocalDate.of(2026, 10, 29),
                "JPY",
                new BigDecimal("1500000"),
                "ROYCCAT2",
                "BOTKJPJT",
                "ROYCCAT2",
                null,
                "BOTKJPJT",
                debtor(),
                new Party("TEST CREDITOR JP", "1234567", List.of(), "TOKYO", "JP"),
                null,
                ChargeBearer.OUR);
    }
}
