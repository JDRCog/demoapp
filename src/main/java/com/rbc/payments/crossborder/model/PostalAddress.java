package com.rbc.payments.crossborder.model;

import java.util.List;
import java.util.Objects;

/**
 * Party postal address. Structured fields are preferred; {@code unstructuredLines} is a fallback
 * for legacy records that only carry free-text address lines.
 */
public record PostalAddress(
        String streetName,
        String buildingNumber,
        String postCode,
        String townName,
        String countrySubDivision,
        String country,
        List<String> unstructuredLines) {

    public PostalAddress {
        Objects.requireNonNull(country, "country");
        unstructuredLines = unstructuredLines == null ? List.of() : List.copyOf(unstructuredLines);
    }

    public boolean isStructured() {
        return townName != null && !townName.isBlank();
    }
}
