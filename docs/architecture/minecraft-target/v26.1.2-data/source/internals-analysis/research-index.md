# Research Index

This file tracks the current state of Minecraft 26.1.2 internals research for Spindle.

## Current Status

Research state: confirmation and Spindle translation pass complete
Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted
Primary workspace: `../fabric-workspace`
Primary source scratch: `../.research-src/common` and `../.research-src/client`
Primary output target: Spindle Target Layer documentation and SteelHook planning
Handoff readiness: partially ready

## Research Rules Checkpoint

The folder avoids Minecraft source redistribution. Findings use class names, method names, line references, compact behavioral summaries, evidence IDs, and explicit uncertainty.

## Concept Queue

| Concept | Status | Classification | Handoff readiness |
|---|---|---|---|
| Server lifecycle | confirmed | first-wave limited | partial |
| Commands | confirmed | first-wave limited | partial |
| Registries | confirmed | first-wave limited | lookup-ready |
| Resources and datapacks | confirmed | SteelHook-gated | event-only partial |
| World and level | confirmed | first-wave limited | lookup-ready after lifecycle |
| Ticking | confirmed | first-wave stable | ready for first implementation pass |
| Networking | confirmed | first-wave limited | documentation/phase-only |
| Data generation and assets | added | research-gated | documentation-only |

## Candidate Queue

Candidate files now contain explicit `keep`, `reject`, or `uncertain` decisions, confidence, source-backed reason, Spindle implication, SteelHook primitive implication, and what evidence would change the decision.

## Matrix Queue

| Matrix | Status |
|---|---|
| `matrices/concept-binding-matrix.md` | updated |
| `matrices/confidence-matrix.md` | updated |
| `matrices/steelhook-primitive-fit.md` | updated |
| `matrices/target-layer-first-wave.md` | added |
| `matrices/steelhook-gap-map.md` | added |
| `matrices/runtime-confirmation-status.md` | added |

## Evidence Packets

| Evidence file | Status |
|---|---|
| `evidence/lifecycle-confirmation-evidence.md` | added by retrieval worker; orchestrator spot-checked |
| `evidence/commands-confirmation-evidence.md` | added by retrieval worker; orchestrator spot-checked |
| `evidence/registry-confirmation-evidence.md` | added by retrieval worker; orchestrator spot-checked |
| `evidence/resource-reload-confirmation-evidence.md` | added by retrieval worker; orchestrator spot-checked |
| `evidence/world-level-confirmation-evidence.md` | added by retrieval worker; orchestrator spot-checked |
| `evidence/ticking-confirmation-evidence.md` | added by retrieval worker; orchestrator spot-checked |
| `evidence/networking-confirmation-evidence.md` | added by retrieval worker; orchestrator spot-checked |
| `evidence/data-generation-and-assets-evidence.md` | added by retrieval worker; orchestrator spot-checked |

## Completed Findings

- Lifecycle has distinct post-load, dedicated-started, first-tick-ready, and shutdown points.
- Commands are owned by `Commands`, created through `ReloadableServerResources`, and replaced on reload.
- Registries have explicit layers and lookup surfaces; mutation remains out of first wave.
- Resource reload is asynchronous and post-swap updates occur after resource assignment.
- Loaded levels are map-backed on `MinecraftServer`; storage APIs are separate.
- Server and level ticks are clear first-wave event boundaries.
- Networking phase handoffs are source-backed, while raw packet interception remains rejected.
- Generated JSON assets are future datapack inputs, not runtime internals API in this pass.

## Blocked Questions

- Runtime confirmation for lifecycle ready and reload-complete safety.
- Exact command insertion timing before function compilation/player sync.
- Async/listener SteelHook primitive design.
- Registry mutation and contribution safety.
- Whether networking phase events belong in first-wave implementation.

## Next Read Targets

1. Runtime probes for dedicated/integrated startup, reload, and shutdown.
2. Command insertion timing around `Commands` construction and `ServerFunctionLibrary`.
3. Reload listener composition around `SimpleReloadInstance`.
4. Registry contribution and client sync consequences.
5. Fabric datagen entrypoint wiring only if asset generation becomes a near-term Spindle feature.

## Handoff Readiness

Partially ready for Spindle documentation.

Ready:

- First-wave classifications exist for every concept.
- Translation docs map source-backed evidence to Target/SteelHook implications.
- Ticking, lookup-only registries, and guarded world/level lookup are ready for narrow planning.

Not ready:

- SteelHook implementation requirements for async reload, constructor-tail command registration, and registry contribution.
- Final public Target Layer API names.
- Claims of runtime-confirmed lifecycle or reload safety.
