# Security changes and deployment requirements

## Applied changes

- Removed embedded provider credentials and personal callback/contact defaults from application.yml. JWT and QR secrets now require environment configuration in standard mode; JWT rejects keys shorter than 32 bytes.
- Restricted default CORS to the local web origin. Device-session validity, expiry and revocation are checked during bound JWT authentication. Roles and account status are read from the database.
- Removed HTTP Basic authentication; added explicit JSON 401/403 errors. Generic failures no longer return exception class names. Malformed inputs return 400 and constraint conflicts return 409.
- Password hashes, provider poll URLs, and idempotency keys are excluded from entity JSON. Profile edits cannot change roles. Self-disabling is blocked; account status values are allowlisted.
- Disabled public first-admin bootstrap and manual transitions to provider-paid states. Manual failure needs a reason and cannot alter a terminal/paid record.
- Disabled Stellar produces no chain proof and cannot return verified=true. Exact paid-state matching avoids treating text such as unpaid as paid.
- Added transactional settlement validation, persisted actor/evidence history, full-amount refund eligibility, unique refund-request protection, and bounded exports with CSV formula neutralization.
- Standard upload limits are 5 MB per file and 6 MB per request. No new file upload/receipt authenticity feature is claimed.
- Web auth credentials are held in sessionStorage rather than persistent localStorage. XSS can still access these credentials; production deployment should assess a secure HttpOnly session design and CSP.
- Android backup and release cleartext traffic are disabled; unnecessary storage/audio permissions removed from declared configuration. Confirm merged native permissions in a release build.

## Before publishing or deploying

Revoke/rotate provider credentials present in the supplied source. Review history and copies separately. Configure HTTPS, explicit CORS, independent random secrets, least-privilege MySQL credentials, provider callbacks and webhook validation. The optional worker now binds loopback by default; keep it on a private network if overriding HOST. Do not distribute production `.env` files or personal records.

This work does not establish PCI DSS compliance, formal WCAG conformance, production readiness, or complete provider/webhook correctness. Existing provider integrations and password recovery require dedicated review. Rate limiting is per-process and is not suitable for multi-instance enforcement. Dependency checks and unresolved items are recorded in VALIDATION.md.


Final payment-state protections also include optimistic locking (V10), idempotent confirmation of already-paid records, rejection of late failure downgrades, and no proof insertion when anchoring is disabled. Mobile receipt views handle unavailable chain references without crashing or claiming anchoring succeeded.

