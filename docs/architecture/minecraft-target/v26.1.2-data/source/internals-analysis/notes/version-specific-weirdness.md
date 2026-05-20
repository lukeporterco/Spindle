# Version Specific Weirdness

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

## Current Observations

- Source mappings use Yarn named names in the extracted `.research-src` tree, but several decompiled signatures use Java source signatures rather than bytecode descriptors.
- `MinecraftServer.isReady` is assigned after a tick-loop iteration, not during level load or dedicated `serverStarted()`.
- Dedicated server has `notificationManager().serverStarted()` and `serverShuttingDown()` points; integrated server uses shared base lifecycle with local overrides and no equivalent `serverStarted()` evidence.
- Server resource reload assigns `this.resources = newResources` before downstream updates such as component/tag application, recipe finalization, player reload, function-library replacement, structure refresh, and fuel recomputation.
- Registry layers are explicitly named `STATIC`, `WORLDGEN`, `DIMENSIONS`, and `RELOADABLE` in this version.
- Configuration networking includes known-pack negotiation, registry synchronization, optional resource-pack tasking, prepare-spawn, and join-world tasks.
- Fabric workspace uses Java 25 and Loom split environment source sets; it is useful for probes but is not vanilla source evidence.

## Evidence

`LC-001` to `LC-005`, `RR-001`, `RCF-001`, `NET-07`, `E-001`.

## Impact

Do not carry older-version assumptions into Spindle planning. In particular, avoid assuming one universal ready event, simple synchronous reload completion, or flat registry state.

## Follow-up

Runtime-log start/reload/stop timing before SteelHook implementation planning.
