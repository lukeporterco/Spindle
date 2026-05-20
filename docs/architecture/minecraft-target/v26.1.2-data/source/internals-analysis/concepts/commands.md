# Commands

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted
Status: confirmed static-source pass complete
Confidence: medium
Handoff readiness: partially ready
First-wave classification: first-wave limited

## What this concept means in Minecraft 26.1.2

Commands are owned by `net.minecraft.commands.Commands`. The class creates its Brigadier `CommandDispatcher<CommandSourceStack>` as a final field, fills it in the constructor by calling many command registration methods, executes parsed commands through `performCommand(...)`, exposes the dispatcher through `getDispatcher()`, and publishes usable command trees to players through `sendCommands(...)`.

`ReloadableServerResources` constructs a fresh `Commands` instance during server data loading and wires `commands.getDispatcher()` into `ServerFunctionLibrary`. `MinecraftServer.reloadResources(...)` loads a new `ReloadableServerResources` bundle and, after completion, closes old resources, assigns the new resources, runs downstream update calls, reloads player resources, and replaces the server function library.

## Important source boundaries

| Boundary | Source | Evidence |
|---|---|---|
| Dispatcher owner and constructor population | `net.minecraft.commands.Commands`, `Commands.java:190-305` | `CMD-CONF-001` |
| Reload creates and swaps command/resource bundle | `MinecraftServer.reloadResources`, `ReloadableServerResources.loadResources` | `CMD-CONF-002`, `RR-001`, `RR-002` |
| Commands precede function library construction | `ReloadableServerResources.<init>`, `ReloadableServerResources.java:46-49` | `CMD-CONF-003` |
| Player command-tree sync | `Commands.sendCommands`, `PlayerList.sendPlayerPermissionLevel` | `CMD-CONF-004` |
| Player command execution path | `ServerGamePacketListenerImpl.parseCommand`, `performUnsignedChatCommand`, `performSignedChatCommand` | `CMD-CONF-005` |

## Confirmed lifecycle or timing behavior

- `ReloadableServerResources` constructs `Commands` before constructing `ServerFunctionLibrary`.
- `ServerFunctionLibrary` receives the dispatcher from that `Commands` instance.
- Resource reload constructs a new server resource bundle; command support must be reload-aware if registrations should survive `/reload`.
- `sendCommands(...)` serializes and sends a filtered command tree to a player; it is a sync/publication path, not evidence for registration.
- Player command execution parses against `server.getCommands().getDispatcher()` and delegates execution through `server.getCommands().performCommand(...)`.

## Candidate Spindle binding direction

First-wave command support should be limited to registration-time and reload-aware behavior. The source supports the command owner, dispatcher, execution path, and reload replacement. It does not yet prove a stable public insertion point that composes with vanilla registration and function compilation without a hook around construction or reload listener composition.

## SteelHook primitive implications

- Constructor-tail or method-around/wrap around `Commands` construction may be needed if external registrations must land before `ServerFunctionLibrary` compiles functions.
- Method-around/wrap around `ReloadableServerResources.loadResources(...)` may be needed for reload-aware command contribution.
- `sendCommands(...)` only needs method-entry/exit if Spindle later exposes command-tree sync events; no hook is needed for registration there.

## Rejected or deferred paths

- `MinecraftServer.getCommands()` as a hook boundary is rejected; it is an accessor.
- `Commands.sendCommands(...)` as a registration boundary is rejected.
- Runtime command mutation after resource load is deferred until dispatcher mutability and player sync semantics are proven.

## Open questions

- Where should external command registration be inserted so function compilation sees contributed commands?
- Does every reload replace the dispatcher in all practical server paths?
- Is a constructor-tail primitive enough, or does registration need method-around/wrap of resource construction?
