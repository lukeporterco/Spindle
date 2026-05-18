# Target-35: SteelHook 0.4 Gated Runtime Proof

Target-35 proves isolated gated runtime class definition for the three approved SteelHook 0.4 internal primitives:

- `RETURN_VALUE_INTERCEPT`
- `INVOKE_REDIRECT`
- `INVOKE_WRAP`

The pass writes deterministic evidence to `minecraft-steelhook-0-4-gated-runtime-proof.json` with schema `1`, milestone `Target-35`, target `minecraft`, and `steelHookVersion: "0.4"`.

Target-35 is class-definition proof only. It is not Minecraft execution, mod execution, hook installation, or completion verification.

## What Target-35 proves

- Each approved primitive can transform its bounded fixture bytes and define the transformed class through an isolated `MinecraftRuntimeClassLoader` session.
- Each session uses `Class.forName(binaryName, false, runtimeClassLoader)`.
- The `false` initialization flag is mandatory and Target-35 keeps `classInitialized: false`.
- Unsupported or malformed primitive plans are rejected before class definition.
- Reports include hashes, code lengths, loader ids, and no-execution evidence only.

## What Target-35 does not do

- It does not initialize transformed classes.
- It does not invoke transformed methods.
- It does not execute wrappers.
- It does not execute `SteelHookDispatcher`.
- It does not install hooks.
- It does not invoke Minecraft main.
- It does not launch a Minecraft server.
- It does not expose public APIs.
- It does not claim Java mod execution sandboxing.
- It does not serialize raw class bytes, transformed class bytes, method byte payloads, or stack-map payloads.

## Scope boundary

Target-35 consumes the in-memory Target-32, Target-33, and Target-34 reports from `MinecraftDryRunFlow`. It reuses the bounded Target-33 and Target-34 transformers inside isolated runtime classloader sessions and records deterministic evidence that class definition occurred without execution beyond definition.

SteelHook 0.4 remains incomplete after Target-35. Target-36 is still required to verify the full evidence chain and close the arc.
