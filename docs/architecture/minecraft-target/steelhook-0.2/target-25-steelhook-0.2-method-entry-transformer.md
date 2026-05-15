# Target-25: SteelHook 0.2 Method-Entry Transformer

## Goal

Target-25 extracts the reusable SteelHook 0.2 method-entry bytecode transformer from the earlier fixture-only path and proves that the approved Target-24 descriptor set can drive a bounded transformation.

## Inputs

- Target-7 hook patch plan
- Target-23 primitive boundary analysis
- Target-24 contract generalization analysis
- Target class bytes read from resolved runtime artifacts by `classEntryName`

## Output report

Target-25 writes `minecraft-steelhook-0-2-method-entry-transformer-result.json`.

The report is deterministic and records:

- the upstream milestone chain
- whether the Target-24 gate remained valid
- whether offline method-entry transformation occurred
- SHA-256 summaries, code lengths, constant-pool counts, and inserted instruction metadata
- whether Target-26 gated runtime transformation remains the next step

## Reusable method-entry transformer

Target-25 extracts reusable classfile rewrite machinery for one bounded primitive shape:

- target class `net/minecraft/server/Main`
- target method `main([Ljava/lang/String;)V`
- insertion offset `0`
- dispatcher `SteelHookDispatcher.beforeMinecraftServerMain()V`
- opcode `invokestatic`

The reusable transformer may produce transformed class bytes in memory for local verification.

## Offline-only transformation boundary

Target-25 is local/offline only.

It does not:

- write transformed class bytes into the Minecraft jar
- classload transformed bytes
- install hooks
- invoke runtime dispatch
- expose public API
- sandbox Java mod execution
- support StackMapTable rewriting

## What Target-25 proves

Target-25 proves that the approved Target-24 descriptor contract is sufficient to drive a reusable method-entry bytecode rewrite core without relying on Target-8 fixture-specific hardcoding.

## What Target-25 does not prove

Target-25 does not prove:

- gated runtime classloading
- real Minecraft runtime transformation
- dispatcher execution
- hook installation
- broader bytecode primitive support

`minecraftRuntimeTransformReady` remains `false`.

## Why Target-26 is next

Target-26 is the next pass because runtime classloading gates, runtime activation policy, and real-runtime boundary enforcement are separate concerns from offline byte rewriting.

## No sandbox claims

Target-25 does not change the project posture on Java mod execution safety. Producing transformed bytes in memory is not a sandbox claim and is not a runtime-readiness claim.
