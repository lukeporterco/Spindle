# Resources And Datapacks

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted
Status: confirmed static-source pass complete
Confidence: medium
Handoff readiness: partially ready
First-wave classification: SteelHook-gated

## What this concept means in Minecraft 26.1.2

Server datapack/resource reload is coordinated by `MinecraftServer.reloadResources(...)`. It opens selected packs into a `MultiPackResourceManager`, loads tags for existing registries, calls `ReloadableServerResources.loadResources(...)`, and then asynchronously swaps the completed bundle into `this.resources`. After the swap it updates components/static tags, finalizes recipes, saves/reloads player resources, replaces the function library, refreshes structures, and recomputes fuel values.

`ReloadableServerResources.loadResources(...)` first reloads the reloadable registry layer, builds pending data components, constructs the resource bundle, and waits on `SimpleReloadInstance.create(...).done()` over the resource listeners. `SimpleReloadInstance` and `SimplePreparableReloadListener` provide prepare/apply future gating.

## Important source boundaries

| Boundary | Source | Evidence |
|---|---|---|
| Top-level reload and post-swap update order | `MinecraftServer.reloadResources`, `MinecraftServer.java:1539-1588` | `RR-001` |
| Server resource load future | `ReloadableServerResources.loadResources`, `ReloadableServerResources.java:76-110` | `RR-002` |
| Listener prepare/apply barrier | `SimpleReloadInstance`, `SimplePreparableReloadListener` | `RR-003`, `RR-004` |
| Generic resource manager reload | `ReloadableResourceManager.createReload` | `RR-005` |
| First-wave server data listeners | `RecipeManager`, `ServerFunctionLibrary`, `ServerAdvancementManager` | `RR-006`, `RR-007`, `RR-008`, `RR-009` |
| Reloadable registry/tag bridge | `ReloadableServerRegistries.reload`, `TagLoader` | `RR-010` |

## Confirmed lifecycle or timing behavior

- `MinecraftServer.reloadResources(...)` assigns `this.resources = newResources` before running downstream update calls.
- Safe post-reload access for all server systems is later than the assignment alone, because recipes, player resources, function library, structures, and fuel values are updated after assignment.
- Listener prepare/apply execution is future-based and barrier-gated.
- Recipes, functions, advancements, tags, loot-backed reloadable registries, structures, and server data components are distinct data-system concerns; recipes/functions/advancements are the direct `ReloadableServerResources.listeners()` list.

## Candidate Spindle binding direction

Treat resources/datapacks as SteelHook-gated for contribution and first-wave event-only for observation. A first Target pass may document reload start/complete events, but datapack contribution needs a future composition story around async reload futures and listener insertion.

## SteelHook primitive implications

- Reload observation after downstream updates: async continuation or method-exit on the `CompletableFuture` chain.
- Listener composition: method-around/wrap or a future reload-listener composition primitive.
- Data contribution through packs: no runtime hook if assets are present before pack discovery; runtime-generated pack insertion would need a pack repository/resource manager composition primitive.

## Rejected or deferred paths

- Generic `ReloadableResourceManager` as the main server datapack boundary is rejected for first wave.
- Claiming safe access immediately after `this.resources = newResources` is rejected.
- Datapack contribution APIs are deferred until async composition is understood.

## Open questions

- Which post-swap call should define "reload complete" for Spindle: function library replacement, structure refresh, or completion of the returned future?
- How should contributed reload listeners compose with vanilla listener ordering?
- Should recipes, loot, tags, functions, advancements, structures, and templates become separate Target concepts or remain under datapacks for first wave?
