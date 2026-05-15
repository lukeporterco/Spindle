# Target-27: SteelHook 0.2 Completion

## Goal

Target-27 closes SteelHook 0.2 by verifying the persisted Target-7 through Target-26 report chain and writing `minecraft-steelhook-0-2-report.json`.

## Inputs

- `minecraft-hook-contracts.json`
- `minecraft-hook-placement-plan.json`
- `minecraft-hook-bytecode-analysis.json`
- `minecraft-hook-patch-plan.json`
- `minecraft-steelhook-0-2-primitive-boundary.json`
- `minecraft-steelhook-0-2-contract-generalization.json`
- `minecraft-steelhook-0-2-method-entry-transformer-result.json`
- `minecraft-steelhook-0-2-gated-runtime-transformation-result.json`

Forbidden side-effect reports for the completion check path:

- `minecraft-hook-installation-result.json`
- `minecraft-fixture-transformation-result.json`
- `minecraft-hook-bootstrap-transformation-result.json`
- `minecraft-server-bootstrap-result.json`

## Output Report

- `minecraft-steelhook-0-2-report.json`

The report is deterministic, schema `1`, milestone `Target-27`, target `minecraft`, and `steelHookVersion: "0.2"`.

## Verified Chain

```text
Target-3 known contracts
-> Target-5 method-entry placement
-> Target-6 bytecode analysis
-> Target-7 patch plan
-> Target-23 primitive boundary
-> Target-24 contract generalization
-> Target-25 method-entry transformer
-> Target-26 gated runtime transformation
-> Target-27 completion verification
```

## Safety Invariants

Target-27 verifies that SteelHook 0.2 remains bounded to one approved method-entry static-dispatch primitive, one fixed target shape, one gated runtime classloader definition path, and no hook installation, Minecraft main invocation, server launch, dispatcher observation requirement, public API exposure, Java agent use, Mixin use, remapping, access wideners, sandbox claim, or raw transformed-byte serialization.

## Capability Boundaries

SteelHook 0.2 now supports one approved gated runtime classloader transformation path for `net.minecraft.server.Main`.

It still does not support:

- arbitrary bytecode editing
- Mixin replacement
- public SteelHook APIs
- Java mod execution sandboxing
- StackMapTable rewriting
- method-exit hooks
- cancellable hooks
- callsite redirects
- return-value interception
- field hooks
- constructor hooks
- multi-hook composition
- registries, commands, resources, lifecycle events, or Modding API features

## Failure Semantics

Target-27 fails closed when required reports are missing, malformed, incoherent, or when forbidden side-effect reports are present.

- Restore Target-26 when the gated runtime transformation proof is missing or failed.
- Restore the upstream SteelHook 0.2 chain when Target-7, Target-23, Target-24, or Target-25 drifted.

## Completion Meaning

SteelHook 0.2 is complete only in the narrow capability-ladder sense:

- one approved method-entry static-dispatch primitive
- one approved target
- one gated runtime classloader definition proof

## What SteelHook 0.2 Now Supports

- deterministic verification of the Target-7 through Target-26 report chain
- deterministic completion and handoff reporting
- one approved runtime classloader-defined transformed target class

## What SteelHook 0.2 Still Does Not Support

SteelHook 0.2 does not invoke Minecraft main, launch a server, install hooks, observe dispatcher execution, expose public APIs, claim sandboxing, or broaden into additional primitive families.

## Handoff After Target-27

The next SteelHook direction is SteelHook 0.3 planning for StackMapTable handling and additional insertion modes, especially method-exit and related bounded primitive expansion.

## No Sandbox Claims

SteelHook 0.2 completion is a deterministic capability and boundary report. It is not a sandbox verdict and does not mean Java mod execution is isolated or safe.
