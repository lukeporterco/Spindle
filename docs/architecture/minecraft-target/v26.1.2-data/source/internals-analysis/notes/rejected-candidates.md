# Rejected Candidates

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

## Rejections

| Candidate | Reason | Evidence |
|---|---|---|
| `MinecraftServer.getCommands()` as command registration hook | Accessor only; does not prove registration timing | `CMD-CONF-005` |
| `Commands.sendCommands(...)` as command registration hook | Player sync/publication path, not dispatcher mutation | `CMD-CONF-004` |
| Raw `Connection.send(...)` packet interception | Too low-level and protocol-wide for first-wave Target scope | `NET-03` |
| `LevelTicks.tick(...)` as general tick event | Scheduled block/fluid subsystem, not server/level tick boundary | `TC-004` |
| `LevelStorageSource`/`LevelStorageAccess` as runtime world API | File/path/lock storage APIs, not live level lookup | `WLC-003` |
| Generic `ReloadableResourceManager` as datapack boundary | Generic resource plumbing; server datapack semantics live higher | `RR-001`, `RR-005` |
| Built-in registry mutation first wave | Freeze/write timing not safe; mutation lifecycle unproven | `RCF-004`, `RCF-005` |
| Dedicated `serverStarted()` as shared lifecycle marker | Integrated server path does not show the same notification call | `LC-001`, `LC-005` |

## Follow-up

Rejected here means rejected for first wave. Some items, especially packet interception, storage APIs, and registry mutation, may be revisited only after explicit Spindle requirements and separate safety research.
