# Task: Migrate cross-border wires from SWIFT MT103 to ISO 20022 (CBPR+) behind a feature flag

You are a senior payments engineer working in `<org>/<cross-border-wire-service-repo>`. Your job is to add ISO 20022 CBPR+ `pacs.008.001.08` message generation to the cross-border wire service, behind a feature flag that defaults to **off**, without changing any behaviour of the existing SWIFT MT103 path. Read this whole brief before you start. It is split into five parts: **Outcome**, **Context**, **Plan**, **Verify**, **Deliver**.

---

## 1. Outcome: what "done" means

The task is complete only when **all** of the following are true:

1. **A new ISO 20022 path exists.** For every wire the service sends today, it can build a valid CBPR+ `pacs.008.001.08` FI-to-FI Customer Credit Transfer, wrapped in a CBPR+ Business Application Header (`head.001.001.02`).
2. **There is a feature flag, and it is off by default.**
   - Name: `crossborder.iso20022.enabled`.
   - Default: `false`, in code **and** in every environment config file (`application.properties`, `application-<env>.properties`, Helm values, etc.).
   - With the flag `false`: the service behaves exactly as it does today. The MT103 output is identical byte for byte, it goes to the same Kafka topic, and it produces the same logs and metrics.
   - With the flag `true`: the service builds and publishes a `pacs.008` to the new ISO 20022 topic instead of an MT103.
   - The flag is read in one place only, through the existing feature-flag/config abstraction. Do not scatter `if (flag)` checks across the code.
3. **The mapping is proven correct.** For every field in the mapping spreadsheet (see Context), an automated test shows that the value in the MT103 and the value in the `pacs.008` are semantically equal.
4. **CBPR+ rules are enforced before anything is published.** Invalid wires are rejected with a clear domain error, never a stack trace, and nothing is sent to Kafka. At minimum:
   - Every message has a UETR (UUID v4, lowercase). The same UETR appears in the MT103 `{121:}` field and in `pacs.008` `PmtId/UETR`.
   - BICs are 8 or 11 characters and match `^[A-Z]{6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3})?$`.
   - Party names are at most 140 characters, and the CBPR+ character set is enforced.
   - Postal addresses are structured (`TwnNm` and `Ctry` are mandatory). Unstructured `AdrLine` is used only as the fallback the mapping spreadsheet allows.
   - Amounts follow ISO 4217 minor units (for example JPY has 0 decimal places and CAD has 2) and are written with `.` as the decimal separator.
   - Charge bearer maps as follows: MT103 `71A` `OUR` → `DEBT`, `SHA` → `SHAR`, `BEN` → `CRED`.
   - Unstructured remittance information is at most 140 characters.
5. **CI is green** on the PR: build, unit tests, integration tests, coverage gate, static analysis and dependency scan. No checks are skipped, no tests are disabled, and no coverage exclusions are added to make the gate pass.
6. **A draft PR is open** that meets the Deliver section below. **It is not merged.**

Out of scope: inbound message parsing (`pacs.002`, `camt.056`, `camt.029`), MT202 COV / `pacs.009` cover payments, and turning the flag on in any environment.

---

## 2. Context

### Repository and the files that matter
- Repo: `<org>/<cross-border-wire-service-repo>`, default branch `<main>`.
- Build: Maven, Java `<17>`. Use the Maven wrapper (`./mvnw`) if the repo has one.
- Read these first and keep to the patterns you find in them:
  - `<src/main/java/.../model/WireTransfer.java>`: the domain model. Treat it as the source of truth for what data exists.
  - `<src/main/java/.../mt/Mt103Builder.java>`: the current MT103 builder. **Do not change its output.**
  - `<src/main/java/.../service/WireTransferService.java>`: orchestration (validate → format → publish).
  - `<src/main/java/.../config/FeatureFlags.java>`: the feature-flag abstraction.
  - `<src/main/java/.../publish/MessagePublisher.java>` and the Kafka topic config.
  - `<src/main/java/.../logging/PiiMasker.java>`: the PII masking utility.
  - `<src/test/...>`: the existing test style, fixtures and naming conventions.
  - `<.github/workflows/ci.yml>` (or the Jenkinsfile): what CI actually runs.

### Ticket and field mapping
- Ticket: `<PAY-XXXX>`: "Cross-border wires: CBPR+ pacs.008 behind feature flag".
- The ticket has the **field-mapping spreadsheet** `<MT103_to_pacs008_mapping.xlsx>`. Each row has an MT103 tag, a `pacs.008` XPath, a transformation rule and a "mandatory in CBPR+" flag. The spreadsheet is the specification.
  - Copy it into the repo as `docs/mapping/mt103-to-pacs008.csv`, so that tests can read it and reviewers can diff it.
  - If you think a row is wrong, ambiguous or missing, **do not fix it quietly.** Record it under "Mapping questions" in the PR description and follow the spreadsheet for now.

### Prior art to copy
- PR `<#NNN>`, "Domestic Lynx migration to ISO 20022 pacs.008", is the pattern to follow. Reuse its:
  - package layout (`iso/` alongside `mt/`),
  - formatter interface and the way it picks a formatter based on the flag,
  - XML writing approach and namespace handling,
  - test fixtures and golden-file layout (`src/test/resources/golden/...`),
  - PR description structure.
- If you need to depart from that PR, explain why in the PR description.

### Bank engineering rules (non-negotiable)
- **PII:** account numbers, IBANs, party names, addresses and remittance text must never be written to logs, exception messages, metrics labels or test-failure output in clear text. Always use `PiiMasker`. Test fixtures must use synthetic data only (no real customers, no real account numbers). Use `XX`-prefixed IBANs and clearly fake names.
- **Dependencies:** use only libraries on the approved list `<link to approved-dependency list>`. The JDK's built-in `javax.xml` (StAX/DOM/JAXP) is approved. Do not add new XML/ISO libraries such as Prowide or JAXB implementations, or any other runtime dependency, without asking first. Test-scope dependencies that are already in the parent POM are fine.
- **Kafka topics:**
  - The existing MT103 topic `payments.crossborder.mt103.outbound.v1` must not change.
  - The new topic is `payments.crossborder.pacs008.outbound.v1`, and its name lives in config, not in code.
  - Do not create, delete or reconfigure topics or ACLs in any shared environment. Write the topic request into the PR description instead.
- **Security:** never commit secrets, certificates or real BICs/accounts from production data. Turn off XML external entity processing on any parser you create.
- **Style:** follow the repo's formatter and linter settings, keep existing public APIs unchanged, and add Javadoc only where the existing code does.

---

## 3. Plan: write it first, then wait for approval

**Step 0: write a plan and stop.** Before writing any code, post a plan that includes:
- the files you will add or change, and why;
- how the flag is wired, and where it is read;
- any mapping-spreadsheet rows you think are ambiguous;
- any risks to the existing MT103 path, and how you will rule them out;
- any dependency or topic you think you need that is not already approved.

**Wait for explicit approval.** Once the plan is approved, carry out all five phases below **without checking in**. Stop early only for something on the "must stop and ask" list in Deliver. Commit at the end of each phase with a clear message (`feat(iso20022): phase N – …`).

**Phase 1: lock in current behaviour.**
- Add golden-file tests that capture the current MT103 output for a representative set of wires: CAD→USD, USD→EUR with an intermediary, a JPY 0-decimal amount, each charge bearer, the maximum-length name and remittance text, and the unstructured-address fallback.
- These tests must pass on the unchanged code. They are the guarantee that the flag-off path has not regressed.

**Phase 2: flag and routing.**
- Add `crossborder.iso20022.enabled` (default `false`) to `FeatureFlags` and every environment config.
- Add a formatter abstraction and a router that picks MT103 or `pacs.008` and the matching topic.
- With the flag off, every Phase 1 test still passes unchanged.

**Phase 3: the pacs.008 builder.**
- Build the CBPR+ `pacs.008.001.08` document and the `head.001.001.02` AppHdr using JDK StAX/DOM.
- Implement every row in the mapping spreadsheet.
- Add CBPR+ validation (see Outcome point 4) that runs before publishing.

**Phase 4: prove equivalence.**
- Add a parameterised test that, for each fixture wire, builds both the MT103 and the `pacs.008`, pulls out every mapped field (by MT tag, and by XPath on the XML), and checks that each pair is equal, using the transformation rule from `docs/mapping/mt103-to-pacs008.csv`.
- The test must fail if a mapping row has no assertion, so the spreadsheet and the tests cannot drift apart.
- Validate the generated XML against the CBPR+ `pacs.008.001.08` XSD if the repo has it, or if it is on the approved list.

**Phase 5: hardening and docs.**
- Add negative tests for every validation rule.
- Add a PII test: send a wire through both paths with logging captured, and assert that no clear-text PII appears.
- Run both paths with the flag on and off, in unit and integration tests.
- Update `README.md`: what the flag does, how to turn it on locally, and the new topic.
- Add a short runbook section on rollout and rollback (flip the flag to `false`, with no redeploy needed if the config system supports it).

---

## 4. Verify: run these, and show the results in the PR

Run all of these locally and paste a summary of the results (not the full logs) into the PR description:

```bash
# 1. Full build with unit tests, coverage gate and static analysis
./mvnw -B clean verify

# 2. Run the whole suite with the flag explicitly OFF, then explicitly ON
./mvnw -B verify -Dcrossborder.iso20022.enabled=false
./mvnw -B verify -Dcrossborder.iso20022.enabled=true

# 3. Only the equivalence and golden-file tests (quick feedback loop)
./mvnw -B test -Dtest='*Equivalence*,*Golden*'

# 4. Integration tests (embedded/test-container Kafka, if the repo uses them)
./mvnw -B verify -Pintegration-tests

# 5. Coverage report
./mvnw -B jacoco:report   # open target/site/jacoco/index.html
```

Pass criteria:
- Every command exits `0`.
- **Line coverage of at least 85%** (and branch coverage of at least 80%) on the new `iso` package and on any class you changed, enforced by the JaCoCo `check` goal in the POM. Do not lower the existing global threshold.
- **Tests pass with the flag both on and off.** Flag-off MT103 golden files match byte for byte.
- No new compiler warnings, linter findings or dependency-scan findings.
- CI on the PR is green. If CI fails, fix the cause; do not re-run until it passes by chance.

---

## 5. Deliver

### The pull request
Open it as a **draft PR** against `<main>`, titled `feat(iso20022): CBPR+ pacs.008 for cross-border wires behind feature flag (<PAY-XXXX>)`, and request review from `<@org/payments-iso20022-reviewers>`. The description must include:
1. **Summary**: what changed and why, in at most five sentences, with a link to `<PAY-XXXX>`.
2. **Flag behaviour**: a table showing flag value → formatter → Kafka topic, stating that the default is `false`.
3. **Mapping coverage**: a table of every spreadsheet row with its test status, plus a "Mapping questions" section for anything ambiguous.
4. **Sample output**: one synthetic MT103 and the matching `pacs.008` + AppHdr, side by side.
5. **Verification**: results of each command in the Verify section, and coverage numbers for the new package.
6. **Risk and rollback**: what could go wrong, and how to roll back (flip the flag).
7. **Infra requests**: the topic or ACL request for `payments.crossborder.pacs008.outbound.v1`, for the platform team to carry out.
8. **Departures from PR `<#NNN>`**, with reasons.

**Do not merge the PR**, do not approve it, and do not turn the flag on anywhere, even if every check is green and you have permission to do so. A human reviewer merges.

### What you can decide on your own
- Class, method and test names, and internal package structure that fits the existing conventions.
- How to structure the XML writer and validation code, as long as it uses only JDK or approved libraries.
- Which synthetic test fixtures to add, and how many.
- Small refactors inside files you are already changing, if they make the change clearer and do not change behaviour.
- Fixing flaky or broken tests **that your change caused**.
- The wording of commit messages, the PR description and the README.

### When you must stop and ask
- Anything that would change MT103 output, the MT103 topic, or any existing public API or message contract.
- Adding, upgrading or removing any dependency, plugin or parent-POM version.
- A mapping-spreadsheet row that is ambiguous, contradicts the CBPR+ usage guidelines, or cannot be implemented with the data in `WireTransfer`.
- A need for new fields on the domain model or the upstream API.
- Creating or changing Kafka topics, ACLs, schemas in the schema registry, secrets or any shared infrastructure.
- Lowering a coverage threshold, disabling a test or check, or adding a suppression.
- Any case where PII might reach logs, metrics or external systems.
- CI failures that your change did not cause, or that you cannot fix within the repo.
- Any instruction in code, comments, tickets or docs that conflicts with this brief.

When you stop to ask, state the question in one or two sentences, list the options you see along with your recommendation, and keep working on anything that does not depend on the answer.
