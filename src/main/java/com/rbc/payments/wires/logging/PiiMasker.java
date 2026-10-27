package com.rbc.payments.wires.logging;

/** Masks customer identifiers before they are written to logs, metrics or exception messages. */
public final class PiiMasker {

    private PiiMasker() {
    }

    /** Keeps the last 4 characters of an account or IBAN, e.g. {@code ****6789}. */
    public static String account(String account) {
        if (account == null || account.isEmpty()) {
            return "";
        }
        int keep = Math.min(4, account.length() / 2);
        return "****" + account.substring(account.length() - keep);
    }

    /** Keeps only the first initial of a name, e.g. {@code J***}. */
    public static String name(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        return name.strip().charAt(0) + "***";
    }
}
