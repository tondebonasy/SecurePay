# Production release status — 19 October 2026

Release is NOT approved. Local builds are evidence of compilation and automated behavior, not live provider correctness.

Pending: hosting platform, HTTPS web/API domains, MySQL version and access configuration, enabled provider list and callbacks, private worker topology, deployment secret-store configuration, backup/restore evidence, sandbox contract tests and native device QA.

Audit findings requiring resolution and regression tests before release:
- Paynow poll/trace ownership checks; amount/currency validation against configured settlement currency.
- PayPal webhook capture/order correlation and replay/retry handling; untrusted events must not poison deduplication.
- Durable post-payment anchoring/notifications so ancillary provider failures cannot roll back a captured payment record.
- Session refresh concurrency, logout ownership, and client refresh retry bounds.
- Remove mock SMS success/logging behavior from production configuration; verify real OTP delivery/recovery.
- Authenticate private Stellar worker operations; verify successful transactions, expected signer and exact receipt key, with safe replay handling.
- Replace the unsupported backend dependency line or establish vendor support; assess dependencies in all modules.
- Validate normal MySQL migrations and real administrator provisioning in the target staging environment.

Frontend framework migration results and remaining presentation scope: FRONTEND_STACK.md. No live payment was made, no production deployment performed, and no production secrets were requested in chat.

Current automated baseline: backend package and 20 tests pass; web 8 tests/typecheck/build pass; mobile types and Android Hermes export pass. These checks do not cover the outstanding provider/session/worker findings listed above.


Mobile compatible patch remediation: 68 findings reduced to 46 (20 moderate, 26 high; zero critical). Remaining toolchain advisories require a planned major-version upgrade and regression/native-device validation. Do not treat npm audit fix --force as a release procedure.

