package com.rbc.payments.wires.domestic;

import com.rbc.payments.wires.iso20022.IsoXml;
import com.rbc.payments.wires.model.WireTransfer;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static com.rbc.payments.wires.iso20022.Pacs008Elements.NOT_PROVIDED;
import static com.rbc.payments.wires.iso20022.Pacs008Elements.PACS008_NS;
import static com.rbc.payments.wires.iso20022.Pacs008Elements.account;
import static com.rbc.payments.wires.iso20022.Pacs008Elements.agent;
import static com.rbc.payments.wires.iso20022.Pacs008Elements.amount;
import static com.rbc.payments.wires.iso20022.Pacs008Elements.party;

/** Renders a domestic CAD transfer as a Lynx (Payments Canada) pacs.008.001.08. */
public class LynxPacs008Builder {

    private final Clock clock;

    public LynxPacs008Builder(Clock clock) {
        this.clock = clock;
    }

    public String build(WireTransfer t) {
        IsoXml xml = IsoXml.create().startRoot("Document", PACS008_NS).start("FIToFICstmrCdtTrf");

        xml.start("GrpHdr")
                .leaf("MsgId", t.reference())
                .leaf("CreDtTm", OffsetDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS)
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .leaf("NbOfTxs", "1")
                .start("SttlmInf").leaf("SttlmMtd", "CLRG").start("ClrSys").leaf("Prtry", "LYNX").end().end()
                .end();

        xml.start("CdtTrfTxInf");
        xml.start("PmtId")
                .leaf("InstrId", t.reference())
                .leaf("EndToEndId", NOT_PROVIDED)
                .leaf("UETR", t.uetr().toString())
                .end();
        xml.leaf("IntrBkSttlmAmt", amount(t.amount(), t.currency()), "Ccy", t.currency());
        xml.leaf("IntrBkSttlmDt", t.valueDate().toString());
        xml.leaf("ChrgBr", "SLEV");
        agent(xml, "InstgAgt", t.senderBic());
        agent(xml, "InstdAgt", t.receiverBic());
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

        return xml.end().end().toXml();
    }
}
