# SteelHook 0.3 Target Passes

This folder records the opening SteelHook 0.3 Target passes after the completed SteelHook 0.2 handoff.

Current status: SteelHook 0.3 is complete through Target-31. It remains a bounded verifier-backed primitive expansion, not broad StackMapTable recomputation and not a general bytecode-mutation expansion.

Target-28 is the first SteelHook 0.3 pass. It adds one deterministic StackMapTable handling path for method-entry insertion at offset `0` with the existing three-byte `invokestatic` dispatcher shape. It shifts only the first explicit StackMapTable frame offset, parses and preserves later frames unchanged, and fails closed on malformed frame data or offset overflow.

Target-29 adds the sibling primitive `METHOD_EXIT_STATIC_DISPATCH` as offline-only proof work. It inserts `invokestatic SteelHookDispatcher.afterMinecraftServerMain:()V` immediately before supported normal return opcodes in a controlled unframed fixture method, rejects framed, branched, switched, exception-table, synchronized, constructor, and class-initializer cases, and still does not enable runtime classloading, install hooks, invoke Minecraft main, execute the dispatcher, expose public APIs, or claim Java mod execution sandboxing.

Target-30 lifts both approved SteelHook 0.3 primitives through the existing gated runtime classloader proof boundary, but still proves them separately. It defines transformed `net.minecraft.server.Main` fixture classes through isolated `MinecraftRuntimeClassLoader` sessions, does not compose hooks into one class, does not install hooks, does not invoke Minecraft main, does not launch Minecraft, does not execute either dispatcher, does not expose public APIs, and does not claim Java mod execution sandboxing.

Target-31 completes SteelHook 0.3 by verifying the accepted SteelHook 0.2 handoff plus the Target-28, Target-29, and Target-30 reports, rejecting stale side-effect outputs and raw byte payload keys, and writing `minecraft-steelhook-0-3-report.json`. The passing handoff is `steelhook-0-3-complete`. SteelHook 0.3 still does not compose hooks into one class, install hooks, launch Minecraft, invoke Minecraft main, execute dispatchers, expose public APIs, or claim Java mod execution sandboxing.
