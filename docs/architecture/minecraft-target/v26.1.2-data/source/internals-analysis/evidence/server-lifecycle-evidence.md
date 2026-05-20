# Server Lifecycle Evidence

Minecraft version: 26.1.2
Mapping namespace: Yarn named
Status: needs orchestrator review

## Evidence 1

Evidence ID: SL-001
Concept: Server lifecycle
Worker: retrieval worker
Source artifact: `.research-src/common/net/minecraft/server/Main.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/Main.java`
Class: `net.minecraft.server.Main`
Member: `public static void main(String[] args)`
Descriptor: `([Ljava/lang/String;)V`
Line range or local reference: `68-219`
Exact command used: `nl -ba .research-src/common/net/minecraft/server/Main.java | sed -n '1,260p'`
Why this matched: This is the dedicated server entrypoint class and the only `main` method in the server bootstrap path.
Raw support: Parses server flags, loads `server.properties` and EULA, validates or loads world data, constructs `WorldStem` via `WorldLoader.load(...)`, then starts `DedicatedServer` through `MinecraftServer.spin(...)` and installs a shutdown hook.
Related symbols/files: `net.minecraft.server.WorldLoader`, `net.minecraft.server.WorldStem`, `net.minecraft.server.dedicated.DedicatedServer`, `net.minecraft.server.MinecraftServer.spin`
Confidence: High
Unknowns: This is the process bootstrap boundary, but it does not itself expose a lifecycle callback.
Orchestrator verification needed: Confirm whether the target boundary should be anchored at process entry (`Main.main`) or at server-thread start (`MinecraftServer.spin`).

## Evidence 2

Evidence ID: SL-002
Concept: Server lifecycle
Worker: retrieval worker
Source artifact: `.research-src/common/net/minecraft/server/MinecraftServer.java` and `.research-src/common/net/minecraft/server/dedicated/DedicatedServer.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java`
Class: `net.minecraft.server.MinecraftServer`
Member: `public MinecraftServer(...)`
Descriptor: source signature only in this packet
Line range or local reference: `333-384`
Exact command used: `nl -ba .research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '300,860p'`
Why this matched: This constructor is where the shared server object is assembled from `WorldStem`, storage, registries, data packs, function manager, and tick/runtime services.
Raw support: Copies `worldStem.registries()`, wraps `worldStem.resourceManager()` and `worldStem.dataPackResources()` into `ReloadableResources`, creates `ServerConnectionListener`, `ServerTickRateManager`, `ServerFunctionManager`, `StructureTemplateManager`, and initializes clock, timers, and saved data.
Related symbols/files: `net.minecraft.server.WorldStem`, `net.minecraft.server.ReloadableServerResources`, `net.minecraft.server.MinecraftServer.ReloadableResources`, `net.minecraft.server.dedicated.DedicatedServer.<init>`
Confidence: High
Unknowns: Construction is shared between dedicated and integrated servers; this packet does not decide whether a later hook should bind at base constructor end or subclass construction end.
Orchestrator verification needed: Decide whether “server constructed” means base `MinecraftServer` construction complete or subclass-specific setup complete.

## Evidence 3

Evidence ID: SL-003
Concept: Server lifecycle
Worker: retrieval worker
Source artifact: `.research-src/common/net/minecraft/server/WorldLoader.java` and `.research-src/common/net/minecraft/server/ReloadableServerResources.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/WorldLoader.java`
Class: `net.minecraft.server.WorldLoader`
Member: `public static <D, R> CompletableFuture<R> load(...)`
Descriptor: source signature only in this packet
Line range or local reference: `25-86`
Exact command used: `nl -ba .research-src/common/net/minecraft/server/ReloadableServerResources.java | sed -n '1,240p' && nl -ba .research-src/common/net/minecraft/server/WorldLoader.java | sed -n '1,260p'`
Why this matched: This is the main datapack + registry loading chain used before server construction.
Raw support: Creates a resource manager from selected packs, loads static tags, loads worldgen registries, loads dimension registries, runs the supplied world-data factory, then calls `ReloadableServerResources.loadResources(...)` and applies `updateComponentsAndStaticRegistryTags()` before producing the result object.
Related symbols/files: `net.minecraft.server.ReloadableServerResources.loadResources`, `net.minecraft.server.WorldStem`, `net.minecraft.world.level.storage.LevelStorageSource.getPackConfig`, `net.minecraft.server.Main.main`
Confidence: High
Unknowns: This is the bootstrap resource-load path, not the later runtime reload path.
Orchestrator verification needed: Confirm whether the lifecycle concept needs both bootstrap loading and runtime reload, or only the bootstrap boundary.

## Evidence 4

Evidence ID: SL-004
Concept: Server lifecycle
Worker: retrieval worker
Source artifact: `.research-src/common/net/minecraft/server/MinecraftServer.java` and `.research-src/common/net/minecraft/server/dedicated/DedicatedServer.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/dedicated/DedicatedServer.java`
Class: `net.minecraft.server.dedicated.DedicatedServer`
Member: `protected boolean initServer() throws IOException`
Descriptor: source signature only in this packet
Line range or local reference: `200-338`
Exact command used: `nl -ba .research-src/common/net/minecraft/server/dedicated/DedicatedServer.java | sed -n '1,220p' && nl -ba .research-src/common/net/minecraft/server/dedicated/DedicatedServer.java | sed -n '220,380p'`
Why this matched: This is the dedicated-server startup phase after object construction and before the main server loop begins.
Raw support: Starts the JSON-RPC management server if enabled, starts the console reader thread, logs version/property setup, binds the TCP listener, performs old-user conversion, installs the player list, resolves offline users, calls `loadLevel()`, starts query/RCON/watchdog/JMX services, then calls `saveEverything(false, true, true)` and `notificationManager().serverStarted()`.
Related symbols/files: `net.minecraft.server.MinecraftServer.loadLevel`, `net.minecraft.server.MinecraftServer.runServer`, `net.minecraft.server.MinecraftServer.spin`, `net.minecraft.server.level.progress.LoggingLevelLoadListener`
Confidence: High
Unknowns: `notificationManager().serverStarted()` is the clearest explicit “started” marker here, but the exact intended hook point may still be earlier or later in the startup sequence.
Orchestrator verification needed: Check whether the stable “server started” boundary should be keyed to `loadLevel()` completion, post-save completion, or the `serverStarted()` notification.

## Evidence 5

Evidence ID: SL-005
Concept: Server lifecycle
Worker: retrieval worker
Source artifact: `.research-src/common/net/minecraft/server/MinecraftServer.java` and `.research-src/common/net/minecraft/server/dedicated/DedicatedServer.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java`
Class: `net.minecraft.server.MinecraftServer`
Member: `protected void runServer()`
Descriptor: `()V`
Line range or local reference: `746-825`
Exact command used: `nl -ba .research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '730,860p'`
Why this matched: This is the long-running server tick loop and the main running-state boundary.
Raw support: Calls `initServer()`, initializes tick timing and server status, loops while `this.running`, marks `this.isReady = true` during the loop, and in the `finally` block sets `this.stopped = true`, calls `stopServer()`, and then `onServerExit()`.
Related symbols/files: `net.minecraft.server.MinecraftServer.halt`, `net.minecraft.server.MinecraftServer.isRunning`, `net.minecraft.server.MinecraftServer.isStopped`, `net.minecraft.server.MinecraftServer.stopServer`, `net.minecraft.server.dedicated.DedicatedServer.stopServer`
Confidence: High
Unknowns: This establishes the runtime run/stop boundary, but the precise meaning of `isReady = true` is not independently documented in-source.
Orchestrator verification needed: Confirm whether “running” should be treated as the `while (running)` loop, the `isReady` flip, or both.

## Evidence 6

Evidence ID: SL-006
Concept: Server lifecycle
Worker: retrieval worker
Source artifact: `.research-src/common/net/minecraft/server/MinecraftServer.java` and `.research-src/common/net/minecraft/server/dedicated/DedicatedServer.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java`
Class: `net.minecraft.server.MinecraftServer`
Member: `public void halt(boolean wait)` / `protected void stopServer()` / `public boolean isStopped()`
Descriptor: `halt(Z)V`, `stopServer()V`, `isStopped()Z`
Line range or local reference: `662-721`, `731-743`, `1405-1407`
Exact command used: `nl -ba .research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '620,740p' && nl -ba .research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '1390,1425p' && nl -ba .research-src/common/net/minecraft/server/dedicated/DedicatedServer.java | sed -n '776,812p'`
Why this matched: These are the public shutdown request, the shared stop implementation, and the stopped-state accessor.
Raw support: `halt(false)` sets `running = false` and may join the server thread; `stopServer()` closes packet processing, saves players/worlds, closes data storage/resources, and unlocks storage; `isStopped()` exposes the `stopped` flag; the dedicated override emits `notificationManager().serverShuttingDown()` before delegating and shuts down executors after.
Related symbols/files: `net.minecraft.server.MinecraftServer.runServer`, `net.minecraft.server.MinecraftServer.close`, `net.minecraft.server.dedicated.DedicatedServer.onServerExit`, `net.minecraft.server.dedicated.DedicatedServer.stopServer`
Confidence: High
Unknowns: `halt()` is the external stop request, but the “fully stopped” boundary is the `runServer()` finally block and `onServerExit()` ordering.
Orchestrator verification needed: Decide whether the stopping boundary should be anchored at `halt(false)`, `notificationManager().serverShuttingDown()`, or entry into `stopServer()`.

## Evidence 7

Evidence ID: SL-007
Concept: Server lifecycle
Worker: retrieval worker
Source artifact: `.research-src/common/net/minecraft/server/MinecraftServer.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java`
Class: `net.minecraft.server.MinecraftServer`
Member: `public CompletableFuture<Void> reloadResources(Collection<String> packsToEnable)`
Descriptor: `reloadResources(Ljava/util/Collection;)Ljava/util/concurrent/CompletableFuture;`
Line range or local reference: `1539-1588`
Exact command used: `nl -ba .research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '1480,1595p'`
Why this matched: This is the runtime resource-reload boundary after the server is already running.
Raw support: Opens selected packs, builds a new `MultiPackResourceManager`, loads postponed registry tags, calls `ReloadableServerResources.loadResources(...)`, swaps the current resource bundle, updates components and recipe loading, reloads player resources, replaces the function library, refreshes the structure template manager, and recomputes fuel values.
Related symbols/files: `net.minecraft.server.ReloadableServerResources.loadResources`, `net.minecraft.server.MinecraftServer.ReloadableResources`, `net.minecraft.server.WorldLoader.load`, `net.minecraft.server.packs.repository.PackRepository`
Confidence: High
Unknowns: This is clearly a post-start reload path, but whether it belongs in the primary lifecycle concept or a separate resources concept needs review.
Orchestrator verification needed: Confirm whether runtime reload belongs in the server-lifecycle concept or should be split into resources/datapacks research only.
