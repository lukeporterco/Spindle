# Target-28: SteelHook 0.3 Framed Method Foundation

## Goal

Target-28 starts SteelHook 0.3 by replacing the prior blanket `StackMapTable` rejection with one bounded method-entry rewrite policy for framed methods.

## What Target-28 Adds

- a raw `StackMapTable` parser and bounded rewriter for method-entry insertion at offset `0`
- support for shifting the first explicit StackMapTable `offset_delta` by the inserted instruction length
- validation and unchanged preservation of later StackMapTable frames after parsing
- deterministic failure on malformed frames, unknown verification tags, trailing bytes, duplicate `StackMapTable` attributes, or first-frame offset overflow
- a deterministic report: `minecraft-steelhook-0-3-framed-method-foundation.json`

## Supported Boundary

Target-28 supports only this primitive shape:

- method-entry static dispatch only
- insertion offset `0` only
- inserted instruction length `3` only
- the existing no-argument void `invokestatic` dispatcher shape only

For framed methods, Target-28 shifts only the first explicit StackMapTable frame. It does not recompute all frames and it does not rewrite later relative deltas.

## What Target-28 Does Not Add

Target-28 does not add:

- method-exit hooks
- cancellable hooks
- runtime classloading for framed targets
- hook installation
- Minecraft main invocation
- Minecraft server launch
- dispatcher observation requirements
- public SteelHook APIs
- Java agents, Mixin support, remapping, or access wideners
- Java mod execution sandboxing claims

## Proof Shape

Target-28 proves the bounded framed-method rewrite through controlled fixture class bytes. The proof uses a framed method with a branch and a non-empty `StackMapTable` whose first explicit frame shifts by `3`.

The report records the SteelHook 0.2 source handoff state, whether StackMapTable rewriting was supported and applied, whether the first-frame shift was proved, and whether all runtime-facing non-goals remained disabled.

## Handoff

If Target-28 passes, the next direction is `move-to-target-29-method-exit-static-dispatch`.
