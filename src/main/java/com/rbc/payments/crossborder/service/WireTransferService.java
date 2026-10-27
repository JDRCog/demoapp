package com.rbc.payments.crossborder.service;

import com.rbc.payments.crossborder.config.FeatureFlags;
import com.rbc.payments.crossborder.logging.PiiMasker;
import com.rbc.payments.crossborder.model.WireTransfer;
import com.rbc.payments.crossborder.mt.Mt103Builder;
import com.rbc.payments.crossborder.publish.MessagePublisher;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/** Validates, formats and publishes outbound cross-border wires. */
public final class WireTransferService {

    private static final Logger LOG = Logger.getLogger(WireTransferService.class.getName());
    private static final Pattern BIC =
            Pattern.compile("^[A-Z]{6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3})?$");

    private final Mt103Builder mt103Builder;
    private final MessagePublisher publisher;
    private final String mt103Topic;

    public WireTransferService(FeatureFlags flags, Mt103Builder mt103Builder, MessagePublisher publisher) {
        this.mt103Builder = mt103Builder;
        this.publisher = publisher;
        this.mt103Topic = flags.get("crossborder.topic.mt103");
    }

    public void send(WireTransfer wire) {
        validate(wire);
        String payload = mt103Builder.build(wire);
        publisher.publish(mt103Topic, wire.uetr(), payload);
        LOG.info(() -> "Published MT103 ref=" + wire.reference()
                + " debtorAccount=" + PiiMasker.maskAccount(wire.debtor().account())
                + " creditorAccount=" + PiiMasker.maskAccount(wire.creditor().account()));
    }

    private static void validate(WireTransfer wire) {
        if (wire.amount().signum() <= 0) {
            throw new WireValidationException("Amount must be positive for ref=" + wire.reference());
        }
        requireBic("debtorAgentBic", wire.debtorAgentBic());
        requireBic("creditorAgentBic", wire.creditorAgentBic());
        wire.intermediary().ifPresent(bic -> requireBic("intermediaryAgentBic", bic));
    }

    private static void requireBic(String field, String bic) {
        if (!BIC.matcher(bic).matches()) {
            throw new WireValidationException("Invalid BIC in " + field);
        }
    }
}
