# SecurePay

SecurePay is a payment operations workspace for teams that need a clear, controlled view of transaction activity, evidence review, reconciliation, and operational risk.

The recruiter-facing application in this repository is `admin-web/`, a React and TypeScript web console. Its complete setup, product objectives, workflows, technology stack, integration boundary, and release checklist are documented in [admin-web/README.md](admin-web/README.md).

## Quick start

```powershell
cd admin-web
npm ci
npm run dev
```

Run the release checks before publishing:

```powershell
npm run typecheck
npm test -- --pool=threads --maxWorkers=1
npm run build
```

The web client expects a running SecurePay API configured through `admin-web/.env.example`. Authentication, authorization, payment-provider credentials, and administrator provisioning remain server-side responsibilities; the browser does not grant roles or contain provider secrets.

## Repository layout

- `admin-web/` — SecurePay administration workspace and recruiter-facing frontend.
- `backend/` — API service consumed by the workspace.
- `mobile/` — companion mobile client.
- `docs/` — API, security, architecture, and validation notes.

Do not commit credentials, local databases, generated logs, build output, or environment files.
