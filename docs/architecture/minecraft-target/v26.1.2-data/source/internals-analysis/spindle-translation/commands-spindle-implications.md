# Commands Spindle Implications

## Summary

Commands are source-backed around the `Commands` dispatcher owner and reload-created `ReloadableServerResources` bundle. First-wave support should be registration-time and reload-aware, not late accessor mutation.

## First-wave Target Layer recommendation

include first wave

## Why

`Commands` owns/populates the dispatcher (`CMD-CONF-001`), `ReloadableServerResources` constructs `Commands` before `ServerFunctionLibrary` (`CMD-CONF-003`), and reload replaces the resource/command bundle (`CMD-CONF-002`, `RR-001`).

## Boundary recommendation

`net.minecraft.commands.Commands` constructor/constructor-tail, plus `net.minecraft.server.ReloadableServerResources.loadResources(...)` for reload replay.

## SteelHook requirement

constructor-tail or method-around/wrap. Async continuation may be needed if replay happens through reload futures.

## API caution

Do not expose `MinecraftServer.getCommands()` or `Commands.sendCommands(...)` as registration semantics.

## Required follow-up before implementation

Prove exact insertion timing so contributed commands are visible to `ServerFunctionLibrary` and player command sync.

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted
