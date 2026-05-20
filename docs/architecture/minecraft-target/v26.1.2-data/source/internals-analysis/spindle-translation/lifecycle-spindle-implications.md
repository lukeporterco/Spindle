# Lifecycle Spindle Implications

## Summary

Minecraft 26.1.2 has distinct post-world-load, dedicated-started, first-tick-ready, and shutdown boundaries. First-wave Spindle lifecycle should expose a limited vocabulary rather than a single overloaded ready event.

## First-wave Target Layer recommendation

include as event-only

## Why

`loadLevel()` creates/prepares levels (`LC-002`, `WLC-001`), dedicated `serverStarted()` occurs later but only on `DedicatedServer` (`LC-001`), and `isReady` is set after a loop iteration (`LC-003`).

## Boundary recommendation

Primary shared boundary: `net.minecraft.server.MinecraftServer.loadLevel()` method-exit. Dedicated observation: `net.minecraft.server.dedicated.DedicatedServer.initServer()` after `notificationManager().serverStarted()`.

## SteelHook requirement

method-exit for post-load; method-entry/exit for dedicated started and shutdown.

## API caution

Do not promise that "ready" means both worlds prepared and first tick completed. Do not claim integrated parity for `serverStarted()`.

## Required follow-up before implementation

Runtime probe dedicated and integrated startup order around `loadLevel()`, first `tickServer()`, `isReady`, and shutdown.

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted
