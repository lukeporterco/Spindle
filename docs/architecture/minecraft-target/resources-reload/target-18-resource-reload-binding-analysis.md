# Target-18: Resource and Reload Binding Requirement Analysis

This is an analysis-only binding requirement analysis pass document for the Minecraft Target Layer. It records what Target-18 adds or decides while preserving the current Minecraft, SteelHook, and public API boundaries.

## Inputs

- Target-17 resource/reload symbol analysis.

## Output

- Deterministic `minecraft-resource-reload-binding-analysis.json`.
- `minecraft-resource-reload-arc-decision.json`.

## Capability Added Or Recorded

- Classifies binding and access requirements for discovered resource/reload candidates only.

### Preserved Source Notes

Target-18 is analysis-only.

It consumes Target-17 resource/reload symbol analysis.

It classifies binding and access requirements for discovered candidates.

It classifies many candidates and does not select one stable reload target.

It writes one deterministic report:

- `minecraft-resource-reload-binding-analysis.json`

It does not discover new symbols.
It does not inspect bytecode instructions, StackMapTable, mappings, decompiled source, live classes, online docs, resources, datapacks, generated JSON, registries, or command trees.
It does not bind reload timing or reload apply behavior.
It does not access resources or datapacks.
It does not generate data.
It does not mutate registries.
It does not expose public APIs.
It does not add SteelHook primitives.
It does not transform real Minecraft runtime artifacts.
Java mod execution is not sandboxed.

Target-18 classifies binding/access requirements only.
Target-18 does not make reload implementation ready.
Target-19 now separates runtime resource visibility from future offline data generation design.
Target-20 writes `minecraft-resource-reload-arc-decision.json`, closes the resource/reload arc for now, and records the decision to move next to Registry Bootstrap and Content Registration.

## Boundaries Preserved

- Does not select a stable reload target, make reload implementation ready, bind reload timing or apply behavior, access resources or datapacks, generate data, mutate registries, expose APIs, add SteelHook primitives, or imply sandboxing.

## Follow-On Direction

- Target-19 separates runtime resource visibility from future offline data generation lanes.
