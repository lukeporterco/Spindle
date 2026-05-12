# Minecraft Concept Roadmap

This document defines the first Minecraft concept families Spindle should model after SteelHook 0.1.

SteelHook 0.1 proves that Spindle can identify a known Minecraft symbol, analyze placement, decode bytecode, plan a patch, transform fixture/fake-server bytecode, route bootstrap class loading, invoke an internal dispatcher, and write deterministic reports. That proves the hook spine, but it does not yet define the Minecraft-facing concepts that future Target Layer and Modding API features should expose.

The next stage should grow from named Minecraft concepts, not from arbitrary bytecode power. SteelHook should remain the internal machine layer. The Minecraft Target Layer should translate SteelHook machinery into named Minecraft concepts. The future Modding API should expose ergonomic developer-facing APIs on top of those concepts.

The ordering below is intentionally conservative. It starts with server-side concepts closest to the current SteelHook 0.1 proof, then expands toward content, runtime behavior, world state, gameplay, networking, components, and low-level escape hatches.

## Concept Order

1. Server Lifecycle
2. Command Registration
3. Data, Resources, Reload, and Future Data Generation
4. Registry Bootstrap and Content Registration
5. Server Tick and Scheduled Work
6. World, Dimension, Chunk, and Persistence Lifecycle
7. Entity, Player, Interaction, and Gameplay Boundaries
8. Networking and Packet Boundaries
9. Attachable State, Components, and Capability-Style Extension
10. Controlled Access, Bridges, and Low-Level Escape Hatches

## 1. Server Lifecycle

Server lifecycle should be the first Minecraft concept family because it is closest to what SteelHook 0.1 already proves: a controlled launch-boundary path around the server entrypoint.

Initial Target Layer concepts should include:

```text
minecraft.server.lifecycle.starting
minecraft.server.lifecycle.started
minecraft.server.lifecycle.stopping
minecraft.server.lifecycle.stopped
minecraft.server.lifecycle.crashed
minecraft.server.lifecycle.reload_requested
```

This concept family matters because nearly every later feature depends on knowing what phase the server is in. Commands, registries, data reload, scheduled tasks, networking, and world loading all need reliable lifecycle boundaries.

SteelHook implications:

- Method-entry hooks are enough for the earliest proof points.
- Method-exit hooks become useful for completed startup/shutdown boundaries.
- Failure-path observation may be needed for crash or aborted-startup reporting.

Initial caution:

Do not expose this as a broad public event API too early. First define stable Target Layer lifecycle points and prove they map cleanly to supported Minecraft version catalogs.

## 2. Command Registration

Command registration should come second because it is immediately useful, server-side, and grounded in Minecraft's Brigadier command system.

Initial Target Layer concepts should include:

```text
minecraft.commands.dispatcher.available
minecraft.commands.registration.before
minecraft.commands.registration.apply
minecraft.commands.registration.after
minecraft.commands.reload.reapply
```

This concept family gives mod developers a visible and useful early feature without requiring blocks, items, entities, rendering, networking, or deep gameplay hooks.

SteelHook implications:

- Likely placement around command dispatcher construction or refresh.
- Reload-safe command registration should be modeled from the start.
- Dispatcher contribution should be deterministic and reportable.

Initial caution:

Do not model command registration as a one-time mutation that may break after reload. Command registration should be phase-aware and reload-safe.

## 3. Data, Resources, Reload, and Future Data Generation

This concept family has two related but distinct parts.

Runtime data/resource integration is about how Spindle participates in Minecraft's data and resource loading pipeline. This includes tags, recipes, loot tables, advancements, predicates, item modifiers, worldgen JSON, language files, and other data-driven assets.

Data generation is a future developer-experience feature where mod authors write Java code that emits JSON assets during development or build workflows. It should be treated as adjacent to runtime data/resource support, not as the same subsystem.

Initial Target Layer concepts should include:

```text
minecraft.data_pack.discovery
minecraft.data_pack.reload.before
minecraft.data_pack.reload.after
minecraft.resources.reload.before
minecraft.resources.reload.after
minecraft.tags.registration
minecraft.recipes.registration
minecraft.loot_tables.registration
```

This concept family matters because Minecraft already has a large data-driven customization surface. Spindle should use that surface instead of forcing every content feature through bytecode transformation.

SteelHook implications:

- Runtime reload concepts may require lifecycle hooks and resource manager hooks.
- Data generation itself should not require SteelHook.
- Registry/bootstrap awareness may become necessary for some generated or loaded data.

Initial caution:

Keep build-time data generation separate from runtime data reload. They should cooperate, but they are not the same mechanism.

## 4. Registry Bootstrap and Content Registration

Registry bootstrap is the foundation for real content mods. Items, blocks, entities, particles, attributes, game rules, biomes, dimensions, effects, and similar game objects ultimately need to become known to Minecraft.

Initial Target Layer concepts should include:

```text
minecraft.registry.bootstrap.before
minecraft.registry.bootstrap.after
minecraft.registry.freeze.before
minecraft.registry.freeze.after
minecraft.registry.entry.register
minecraft.registry.entry.replace
minecraft.registry.sync.prepare
```

This concept family should come after lifecycle, commands, and data/resource concepts because registry timing is sensitive. However, it should not be delayed too far because maximum modding capability depends on it.

SteelHook implications:

- Registry bootstrap may require class-initializer hooks, bootstrap hooks, access bridges, or generated adapters.
- Version catalogs will be important because registry internals are version-sensitive.
- Future registry sync semantics must be considered before client/server support expands.

Initial caution:

Do not promise ergonomic block, item, or entity registration until registry timing and version catalogs are reliable.

## 5. Server Tick and Scheduled Work

Server tick and scheduling should be the first runtime behavior concept after startup/bootstrap concepts. This gives mods a controlled way to run repeated or delayed work without introducing broad thread management.

Initial Target Layer concepts should include:

```text
minecraft.server.tick.before
minecraft.server.tick.after
minecraft.world.tick.before
minecraft.world.tick.after
minecraft.scheduler.task.enqueue
minecraft.scheduler.task.run
minecraft.scheduler.task.cancel
```

This concept family unlocks a large amount of normal mod behavior while keeping execution semantics understandable.

SteelHook implications:

- Method-entry and method-exit hooks around server tick and world tick methods.
- Dispatcher ordering, priorities, and error handling will become important.
- Scheduled work should initially run through deterministic main-thread execution.

Initial caution:

Keep initial scheduling main-thread and deterministic. Do not introduce broad async execution, worker-thread simulation, or ECS behavior in this concept pass.

## 6. World, Dimension, Chunk, and Persistence Lifecycle

World and chunk lifecycle concepts are needed for serious gameplay mods, persistence, dimension-aware behavior, terrain features, region cleanup, and save/load integration.

Initial Target Layer concepts should include:

```text
minecraft.world.load.before
minecraft.world.load.after
minecraft.world.save.before
minecraft.world.save.after
minecraft.chunk.load.before
minecraft.chunk.load.after
minecraft.chunk.save.before
minecraft.chunk.save.after
minecraft.dimension.load
minecraft.dimension.unload
```

This concept family is more complex than lifecycle, commands, registries, or tick, but it is foundational for deep server-side modding.

SteelHook implications:

- Method-entry and method-exit hooks around world/chunk lifecycle methods.
- Object identity tracking may be required.
- Persistence boundaries must eventually cooperate with attachable state cleanup and serialization.

Initial caution:

World and chunk lifecycle must be precise about thread, phase, and object lifetime. Loose semantics here can cause subtle persistence and data-corruption bugs.

## 7. Entity, Player, Interaction, and Gameplay Boundaries

This is the broad normal-modding surface: player join/leave, entity spawn/despawn, entity tick, item use, block interaction, damage, death, teleportation, inventory interaction, and similar gameplay events.

Initial Target Layer concepts should include:

```text
minecraft.player.join
minecraft.player.leave
minecraft.entity.spawn
minecraft.entity.despawn
minecraft.entity.tick.before
minecraft.entity.tick.after
minecraft.interaction.block.use
minecraft.interaction.item.use
minecraft.combat.damage.before
minecraft.combat.damage.after
minecraft.entity.death
```

This concept family is highly valuable, but it should follow lifecycle, commands, data/resource support, registry bootstrap, and tick semantics because gameplay events require stronger dispatcher conventions.

SteelHook implications:

- Observation hooks can come first.
- Cancellable behavior requires return-value interception or callsite wrapping.
- Result replacement requires explicit dispatcher result conventions.
- Conflict semantics become more important once multiple hooks can alter behavior.

Initial caution:

Do not start with cancellable gameplay events unless SteelHook has the required return-value or callsite primitive. Observation events are safer than behavior-changing events at this stage.

## 8. Networking and Packet Boundaries

Networking is required for serious client/server mods, synchronization, GUIs, custom payloads, multiplayer behavior, and eventual client integration.

Initial Target Layer concepts should include:

```text
minecraft.network.serverbound.packet.receive
minecraft.network.clientbound.packet.send
minecraft.network.custom_payload.register
minecraft.network.custom_payload.receive
minecraft.network.connection.open
minecraft.network.connection.close
```

This concept family is necessary for maximum modding capability, but it multiplies complexity through sidedness, serialization, trust boundaries, protocol compatibility, and client/server versioning.

SteelHook implications:

- Packet receive/send hooks.
- Sided Target Layer catalogs.
- Dispatcher validation for payload identity, serialization, and authority.
- Eventual client-side target support.

Initial caution:

Keep the first networking model server-side or server-authoritative. Do not prematurely design the full client API.

## 9. Attachable State, Components, and Capability-Style Extension

Mods need to attach state and behavior to objects they do not own: players, entities, item stacks, block entities, worlds, chunks, and possibly registry entries.

Initial Target Layer concepts should include:

```text
minecraft.state.attach.entity
minecraft.state.attach.player
minecraft.state.attach.item_stack
minecraft.state.attach.block_entity
minecraft.state.attach.world
minecraft.state.attach.chunk
minecraft.state.serialize
minecraft.state.deserialize
```

This concept family is essential for maximum modding capability, but it depends on lifecycle, persistence, networking, and object identity semantics.

SteelHook implications:

- Side-table-backed state should come first.
- Field addition, interface injection, and generated bridges can come later.
- Cleanup hooks are required to avoid leaks.
- Serialization and sync must be explicit.

Initial caution:

Do not start with bytecode field injection. Start with side-table-backed attachments and deterministic cleanup.

## 10. Controlled Access, Bridges, and Low-Level Escape Hatches

This is not a normal developer-facing feature, but it is required for Spindle's long-term power. Many Minecraft concepts will need controlled access to internals, generated bridge methods, interface attachment, access changes, or carefully validated redirects.

Initial Target Layer concepts should include:

```text
minecraft.bridge.synthetic_method
minecraft.bridge.accessor
minecraft.bridge.invoker
minecraft.bridge.interface_attach
minecraft.access.private_read
minecraft.access.private_write
minecraft.low_level.redirect_callsite
minecraft.low_level.replace_constant
```

This concept family should remain mostly internal. It gives Spindle power without making normal mods depend on raw bytecode mutation.

SteelHook implications:

- Access changes.
- Synthetic method generation.
- Interface injection.
- Callsite redirects.
- Constant replacement.
- Conflict detection.
- Version-catalog validation.

Initial caution:

This must not become a public arbitrary injection API. SteelHook should remain powerful through validated patch modes, while the Target Layer exposes named Minecraft concepts and the future Modding API exposes ergonomic developer-facing features.

## First Bundle Recommendation

The strongest first bundle is:

```text
1. Server Lifecycle
2. Command Registration
3. Data, Resources, Reload, and Future Data Generation
4. Registry Bootstrap and Content Registration
5. Server Tick and Scheduled Work
```

This bundle gives Spindle startup semantics, developer-visible utility, data-driven content support, content registration direction, and basic runtime behavior without prematurely jumping into entities, networking, multithreading, ECS, or arbitrary bytecode mutation.

## Boundary Notes

- SteelHook is an internal transformation engine, not a public arbitrary bytecode mutation API.
- The Minecraft Target Layer should define named Minecraft concepts before SteelHook grows new patch primitives.
- The future Modding API should expose ergonomic concepts only after the Target Layer has stable concept mappings.
- Java mod execution must not be described as sandboxed.
- Real Minecraft runtime transformation, StackMapTable rewriting, client support, ECS, multithreaded simulation, and broad gameplay mutation should remain out of scope until a specific concept requires them.
