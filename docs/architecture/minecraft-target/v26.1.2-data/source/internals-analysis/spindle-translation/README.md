# Spindle Translation

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted
Status: confirmation-pass synthesis

This folder translates source-backed Minecraft internals research into narrow Spindle-facing implications. It is not final Target Layer API design and it is not SteelHook implementation work.

## Classification Summary

| Concept | Classification | First-wave recommendation |
|---|---|---|
| Server lifecycle | first-wave limited | include as dedicated/shared split with readiness caveats |
| Commands | first-wave limited | include registration-time and reload-aware only |
| Registries | first-wave limited | include as lookup-only |
| Resources and datapacks | SteelHook-gated | event-only documentation now; defer contribution |
| World and level | first-wave limited | include guarded lookup/enumeration after readiness |
| Ticking | first-wave stable | include server and level tick events |
| Networking | first-wave limited | phase-event-only or documentation-only |
| Data generation and assets | research-gated | document as asset/datapack input, not runtime API |

## Evidence Discipline

Use evidence IDs from `internals-analysis/evidence/`. Do not copy Minecraft source into Spindle docs. Treat all recommendations here as non-final until a Target implementation pass chooses exact API names.
