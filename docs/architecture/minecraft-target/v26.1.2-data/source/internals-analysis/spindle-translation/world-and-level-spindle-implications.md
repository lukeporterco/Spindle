# World And Level Spindle Implications

## Summary

Loaded server worlds are accessible through `MinecraftServer` after `loadLevel()`/`prepareLevels()`. First-wave support should provide guarded lookup/enumeration, not raw storage APIs.

## First-wave Target Layer recommendation

include as lookup-only

## Why

`MinecraftServer.levels` and accessors are source-backed (`WLC-002`), while storage APIs are separate file/path/lock surfaces (`WLC-003`).

## Boundary recommendation

Direct lookup binding to `MinecraftServer.overworld()`, `getLevel(...)`, `levelKeys()`, and `getAllLevels()` after lifecycle readiness.

## SteelHook requirement

none for lookup after readiness; method-exit on `loadLevel()`/`prepareLevels()` only if Spindle emits a world-loaded event.

## API caution

Do not promise immutable snapshots if backed by live `getAllLevels()` values. Do not expose `LevelStorageSource` as runtime world API.

## Required follow-up before implementation

Choose snapshot versus guarded live view and confirm readiness timing with lifecycle probes.

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted
