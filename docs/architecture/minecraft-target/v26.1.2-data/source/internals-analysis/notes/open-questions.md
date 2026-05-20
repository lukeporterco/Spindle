# Open Questions

## Context

Questions carried forward from Minecraft 26.1.2 internals confirmation and Spindle translation. These are intentionally concrete so later Target Layer and SteelHook planning does not inherit hidden assumptions.

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

## Lifecycle

- Does Spindle need a single public "server ready" event, or separate post-world-load, dedicated-started, and first-tick-complete events?
- Does integrated server startup need a separate first-wave lifecycle path?
- Should shutdown include separate stopping, stopped, and exited events?

## Commands

- Can command contribution be safely inserted with constructor-tail, or does it need method-around/wrap of `ReloadableServerResources.loadResources(...)`?
- Must contributed commands be visible to `ServerFunctionLibrary` during reload compilation?
- What player command-tree sync is required after contributed command registration?

## Registries

- Is any registry mutation safe first wave, or should all mutation stay out until a dedicated registry contribution pass?
- How do reloadable registry contributions interact with tag validation and `SynchronizeRegistriesTask`?
- Should Spindle expose `RegistryAccess`, `HolderLookup`, and `HolderGetter` as distinct concepts or one wrapped lookup concept?

## Resources and Datapacks

- Is the safe post-reload point the returned future completion from `MinecraftServer.reloadResources(...)`, or a later runtime-observed point?
- What is the narrowest reload-listener composition primitive that avoids broad method wrapping?
- Which server data systems deserve separate Target concepts: recipes, loot, tags, functions, advancements, structures, templates, data components?

## World and Level

- Should loaded level enumeration be a stable snapshot or guarded live view?
- Is `prepareLevels()` enough for public world access, or should first tick be required?
- How should dimension keys be represented without overexposing Minecraft internals?

## Ticking

- Should first-wave events include before/after server tick and before/after level tick only?
- Does integrated pause behavior need explicit Target semantics?
- Should any post-network or post-player tick boundary be exposed later?

## Networking

- Should networking be first-wave phase-event-only, or documentation-only until a feature needs it?
- Are resource-pack, keepalive, disconnect, and known-pack tasks part of public phase events or internal details?
- What client-side networking evidence is required before any client Target scope?

## Data Generation and Assets

- Does Spindle want offline data generation, runtime pack contribution, or both?
- Where would Fabric datagen wiring enter if Spindle later targets generated JSON assets?
- Which generated asset families should be first documented: recipes, loot, tags, advancements, functions, structures/templates?
