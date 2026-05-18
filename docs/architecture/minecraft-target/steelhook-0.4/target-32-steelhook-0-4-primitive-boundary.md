# Target-32: SteelHook 0.4 Primitive Boundary

This is an analysis-only SteelHook 0.4 boundary-definition pass for the Minecraft Target Layer. It names exactly which internal primitive families later Target-33 through Target-36 passes may pursue and records the evidence and rejection surface those later passes must satisfy.

## Inputs

- `minecraft-steelhook-0-3-report.json`
- The in-memory `SteelHook03CompletionReport` object produced by Target-31

## Output

- Deterministic `minecraft-steelhook-0-4-primitive-boundary.json`

The report uses schema `1`, milestone `Target-32`, target `minecraft`, and `steelHookVersion: "0.4"`.

## Capability Added Or Recorded

- Target-32 approves exactly three planned internal SteelHook primitive families:
- `RETURN_VALUE_INTERCEPT`
- `INVOKE_REDIRECT`
- `INVOKE_WRAP`
- These names are internal SteelHook primitive names only.
- They are not public SteelHook APIs.
- They are not Minecraft Modding APIs.
- They are not evidence that arbitrary bytecode mutation is supported.
- Target-32 records the Target-31 source gate fields:
- `sourceSteelHook03Milestone`
- `sourceSteelHook03Status`
- `sourceSteelHook03CompletionReady`
- `sourceSteelHook03HandoffStatus`
- The source gate passes only when:
- `sourceSteelHook03Milestone == "Target-31"`
- `sourceSteelHook03Status == "passed"`
- `sourceSteelHook03CompletionReady == true`
- `sourceSteelHook03HandoffStatus == "steelhook-0-3-complete"`

## Allowed Fixture Shapes

- `RETURN_SINGLE_PRIMITIVE_VALUE`
- `RETURN_SINGLE_REFERENCE_VALUE`
- `INVOKE_SINGLE_STATIC_OR_VIRTUAL_CALLSITE`

## Unsupported Fixture Shapes

- `CONSTRUCTOR_INVOCATION`
- `SPECIAL_INVOCATION`
- `MULTIPLE_MATCHING_CALLSITES`
- `BRANCHING_METHOD`
- `SWITCH_METHOD`
- `EXCEPTION_TABLE_METHOD`
- `SYNCHRONIZED_METHOD`
- `CLASS_INITIALIZER`
- `FRAME_RECOMPUTATION_REQUIRED`

## Rejection Taxonomy

- `SOURCE_STEELHOOK_03_NOT_COMPLETE`
- `UNSUPPORTED_PRIMITIVE_KIND`
- `PUBLIC_API_LEAKAGE`
- `RAW_BYTE_PAYLOAD_PRESENT`
- `UNSUPPORTED_FIXTURE_SHAPE`
- `MALFORMED_PRIMITIVE_PLAN`
- `WRONG_OWNER`
- `WRONG_NAME`
- `WRONG_DESCRIPTOR`
- `WRONG_OPCODE`
- `NO_MATCHING_CALLSITE`
- `AMBIGUOUS_MULTIPLE_CALLSITES`
- `CONSTRUCTOR_OR_SPECIAL_INVOKE_UNSUPPORTED`
- `BRANCH_REWRITE_REQUIRED`
- `SWITCH_REWRITE_REQUIRED`
- `EXCEPTION_TABLE_PRESENT`
- `STACKMAP_FRAME_RECOMPUTATION_REQUIRED`
- `SYNCHRONIZED_METHOD_UNSUPPORTED`
- `CLASS_INITIALIZER_UNSUPPORTED`
- `RUNTIME_CLASSLOADING_ATTEMPTED`
- `HOOK_INSTALLATION_ATTEMPTED`
- `DISPATCHER_EXECUTION_ATTEMPTED`
- `MINECRAFT_EXECUTION_ATTEMPTED`

## Evidence Requirements

- Target-33 must prove `RETURN_VALUE_INTERCEPT` offline only for observation and replacement on controlled return shapes, including at least one primitive return and one reference return when cleanly local, and must reject unsupported return shapes plus malformed intercept plans deterministically.
- Target-34 must prove `INVOKE_REDIRECT` offline only for one controlled invoke shape with strict owner, name, descriptor, and opcode matching, and must reject wrong owner, wrong name, wrong descriptor, wrong opcode, no matching callsite, ambiguous multiple matching callsites, and constructor or special invocation shapes.
- Target-34 must prove `INVOKE_WRAP` offline only for the same bounded callsite matching surface as redirect, preserve the same strict matching contract, and reject the same mismatch and unsupported callsite cases.
- Target-35 must prove isolated gated runtime class definition for all three approved primitives. Unsupported primitive plans must be rejected before class definition.
- Target-36 must verify the Target-32 through Target-35 evidence chain and reject stale side-effect reports, schema mismatches, missing primitive evidence, unsupported primitive leakage, raw byte payloads, and reports that imply execution beyond class definition.

## Boundaries Preserved

- SteelHook 0.4 is not complete after Target-32.
- Target-32 does not transform bytecode.
- Target-32 does not define transformed classes.
- Target-32 does not install hooks.
- Target-32 does not execute Minecraft, mods, hooks, or dispatchers.
- Target-32 does not invoke Minecraft main.
- Target-32 does not launch a Minecraft server.
- Target-32 does not expose public SteelHook APIs or Modding APIs.
- Target-32 does not claim Java mod execution sandboxing.
- Target-32 does not add constructor hooks, field hooks, exceptional-exit hooks, arbitrary frame recomputation, branch rewriting, switch rewriting, hook composition, priorities, conflict resolution, Fabric compatibility, Mixin compatibility, remapping, access wideners, Java agent behavior, or gameplay hooks.

## Follow-On Direction

- Target-33 through Target-36 can now operate as constrained implementation and verification passes instead of primitive-family discovery passes.
- No later SteelHook 0.4 pass may add primitive kinds beyond `RETURN_VALUE_INTERCEPT`, `INVOKE_REDIRECT`, and `INVOKE_WRAP` without a new boundary pass.
