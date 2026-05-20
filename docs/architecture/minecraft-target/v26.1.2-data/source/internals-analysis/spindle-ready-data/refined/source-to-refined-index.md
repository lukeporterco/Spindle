# Source To Refined Index

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

Use this index to jump from refined outputs back to original source research. The refined files are navigation and synthesis, not replacements for evidence.

| Original research source | Refined output that uses it | Use it for |
|---|---|---|
| `internals-analysis/README.md` | `../README.md`, `README.md` | Version, mapping namespace, folder purpose, source redistribution rules. |
| `internals-analysis/research-index.md` | `decision-matrix.md`, `target-layer-first-wave-plan.md` | Overall status, concept queue, completed findings, readiness gaps. |
| `internals-analysis/research-rules.md` | All refined files | Evidence discipline and naming constraints. |
| `internals-analysis/concepts/ticking.md` | `decision-matrix.md`, `implementation-packets/ticking.md` | Tick concept definition and rejected scheduled tick subsystem. |
| `internals-analysis/candidates/ticking-candidates.md` | `implementation-packets/ticking.md` | Candidate hook boundaries for server and level ticks. |
| `internals-analysis/evidence/ticking-confirmation-evidence.md` | `implementation-packets/ticking.md` | Source-backed tick touchpoints. |
| `internals-analysis/concepts/server-lifecycle.md`, `internals-analysis/candidates/lifecycle-candidates.md`, `internals-analysis/evidence/lifecycle-confirmation-evidence.md` | `implementation-packets/server-lifecycle.md`, `runtime-confirmation-synthesis.md` | Post-load, dedicated-started, ready observation, reload, stopping, stopped semantics. |
| `internals-analysis/concepts/world-and-level.md`, `internals-analysis/candidates/world-level-candidates.md`, `internals-analysis/evidence/world-level-confirmation-evidence.md` | `implementation-packets/world-and-level.md` | Guarded lookup/enumeration, level readiness, storage rejection. |
| `internals-analysis/concepts/registries.md`, `internals-analysis/candidates/registry-candidates.md`, `internals-analysis/evidence/registries-evidence.md`, `internals-analysis/evidence/registry-confirmation-evidence.md` | `implementation-packets/registries.md`, `decision-matrix.md` | lookup-only registry access and mutation rejection. |
| `internals-analysis/concepts/commands.md`, `internals-analysis/candidates/command-candidates.md`, `internals-analysis/evidence/commands-evidence.md`, `internals-analysis/evidence/commands-confirmation-evidence.md` | `implementation-packets/commands.md`, `steelhook-primitive-roadmap.md` | `Commands` construction, dispatcher ownership, constructor-tail proof needs. |
| `internals-analysis/concepts/resources-and-datapacks.md`, `internals-analysis/candidates/resource-reload-candidates.md`, `internals-analysis/evidence/resources-and-datapacks-evidence.md`, `internals-analysis/evidence/resource-reload-confirmation-evidence.md` | `implementation-packets/resources-and-datapacks.md`, `runtime-confirmation-synthesis.md` | Reload observation, async completion, datapack contribution deferral. |
| `internals-analysis/concepts/networking.md`, `internals-analysis/candidates/networking-candidates.md`, `internals-analysis/evidence/networking-evidence.md`, `internals-analysis/evidence/networking-confirmation-evidence.md` | `implementation-packets/networking.md` | Phase-event-only networking scope and raw packet rejection. |
| `internals-analysis/evidence/data-generation-and-assets-evidence.md` | `implementation-packets/data-generation-and-assets.md` | Deferred generated asset and datapack input design bridge. |
| `internals-analysis/matrices/concept-binding-matrix.md` | `decision-matrix.md` | Concept classifications, Target direction, boundaries, evidence IDs. |
| `internals-analysis/matrices/target-layer-first-wave.md` | `target-layer-first-wave-plan.md` | First-wave include/defer sequence and non-included areas. |
| `internals-analysis/matrices/steelhook-primitive-fit.md`, `internals-analysis/matrices/steelhook-gap-map.md` | `steelhook-primitive-roadmap.md`, implementation packets | Current primitive fit, gaps, rejected primitive ideas. |
| `internals-analysis/matrices/runtime-confirmation-status.md` | `runtime-confirmation-synthesis.md`, `decision-matrix.md` | Runtime-backed dedicated decisions and unresolved runtime gaps. |
| `internals-analysis/notes/runtime-probe-dedicated-2026-05-19.jsonl` | `runtime-confirmation-synthesis.md` | Raw 59-record dedicated probe evidence. |
| `internals-analysis/notes/runtime-probe-dedicated-2026-05-19-latest-log.txt` | `runtime-confirmation-synthesis.md` | Dedicated server log associated with the JSONL probe. |
| `internals-analysis/notes/runtime-probe-results.md`, `internals-analysis/notes/runtime-probe-log.md` | `runtime-confirmation-synthesis.md`, implementation packets | Parsed order, probe counts, availability snapshots, limitations. |
| `internals-analysis/notes/open-questions.md` | `unresolved-questions-and-future-probes.md` | Future probes and unresolved Target/SteelHook choices. |
| `internals-analysis/notes/rejected-candidates.md`, `internals-analysis/notes/contradictions-and-resolutions.md`, `internals-analysis/notes/version-specific-weirdness.md` | Implementation packet risks and non-goals | Preserved uncertainty, rejected paths, and version-specific cautions. |
| `internals-analysis/spindle-translation/` | All refined outputs | Existing Spindle-facing recommendations that this package condenses. |

## Inspection Rule

Start with `decision-matrix.md`, then inspect the specific implementation packet. Only return to broader source research when the packet points to a missing proof, a future probe, or a first-wave boundary that needs re-checking.
