package com.rbc.payments.crossborder.mt;

import com.rbc.payments.crossborder.model.Party;
import com.rbc.payments.crossborder.model.PostalAddress;
import com.rbc.payments.crossborder.model.WireTransfer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Builds SWIFT FIN MT103 single customer credit transfer messages. */
public final class Mt103Builder {

    private static final DateTimeFormatter YYMMDD = DateTimeFormatter.ofPattern("yyMMdd");
    private static final int LINE_LENGTH = 35;

    private final String senderLogicalTerminal;

    public Mt103Builder(String senderLogicalTerminal) {
        this.senderLogicalTerminal = senderLogicalTerminal;
    }

    public String build(WireTransfer wire) {
        StringBuilder sb = new StringBuilder();
        sb.append("{1:F01").append(senderLogicalTerminal).append("0000000000}");
        sb.append("{2:I103").append(padBic(wire.creditorAgentBic())).append("XN}");
        sb.append("{3:{121:").append(wire.uetr()).append("}}");
        sb.append("{4:\n");
        field(sb, "20", wire.reference());
        field(sb, "23B", "CRED");
        field(sb, "32A", wire.valueDate().format(YYMMDD) + wire.currency().getCurrencyCode()
                + formatAmount(wire.amount(), wire.currency().getDefaultFractionDigits()));
        party(sb, "50K", wire.debtor());
        field(sb, "52A", wire.debtorAgentBic());
        wire.intermediary().ifPresent(bic -> field(sb, "56A", bic));
        field(sb, "57A", wire.creditorAgentBic());
        party(sb, "59", wire.creditor());
        wire.remittance().ifPresent(text -> field(sb, "70", String.join("\n", wrap(text, 4))));
        field(sb, "71A", wire.chargeBearer().name());
        sb.append("-}");
        return sb.toString();
    }

    static String formatAmount(BigDecimal amount, int fractionDigits) {
        return amount.setScale(Math.max(fractionDigits, 0), RoundingMode.UNNECESSARY)
                .toPlainString()
                .replace('.', ',')
                + (fractionDigits == 0 ? "," : "");
    }

    private static void party(StringBuilder sb, String tag, Party party) {
        List<String> lines = new ArrayList<>();
        lines.add("/" + party.account());
        lines.addAll(wrap(party.name(), 1));
        lines.addAll(addressLines(party.address()));
        field(sb, tag, String.join("\n", lines.subList(0, Math.min(lines.size(), 5))));
    }

    private static List<String> addressLines(PostalAddress a) {
        if (!a.isStructured()) {
            return a.unstructuredLines();
        }
        List<String> lines = new ArrayList<>();
        String street = join(" ", a.buildingNumber(), a.streetName());
        if (!street.isEmpty()) {
            lines.add(truncate(street));
        }
        lines.add(truncate(join(" ", a.townName(), a.countrySubDivision(), a.postCode())));
        lines.add(a.country());
        return lines;
    }

    private static List<String> wrap(String text, int maxLines) {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < text.length() && lines.size() < maxLines; i += LINE_LENGTH) {
            lines.add(text.substring(i, Math.min(text.length(), i + LINE_LENGTH)));
        }
        return lines;
    }

    private static String join(String sep, String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p != null && !p.isBlank()) {
                if (sb.length() > 0) {
                    sb.append(sep);
                }
                sb.append(p.trim());
            }
        }
        return sb.toString();
    }

    private static String truncate(String s) {
        return s.length() <= LINE_LENGTH ? s : s.substring(0, LINE_LENGTH);
    }

    private static String padBic(String bic) {
        return bic.length() == 8 ? bic + "XXXX" : bic.substring(0, 8) + "X" + bic.substring(8);
    }

    private static void field(StringBuilder sb, String tag, String value) {
        sb.append(':').append(tag).append(':').append(value).append('\n');
    }
}
