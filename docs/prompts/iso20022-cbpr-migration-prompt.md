# Devin prompt: cross-border wires MT103 → ISO 20022 (CBPR+)

> Placeholders are in `<angle brackets>`. Replace them before sending. In this demo repo they resolve to:
> `<org>/<repo>` = `JDRCog/demoapp`, `<TICKET>` = `PAY-4821`, `<LYNX_PR>` = the `PAY-3907` commit on `main`,
> `<REVIEWER_TEAM>` = any reviewer.

---

## Outcome

Migrate outbound cross-border customer wires in `<org>/<repo>` from SWIFT MT103 to ISO 20022 `pacs.008.001.08` with a `head.001.001.02` Business Application Header, following the CBPR+ usage guidelines, behind the feature flag `wire.iso20022.enabled`.

Done means:
- The flag exists in `application.properties` and **defaults to `false`**.
- With the flag **off**, MT103 output and the Kafka topic are byte-for-byte unchanged.
- With the flag **on**, every row of the `<TICKET>` field-mapping spreadsheet is implemented and published to the approved pacs.008 topic, keyed by UETR.
- CI is green and a PR is open for review.

## Context

- **Repo:** `<org>/<repo>`. Files that matter:
  - `crossborder/CrossBorderWireService.java` — entry point you will gate
  - `swift/Mt103Builder.java` — today's output; source of truth for parity
  - `iso20022/IsoXml.java`, `iso20022/Pacs008Elements.java` — shared XML building blocks
  - `config/FeatureFlags.java`, `src/main/resources/application.properties`
  - `messaging/KafkaTopics.java`, `docs/kafka-topics.md`
- **Ticket:** `<TICKET>` (`docs/tickets/<TICKET>.md`) with the field-mapping spreadsheet `docs/tickets/<TICKET>-field-mapping.csv`. The spreadsheet is the spec; one parity assertion per row.
- **Past PR to copy:** `<LYNX_PR>` (domestic Lynx migration, `docs/migrations/PAY-3907-lynx-pacs008.md`). Mirror its structure: one builder with an injected `Clock`, flag gating in the service, flag on/off service tests, field-by-field parity test using `XmlAssert`.
- **Rules (see `CONTRIBUTING.md`):**
  - **PII:** no names, accounts/IBANs or addresses in logs, exceptions or `toString()`. Use `PiiMasker`. Synthetic fixtures only.
  - **Dependencies:** only what is in `docs/approved-dependencies.md`. Use JDK `javax.xml` for XML.
  - **Kafka:** only topics in `KafkaTopics.APPROVED`. `payments.crossborder.pacs008.outbound.v1` is already provisioned.

## Plan

First, reply with a written plan (files to add/change, mapping approach, test strategy, risks and open questions). **Wait for my approval before writing code.**

Once approved, work through these phases without checking in:
1. **Flag** — add `wire.iso20022.enabled=false` and wire `FeatureFlags` into the service.
2. **Builder** — `CbprPacs008Builder` producing BAH + pacs.008 per the spreadsheet, reusing `iso20022/`.
3. **Routing** — service selects builder, `MessageFormat` and topic from the flag; flag-off path untouched.
4. **Tests** — builder unit tests, row-by-row parity test (MT103 vs pacs.008 for the same input), service tests with the flag on and off, logging has no PII.
5. **Docs + PR** — migration note under `docs/migrations/`, README/topic table updates, open the PR.

## Verify

Run and paste results in the PR:
```bash
mvn -B clean verify                              # JaCoCo gate: >= 85% line coverage
mvn -B clean verify -Dwire.iso20022.enabled=false
mvn -B clean verify -Dwire.iso20022.enabled=true
```
- All tests pass with the flag both on and off.
- Line coverage **≥ 85%** (report the number from `target/site/jacoco/index.html`).
- Every spreadsheet row has an assertion; list any row you could not satisfy.

## Deliver

Open a PR against `main` titled `<TICKET>: Cross-border wires on ISO 20022 pacs.008 (CBPR+) behind flag` containing:
- Summary and the mapping table (spreadsheet row → code/test), with any deviations called out.
- Test evidence: commands, results, coverage %.
- Rollout and rollback (flip `WIRE_ISO20022_ENABLED`).
- Reviewers: `<REVIEWER_TEAM>`.

**Do not merge.**

**You can decide alone:**
- Class, method and test names; internal structure of new classes.
- Small refactors confined to files you are already changing.
- Test fixture design (synthetic data only).
- Choice between JDK XML APIs.

**Stop and ask before:**
- Adding any dependency not in `docs/approved-dependencies.md`.
- Producing to a topic not in `KafkaTopics.APPROVED`, or changing a payload schema.
- Any change to flag-off (MT103) behaviour or output.
- A mapping row that is ambiguous, contradicts CBPR+, or would truncate/lose data.
- Changing CI config, the coverage threshold, or files outside the cross-border flow.
