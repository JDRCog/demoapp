# Cross-Border Wire Service

> Demo repository. All bank names, BICs, accounts and customers are synthetic.

Validates outbound customer wires, renders them in the correct interbank format and publishes them to Kafka for the SWIFT gateway.

| Flow | Format today | Flag | Topic |
|---|---|---|---|
| Cross-border (`crossborder/`) | SWIFT MT103 | — | `payments.crossborder.mt103.outbound.v1` |
| Domestic Lynx (`domestic/`) | ISO 20022 pacs.008 | `wire.lynx.iso20022.enabled` | `payments.domestic.lynx.*` |

## Build

```bash
mvn -B clean verify            # compile, test, JaCoCo report + 85% line-coverage gate
```

Coverage report: `target/site/jacoco/index.html`.

## Layout

```
src/main/java/com/rbc/payments/wires/
  model/        WireTransfer, Party, ChargeBearer (format-agnostic domain)
  swift/        Mt103Builder (SWIFT FIN MT103)
  config/       FeatureFlags (env > -D system property > application.properties > default)
  messaging/    MessagePublisher, KafkaTopics (approved topics), TopicGuardPublisher, SendResult
  validation/   WireTransferValidator
  logging/      PiiMasker
  crossborder/  CrossBorderWireService
  iso20022/     IsoXml (StAX writer), Pacs008Elements (shared pacs.008 blocks)
  domestic/     LynxPaymentService, LynxPacs008Builder
```

See [CONTRIBUTING.md](CONTRIBUTING.md) for engineering rules (PII, dependencies, Kafka topics).
