package com.rbc.payments.crossborder.logging;

/** Masks customer PII before it is written to logs, metrics or exception messages. */
public final class PiiMasker {

    private PiiMasker() {}

    /** Keeps the last four characters of an account or IBAN, e.g. {@code ****1234}. */
    public static String maskAccount(String account) {
        if (account == null || account.isEmpty()) {
            return "****";
        }
        int visible = Math.min(4, account.length() / 2);
        return "****" + account.substring(account.length() - visible);
    }

    /** Keeps only the first character of a name, e.g. {@code J***}. */
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return "***";
        }
        return name.charAt(0) + "***";
    }
}
