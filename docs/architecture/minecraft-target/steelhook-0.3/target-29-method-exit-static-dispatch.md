# Target-29: SteelHook 0.3 Method-Exit Static Dispatch

## Goal

Target-29 adds `METHOD_EXIT_STATIC_DISPATCH` as the second SteelHook 0.3 primitive proof.

## What Target-29 Adds

- a bounded offline classfile rewrite that inserts `invokestatic SteelHookDispatcher.afterMinecraftServerMain:()V` immediately before supported normal return opcodes
- deterministic fail-closed scanning for unsupported control-flow and bytecode shapes
- a deterministic report: `minecraft-steelhook-0-3-method-exit-static-dispatch.json`

## Supported Boundary

Target-29 supports only this shape:

- target method: `net.minecraft.server.Main.main([Ljava/lang/String;)V`
- dispatcher: `com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher.afterMinecraftServerMain:()V`
- opcode: `invokestatic` (`0xb8`)
- inserted instruction length: `3`
- insertion point: immediately before supported normal return opcodes

Supported normal return opcodes are `ireturn`, `lreturn`, `freturn`, `dreturn`, `areturn`, and `return`, with descriptor matching based on the official JVM return categories.

## What Target-29 Rejects

Target-29 rejects:

- `StackMapTable` attributes
- exception table entries
- branch opcodes
- `tableswitch`
- `lookupswitch`
- `jsr`, `jsr_w`, `ret`, and `wide`
- `athrow` and exceptional exits
- synchronized methods
- constructors and class initializers
- malformed or unsupported instruction encodings

## What Target-29 Does Not Add

Target-29 does not:

- enable runtime classloading
- install hooks
- invoke Minecraft main
- execute the dispatcher
- observe exceptional exits
- pass or replace return values
- expose public APIs
- claim Java mod execution sandboxing

## Handoff

If Target-29 passes, the next direction is `move-to-target-30-generalized-transformer-gated-runtime-proof`.
