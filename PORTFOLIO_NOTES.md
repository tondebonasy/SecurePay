SecurePay
Payment Operations • Reconciliation • Risk Monitoring

SecurePay is a full-stack payment operations platform built to help teams monitor transactions, investigate payment issues, reconcile settlements, and review receipt evidence through a centralized admin dashboard and mobile app.
Key Features
- Transaction management: Search, filter, and inspect payments, statuses, and audit history.
- Reconciliation: Import settlement CSVs and detect mismatched, missing, or duplicate records.
- Risk monitoring: Review high-value transactions, duplicate references, and repeated failures.
- Reports & analytics: Explore payment trends and export filtered transaction data.
- Secure access: Backend-enforced roles, JWT authentication, device sessions, and audit logging.
- Integrations: PayPal and Paynow support, plus optional Stellar receipt-hash verification.
Tech Stack
Layer	Technologies
Web	React, TypeScript, Vite, Material UI, Tailwind CSS
Backend	Java, Spring Boot, Spring Security, REST APIs
Database	MySQL, Flyway
Mobile	React Native, Expo
Testing	Vitest, Testing Library


Quick Start
cd admin-web
npm ci
npm run dev
The web app requires a running SecurePay API. See [`admin-web/.env.example`](admin-web/.env.example) for configuration.

Explore the project: [Admin web documentation](admin-web/README.md) · [Architecture, API & security notes](docs/)
Payment-provider integrations and optional blockchain verification require backend configuration and credentials.
