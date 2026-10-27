package com.rbc.payments.crossborder.mt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rbc.payments.crossborder.fixtures.Fixtures;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class Mt103BuilderTest {

    private final Mt103Builder builder = new Mt103Builder("TESTCATTAXXX");

    @Test
    void buildsCadToUsdMessage() {
        String expected = """
                {1:F01TESTCATTAXXX0000000000}{2:I103TESTUS33XXXXXN}{3:{121:8a562c67-ca16-48ba-b074-65581be6f001}}{4:
                :20:REF20261027A
                :23B:CRED
                :32A:261027USD12500,50
                :50K:/XX00123456789
                Test Debtor Corp
                100 Front Street West
                Toronto ON M5J 1E3
                CA
                :52A:TESTCATTXXX
                :57A:TESTUS33
                :59:/XX00987654321
                Sample Creditor LLC
                1 Fake Avenue
                New York NY 10001
                US
                :70:INVOICE 4471 OCTOBER SERVICES
                :71A:SHA
                -}""";
        assertEquals(expected, builder.build(Fixtures.cadToUsd()));
    }

    @Test
    void includesIntermediaryWhenPresent() {
        String mt = builder.build(Fixtures.withIntermediary());
        assertTrue(mt.contains(":56A:TESTDEFFXXX\n"));
        assertTrue(mt.contains(":32A:261027EUR990,00\n"));
        assertTrue(mt.contains(":71A:OUR\n"));
    }

    @Test
    void omitsOptionalFields() {
        String mt = builder.build(Fixtures.jpyZeroDecimal());
        assertTrue(!mt.contains(":56A:"));
        assertTrue(!mt.contains(":70:"));
        assertTrue(mt.contains("{2:I103TESTJPJTXXXXXN}"));
    }

    @Test
    void wrapsRemittanceAtThirtyFiveCharacters() {
        String mt = builder.build(Fixtures.unstructuredAddress());
        assertTrue(mt.contains(":70:" + "A".repeat(35) + "\n" + "A".repeat(35) + "\n"));
        assertTrue(mt.contains("1 SAMPLE ROAD\nLONDON EC1A 1AA\n"));
    }

    @Test
    void formatsAmountsUsingMinorUnits() {
        assertEquals("1500000,", Mt103Builder.formatAmount(new BigDecimal("1500000"), 0));
        assertEquals("75,25", Mt103Builder.formatAmount(new BigDecimal("75.25"), 2));
    }
}
