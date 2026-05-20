# SteelHook Primitive Fit

## Purpose

Compare researched Minecraft 26.1.2 candidates against known general SteelHook primitive categories. Future needs are named as future needs, not invented current primitives.

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

| Candidate | Decision | Primitive fit | Gap | Evidence |
|---|---|---|---|---|
| dedicated-server-started-notification | keep | method-entry/exit | integrated parity unresolved | `LC-001`, `LC-005` |
| post-loadlevel-world-prepared | keep | method-exit | runtime ready semantics | `LC-002`, `WLC-001` |
| first-tick-ready-flag | uncertain | no hook yet | runtime proof needed | `LC-003` |
| commands-constructor-registration-window | keep | constructor-tail or method-around/wrap | exact insertion timing | `CMD-CONF-001`, `CMD-CONF-003` |
| reloadable-server-resources-command-replacement | keep | method-around/wrap, async continuation | reload replay model | `CMD-CONF-002`, `RR-001` |
| registry-lookup-surface | keep | no hook/direct lookup | none | `RCF-006` |
| reloadable-registry-layer-replacement | keep | method-around/wrap, async continuation | contribution safety | `RCF-003` |
| server-reloadresources-completion | keep | async continuation | future-completion primitive | `RR-001` |
| reloadable-server-resources-listener-barrier | keep | method-around/wrap | listener composition primitive | `RR-003`, `RR-006` |
| minecraftserver-level-enumeration | keep | no hook/direct lookup | readiness gate | `WLC-002` |
| server-tickserver-boundary | keep | method-entry/exit | none for event-only | `TC-001` |
| serverlevel-tick-boundary | keep | method-entry/exit | none for event-only | `TC-003` |
| login-to-configuration-handoff | keep | method-entry/exit | public scope decision | `NET-04` |
| configuration-to-play-handoff | keep | method-entry/exit | public scope decision | `NET-07` |
| raw-connection-packet-send | reject | future wrap only | rejected first wave | `NET-03` |

## Open Questions

- Does SteelHook already support constructor-tail?
- Does SteelHook already support `CompletableFuture` continuation hooks?
- Is method-around/wrap acceptable for reload listener composition, or should a narrower future primitive exist?
