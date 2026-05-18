# SteelHook 0.3 Target Passes

This folder records the opening SteelHook 0.3 Target passes after the completed SteelHook 0.2 handoff.

Current status: SteelHook 0.3 begins with bounded framed-method support for the existing method-entry static-dispatch primitive. It is not broad StackMapTable recomputation and it is not a general bytecode-mutation expansion.

Target-28 is the first SteelHook 0.3 pass. It adds one deterministic StackMapTable handling path for method-entry insertion at offset `0` with the existing three-byte `invokestatic` dispatcher shape. It shifts only the first explicit StackMapTable frame offset, parses and preserves later frames unchanged, and fails closed on malformed frame data or offset overflow.

Target-29 adds the sibling primitive `METHOD_EXIT_STATIC_DISPATCH` as offline-only proof work. It inserts `invokestatic SteelHookDispatcher.afterMinecraftServerMain:()V` immediately before supported normal return opcodes in a controlled unframed fixture method, rejects framed, branched, switched, exception-table, synchronized, constructor, and class-initializer cases, and still does not enable runtime classloading, install hooks, invoke Minecraft main, execute the dispatcher, expose public APIs, or claim Java mod execution sandboxing.
