# SteelHook Primitive Requirements

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

## Current primitives that appear sufficient

- method-entry: server tick, level tick, shutdown start, networking phase entry.
- method-exit: server tick, level tick, post-load lifecycle, command/lifecycle observations.
- direct lookup binding: registry lookup, world/level enumeration after readiness.

## Future primitives likely needed

- async continuation for `CompletableFuture` reload completion.
- method-around/wrap for reload listener composition and possible command/registry contribution.
- constructor-tail for `Commands` registration after vanilla constructor population.

## Primitive gaps by concept

| Concept | Gap |
|---|---|
| Commands | constructor-tail or method-around/wrap may be required before function compilation |
| Registries | contribution through reloadable layers needs async/wrap proof |
| Resources and datapacks | async continuation and listener composition are missing/uncertain |
| Networking | raw packet interception would need wrap, but is rejected |

## Async or CompletableFuture continuation needs

`MinecraftServer.reloadResources(...)`, `ReloadableServerResources.loadResources(...)`, `ReloadableServerRegistries.reload(...)`, and `SimpleReloadInstance.done()` are future-based boundaries.

## Constructor-tail needs

`net.minecraft.commands.Commands` is the clearest constructor-tail candidate if external command registration must run after vanilla registration and before function library use.

## Method-around/wrap needs

Reload listener contribution, reloadable registry contribution, and any future packet interception would need around/wrap semantics.

## Direct lookup bindings that need no hook

- `MinecraftServer.getLevel(...)`, `getAllLevels()`, `levelKeys()`.
- `RegistryAccess`/`HolderLookup.Provider` lookup surfaces.

## Rejected hook ideas

- Hooking `MinecraftServer.getCommands()` for registration.
- Hooking `Commands.sendCommands(...)` for registration.
- Hooking `Connection.send(...)` for first-wave public networking.
- Hooking `LevelTicks.tick(...)` for general Target ticking.

## Risks of over-expanding SteelHook too early

Adding async, wrap, packet, and registry mutation primitives before a narrow Target use case risks encoding unstable Minecraft internals into SteelHook. Keep first-wave primitives small and evidence-driven.
