# World And Level

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted
Status: confirmed static-source pass complete
Confidence: medium
Handoff readiness: partially ready
First-wave classification: first-wave limited

## What this concept means in Minecraft 26.1.2

Runtime worlds are `ServerLevel` instances stored in `MinecraftServer.levels`, a `LinkedHashMap<ResourceKey<Level>, ServerLevel>`. `MinecraftServer.loadLevel()` calls `createLevels()` and then `prepareLevels()`. `createLevels()` constructs the overworld and additional dimension levels; `prepareLevels()` waits for initial chunk preparation. Runtime accessors include `overworld()`, nullable `getLevel(ResourceKey<Level>)`, `levelKeys()`, and `getAllLevels()`.

Storage APIs such as `LevelStorageSource` and `LevelStorageAccess` are file/path/lock oriented and should stay separate from live runtime world access.

## Important source boundaries

| Boundary | Source | Evidence |
|---|---|---|
| Level creation and preparation | `MinecraftServer.loadLevel`, `createLevels`, `prepareLevels` | `WLC-001`, `LC-002` |
| Runtime loaded-level map/accessors | `MinecraftServer.levels`, `overworld`, `getLevel`, `levelKeys`, `getAllLevels` | `WLC-002` |
| Storage access | `LevelStorageSource`, `LevelStorageSource.LevelStorageAccess` | `WLC-003` |
| Live level object lifecycle/tick | `ServerLevel.<init>`, `ServerLevel.tick`, `tickTime` | `WLC-004` |
| Level data contract | `ServerLevelData` | `WLC-005` |

## Confirmed lifecycle or timing behavior

- Loaded levels are created before `prepareLevels()` returns.
- `prepareLevels()` waits for initial chunk preparation, but deeper "safe for all world operations" semantics are not runtime-confirmed.
- `getAllLevels()` exposes the live map values; source does not prove snapshot semantics.
- `getLevel(...)` is nullable by dimension key.

## Candidate Spindle binding direction

Use world/level as first-wave limited lookup/enumeration after lifecycle readiness. Prefer a controlled snapshot/wrapper concept over exposing the live map view. Keep persistence/storage APIs documentation-only until a separate storage pass.

## SteelHook primitive implications

- Lookup/enumeration needs no hook after a known server-ready boundary.
- A post-level-load event would need method-exit on `MinecraftServer.prepareLevels()` or `loadLevel()`.
- Deep storage/persistence hooks are out of first-wave scope.

## Rejected or deferred paths

- Exposing `LevelStorageSource` as runtime world API is rejected.
- Treating `getAllLevels()` as a stable immutable snapshot is rejected.
- Public world access before `prepareLevels()` completes is deferred/rejected for first wave.

## Open questions

- Should first-wave Target expose a snapshot of loaded levels or a guarded live view?
- Is `prepareLevels()` enough for public world-ready semantics, or should first-tick confirmation be required?
- What is the minimal dimension-key abstraction Spindle needs without leaking raw Minecraft types?
