# Target-10: SteelHook Hardening Caboose

Target-10 completes SteelHook 0.1 in the narrow sense of verifying the internal hook spine built across Target-3 through Target-9.

It does not add a new transformation mode. It reuses the existing fake-server bootstrap transformation path, verifies the report chain, and writes `minecraft-steelhook-0.1-report.json`.

## SteelHook 0.1 Completion Chain

```text
known contract
-> method-entry placement
-> instruction-aware bytecode analysis
-> dry-run patch planning
-> fixture transform primitive
-> fake-server bootstrap transformation
-> dispatcher invocation
-> completion verification
```

The explicit completion check is driven by:

- `--minecraft-steelhook-0-1-check`
- `--minecraft-explain-steelhook-0-1-check`

It implies the existing Target-9 fake-server bootstrap path and the Target-7 planning chain. It does not imply Target-4 hook installation, real launch, baseline acquisition, real smoke, cache repair, or cache inspection.

## Verification Output

Target-10 writes one deterministic completion report:

- `minecraft-steelhook-0.1-report.json`

That report verifies, in fixed order:

1. Target-3 known-symbol hook contract validation
2. Target-5 method-entry placement
3. Target-6 instruction-aware bytecode analysis
4. Target-7 dry-run patch planning
5. Target-8 fixture transform primitive usage through the Target-9 path
6. Target-9 fake-server bootstrap transformation and dispatcher observation
7. Target-10 safety invariants

The report is schema `1`, milestone `Target-10`, `steelHookVersion: "0.1"`, pretty JSON, deterministic field order, serialized nulls, no timestamps, no new absolute paths, and no transformed class byte arrays.

## Safety Invariants

SteelHook 0.1 completion requires all of the following to remain true:

- fake-server runtime transformation happened
- bytecode was modified
- dispatcher invocation was observed exactly once
- Minecraft main was invoked
- bootstrap transformation remained enabled
- runtime classloader transformation remained enabled
- Target-7 still reported `transformReadyForMinecraftRuntime: false`
- no Target-4 hook installation result was written during the Target-10 check
- real Minecraft runtime transformation remained false
- public API exposure remained false
- Java agent, Mixin, remapping, and access widener usage remained false
- Java mod execution sandboxing remained false

## What Target-10 Does Not Mean

Target-10 does not mean SteelHook is broadly ready.

SteelHook 0.1 still does not:

- transform real Minecraft runtime artifacts
- support arbitrary hooks
- support method-exit, return, callsite, field, constructor, or class-initializer hooks
- rewrite `StackMapTable`
- expose public APIs
- add gameplay hooks
- use Mixin or Java agents
- imply Java mod execution is sandboxed

Target-10 is a hardening and verification caboose for one fake-server-only method-entry proof.
