# PAY-4821: Cross-border wires on ISO 20022 pacs.008 (CBPR+)

Follows the PAY-3907 Lynx pattern.

* Flag: `wire.iso20022.enabled` (default `false`). Env override `WIRE_ISO20022_ENABLED`.
* Flag on: `CbprPacs008Builder` emits `<Envelope>` with `head.001.001.02` AppHdr + `pacs.008.001.08` Document to `payments.crossborder.pacs008.outbound.v1`, keyed by UETR.
* Flag off: unchanged MT103 to `payments.crossborder.mt103.outbound.v1`.
* Parity: `CbprPacs008BuilderTest` asserts every row of `docs/tickets/PAY-4821-field-mapping.csv` (row 6, `:23B:`, intentionally unmapped).
* Rollback: set `WIRE_ISO20022_ENABLED=false`.
