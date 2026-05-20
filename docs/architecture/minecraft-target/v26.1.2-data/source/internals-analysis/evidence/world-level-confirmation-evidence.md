# World Level Confirmation Evidence

Minecraft version: 26.1.2
Mapping namespace: Yarn named

Evidence ID: WLC-001
Concept: World and level
Worker: GPT-5.4-Mini retrieval worker
Source artifact: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java`
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java`
Class: `net.minecraft.server.MinecraftServer`
Member: `loadLevel()`, `createLevels()`, `prepareLevels()`
Descriptor if available: not available from source scan
Line range or local reference: `421-429`, `446-505`, `570-593`
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '420,620p'`
Why this matched: This is the direct startup path that creates loaded `ServerLevel` instances and then waits for initial chunk preparation.
Raw support: `loadLevel()` calls `createLevels()` and `prepareLevels()`. `createLevels()` constructs the overworld `ServerLevel`, stores it in `this.levels`, then iterates `dimensions.entrySet()` and creates each non-overworld `ServerLevel` into the same map. `prepareLevels()` tracks each level with a `ChunkLoadCounter`, repeatedly waits while `pendingChunks() > 0`, and then finishes the initial chunk-load stage.
Related symbols/files: `net.minecraft.server.level.ServerLevel`, `net.minecraft.world.level.dimension.LevelStem`, `net.minecraft.server.level.progress.LevelLoadListener`, `java.util.Map<ResourceKey<Level>, ServerLevel> levels`
Confidence: high
Unknowns: needs orchestrator review for any later startup boundary beyond `prepareLevels()`
Orchestrator verification: confirm whether this is the intended post-creation world-ready checkpoint.
Decision impact: source-backed startup evidence for loaded-world creation and initial chunk prep

Evidence ID: WLC-002
Concept: World and level
Worker: GPT-5.4-Mini retrieval worker
Source artifact: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java`
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java`
Class: `net.minecraft.server.MinecraftServer`
Member: `levels`, `overworld()`, `getLevel(ResourceKey<Level>)`, `levelKeys()`, `getAllLevels()`, `findRespawnDimension()`, `saveDebugReport(...)`
Descriptor if available: not available from source scan
Line range or local reference: `253`, `1209-1223`, `1715-1725`, `1862-1870`
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '244,258p'; nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '1180,1260p'; nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '1708,1730p'; nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '1858,1874p'`
Why this matched: These are the runtime accessors and enumeration surfaces for loaded server worlds.
Raw support: `levels` is a `LinkedHashMap<ResourceKey<Level>, ServerLevel>`. `overworld()` returns `this.levels.get(Level.OVERWORLD)`. `getLevel(...)` returns a nullable lookup by dimension key. `levelKeys()` exposes the key set. `getAllLevels()` exposes the map values. `findRespawnDimension()` reads the respawn dimension from world data, resolves it through `getLevel(...)`, and falls back to `overworld()` when absent. `saveDebugReport(...)` iterates `this.levels.entrySet()` and emits one report directory per loaded level.
Related symbols/files: `net.minecraft.world.level.Level`, `net.minecraft.resources.ResourceKey`, `net.minecraft.server.level.ServerLevel`
Confidence: high
Unknowns: needs orchestrator review for whether live map views are acceptable at the Target Layer boundary
Orchestrator verification: confirm whether enumeration should stay live through `getAllLevels()` or be wrapped elsewhere.
Decision impact: runtime loaded-world access is map-backed and nullable by dimension key

Evidence ID: WLC-003
Concept: World and level
Worker: GPT-5.4-Mini retrieval worker
Source artifact: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/world/level/storage/LevelStorageSource.java`
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/world/level/storage/LevelStorageSource.java`
Class: `net.minecraft.world.level.storage.LevelStorageSource` and `LevelStorageSource.LevelStorageAccess`
Member: `findLevelCandidates()`, `loadLevelSummaries(...)`, `getLevelPath(...)`, `validateAndCreateAccess(...)`, `createAccess(...)`, `LevelStorageAccess.getLevelId()`, `LevelStorageAccess.getLevelPath(LevelResource)`, `LevelStorageAccess.getDimensionPath(ResourceKey<Level>)`, `LevelStorageAccess.hasWorldData()`
Descriptor if available: not available from source scan
Line range or local reference: `225-299`, `390-433`, `484-780`
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/world/level/storage/LevelStorageSource.java | sed -n '220,330p'; nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/world/level/storage/LevelStorageSource.java | sed -n '390,520p'; nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/world/level/storage/LevelStorageSource.java | sed -n '520,780p'`
Why this matched: This is the save-folder discovery, locking, and per-dimension path API surface.
Raw support: `findLevelCandidates()` enumerates world directories under the base directory and filters to directories containing `level.dat` or `level.dat_old`. `loadLevelSummaries(...)` reads those candidates asynchronously. `getLevelPath(...)` resolves a level ID under the base directory. `validateAndCreateAccess(...)` validates the level directory before constructing `LevelStorageAccess`. `createAccess(...)` constructs `LevelStorageAccess` directly. `LevelStorageAccess` keeps a `DirectoryLock`, exposes `getLevelId()`, resolves resource paths with `getLevelPath(LevelResource)`, resolves dimension storage folders with `getDimensionPath(ResourceKey<Level>)`, and reports `hasWorldData()` by checking the current or fallback level data files.
Related symbols/files: `LevelSummary`, `DirectoryLock`, `LevelResource`, `PlayerDataStorage`, `WorldData`
Confidence: high
Unknowns: needs orchestrator review for whether storage-path APIs should remain distinct from runtime level access
Orchestrator verification: confirm the save-folder boundary stays separate from live `ServerLevel` access.
Decision impact: storage enumeration and access are file/path/lock oriented, not runtime world lookup

Evidence ID: WLC-004
Concept: World and level
Worker: GPT-5.4-Mini retrieval worker
Source artifact: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/level/ServerLevel.java`
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/level/ServerLevel.java`
Class: `net.minecraft.server.level.ServerLevel`
Member: constructor `ServerLevel(...)`, `tick(final BooleanSupplier haveTime)`, `tickTime()`
Descriptor if available: not available from source scan
Line range or local reference: `229-311`, `345-458`, `465-472`
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/level/ServerLevel.java | sed -n '220,330p'; nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/level/ServerLevel.java | sed -n '345,520p'`
Why this matched: This is the per-dimension live world object construction and ticking site.
Raw support: The constructor receives `LevelStorageSource.LevelStorageAccess`, `ServerLevelData`, and `ResourceKey<Level>`; it creates entity storage using `levelStorage.getDimensionPath(dimension).resolve("entities")`, passes the same storage access into `ServerChunkCache`, and initializes world systems before calling `ensureStructuresGenerated()`. `tick(...)` runs world-update logic, and `tickTime()` advances `this.serverLevelData` by calling `setGameTime(time)` before ticking scheduled events.
Related symbols/files: `net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess`, `net.minecraft.world.level.storage.ServerLevelData`, `net.minecraft.world.level.Level`, `net.minecraft.world.level.dimension.LevelStem`
Confidence: high
Unknowns: needs orchestrator review for how much of this constructor/tick wiring should be surfaced as a public runtime API
Orchestrator verification: confirm whether the live world object itself or only its accessors belong at the Target Layer boundary.
Decision impact: direct evidence of runtime world lifecycle and tick progression on `ServerLevel`

Evidence ID: WLC-005
Concept: World and level
Worker: GPT-5.4-Mini retrieval worker
Source artifact: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/world/level/storage/ServerLevelData.java`
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/world/level/storage/ServerLevelData.java`
Class: `net.minecraft.world.level.storage.ServerLevelData`
Member: `isInitialized()`, `setInitialized(boolean)`, `setGameTime(long)`, `getLevelName()`, `getGameType()`
Descriptor if available: not available from source scan
Line range or local reference: `9-39`
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/world/level/storage/ServerLevelData.java | sed -n '1,240p'`
Why this matched: This interface is the server-level data contract used by world creation and time progression.
Raw support: `ServerLevelData` extends `WritableLevelData`, adds `getLevelName()`, `isInitialized()`, `setInitialized(boolean)`, and `setGameTime(long)`, and its crash-report helper prints the level name and game mode details. `MinecraftServer.createLevels()` checks `levelData.isInitialized()` before initial spawn work, and `ServerLevel.tickTime()` calls `this.serverLevelData.setGameTime(time)`.
Related symbols/files: `net.minecraft.world.level.storage.WritableLevelData`, `net.minecraft.server.MinecraftServer`, `net.minecraft.server.level.ServerLevel`
Confidence: high
Unknowns: needs orchestrator review for whether initialization state should be exposed separately from runtime ticking
Orchestrator verification: confirm whether `isInitialized` and `setGameTime` are enough evidence for the intended lifecycle boundary.
Decision impact: data contract evidence for world initialization and server time updates
