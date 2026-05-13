# Minecraft Target Concept Roadmap

This note formalizes the first named Minecraft Target Layer concept families after SteelHook 0.1.

The intent is architectural, not executable. In this pass, the roadmap and the matching internal concept catalog are documentation/model-only. They do not add runtime hooks, public APIs, real Minecraft runtime transformation, `StackMapTable` rewriting, command registration, registry behavior, data generation tooling, tick scheduling, networking, client support, or any new bootstrap behavior.

SteelHook remains the internal machine layer inside `target-minecraft`. It is not a public arbitrary bytecode mutation API. The Minecraft Target Layer sits above that machinery and names Minecraft concepts in a form that future planning can reason about. The future Modding API remains deferred and should later expose ergonomic APIs built on top of the Target Layer, not on top of raw SteelHook mechanics.

Java mod execution is still not sandboxed. Nothing in this roadmap changes that security posture.

## Relationship Between Layers

```text
SteelHook
  Internal hook and transformation machinery for bounded target-layer work.

Minecraft Target Layer
  Named Minecraft concepts, reports, planning, ownership, and future target operations.

Future Modding API
  Deferred ergonomic APIs for developers, built above named target concepts.
```

## Ordered Concept Families

The order below is intentional. Early concepts focus on server ownership and lifecycle boundaries. Later concepts move toward content, gameplay, networking, state attachment, and controlled escape hatches. This sequence should guide future `Minecraft-*`, `SteelHook-*`, and Modding API planning.

### 1. Server Lifecycle

Purpose:
Name the server startup, ready, stopping, stopped, and failure boundaries that future target work must model before broader gameplay features.

Initial Target Layer concept names:
`MinecraftServerLifecycle`, `MinecraftServerStartContext`, `MinecraftServerStopReason`

Why it is ordered here:
Lifecycle ownership is the first stable target concept because the loader already owns server bootstrap planning and execution boundaries.

SteelHook implications:
Future hooks should remain subordinate to explicit lifecycle phases instead of inventing ad hoc entrypoints.

Initial caution:
This pass does not add lifecycle hooks or public lifecycle APIs.

Target-11 now grounds this family in one deterministic analysis-only report. Only `minecraft.server.lifecycle.starting` is bound, and only to the existing Target-3 Minecraft `26.1.2` server main entrypoint contract. `started`, `stopping`, `stopped`, `crashed`, and `reload_requested` remain declared but unbound. No runtime callback, public API, new SteelHook primitive, or sandboxing is added. See [Target-11: Server Lifecycle Binding Analysis](target-11-server-lifecycle-binding-analysis.md).

Target-12 now adds one deterministic analysis-only dispatch plan above that binding report. It plans exactly one symbolic internal static dispatch for `minecraft.server.lifecycle.starting` before Minecraft server main, leaves the other five lifecycle phases declared unsupported for dispatch, does not implement or call a dispatcher, does not add public listener registration, and does not add sandboxing. See [Target-12: Server Lifecycle Dispatch Plan](target-12-server-lifecycle-dispatch-plan.md).

### 2. Command Registration

Purpose:
Name the future command registration boundary so command-facing APIs are planned from a target concept rather than from raw injection points.

Initial Target Layer concept names:
`MinecraftCommandRegistrar`, `MinecraftCommandRegistrationContext`, `MinecraftCommandTreeAccess`

Why it is ordered here:
Commands depend on lifecycle timing, but they should be named before broader data and registry systems to keep early server tooling coherent.

SteelHook implications:
Any future command bridge should install against a clearly named registration phase, not expose SteelHook primitives.

Initial caution:
This pass does not implement command registration behavior.

Target-13 now adds one deterministic analysis-only report for this family. It writes `minecraft-command-registration-analysis.json`, anchors command registration analysis only to Target-12's symbolic `minecraft.server.lifecycle.starting` dispatch, marks the lifecycle anchor as available only when that upstream dispatch is planned, and leaves dispatcher discovery, registration window, registration apply, and reload reapply declared but unbound. No Minecraft command dispatcher symbol is bound in this pass. No Brigadier adapter, command registration, command execution, command tree access, public command API, public Modding API, runtime callback, new SteelHook primitive, or sandboxing is added. See [Target-13: Command Registration Concept Analysis](target-13-command-registration-concept-analysis.md).

Target-14 now adds the next deterministic analysis-only report for this family. It writes `minecraft-command-dispatcher-symbol-analysis.json`, scans only Target-1 interpreted metadata for Brigadier `CommandDispatcher` descriptor references, and may declare a future minimal command registration proof eligible only when exactly one selectable non-library target is discovered. It does not register commands, execute commands, read or mutate a command tree, add Brigadier dependencies, add hook contracts for command classes, expose public command APIs or a public Modding API, add runtime callbacks, add new SteelHook primitives, or imply sandboxing. See [Target-14: Command Dispatcher Symbol Analysis](target-14-command-dispatcher-symbol-analysis.md).

Target-15 now adds the next deterministic analysis-only report for this family. It writes `minecraft-command-dispatcher-binding-analysis.json`, consumes the Target-14 symbol selection result, classifies whether the selected candidate would require method descriptor interpretation, static field access, instance field owner capture, or instance method receiver capture, and makes explicit that SteelHook 0.1 method-entry dispatch is still insufficient for live dispatcher value access. It does not register commands, add Brigadier, add public APIs, add new SteelHook primitives, or imply sandboxing. See [Target-15: Command Dispatcher Binding Analysis](target-15-command-dispatcher-binding-analysis.md).

### 3. Data, Resources, Reload, and Future Data Generation

Purpose:
Name the future target concepts around resource reload, server data visibility, and any later data-generation-adjacent planning.

Initial Target Layer concept names:
`MinecraftResourceReloadPhase`, `MinecraftDataPackView`, `MinecraftReloadContext`

Why it is ordered here:
Resource and data reload concerns usually arrive after lifecycle and commands but before registry-heavy content systems.

SteelHook implications:
Future internal hooks must respect deterministic reload boundaries and should not blur reload-time analysis with live mutation.

Initial caution:
This pass does not implement reload handling, resource APIs, or data generation tooling.

### 4. Registry Bootstrap and Content Registration

Purpose:
Name the future registry bootstrap and content registration concepts for a controlled content-facing layer.

Initial Target Layer concept names:
`MinecraftRegistryBootstrap`, `MinecraftContentRegistrationContext`, `MinecraftRegistryKey`

Why it is ordered here:
Registries depend on earlier lifecycle and reload concepts, and they should be named before gameplay abstractions grow around them.

SteelHook implications:
Registry-related internals should remain target-owned and deterministic rather than becoming general mutation points.

Initial caution:
This pass does not implement registry or content registration behavior.

### 5. Server Tick and Scheduled Work

Purpose:
Name the future concepts for main-thread tick coordination and loader-approved scheduled server work.

Initial Target Layer concept names:
`MinecraftServerTickPhase`, `MinecraftScheduledWork`, `MinecraftTickContext`

Why it is ordered here:
Tick work should follow lifecycle and registration concepts so execution phases have clear ownership before scheduling abstractions appear.

SteelHook implications:
Future scheduling hooks must preserve the server-first runtime model and avoid implying worker scheduling or simulation replacement.

Initial caution:
This pass does not implement tick callbacks or scheduling.

### 6. World, Dimension, Chunk, and Persistence Lifecycle

Purpose:
Name the boundaries around world creation, dimension access, chunk persistence, and save lifecycle.

Initial Target Layer concept names:
`MinecraftWorldLifecycle`, `MinecraftDimensionHandle`, `MinecraftChunkPersistenceContext`

Why it is ordered here:
World and persistence concepts should follow core server timing concepts because they depend on owned lifecycle and scheduling boundaries.

SteelHook implications:
Future world-facing hooks must stay explicit about persistence safety and must not bypass deterministic boundary reports.

Initial caution:
This pass does not implement world access or persistence APIs.

### 7. Entity, Player, Interaction, and Gameplay Boundaries

Purpose:
Name future gameplay-facing concepts without claiming that an ergonomic gameplay API already exists.

Initial Target Layer concept names:
`MinecraftEntityAccess`, `MinecraftPlayerContext`, `MinecraftInteractionBoundary`

Why it is ordered here:
Gameplay concepts should arrive only after lifecycle, registration, ticking, and world ownership have names and boundaries.

SteelHook implications:
Future SteelHook work should support bounded gameplay-facing target concepts, not expose raw patch surfaces to mods.

Initial caution:
This pass does not add gameplay hooks or public gameplay APIs.

### 8. Networking and Packet Boundaries

Purpose:
Name the future networking concepts for packet boundaries, connection roles, and controlled message handling.

Initial Target Layer concept names:
`MinecraftPacketBoundary`, `MinecraftConnectionRole`, `MinecraftNetworkingContext`

Why it is ordered here:
Networking concepts are downstream from gameplay and lifecycle concerns and should only be planned after core server ownership is clear.

SteelHook implications:
Future networking hooks must remain bounded, explicit, and server-first rather than becoming arbitrary packet mutation surfaces.

Initial caution:
This pass does not implement networking support.

### 9. Attachable State, Components, and Capability-Style Extension

Purpose:
Name the future extension model for attachable target-owned state without leaking loader internals or copying compatibility-layer semantics.

Initial Target Layer concept names:
`MinecraftAttachableState`, `MinecraftComponentKey`, `MinecraftAttachmentContext`

Why it is ordered here:
Attachable state should be planned after the main target ownership model for worlds, entities, players, and networking is named.

SteelHook implications:
Future attachment internals should serve named target concepts and explicit permissions instead of becoming arbitrary storage patches.

Initial caution:
This pass does not implement components, capabilities, or attachment behavior.

### 10. Controlled Access, Bridges, and Low-Level Escape Hatches

Purpose:
Name the final controlled-access layer for cases where future target work needs explicit low-level bridges without collapsing back into raw internals.

Initial Target Layer concept names:
`MinecraftControlledAccess`, `MinecraftTargetBridge`, `MinecraftLowLevelOperation`

Why it is ordered here:
Escape hatches belong last because they should be shaped only after higher-level target concepts have explicit names and boundaries.

SteelHook implications:
This family is the strongest reminder that SteelHook stays internal. Any low-level bridge should still be bounded by named target concepts, reports, and policy.

Initial caution:
This pass does not expose public bridges, arbitrary mutation APIs, or low-level runtime transformation support.

## Planning Guidance

Future Target Layer, SteelHook, and Modding API passes should inspect this roadmap before adding new concept families, patch primitives, or public Minecraft-facing names.

The roadmap is the stable source of truth for concept ordering in this phase. The matching internal catalog under `target-minecraft` exists to support deterministic tests and future planning, not current runtime behavior.
