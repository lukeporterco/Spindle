# Minecraft 26.1.2 Imported Research Data

This folder is the Spindle-local import of the Minecraft `26.1.2` research data. It makes the current Minecraft Target Layer and SteelHook planning evidence durable inside this repository.

Minecraft version: `26.1.2`.

Mapping namespace: Yarn named unless an imported research file states otherwise.

## Canonical Paths

- `source/internals-analysis/`: preserved full research archive, including concepts, candidates, evidence, matrices, notes, runtime probe logs, raw JSONL, server log copies, and Spindle translation files.
- `refined/`: canonical Spindle-facing implementation planning data for future Minecraft Target Layer and SteelHook passes.
- `handoff/`: research-local handoff metadata preserved from the imported `spindle-ready-data` package.

The dedicated runtime probe evidence is included. The probe captured `59` JSONL records, observed all required probe groups, confirmed first server and level tick observations, showed `serverStarted` before first tick while `serverReady=false`, and preserved that integrated server parity is not confirmed.

This imported data guides future Target Layer and SteelHook planning. It does not implement APIs, hooks, SteelHook primitives, runtime transformations, Minecraft launch behavior, or compatibility behavior. Java mod execution is not sandboxed. SteelHook remains internal machinery, not a public arbitrary bytecode API.

## Future Planning Entry Points

- `refined/decision-matrix.md`
- `refined/runtime-confirmation-synthesis.md`
- `refined/target-layer-first-wave-plan.md`
- `refined/steelhook-primitive-roadmap.md`
- `refined/unresolved-questions-and-future-probes.md`
- `refined/source-to-refined-index.md`
- `refined/implementation-packets/`
- `source/internals-analysis/`

## Readiness Summary

- Ticking: ready for target implementation.
- Server lifecycle: ready for limited target implementation; it must not expose a universal public ready event.
- World and level: ready for limited guarded lookup/enumeration.
- Registries: ready for lookup-only target implementation.
- Commands: ready for SteelHook primitive planning, not final implementation; constructor-tail insertion proof is still required.
- Resources and datapacks: event-only or documentation until async/post-swap proof exists.
- Networking: event-only or documentation; phase-event-only if included, and raw packet APIs are rejected for the first wave.
- Data generation and assets: deferred.
