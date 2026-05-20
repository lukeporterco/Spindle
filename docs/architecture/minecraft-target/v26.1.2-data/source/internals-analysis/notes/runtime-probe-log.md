# Runtime Probe Log

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

## Probes Run

- Dedicated server runtime probe, May 19, 2026:
  - Command: `./gradlew runServer --args='nogui' --no-parallel`
  - Working directory: `/Users/luke/Documents/MC_Research/v26.1.2/fabric-workspace`
  - Server command input: `reload`, then `stop`
  - Raw JSONL: `/Users/luke/Documents/MC_Research/v26.1.2/internals-analysis/notes/runtime-probe-dedicated-2026-05-19.jsonl`
  - Server log: `/Users/luke/Documents/MC_Research/v26.1.2/internals-analysis/notes/runtime-probe-dedicated-2026-05-19-latest-log.txt`
  - Parsed summary: `/Users/luke/Documents/MC_Research/v26.1.2/internals-analysis/notes/runtime-probe-results.md`

## Result Summary

- Captured 59 probe records from the dedicated server path.
- Observed startup order from `runServer` through `DedicatedServer.initServer`, `loadLevel`, `createLevels`, `prepareLevels`, `serverStarted`, first server tick, first level ticks, readiness observation, reload, and shutdown.
- `serverStarted` occurred with `serverReady=false`, `serverTick=0`, `levelCount=3`, and registry lookups available.
- First server tick head occurred with `serverReady=false`, `serverTick=0`; first server tick return had `serverTick=1`; first observed `isReady=true` occurred at the next `tickServer` head with `serverTick=1`.
- `/reload` produced `MinecraftServer.reloadResources` head/return/future-complete records and a second `ReloadableServerResources` plus `Commands` construction sequence.
- Shutdown produced console-triggered `halt(false)`, `serverShuttingDown`, `stopServer`, `onServerExit`, `runServer` return, and shutdown-thread `halt(true)` records.

## Limitations

- This is dedicated-server-only runtime evidence. It does not confirm integrated server parity.
- It confirms observed timing for one local Fabric/Loom dedicated run, not a universal public API contract.
- The probe did not instrument the exact point inside `MinecraftServer.reloadResources` where `this.resources` is assigned, so post-swap micro-ordering still needs source correlation or a narrower future probe.

## Recommended Future Probes

- Integrated startup: log the same shared points plus integrated pause/tick behavior.
- Reload: log `this.resources` assignment and each post-swap update call inside `MinecraftServer.reloadResources(...)`.
- Shutdown: add a narrower probe for subsystem close order if SteelHook needs exact teardown placement.
