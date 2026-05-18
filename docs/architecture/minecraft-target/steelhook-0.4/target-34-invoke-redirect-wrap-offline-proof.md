# Target-34: Invoke Redirect and Wrap Offline Proof

Target-34 is the second implementation proof inside the SteelHook 0.4 arc after the Target-32 primitive boundary and the Target-33 return-value intercept proof. It proves only the internal planned primitives `INVOKE_REDIRECT` and `INVOKE_WRAP`, and it keeps that proof offline-only.

## Inputs

- `minecraft-steelhook-0-4-primitive-boundary.json`
- The in-memory `SteelHook04PrimitiveBoundaryReport` produced by Target-32
- `minecraft-steelhook-0-4-return-value-intercept-offline-proof.json`
- The in-memory `SteelHook04ReturnValueInterceptOfflineProofReport` produced by Target-33

## Output

- Deterministic `minecraft-steelhook-0-4-invoke-redirect-wrap-offline-proof.json`

The report uses schema `1`, milestone `Target-34`, target `minecraft`, and `steelHookVersion: "0.4"`.

## Capability Proved

- Target-34 proves `INVOKE_REDIRECT` only for one controlled `invokestatic` callsite shape.
- Target-34 proves `INVOKE_WRAP` only for the same controlled `invokestatic` callsite shape.
- Strict matching requires owner, name, descriptor, and opcode to match exactly.
- Redirect rewrites the `invokeValue()` callsite from the original methodref to a redirect methodref.
- Wrap rewrites the same callsite from the original methodref to a wrapper methodref with the same descriptor.
- Wrap preserves the original delegate metadata in the report:
- `wrappedDelegateOwnerInternalName`
- `wrappedDelegateName`
- `wrappedDelegateDescriptor`
- `wrappedDelegateOpcode`
- `wrapperOwnerInternalName`
- `wrapperName`
- `wrapperDescriptor`
- `wrapperOpcode`
- Target-34 rewrites only the two-byte methodref operand after opcode `0xb8`.
- Target-34 keeps method code length unchanged and records hashes plus deterministic evidence rather than raw byte payloads.

## Supported Offline Fixture Shape

- Approved fixture shape:
- `INVOKE_SINGLE_STATIC_OR_VIRTUAL_CALLSITE`
- Controlled class:
- `net/minecraft/server/Target34InvokeCallsiteFixture`
- Successful target method:
- `public static int invokeValue()`
- descriptor `()I`
- bounded shape `invokestatic originalValue:()I; ireturn`

## Rejection Surface

Target-34 rejects malformed plans and unsupported shapes deterministically, including:

- null or malformed requests
- primitive kinds other than `INVOKE_REDIRECT` or `INVOKE_WRAP`
- wrong target owner, method name, or descriptor
- wrong invoke owner, name, descriptor, or opcode
- no matching callsite
- ambiguous multiple matching callsites
- constructor invocation targets
- special invocation targets
- replacement descriptor mismatch
- replacement opcode mismatch
- branching methods
- switch methods
- exception tables
- `StackMapTable` presence
- synchronized methods

Target-34 does not support constructor invocation, special invocation, opcode-changing replacement, multiple matching callsites, branch-heavy methods, switch-heavy methods, exception tables, synchronized methods, or `StackMapTable`.

## Boundaries Preserved

- Target-34 is offline-only.
- Target-34 does not execute the wrapper.
- Target-34 does not perform runtime classloading.
- Target-34 does not define transformed classes.
- Target-34 does not install hooks.
- Target-34 does not run Minecraft.
- Target-34 does not execute dispatchers.
- Target-34 does not expose public APIs.
- Target-34 does not claim Java mod execution sandboxing.
- Target-34 does not support arbitrary invoke rewriting, opcode rewriting, constant-pool growth, or stack-map recomputation.
- SteelHook 0.4 is still incomplete after Target-34. Targets 35 and 36 remain required.

## Next Direction

- Target-35 should prove isolated gated runtime class definition for the approved SteelHook 0.4 primitives without installing hooks or invoking Minecraft.
- Later SteelHook 0.4 passes must continue to keep deterministic reports free of raw class-byte payloads.
