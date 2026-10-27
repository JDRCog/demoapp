package com.rbc.payments.crossborder.fixtures;

import com.rbc.payments.crossborder.model.ChargeBearer;
import com.rbc.payments.crossborder.model.Party;
import com.rbc.payments.crossborder.model.PostalAddress;
import com.rbc.payments.crossborder.model.WireTransfer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

/** Synthetic test data only. No real customers, accounts or production BICs. */
public final class Fixtures {

    private Fixtures() {}

    public static PostalAddress torontoAddress() {
        return new PostalAddress("Front Street West", "100", "M5J 1E3", "Toronto", "ON", "CA", List.of());
    }

    public static PostalAddress newYorkAddress() {
        return new PostalAddress("Fake Avenue", "1", "10001", "New York", "NY", "US", List.of());
    }

    public static Party testDebtor() {
        return new Party("Test Debtor Corp", "XX00123456789", torontoAddress());
    }

    public static Party testCreditor() {
        return new Party("Sample Creditor LLC", "XX00987654321", newYorkAddress());
    }

    public static WireTransfer cadToUsd() {
        return new WireTransfer(
                "REF20261027A",
                "8a562c67-ca16-48ba-b074-65581be6f001",
                LocalDate.of(2026, 10, 27),
                new BigDecimal("12500.50"),
                Currency.getInstance("USD"),
                testDebtor(),
                "TESTCATTXXX",
                testCreditor(),
                "TESTUS33",
                null,
                "INVOICE 4471 OCTOBER SERVICES",
                ChargeBearer.SHA);
    }

    public static WireTransfer withIntermediary() {
        WireTransfer w = cadToUsd();
        return new WireTransfer(w.reference(), "0f4e2b9c-7d1a-4c3e-9b8f-2a6d5e4c3b21", w.valueDate(),
                new BigDecimal("990.00"), Currency.getInstance("EUR"), w.debtor(), w.debtorAgentBic(),
                new Party("Exemple Beneficiaire SARL", "XX7630006000011234567890189",
                        new PostalAddress("Rue Fictive", "12", "75001", "Paris", null, "FR", List.of())),
                "TESTFRPPXXX", "TESTDEFFXXX", null, ChargeBearer.OUR);
    }

    public static WireTransfer jpyZeroDecimal() {
        WireTransfer w = cadToUsd();
        return new WireTransfer("REFJPY0001", "3c9d1e2f-4a5b-4c6d-8e7f-901a2b3c4d5e", w.valueDate(),
                new BigDecimal("1500000"), Currency.getInstance("JPY"), w.debtor(), w.debtorAgentBic(),
                new Party("Sample Kabushiki Kaisha", "XX0012341234",
                        new PostalAddress(null, null, "100-0001", "Tokyo", null, "JP", List.of())),
                "TESTJPJT", null, null, ChargeBearer.BEN);
    }

    public static WireTransfer unstructuredAddress() {
        WireTransfer w = cadToUsd();
        return new WireTransfer("REFLEGACY01", "b1c2d3e4-f5a6-4b7c-9d8e-0f1a2b3c4d5e", w.valueDate(),
                new BigDecimal("75.25"), Currency.getInstance("GBP"), w.debtor(), w.debtorAgentBic(),
                new Party("Legacy Record Ltd", "XX0055556666",
                        new PostalAddress(null, null, null, null, null, "GB",
                                List.of("1 SAMPLE ROAD", "LONDON EC1A 1AA"))),
                "TESTGB2L", null, "A".repeat(140), ChargeBearer.SHA);
    }

    public static List<WireTransfer> all() {
        return List.of(cadToUsd(), withIntermediary(), jpyZeroDecimal(), unstructuredAddress());
    }
}
