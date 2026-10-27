package com.rbc.payments.wires.iso20022;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Pacs008ElementsTest {

    @Test
    void formatsIsoAmounts() {
        assertThat(Pacs008Elements.amount(new BigDecimal("12500.5"), "USD")).isEqualTo("12500.50");
        assertThat(Pacs008Elements.amount(new BigDecimal("1500000"), "JPY")).isEqualTo("1500000");
        assertThatThrownBy(() -> Pacs008Elements.amount(new BigDecimal("0.001"), "CAD"))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    void usesIbanOnlyForIbanShapedAccounts() {
        IsoXml xml = IsoXml.create().startRoot("Document", Pacs008Elements.PACS008_NS);
        Pacs008Elements.account(xml, "A", "GB33BUKB20201555555555");
        Pacs008Elements.account(xml, "B", "00012345678");
        XmlAssert doc = XmlAssert.parse(xml.end().toXml());

        assertThat(doc.text("/Document/A/Id/IBAN")).isEqualTo("GB33BUKB20201555555555");
        assertThat(doc.text("/Document/B/Id/Othr/Id")).isEqualTo("00012345678");
        assertThat(doc.text("/Document/B/Id/IBAN")).isNull();
    }

    @Test
    void escapesMarkupInText() {
        String xml = IsoXml.create().startRoot("Document", Pacs008Elements.PACS008_NS)
                .leaf("Ustrd", "A&B <C>").leafIfPresent("Skip", " ").end().toXml();

        assertThat(xml).contains("A&amp;B &lt;C&gt;").doesNotContain("Skip");
        assertThat(XmlAssert.parse(xml).text("/Document/Ustrd")).isEqualTo("A&B <C>");
    }
}
