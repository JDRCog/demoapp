package com.rbc.payments.wires.crossborder;

import com.rbc.payments.wires.logging.PiiMasker;
import com.rbc.payments.wires.messaging.KafkaTopics;
import com.rbc.payments.wires.messaging.MessageFormat;
import com.rbc.payments.wires.messaging.MessagePublisher;
import com.rbc.payments.wires.messaging.SendResult;
import com.rbc.payments.wires.model.WireTransfer;
import com.rbc.payments.wires.swift.Mt103Builder;
import com.rbc.payments.wires.validation.WireTransferValidator;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

/** Validates, renders and publishes outbound cross-border customer wires. */
public class CrossBorderWireService {

    private static final Logger LOG = System.getLogger(CrossBorderWireService.class.getName());

    private final WireTransferValidator validator;
    private final Mt103Builder mt103Builder;
    private final MessagePublisher publisher;

    public CrossBorderWireService(WireTransferValidator validator, Mt103Builder mt103Builder,
                                  MessagePublisher publisher) {
        this.validator = validator;
        this.mt103Builder = mt103Builder;
        this.publisher = publisher;
    }

    public SendResult send(WireTransfer transfer) {
        validator.validate(transfer);
        String payload = mt103Builder.build(transfer);
        String topic = KafkaTopics.CROSSBORDER_MT103_OUTBOUND;
        publisher.publish(topic, transfer.uetr().toString(), payload);
        LOG.log(Level.INFO, "Sent cross-border wire ref={0} uetr={1} format={2} debtorAcct={3} creditorAcct={4}",
                transfer.reference(), transfer.uetr(), MessageFormat.MT103,
                PiiMasker.account(transfer.orderingCustomer().account()),
                PiiMasker.account(transfer.beneficiary().account()));
        return new SendResult(MessageFormat.MT103, topic, payload);
    }
}
