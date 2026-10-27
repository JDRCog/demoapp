package com.rbc.payments.crossborder.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PiiMaskerTest {

    @Test
    void masksAccounts() {
        assertEquals("****6789", PiiMasker.maskAccount("XX00123456789"));
        assertEquals("****", PiiMasker.maskAccount(null));
        assertEquals("****2", PiiMasker.maskAccount("12"));
    }

    @Test
    void masksNames() {
        assertEquals("T***", PiiMasker.maskName("Test Debtor Corp"));
        assertEquals("***", PiiMasker.maskName(""));
    }
}
