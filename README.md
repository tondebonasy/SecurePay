SecurePay
A full-stack payment operations and reconciliation platform.

SecurePay brings transaction monitoring, settlement reconciliation, risk review, and payment evidence into one workspace. It includes a web administration dashboard, a mobile app, and a Spring Boot API.
Key features
- Transaction dashboard: Monitor payments, statuses, trends, and audit history.
- Reconciliation: Match settlement CSVs against internal records and flag discrepancies.
- Risk review: Investigate duplicate references, repeated failures, and high-value transactions.
- Payment workflows: Create payment requests with configurable PayPal and Paynow integrations.
- Security and evidence: Backend-enforced roles, JWT sessions, audit logs, and optional Stellar receipt-hash verification.
Tech stack
Layer	Technologies
Web	React, TypeScript, Vite, Material UI, Tailwind CSS
API	Java, Spring Boot, Spring Security, REST
Database	MySQL, Flyway
Mobile	React Native, Expo
Integrations	PayPal, Paynow Zimbabwe, optional Stellar


Quick start
cd admin-web
npm ci
npm run dev
The web app requires a running backend API. See [`admin-web/README.md`](admin-web/README.md) for configuration and development details.

Status: Portfolio/development project. Production payment credentials, security review, and deployment validation are still required.
