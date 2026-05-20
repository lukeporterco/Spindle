# Ticking Confirmation Evidence

## TC-001
Evidence ID: TC-001
Concept: server tick
Worker: GPT-5.4-Mini retrieval worker
Source artifact: MinecraftServer.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java
Class: net.minecraft.server.MinecraftServer
Member: tickServer
Descriptor if available: unknown
Line range or local reference: 982-1026
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '982,1026p'
Why this matched: Required term `tickServer` and direct server tick loop body.
Raw support: `protected void tickServer(final BooleanSupplier haveTime)`; `this.tickCount++;`; `this.tickRateManager.tick();`; `this.tickChildren(haveTime);`; tick timing accumulation follows.
Related symbols/files: processPacketsAndTick, logTickMethodTime, tickChildren, tickTimesNanos, aggregatedTickTimesNanos
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: yes
Decision impact: needs orchestrator review

## TC-002
Evidence ID: TC-002
Concept: child/level tick
Worker: GPT-5.4-Mini retrieval worker
Source artifact: MinecraftServer.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java
Class: net.minecraft.server.MinecraftServer
Member: tickChildren
Descriptor if available: unknown
Line range or local reference: 1111-1163
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '1111,1163p'
Why this matched: Required term `tickChildren` and direct child world tick dispatch.
Raw support: `protected void tickChildren(final BooleanSupplier haveTime)`; `this.getFunctions().tick();`; `this.clockManager.tick();`; `level.tick(haveTime);`; `this.playerList.tick();`; `GameTestTicker.SINGLETON.tick();`
Related symbols/files: ServerLevel.tick, tickRateManager.runsNormally, forceGameTimeSynchronization, tickConnection
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: yes
Decision impact: needs orchestrator review

## TC-003
Evidence ID: TC-003
Concept: child/level tick
Worker: GPT-5.4-Mini retrieval worker
Source artifact: ServerLevel.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/server/level/ServerLevel.java
Class: net.minecraft.server.level.ServerLevel
Member: tick
Descriptor if available: unknown
Line range or local reference: 345-392
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/level/ServerLevel.java | sed -n '345,392p'
Why this matched: Required term `ServerLevel.tick` and direct per-level tick body.
Raw support: `public void tick(final BooleanSupplier haveTime)`; `TickRateManager tickRateManager = this.tickRateManager();`; `this.blockTicks.tick(tick, 65536, this::tickBlock);`; `this.fluidTicks.tick(tick, 65536, this::tickFluid);`; `this.getChunkSource().tick(haveTime, true);`
Related symbols/files: blockTicks, fluidTicks, tickRateManager(), runBlockEvents, raids.tick
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: yes
Decision impact: needs orchestrator review

## TC-004
Evidence ID: TC-004
Concept: subsystem tick
Worker: GPT-5.4-Mini retrieval worker
Source artifact: LevelTicks.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/world/ticks/LevelTicks.java
Class: net.minecraft.world.ticks.LevelTicks
Member: tick
Descriptor if available: unknown
Line range or local reference: 31-90
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/world/ticks/LevelTicks.java | sed -n '31,90p'
Why this matched: Required term `LevelTicks` and direct tick-processing subsystem body.
Raw support: `public class LevelTicks<T> implements LevelTickAccess<T>`; `public void tick(final long currentTick, final int maxTicksToProcess, final BiConsumer<BlockPos, T> output)`; `collectTicks(...)`; `runCollectedTicks(output)`; `cleanupAfterTick()`
Related symbols/files: sortContainersToTick, drainContainers, toRunThisTick, nextTickForContainer
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: yes
Decision impact: needs orchestrator review

## TC-005
Evidence ID: TC-005
Concept: tickRateManager
Worker: GPT-5.4-Mini retrieval worker
Source artifact: MinecraftServer.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java
Class: net.minecraft.server.MinecraftServer
Member: tickRateManager
Descriptor if available: unknown
Line range or local reference: 305, 359, 1004, 1117, 1058-1063, 1828-1830
Exact command used: rg -n --no-heading -S "tickRateManager" v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java && nl -ba v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '292,330p'
Why this matched: Required term `tickRateManager` appears in field declaration, accessor, and tick-loop calls.
Raw support: `private final ServerTickRateManager tickRateManager;`; `this.tickRateManager = new ServerTickRateManager(this);`; `this.tickRateManager.tick();`; `this.tickRateManager.runsNormally();`; `this.tickRateManager.isSprinting();`; `this.tickRateManager.tickrate();`
Related symbols/files: ServerLevel.tickRateManager(), getAverageTickTimeNanos, computeNextAutosaveInterval
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: yes
Decision impact: needs orchestrator review

## TC-006
Evidence ID: TC-006
Concept: task execution
Worker: GPT-5.4-Mini retrieval worker
Source artifact: MinecraftServer.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java
Class: net.minecraft.server.MinecraftServer
Member: waitUntilNextTick
Descriptor if available: unknown
Line range or local reference: 882-889
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '874,891p'
Why this matched: Required term `runAllTasks` and direct task-pump call site.
Raw support: `private boolean haveTime()`; `this.runAllTasks();`; `this.managedBlock(() -> !this.haveTime());`
Related symbols/files: haveTime, managedBlock, waitForTasks, shouldRun
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: yes
Decision impact: needs orchestrator review

## TC-007
Evidence ID: TC-007
Concept: task execution
Worker: GPT-5.4-Mini retrieval worker
Source artifact: MinecraftServer.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java
Class: net.minecraft.server.MinecraftServer
Member: haveTime
Descriptor if available: unknown
Line range or local reference: 874-876, 887, 909, 923, 1035
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '874,933p'
Why this matched: Required term `haveTime` and direct use as a tick/task gate.
Raw support: `private boolean haveTime()`; `return this.runningTask() || Util.getNanos() < (...)`; `managedBlock(() -> !this.haveTime())`; `return task.getTick() + 3 < this.tickCount || this.haveTime();`; `this.tickServer(sprinting ? () -> false : this::haveTime);`
Related symbols/files: runningTask, shouldRun, pollTaskInternal, tickServer
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: yes
Decision impact: needs orchestrator review

## TC-008
Evidence ID: TC-008
Concept: tick timing fields
Worker: GPT-5.4-Mini retrieval worker
Source artifact: MinecraftServer.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java
Class: net.minecraft.server.MinecraftServer
Member: smoothedTickTimeMillis
Descriptor if available: unknown
Line range or local reference: 299, 802, 1824-1825, 1888-1890
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '292,305p'; nl -ba v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '800,803p'; nl -ba v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '1824,1825p'; nl -ba v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '1884,1890p'
Why this matched: Required term `averageTickTime` maps to the server's smoothed tick-time field and report output.
Raw support: `private float smoothedTickTimeMillis;`; `JvmProfiler.INSTANCE.onServerTick(this.smoothedTickTimeMillis);`; `public float getCurrentSmoothedTickTime()`; `output.write(String.format(Locale.ROOT, "average_tick_time: %f\n", this.getCurrentSmoothedTickTime()));`
Related symbols/files: tickTimesNanos, aggregatedTickTimesNanos, getAverageTickTimeNanos, logTickMethodTime
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: yes
Decision impact: needs orchestrator review

## TC-009
Evidence ID: TC-009
Concept: tickCount
Worker: GPT-5.4-Mini retrieval worker
Source artifact: MinecraftServer.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java
Class: net.minecraft.server.MinecraftServer
Member: tickCount
Descriptor if available: unknown
Line range or local reference: 257, 777, 905, 1003, 1019, 1451-1452, 1833, 2163
Exact command used: rg -n --no-heading -S "tickCount" v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java && nl -ba v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '253,266p'
Why this matched: Required term `tickCount` appears as the server tick counter and is used for task and timing calculations.
Raw support: `private int tickCount;`; `this.debugCommandProfiler = new MinecraftServer.TimeProfiler(Util.getNanos(), this.tickCount);`; `return new TickTask(this.tickCount, runnable);`; `this.tickCount++;`; `int tickIndex = this.tickCount % 100;`; `public int getTickCount() { return this.tickCount; }`; `return this.aggregatedTickTimesNanos / Math.min(100, Math.max(this.tickCount, 1));`
Related symbols/files: TimeProfiler, TickTask, tickTimesNanos, aggregatedTickTimesNanos, getAverageTickTimeNanos
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: yes
Decision impact: needs orchestrator review
