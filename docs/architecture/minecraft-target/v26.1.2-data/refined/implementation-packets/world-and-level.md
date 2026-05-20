# World And Level Implementation Packet

Minecraft version: 26.1.2
Status: `ready-for-target-implementation-limited`

## Purpose

Define guarded world/level lookup and enumeration after lifecycle readiness.

## Source Evidence To Inspect

- `internals-analysis/concepts/world-and-level.md`
- `internals-analysis/candidates/world-level-candidates.md`
- `internals-analysis/evidence/world-and-level-evidence.md`
- `internals-analysis/evidence/world-level-confirmation-evidence.md`
- `internals-analysis/spindle-translation/world-and-level-spindle-implications.md`

## Runtime Evidence To Inspect

- `internals-analysis/notes/runtime-probe-results.md`
- `internals-analysis/matrices/runtime-confirmation-status.md`

## Confirmed Touchpoints

- Level construction and map-backed loaded levels.
- `MinecraftServer.getLevel(...)`, `getAllLevels()`, `levelKeys()`, and overworld lookup surfaces.
- Dedicated probe availability snapshots for levels and overworld.

## Target Layer Meaning

Expose guarded lookup/enumeration only, preferably behind lifecycle/ready checks.

## SteelHook Requirements

direct lookup binding where no hook is needed. Optional lifecycle event dependency may use method-exit from load/prepare boundaries.

## First Implementation Shape

Guarded lookup/enumeration after readiness. Choose either a snapshot or guarded live view before implementation.

## Tests Future Implementation Should Require

- Lookup unavailable or guarded before readiness.
- Lookup succeeds after readiness for expected loaded levels.
- Enumeration remains bounded to loaded server levels.
- Storage/persistence APIs are absent.

## Documentation Requirements For Future Implementation

Document readiness gating, dedicated-server-only runtime confirmation, and snapshot versus live-view behavior.

## Non-Goals

- Storage/persistence APIs.
- `LevelStorageSource` runtime world API.
- World mutation APIs.
- Java sandbox promises; Java mod execution is not sandboxed.

## Risks

Live enumeration can expose mutable Minecraft internals if not wrapped carefully. Integrated server parity remains unconfirmed.

## Open Questions

- Snapshot or guarded live view?
- Should first tick be required, or is post-prepare enough?
