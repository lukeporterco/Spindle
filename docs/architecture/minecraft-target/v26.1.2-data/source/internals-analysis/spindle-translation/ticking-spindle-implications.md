# Ticking Spindle Implications

## Summary

Server tick and level tick boundaries are clear enough for first-wave Target support.

## First-wave Target Layer recommendation

include first wave

## Why

`MinecraftServer.tickServer(...)` is once-per-server-tick (`TC-001`), `tickChildren(...)` dispatches levels (`TC-002`), and `ServerLevel.tick(...)` is per-level (`TC-003`). `LevelTicks.tick(...)` is a lower-level subsystem (`TC-004`).

## Boundary recommendation

Use `MinecraftServer.tickServer(BooleanSupplier)` for server tick and `ServerLevel.tick(BooleanSupplier)` for level tick.

## SteelHook requirement

method-entry and method-exit. Around/wrap only for future cancellation or tick mutation.

## API caution

Do not expose scheduled block/fluid ticks as first-wave Target events.

## Required follow-up before implementation

Decide event ordering labels and side behavior for dedicated versus integrated pause.

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted
