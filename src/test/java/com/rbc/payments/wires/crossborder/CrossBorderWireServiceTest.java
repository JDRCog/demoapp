package com.rbc.payments.wires.crossborder;

import com.rbc.payments.wires.fixtures.TestTransfers;
import com.rbc.payments.wires.messaging.InMemoryMessagePublisher;
import com.rbc.payments.wires.messaging.KafkaTopics;
import com.rbc.payments.wires.messaging.MessageFormat;
import com.rbc.payments.wires.messaging.SendResult;
import com.rbc.payments.wires.messaging.TopicGuardPublisher;
import com.rbc.payments.wires.swift.Mt103Builder;
import com.rbc.payments.wires.validation.ValidationException;
import com.rbc.payments.wires.validation.WireTransferValidator;
import com.rbc.payments.wires.model.WireTransfer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrossBorderWireServiceTest {

    private final InMemoryMessagePublisher sink = new InMemoryMessagePublisher();
    private final CrossBorderWireService service = new CrossBorderWireService(
            new WireTransferValidator(), new Mt103Builder(), new TopicGuardPublisher(sink));

    @Test
    void publishesMt103KeyedByUetr() {
        SendResult result = service.send(TestTransfers.usdToGb());

        assertThat(result.format()).isEqualTo(MessageFormat.MT103);
        assertThat(result.topic()).isEqualTo(KafkaTopics.CROSSBORDER_MT103_OUTBOUND);
        assertThat(sink.messages()).singleElement().satisfies(m -> {
            assertThat(m.key()).isEqualTo(TestTransfers.UETR.toString());
            assertThat(m.payload()).isEqualTo(new Mt103Builder().build(TestTransfers.usdToGb()));
        });
    }

    @Test
    void doesNotPublishInvalidTransfers() {
        WireTransfer t = TestTransfers.usdToGb();
        WireTransfer invalid = new WireTransfer("", t.uetr(), t.valueDate(), t.currency(), t.amount(),
                t.senderBic(), t.receiverBic(), t.orderingInstitutionBic(), t.intermediaryBic(),
                t.beneficiaryInstitutionBic(), t.orderingCustomer(), t.beneficiary(), t.remittanceInfo(),
                t.chargeBearer());

        assertThatThrownBy(() -> service.send(invalid)).isInstanceOf(ValidationException.class);
        assertThat(sink.messages()).isEmpty();
    }
}
