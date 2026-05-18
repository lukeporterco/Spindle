# Target-33: Return-Value Intercept Offline Proof

Target-33 is the first implementation proof inside the SteelHook 0.4 arc after the Target-32 primitive-boundary pass. It proves only the internal planned primitive `RETURN_VALUE_INTERCEPT`, and it keeps that proof offline-only.

## Inputs

- `minecraft-steelhook-0-4-primitive-boundary.json`
- The in-memory `SteelHook04PrimitiveBoundaryReport` produced by Target-32

## Output

- Deterministic `minecraft-steelhook-0-4-return-value-intercept-offline-proof.json`

The report uses schema `1`, milestone `Target-33`, target `minecraft`, and `steelHookVersion: "0.4"`.

## Capability Proved

- Target-33 proves `RETURN_VALUE_INTERCEPT` only.
- Target-33 supports exactly two controlled fixture shapes:
- `RETURN_SINGLE_PRIMITIVE_VALUE`
- `RETURN_SINGLE_REFERENCE_VALUE`
- Target-33 proves `OBSERVE_ONLY` behavior by matching a supported producer plus return opcode without modifying class bytes.
- Target-33 proves `REPLACE_RETURN_VALUE` behavior by rewriting the bounded producer operand in transformed class bytes offline only.
- Target-33 records deterministic evidence through hashes, code lengths, matched opcodes, counts, and replacement summaries.
- Target-33 does not serialize raw class bytes, transformed class bytes, method code bytes, stack-map payloads, or other raw byte payloads in JSON.

## Supported Offline Fixture Shapes

- Primitive fixture:
- `public static int primitiveValue()`
- descriptor `()I`
- bounded shape `bipush 7; ireturn`
- Reference fixture:
- `public static String referenceValue()`
- descriptor `()Ljava/lang/String;`
- bounded shape `ldc "original"; areturn`

Replacement remains length-preserving:

- Primitive replacement patches `7 -> 42`
- Reference replacement patches `"original" -> "replacement"` by switching the existing `ldc` constant-pool operand

## Rejection Surface

Target-33 rejects malformed plans and unsupported shapes deterministically, including:

- null or malformed requests
- primitive kinds other than `RETURN_VALUE_INTERCEPT`
- wrong owner, method name, or descriptor
- void returns
- constructor targets
- class initializer targets
- multiple return opcodes
- branching methods
- switch methods
- exception tables
- `StackMapTable` presence
- synchronized methods
- missing supported producers before the return
- replacement kinds that do not match the target descriptor

## Boundaries Preserved

- Target-33 is offline-only.
- Target-33 does not perform runtime classloading.
- Target-33 does not define transformed classes.
- Target-33 does not install hooks.
- Target-33 does not run Minecraft.
- Target-33 does not execute dispatchers.
- Target-33 does not expose public APIs.
- Target-33 does not claim Java mod execution sandboxing.
- Target-33 does not add invoke redirect or invoke wrap proof.
- SteelHook 0.4 is still incomplete after Target-33. Targets 34 through 36 remain required.

## Next Direction

- Target-34 should add the same style of bounded offline proof for `INVOKE_REDIRECT` and `INVOKE_WRAP`.
- Later SteelHook 0.4 passes must continue to keep raw byte payloads out of deterministic reports.
