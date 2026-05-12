# Target-7: Injection Patch Planning Dry-Run

Target-7 adds the first SteelHook patch-planning layer after Target-6 in `target-minecraft`.

It builds on the validated Target-5 placement and Target-6 bytecode analysis for Minecraft `26.1.2` server `net.minecraft.server.Main.main(String[])`. Target-7 plans exactly one internal method-entry static-dispatch patch candidate and writes `minecraft-hook-patch-plan.json`.

## Exact Planned Patch

Target-7 supports exactly one dry-run patch candidate:

- `id`: `target-7.minecraft.server.main.method-entry-dispatch-patch`
- `sourcePlacementId`: `target-5.minecraft.server.main.method-entry-placement`
- `sourceContractId`: `minecraft.26_1_2.server.main.entrypoint`
- `sourceBytecodeAnalysisMilestone`: `Target-6`
- `catalogId`: `minecraft-26.1.2-server-known-symbols`
- `kind`: `METHOD_ENTRY_STATIC_DISPATCH`
- `mode`: `dry-run-static-dispatch-invokestatic`
- `ownerInternalName`: `net/minecraft/server/Main`
- `memberName`: `main`
- `descriptor`: `([Ljava/lang/String;)V`
- `insertionOffset`: `0`
- `required`: `true`

The planned instruction shape is one future `invokestatic`:

- dispatcher owner: `com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher`
- dispatcher method: `beforeMinecraftServerMain`
- dispatcher descriptor: `()V`
- opcode mnemonic: `invokestatic`
- opcode hex: `b8`
- symbolic inserted bytes: `b8 ?? ??`
- planned instruction length: `3`
- planned stack delta: `0`
- required max-stack increase: `0`

## Planned Rewrite Impact

Target-7 reports only what a future transform would need to rewrite:

- symbolic constant-pool entries for the dispatcher class, method name, descriptor, name-and-type, and methodref
- `codeLengthDelta` of `3`
- branch target offset adjustment summaries
- switch target offset adjustment summaries
- exception-table field adjustments for `start_pc`, `end_pc`, and `handler_pc`
- nested `Code` attribute impact summaries for `StackMapTable`, `LineNumberTable`, `LocalVariableTable`, and `LocalVariableTypeTable`
- `stackMapTableRewriteRequired: true` when Target-6 reported a `StackMapTable`, otherwise `false`

`transformReadyForFixtureOnly` may be true when Target-6 succeeded and bytecode offset `0` is a valid instruction boundary. `transformReadyForMinecraftRuntime` remains false in all Target-7 reports.

## What Target-7 Does Not Do

Target-7 does not:

- generate transformed class bytes
- rewrite the constant pool
- rewrite the `Code` attribute or bytecode array
- rewrite exception tables
- update `StackMapTable`
- rewrite `LineNumberTable`, `LocalVariableTable`, or `LocalVariableTypeTable`
- inject, transform, patch, remap, or instrument Minecraft classes
- install hooks
- invoke hooks
- expose a public hook API
- add gameplay hooks
- use Mixin or Java agents
- imply Java mod execution is sandboxed

Target-7 remains planning-only. Target-8 is the earliest pass that should consider any real bytecode rewrite work.
