# SecurePay frontend review

## Scope and evidence

This review covers the admin web login and operations workspace plus the mobile authentication and transactions screens. Source review was completed against the current workspace. Live visual capture is blocked: the in-app browser returns `ERR_CONNECTION_TIMED_OUT` for the local Vite server, and the product-design user-context preflight could not run because the configured Python executable is unavailable. The findings below therefore separate source evidence from visual claims; no screenshot-based accessibility sign-off is implied.

## Findings

### P1 — Mobile registration is a separate visual language

**File:** `mobile/src/features/auth/screens/RegisterScreen.tsx`

The registration screen uses large inline style objects, decorative blobs, shimmer animation, hand-built shield/profile illustrations, and badge-like claims such as “Encrypted”, “Private by default”, and “Auditable”. It does not share the restrained login composition or the admin design tokens. This makes the product feel assembled from unrelated templates and creates unsupported security claims in a client-facing flow.

**Required correction:** refactor registration around the same surface, spacing, typography, artwork treatment, and functional copy as login. Use shared components and a `StyleSheet`; remove decorative claims and animation that do not communicate state.

### P1 — Admin theme tokens drift from the approved palette

**File:** `admin-web/src/theme.ts`

The MUI theme used blue/cyan and cool-grey tokens while the current product direction is cream, deep navy, muted gold, and semantic green/amber/red. Components that are not overridden by `polish.css` can therefore render in a visibly different system.

**Status:** corrected to the approved palette in source. Build and tests must be rerun before release.

### P1 — Domain boundaries are weak in the operations workspace

**File:** `admin-web/src/ui/OperationsWorkspace.tsx` (approximately lines 55, 371, 532, 746, 782)

Network errors, analytics state, transactions, and workflow records use `any`. This removes compiler protection at the most sensitive boundary in a payment operations product and makes UI regressions easy to ship.

**Required correction:** define `ApiError`, `AnalyticsResponse`, `Transaction`, and `WorkflowRecord` types; validate API payloads at the client boundary; keep view state typed to those contracts.

### P2 — Hard-coded visual values bypass the design system

**Files:** `admin-web/src/ui/OperationsWorkspace.tsx`, `admin-web/src/ui/FinancialChart.tsx`, mobile auth and transactions screens.

Repeated color literals, inline layout objects, and ad-hoc shadows make responsive tuning and dark-mode support expensive. The requested Tailwind/shadcn direction is not yet represented consistently in the admin or mobile implementation.

**Required correction:** centralize color, spacing, radius, and elevation tokens; use shared primitives for fields, buttons, cards, navigation, and status badges.

### P2 — Mobile navigation and screen props are under-typed

**Files:** `mobile/src/utils/logger.ts`, `mobile/src/services/apiClient.ts`, navigation screens, `mobile/src/types/api.ts`

Several paths use `any`, including navigation and API detail payloads. Development logging is guarded in places, but the release build should be checked to ensure request data and error details cannot leak into production logs.

**Required correction:** use typed navigation param lists, `unknown` for untrusted error details, and a release logger that strips or redacts request data.

## Flow review checklist

| Flow | Source review | Visual capture |
| --- | --- | --- |
| Admin phone sign-in | Reviewed | Blocked by local browser timeout |
| Admin password/error step | Reviewed | Blocked by local browser timeout |
| Admin overview | Reviewed | Blocked by local browser timeout |
| Mobile sign-in | Reviewed | Blocked by local browser timeout |
| Mobile registration | Reviewed | Blocked by local browser timeout |
| Mobile transactions home | Reviewed | Blocked by local browser timeout |

## Release gate

The frontend should not be presented as a finished client handoff until the mobile registration refactor is complete, the remaining `any` boundaries are typed, and a browser-accessible build has been captured for responsive and accessibility review. The admin palette correction is in place; rerun typecheck, tests, and production builds after the final UI changes.
