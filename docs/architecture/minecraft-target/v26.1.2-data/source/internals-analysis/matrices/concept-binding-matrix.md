# Concept Binding Matrix

## Purpose

Map Minecraft 26.1.2 internals concepts to narrow Spindle Target Layer binding directions.

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

| Concept | Classification | Target Layer direction | Boundary | Evidence | Notes |
|---|---|---|---|---|---|
| Server lifecycle | first-wave limited | event-only with dedicated/shared split | `MinecraftServer.loadLevel`, `DedicatedServer.initServer`, `MinecraftServer.stopServer` | `LC-001` to `LC-005` | Public ready remains runtime-confirmation gated |
| Commands | first-wave limited | registration-time and reload-aware | `Commands` constructor, `ReloadableServerResources.loadResources` | `CMD-CONF-001` to `CMD-CONF-003` | Do not use accessor/sync methods as registration |
| Registries | first-wave limited | lookup-only | `RegistryAccess`, `HolderLookup.Provider`, `LayeredRegistryAccess` | `RCF-001` to `RCF-007` | Mutation remains research-gated |
| Resources and datapacks | SteelHook-gated | event-only initially | `MinecraftServer.reloadResources` returned future | `RR-001` to `RR-010` | Contribution needs async/listener composition |
| World and level | first-wave limited | guarded lookup/enumeration | `MinecraftServer.getLevel`, `getAllLevels`, `levelKeys` | `WLC-001` to `WLC-005` | Prefer snapshot/wrapper semantics |
| Ticking | first-wave stable | before/after server and level tick events | `MinecraftServer.tickServer`, `ServerLevel.tick` | `TC-001` to `TC-004` | Scheduled subsystem ticks rejected |
| Networking | first-wave limited | phase-event-only or documentation-only | login/config/play handoff methods | `NET-01` to `NET-12` | Raw packets rejected |
| Data generation and assets | research-gated | documentation-only asset input bridge | `DataGenerator`, `PackOutput`, pack ingestion | `E-001` to `E-010` | Not a runtime API yet |

## Open Questions

- Which lifecycle event should become the public default: post-load, first tick, or both with distinct names?
- Does command registration require constructor-tail or method-around/wrap?
- Should networking be included as phase events in the first implementation pass, or left as documentation-only?
