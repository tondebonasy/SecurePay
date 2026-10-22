# API additions

All routes below require administrator role, bearer token, and the bound `X-Device-Id`, except `/v1/health`. Successful payloads use the existing `data` envelope.

| Method and route | Behavior |
|---|---|
| GET /v1/health | service health status |
| GET /v1/admin/operations/transactions | page (zero based), size (1–100), sort (createdAt/amount/status), direction, currency, status, q, method, from, to |
| GET /v1/admin/operations/analytics | single currency (default USD), UTC from/to; counts, values, daily/method/status aggregates |
| GET /v1/admin/operations/export | same filters as transactions; authenticated CSV; rejects selections over 10,000 rows |
| POST /v1/admin/operations/transactions/{id}/verify | persisted evidence review; requires manual review without provider evidence |
| POST /v1/admin/operations/transactions/{id}/refund | `{ "reason": "10–500 character reason" }`; one full-amount manual request per eligible transaction; no funds moved |
| POST /v1/admin/operations/reconcile | `{ "csv": "reference,amount,currency\n..." }`; validates 1–1000 unquoted rows, max 200,000 characters; returns result rows with run ID in evidence |
| GET /v1/admin/operations/history/{kind} | latest 100 rows; VERIFICATION, REFUND, RECONCILIATION, RISK_REVIEW |
| GET /v1/admin/operations/risk | explainable high-value, duplicate-reference, repeated-failure flags |
| POST /v1/admin/operations/transactions/{id}/risk-review | `{ "reason": "investigation note" }`; persisted review note |
| PATCH /v1/auth/me | authenticated user profile: displayName, email; no role changes |

Original authentication, transaction, payment request, PayPal, Paynow, customer, invite, notification, device and receipt APIs remain. See generated OpenAPI for their request schemas.

Manual transaction PATCH now requires a meaningful reason and permits only marking an unpaid nonterminal record FAILED. Provider-paid states cannot be assigned through the manual admin endpoint. First-admin public claims are disabled; provision an initial administrator through a trusted, reviewed database procedure.

Reconciliation results: MATCHED, DUPLICATE_REFERENCE, MISSING_INTERNAL, MISSING_SETTLEMENT, AMOUNT_MISMATCH, CURRENCY_MISMATCH. Imports are user-supplied settlement evidence, not authenticated provider responses. Refund history uses MANUAL_REQUESTED; it never states that a provider completed a refund.
