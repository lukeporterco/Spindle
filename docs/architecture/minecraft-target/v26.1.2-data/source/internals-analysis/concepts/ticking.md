# Ticking

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted
Status: confirmed static-source pass complete
Confidence: high
Handoff readiness: ready for documentation, partially ready for implementation planning
First-wave classification: first-wave stable

## What this concept means in Minecraft 26.1.2

Ticking has clear server, level, and subsystem boundaries. `MinecraftServer.processPacketsAndTick(...)` calls `tickServer(...)`. `tickServer(...)` increments `tickCount`, ticks the `ServerTickRateManager`, and delegates to `tickChildren(...)`. `tickChildren(...)` ticks command functions, clocks, time sync, every loaded `ServerLevel`, network connection, players, debug subscribers, game tests, tickables, chunk sending, and the activity monitor. Each `ServerLevel.tick(...)` performs per-world work including scheduled block/fluid ticks and chunk ticking.

## Important source boundaries

| Boundary | Source | Evidence |
|---|---|---|
| Server tick | `MinecraftServer.tickServer`, `MinecraftServer.java:982-1026` | `TC-001` |
| Server child/level dispatch | `MinecraftServer.tickChildren`, `MinecraftServer.java:1111-1174` | `TC-002` |
| Per-level tick | `ServerLevel.tick`, `ServerLevel.java:345-392` | `TC-003`, `WLC-004` |
| Scheduled block/fluid tick subsystem | `LevelTicks.tick` | `TC-004` |
| Tick rate/time fields | `tickRateManager`, `tickCount`, `smoothedTickTimeMillis` | `TC-005`, `TC-008`, `TC-009` |
| Task pumping between ticks | `waitUntilNextTick`, `haveTime`, `runAllTasks` call site | `TC-006`, `TC-007` |

## Confirmed lifecycle or timing behavior

- `tickCount` increments once in `MinecraftServer.tickServer(...)` before `tickChildren(...)`.
- `tickChildren(...)` is once per server tick and iterates all loaded levels.
- `ServerLevel.tick(...)` is once per level per normal server tick.
- `LevelTicks.tick(...)` is lower-level scheduled block/fluid behavior, not a general Target tick boundary.

## Candidate Spindle binding direction

Ticking is first-wave stable for before/after server tick and before/after level tick events. Around-tick control should be deferred unless Spindle needs cancellation or timing modification.

## SteelHook primitive implications

- Server before/after tick: method-entry and method-exit on `MinecraftServer.tickServer(...)`.
- Level before/after tick: method-entry and method-exit on `ServerLevel.tick(...)`.
- Around/wrap is only needed if Spindle wants tick cancellation, time mutation, or exception handling behavior.
- Scheduled subsystem ticks are rejected for first-wave public Target scope.

## Rejected or deferred paths

- `LevelTicks.tick(...)` as a public first-wave Target event is rejected.
- Tick-rate manager mutation is deferred.
- Task-queue control through `runAllTasks()` is deferred.

## Open questions

- Should first-wave tick events include dedicated/integrated side labels?
- Does Spindle need post-player-tick or post-network-tick sub-events, or only server/level tick events?
- Should around-tick primitives be explicitly postponed until a use case appears?
