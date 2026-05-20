# World And Level Evidence

Minecraft version: 26.1.2
Mapping namespace: Yarn named

## Evidence ID: WN-001
Concept: World and level
Worker: retrieval
Source artifact: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/Main.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/Main.java`
Class: `net.minecraft.server.Main`
Member: `main(...)`
Descriptor: not available from source scan
Line range or local reference: `127-185`
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/Main.java | sed -n '110,210p'`
Why this matched: This is the top-level server startup path that loads world data and constructs the server-world container.
Raw support: `LevelStorageSource.validateAndCreateAccess(levelName)` creates the locked storage access; existing world data is read through `getUnfixedDataTagWithFallback()` and `LevelStorageSource.getLevelDataAndDimensions(...)`; `WorldLoader.load(...)` returns a `WorldStem` that is passed into `new DedicatedServer(...)`.
Related symbols/files: `net.minecraft.server.WorldLoader`, `net.minecraft.server.WorldStem`, `net.minecraft.world.level.storage.LevelStorageSource`
Confidence: High
Unknowns: This shows the load/creation pipeline, but not the final Target Layer safety boundary.
Orchestrator verification needed: Confirm whether this startup path is only a pre-bootstrap loader or also the point where a Spindle-facing access boundary should begin.

## Evidence ID: WN-002
Concept: World and level
Worker: retrieval
Source artifact: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/WorldLoader.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/WorldLoader.java`
Class: `net.minecraft.server.WorldLoader`
Member: `load(...)`
Descriptor: not available from source scan
Line range or local reference: `25-85`
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/WorldLoader.java | sed -n '1,140p'`
Why this matched: This is the registry/resource load sequence that happens before `WorldStem` is created.
Raw support: The method opens packs, creates the initial registry layers, loads worldgen registries, then dimension registries, calls the `worldDataSupplier`, reloads server resources, and only then calls `resultFactory.create(...)`.
Related symbols/files: `WorldLoader.PackConfig`, `RegistryDataLoader`, `ReloadableServerResources`, `net.minecraft.server.WorldStem`
Confidence: High
Unknowns: This is load-order evidence, not a direct runtime safety guarantee.
Orchestrator verification needed: Use this if you need the exact pre-`WorldStem` loading sequence, but do not treat it as the final access policy.

## Evidence ID: WN-003
Concept: World and level
Worker: retrieval
Source artifact: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java`
Class: `net.minecraft.server.MinecraftServer`
Member: `loadLevel()`, `createLevels()`, `prepareLevels()`, `runServer()`
Descriptor: not available from source scan
Line range or local reference: `421-505`, `570-593`, `746-802`
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '420,520p'; nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '560,760p'; nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '788,812p'`
Why this matched: This is the main server-side creation and readiness path for loaded level instances.
Raw support: `loadLevel()` calls `createLevels()` and `prepareLevels()`. `createLevels()` constructs the overworld `ServerLevel`, stores it in `this.levels`, then iterates the registered `LevelStem` entries, constructs each non-overworld `ServerLevel`, and stores each in the same map. `prepareLevels()` tracks every loaded level in a `ChunkLoadCounter`, waits until pending chunks are loaded, and then finishes the initial chunk-load stage. `runServer()` sets `this.isReady = true` after the first server tick iteration completes.
Related symbols/files: `net.minecraft.server.level.ServerLevel`, `net.minecraft.world.level.dimension.LevelStem`, `net.minecraft.server.level.progress.LevelLoadListener`
Confidence: High
Unknowns: `isReady` is a coarse server flag, not a world-specific contract.
Orchestrator verification needed: If a Target Layer needs a post-load boundary, this is the strongest source-backed candidate, but the final decision still needs architecture review.

## Evidence ID: WN-004
Concept: World and level
Worker: retrieval
Source artifact: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java`
Class: `net.minecraft.server.MinecraftServer`
Member: `overworld()`, `getLevel(ResourceKey<Level>)`, `levelKeys()`, `getAllLevels()`, `saveDebugReport(...)`
Descriptor: not available from source scan
Line range or local reference: `1209-1223`, `1862-1871`
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '1180,1260p'; nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '1848,1885p'`
Why this matched: These are the runtime enumeration and lookup surfaces for loaded server levels.
Raw support: `overworld()` returns the `Level.OVERWORLD` entry from `this.levels`; `getLevel(...)` returns a nullable map lookup by `ResourceKey<Level>`; `levelKeys()` exposes the key set; `getAllLevels()` exposes the values view; `saveDebugReport(...)` iterates `this.levels.entrySet()` and emits one report directory per loaded level.
Related symbols/files: `java.util.Map<ResourceKey<Level>, ServerLevel> levels`, `net.minecraft.server.level.ServerLevel`
Confidence: High
Unknowns: `getLevel(...)` can return null, so callers must handle missing dimensions explicitly.
Orchestrator verification needed: This is the clearest enumeration surface, but it is just a map view and not an access-policy decision.

## Evidence ID: WN-005
Concept: World and level
Worker: retrieval
Source artifact: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/world/level/storage/LevelStorageSource.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/world/level/storage/LevelStorageSource.java`
Class: `net.minecraft.world.level.storage.LevelStorageSource` and `LevelStorageSource.LevelStorageAccess`
Member: `findLevelCandidates()`, `loadLevelSummaries(...)`, `validateAndCreateAccess(...)`, `createAccess(...)`, `LevelStorageAccess.checkLock()`, `saveDataTag(...)`, `saveLevelData(...)`, `getDimensionPath(...)`
Descriptor: not available from source scan
Line range or local reference: `225-299`, `421-433`, `484-643`, `609-635`
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/world/level/storage/LevelStorageSource.java | sed -n '220,330p'; nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/world/level/storage/LevelStorageSource.java | sed -n '420,560p'; nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/world/level/storage/LevelStorageSource.java | sed -n '550,760p'`
Why this matched: This is the storage-side enumeration, locking, and persistence layer for worlds and per-dimension data.
Raw support: `findLevelCandidates()` enumerates world directories under the base save path and filters to directories with `level.dat` or `level.dat_old`; `loadLevelSummaries(...)` reads those candidates asynchronously; `validateAndCreateAccess(...)` validates the directory before constructing a `LevelStorageAccess`; the access object creates a `DirectoryLock`, uses `checkLock()` before read/write operations, computes per-dimension paths with `getDimensionPath(...)`, and persists world data by writing a temp file then replacing the current/old files in `saveLevelData(...)`.
Related symbols/files: `LevelSummary`, `DirectoryLock`, `LevelResource`, `PlayerDataStorage`, `WorldData`, `WorldGenSettings`
Confidence: High
Unknowns: This proves storage safety and enumeration, not runtime level availability.
Orchestrator verification needed: Treat this as the authoritative save-folder boundary; separate it from runtime `ServerLevel` access when mapping Target Layer behavior.

## Evidence ID: WN-006
Concept: World and level
Worker: retrieval
Source artifact: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/level/ServerLevel.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/level/ServerLevel.java`
Class: `net.minecraft.server.level.ServerLevel`
Member: constructor `ServerLevel(...)`
Descriptor: not available from source scan
Line range or local reference: `229-310`
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/level/ServerLevel.java | sed -n '220,330p'`
Why this matched: This is the per-dimension world object construction site.
Raw support: The constructor receives `LevelStorageAccess`, `ServerLevelData`, `ResourceKey<Level>`, and `LevelStem`; it creates entity storage under `levelStorage.getDimensionPath(dimension).resolve("entities")`, constructs the `ServerChunkCache` with the same storage access, creates `SavedDataStorage` under the dimension data folder via the chunk cache, and calls `ensureStructuresGenerated()` before finishing initialization.
Related symbols/files: `net.minecraft.server.level.ServerChunkCache`, `net.minecraft.world.level.chunk.storage.SimpleRegionStorage`, `net.minecraft.world.level.storage.ServerLevelData`
Confidence: High
Unknowns: This is object construction and internal storage wiring, not a separate safety gate.
Orchestrator verification needed: Use this for per-dimension storage wiring, but keep the broader access policy separate.
