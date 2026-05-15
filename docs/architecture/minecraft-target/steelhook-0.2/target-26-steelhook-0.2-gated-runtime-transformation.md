# Target-26: SteelHook 0.2 Gated Runtime Transformation

## Goal

Target-26 proves that the approved SteelHook 0.2 method-entry transformer can run through Spindle's runtime classloader path against the single approved Minecraft target class.

It classloads the transformed `net.minecraft.server.Main` bytes through `MinecraftRuntimeClassLoader`, writes `minecraft-steelhook-0-2-gated-runtime-transformation-result.json`, and prepares Target-27 completion verification.

## Inputs

- Target-7 hook patch plan
- Target-23 primitive boundary analysis
- Target-24 contract generalization analysis
- Target-25 method-entry transformer result
- Minecraft server runtime plan and resolved runtime classpath

## Output Report

Target-26 writes `minecraft-steelhook-0-2-gated-runtime-transformation-result.json`.

The report is deterministic and records:

- gate state and failure reason
- runtime classloading attempt/success state
- target class definition state
- original/transformed class and code hashes
- constant-pool and inserted instruction metadata
- classloading audit summary
- Target-27 eligibility

It does not serialize raw transformed class bytes.

## Gated Runtime Classloader Path

Target-26 enables the gated runtime classloader transformation path for the single approved method-entry static-dispatch primitive.

The runtime path:

1. validates the Target-25 offline-only handoff
2. builds deterministic runtime classpath URLs from the runtime plan
3. loads `net.minecraft.server.Main` with initialization disabled
4. rewrites the class bytes in memory during class definition
5. defines the transformed class through `MinecraftRuntimeClassLoader`

Target-26 may define transformed class bytes through the runtime classloader, but it does not write transformed class bytes into the Minecraft jar.

## What Target-26 Proves

- the approved Target-24 descriptor shape still matches the runtime target
- the Target-25 offline transform hashes still match the runtime transform path
- the transformed target class can be defined through the runtime classloader
- the gated runtime transformation path is ready for Target-27 completion verification

## What Target-26 Does Not Prove

Target-26 does not:

- launch Minecraft
- invoke `net.minecraft.server.Main.main`
- launch a real Minecraft server
- install hooks
- invoke runtime dispatch
- observe dispatcher execution
- expose public API
- sandbox Java mod execution
- support StackMapTable rewriting

## Why Minecraft Main Is Not Invoked

Target-26 is a class-definition proof, not a server-execution pass.

The target class is loaded with initialization disabled so the runtime path can prove transformed definition without turning the pass into a real launch or hook-execution milestone.

## Why Target-27 Is Next

Target-26 stops after successful transformed definition through the runtime classloader.

Target-27 is the completion and handoff pass that should verify the full SteelHook 0.2 chain, decide the caboose state, and document the next architectural direction.

## No Sandbox Claims

Target-26 does not claim that Java mod execution is sandboxed or generally safe.

`spindle.security-report.json` and SteelHook reports remain deterministic trust-boundary artifacts, not runtime safety guarantees.
