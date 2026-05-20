# Refined Spindle-Facing Synthesis

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

This directory is the Spindle-facing synthesis for the local Minecraft 26.1.2 research handoff. It is derived from the original `internals-analysis/` files and does not modify the Spindle repository.

Future Spindle or Codex prompts should inspect these files first:

1. `decision-matrix.md` for concept readiness and first-wave boundaries.
2. `runtime-confirmation-synthesis.md` for dedicated-server-only runtime facts and limitations.
3. `target-layer-first-wave-plan.md` for implementation ordering.
4. `steelhook-primitive-roadmap.md` for current, missing, and rejected SteelHook primitive needs.
5. `implementation-packets/` for concept-by-concept implementation guidance.
6. `unresolved-questions-and-future-probes.md` before widening scope.
7. `source-to-refined-index.md` to jump back to original evidence.

## Global Boundaries

- Dedicated runtime probe evidence is dedicated-server-only.
- Integrated server parity is not confirmed.
- Ticking is ready for first implementation.
- Lifecycle is limited and must not expose a universal ready event.
- World/level access is guarded lookup/enumeration only.
- Registries are lookup-only.
- Commands are plausible but still require constructor-tail insertion proof.
- Resources/datapacks are event-only or documentation until async/post-swap proof exists.
- Networking is phase-event-only if included, with raw packet APIs rejected for first wave.
- Data generation/assets are deferred.
- Java mod execution is not sandboxed.
- SteelHook remains internal machinery, not a public arbitrary bytecode API.
