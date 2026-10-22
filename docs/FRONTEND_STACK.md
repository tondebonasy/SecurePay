# Frontend framework migration

Web: React 18 and TypeScript. Tailwind v4 is integrated through the Vite plugin. Existing MUI components remain while individual screens migrate; Tailwind preflight is intentionally not enabled globally during this transition. New components use Radix Slot, class-variance-authority and tailwind-merge following the shadcn source-component model.

Implemented: Lucide icon set; Recharts daily financial chart with accessible text data; TanStack Table v9 transaction rows preserving server pagination/filtering; React Hook Form and Zod sign-in/registration validation; Motion reduced-motion-aware authentication entry; reusable Tailwind/Radix button.

Mobile: Expo 54, NativeWind v4, Expo-aligned Reanimated 4.1 and Worklets 0.5.1. Shared native button uses NativeWind and reusable variant composition. Existing Paper screens remain. This is a staged migration, not a complete replacement of every form and screen or a claim of production readiness.

Visual browser verification is blocked while local browser use is disabled in Work settings. Record completed build/test results separately; dependency installation alone is not validation.

Executed checks: web TypeScript and all 8 interaction tests pass; production build passes with analytics in a separate 352 kB chunk (102 kB gzip). Mobile TypeScript passes and Android Hermes export succeeds (1882 modules). No device rendering or browser screenshots were verified in this pass. Mobile dependency audit reported 68 findings before compatible remediation; production release remains blocked until the audit and live integration checks are resolved.


Compatible mobile dependency updates reduced findings from 68 to 46 (20 moderate, 26 high); no critical findings remain. Full remediation currently requires a breaking Expo/NativeWind/toolchain migration, so no forced major upgrades were applied. Visual review remains blocked by local browser access being disabled.


Final patch validation: Expo dependency compatibility check passes; Android export after compatible updates succeeds (1881 modules, approximately 5.21 MB Hermes bundle). Web final type check and production build pass; web dependency audit reports zero findings. Mobile audit remains 46 findings. The combined mobile command exits nonzero because npm audit reports remaining advisories; the export itself succeeds.

