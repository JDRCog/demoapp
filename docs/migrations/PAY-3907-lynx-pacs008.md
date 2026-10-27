# PAY-3907: Domestic Lynx payments on ISO 20022 pacs.008

**Status:** shipped, flag at 100% (`wire.lynx.iso20022.enabled=true`).
**Use this change as the template for future MT → ISO 20022 migrations in this repo.**

## Pattern

1. **Shared XML building blocks** live in `iso20022/` (`IsoXml`, `Pacs008Elements`). Reuse them; do not add an XML library.
2. **One builder per flow** (`domestic/LynxPacs008Builder`) that takes a `java.time.Clock` so output is deterministic in tests.
3. **Flag gating in the service** (`LynxPaymentService`): the flag picks builder, `MessageFormat` and topic together. Flag off path is untouched.
4. **Topics** are pre-registered in `KafkaTopics` + `docs/kafka-topics.md` via Platform Messaging before the PR.
5. **Tests**
   * service test with the flag forced on and forced off (`FeatureFlags` built from explicit `Properties`),
   * field-by-field parity test asserting each MT103 field's value appears at the mapped pacs.008 XPath (`XmlAssert`),
   * the MT103 flag-off output compared byte-for-byte with `Mt103Builder`.
6. **Rollout:** flag ships `false`, flipped per environment via `WIRE_LYNX_ISO20022_ENABLED`. Rollback = flip the flag.
