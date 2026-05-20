# Runtime Probe Results

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

Source log: `/Users/luke/Documents/MC_Research/v26.1.2/internals-analysis/notes/runtime-probe-dedicated-2026-05-19.jsonl`
Records parsed: 59

## Probe Counts

- `RPROBE-CMD-CONSTRUCTOR-HEAD`: 2
- `RPROBE-CMD-CONSTRUCTOR-RETURN`: 2
- `RPROBE-CMD-FUNCTIONLIB-DISPATCHER-BEFORE`: 2
- `RPROBE-CMD-GETDISPATCHER-HEAD`: 2
- `RPROBE-CMD-GETDISPATCHER-RETURN`: 2
- `RPROBE-LC-DEDICATED-INIT-HEAD`: 1
- `RPROBE-LC-DEDICATED-INIT-RETURN`: 1
- `RPROBE-LC-LOADLEVEL-HEAD`: 1
- `RPROBE-LC-LOADLEVEL-RETURN`: 1
- `RPROBE-LC-READY-FIRST-OBSERVED`: 1
- `RPROBE-LC-RUNSERVER-HEAD`: 1
- `RPROBE-LC-RUNSERVER-RETURN`: 1
- `RPROBE-LC-SERVERSTARTED-AFTER`: 1
- `RPROBE-LC-SERVERSTARTED-BEFORE`: 1
- `RPROBE-MOD-INIT`: 1
- `RPROBE-RELOAD-RESOURCES-FUTURE-COMPLETE`: 1
- `RPROBE-RELOAD-RESOURCES-HEAD`: 1
- `RPROBE-RELOAD-RESOURCES-RETURN`: 1
- `RPROBE-RELOADABLE-LOADRESOURCES-FUTURE-COMPLETE`: 2
- `RPROBE-RELOADABLE-LOADRESOURCES-HEAD`: 2
- `RPROBE-RELOADABLE-LOADRESOURCES-RETURN`: 2
- `RPROBE-RELOADABLE-RESOURCES-CONSTRUCTOR-HEAD`: 2
- `RPROBE-RELOADABLE-RESOURCES-CONSTRUCTOR-RETURN`: 2
- `RPROBE-SHUTDOWN-EXIT-HEAD`: 1
- `RPROBE-SHUTDOWN-EXIT-RETURN`: 1
- `RPROBE-SHUTDOWN-HALT-HEAD`: 2
- `RPROBE-SHUTDOWN-HALT-RETURN`: 2
- `RPROBE-SHUTDOWN-NOTIFY-AFTER`: 1
- `RPROBE-SHUTDOWN-NOTIFY-BEFORE`: 1
- `RPROBE-SHUTDOWN-STOPSERV-HEAD`: 1
- `RPROBE-SHUTDOWN-STOPSERV-RETURN`: 1
- `RPROBE-TICK-CHILDREN-FIRST-HEAD`: 1
- `RPROBE-TICK-LEVEL-FIRST-HEAD`: 3
- `RPROBE-TICK-LEVEL-FIRST-RETURN`: 3
- `RPROBE-TICK-SERVER-FIRST-HEAD`: 1
- `RPROBE-TICK-SERVER-FIRST-RETURN`: 1
- `RPROBE-WORLD-CREATELEVELS-HEAD`: 1
- `RPROBE-WORLD-CREATELEVELS-RETURN`: 1
- `RPROBE-WORLD-PREPARELEVELS-HEAD`: 1
- `RPROBE-WORLD-PREPARELEVELS-RETURN`: 1
- `RPROBE-WORLD-SERVERLEVEL-CONSTRUCTED`: 3

## First Observed Order

- 1: `RPROBE-MOD-INIT` - fabric-mod-initialize
- 2: `RPROBE-RELOADABLE-LOADRESOURCES-HEAD` - ReloadableServerResources.loadResources HEAD
- 3: `RPROBE-RELOADABLE-LOADRESOURCES-RETURN` - ReloadableServerResources.loadResources RETURN
- 4: `RPROBE-RELOADABLE-RESOURCES-CONSTRUCTOR-HEAD` - ReloadableServerResources constructor HEAD
- 5: `RPROBE-CMD-CONSTRUCTOR-HEAD` - Commands constructor HEAD
- 6: `RPROBE-CMD-CONSTRUCTOR-RETURN` - Commands constructor RETURN
- 7: `RPROBE-CMD-FUNCTIONLIB-DISPATCHER-BEFORE` - ReloadableServerResources before ServerFunctionLibrary dispatcher capture
- 8: `RPROBE-CMD-GETDISPATCHER-HEAD` - Commands.getDispatcher HEAD
- 9: `RPROBE-CMD-GETDISPATCHER-RETURN` - Commands.getDispatcher RETURN
- 10: `RPROBE-RELOADABLE-RESOURCES-CONSTRUCTOR-RETURN` - ReloadableServerResources constructor RETURN
- 11: `RPROBE-RELOADABLE-LOADRESOURCES-FUTURE-COMPLETE` - ReloadableServerResources.loadResources future complete
- 12: `RPROBE-LC-RUNSERVER-HEAD` - MinecraftServer.runServer HEAD (tick=0, ready=false, levels=0)
- 13: `RPROBE-LC-DEDICATED-INIT-HEAD` - DedicatedServer.initServer HEAD (tick=0, ready=false, levels=0)
- 14: `RPROBE-LC-LOADLEVEL-HEAD` - MinecraftServer.loadLevel HEAD (tick=0, ready=false, levels=0)
- 15: `RPROBE-WORLD-CREATELEVELS-HEAD` - MinecraftServer.createLevels HEAD (tick=0, ready=false, levels=0)
- 16: `RPROBE-WORLD-SERVERLEVEL-CONSTRUCTED` - ServerLevel constructor RETURN (tick=0, ready=false, levels=0, dimension=minecraft:overworld)
- 17: `RPROBE-WORLD-SERVERLEVEL-CONSTRUCTED` - ServerLevel constructor RETURN (tick=0, ready=false, levels=1, dimension=minecraft:the_end)
- 18: `RPROBE-WORLD-SERVERLEVEL-CONSTRUCTED` - ServerLevel constructor RETURN (tick=0, ready=false, levels=2, dimension=minecraft:the_nether)
- 19: `RPROBE-WORLD-CREATELEVELS-RETURN` - MinecraftServer.createLevels RETURN (tick=0, ready=false, levels=3)
- 20: `RPROBE-WORLD-PREPARELEVELS-HEAD` - MinecraftServer.prepareLevels HEAD (tick=0, ready=false, levels=3)
- 21: `RPROBE-WORLD-PREPARELEVELS-RETURN` - MinecraftServer.prepareLevels RETURN (tick=0, ready=false, levels=3)
- 22: `RPROBE-LC-LOADLEVEL-RETURN` - MinecraftServer.loadLevel RETURN (tick=0, ready=false, levels=3)
- 23: `RPROBE-LC-SERVERSTARTED-BEFORE` - DedicatedServer before notificationManager.serverStarted (tick=0, ready=false, levels=3)
- 24: `RPROBE-LC-SERVERSTARTED-AFTER` - DedicatedServer after notificationManager.serverStarted (tick=0, ready=false, levels=3)
- 25: `RPROBE-LC-DEDICATED-INIT-RETURN` - DedicatedServer.initServer RETURN (tick=0, ready=false, levels=3)
- 26: `RPROBE-TICK-SERVER-FIRST-HEAD` - MinecraftServer.tickServer first HEAD (tick=0, ready=false, levels=3)
- 27: `RPROBE-TICK-CHILDREN-FIRST-HEAD` - MinecraftServer.tickChildren first HEAD (tick=1, ready=false, levels=3)
- 28: `RPROBE-TICK-LEVEL-FIRST-HEAD` - ServerLevel.tick first HEAD for dimension (tick=1, ready=false, levels=3, dimension=minecraft:overworld)
- 29: `RPROBE-TICK-LEVEL-FIRST-RETURN` - ServerLevel.tick first RETURN for dimension (tick=1, ready=false, levels=3, dimension=minecraft:overworld)
- 30: `RPROBE-TICK-LEVEL-FIRST-HEAD` - ServerLevel.tick first HEAD for dimension (tick=1, ready=false, levels=3, dimension=minecraft:the_end)
- 31: `RPROBE-TICK-LEVEL-FIRST-RETURN` - ServerLevel.tick first RETURN for dimension (tick=1, ready=false, levels=3, dimension=minecraft:the_end)
- 32: `RPROBE-TICK-LEVEL-FIRST-HEAD` - ServerLevel.tick first HEAD for dimension (tick=1, ready=false, levels=3, dimension=minecraft:the_nether)
- 33: `RPROBE-TICK-LEVEL-FIRST-RETURN` - ServerLevel.tick first RETURN for dimension (tick=1, ready=false, levels=3, dimension=minecraft:the_nether)
- 34: `RPROBE-TICK-SERVER-FIRST-RETURN` - MinecraftServer.tickServer first RETURN (tick=1, ready=false, levels=3)
- 35: `RPROBE-LC-READY-FIRST-OBSERVED` - MinecraftServer.isReady first observed true at tickServer HEAD (tick=1, ready=true, levels=3)
- 36: `RPROBE-RELOAD-RESOURCES-HEAD` - MinecraftServer.reloadResources HEAD (tick=73, ready=true, levels=3)
- 37: `RPROBE-RELOADABLE-LOADRESOURCES-HEAD` - ReloadableServerResources.loadResources HEAD
- 38: `RPROBE-RELOADABLE-LOADRESOURCES-RETURN` - ReloadableServerResources.loadResources RETURN
- 39: `RPROBE-RELOADABLE-RESOURCES-CONSTRUCTOR-HEAD` - ReloadableServerResources constructor HEAD
- 40: `RPROBE-CMD-CONSTRUCTOR-HEAD` - Commands constructor HEAD
- 41: `RPROBE-CMD-CONSTRUCTOR-RETURN` - Commands constructor RETURN
- 42: `RPROBE-CMD-FUNCTIONLIB-DISPATCHER-BEFORE` - ReloadableServerResources before ServerFunctionLibrary dispatcher capture
- 43: `RPROBE-CMD-GETDISPATCHER-HEAD` - Commands.getDispatcher HEAD
- 44: `RPROBE-CMD-GETDISPATCHER-RETURN` - Commands.getDispatcher RETURN
- 45: `RPROBE-RELOADABLE-RESOURCES-CONSTRUCTOR-RETURN` - ReloadableServerResources constructor RETURN
- 46: `RPROBE-RELOADABLE-LOADRESOURCES-FUTURE-COMPLETE` - ReloadableServerResources.loadResources future complete
- 47: `RPROBE-RELOAD-RESOURCES-RETURN` - MinecraftServer.reloadResources RETURN (tick=73, ready=true, levels=3)
- 48: `RPROBE-RELOAD-RESOURCES-FUTURE-COMPLETE` - MinecraftServer.reloadResources returned future complete (tick=73, ready=true, levels=3)
- 49: `RPROBE-SHUTDOWN-HALT-HEAD` - MinecraftServer.halt HEAD (tick=219, ready=true, levels=3)
- 50: `RPROBE-SHUTDOWN-HALT-RETURN` - MinecraftServer.halt RETURN (tick=219, ready=true, levels=3)
- 51: `RPROBE-SHUTDOWN-NOTIFY-BEFORE` - DedicatedServer before notificationManager.serverShuttingDown (tick=219, ready=true, levels=3)
- 52: `RPROBE-SHUTDOWN-NOTIFY-AFTER` - DedicatedServer after notificationManager.serverShuttingDown (tick=219, ready=true, levels=3)
- 53: `RPROBE-SHUTDOWN-STOPSERV-HEAD` - MinecraftServer.stopServer HEAD (tick=219, ready=true, levels=3)
- 54: `RPROBE-SHUTDOWN-STOPSERV-RETURN` - MinecraftServer.stopServer RETURN (tick=219, ready=true, levels=3)
- 55: `RPROBE-SHUTDOWN-EXIT-HEAD` - DedicatedServer.onServerExit HEAD (tick=219, ready=true, levels=3)
- 56: `RPROBE-SHUTDOWN-EXIT-RETURN` - DedicatedServer.onServerExit RETURN (tick=219, ready=true, levels=3)
- 57: `RPROBE-LC-RUNSERVER-RETURN` - MinecraftServer.runServer RETURN (tick=219, ready=true, levels=3)
- 58: `RPROBE-SHUTDOWN-HALT-HEAD` - MinecraftServer.halt HEAD (tick=219, ready=true, levels=3)
- 59: `RPROBE-SHUTDOWN-HALT-RETURN` - MinecraftServer.halt RETURN (tick=219, ready=true, levels=3)

## Required Probe Groups

- Dedicated startup order: observed (16 records)
- First server tick and first level tick: observed (10 records)
- Reload start and future completion: observed (13 records)
- Command dispatcher construction timing: observed (10 records)
- Registry/world availability after readiness points: observed (8 records)
- Shutdown order: observed (10 records)

## Availability Snapshots

- 12: `RPROBE-LC-RUNSERVER-HEAD` ready=false tick=0 levels=0 overworld=false registries=141 biome=true level_stem=true
- 13: `RPROBE-LC-DEDICATED-INIT-HEAD` ready=false tick=0 levels=0 overworld=false registries=141 biome=true level_stem=true
- 14: `RPROBE-LC-LOADLEVEL-HEAD` ready=false tick=0 levels=0 overworld=false registries=141 biome=true level_stem=true
- 15: `RPROBE-WORLD-CREATELEVELS-HEAD` ready=false tick=0 levels=0 overworld=false registries=141 biome=true level_stem=true
- 16: `RPROBE-WORLD-SERVERLEVEL-CONSTRUCTED` ready=false tick=0 levels=0 overworld=false registries=141 biome=true level_stem=true
- 17: `RPROBE-WORLD-SERVERLEVEL-CONSTRUCTED` ready=false tick=0 levels=1 overworld=true registries=141 biome=true level_stem=true
- 18: `RPROBE-WORLD-SERVERLEVEL-CONSTRUCTED` ready=false tick=0 levels=2 overworld=true registries=141 biome=true level_stem=true
- 19: `RPROBE-WORLD-CREATELEVELS-RETURN` ready=false tick=0 levels=3 overworld=true registries=141 biome=true level_stem=true
- 20: `RPROBE-WORLD-PREPARELEVELS-HEAD` ready=false tick=0 levels=3 overworld=true registries=141 biome=true level_stem=true
- 21: `RPROBE-WORLD-PREPARELEVELS-RETURN` ready=false tick=0 levels=3 overworld=true registries=141 biome=true level_stem=true
- 22: `RPROBE-LC-LOADLEVEL-RETURN` ready=false tick=0 levels=3 overworld=true registries=141 biome=true level_stem=true
- 23: `RPROBE-LC-SERVERSTARTED-BEFORE` ready=false tick=0 levels=3 overworld=true registries=141 biome=true level_stem=true
- 24: `RPROBE-LC-SERVERSTARTED-AFTER` ready=false tick=0 levels=3 overworld=true registries=141 biome=true level_stem=true
- 25: `RPROBE-LC-DEDICATED-INIT-RETURN` ready=false tick=0 levels=3 overworld=true registries=141 biome=true level_stem=true
- 26: `RPROBE-TICK-SERVER-FIRST-HEAD` ready=false tick=0 levels=3 overworld=true registries=141 biome=true level_stem=true
- 27: `RPROBE-TICK-CHILDREN-FIRST-HEAD` ready=false tick=1 levels=3 overworld=true registries=141 biome=true level_stem=true
- 28: `RPROBE-TICK-LEVEL-FIRST-HEAD` ready=false tick=1 levels=3 overworld=true registries=141 biome=true level_stem=true
- 29: `RPROBE-TICK-LEVEL-FIRST-RETURN` ready=false tick=1 levels=3 overworld=true registries=141 biome=true level_stem=true
- 30: `RPROBE-TICK-LEVEL-FIRST-HEAD` ready=false tick=1 levels=3 overworld=true registries=141 biome=true level_stem=true
- 31: `RPROBE-TICK-LEVEL-FIRST-RETURN` ready=false tick=1 levels=3 overworld=true registries=141 biome=true level_stem=true
- 32: `RPROBE-TICK-LEVEL-FIRST-HEAD` ready=false tick=1 levels=3 overworld=true registries=141 biome=true level_stem=true
- 33: `RPROBE-TICK-LEVEL-FIRST-RETURN` ready=false tick=1 levels=3 overworld=true registries=141 biome=true level_stem=true
- 34: `RPROBE-TICK-SERVER-FIRST-RETURN` ready=false tick=1 levels=3 overworld=true registries=141 biome=true level_stem=true
- 35: `RPROBE-LC-READY-FIRST-OBSERVED` ready=true tick=1 levels=3 overworld=true registries=141 biome=true level_stem=true
- 36: `RPROBE-RELOAD-RESOURCES-HEAD` ready=true tick=73 levels=3 overworld=true registries=141 biome=true level_stem=true
- 47: `RPROBE-RELOAD-RESOURCES-RETURN` ready=true tick=73 levels=3 overworld=true registries=141 biome=true level_stem=true
- 48: `RPROBE-RELOAD-RESOURCES-FUTURE-COMPLETE` ready=true tick=73 levels=3 overworld=true registries=141 biome=true level_stem=true
- 49: `RPROBE-SHUTDOWN-HALT-HEAD` ready=true tick=219 levels=3 overworld=true registries=141 biome=true level_stem=true
- 50: `RPROBE-SHUTDOWN-HALT-RETURN` ready=true tick=219 levels=3 overworld=true registries=141 biome=true level_stem=true
- 51: `RPROBE-SHUTDOWN-NOTIFY-BEFORE` ready=true tick=219 levels=3 overworld=true registries=141 biome=true level_stem=true
- 52: `RPROBE-SHUTDOWN-NOTIFY-AFTER` ready=true tick=219 levels=3 overworld=true registries=141 biome=true level_stem=true
- 53: `RPROBE-SHUTDOWN-STOPSERV-HEAD` ready=true tick=219 levels=3 overworld=true registries=141 biome=true level_stem=true
- 54: `RPROBE-SHUTDOWN-STOPSERV-RETURN` ready=true tick=219 levels=3 overworld=true registries=141 biome=true level_stem=true
- 55: `RPROBE-SHUTDOWN-EXIT-HEAD` ready=true tick=219 levels=3 overworld=true registries=141 biome=true level_stem=true
- 56: `RPROBE-SHUTDOWN-EXIT-RETURN` ready=true tick=219 levels=3 overworld=true registries=141 biome=true level_stem=true
- 57: `RPROBE-LC-RUNSERVER-RETURN` ready=true tick=219 levels=3 overworld=true registries=141 biome=true level_stem=true
- 58: `RPROBE-SHUTDOWN-HALT-HEAD` ready=true tick=219 levels=3 overworld=true registries=141 biome=true level_stem=true
- 59: `RPROBE-SHUTDOWN-HALT-RETURN` ready=true tick=219 levels=3 overworld=true registries=141 biome=true level_stem=true

## Interpretation Boundary

This summary is runtime evidence from the disposable Fabric probe only. It should be correlated with source evidence before changing Spindle Target Layer or SteelHook decisions.
