# SteelHook 0.3 Target Passes

This folder records the opening SteelHook 0.3 Target passes after the completed SteelHook 0.2 handoff.

Current status: SteelHook 0.3 begins with bounded framed-method support for the existing method-entry static-dispatch primitive. It is not broad StackMapTable recomputation and it is not a general bytecode-mutation expansion.

Target-28 is the first SteelHook 0.3 pass. It adds one deterministic StackMapTable handling path for method-entry insertion at offset `0` with the existing three-byte `invokestatic` dispatcher shape. It shifts only the first explicit StackMapTable frame offset, parses and preserves later frames unchanged, and fails closed on malformed frame data or offset overflow.

Target-28 remains offline-only proof work. It does not enable runtime classloading for framed targets, install hooks, invoke Minecraft main, launch a server, expose public APIs, add method-exit hooks, or claim Java mod execution sandboxing.
