# Runtime Confirmation Synthesis

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

Source runtime files:

- `internals-analysis/notes/runtime-probe-dedicated-2026-05-19.jsonl`
- `internals-analysis/notes/runtime-probe-dedicated-2026-05-19-latest-log.txt`
- `internals-analysis/notes/runtime-probe-results.md`
- `internals-analysis/notes/runtime-probe-log.md`
- `internals-analysis/matrices/runtime-confirmation-status.md`

## Dedicated Probe Result

The dedicated runtime probe captured 59 JSONL records. All required probe groups were observed:

- Dedicated startup order.
- First server tick and first level tick.
- Reload start and future completion.
- Command dispatcher construction timing.
- Registry/world availability after readiness points.
- Shutdown order.

## Confirmed Dedicated Order

Dedicated startup order was observed from resource loading through server startup and first ticks. `serverStarted` occurs before the first tick while `serverReady=false`.

The first server tick and first per-level ticks were observed. `isReady=true` is first observed after first `tickServer` return, at the next `tickServer` head.

Registry lookup and world/level availability snapshots were observed through startup, tick, reload, and shutdown points. The probe saw registry lookup availability before worlds were fully populated, then level counts and overworld availability after level construction.

Shutdown order was observed through halt, dedicated shutdown notification, `stopServer`, `onServerExit`, `runServer` return, and shutdown-thread halt.

## Reload Confirmation

Reload head, return, returned future completion, `ReloadableServerResources`, and `Commands` reconstruction were observed.

This supports reload start/complete observation planning, but it does not prove datapack contribution or listener composition safety. Reload post-swap micro-order remains not fully proven because the exact `this.resources` assignment point was not instrumented.

## Interpretation Boundary

Dedicated server only. Integrated server parity remains unconfirmed.

The probe is runtime confirmation for one dedicated-server path and must be correlated with static evidence before future Target Layer or SteelHook implementation. It is not a universal API guarantee. Java mod execution is not sandboxed.
