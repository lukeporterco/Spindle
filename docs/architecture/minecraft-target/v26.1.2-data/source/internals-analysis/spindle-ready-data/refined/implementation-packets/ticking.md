# Ticking Implementation Packet

Minecraft version: 26.1.2
Status: `ready-for-target-implementation`

## Purpose

Define the first Target Layer tick events from clear server and level tick boundaries.

## Source Evidence To Inspect

- `internals-analysis/concepts/ticking.md`
- `internals-analysis/candidates/ticking-candidates.md`
- `internals-analysis/evidence/ticking-confirmation-evidence.md`
- `internals-analysis/spindle-translation/ticking-spindle-implications.md`

## Runtime Evidence To Inspect

- `internals-analysis/notes/runtime-probe-results.md`
- `internals-analysis/matrices/runtime-confirmation-status.md`

## Confirmed Touchpoints

- `MinecraftServer.tickServer(BooleanSupplier)` HEAD and RETURN.
- `ServerLevel.tick(BooleanSupplier)` HEAD and RETURN.
- First dedicated server tick and first per-level ticks were observed.

## Target Layer Meaning

Expose before/after server tick and before/after level tick events as the first Minecraft Target Layer implementation shape.

## SteelHook Requirements

method-entry static dispatch and method-exit static dispatch.

## First Implementation Shape

Use server tick and level tick HEAD/RETURN as first implementation shape. Keep event semantics observational and ordered; do not include tick cancellation or mutation.

## Tests Future Implementation Should Require

- Event order test for server tick before/after.
- Event order test for level tick before/after per loaded level.
- Dedicated runtime smoke validation against a small probe harness.
- Explicit no-event test for scheduled block/fluid tick subsystem.

## Documentation Requirements For Future Implementation

Document Minecraft version, Yarn named mapping namespace, dedicated-server-only runtime confirmation, and integrated server parity gap.

## Non-Goals

- Scheduled block/fluid tick subsystem.
- Tick cancellation.
- Tick mutation.
- Java sandbox promises; Java mod execution is not sandboxed.

## Risks

Integrated pause behavior is not confirmed. Event naming may overpromise if it implies all game subsystems have completed.

## Open Questions

- Should integrated pause behavior have explicit Target semantics?
- Are post-network or post-player tick boundaries future concepts?
