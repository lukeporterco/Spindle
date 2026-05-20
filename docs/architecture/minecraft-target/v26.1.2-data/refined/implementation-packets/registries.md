# Registries Implementation Packet

Minecraft version: 26.1.2
Status: `ready-for-target-implementation-limited`

## Purpose

Define lookup-only registry access for first-wave Target Layer planning.

## Source Evidence To Inspect

- `internals-analysis/concepts/registries.md`
- `internals-analysis/candidates/registry-candidates.md`
- `internals-analysis/evidence/registries-evidence.md`
- `internals-analysis/evidence/registry-confirmation-evidence.md`
- `internals-analysis/spindle-translation/registries-spindle-implications.md`

## Runtime Evidence To Inspect

- `internals-analysis/notes/runtime-probe-results.md`
- `internals-analysis/matrices/runtime-confirmation-status.md`

## Confirmed Touchpoints

- `RegistryAccess`, `HolderLookup.Provider`, and related lookup surfaces.
- Dedicated availability snapshots showed registry lookups available during startup and readiness observations.

## Target Layer Meaning

Expose lookup-only wrappers. Keep contribution and mutation out of first wave.

## SteelHook Requirements

direct lookup binding where no hook is needed. No registry mutation primitives.

## First Implementation Shape

lookup-only registry access after a documented lifecycle/readiness boundary.

## Tests Future Implementation Should Require

- Lookup-only access test for known registry keys.
- No mutation/write methods in first-wave public API.
- Guarding behavior before readiness if lookup is exposed through server context.

## Documentation Requirements For Future Implementation

Document lookup-only scope, registry mutation rejection, and that dedicated probe evidence does not prove integrated server parity.

## Non-Goals

- Registry mutation.
- Registry contribution.
- Reloadable registry writes.
- Post-freeze writes.
- Java sandbox promises; Java mod execution is not sandboxed.

## Risks

Lookup availability can be mistaken for safe mutation. Reloadable registry contribution has client sync and validation consequences not solved here.

## Open Questions

- Should `RegistryAccess`, `HolderLookup`, and `HolderGetter` be distinct public concepts or wrapped as one lookup concept?
- What future design, if any, would reopen registry mutation/contribution?
