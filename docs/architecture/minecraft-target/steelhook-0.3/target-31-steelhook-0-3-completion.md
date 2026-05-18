# Target-31: SteelHook 0.3 Completion

## Goal

Target-31 completes SteelHook 0.3 by verifying the persisted Target-28, Target-29, and Target-30 report chain on top of the accepted Target-27 SteelHook 0.2 handoff.

## Inputs

- `minecraft-steelhook-0-2-report.json`
- `minecraft-steelhook-0-3-framed-method-foundation.json`
- `minecraft-steelhook-0-3-method-exit-static-dispatch.json`
- `minecraft-steelhook-0-3-generalized-transformer-gated-runtime-proof.json`

Forbidden side-effect reports for the completion path:

- `minecraft-hook-installation-result.json`
- `minecraft-server-bootstrap-result.json`
- `minecraft-fixture-transformation-result.json`
- `minecraft-hook-bootstrap-transformation-result.json`

## Output Report

- `minecraft-steelhook-0-3-report.json`

The report is deterministic, schema `1`, milestone `Target-31`, target `minecraft`, and `steelHookVersion: "0.3"`.

The passing handoff state is `handoffStatus: "steelhook-0-3-complete"` with `status: "passed"` and `completionReady: true`.

## Verified Chain

```text
Target-27 SteelHook 0.2 completion
-> Target-28 framed method foundation
-> Target-29 method-exit static dispatch
-> Target-30 generalized transformer gated runtime proof
-> Target-31 SteelHook 0.3 completion verification
```

## Completed Capabilities

SteelHook 0.3 is complete only within this narrow verified boundary:

- bounded `METHOD_ENTRY_STATIC_DISPATCH` with first-frame `StackMapTable` shifting for insertion at offset `0`
- bounded `METHOD_EXIT_STATIC_DISPATCH` before supported normal return opcodes in unframed methods
- isolated gated runtime class definition proof for each approved primitive

## Safety Invariants

Target-31 verifies that:

- the SteelHook 0.2 completion handoff remains accepted and coherent enough to serve as the 0.3 base
- Target-28, Target-29, and Target-30 all preserve their documented handoff and safety fields
- Target-30 entry and exit runtime proof sessions use different runtime loader ids
- raw byte payload keys are absent from the checked SteelHook 0.3 source reports and the final Target-31 report
- stale side-effect reports are absent

## What Target-31 Does Not Add

Target-31 does not:

- add new bytecode primitives
- compose method-entry and method-exit hooks into one class
- install hooks
- launch Minecraft
- invoke `Main.main`
- execute either dispatcher
- expose public SteelHook APIs
- claim Java mod execution sandboxing

## Completion Meaning

SteelHook 0.3 completion is a verified handoff over persisted evidence. It is not a new transformation pass, not a runtime hook-installation pass, and not a sandbox verdict.
