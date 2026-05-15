# Target-23: SteelHook 0.2 Primitive Boundary and Candidate Selection

This is an analysis-only SteelHook 0.2 primitive-boundary pass document for the Minecraft Target Layer. It records what Target-23 adds or decides while preserving the current Minecraft, SteelHook, and public API boundaries.

## Goal

Start SteelHook 0.2 by selecting one approved primitive boundary for the next generalization pass.

Target-23 should convert the completed SteelHook 0.1 spine into one bounded planning target that future passes can consume without claiming runtime transformation readiness.

## Inputs

- Target-7 hook patch plan.
- SteelHook 0.1 completion context from Target-10.
- Target-22 registry arc hardening handoff direction.

## Output Report

- Deterministic `minecraft-steelhook-0-2-primitive-boundary.json`.

The report records only candidate identity, boundary semantics, gate state, findings, and next direction. It does not serialize raw bytecode payloads, decoded instructions, or transformed class bytes.

## Selected Candidate

Target-23 approves exactly one current primitive shape:

- patch kind: `METHOD_ENTRY_STATIC_DISPATCH`
- owner: `net/minecraft/server/Main`
- member: `main`
- descriptor: `([Ljava/lang/String;)V`
- insertion offset: `0`
- dispatcher: `com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher.beforeMinecraftServerMain:()V`

That candidate is approved only as:

- `APPROVED_FOR_TARGET_24_CONTRACT_GENERALIZATION`

It is not approved as runtime-ready transformation machinery.

## Explicit Non-Goals

Target-23 does not:

- transform real Minecraft classes
- install hooks
- execute runtime dispatch
- expose public SteelHook or Modding API surfaces
- add new primitive kinds
- add `StackMapTable` rewriting
- claim Java mod execution sandboxing

## Why Target-24 Is Next

Target-23 proves that the existing Target-7 method-entry static-dispatch proof is still the only approved 0.2 primitive boundary.

That gives Target-24 one narrow next step:

- generalize the approved primitive into bounded contract and patch-plan machinery without yet claiming runtime transformation support

## What Remains Blocked

The following remain blocked after Target-23:

- real Minecraft runtime transformation
- broader primitive families such as method-exit, callsite, field, constructor, or return interception
- public Minecraft-facing modder APIs
- registry, command, resource, or lifecycle implementation work built on a runtime-ready transformer

## No Sandbox Claims

Java mod execution is still not sandboxed.

`minecraft-steelhook-0-2-primitive-boundary.json` is a deterministic boundary-selection report, not a safety verdict and not proof that runtime mod execution is isolated.
