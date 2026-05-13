# Target-19: Resource Visibility and Data Generation Separation Analysis

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
Target-20 may act as a caboose/decision pass for the resource/reload arc and decide whether to move to registries, pause for SteelHook primitive design, or continue resource/reload analysis.
