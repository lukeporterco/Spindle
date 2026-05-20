# Server Lifecycle Implementation Packet

Minecraft version: 26.1.2
Status: `ready-for-target-implementation-limited`

## Purpose

Define limited lifecycle observations without inventing a universal public ready event.

## Source Evidence To Inspect

- `internals-analysis/concepts/server-lifecycle.md`
- `internals-analysis/candidates/lifecycle-candidates.md`
- `internals-analysis/evidence/server-lifecycle-evidence.md`
- `internals-analysis/evidence/lifecycle-confirmation-evidence.md`
- `internals-analysis/spindle-translation/lifecycle-spindle-implications.md`

## Runtime Evidence To Inspect

- `internals-analysis/notes/runtime-probe-dedicated-2026-05-19.jsonl`
- `internals-analysis/notes/runtime-probe-results.md`
- `internals-analysis/notes/runtime-probe-log.md`
- `internals-analysis/matrices/runtime-confirmation-status.md`

## Confirmed Touchpoints

- `MinecraftServer.loadLevel()` completion as post-load evidence.
- `DedicatedServer.initServer()` around dedicated `serverStarted`.
- First `MinecraftServer.tickServer` head/return and first readiness observation.
- Reload observation points.
- Shutdown notification, `stopServer`, `onServerExit`, and `runServer` return.

## Target Layer Meaning

Expose distinct observations for post-load, dedicated-started, first-tick/ready-observed, reload, stopping, and stopped semantics.

## SteelHook Requirements

method-entry static dispatch and method-exit static dispatch are enough for limited observations. Integrated-server lifecycle parity probe is still needed before widening claims.

## First Implementation Shape

Limited lifecycle events only. Preserve explicit ready semantics and dedicated-only limitation. Do not define a universal public ready event.

## Tests Future Implementation Should Require

- Event ordering test for post-load before first tick.
- Dedicated-started observation test showing it is not equivalent to first ready tick.
- Shutdown ordering test for stopping/stopped events.
- Negative documentation test or API review gate preventing a universal ready event.

## Documentation Requirements For Future Implementation

Document that dedicated `serverStarted` was observed before first tick while `serverReady=false`, and `isReady=true` was first observed after the first `tickServer` return. State that integrated server parity is unconfirmed.

## Non-Goals

- Universal public ready event.
- Integrated parity claim.
- Subsystem-specific shutdown ordering unless future APIs require it.
- Java sandbox promises; Java mod execution is not sandboxed.

## Risks

Lifecycle names can easily imply stronger readiness than the probe proved. Dedicated server only evidence must not become a universal guarantee.

## Open Questions

- What integrated server lifecycle points match the dedicated observations?
- Should stopping and stopped be enough, or does a later API need subsystem-specific shutdown order?
