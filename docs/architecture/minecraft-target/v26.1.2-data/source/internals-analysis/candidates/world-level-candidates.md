# World And Level Candidates

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

## Candidate: post-preparelevels-world-ready

Decision: keep
Confidence: medium
Source-backed reason: `MinecraftServer.loadLevel()` calls `createLevels()` and `prepareLevels()`; `prepareLevels()` waits on initial chunk preparation. Evidence: `WLC-001`.
Spindle implication: Best first-wave post-world-load documentation/event boundary, with public-ready caveats.
SteelHook primitive implication: method-exit on `loadLevel()` or `prepareLevels()`.
What would change the decision: Runtime evidence that the first safe public access point is after first tick, not after `prepareLevels()`.

## Candidate: minecraftserver-level-enumeration

Decision: keep
Confidence: high
Source-backed reason: `MinecraftServer` stores loaded levels in `levels` and exposes `overworld()`, nullable `getLevel(...)`, `levelKeys()`, and `getAllLevels()`. Evidence: `WLC-002`.
Spindle implication: First-wave level lookup/enumeration should wrap these accessors, preferably as a controlled snapshot.
SteelHook primitive implication: none after readiness.
What would change the decision: If live view mutation risk proves high, expose snapshot only and reject live view.

## Candidate: serverlevel-runtime-object

Decision: uncertain
Confidence: medium
Source-backed reason: `ServerLevel` constructor and `tick(...)` show the live world object, storage access, chunk cache, and tick systems. Evidence: `WLC-004`.
Spindle implication: Useful implementation object, but exposing raw `ServerLevel` would leak internals.
SteelHook primitive implication: no hook for lookup; method-entry/exit for level tick events.
What would change the decision: A Target design choosing explicit raw-handle escape hatches or a wrapper-only policy.

## Candidate: level-storage-access

Decision: reject
Confidence: high
Source-backed reason: `LevelStorageSource` and `LevelStorageAccess` are save-folder/path/lock APIs, not live world lookup. Evidence: `WLC-003`.
Spindle implication: Keep persistence/storage separate from runtime world Target scope.
SteelHook primitive implication: none first wave.
What would change the decision: A separate Spindle persistence feature request.
