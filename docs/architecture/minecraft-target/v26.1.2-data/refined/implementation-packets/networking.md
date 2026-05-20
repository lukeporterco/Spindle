# Networking Implementation Packet

Minecraft version: 26.1.2
Status: `event-only-or-documentation`

## Purpose

Keep networking as phase-event-only documentation unless a later Target plan selects it.

## Source Evidence To Inspect

- `internals-analysis/concepts/networking.md`
- `internals-analysis/candidates/networking-candidates.md`
- `internals-analysis/evidence/networking-evidence.md`
- `internals-analysis/evidence/networking-confirmation-evidence.md`
- `internals-analysis/spindle-translation/networking-spindle-implications.md`

## Runtime Evidence To Inspect

- No dedicated runtime networking phase probe was run in this pass.
- Use `internals-analysis/matrices/target-layer-first-wave.md` and original networking evidence before selection.

## Confirmed Touchpoints

- Login/configuration/play/reconfiguration phase handoff methods are source-backed.
- Raw packet send interception was rejected for first wave.

## Target Layer Meaning

Networking may expose server-side phase events only if later selected. Otherwise it should remain documentation-only.

## SteelHook Requirements

method-entry static dispatch and method-exit static dispatch for phase events if selected. Raw packet interception is rejected for first wave.

## First Implementation Shape

Phase events only if later selected. No raw packet APIs.

## Tests Future Implementation Should Require

- Phase transition event order tests.
- No public raw packet interception API.
- Scope test excluding client-side networking unless separately researched.

## Documentation Requirements For Future Implementation

Document that networking was not runtime-probed in this dedicated pass and that integrated server parity is not confirmed.

## Non-Goals

- Raw packet APIs.
- Packet mutation.
- Client-side networking.
- Compatibility claims for Fabric, Mixin, Forge, NeoForge, Quilt, Paper, Bukkit, or Sponge.
- Java sandbox promises; Java mod execution is not sandboxed.

## Risks

Networking scope can expand quickly into transport and protocol mutation. Keep first-wave scope phase-event-only or documentation-only.

## Open Questions

- Should networking phase events be included in first wave?
- Which phase transitions are stable enough for public Target semantics?
