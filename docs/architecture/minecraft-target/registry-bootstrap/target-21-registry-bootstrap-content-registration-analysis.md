# Target-21: Registry Bootstrap and Content Registration Analysis

This is an analysis-only registry boundary pass document for the Minecraft Target Layer. It records what Target-21 adds or decides while preserving the current Minecraft, SteelHook, and public API boundaries.

## Inputs

- Minecraft Target Concept Roadmap.
- Target-20 registry handoff decision.

## Output

- Deterministic registry bootstrap/content registration analysis report.
- `minecraft-registry-bootstrap-analysis.json`.

## Capability Added Or Recorded

- Begins the registry bootstrap and content registration concept family after the resource/reload caboose decision.

### Preserved Source Notes

Target-21 is analysis-only.

It consumes Target-1 interpreted metadata and Target-20 registry handoff.

It writes `minecraft-registry-bootstrap-analysis.json`.

It combines boundary naming, metadata discovery, and access classification for the registry concept family.

It does not implement registries, content registration, public APIs, callbacks, hooks, runtime mutation, data generation, resource access, datapack access, mappings, decompiled source inspection, SteelHook primitive design, real Minecraft transformation, or sandboxing.

Target-22 completes the next handoff with invariant hardening and synthesis in [Target-22: Registry Arc Hardening and Synthesis](target-22-registry-arc-hardening-synthesis.md).

## Boundaries Preserved

- Does not implement registry behavior, mutate registries, expose content registration APIs, add runtime callbacks, add SteelHook primitives, transform Minecraft, or imply sandboxing.

## Follow-On Direction

- Target-22 hardens and synthesizes the registry arc boundary.
