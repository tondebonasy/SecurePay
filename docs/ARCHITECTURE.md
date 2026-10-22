# Architecture and decisions

SecurePay retains a modular Spring Boot monolith, React/MUI operations client, Expo customer client, and optional Stellar worker. It does not introduce microservices.

Transactions store decimal amounts and explicit currencies. New analytics always select one currency; UTC boundaries are inclusive at the start and exclusive at the end. The web date filter converts an inclusive end date into the next UTC midnight.

`OperationsService` owns manual verification, refund eligibility, and deterministic settlement rules. `operations` stores actor, transaction link, state, evidence, timestamp, and optional unique deduplication key. V9 adds this table without modifying historical migrations. The refund key `REFUND:<transaction-id>` prevents concurrent duplicate requests. Conflicts return HTTP 409. Reconciliation validates every row before writing and runs in one database transaction. Runs never change transaction values or balances.

Verification reviews explicitly state that no fresh trusted provider response was obtained. Existing receipt anchoring is a separate integrity mechanism; disabled Stellar never produces a fabricated successful proof.


The web shell retains customer and invitation workflows while replacing the operations presentation. MUI provides dialog focus handling, labels, form controls and responsive layout. Daily chart values also have a text representation. Themes use navy, royal blue, cyan, and semantic status colours.

Historical Java/native/database/anchor identifiers are intentional compatibility exceptions. A migration to a new Android/iOS package ID would create a different installed application and is outside a display rebrand.


V10 adds a JPA optimistic version column to transactions. A stale write fails with HTTP 409 instead of overwriting a concurrent payment update. Disabled anchoring creates no proof entity; provider-confirmed payment and receipt integrity remain separate. Duplicate confirmed PayPal operations reuse the existing record, and delayed failure events cannot downgrade a paid state.

