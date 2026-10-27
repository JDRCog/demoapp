package com.rbc.payments.wires.validation;

import com.rbc.payments.wires.model.Party;
import com.rbc.payments.wires.model.WireTransfer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.regex.Pattern;

/** Validates a transfer against the SWIFT MT103 field constraints the service relies on. */
public class WireTransferValidator {

    private static final Pattern BIC = Pattern.compile("[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?");
    private static final Pattern SWIFT_X = Pattern.compile("[A-Za-z0-9/\\-?:().,'+ ]*");
    private static final Pattern COUNTRY = Pattern.compile("[A-Z]{2}");
    private static final int MAX_LINE = 35;

    public void validate(WireTransfer t) {
        List<String> errors = new ArrayList<>();
        if (t.reference().isEmpty() || t.reference().length() > 16 || t.reference().startsWith("/")
                || t.reference().endsWith("/") || t.reference().contains("//")) {
            errors.add("reference must be 1-16 chars and follow SWIFT slash rules");
        }
        if (t.amount().signum() <= 0) {
            errors.add("amount must be positive");
        }
        try {
            Currency c = Currency.getInstance(t.currency());
            if (t.amount().stripTrailingZeros().scale() > Math.max(c.getDefaultFractionDigits(), 0)) {
                errors.add("amount has more decimals than the currency allows");
            }
        } catch (IllegalArgumentException e) {
            errors.add("currency is not a valid ISO 4217 code");
        }
        if (t.amount().compareTo(new BigDecimal("999999999999.99")) > 0) {
            errors.add("amount exceeds 15 digit SWIFT limit");
        }
        bic(errors, "senderBic", t.senderBic(), true);
        bic(errors, "receiverBic", t.receiverBic(), true);
        bic(errors, "orderingInstitutionBic", t.orderingInstitutionBic(), true);
        bic(errors, "intermediaryBic", t.intermediaryBic(), false);
        bic(errors, "beneficiaryInstitutionBic", t.beneficiaryInstitutionBic(), true);
        party(errors, "orderingCustomer", t.orderingCustomer());
        party(errors, "beneficiary", t.beneficiary());
        if (t.remittanceInfo() != null) {
            if (t.remittanceInfo().length() > 4 * MAX_LINE) {
                errors.add("remittanceInfo exceeds 140 chars");
            }
            if (!SWIFT_X.matcher(t.remittanceInfo()).matches()) {
                errors.add("remittanceInfo contains characters outside the SWIFT x character set");
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private static void bic(List<String> errors, String field, String value, boolean required) {
        if (value == null) {
            if (required) {
                errors.add(field + " is required");
            }
            return;
        }
        if (!BIC.matcher(value).matches()) {
            errors.add(field + " is not a valid BIC");
        }
    }

    private static void party(List<String> errors, String field, Party p) {
        if (p.name().isBlank() || p.name().length() > MAX_LINE) {
            errors.add(field + ".name must be 1-35 chars");
        }
        if (p.account().isBlank() || p.account().length() > 34) {
            errors.add(field + ".account must be 1-34 chars");
        }
        if (p.addressLines().size() > 2) {
            errors.add(field + ".addressLines allows at most 2 lines");
        }
        for (String line : p.addressLines()) {
            if (line.length() > MAX_LINE) {
                errors.add(field + ".addressLines entries must be at most 35 chars");
            }
        }
        if (p.countryCode() == null || !COUNTRY.matcher(p.countryCode()).matches()) {
            errors.add(field + ".countryCode must be ISO 3166 alpha-2");
        }
        if (p.townName() == null || p.townName().isBlank() || p.townName().length() > MAX_LINE - 3) {
            errors.add(field + ".townName must be 1-32 chars");
        }
        String all = p.name() + p.account() + String.join("", p.addressLines()) + p.townName();
        if (!SWIFT_X.matcher(all).matches()) {
            errors.add(field + " contains characters outside the SWIFT x character set");
        }
    }
}
