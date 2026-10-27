package com.rbc.payments.wires.crossborder;

import com.rbc.payments.wires.iso20022.IsoXml;
import com.rbc.payments.wires.model.ChargeBearer;
import com.rbc.payments.wires.model.WireTransfer;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static com.rbc.payments.wires.iso20022.Pacs008Elements.NOT_PROVIDED;
import static com.rbc.payments.wires.iso20022.Pacs008Elements.PACS008_NS;
import static com.rbc.payments.wires.iso20022.Pacs008Elements.account;
import static com.rbc.payments.wires.iso20022.Pacs008Elements.agent;
import static com.rbc.payments.wires.iso20022.Pacs008Elements.amount;
import static com.rbc.payments.wires.iso20022.Pacs008Elements.party;

/** Renders a cross-border transfer as CBPR+ head.001.001.02 + pacs.008.001.08 (PAY-4821 mapping). */
public class CbprPacs008Builder {

    static final String HEAD_NS = "urn:iso:std:iso:20022:tech:xsd:head.001.001.02";
    static final String BIZ_SVC = "swift.cbprplus.02";
    static final String MSG_DEF = "pacs.008.001.08";

    private final Clock clock;

    public CbprPacs008Builder(Clock clock) {
        this.clock = clock;
    }

    public String build(WireTransfer t) {
        String created = OffsetDateTime.now(clock).withOffsetSameInstant(ZoneOffset.UTC)
                .truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String amount = amount(t.amount(), t.currency());

        IsoXml xml = IsoXml.create().start("Envelope");

        xml.startRoot("AppHdr", HEAD_NS);
        xml.start("Fr").start("FIId").start("FinInstnId").leaf("BICFI", t.senderBic()).end().end().end();
        xml.start("To").start("FIId").start("FinInstnId").leaf("BICFI", t.receiverBic()).end().end().end();
        xml.leaf("BizMsgIdr", t.reference())
                .leaf("MsgDefIdr", MSG_DEF)
                .leaf("BizSvc", BIZ_SVC)
                .leaf("CreDt", created)
                .end();

        xml.startRoot("Document", PACS008_NS).start("FIToFICstmrCdtTrf");
        xml.start("GrpHdr")
                .leaf("MsgId", t.reference())
                .leaf("CreDtTm", created)
                .leaf("NbOfTxs", "1")
                .start("SttlmInf").leaf("SttlmMtd", "INDA").end()
                .end();

        xml.start("CdtTrfTxInf");
        xml.start("PmtId")
                .leaf("InstrId", t.reference())
                .leaf("EndToEndId", NOT_PROVIDED)
                .leaf("UETR", t.uetr().toString())
                .end();
        xml.leaf("IntrBkSttlmAmt", amount, "Ccy", t.currency());
        xml.leaf("IntrBkSttlmDt", t.valueDate().toString());
        xml.leaf("InstdAmt", amount, "Ccy", t.currency());
        xml.leaf("ChrgBr", chargeBearer(t.chargeBearer()));
        agent(xml, "InstgAgt", t.senderBic());
        agent(xml, "InstdAgt", t.receiverBic());
        if (t.intermediaryBic() != null) {
            agent(xml, "IntrmyAgt1", t.intermediaryBic());
        }
        party(xml, "Dbtr", t.orderingCustomer());
        account(xml, "DbtrAcct", t.orderingCustomer().account());
        agent(xml, "DbtrAgt", t.orderingInstitutionBic());
        agent(xml, "CdtrAgt", t.beneficiaryInstitutionBic());
        party(xml, "Cdtr", t.beneficiary());
        account(xml, "CdtrAcct", t.beneficiary().account());
        if (t.remittanceInfo() != null && !t.remittanceInfo().isBlank()) {
            xml.start("RmtInf").leaf("Ustrd", t.remittanceInfo()).end();
        }
        xml.end();

        return xml.end().end().end().toXml();
    }

    static String chargeBearer(ChargeBearer bearer) {
        return switch (bearer) {
            case OUR -> "DEBT";
            case SHA -> "SHAR";
        };
    }
}
