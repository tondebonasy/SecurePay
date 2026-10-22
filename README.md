SecurePay
Payment operations, reconciliation, and transaction intelligence in one workspace.

SecurePay is a full-stack payment operations platform for managing transaction activity, reviewing payment evidence, investigating operational risk, and reconciling settlement data. It combines a responsive administration console, a Java Spring Boot API, and an Expo mobile application, with configurable payment-provider integrations and optional blockchain-based receipt verification.
Project status: Portfolio / development project. Core application workflows are implemented, but live provider configuration, security hardening, and production validation are still required. This repository is not presented as a PCI DSS-certified payment processor.

At a glance
Area	Capabilities
Payment operations	Transaction monitoring, payment requests, status tracking, and manual refund requests
Admin console	Analytics dashboard, advanced filtering, evidence review, and audit history
Reconciliation	Settlement CSV imports, reference and amount matching, mismatch and duplicate detection
Risk review	High-value flags, duplicate references, repeated failures, and investigation notes
Reporting	Filtered analytics, bounded CSV exports, and printable views
Mobile experience	Authentication, payments and payment requests, QR scanning, history, and notifications
Receipt integrity	Optional SHA-256 receipt-hash anchoring and verification through Stellar


Architecture
        ┌────────────────────────┐     ┌────────────────────────┐
        │  Admin Web             │     │  Mobile App            │
        │  React + TypeScript    │     │  Expo + React Native   │
        └────────────┬───────────┘     └────────────┬───────────┘
                     │                              │
                     └──────────────┬───────────────┘
                                    │ HTTPS / REST
                     ┌──────────────▼───────────────┐
                     │   SecurePay API              │
                     │   Java + Spring Boot         │
                     │   Security · Business Rules  │
                     │   Payments · Audit · Jobs    │
                     └──────────────┬───────────────┘
                                    │
          ┌─────────────────────────┼─────────────────────────┐
          │                         │                         │
   ┌──────▼──────┐         ┌────────▼────────┐       ┌────────▼────────┐
   │ MySQL       │         │ Payment &       │       │ Optional Stellar│
   │ + Flyway    │         │ Messaging APIs  │       │ Receipt Worker  │
   └─────────────┘         │ PayPal · Paynow │       └─────────────────┘
                           │ SMS · Expo Push │
                           └─────────────────┘
The backend is a modular monolith, not a collection of independently deployed microservices. It owns authorization, payment-provider communication, persistence, webhook handling, and audit records. Provider secrets and Stellar signing keys remain on the server side.
Technology stack
Layer	Technologies
Web frontend	React 18, TypeScript, Vite, Material UI, Tailwind CSS 4, Radix UI, TanStack Table, Recharts
Web forms and testing	React Hook Form, Zod, Axios, Vitest, Testing Library
Backend	Java, Spring Boot, Spring Security, Spring Data JPA, Hibernate, WebClient
Database	MySQL, Flyway
Authentication	JWT, BCrypt, refresh sessions, device-bound session checks, role-based authorization
Mobile	Expo, React Native, TypeScript, React Native Paper, React Navigation, Expo SecureStore
Integrations	PayPal, Paynow Zimbabwe, SMS/OTP services, Expo Notifications
Optional integrity layer	Stellar SDK, Stellar Horizon, SHA-256 receipt hashes


Key workflows
1. Operations dashboard
Monitor collections, receipt activity, payment-method distribution, status breakdowns, and trends. Search and inspect transactions using filters for date, currency, provider, and status.
2. Payment verification and risk review
Inspect payment evidence, add verification notes, and investigate flagged activity such as duplicate references, repeated failures, and high-value transactions. Operational review status is tracked separately from provider payment status.
3. Settlement reconciliation
Import settlement CSV files and compare them with internal transaction records by reference, amount, and currency. Reconciliation identifies discrepancies, missing records, and duplicates without silently changing balances.
4. Payment requests and mobile workflows
Create payment requests, initiate configured provider payments, scan QR payment requests, view history, and receive notifications through the mobile client.
5. Optional receipt integrity verification
SecurePay can anchor a SHA-256 receipt hash on Stellar using a separate worker and later compare that hash with on-chain data. The receipt itself and payment details are not placed on-chain. Blockchain verification is independent of provider payment confirmation and is unavailable when the worker is disabled.
Security and data integrity
The implementation includes:
- Backend-enforced administrator roles and account-status checks.
- JWT-based authentication, refresh sessions, device identifiers, and session revocation.
- BCrypt password hashing, input validation, and generic invalid-login responses.
- Request rate limiting, audit events, and restricted administrative routes.
- Idempotency protections and optimistic transaction locking.
- Bounded CSV exports with spreadsheet-formula neutralization.
- Server-side provider credentials and validation of supported payment-provider responses.
Important: Security controls described here are implemented features, not a claim of a completed security audit or regulatory certification. Process-local rate limiting and current JWT storage choices require additional review before high-security deployment.
Repository structure
SecurePay/
├── admin-web/       # React + TypeScript administration console
├── backend/         # Spring Boot API and domain modules
├── mobile/          # Expo / React Native application
├── stellar-worker/  # Optional receipt-anchoring service
├── docs/            # Architecture, API, security, and validation notes
└── README.md
For the admin console's detailed workflows and setup, see [README.md](admin-web/README.md).
Getting started
Prerequisites
- Node.js and npm compatible with the frontend and mobile projects.
- A Java JDK and Maven compatible with the backend project.
- MySQL with a database and application credentials.
- Environment-specific JWT and QR-signing secrets.
- Provider credentials only if testing external integrations.
1. Run the backend
Configure database access and required application secrets using the backend's environment/deployment configuration, then run:
cd backend
mvn spring-boot:run
Flyway manages the database migrations. Do not commit secrets to the repository.
2. Run the admin console
In another terminal:
cd admin-web
npm ci
npm run dev
The Vite development server normally starts on http://localhost:5173. Configure the client API endpoint using the variables described in [`admin-web/.env.example`](admin-web/.env.example); the API must be running for authenticated workflows.
3. Run the mobile app
In another terminal:
cd mobile
npm ci
npm start
For an Android development environment, use npm run android if configured. A physical device must use an API address reachable from that device; its localhost does not refer to your computer.
4. Optional: run the Stellar worker
cd stellar-worker
npm ci
npm start
Stellar is disabled by default. Enabling anchoring requires worker configuration, a Stellar secret key, a funded account on the chosen network, and network settings. Development defaults use Stellar testnet; keep the worker private and protected.
Validation
From admin-web/, run:
npm run typecheck
npm test -- --pool=threads --maxWorkers=1
npm run build
These are project validation commands, not a claim that all tests or production acceptance checks currently pass. Backend, mobile, integration, and end-to-end validation should be performed separately before release.
Deployment and integration notes
- PayPal: Uses backend-side order creation, capture, and optional verified webhooks. Sandbox defaults require production configuration before live use.
- Paynow Zimbabwe: Uses backend-side transaction creation, status queries, and response-hash verification; integration keys and callbacks must be configured.
- SMS/OTP: Local development defaults to a mock SMS provider. A production messaging provider must be configured separately.
- Stellar: Optional receipt-integrity proof, not a payment processor. No successful blockchain proof is fabricated when the feature is disabled.
Known limitations and work before production
- Align the first-administrator registration policy across the implementation, tests, and documentation. The documented current backend behavior assigns ADMIN to the first successful registration. This bootstrap behavior needs a security review before exposing public registration.
- Resolve historical internal package names remaining from the previous project identity.
- Replace mock/test credentials and endpoints with verified production configuration.
- Protect the Stellar worker with private networking and/or service authentication.
- Review client-managed JWT storage and distributed rate limiting.
- Complete payment webhook testing, device testing, deployment review, and accessibility evaluation.
- Establish any required PCI DSS and WCAG compliance through formal assessment; neither is claimed here.
Project focus
SecurePay demonstrates end-to-end engineering across modern React interfaces, Spring Boot backend design, financial transaction workflows, mobile integration, database consistency, authorization, reconciliation, and optional cryptographic receipt verification.

Design principle: Payment confirmation, administrator review, and receipt integrity are distinct states. SecurePay records and presents them separately instead of treating any one as proof of the others.
