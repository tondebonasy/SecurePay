# SecurePay Admin Web

SecurePay Admin Web is a React and TypeScript operations console for authenticated payment teams. It provides one place to review transaction activity, inspect verification evidence, monitor operational risk, and reconcile payment work.

## Objectives

- Give operators a fast, readable view of payment health.
- Make transaction evidence and review states easy to inspect.
- Provide controlled access with a phone-first sign-in flow.
- Keep status, risk, and reconciliation decisions explicit and traceable.
- Present a responsive interface that works on desktop, tablet, and mobile widths.

## Included

- Phone-first authentication followed by password verification.
- Password visibility, Caps Lock feedback, generic sign-in errors, lockout feedback, and remembered-device sessions.
- Operations overview with term totals, receipts, collection rate, trends, and activity summaries.
- Transaction review and filtering.
- Verification and workflow review surfaces.
- Reports and settings navigation.
- Responsive sidebar and mobile navigation.
- Light, warm SecurePay visual system with navy, cream, gold, and semantic status colors.

## Technology

- React 18 and TypeScript.
- Vite for local development and production bundling.
- Material UI for accessible component primitives and theming.
- Tailwind CSS 4 utilities for layout and responsive composition.
- Radix Slot and class-variance-authority for composable UI primitives.
- React Hook Form and Zod for typed form validation.
- TanStack Table for transaction data grids.
- Recharts for operational analytics.
- Motion for restrained interface transitions.
- Lucide React for the icon system.
- Axios for API communication.
- Vitest and Testing Library for automated UI checks.

## Setup

Requirements: Node.js 22.12 or newer and a running SecurePay API.

```bash
cd admin-web
npm install
npm run dev
```

The development server runs at `http://localhost:5173`. Configure the API base URL through the environment file used by `src/util/api.ts` before connecting to a deployed service. For a production build:

```bash
npm run typecheck
npm test -- --pool=threads --maxWorkers=1
npm run build
npm run preview
```

## Use

1. Open the admin web URL.
2. Enter the phone number associated with the operator account.
3. Continue to the password step and complete authentication.
4. Review the overview for current payment health.
5. Use Transactions for detailed review, Verification for evidence workflows, Reports for analysis, and Settings for workspace controls.

Administrator provisioning is controlled by the API and database policy. The frontend does not grant roles or bypass server authorization. Configure the initial administrator through the reviewed provisioning procedure for the target environment, then disable or protect any public registration route according to that policy.

## Integrations

The admin web integrates with the SecurePay API for authentication, analytics, transactions, workflow verification, reports, and workspace settings. Payment provider calls belong behind the API boundary; browser code does not contain provider secrets. The app is prepared for deployment behind a standard HTTPS reverse proxy with environment-specific API configuration.

## Project structure

- `src/ui` — page-level screens and operations workspace views.
- `src/components` — reusable interface primitives.
- `src/util` — API and session utilities.
- `src/theme.ts` — Material UI theme tokens.
- `src/framework.css` and `src/polish.css` — responsive layout and SecurePay visual rules.
- `public` — brand assets used by the web build.

## Release expectations

Run typecheck, tests, and a production build before publishing. Review environment variables and API origin configuration separately for each deployment. Do not commit credentials, generated logs, local databases, or build output.
