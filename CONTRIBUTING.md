# Engineering rules

These apply to every change in this repo, human or AI-authored.

## 1. PII

* Customer names, account numbers / IBANs and street addresses are PII.
* Never write them to logs, metrics, exception messages or `toString()` output. Use `logging.PiiMasker`.
* Test fixtures must use synthetic data only (see `fixtures/TestTransfers`). Never paste production messages.

## 2. Dependencies

* Only artifacts listed in [docs/approved-dependencies.md](docs/approved-dependencies.md) may be added.
* Prefer the JDK (e.g. `javax.xml.stream` for XML) over a new library.
* Adding anything else requires an Architecture Review Board ticket first.

## 3. Kafka topics

* Produce only to topics in `messaging.KafkaTopics.APPROVED`, which mirrors [docs/kafka-topics.md](docs/kafka-topics.md).
* All publishing goes through `TopicGuardPublisher`.
* New topics or payload schema changes need a Platform Messaging request; do not add them in a feature PR.

## 4. Feature flags

* Behaviour changes to live payment flows ship behind a flag in `application.properties`, defaulting to `false`.
* With the flag off, output must be byte-for-byte identical to the previous release.
* CI runs the suite with the flag both on and off.

## 5. Pull requests

* Title: `<TICKET>: <summary>`.
* Include test evidence (commands run, results, coverage) and a rollback note.
* Require approval from the payments reviewers group. Authors never merge their own PRs.
