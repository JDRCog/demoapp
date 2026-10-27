package com.rbc.payments.crossborder.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rbc.payments.crossborder.config.FeatureFlags;
import com.rbc.payments.crossborder.fixtures.Fixtures;
import com.rbc.payments.crossborder.model.WireTransfer;
import com.rbc.payments.crossborder.mt.Mt103Builder;
import com.rbc.payments.crossborder.publish.InMemoryPublisher;
import java.math.BigDecimal;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class WireTransferServiceTest {

    private final InMemoryPublisher publisher = new InMemoryPublisher();

    private WireTransferService service() {
        Properties p = new Properties();
        p.setProperty("crossborder.topic.mt103", "payments.crossborder.mt103.outbound.v1");
        return new WireTransferService(new FeatureFlags(p), new Mt103Builder("TESTCATTAXXX"), publisher);
    }

    @Test
    void publishesMt103KeyedByUetr() {
        service().send(Fixtures.cadToUsd());
        assertEquals(1, publisher.messages().size());
        InMemoryPublisher.Published m = publisher.messages().get(0);
        assertEquals("payments.crossborder.mt103.outbound.v1", m.topic());
        assertEquals("8a562c67-ca16-48ba-b074-65581be6f001", m.key());
        assertTrue(m.payload().startsWith("{1:F01"));
    }

    @Test
    void rejectsNonPositiveAmount() {
        WireTransfer w = Fixtures.cadToUsd();
        WireTransfer zero = new WireTransfer(w.reference(), w.uetr(), w.valueDate(), BigDecimal.ZERO,
                w.currency(), w.debtor(), w.debtorAgentBic(), w.creditor(), w.creditorAgentBic(),
                null, null, w.chargeBearer());
        assertThrows(WireValidationException.class, () -> service().send(zero));
        assertTrue(publisher.messages().isEmpty());
    }

    @Test
    void rejectsInvalidBicWithoutLeakingPii() {
        WireTransfer w = Fixtures.cadToUsd();
        WireTransfer bad = new WireTransfer(w.reference(), w.uetr(), w.valueDate(), w.amount(),
                w.currency(), w.debtor(), "bad-bic", w.creditor(), w.creditorAgentBic(),
                null, null, w.chargeBearer());
        WireValidationException ex = assertThrows(WireValidationException.class, () -> service().send(bad));
        assertFalse(ex.getMessage().contains(w.debtor().account()));
        assertTrue(publisher.messages().isEmpty());
    }
}
