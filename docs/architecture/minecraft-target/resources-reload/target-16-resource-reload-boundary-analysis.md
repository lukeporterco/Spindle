# Target-16: Resource and Reload Boundary Analysis

This is an analysis-only resource/reload boundary pass document for the Minecraft Target Layer. It records what Target-16 adds or decides while preserving the current Minecraft, SteelHook, and public API boundaries.

## Inputs

- Minecraft Target Concept Roadmap.
- Coarse lifecycle anchor from the server lifecycle passes.

## Output

- Deterministic `minecraft-resource-reload-analysis.json`.
- `minecraft-resource-reload-symbol-analysis.json`.

## Capability Added Or Recorded

- Defines resource/reload concept boundaries and keeps the symbolic lifecycle start dispatch as a coarse anchor only.
- Separates runtime reload timing, resource visibility, and future offline data generation as still-unbound areas.

### Preserved Source Notes

Target-16 is analysis-only.

It names the Data, Resources, Reload, and Future Data Generation Target Layer boundaries without discovering Minecraft reload symbols or implementing runtime behavior.

It consumes Target-12 lifecycle dispatch planning only as a coarse anchor. The lifecycle anchor is not a reload hook.

### Output

Target-16 writes one deterministic report:

- `minecraft-resource-reload-analysis.json`

### Boundaries In This Pass

The following seven boundaries are always reported:

- `minecraft.resources.lifecycle_anchor`
- `minecraft.resources.reload.discovery`
- `minecraft.resources.reload.window`
- `minecraft.resources.reload.apply`
- `minecraft.resources.datapack.view`
- `minecraft.resources.resource_manager.view`
- `minecraft.resources.future_data_generation`

Only `minecraft.resources.lifecycle_anchor` can become available in this pass, and only when Target-12 planned `target-12.minecraft.server.lifecycle.starting.dispatch`.

That available boundary is only a coarse server lifecycle anchor. It is not a Minecraft resource reload hook.

Reload discovery, reload window, reload apply, datapack view, resource manager view, and future data generation remain declared unbound.

Future data generation is intentionally offline/future-facing and separate from runtime reload.

### What This Pass Does Not Add

No resource reload occurs.
No resource access occurs.
No datapack access occurs.
No data generation occurs.
No registry mutation occurs.
No public API is exposed.
No SteelHook primitive is added.
No real Minecraft runtime artifact is transformed.
Java mod execution is not sandboxed.

This pass does not add:

- reload symbol discovery
- interpreted metadata scanning for reload descriptors
- datapack mutation
- resource manager mutation
- generated JSON output
- registry integration or registry mutation
- public Minecraft resource, reload, or data generation APIs
- lifecycle callbacks or reload callbacks
- hook contracts for resource or reload classes
- hook placement
- bytecode analysis
- patch planning
- bootstrap transformation
- SteelHook completion behavior

Target-17 writes `minecraft-resource-reload-symbol-analysis.json` and performs candidate-only resource/reload symbol discovery.
Target-18 now classifies resource/reload binding strategy.
Target-19 now separates runtime resource visibility from future offline data generation design.

## Boundaries Preserved

- Does not discover Minecraft reload symbols, access resources or datapacks, generate data, mutate registries, expose APIs, add SteelHook primitives, add transformation support, or imply sandboxing.

## Follow-On Direction

- Target-17 can discover resource and reload metadata symbols without binding reload behavior.
