# Runtime Confirmation Status

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

| Question | Static status | Runtime status | Current decision |
|---|---|---|---|
| Dedicated startup order | source confirmed | confirmed in dedicated probe | runtime-backed for dedicated server documentation |
| Integrated startup parity | source partially confirmed | not run | lifecycle remains split/limited |
| Public ready marker | source shows multiple candidates | dedicated probe observed `serverStarted` before first tick and `isReady=true` after first tick return | first-wave lifecycle remains limited; dedicated ready semantics are narrower |
| Reload completion safety | source shows post-swap order | dedicated probe observed reload head, loadResources future completion, reload return, returned future completion | event-only, async-gated; post-swap micro-order still needs narrower proof |
| Command registration insertion | source shows owner/order | dedicated startup and reload both observed `Commands` construction before function-library dispatcher capture | constructor-tail remains plausible; exact hook insertion still needs implementation probe |
| Registry mutation safety | source shows freeze/write gate | not run | lookup-only first wave |
| Server and level tick events | source confirmed | dedicated probe confirmed first server tick and first per-level ticks | ready for first implementation pass |
| Networking phase handoffs | source confirmed | not run | phase-event-only if included |

## Runtime Probes Run

- Dedicated server probe, May 19, 2026.
- Raw JSONL: `/Users/luke/Documents/MC_Research/v26.1.2/internals-analysis/notes/runtime-probe-dedicated-2026-05-19.jsonl`
- Server log: `/Users/luke/Documents/MC_Research/v26.1.2/internals-analysis/notes/runtime-probe-dedicated-2026-05-19-latest-log.txt`
- Parsed summary: `/Users/luke/Documents/MC_Research/v26.1.2/internals-analysis/notes/runtime-probe-results.md`
