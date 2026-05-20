# Ticking Evidence

Minecraft version: 26.1.2
Mapping namespace: Yarn named

## Evidence ID: ticking-001
Concept: Ticking
Worker: retrieval worker
Source artifact: `.research-src/common/net/minecraft/server/MinecraftServer.java`
Mapping namespace: Yarn named
File path: `.research-src/common/net/minecraft/server/MinecraftServer.java`
Class: `MinecraftServer`
Member: `runServer`, `processPacketsAndTick`, `tickServer`, `tickChildren`, `haveTime`, `tickCount`
Descriptor: `runServer()V`, `processPacketsAndTick(Z)V`, `tickServer(Ljava/util/function/BooleanSupplier;)V`, `tickChildren(Ljava/util/function/BooleanSupplier;)V`, `haveTime()Z`
Line range or local reference: `746-803`, `874-875`, `982-1038`, `1111-1165`
Exact command used: `nl -ba .research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '730,1165p'`
Why this matched: This is the main server loop and the methods that define one server tick boundary.
Raw support: `runServer()` loops while running, advances `nextTickTimeNanos`, then calls `processPacketsAndTick(sprinting)`. `processPacketsAndTick` calls `tickServer(...)`. `tickServer` increments `tickCount` once, then calls `tickChildren(haveTime)`. `haveTime()` gates task execution between ticks, but it is not the tick boundary itself.
Related symbols/files: `net.minecraft.server.dedicated.ServerWatchdog`, `net.minecraft.server.TickTask`, `net.minecraft.server.level.ServerLevel`
Confidence: High
Unknowns: This proves the server-wide cadence and the ordering around it, but not a Spindle API shape.
Orchestrator verification needed: Confirm whether the intended hook should sit before `tickChildren`, after `tickChildren`, or around the whole `tickServer` body.

## Evidence ID: ticking-002
Concept: Ticking
Worker: retrieval worker
Source artifact: `.research-src/common/net/minecraft/server/level/ServerLevel.java`
Mapping namespace: Yarn named
File path: `.research-src/common/net/minecraft/server/level/ServerLevel.java`
Class: `ServerLevel`
Member: `tick`, `tickTime`, `handlingTick`, `tickCount` usage elsewhere in class
Descriptor: `tick(Ljava/util/function/BooleanSupplier;)V`, `tickTime()V`
Line range or local reference: `345-399`, `465-472`, `632-633`
Exact command used: `nl -ba .research-src/common/net/minecraft/server/level/ServerLevel.java | sed -n '320,560p'`
Why this matched: This is the per-level tick entry called from `MinecraftServer.tickChildren`.
Raw support: `ServerLevel.tick(haveTime)` sets `handlingTick = true`, performs level-local work, then clears `handlingTick = false`. Inside the method, `tickTime()` advances level game time and scheduled functions only when `tickTime` is enabled: it computes `levelData.getGameTime() + 1L`, stores it back, then calls `server.getScheduledEvents().tick(this.server, time)`. This makes the level tick boundary distinct from the server-wide `tickCount` boundary.
Related symbols/files: `net.minecraft.server.MinecraftServer.tickChildren`, `net.minecraft.server.level.ServerChunkCache.tick`, `net.minecraft.world.ticks.LevelTicks.tick`
Confidence: High
Unknowns: The method body includes multiple subsystems; this does not by itself decide which sub-bounds are safest for hooks.
Orchestrator verification needed: Confirm whether a per-level hook should be conceptually tied to `ServerLevel.tick` or to a narrower subsystem inside it.

## Evidence ID: ticking-003
Concept: Ticking
Worker: retrieval worker
Source artifact: `.research-src/common/net/minecraft/server/MinecraftServer.java`
Mapping namespace: Yarn named
File path: `.research-src/common/net/minecraft/server/MinecraftServer.java`
Class: `MinecraftServer`
Member: `tickChildren`
Descriptor: `tickChildren(Ljava/util/function/BooleanSupplier;)V`
Line range or local reference: `1111-1165`
Exact command used: `nl -ba .research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '1111,1165p'`
Why this matched: This method is the server-to-level fanout point.
Raw support: `tickChildren` iterates `for (ServerLevel level : this.getAllLevels())` and calls `level.tick(haveTime)` for each level. That means a server tick contains one level tick per loaded server level, not one global level tick total.
Related symbols/files: `ServerLevel.tick`, `ServerLevel.tickTime`, `MinecraftServer.getAllLevels`
Confidence: High
Unknowns: The exact iteration order of levels is not part of this evidence packet.
Orchestrator verification needed: Confirm whether downstream composition expects one callback per level or only a single callback on the server boundary.

## Evidence ID: ticking-004
Concept: Ticking
Worker: retrieval worker
Source artifact: `.research-src/common/net/minecraft/server/level/ServerLevel.java`
Mapping namespace: Yarn named
File path: `.research-src/common/net/minecraft/server/level/ServerLevel.java`
Class: `ServerLevel`
Member: `blockTicks`, `fluidTicks`, `chunkSource.tick`, `entityTickList`, `tickPending`
Descriptor: `tick(Ljava/util/function/BooleanSupplier;)V`
Line range or local reference: `376-397`, `410-447`
Exact command used: `nl -ba .research-src/common/net/minecraft/server/level/ServerLevel.java | sed -n '430,560p'`
Why this matched: These are the level-local subsystem tick calls nested inside the per-level tick boundary.
Raw support: Inside `ServerLevel.tick`, after `tickTime()`, the method runs `blockTicks.tick(tick, 65536, this::tickBlock)` and `fluidTicks.tick(tick, 65536, this::tickFluid)` only when not in debug and when `runs` is true. It then ticks the chunk source with `this.getChunkSource().tick(haveTime, true)`, runs block events, and later ticks entities and block entities. These are level/subsystem ticks, not server-wide ticks.
Related symbols/files: `net.minecraft.world.ticks.LevelTicks`, `net.minecraft.server.level.ServerChunkCache`, `net.minecraft.world.level.chunk.ChunkSource`
Confidence: High
Unknowns: This shows composition and nesting, but not whether all of these subsystems should be exposed separately.
Orchestrator verification needed: Confirm whether a hook should target the level boundary or one of these subordinate schedulers.

## Evidence ID: ticking-005
Concept: Ticking
Worker: retrieval worker
Source artifact: `.research-src/common/net/minecraft/server/level/ServerChunkCache.java`
Mapping namespace: Yarn named
File path: `.research-src/common/net/minecraft/server/level/ServerChunkCache.java`
Class: `ServerChunkCache`
Member: `tick`, `tickChunks`, `chunkMap.tick`, `runDistanceManagerUpdates`
Descriptor: `tick(Ljava/util/function/BooleanSupplier;Z)V`
Line range or local reference: `320-337`, `340-355`
Exact command used: `nl -ba .research-src/common/net/minecraft/server/level/ServerChunkCache.java | sed -n '300,360p'`
Why this matched: This is a per-level chunk subsystem tick, reached from `ServerLevel.tick`.
Raw support: `tick(BooleanSupplier haveTime, boolean tickChunks)` performs ticket purging, distance-manager updates, optional chunk ticking, then `chunkMap.tick(haveTime)` and cache clearing. The method is nested under the level tick path, so it is not a server-wide once-per-tick boundary.
Related symbols/files: `ServerLevel.tick`, `ChunkMap.tick`, `ServerChunkCache.tickChunks`
Confidence: High
Unknowns: The exact schedule semantics of `chunkMap.tick(haveTime)` are broader than this snippet.
Orchestrator verification needed: Confirm whether chunk ticking is just a subsystem concern and should not be mistaken for the main tick boundary.

## Evidence ID: ticking-006
Concept: Ticking
Worker: retrieval worker
Source artifact: `.research-src/common/net/minecraft/world/ticks/LevelTicks.java`
Mapping namespace: Yarn named
File path: `.research-src/common/net/minecraft/world/ticks/LevelTicks.java`
Class: `LevelTicks<T>`
Member: `tick`, `collectTicks`, `drainContainers`, `runCollectedTicks`, `cleanupAfterTick`
Descriptor: `tick(JILjava/util/function/BiConsumer;)V`
Line range or local reference: `81-98`, `126-199`
Exact command used: `nl -ba .research-src/common/net/minecraft/world/ticks/LevelTicks.java | sed -n '1,260p'`
Why this matched: This is the scheduled tick scheduler used for block/fluid tick processing.
Raw support: `LevelTicks.tick(currentTick, maxTicksToProcess, output)` collects due scheduled ticks, drains containers into `toRunThisTick`, runs them through the provided `BiConsumer<BlockPos, T>`, and then clears per-tick state. Because it takes `currentTick` and is fed from `ServerLevel.tick`, it is a per-level subsystem scheduler rather than a server-wide boundary.
Related symbols/files: `ServerLevel.blockTicks`, `ServerLevel.fluidTicks`, `LevelChunkTicks`, `ScheduledTick`
Confidence: High
Unknowns: This evidence covers scheduler behavior, not every possible caller.
Orchestrator verification needed: Confirm whether scheduled block/fluid callbacks need a separate primitive from the level tick boundary.

## Evidence ID: ticking-007
Concept: Ticking
Worker: retrieval worker
Source artifact: `.research-src/common/net/minecraft/server/dedicated/ServerWatchdog.java`
Mapping namespace: Yarn named
File path: `.research-src/common/net/minecraft/server/dedicated/ServerWatchdog.java`
Class: `ServerWatchdog`
Member: `run`, `maxTickTimeNanos`, `server.getNextTickTime`
Descriptor: `run()V`
Line range or local reference: `36-71`
Exact command used: `nl -ba .research-src/common/net/minecraft/server/dedicated/ServerWatchdog.java | sed -n '1,120p'`
Why this matched: This cross-checks the server-wide cadence from a separate monitoring path.
Raw support: The watchdog compares `Util.getNanos()` against `server.getNextTickTime()` and reports when “a single server tick” exceeds the max tick time. That reinforces that `nextTickTimeNanos` in `MinecraftServer` is the server-tick cadence, not a per-level cadence.
Related symbols/files: `MinecraftServer.nextTickTimeNanos`, `MinecraftServer.runServer`, `MinecraftServer.tickServer`
Confidence: Medium
Unknowns: This is monitoring evidence, not the tick loop itself.
Orchestrator verification needed: Use this only as corroboration, not as the primary boundary definition.
