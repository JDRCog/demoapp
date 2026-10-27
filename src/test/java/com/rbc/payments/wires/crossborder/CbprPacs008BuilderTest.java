package com.rbc.payments.wires.crossborder;

import com.rbc.payments.wires.fixtures.TestTransfers;
import com.rbc.payments.wires.iso20022.XmlAssert;
import com.rbc.payments.wires.model.WireTransfer;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

/** Field-by-field parity with docs/tickets/PAY-4821-field-mapping.csv (row numbers in comments). */
class CbprPacs008BuilderTest {

    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-27T14:30:00Z"), ZoneOffset.UTC);
    private static final String HDR = "/Envelope/AppHdr";
    private static final String GRP = "/Envelope/Document/FIToFICstmrCdtTrf/GrpHdr";
    private static final String TX = "/Envelope/Document/FIToFICstmrCdtTrf/CdtTrfTxInf";

    private final CbprPacs008Builder builder = new CbprPacs008Builder(CLOCK);

    @Test
    void mapsEverySpreadsheetRow() {
        WireTransfer t = TestTransfers.usdToGb();
        XmlAssert x = XmlAssert.parse(builder.build(t));

        assertThat(x.text(HDR + "/Fr/FIId/FinInstnId/BICFI")).isEqualTo("ROYCCAT2");              // 1
        assertThat(x.text(TX + "/InstgAgt/FinInstnId/BICFI")).isEqualTo("ROYCCAT2");               // 1
        assertThat(x.text(HDR + "/To/FIId/FinInstnId/BICFI")).isEqualTo("CHASUS33XXX");           // 2
        assertThat(x.text(TX + "/InstdAgt/FinInstnId/BICFI")).isEqualTo("CHASUS33XXX");            // 2
        assertThat(x.text(TX + "/PmtId/UETR")).isEqualTo(t.uetr().toString());                    // 3
        assertThat(x.text(HDR + "/BizMsgIdr")).isEqualTo(t.reference());                           // 4
        assertThat(x.text(GRP + "/MsgId")).isEqualTo(t.reference());                               // 4
        assertThat(x.text(TX + "/PmtId/InstrId")).isEqualTo(t.reference());                        // 4
        assertThat(x.text(TX + "/PmtId/EndToEndId")).isEqualTo("NOTPROVIDED");                    // 5
        assertThat(x.text(TX + "/IntrBkSttlmDt")).isEqualTo("2026-10-28");                         // 7
        assertThat(x.text(TX + "/IntrBkSttlmAmt")).isEqualTo("12500.50");                          // 8
        assertThat(x.text(TX + "/IntrBkSttlmAmt/@Ccy")).isEqualTo("USD");                          // 8
        assertThat(x.text(TX + "/InstdAmt")).isEqualTo("12500.50");                                // 9
        assertThat(x.text(TX + "/InstdAmt/@Ccy")).isEqualTo("USD");                                // 9
        assertThat(x.text(TX + "/DbtrAcct/Id/Othr/Id")).isEqualTo("00012345678");                  // 10
        assertThat(x.text(TX + "/Dbtr/Nm")).isEqualTo("TEST DEBTOR ONE");                          // 11
        assertThat(x.texts(TX + "/Dbtr/PstlAdr/AdrLine")).containsExactly("100 TEST STREET", "SUITE 200"); // 12
        assertThat(x.text(TX + "/Dbtr/PstlAdr/TwnNm")).isEqualTo("TORONTO");                       // 13
        assertThat(x.text(TX + "/Dbtr/PstlAdr/Ctry")).isEqualTo("CA");                             // 13
        assertThat(x.text(TX + "/DbtrAgt/FinInstnId/BICFI")).isEqualTo("ROYCCAT2");                // 14
        assertThat(x.text(TX + "/IntrmyAgt1/FinInstnId/BICFI")).isEqualTo("CHASUS33XXX");          // 15
        assertThat(x.text(TX + "/CdtrAgt/FinInstnId/BICFI")).isEqualTo("BUKBGB22");                // 16
        assertThat(x.text(TX + "/CdtrAcct/Id/IBAN")).isEqualTo("GB33BUKB20201555555555");          // 17
        assertThat(x.text(TX + "/Cdtr/Nm")).isEqualTo("TEST CREDITOR TWO");                        // 18
        assertThat(x.texts(TX + "/Cdtr/PstlAdr/AdrLine")).containsExactly("1 SAMPLE ROAD");        // 19
        assertThat(x.text(TX + "/Cdtr/PstlAdr/TwnNm")).isEqualTo("LONDON");                        // 20
        assertThat(x.text(TX + "/Cdtr/PstlAdr/Ctry")).isEqualTo("GB");                             // 20
        assertThat(x.text(TX + "/RmtInf/Ustrd")).isEqualTo(t.remittanceInfo());                    // 21
        assertThat(x.text(TX + "/ChrgBr")).isEqualTo("SHAR");                                      // 23
        assertThat(x.text(GRP + "/SttlmInf/SttlmMtd")).isEqualTo("INDA");                          // 24
        assertThat(x.text(HDR + "/MsgDefIdr")).isEqualTo("pacs.008.001.08");                       // 25
        assertThat(x.text(HDR + "/BizSvc")).isEqualTo("swift.cbprplus.02");                        // 25
        assertThat(x.text(HDR + "/CreDt")).isEqualTo("2026-10-27T14:30:00Z");                      // 26
        assertThat(x.text(GRP + "/CreDtTm")).isEqualTo("2026-10-27T14:30:00Z");                    // 26
    }

    @Test
    void omitsOptionalElementsAndMapsOurToDebt() {
        XmlAssert x = XmlAssert.parse(builder.build(TestTransfers.jpyNoIntermediary()));

        assertThat(x.text(TX + "/IntrmyAgt1/FinInstnId/BICFI")).isNull();                          // 15
        assertThat(x.text(TX + "/RmtInf/Ustrd")).isNull();                                         // 21
        assertThat(x.text(TX + "/ChrgBr")).isEqualTo("DEBT");                                      // 22
        assertThat(x.text(TX + "/IntrBkSttlmAmt")).isEqualTo("1500000");                           // 8
    }
}
