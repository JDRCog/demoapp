package com.rbc.payments.wires.swift;

import com.rbc.payments.wires.fixtures.TestTransfers;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Mt103BuilderTest {

    private final Mt103Builder builder = new Mt103Builder();

    @Test
    void rendersFullMt103() {
        String mt = builder.build(TestTransfers.usdToGb());

        assertThat(mt).isEqualTo("""
                {1:F01ROYCCAT2AXXX0000000000}{2:I103CHASUS33AXXXN}{3:{121:8a562c67-ca16-48ba-b074-65581be6f001}}{4:
                :20:RBC2410270001
                :23B:CRED
                :32A:261028USD12500,50
                :33B:USD12500,50
                :50K:/00012345678
                TEST DEBTOR ONE
                100 TEST STREET
                SUITE 200
                CA/TORONTO
                :52A:ROYCCAT2
                :56A:CHASUS33XXX
                :57A:BUKBGB22
                :59:/GB33BUKB20201555555555
                TEST CREDITOR TWO
                1 SAMPLE ROAD
                GB/LONDON
                :70:INVOICE 2026-118 SAMPLE SERVICES
                :71A:SHA
                -}""");
    }

    @Test
    void omitsOptionalFields() {
        String mt = builder.build(TestTransfers.jpyNoIntermediary());

        assertThat(mt).doesNotContain(":56A:").doesNotContain(":70:");
        assertThat(mt).contains(":32A:261029JPY1500000,\n").contains(":71A:OUR\n");
    }

    @Test
    void formatsAmountsUsingCurrencyMinorUnits() {
        assertThat(Mt103Builder.formatAmount(new BigDecimal("10"), "USD")).isEqualTo("10,00");
        assertThat(Mt103Builder.formatAmount(new BigDecimal("10"), "JPY")).isEqualTo("10,");
        assertThat(Mt103Builder.formatAmount(new BigDecimal("1.234"), "BHD")).isEqualTo("1,234");
        assertThatThrownBy(() -> Mt103Builder.formatAmount(new BigDecimal("1.001"), "USD"))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    void splitsLongTextIntoAtMostFourLines() {
        String text = "x".repeat(200);
        assertThat(Mt103Builder.split(text, 35, 4)).hasSize(4).allMatch(l -> l.length() == 35);
    }
}
