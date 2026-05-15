# Target-24: SteelHook 0.2 Contract and Patch-Plan Generalization

This is an analysis-only SteelHook 0.2 Target pass document for the Minecraft Target Layer. It records the bounded Target-24 step that generalizes the approved Target-23 primitive into reusable descriptors for Target-25.

## Goal

Turn the one approved Target-23 method-entry static-dispatch candidate into explicit Target-24 target, dispatcher, primitive contract, and generalized patch-plan descriptors.

Target-24 removes architectural ambiguity for later extraction work, but it does not make runtime transformation ready.

## Inputs

- Target-23 primitive boundary analysis.
- Target-7 hook patch plan.

Both inputs must still describe the same approved `net/minecraft/server/Main.main([Ljava/lang/String;)V` method-entry proof with `SteelHookDispatcher.beforeMinecraftServerMain:()V` at insertion offset `0`.

## Output Report

- Deterministic `minecraft-steelhook-0-2-contract-generalization.json`.

The report records only descriptor identity, contract shape, rewrite requirements already proven by Target-7, gate state, and next handoff direction.

It does not serialize decoded instructions, raw bytecode, transformed class bytes, or classfile payloads.

## Target Descriptor

Target-24 emits a bounded target descriptor for:

- owner: `net/minecraft/server/Main`
- binary name: `net.minecraft.server.Main`
- class entry: `net/minecraft/server/Main.class`
- member: `main`
- descriptor: `([Ljava/lang/String;)V`
- side: `server`
- insertion offset: `0`

The descriptor is method-entry-only and remains tied to the approved Target-7 placement and patch ids.

## Dispatcher Descriptor

Target-24 emits a bounded dispatcher descriptor for:

- owner: `com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher`
- method: `beforeMinecraftServerMain`
- descriptor: `()V`
- opcode: `invokestatic`
- opcode hex: `b8`
- instruction length: `3`

This remains an internal descriptor only. Target-24 does not expose SteelHook as public API.

## Primitive Contract

The Target-24 primitive contract records that the approved SteelHook 0.2 primitive remains:

- primitive kind: `METHOD_ENTRY_STATIC_DISPATCH`
- patch kind: `METHOD_ENTRY_STATIC_DISPATCH`
- patch mode: `STEELHOOK_0_2_CONTRACT_GENERALIZED_STATIC_DISPATCH_INVOKESTATIC`
- patch eligibility: `STEELHOOK_0_2_CONTRACT_READY_RUNTIME_CANDIDATE`
- insertion offset policy: `METHOD_ENTRY_OFFSET_ZERO_ONLY`

The contract is generalized for later extraction work, but `minecraftRuntimeTransformReady` remains `false`.

## Generalized Patch Plan

Target-24 emits a generalized patch-plan descriptor that carries forward the already-known Target-7 rewrite requirements:

- constant-pool rewrite requirement
- code rewrite requirement
- exception-table rewrite requirement
- `StackMapTable` rewrite requirement
- nested code attribute rewrite requirement
- line number and local variable table rewrite requirements
- branch and switch offset rewrite requirements

These values remain descriptor data only. Target-24 does not perform the rewrites.

## Why Target-25 Is Next

Target-24 exists so Target-25 can extract a runtime-safe method-entry transformer from a stable, explicit descriptor contract instead of from hardcoded Target-7 proof details.

Target-25 is the next pass because runtime transformation is still blocked until a bounded transformer is extracted and then separately validated.

## Explicit Non-Goals

Target-24 does not:

- transform real Minecraft classes
- install hooks
- execute runtime dispatch
- expose SteelHook as public API
- sandbox Java mod execution
- add a runtime classloader transformation path
- add a new bootstrap transformation mode
- add `StackMapTable` rewriting as an implementation capability
- broaden beyond the approved method-entry static-dispatch primitive

## What Remains Blocked

The following remain blocked after Target-24:

- Target-25 runtime-safe method-entry transformer extraction
- Target-26 runtime transformation enablement
- real Minecraft class transformation
- hook installation
- broader primitive families such as exit, field, constructor, redirect, or return interception

## No Sandbox Claims

Java mod execution is still not sandboxed.

`minecraft-steelhook-0-2-contract-generalization.json` is a deterministic planning report, not a runtime safety verdict and not proof that Minecraft transformation is available.
