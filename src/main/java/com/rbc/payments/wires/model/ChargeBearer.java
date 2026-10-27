package com.rbc.payments.wires.model;

/** Who pays transaction charges (MT103 field 71A). */
public enum ChargeBearer {
    /** All charges borne by the ordering customer. */
    OUR,
    /** Charges shared between ordering customer and beneficiary. */
    SHA
}
