# Cross-Border Wire Service

Builds and publishes outbound cross-border customer credit transfers.

> Demo repository. All data is synthetic. Not affiliated with any real bank system.

## Message formats

| Format | Topic |
| --- | --- |
| SWIFT MT103 | `payments.crossborder.mt103.outbound.v1` |

## Build

```bash
mvn -B clean verify   # unit tests + JaCoCo line coverage gate (>= 85%)
```

## Layout

- `model/` – domain model (`WireTransfer`, `Party`, `PostalAddress`)
- `mt/` – SWIFT FIN MT103 builder
- `service/` – validate → format → publish
- `config/` – `FeatureFlags` (single access point for flags/config)
- `publish/` – `MessagePublisher` abstraction over Kafka
- `logging/` – `PiiMasker`
- `docs/mapping/mt103-to-pacs008.csv` – MT103 → ISO 20022 pacs.008 field mapping
- `docs/prompts/` – engineering task briefs
