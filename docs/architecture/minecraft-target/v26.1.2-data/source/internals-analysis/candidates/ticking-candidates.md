# Ticking Candidates

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

## Candidate: server-tickserver-boundary

Decision: keep
Confidence: high
Source-backed reason: `MinecraftServer.tickServer(...)` increments `tickCount`, ticks rate manager, and delegates to `tickChildren(...)`. Evidence: `TC-001`, `TC-009`.
Spindle implication: First-wave server tick before/after event boundary.
SteelHook primitive implication: method-entry and method-exit.
What would change the decision: Only a later requirement for cancellation/time mutation would add around/wrap.

## Candidate: server-tickchildren-boundary

Decision: keep
Confidence: high
Source-backed reason: `tickChildren(...)` performs once-per-server-tick child work and iterates all loaded levels. Evidence: `TC-002`.
Spindle implication: Useful internal documentation boundary; public API should probably prefer `tickServer` and `ServerLevel.tick`.
SteelHook primitive implication: method-entry/exit if a post-subsystem or post-level aggregate event is needed.
What would change the decision: If Spindle needs a single "all levels ticked but players/network not done" event.

## Candidate: serverlevel-tick-boundary

Decision: keep
Confidence: high
Source-backed reason: `ServerLevel.tick(...)` is the per-level tick site and includes scheduled block/fluid/chunk/world work. Evidence: `TC-003`, `WLC-004`.
Spindle implication: First-wave level tick before/after event boundary.
SteelHook primitive implication: method-entry and method-exit.
What would change the decision: If integrated pause behavior requires additional side-specific filtering.

## Candidate: levelticks-scheduled-block-fluid

Decision: reject
Confidence: high
Source-backed reason: `LevelTicks.tick(...)` is a scheduled block/fluid subsystem invoked inside level tick, not a general simulation boundary. Evidence: `TC-004`.
Spindle implication: Keep out of first-wave Target API.
SteelHook primitive implication: none first wave.
What would change the decision: A later low-level block/fluid scheduling feature request.
