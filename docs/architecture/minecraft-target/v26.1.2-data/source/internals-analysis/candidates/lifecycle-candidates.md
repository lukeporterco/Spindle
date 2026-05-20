# Lifecycle Candidates

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

## Candidate: dedicated-server-started-notification

Decision: keep
Confidence: medium
Source-backed reason: `DedicatedServer.initServer()` calls `loadLevel()`, performs dedicated setup/save work, then calls `notificationManager().serverStarted()` before returning true. Evidence: `LC-001`.
Spindle implication: Useful as a dedicated-server started observation, not as a shared ready boundary.
SteelHook primitive implication: method-entry/exit around `DedicatedServer.initServer()` or a narrowly placed callback near the notification point.
What would change the decision: Evidence of an equivalent integrated notification or a better shared vanilla post-load event would narrow or replace this candidate.

## Candidate: post-loadlevel-world-prepared

Decision: keep
Confidence: medium
Source-backed reason: `MinecraftServer.loadLevel()` calls `createLevels()` and `prepareLevels()` before returning. Evidence: `LC-002`, `WLC-001`.
Spindle implication: Best first-pass shared lifecycle anchor for "loaded server worlds exist and initial chunk preparation finished."
SteelHook primitive implication: method-exit on `MinecraftServer.loadLevel()` or `prepareLevels()`.
What would change the decision: Runtime evidence that important world systems become safe only after first tick would make this documentation-only or split it from public-ready.

## Candidate: first-tick-ready-flag

Decision: uncertain
Confidence: medium
Source-backed reason: `MinecraftServer.runServer()` assigns `isReady = true` after processing tick work and waiting for the next tick in the loop. Evidence: `LC-003`.
Spindle implication: Possible first-tick-complete signal, but not proven as a safe public ready marker.
SteelHook primitive implication: no hook recommended yet; may need loop/tick observation if promoted.
What would change the decision: Runtime logs showing the relationship among `loadLevel()`, `serverStarted()`, first `tickServer()`, and `isReady()` for dedicated and integrated servers.

## Candidate: shutdown-execution-boundary

Decision: keep
Confidence: medium
Source-backed reason: `runServer()` finally sets stopped, calls `stopServer()`, and then `onServerExit()`; `DedicatedServer.stopServer()` calls `notificationManager().serverShuttingDown()` before base stop. Evidence: `LC-004`.
Spindle implication: Good first-wave stopping observation; fully-stopped/exited should remain separate.
SteelHook primitive implication: method-entry on `stopServer()` for stopping; method-exit/finally around `onServerExit()` only if a final-exit event is needed.
What would change the decision: Runtime evidence that shutdown can bypass `stopServer()` or that integrated shutdown needs a different public boundary.
