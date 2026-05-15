# Target-8: Fixture-Only Bytecode Transformation

This is a fixture-only implementation proof pass document for the Minecraft Target Layer. It records what Target-8 adds or decides while preserving the current Minecraft, SteelHook, and public API boundaries.

## Inputs

- Target-7 dry-run patch plan.
- Target-6 method-code metadata.
- Fixture class bytes without `StackMapTable`.

## Output

- Deterministic transformed fixture class bytes and SHA-256 summaries for fixture tests.

## Capability Added Or Recorded

- Appends deterministic constant-pool entries and inserts one real `invokestatic` at bytecode offset `0` for the fixture-only target.

### Preserved Source Notes

Target-8 adds the first real SteelHook bytecode rewrite proof after Target-7 in `target-minecraft`.

It builds on the validated Target-7 dry-run patch plan for Minecraft `26.1.2` server `net.minecraft.server.Main.main(String[])`. Target-8 supports exactly one fixture-only transform candidate and produces transformed class bytes only for fixture tests.

### Exact Fixture-Only Transform

Target-8 applies exactly one inserted instruction:

- `sourcePatchId`: `target-7.minecraft.server.main.method-entry-dispatch-patch`
- `sourcePlacementId`: `target-5.minecraft.server.main.method-entry-placement`
- `sourceContractId`: `minecraft.26_1_2.server.main.entrypoint`
- `targetClass`: `net/minecraft/server/Main`
- `targetMethod`: `main`
- `targetDescriptor`: `([Ljava/lang/String;)V`
- `insertionOffset`: `0`
- inserted instruction: `invokestatic com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher.beforeMinecraftServerMain:()V`
- scope: `fixture-only`

For a passing fixture plan, Target-8:

- appends six deterministic constant-pool entries in the exact Target-7 order
- inserts one real `invokestatic` at bytecode offset `0`
- updates `constant_pool_count`
- updates `Code.code_length`
- updates the enclosing `Code` attribute length
- shifts exception-table `start_pc`, `end_pc`, and `handler_pc` by `+3`
- preserves `max_stack` and `max_locals`
- returns deterministic transformed class bytes plus SHA-256 summaries

### Fixture-Only Gate

Target-8 passes only when Target-7 already produced exactly one supported patch and:

- `transformReadyForFixtureOnly` is `true`
- `transformReadyForMinecraftRuntime` is `false`
- the target remains `net/minecraft/server/Main.main([Ljava/lang/String;)V`
- the insertion offset remains `0`
- the planned opcode remains one 3-byte `invokestatic`
- the dispatcher remains `com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher.beforeMinecraftServerMain:()V`

Target-8 rejects the selected method when it has `StackMapTable`. This pass does not rewrite stack-map frames.

### What Target-8 Does Not Do

Target-8 does not:

- transform real Minecraft runtime artifacts
- wire transformation into bootstrap or runtime classloading
- install hooks in production
- update `StackMapTable`
- rewrite `LineNumberTable`, `LocalVariableTable`, or `LocalVariableTypeTable`
- expose a public hook API
- add gameplay hooks
- use Mixin or Java agents
- imply Java mod execution is sandboxed

Target-8 is a fixture/test proof only. Target-9 reuses that validated rewrite inside fake-server bootstrap classloading, and Target-10 verifies that Target-8 was only consumed through that bounded fake-server path. Target-8 itself still does not transform real Minecraft runtime artifacts, rewrite `StackMapTable`, expose public APIs, add gameplay hooks, use Java agents or Mixin, or imply Java mod execution is sandboxed.

## Boundaries Preserved

- Does not transform real Minecraft runtime artifacts, wire transformation into real runtime classloading, install production hooks, update `StackMapTable`, expose APIs, add gameplay hooks, use Mixin or Java agents, or imply sandboxing.

## Follow-On Direction

- Target-9 reuses this validated rewrite inside fake-server bootstrap classloading.
