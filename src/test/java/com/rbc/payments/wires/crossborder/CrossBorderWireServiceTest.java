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
import com.rbc.payments.wires.config.FeatureFlags;

import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrossBorderWireServiceTest {

    private final InMemoryMessagePublisher sink = new InMemoryMessagePublisher();
    private final CrossBorderWireService service = service(false);

    private CrossBorderWireService service(boolean iso) {
        Properties sys = new Properties();
        sys.setProperty(CrossBorderWireService.ISO20022_FLAG, Boolean.toString(iso));
        return new CrossBorderWireService(new WireTransferValidator(), new Mt103Builder(),
                new CbprPacs008Builder(CbprPacs008BuilderTest.CLOCK), new TopicGuardPublisher(sink),
                new FeatureFlags(new Properties(), sys, Map.of()));
    }

    @Test
    void flagOnPublishesPacs008ToApprovedTopic() {
        SendResult result = service(true).send(TestTransfers.usdToGb());

        assertThat(result.format()).isEqualTo(MessageFormat.PACS008);
        assertThat(result.topic()).isEqualTo(KafkaTopics.CROSSBORDER_PACS008_OUTBOUND);
        assertThat(sink.messages()).singleElement().satisfies(m -> {
            assertThat(m.key()).isEqualTo(TestTransfers.UETR.toString());
            assertThat(m.payload()).isEqualTo(
                    new CbprPacs008Builder(CbprPacs008BuilderTest.CLOCK).build(TestTransfers.usdToGb()));
        });
    }

    @Test
    void flagDefaultsToOffFromApplicationProperties() {
        assertThat(new FeatureFlags(loadAppProperties(), new Properties(), Map.of())
                .isEnabled(CrossBorderWireService.ISO20022_FLAG, true)).isFalse();
    }

    private static Properties loadAppProperties() {
        Properties p = new Properties();
        try (var in = CrossBorderWireServiceTest.class.getResourceAsStream("/application.properties")) {
            p.load(in);
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
        return p;
    }

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
