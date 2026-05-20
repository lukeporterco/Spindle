# Server Lifecycle

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted
Status: confirmed static-source pass complete
Confidence: medium
Handoff readiness: partially ready
First-wave classification: first-wave limited

## What this concept means in Minecraft 26.1.2

Server lifecycle covers construction, bootstrap, world loading, public readiness, ticking, shutdown request, subsystem close, and final exit. In 26.1.2 the strongest shared vanilla boundary is `MinecraftServer.loadLevel()`, which calls `createLevels()`, `forceDifficulty()`, and `prepareLevels()` before tick-loop entry. Dedicated startup adds a dedicated-only notification point in `DedicatedServer.initServer()`: after `loadLevel()`, post-load logging, optional query/RCON/watchdog setup, and `saveEverything(...)`, it calls `notificationManager().serverStarted()` before returning true.

`MinecraftServer.isReady` is not set during `loadLevel()` or `serverStarted()`. Static source places the assignment inside `MinecraftServer.runServer()` after `processPacketsAndTick(...)`, `waitUntilNextTick()`, tick timing, and cleanup for a loop iteration. That makes it a first-tick-complete signal, not a pure post-world-load signal.

Integrated server parity is partial. `IntegratedServer.initServer()` calls `loadLevel()` and `saveEverything(...)`, but it does not call `notificationManager().serverStarted()`. It overrides `tickServer(...)`, `stopServer()`, and `halt(...)`, usually delegating to `super` with client/local-player behavior around the shared base lifecycle.

## Important source boundaries

| Boundary | Source | Evidence |
|---|---|---|
| Dedicated init and started notification | `net.minecraft.server.dedicated.DedicatedServer.initServer`, `DedicatedServer.java:272-338` | `LC-001` |
| Shared world load order | `net.minecraft.server.MinecraftServer.loadLevel`, `MinecraftServer.java:421-429` | `LC-002`, `WLC-001` |
| First tick/readiness assignment | `net.minecraft.server.MinecraftServer.runServer`, `MinecraftServer.java:746-825` | `LC-003` |
| Shutdown and final exit | `MinecraftServer.close`, `halt`, `runServer` finally block, `stopServer`; `DedicatedServer.stopServer` | `LC-004` |
| Integrated lifecycle overrides | `net.minecraft.client.server.IntegratedServer.initServer`, `tickServer`, `stopServer`, `halt` | `LC-005` |

## Confirmed lifecycle or timing behavior

- `DedicatedServer.initServer()` starts the TCP listener before world load, calls `loadLevel()`, then reaches `notificationManager().serverStarted()` only after post-load dedicated setup and `saveEverything(...)`.
- `MinecraftServer.loadLevel()` creates and prepares levels before returning.
- `MinecraftServer.runServer()` calls `initServer()` before entering the main loop.
- `isReady` is assigned after one loop iteration has processed tick work and next-tick waiting.
- `DedicatedServer.stopServer()` sends `notificationManager().serverShuttingDown()` before delegating to `MinecraftServer.stopServer()`.
- `IntegratedServer` shares `loadLevel()`, base `runServer()`, and base stop mechanics, but the dedicated `serverStarted` notification is not integrated parity evidence.

## Candidate Spindle binding direction

Use lifecycle as a first-wave limited Target concept. The safest early documentation boundary is "server worlds loaded and prepared" after `loadLevel()`/`prepareLevels()`, with a dedicated-only "started notification" marker and a separate first-tick-complete readiness signal. Do not collapse these into one public "ready" event.

## SteelHook primitive implications

- `DedicatedServer.initServer()` post-notification observation: method-exit or narrowly placed method-entry/exit.
- `MinecraftServer.loadLevel()` or `prepareLevels()` completion: method-exit if Spindle needs a post-world-load event.
- `MinecraftServer.runServer()` first-tick readiness: method-exit is not sufficient; this would need method-entry/exit around loop/tick state or an explicit future runtime probe before implementation.
- Shutdown observation: method-entry on `DedicatedServer.stopServer()` and `MinecraftServer.stopServer()`; method-exit around `onServerExit()` only if "fully exited" becomes a requirement.

## Rejected or deferred paths

- Treating `isReady()` as the public ready boundary is deferred; source proves timing but not safety semantics.
- Treating `notificationManager().serverStarted()` as a shared lifecycle signal is rejected for integrated parity.
- Treating Fabric loader lifecycle as vanilla lifecycle proof remains rejected.

## Open questions

- Does Spindle need one lifecycle vocabulary for dedicated and integrated, or an explicit split?
- Should a runtime probe confirm first-tick ordering before any public "server ready" SteelHook requirement?
- Does shutdown need separate "stopping", "resources closed", and "exited" events, or only a first-wave stopping observation?
