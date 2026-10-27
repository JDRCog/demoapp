package com.rbc.payments.wires.domestic;

import com.rbc.payments.wires.config.FeatureFlags;
import com.rbc.payments.wires.logging.PiiMasker;
import com.rbc.payments.wires.messaging.KafkaTopics;
import com.rbc.payments.wires.messaging.MessageFormat;
import com.rbc.payments.wires.messaging.MessagePublisher;
import com.rbc.payments.wires.messaging.SendResult;
import com.rbc.payments.wires.model.WireTransfer;
import com.rbc.payments.wires.swift.Mt103Builder;
import com.rbc.payments.wires.validation.ValidationException;
import com.rbc.payments.wires.validation.WireTransferValidator;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;

/** Sends domestic CAD high-value payments over Lynx, as MT103 or pacs.008 depending on the flag. */
public class LynxPaymentService {

    public static final String ISO20022_FLAG = "wire.lynx.iso20022.enabled";

    private static final Logger LOG = System.getLogger(LynxPaymentService.class.getName());

    private final WireTransferValidator validator;
    private final Mt103Builder mt103Builder;
    private final LynxPacs008Builder pacs008Builder;
    private final MessagePublisher publisher;
    private final FeatureFlags flags;

    public LynxPaymentService(WireTransferValidator validator, Mt103Builder mt103Builder,
                              LynxPacs008Builder pacs008Builder, MessagePublisher publisher, FeatureFlags flags) {
        this.validator = validator;
        this.mt103Builder = mt103Builder;
        this.pacs008Builder = pacs008Builder;
        this.publisher = publisher;
        this.flags = flags;
    }

    public SendResult send(WireTransfer transfer) {
        validator.validate(transfer);
        if (!"CAD".equals(transfer.currency())) {
            throw new ValidationException(List.of("Lynx payments must be in CAD"));
        }
        boolean iso = flags.isEnabled(ISO20022_FLAG, false);
        MessageFormat format = iso ? MessageFormat.PACS008 : MessageFormat.MT103;
        String topic = iso ? KafkaTopics.LYNX_PACS008_OUTBOUND : KafkaTopics.LYNX_MT103_OUTBOUND;
        String payload = iso ? pacs008Builder.build(transfer) : mt103Builder.build(transfer);
        publisher.publish(topic, transfer.uetr().toString(), payload);
        LOG.log(Level.INFO, "Sent Lynx payment ref={0} uetr={1} format={2} debtorAcct={3} creditorAcct={4}",
                transfer.reference(), transfer.uetr(), format,
                PiiMasker.account(transfer.orderingCustomer().account()),
                PiiMasker.account(transfer.beneficiary().account()));
        return new SendResult(format, topic, payload);
    }
}
