# Resources And Datapacks Evidence

Minecraft version: 26.1.2
Mapping namespace: Yarn named

## Evidence 1
Evidence ID: RAD-001
Concept: Resources and datapacks
Worker: GPT-5.4-Mini retrieval worker
Source artifact: `.research-src/common/net/minecraft/server/commands/ReloadCommand.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/commands/ReloadCommand.java`
Class: `net.minecraft.server.commands.ReloadCommand`
Member: `reloadPacks(Collection<String>, CommandSourceStack)` and `register(CommandDispatcher<CommandSourceStack>)`
Descriptor: source signature only
Line range or local reference: `18-24`, `40-51`
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/commands/ReloadCommand.java | sed -n '1,220p'`
Why this matched: Command entry point for runtime datapack reload; `reloadPacks` calls `source.getServer().reloadResources(selectedPacks)`.
Raw support: The reload command collects selected pack ids, calls server reload, and only reports failure through the returned future's exceptional path.
Related symbols/files: `MinecraftServer.reloadResources(Collection<String>)`, `PackRepository.reload()`
Confidence: High
Unknowns: This is the user-facing trigger, not the internal prepare/apply split.
Orchestrator verification needed: Confirm whether any other command or admin path bypasses `ReloadCommand` and calls server reload directly.

## Evidence 2
Evidence ID: RAD-002
Concept: Resources and datapacks
Worker: GPT-5.4-Mini retrieval worker
Source artifact: `.research-src/common/net/minecraft/server/MinecraftServer.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java`
Class: `net.minecraft.server.MinecraftServer`
Member: `reloadResources(Collection<String>)`, `configurePackRepository(PackRepository, WorldDataConfiguration, boolean, boolean)`, `configureRepositoryWithSelection(...)`, `getSelectedPacks(...)`
Descriptor: source signature only
Line range or local reference: `1539-1588`, `1591-1690`
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '1520,1710p'`
Why this matched: This is the runtime server reload chain that opens selected packs, builds a new `MultiPackResourceManager`, loads datapack-backed resources, then swaps the server's live resource state.
Raw support: The method opens packs on the server executor, constructs `MultiPackResourceManager(PackType.SERVER_DATA, packsToLoad)`, loads `ReloadableServerResources`, closes the temporary resource manager on failure, and in the completion handler replaces `this.resources`, updates `worldData` pack config, applies tags/components, finalizes recipes, reloads player data, replaces the function library, refreshes the structure template manager, and recalculates fuel values.
Related symbols/files: `WorldDataConfiguration`, `PackRepository.openAllSelected()`, `ReloadableServerResources.loadResources(...)`, `PlayerList.reloadResources()`
Confidence: High
Unknowns: The exact point of "server-side data access safe" is inferred from the completion handler and field swap, not stated as a comment in source.
Orchestrator verification needed: Verify whether any server subsystems read old resource state after `this.resources` is replaced but before the post-load updates finish.

## Evidence 3
Evidence ID: RAD-003
Concept: Resources and datapacks
Worker: GPT-5.4-Mini retrieval worker
Source artifact: `.research-src/common/net/minecraft/server/ReloadableServerResources.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/ReloadableServerResources.java`
Class: `net.minecraft.server.ReloadableServerResources`
Member: `loadResources(...)`, `listeners()`, `updateComponentsAndStaticRegistryTags()`
Descriptor: source signature only
Line range or local reference: `72-115`
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/ReloadableServerResources.java | sed -n '1,260p'`
Why this matched: This is the server datapack resource bundle that runs the registry reload first, then runs the server-side resource listeners, then exposes a post-reload apply step for tags and components.
Raw support: `loadResources` first calls `ReloadableServerRegistries.reload(...)`, then builds `DATA_COMPONENT_INITIALIZERS`, constructs `ReloadableServerResources`, runs `SimpleReloadInstance.create(...).done()`, and only then returns the assembled manager object. `updateComponentsAndStaticRegistryTags` applies postponed registry tags and pending data components after reload.
Related symbols/files: `ReloadableServerRegistries.reload(...)`, `SimpleReloadInstance.create(...)`, `RecipeManager`, `ServerFunctionLibrary`, `ServerAdvancementManager`
Confidence: High
Unknowns: The method does not explicitly label "complete" beyond the `done()` future.
Orchestrator verification needed: Confirm whether `updateComponentsAndStaticRegistryTags()` is always called before any gameplay code consumes the new managers.

## Evidence 4
Evidence ID: RAD-004
Concept: Resources and datapacks
Worker: GPT-5.4-Mini retrieval worker
Source artifact: `.research-src/common/net/minecraft/server/packs/resources/SimpleReloadInstance.java` and `.research-src/common/net/minecraft/server/packs/resources/SimplePreparableReloadListener.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/packs/resources/SimpleReloadInstance.java`
Class: `net.minecraft.server.packs.resources.SimpleReloadInstance` / `net.minecraft.server.packs.resources.SimplePreparableReloadListener`
Member: `startTasks(...)`, `prepareTasks(...)`, `createBarrierForListener(...)`, `done()`, `reload(...)`
Descriptor: source signature only
Line range or local reference: `SimpleReloadInstance.java:29-145`, `SimplePreparableReloadListener.java:10-24`
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/packs/resources/SimpleReloadInstance.java | sed -n '1,260p'; nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/packs/resources/SimplePreparableReloadListener.java | sed -n '1,220p'`
Why this matched: These are the generic reload mechanics that define when preparation starts, when application is allowed to run, and when the reload future is considered done.
Raw support: `prepareTasks` creates a shared reload state, calls each listener's `prepareSharedState`, then runs each listener through a preparation barrier. The barrier completes `allPreparations` after the last listener clears. `SimplePreparableReloadListener.reload` is the standard prepare -> barrier wait -> apply chain. `done()` returns the reload future and `getActualProgress()` reads task/reload counts and listener completion state.
Related symbols/files: `PreparableReloadListener.SharedState`, `PreparableReloadListener.PreparationBarrier`, `ProfiledReloadInstance`
Confidence: High
Unknowns: The code gives stage ordering, but not a separate semantic label for "safe to read server data."
Orchestrator verification needed: Use this only as stage-order evidence; do not infer hook placement from progress reporting.

## Evidence 5
Evidence ID: RAD-005
Concept: Resources and datapacks
Worker: GPT-5.4-Mini retrieval worker
Source artifact: `.research-src/common/net/minecraft/server/packs/resources/ReloadableResourceManager.java`, `.research-src/common/net/minecraft/server/packs/resources/ResourceManagerReloadListener.java`, `.research-src/common/net/minecraft/server/ServerFunctionLibrary.java`, `.research-src/common/net/minecraft/world/item/crafting/RecipeManager.java`, `.research-src/common/net/minecraft/server/ServerAdvancementManager.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/packs/resources/ReloadableResourceManager.java`
Class: `net.minecraft.server.packs.resources.ReloadableResourceManager` and concrete reload listeners
Member: `createReload(...)`, `ResourceManagerReloadListener.reload(...)`, `ServerFunctionLibrary.reload(...)`, `RecipeManager.prepare(...)`, `RecipeManager.apply(...)`, `ServerAdvancementManager.apply(...)`
Descriptor: source signature only
Line range or local reference: `ReloadableResourceManager.java:39-46`, `ResourceManagerReloadListener.java:9-26`, `ServerFunctionLibrary.java:68-117`, `RecipeManager.java:73-122`, `ServerAdvancementManager.java:34-51`
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/packs/resources/ReloadableResourceManager.java | sed -n '1,180p'; nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/packs/resources/ResourceManagerReloadListener.java | sed -n '1,120p'; nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/ServerFunctionLibrary.java | sed -n '1,260p'; nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/world/item/crafting/RecipeManager.java | sed -n '1,260p'; nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/ServerAdvancementManager.java | sed -n '1,240p'`
Why this matched: These concrete listeners show the datapack payloads being prepared from `ResourceManager`, then installed into live fields during apply.
Raw support: `ServerFunctionLibrary.reload` loads tags and functions asynchronously, waits on the preparation barrier, then assigns the function and tag maps in the reload executor. `RecipeManager.prepare` scans recipe JSON from the resource manager and `apply` replaces the live recipe map. `ServerAdvancementManager.apply` validates and installs the advancement map and tree.
Related symbols/files: `ResourceManager`, `TagLoader`, `SimpleJsonResourceReloadListener`, `CommandDispatcher`, `FeatureFlagSet`
Confidence: High
Unknowns: These listeners are examples of the server datapack listeners, not the full list of every reload participant in the game.
Orchestrator verification needed: Confirm whether additional server reload listeners exist outside `ReloadableServerResources.listeners()`.

## Evidence 6
Evidence ID: RAD-006
Concept: Resources and datapacks
Worker: GPT-5.4-Mini retrieval worker
Source artifact: `.research-src/common/net/minecraft/server/WorldLoader.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/WorldLoader.java`
Class: `net.minecraft.server.WorldLoader` and `net.minecraft.server.WorldLoader.PackConfig`
Member: `load(...)`, `PackConfig.createResourceManager()`
Descriptor: source signature only
Line range or local reference: `25-85`, `99-105`
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/WorldLoader.java | sed -n '1,220p'`
Why this matched: This is the boot-time world loading path that creates the resource manager from the selected packs before registry and datapack reload work begins.
Raw support: `load` first creates the pack-config/resource-manager pair, then loads registry-driven worldgen and dimension data, then calls `ReloadableServerResources.loadResources(...)`. `PackConfig.createResourceManager` calls `MinecraftServer.configurePackRepository(...)`, opens all selected packs, and wraps them in `MultiPackResourceManager(PackType.SERVER_DATA, ...)`.
Related symbols/files: `MinecraftServer.configurePackRepository(...)`, `ReloadableServerResources.loadResources(...)`, `TagLoader`
Confidence: High
Unknowns: This is the startup path, not the in-game `/reload` path.
Orchestrator verification needed: Decide whether startup and runtime reload should be treated as separate hook surfaces in Spindle analysis.
