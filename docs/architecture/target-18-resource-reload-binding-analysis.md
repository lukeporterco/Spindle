# Target-18: Resource and Reload Binding Requirement Analysis

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
Target-19 may separate runtime resource visibility from future offline data generation design.
