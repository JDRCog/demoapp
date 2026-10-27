package com.rbc.payments.wires.logging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PiiMaskerTest {

    @Test
    void masksAccounts() {
        assertThat(PiiMasker.account("GB33BUKB20201555555555")).isEqualTo("****5555");
        assertThat(PiiMasker.account("123")).isEqualTo("****3");
        assertThat(PiiMasker.account("")).isEmpty();
        assertThat(PiiMasker.account(null)).isEmpty();
    }

    @Test
    void masksNames() {
        assertThat(PiiMasker.name(" Test Debtor")).isEqualTo("T***");
        assertThat(PiiMasker.name(" ")).isEmpty();
        assertThat(PiiMasker.name(null)).isEmpty();
    }
}
