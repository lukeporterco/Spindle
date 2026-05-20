# SteelHook Gap Map

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

| Gap | Needed by | Why | Evidence |
|---|---|---|---|
| Async continuation | resources/datapacks, reloadable registries, command reload | reload paths return and compose `CompletableFuture` | `RR-001`, `RR-002`, `RR-003`, `RCF-003` |
| Constructor-tail | commands | external registrations may need to run after vanilla dispatcher population and before function library use | `CMD-CONF-001`, `CMD-CONF-003` |
| Method-around/wrap | commands, resources, registries | contribution may require changing values passed through construction/reload | `CMD-CONF-002`, `RR-006`, `RCF-003` |
| Phase event hooks | networking | login/config/play handoffs are method-boundary events | `NET-04`, `NET-07`, `NET-12` |
| Direct lookup binding | registries, world/level | no hook needed after readiness | `RCF-006`, `WLC-002` |

## Do Not Add Yet

- Packet interception primitives.
- Registry mutation primitives.
- Storage/persistence hooks.
