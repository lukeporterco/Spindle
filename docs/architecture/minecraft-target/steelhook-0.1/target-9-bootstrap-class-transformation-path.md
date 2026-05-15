# Target-9: Bootstrap Class Transformation Path

This is a fake-server bootstrap implementation proof pass document for the Minecraft Target Layer. It records what Target-9 adds or decides while preserving the current Minecraft, SteelHook, and public API boundaries.

## Inputs

- Target-7 patch plan.
- Target-8 bytecode rewrite proof.
- Fake-server bootstrap classloader path.

## Output

- Deterministic `minecraft-hook-bootstrap-transformation-result.json`.
- Optional follow-on `minecraft-steelhook-0.1-report.json` when Target-10 runs.
- `minecraft-hook-patch-plan.json`.

## Capability Added Or Recorded

- Applies the validated fixture rewrite in fake-server bootstrap classloading for `net.minecraft.server.Main`.
- Observes dispatcher invocation through the normal bootstrap path.

### Preserved Source Notes

Target-9 wires the validated Target-7 patch plan and the Target-8 bytecode rewrite proof into bootstrap classloading, but only for fake-server bootstrap execution.

Target-10 now sits behind this path as the explicit SteelHook 0.1 completion verifier. Target-9 remains the execution step; Target-10 verifies the full report chain after the child bootstrap process completes.

It supports exactly one bootstrap transform target:

- `binaryName`: `net.minecraft.server.Main`
- `internalName`: `net/minecraft/server/Main`
- `sourcePatchId`: `target-7.minecraft.server.main.method-entry-dispatch-patch`
- dispatcher: `com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher.beforeMinecraftServerMain:()V`
- `mode`: `bootstrap-fake-server-method-entry-transform`
- scope: `bootstrap-fake-server-only`

### Bootstrap Path

When `--minecraft-bootstrap-transform-hooks --minecraft-bootstrap-fake-server` is used, Target-9:

- generates the normal Target-7 `minecraft-hook-patch-plan.json`
- fingerprints that patch plan and passes it into the bootstrap child JVM
- verifies the frozen runtime, boundary, integration, execution, and hook patch plans
- loads only `net.minecraft.server.Main` child-first in the bootstrap runtime classloader
- transforms that class before definition
- invokes `Main.main(String[])` through the normal bootstrap path
- records dispatcher invocation and writes `minecraft-hook-bootstrap-transformation-result.json`
- may be followed by Target-10 completion verification writing `minecraft-steelhook-0.1-report.json`

Every other class keeps the existing parent-first bootstrap classloading behavior.

### Guard Rails

Target-9 is fake-server only:

- it requires `--minecraft-bootstrap-fake-server`
- it rejects real Minecraft bootstrap execution
- it must not be combined with Target-4 `--minecraft-install-hooks`
- it keeps `transformReadyForMinecraftRuntime: false`

### What Target-9 Does Not Do

Target-9 does not:

- transform real Minecraft runtime artifacts
- add a general transformer framework
- rewrite `StackMapTable`
- use Java agents, Mixin, ASM, Byte Buddy, remapping, or access wideners
- expose a public hook API
- add gameplay hooks
- imply Java mod execution is sandboxed

Target-9 proves the bootstrap application path only. It does not claim runtime-safe transformation readiness for real Minecraft.

## Boundaries Preserved

- Fake-server only.
- Does not transform real Minecraft runtime artifacts, add a general transformer framework, rewrite `StackMapTable`, use Java agents, Mixin, ASM, Byte Buddy, remapping, or access wideners, expose public APIs, add gameplay hooks, or imply sandboxing.

## Follow-On Direction

- Target-10 verifies the full SteelHook 0.1 report chain after bootstrap execution.
