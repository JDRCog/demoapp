package com.rbc.payments.wires.swift;

import com.rbc.payments.wires.model.Party;
import com.rbc.payments.wires.model.WireTransfer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

/** Renders a {@link WireTransfer} as a SWIFT FIN MT103 single customer credit transfer. */
public class Mt103Builder {

    static final int LINE_LENGTH = 35;
    private static final DateTimeFormatter YYMMDD = DateTimeFormatter.ofPattern("yyMMdd");

    public String build(WireTransfer t) {
        StringBuilder sb = new StringBuilder();
        sb.append("{1:F01").append(lt(t.senderBic())).append("0000000000}");
        sb.append("{2:I103").append(lt(t.receiverBic())).append("N}");
        sb.append("{3:{121:").append(t.uetr()).append("}}");
        sb.append("{4:\n");
        field(sb, "20", t.reference());
        field(sb, "23B", "CRED");
        String amount = formatAmount(t.amount(), t.currency());
        field(sb, "32A", t.valueDate().format(YYMMDD) + t.currency() + amount);
        field(sb, "33B", t.currency() + amount);
        field(sb, "50K", partyLines(t.orderingCustomer()));
        field(sb, "52A", t.orderingInstitutionBic());
        if (t.intermediaryBic() != null) {
            field(sb, "56A", t.intermediaryBic());
        }
        field(sb, "57A", t.beneficiaryInstitutionBic());
        field(sb, "59", partyLines(t.beneficiary()));
        if (t.remittanceInfo() != null && !t.remittanceInfo().isBlank()) {
            field(sb, "70", String.join("\n", split(t.remittanceInfo(), LINE_LENGTH, 4)));
        }
        field(sb, "71A", t.chargeBearer().name());
        sb.append("-}");
        return sb.toString();
    }

    /** SWIFT amount format: comma decimal separator, scale from ISO 4217 minor units. */
    static String formatAmount(BigDecimal amount, String currency) {
        int digits = Math.max(Currency.getInstance(currency).getDefaultFractionDigits(), 0);
        String plain = amount.setScale(digits, RoundingMode.UNNECESSARY).toPlainString();
        return digits == 0 ? plain + "," : plain.replace('.', ',');
    }

    private static String partyLines(Party p) {
        List<String> lines = new ArrayList<>();
        lines.add("/" + p.account());
        lines.add(p.name());
        lines.addAll(p.addressLines());
        lines.add(p.countryCode() + "/" + p.townName());
        return String.join("\n", lines);
    }

    private static void field(StringBuilder sb, String tag, String value) {
        sb.append(':').append(tag).append(':').append(value).append('\n');
    }

    /** Logical terminal address: 8-char BIC + terminal code 'A' + 3-char branch. */
    private static String lt(String bic) {
        String bic11 = bic.length() == 8 ? bic + "XXX" : bic;
        return bic11.substring(0, 8) + "A" + bic11.substring(8);
    }

    static List<String> split(String text, int width, int maxLines) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < text.length() && out.size() < maxLines; i += width) {
            out.add(text.substring(i, Math.min(text.length(), i + width)));
        }
        return out;
    }
}
