package com.rbc.payments.wires.crossborder;

import com.rbc.payments.wires.config.FeatureFlags;
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

/** Validates, renders and publishes outbound cross-border customer wires as MT103 or CBPR+ pacs.008. */
public class CrossBorderWireService {

    public static final String ISO20022_FLAG = "wire.iso20022.enabled";

    private static final Logger LOG = System.getLogger(CrossBorderWireService.class.getName());

    private final WireTransferValidator validator;
    private final Mt103Builder mt103Builder;
    private final CbprPacs008Builder pacs008Builder;
    private final MessagePublisher publisher;
    private final FeatureFlags flags;

    public CrossBorderWireService(WireTransferValidator validator, Mt103Builder mt103Builder,
                                  CbprPacs008Builder pacs008Builder, MessagePublisher publisher,
                                  FeatureFlags flags) {
        this.validator = validator;
        this.mt103Builder = mt103Builder;
        this.pacs008Builder = pacs008Builder;
        this.publisher = publisher;
        this.flags = flags;
    }

    public SendResult send(WireTransfer transfer) {
        validator.validate(transfer);
        boolean iso = flags.isEnabled(ISO20022_FLAG, false);
        MessageFormat format = iso ? MessageFormat.PACS008 : MessageFormat.MT103;
        String topic = iso ? KafkaTopics.CROSSBORDER_PACS008_OUTBOUND : KafkaTopics.CROSSBORDER_MT103_OUTBOUND;
        String payload = iso ? pacs008Builder.build(transfer) : mt103Builder.build(transfer);
        publisher.publish(topic, transfer.uetr().toString(), payload);
        LOG.log(Level.INFO, "Sent cross-border wire ref={0} uetr={1} format={2} debtorAcct={3} creditorAcct={4}",
                transfer.reference(), transfer.uetr(), format,
                PiiMasker.account(transfer.orderingCustomer().account()),
                PiiMasker.account(transfer.beneficiary().account()));
        return new SendResult(format, topic, payload);
    }
}
