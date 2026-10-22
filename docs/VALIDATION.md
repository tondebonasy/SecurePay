# Executed validation — 18 October 2026

## Passing checks

| Check | Executed command / evidence | Result |
|---|---|---|
| Backend tests and executable package | `mvn -o -B -f backend/pom.xml verify` after downloading dependencies | BUILD SUCCESS; 20 tests, zero failures/errors |
| Backend business rules | OperationsServiceTest, IdempotencyServiceTest, and PaymentStateTest | 13 unit tests passed: payment-state transitions, disabled anchoring, duplicate confirmation, delayed failure protection, decimal/currency matching, evidence limits, refund eligibility/reasons/duplicates, settlement validation/matches, sensitive-field exclusion, idempotent replay/mismatch/expiry |
| MySQL migrations | Standard-mode packaged API against disposable MySQL 8.4.11 container | Flyway validated and applied V1–V10; queried flyway_schema_history and confirmed success=1 for every version |
| Web types | `npm run typecheck` in admin-web | Passed |
| Web production bundle | `npm run build` in admin-web | Passed; approximately 526 kB main JS, approximately 167 kB gzip; nonblocking chunk-size warning remains |
| Web dependency audit | `npm audit --json` after compatible fixes and Vite/Vitest/Router upgrades | 0 reported vulnerabilities |
| Mobile types | `npm run typecheck` and direct TypeScript CLI after compatibility fixes | Passed |
| Expo Android JavaScript/Hermes bundle | `node node_modules/expo/bin/cli export --platform android --output-dir dist` | Exported successfully; one approximately 4 MB Android bundle |
| Browser inspection | Local React UI on port 5178 connected to API on 8088 | Login and database-backed dashboard inspected; USD 42 records reconciled with integration-test counts |
| Credential and branding checks | Source scan for prior embedded provider/seed-key patterns; case-insensitive branding search excluding generated/dependency directories | Embedded provider defaults removed; compatibility identifiers documented in README; only example environment files found |


## Failures found and resolved

- Maven cache writes and npm DNS/process creation were restricted in the sandbox. Dependency installation/builds were retried with approved access.
- Initial integration tests exposed inconsistent 401/403 handling. Explicit JSON security handlers were added. The JDK HTTP client's streaming-401 limitation was avoided with Spring's JDK HTTP request factory in tests.
- An overlapping Maven build encountered stale/missing class files. Subsequent verification ran sequentially with a clean build and passed.
- A new profile form had a JSX syntax error; corrected before the final passing build and interaction suite.
- Browser sign-in exposed a stale route-level authentication check that redirected newly authenticated users to login. A mounted protected-route guard and two route-level regression tests fixed it.
- Mobile type checking exposed pre-existing module-resolution, theme spacing, subscription cleanup, camera-property, undefined state, and icon dependency errors. These were corrected.
- Initial npm audit reported 20 vulnerabilities including critical development-tool issues. The final web audit reports zero.

## Limits

Live PayPal/Paynow confirmations, real refunds, provider webhook contracts, external notification delivery, native APK/iOS builds, and physical-device permissions/camera flows were not executed. The final Android bundle includes the vector branding and corrected receipt viewer; it is not a native release-build result. Mobile and backend dependency trees have not received a complete vulnerability assessment. Hosted GitHub Actions has not run because the supplied directory has no Git repository or remote. Formal WCAG conformance, full small-screen device testing, a load test, and production security/compliance assessment remain outstanding.

README.md lists the larger unfinished product scope. These checks validate the implemented subset; they do not establish completion of every phase in the master brief.




The optional Stellar worker also installed successfully, passed node --check, returned enabled=false from /health, and rejected POST /anchor with HTTP 400 while disabled. No chain operation was performed. The retained stellar-sdk package emits a deprecation warning and should be migrated separately.




Final backend verification passed 20 tests after adding optimistic locking and payment-state regression coverage. Mockito uses the subclass mock maker because inline instrumentation in the bundled version does not support the installed JDK 25. Final Expo Android export also passed after receipt-viewer and branding changes.


## 19 October 2026 — runtime cleanup

Removed sample-data runtime profiles, seeded accounts, UI environment badge, and environment-mode headers. H2 and integration fixtures are test-scoped only. Backend: 20 tests passed. Frontend: 8 tests, TypeScript checks and production build passed. Packaged API inspected to exclude test fixtures and H2. Standard runtime requires MySQL and configured signing secrets; it was not started with missing environment settings.

