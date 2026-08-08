# Architecture Decision Records

Architecture Decision Records (ADRs) preserve decisions that are expensive or unsafe to rediscover from source code alone. They are not a changelog: each record explains context, the chosen approach, alternatives, and constraints future changes must preserve.

| ADR | Decision | Status |
|---|---|---|
| [0001](0001-dual-overlay-hosts.md) | Use normal and accessibility overlay hosts | Accepted |
| [0002](0002-touch-through-opacity-policy.md) | Enforce Android touch-through opacity policy | Accepted |
| [0003](0003-single-overlay-runtime-state.md) | Keep one actual runtime state and one shown overlay | Accepted |
| [0004](0004-accessibility-service-boundary.md) | Keep accessibility usage render-only | Accepted |
| [0005](0005-shizuku-accessibility-toggle.md) | Make Shizuku an explicit, scoped convenience | Accepted |
| [0006](0006-foreground-service-lifecycle.md) | Use a foreground service with deliberate stop/recovery semantics | Accepted |

New ADRs should use the structure: status, date, context, decision, constraints/invariants, alternatives, consequences, implementation, and validation.
