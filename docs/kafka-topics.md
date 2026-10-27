# Kafka topics produced by this service

| Topic | Payload | Key | Status |
|---|---|---|---|
| `payments.crossborder.mt103.outbound.v1` | SWIFT FIN MT103 text | UETR | Live |
| `payments.crossborder.pacs008.outbound.v1` | CBPR+ `head.001.001.02` + `pacs.008.001.08` XML | UETR | Provisioned, not yet produced to |

Keep this table in sync with `messaging.KafkaTopics`. New topics require a Platform Messaging request.
