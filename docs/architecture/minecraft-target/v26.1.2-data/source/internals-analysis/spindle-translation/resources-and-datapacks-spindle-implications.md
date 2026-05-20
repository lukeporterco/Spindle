# Resources And Datapacks Spindle Implications

## Summary

Server resource reload is asynchronous, listener-based, and has downstream post-swap updates. Observation is plausible first wave; contribution is SteelHook-gated.

## First-wave Target Layer recommendation

include as event-only

## Why

`MinecraftServer.reloadResources(...)` swaps resources before downstream updates (`RR-001`), `ReloadableServerResources.loadResources(...)` waits on `SimpleReloadInstance.done()` (`RR-002`), and listener prepare/apply is future-gated (`RR-003`, `RR-004`).

## Boundary recommendation

Observation boundary should be the completion of the returned `CompletableFuture<Void>` from `MinecraftServer.reloadResources(...)`, not the assignment to `this.resources`.

## SteelHook requirement

async continuation for reload completion; method-around/wrap or future reload-listener composition for contribution.

## API caution

Do not promise datapack contribution, listener insertion, or safe access immediately after `this.resources = newResources`.

## Required follow-up before implementation

Runtime probe `/reload` order and decide whether recipes, loot, tags, functions, advancements, structures, and templates are separate Target concepts.

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted
