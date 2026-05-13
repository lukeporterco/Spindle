# Target-17: Resource and Reload Symbol Discovery Analysis

Target-17 is analysis-only.

It consumes Target-1 interpreted metadata and Target-16 resource/reload boundary analysis.

It scans class names, package names, field names, field descriptors, method names, and method descriptors.

It uses fixed resource/reload discovery tokens.

It writes one deterministic report:

- `minecraft-resource-reload-symbol-analysis.json`

It reports candidates, not hook targets.

It does not select a stable reload target.
It does not bind reload timing or reload apply behavior.
It does not access resources or datapacks.
It does not generate data.
It does not mutate registries.
It does not expose public APIs.
It does not add SteelHook primitives.
It does not inspect bytecode instructions, StackMapTable, mappings, decompiled source, live classes, online docs, resources, datapacks, or generated JSON.
It does not transform real Minecraft runtime artifacts.
Java mod execution is not sandboxed.

Target-17 discovers candidate metadata symbols only.
Target-17 does not make reload implementation ready by itself.
Target-18 may classify binding/access strategy for discovered resource/reload symbols.
