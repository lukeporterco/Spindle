# SteelHook 0.4 Target Passes

This folder records the SteelHook 0.4 Target arc beginning with Target-32.

Current status: SteelHook 0.4 completes only after Target-36 passes. Target-32 begins the arc as an analysis-only primitive-boundary pass over the completed Target-31 SteelHook 0.3 handoff, Target-33 adds the first offline-only primitive proof for bounded `RETURN_VALUE_INTERCEPT` fixtures, Target-34 adds the second offline-only primitive proof for bounded `INVOKE_REDIRECT` and `INVOKE_WRAP` fixtures, Target-35 proves isolated gated runtime class definition for those three approved primitives through `Class.forName(binaryName, false, runtimeClassLoader)`, and Target-36 verifies the persisted Target-32 through Target-35 evidence chain, rejects stale side-effect reports, rejects raw byte payloads, rejects unsupported primitive leakage, and rejects any report that implies execution beyond class definition. The completed primitive set remains exactly `RETURN_VALUE_INTERCEPT`, `INVOKE_REDIRECT`, and `INVOKE_WRAP`, and those remain internal primitives rather than public APIs.

Planned arc:

- Target-32: primitive boundary definition
- Target-33: bounded return-value interception offline proof
- Target-34: bounded invoke redirect and invoke wrap offline evidence
- Target-35: isolated gated runtime class-definition proof for approved primitives
- Target-36: SteelHook 0.4 completion verification
