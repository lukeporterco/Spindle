# Target-6: Instruction-Aware Bytecode Model

Target-6 adds the first instruction-aware SteelHook bytecode analysis pass in `target-minecraft`.

It builds directly on the Target-5 method-entry placement plan for Minecraft `26.1.2` server `net.minecraft.server.Main.main(String[])`. Target-6 reuses the same validated runtime artifact, decodes the selected method `Code` bytes into a deterministic internal instruction stream model, validates instruction boundaries, summarizes branch and switch targets, preserves exception-table and nested `Code` attribute metadata, and writes `minecraft-hook-bytecode-analysis.json`.

## Target-6 Scope

Target-6 records:

- the selected Target-5 placement metadata
- a deterministic decoded instruction list
- `Code` attribute metadata including `maxStack`, `maxLocals`, `codeLength`, and `codeSha256`
- branch targets and switch match-to-target pairs
- exception-table entries
- nested `Code` attribute summaries
- `StackMapTable` presence and entry count when present

## Validation Scope

Target-6 validates only basic bytecode structure:

- the decoded instruction stream covers the full `code` array without gaps or trailing bytes
- every branch target lands on a decoded instruction boundary inside the method
- every `tableswitch` and `lookupswitch` default and case target lands on a decoded instruction boundary inside the method
- every exception-table `start_pc` and `handler_pc` lands on a decoded instruction boundary
- every exception-table `end_pc` lands on a decoded instruction boundary or exactly `codeLength`
- reserved and unknown opcodes are reported deterministically and fail validation

Planning-only mode records gate failure in `minecraft-hook-bytecode-analysis.json` without throwing.

## What Target-6 Does Not Do

Target-6 does not:

- modify bytecode
- inject, transform, patch, rewrite, remap, or instrument Minecraft classes
- update `StackMapTable`
- compute a full control-flow graph
- compute stack types, verifier frames, locals state, or data-flow
- generate patch plans or transformed class bytes
- install hooks
- expose a public hook API
- add gameplay hooks
- use Mixin or Java agents
- imply Java mod execution is sandboxed

Target-6 remains an internal analysis layer. Target-7 is the earliest pass that may plan one internal method-entry static-dispatch patch for `net.minecraft.server.Main.main(String[])`, but it still must not generate transformed class bytes, rewrite the constant pool, rewrite `Code`, update `StackMapTable`, install hooks, expose public APIs, add gameplay hooks, use Mixin or Java agents, or imply Java mod execution is sandboxed.
