# Contradictions And Resolutions

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

## Resolved

| Question | Resolution | Evidence |
|---|---|---|
| Is `serverStarted()` the public ready boundary? | No. It is dedicated-specific and distinct from shared `loadLevel()` and first tick readiness. | `LC-001`, `LC-003`, `LC-005` |
| Is `isReady` set immediately after world load? | No. Static source sets it after tick-loop work. | `LC-003` |
| Is `sendCommands(...)` command registration? | No. It builds and sends a filtered player command tree. | `CMD-CONF-004` |
| Is reload safe immediately after `this.resources` assignment? | No conclusion. Source shows downstream updates after assignment, so the safer evidence-backed boundary is later. | `RR-001` |
| Is raw packet send first-wave networking? | No. It is transport-level and rejected for first wave. | `NET-03` |
| Is scheduled block/fluid tick a public tick boundary? | No. It is a lower-level subsystem inside level tick. | `TC-004` |

## Unresolved

- Exact public lifecycle ready semantics still need runtime confirmation.
- Command insertion timing still needs a SteelHook-aware design pass.
- Datapack contribution still needs async/listener composition design.
