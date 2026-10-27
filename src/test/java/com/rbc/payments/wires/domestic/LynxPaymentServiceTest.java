package com.rbc.payments.wires.domestic;

import com.rbc.payments.wires.config.FeatureFlags;
import com.rbc.payments.wires.fixtures.TestTransfers;
import com.rbc.payments.wires.iso20022.XmlAssert;
import com.rbc.payments.wires.messaging.InMemoryMessagePublisher;
import com.rbc.payments.wires.messaging.KafkaTopics;
import com.rbc.payments.wires.messaging.MessageFormat;
import com.rbc.payments.wires.messaging.SendResult;
import com.rbc.payments.wires.messaging.TopicGuardPublisher;
import com.rbc.payments.wires.model.WireTransfer;
import com.rbc.payments.wires.swift.Mt103Builder;
import com.rbc.payments.wires.validation.ValidationException;
import com.rbc.payments.wires.validation.WireTransferValidator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LynxPaymentServiceTest {

    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-27T14:30:00Z"), ZoneOffset.UTC);

    private final InMemoryMessagePublisher sink = new InMemoryMessagePublisher();

    static WireTransfer cad() {
        WireTransfer t = TestTransfers.usdToGb();
        return new WireTransfer(t.reference(), t.uetr(), t.valueDate(), "CAD", new BigDecimal("250000.00"),
                "ROYCCAT2", "TDOMCATT", "ROYCCAT2", null, "TDOMCATT", t.orderingCustomer(),
                TestTransfers.debtor(), t.remittanceInfo(), t.chargeBearer());
    }

    private LynxPaymentService service(boolean iso) {
        Properties sys = new Properties();
        sys.setProperty(LynxPaymentService.ISO20022_FLAG, Boolean.toString(iso));
        return new LynxPaymentService(new WireTransferValidator(), new Mt103Builder(),
                new LynxPacs008Builder(CLOCK), new TopicGuardPublisher(sink),
                new FeatureFlags(new Properties(), sys, Map.of()));
    }

    @Test
    void flagOffSendsMt103() {
        SendResult r = service(false).send(cad());

        assertThat(r.format()).isEqualTo(MessageFormat.MT103);
        assertThat(r.topic()).isEqualTo(KafkaTopics.LYNX_MT103_OUTBOUND);
        assertThat(r.payload()).isEqualTo(new Mt103Builder().build(cad()));
    }

    @Test
    void flagOnSendsPacs008() {
        SendResult r = service(true).send(cad());

        assertThat(r.format()).isEqualTo(MessageFormat.PACS008);
        assertThat(r.topic()).isEqualTo(KafkaTopics.LYNX_PACS008_OUTBOUND);
        assertThat(sink.messages()).singleElement().extracting(InMemoryMessagePublisher.Published::key)
                .isEqualTo(TestTransfers.UETR.toString());
    }

    @Test
    void rejectsNonCad() {
        assertThatThrownBy(() -> service(true).send(TestTransfers.usdToGb())).isInstanceOf(ValidationException.class);
        assertThat(sink.messages()).isEmpty();
    }

    /** Parity: every MT103 field used for Lynx maps to the expected pacs.008 element. */
    @Test
    void pacs008MatchesMt103FieldByField() {
        WireTransfer t = cad();
        XmlAssert x = XmlAssert.parse(new LynxPacs008Builder(CLOCK).build(t));
        String tx = "/Document/FIToFICstmrCdtTrf/CdtTrfTxInf";

        assertThat(x.namespace()).isEqualTo("urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08");
        assertThat(x.text("/Document/FIToFICstmrCdtTrf/GrpHdr/MsgId")).isEqualTo(t.reference());
        assertThat(x.text("/Document/FIToFICstmrCdtTrf/GrpHdr/CreDtTm")).isEqualTo("2026-10-27T14:30:00Z");
        assertThat(x.text("/Document/FIToFICstmrCdtTrf/GrpHdr/SttlmInf/ClrSys/Prtry")).isEqualTo("LYNX");
        assertThat(x.text(tx + "/PmtId/InstrId")).isEqualTo(t.reference());
        assertThat(x.text(tx + "/PmtId/UETR")).isEqualTo(t.uetr().toString());
        assertThat(x.text(tx + "/IntrBkSttlmAmt")).isEqualTo("250000.00");
        assertThat(x.text(tx + "/IntrBkSttlmAmt/@Ccy")).isEqualTo("CAD");
        assertThat(x.text(tx + "/IntrBkSttlmDt")).isEqualTo("2026-10-28");
        assertThat(x.text(tx + "/InstgAgt/FinInstnId/BICFI")).isEqualTo("ROYCCAT2");
        assertThat(x.text(tx + "/InstdAgt/FinInstnId/BICFI")).isEqualTo("TDOMCATT");
        assertThat(x.text(tx + "/Dbtr/Nm")).isEqualTo(t.orderingCustomer().name());
        assertThat(x.texts(tx + "/Dbtr/PstlAdr/AdrLine")).isEqualTo(t.orderingCustomer().addressLines());
        assertThat(x.text(tx + "/DbtrAcct/Id/Othr/Id")).isEqualTo(t.orderingCustomer().account());
        assertThat(x.text(tx + "/DbtrAgt/FinInstnId/BICFI")).isEqualTo("ROYCCAT2");
        assertThat(x.text(tx + "/CdtrAgt/FinInstnId/BICFI")).isEqualTo("TDOMCATT");
        assertThat(x.text(tx + "/Cdtr/Nm")).isEqualTo(t.beneficiary().name());
        assertThat(x.text(tx + "/Cdtr/PstlAdr/TwnNm")).isEqualTo("TORONTO");
        assertThat(x.text(tx + "/RmtInf/Ustrd")).isEqualTo(t.remittanceInfo());
    }
}
