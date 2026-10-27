package com.rbc.payments.wires.validation;

import com.rbc.payments.wires.fixtures.TestTransfers;
import com.rbc.payments.wires.model.Party;
import com.rbc.payments.wires.model.WireTransfer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class WireTransferValidatorTest {

    private final WireTransferValidator validator = new WireTransferValidator();

    @Test
    void acceptsValidTransfers() {
        assertThatCode(() -> validator.validate(TestTransfers.usdToGb())).doesNotThrowAnyException();
        assertThatCode(() -> validator.validate(TestTransfers.jpyNoIntermediary())).doesNotThrowAnyException();
    }

    @Test
    void reportsEveryProblemWithoutLeakingPii() {
        WireTransfer t = TestTransfers.usdToGb();
        Party badParty = new Party("N".repeat(36), "", List.of("a", "b", "c".repeat(36)), "", "can");
        WireTransfer bad = new WireTransfer("/BAD//REF/", t.uetr(), t.valueDate(), "XXY",
                new BigDecimal("-1"), "bad", null, "ROYCCAT2", "nope", "BUKBGB22",
                badParty, t.beneficiary(), "é".repeat(141), t.chargeBearer());

        ValidationException e = catchThrowableOfType(() -> validator.validate(bad), ValidationException.class);

        assertThat(e.errors()).contains(
                "reference must be 1-16 chars and follow SWIFT slash rules",
                "amount must be positive",
                "currency is not a valid ISO 4217 code",
                "senderBic is not a valid BIC",
                "receiverBic is required",
                "intermediaryBic is not a valid BIC",
                "orderingCustomer.name must be 1-35 chars",
                "orderingCustomer.account must be 1-34 chars",
                "orderingCustomer.addressLines allows at most 2 lines",
                "orderingCustomer.addressLines entries must be at most 35 chars",
                "orderingCustomer.countryCode must be ISO 3166 alpha-2",
                "orderingCustomer.townName must be 1-32 chars",
                "remittanceInfo exceeds 140 chars",
                "remittanceInfo contains characters outside the SWIFT x character set");
        assertThat(e.getMessage()).doesNotContain("N".repeat(36));
    }

    @Test
    void rejectsExcessDecimalsAndOversizedAmounts() {
        WireTransfer t = TestTransfers.usdToGb();
        WireTransfer decimals = new WireTransfer(t.reference(), t.uetr(), t.valueDate(), "JPY",
                new BigDecimal("1.5"), t.senderBic(), t.receiverBic(), t.orderingInstitutionBic(),
                t.intermediaryBic(), t.beneficiaryInstitutionBic(), t.orderingCustomer(), t.beneficiary(),
                t.remittanceInfo(), t.chargeBearer());
        WireTransfer huge = new WireTransfer(t.reference(), t.uetr(), t.valueDate(), "USD",
                new BigDecimal("1000000000000"), t.senderBic(), t.receiverBic(), t.orderingInstitutionBic(),
                t.intermediaryBic(), t.beneficiaryInstitutionBic(), t.orderingCustomer(), t.beneficiary(),
                t.remittanceInfo(), t.chargeBearer());

        assertThat(catchThrowableOfType(() -> validator.validate(decimals), ValidationException.class).errors())
                .containsExactly("amount has more decimals than the currency allows");
        assertThat(catchThrowableOfType(() -> validator.validate(huge), ValidationException.class).errors())
                .containsExactly("amount exceeds 15 digit SWIFT limit");
    }

    @Test
    void partyToStringIsMasked() {
        assertThat(TestTransfers.debtor().toString()).doesNotContain("TEST DEBTOR").doesNotContain("00012345678");
    }
}
