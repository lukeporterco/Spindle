# Target-30: SteelHook 0.3 Generalized Transformer Gated Runtime Proof

## Goal

Target-30 proves that the two approved SteelHook 0.3 primitives can each pass through the gated runtime classloader definition path.

The proof uses `Class.forName("net.minecraft.server.Main", false, runtimeClassLoader)` so the transformed target class is defined without intentional initialization. Target-30 runs two isolated runtime classloader sessions:

- one for `METHOD_ENTRY_STATIC_DISPATCH`
- one for `METHOD_EXIT_STATIC_DISPATCH`

## What Target-30 Adds

- a generalized SteelHook 0.3 gated runtime transformer that accepts one approved primitive spec per instance
- two isolated runtime fixture jars containing only controlled `net.minecraft.server.Main` fixture class bytes
- a deterministic report: `minecraft-steelhook-0-3-generalized-transformer-gated-runtime-proof.json`

## Proof Boundary

Target-30 proves only class definition through `MinecraftRuntimeClassLoader`.

For both isolated sessions, Target-30 proves:

- the transformed class was defined by the gated runtime classloader
- `net.minecraft.server.Main` was not intentionally initialized
- Minecraft main was not invoked
- Minecraft server launch stayed disabled
- hook installation stayed disabled
- the inserted dispatcher call was not executed

## What Target-30 Does Not Add

Target-30 does not:

- compose method-entry and method-exit hooks into one transformed class
- add multi-hook ordering, priorities, or conflict resolution
- install hooks
- invoke Minecraft main
- launch Minecraft
- execute either dispatcher
- expose public APIs
- mutate Minecraft jars
- claim Java mod execution sandboxing

Target-30 still uses controlled fixture class bytes rather than mutated Minecraft runtime jars.

## Handoff

If Target-30 passes, the next direction is `move-to-target-31-steelhook-0-3-completion`.
