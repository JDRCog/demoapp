package com.rbc.payments.wires.iso20022;

import com.rbc.payments.wires.model.Party;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.regex.Pattern;

/** Reusable pacs.008 building blocks shared by every ISO 20022 flow in this service. */
public final class Pacs008Elements {

    public static final String PACS008_NS = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08";
    public static final String NOT_PROVIDED = "NOTPROVIDED";

    private static final Pattern IBAN = Pattern.compile("[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}");

    private Pacs008Elements() {
    }

    /** ISO 20022 decimal amount: dot separator, scale from ISO 4217 minor units. */
    public static String amount(BigDecimal amount, String currency) {
        int digits = Math.max(Currency.getInstance(currency).getDefaultFractionDigits(), 0);
        return amount.setScale(digits, RoundingMode.UNNECESSARY).toPlainString();
    }

    public static void agent(IsoXml xml, String element, String bic) {
        xml.start(element).start("FinInstnId").leaf("BICFI", bic).end().end();
    }

    /** Writes Dbtr/Cdtr with a hybrid postal address (TwnNm + Ctry + AdrLine). */
    public static void party(IsoXml xml, String element, Party party) {
        xml.start(element).leaf("Nm", party.name()).start("PstlAdr");
        xml.leaf("TwnNm", party.townName()).leaf("Ctry", party.countryCode());
        party.addressLines().forEach(line -> xml.leaf("AdrLine", line));
        xml.end().end();
    }

    /** Writes DbtrAcct/CdtrAcct, using IBAN when the identifier is IBAN-shaped. */
    public static void account(IsoXml xml, String element, String account) {
        xml.start(element).start("Id");
        if (IBAN.matcher(account).matches()) {
            xml.leaf("IBAN", account);
        } else {
            xml.start("Othr").leaf("Id", account).end();
        }
        xml.end().end();
    }
}
