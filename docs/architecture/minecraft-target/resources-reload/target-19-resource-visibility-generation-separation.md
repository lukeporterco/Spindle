# Target-19: Resource Visibility and Data Generation Separation Analysis

This is an analysis-only lane separation pass document for the Minecraft Target Layer. It records what Target-19 adds or decides while preserving the current Minecraft, SteelHook, and public API boundaries.

## Inputs

- Target-16 resource/reload boundary analysis.
- Target-18 binding analysis.

## Output

- Deterministic `minecraft-resource-visibility-generation-analysis.json`.
- `minecraft-resource-reload-arc-decision.json`.

## Capability Added Or Recorded

- Separates runtime reload timing, runtime resource visibility, and future offline data generation lanes.

### Preserved Source Notes

Target-19 is analysis-only.

It consumes Target-16 resource/reload boundary analysis and Target-18 binding analysis.

It separates runtime reload timing, runtime resource visibility, and future offline data generation.

It writes one deterministic report:

- `minecraft-resource-visibility-generation-analysis.json`

It does not inspect Target-1 metadata.
It does not discover new symbols.
It does not select a stable reload target.
It does not bind reload timing or apply behavior.
It does not access resources or datapacks.
It does not expose resource manager or datapack views.
It does not generate data.
It does not write generated JSON or generated files.
It does not mutate registries.
It does not expose public APIs.
It does not add SteelHook primitives.
It does not transform real Minecraft runtime artifacts.
Future offline data generation is intentionally separate from runtime reload.
Java mod execution is not sandboxed.

Target-19 separates lanes only.
Runtime resource visibility is not an API yet.
Offline data generation is not implemented yet.
Target-20 writes `minecraft-resource-reload-arc-decision.json`, closes the resource/reload arc for now, and records the decision to move next to Registry Bootstrap and Content Registration.

Target-20 does not implement registry behavior and does not design a SteelHook primitive.

Target-21 may begin Registry Bootstrap and Content Registration boundary analysis.

## Boundaries Preserved

- Does not inspect Target-1 metadata, discover new symbols, select a stable reload target, bind reload behavior, access resources or datapacks, generate data, write generated files, mutate registries, expose APIs, add SteelHook primitives, or imply sandboxing.

## Follow-On Direction

- Target-20 can close the resource/reload arc and choose the next concept family.
