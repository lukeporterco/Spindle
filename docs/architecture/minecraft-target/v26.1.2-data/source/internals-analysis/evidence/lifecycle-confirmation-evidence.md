Evidence ID: LC-001
Concept: Dedicated server initialization orders level load before server-start notification
Worker: GPT-5.4-Mini retrieval worker
Source artifact: Minecraft source decompilation
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/server/dedicated/DedicatedServer.java
Class: net.minecraft.server.dedicated.DedicatedServer
Member: initServer
Descriptor if available: unknown
Line range or local reference: 200-338
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/dedicated/DedicatedServer.java | sed -n '320,860p'
Why this matched: Contains the dedicated-server init method and the post-load startup notification call.
Raw support: `this.loadLevel();` then later `this.notificationManager().serverStarted();` before `return true;`
Related symbols/files: net.minecraft.server.MinecraftServer.loadLevel; net.minecraft.server.notifications.NotificationManager.serverStarted; v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java; v26.1.2/.research-src/common/net/minecraft/server/Main.java
Confidence: high
Unknowns: Orchestrator review needed for whether this is the only startup-complete marker.
Orchestrator verification: confirm no earlier dedicated-server ready signal exists in other startup paths.
Decision impact: start-order evidence only

Evidence ID: LC-002
Concept: Shared server world loading always creates levels before preparing initial chunks
Worker: GPT-5.4-Mini retrieval worker
Source artifact: Minecraft source decompilation
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java
Class: net.minecraft.server.MinecraftServer
Member: loadLevel
Descriptor if available: unknown
Line range or local reference: 421-440
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '360,760p'
Why this matched: The required lifecycle terms appear directly in the load sequence.
Raw support: `this.createLevels();` then `this.forceDifficulty();` then `this.prepareLevels();`
Related symbols/files: net.minecraft.server.MinecraftServer.createLevels; net.minecraft.server.MinecraftServer.prepareLevels; v26.1.2/.research-src/common/net/minecraft/server/dedicated/DedicatedServer.java; v26.1.2/.research-src/client/net/minecraft/client/server/IntegratedServer.java
Confidence: high
Unknowns: Orchestrator review needed for whether createLevels/prepareLevels are sufficient to characterize readiness.
Orchestrator verification: confirm no alternate load path bypasses prepareLevels.
Decision impact: load-order evidence only

Evidence ID: LC-003
Concept: Readiness flag is set inside the main server loop after tick work completes
Worker: GPT-5.4-Mini retrieval worker
Source artifact: Minecraft source decompilation
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java
Class: net.minecraft.server.MinecraftServer
Member: runServer
Descriptor if available: unknown
Line range or local reference: 746-825
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '760,1180p'
Why this matched: The loop contains the `isReady` assignment and the shutdown finally block.
Raw support: `this.isReady = true;` appears after the tick body; later `this.stopped = true; this.stopServer();` in `finally`
Related symbols/files: net.minecraft.server.MinecraftServer.isReady; net.minecraft.server.MinecraftServer.stopServer; net.minecraft.server.MinecraftServer.halt; v26.1.2/.research-src/common/net/minecraft/server/Main.java
Confidence: high
Unknowns: Orchestrator review needed for whether readiness should be treated as first-tick completion or later.
Orchestrator verification: check if any other code mutates isReady before the loop.
Decision impact: readiness evidence only

Evidence ID: LC-004
Concept: Shutdown ordering closes server subsystems after saving and world teardown work
Worker: GPT-5.4-Mini retrieval worker
Source artifact: Minecraft source decompilation
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java
Class: net.minecraft.server.MinecraftServer
Member: close; stopServer; halt
Descriptor if available: unknown
Line range or local reference: 662-721, 735-744
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '360,760p'
Why this matched: The close/stop/halt lifecycle methods are adjacent and include the shutdown sequence.
Raw support: `close()` calls `stopServer()`; `stopServer()` closes `packetProcessor`, stops connection, saves players/worlds, closes each level, then `savedDataStorage.close(); this.resources.close(); this.storageSource.close();`; `halt(wait)` sets `running = false` and optionally joins `serverThread`
Related symbols/files: net.minecraft.server.dedicated.DedicatedServer.stopServer; net.minecraft.client.server.IntegratedServer.stopServer; net.minecraft.client.server.IntegratedServer.halt; v26.1.2/.research-src/common/net/minecraft/server/Main.java
Confidence: high
Unknowns: Orchestrator review needed for whether downstream resource-close ordering matters for target analysis.
Orchestrator verification: confirm no separate close path exists outside stopServer.
Decision impact: shutdown-order evidence only

Evidence ID: LC-005
Concept: Integrated server follows the same lifecycle hook pattern with local overrides and super calls
Worker: GPT-5.4-Mini retrieval worker
Source artifact: Minecraft source decompilation
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/client/net/minecraft/client/server/IntegratedServer.java
Class: net.minecraft.client.server.IntegratedServer
Member: initServer; tickServer; stopServer; halt
Descriptor if available: unknown
Line range or local reference: 92-101, 134-166, 248-269
Exact command used: nl -ba v26.1.2/.research-src/client/net/minecraft/client/server/IntegratedServer.java | sed -n '1,320p'
Why this matched: The integrated server overrides the same lifecycle hooks named in the search terms.
Raw support: `initServer()` calls `this.loadLevel();`; `tickServer()` conditionally `super.tickServer(haveTime);`; `stopServer()` calls `super.stopServer();`; `halt(wait)` does local player cleanup then `super.halt(wait);`
Related symbols/files: net.minecraft.server.MinecraftServer.loadLevel; net.minecraft.server.MinecraftServer.tickServer; net.minecraft.server.MinecraftServer.stopServer; net.minecraft.server.MinecraftServer.halt; v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java
Confidence: high
Unknowns: Orchestrator review needed for any client-side readiness parity beyond these hook calls.
Orchestrator verification: confirm whether the integrated path has any extra readiness gate besides base isReady.
Decision impact: integrated-parity evidence only
