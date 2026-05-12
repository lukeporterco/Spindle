# Target-5: Hook Placement Analysis Scaffold

Target-5 adds the first internal hook placement analysis pass in `target-minecraft`.

Its scope is intentionally narrow. Spindle reuses the validated Target-3 contract for Minecraft `26.1.2` server `net.minecraft.server.Main.main(String[])`, locates the selected runtime class, reads that method's `Code` attribute as opaque bytecode, fingerprints the method body, and writes one deterministic `minecraft-hook-placement-plan.json` report.

## Exact Target-5 Placement

Target-5 supports exactly one placement candidate:

- `id`: `target-5.minecraft.server.main.method-entry-placement`
- `sourceContractId`: `minecraft.26_1_2.server.main.entrypoint`
- `catalogId`: `minecraft-26.1.2-server-known-symbols`
- `kind`: `METHOD_ENTRY`
- `ownerInternalName`: `net/minecraft/server/Main`
- `memberName`: `main`
- `descriptor`: `([Ljava/lang/String;)V`
- `bytecodeOffset`: `0`
- `mode`: `method-entry-analysis-only`
- `required`: `true`

## Planning Gate

Target-5 only plans placement when all of the following are true:

- the Target-3 catalog id is `minecraft-26.1.2-server-known-symbols`
- Target-3 validation passed
- Target-3 error count is `0`
- the Target-3 report contains a valid `minecraft.26_1_2.server.main.entrypoint` contract
- the frozen execution plan main class is `net.minecraft.server.Main`
- the planned runtime contains `net/minecraft/server/Main.class`
- `main([Ljava/lang/String;)V` has a `Code` attribute

Planning-only mode records gate failure in `minecraft-hook-placement-plan.json` without throwing.

## Method Code Summary

Target-5 reads only enough class-file structure to find the selected method and read its `Code` attribute metadata:

- `maxStack`
- `maxLocals`
- `codeLength`
- `codeSha256`
- `exceptionTableCount`
- `nestedCodeAttributeCount`
- `hasCodeAttribute`
- `abstractOrNative`
- `methodEntryOffset`

The method body is treated as opaque bytes. Target-5 does not decode JVM instructions or inspect control flow.

## What Target-5 Does Not Do

Target-5 does not:

- decode instructions
- inspect callsites
- modify bytecode
- inject, transform, patch, remap, or instrument Minecraft classes
- install hooks
- expose a public hook API
- add gameplay hooks
- use Mixin or Java agents
- imply Java mod execution is sandboxed

Target-5 sits beside Target-4. It does not require hook installation flags and it does not change normal bootstrap behavior.
