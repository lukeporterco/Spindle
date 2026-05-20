# Registries Evidence Packet

Minecraft version: 26.1.2
Mapping namespace: Yarn named
Worker: retrieval

## Evidence ID: EVID-REG-001
Concept: Registries
Worker: retrieval
Source artifact: `.research-src/common/net/minecraft/core/registries/BuiltInRegistries.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/registries/BuiltInRegistries.java`
Class: `net.minecraft.core.registries.BuiltInRegistries`
Member: `bootStrap`, `freeze`, `internalRegister`, `registerSimple`, `registerDefaulted`, `registerSimpleWithIntrusiveHolders`, `registerDefaultedWithIntrusiveHolders`
Descriptor: `bootStrap(): void`; `freeze(): void`; `internalRegister(name, registry, loader): R`
Line range or local reference: 169-176, 355-429
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/registries/BuiltInRegistries.java | sed -n '1,240p' && printf '\n---FILE---\n' && nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/registries/BuiltInRegistries.java | sed -n '240,440p'`
Why this matched: This class owns the built-in registry table and the bootstrap/freeze sequence.
Raw support: Static registry fields are created through helper methods that register each built-in registry into `WRITABLE_REGISTRY`. `bootStrap()` calls `createContents()`, then `freeze()`, then `validate(REGISTRY)`. `freeze()` freezes the root registry and then each child registry. `internalRegister()` also calls `Bootstrap.checkBootstrapCalled(...)`, showing bootstrap-time gating.
Related symbols/files: `net.minecraft.server.Bootstrap`, `net.minecraft.core.MappedRegistry`, `net.minecraft.core.DefaultedMappedRegistry`, `net.minecraft.core.Registry`, `net.minecraft.core.WritableRegistry`, `net.minecraft.world.item.Item` uses `BuiltInRegistries.acquireBootstrapRegistrationLookup(...)`
Confidence: High
Unknowns: The exact runtime call order beyond `Bootstrap.checkBootstrapCalled` was not traced further.
Orchestrator verification needed: Confirm whether built-in registry bootstrap is the only mutable pre-freeze phase or whether any other bootstrap path mutates the same root registry.

## Evidence ID: EVID-REG-002
Concept: Registries
Worker: retrieval
Source artifact: `.research-src/common/net/minecraft/core/RegistryAccess.java`, `.research-src/common/net/minecraft/core/HolderLookup.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/RegistryAccess.java`; `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/HolderLookup.java`
Class: `net.minecraft.core.RegistryAccess`; `net.minecraft.core.HolderLookup`
Member: `RegistryAccess.lookupOrThrow`, `RegistryAccess.fromRegistryOfRegistries`, `RegistryAccess.freeze`, `RegistryAccess.ImmutableRegistryAccess`, `HolderLookup.Provider.lookupOrThrow`, `HolderLookup.Provider.create`, `HolderLookup.Provider.createSerializationContext`, `HolderLookup.Provider.listRegistries`, `HolderLookup.RegistryLookup.registryLifecycle`
Descriptor: `lookup(ResourceKey<Registry<? extends E>>): Optional<Registry<E>>`; `freeze(): RegistryAccess.Frozen`; `create(Stream<RegistryLookup<?>>): Provider`
Line range or local reference: `RegistryAccess.java` 15-67, 67-110; `HolderLookup.java` 30-67, 70-135
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/RegistryAccess.java | sed -n '1,180p' && printf '\n---FILE---\n' && nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/HolderLookup.java | sed -n '1,180p'`
Why this matched: These are the core lookup-oriented registry abstractions.
Raw support: `RegistryAccess` extends `HolderLookup.Provider`, exposes `lookup`/`lookupOrThrow`, and defines `Frozen`. `freeze()` converts any `RegistryAccess` into an immutable frozen access by freezing each `RegistryEntry`. `HolderLookup.Provider.create(...)` builds a provider from a stream of registry lookups, and `createSerializationContext(...)` wraps the provider for `RegistryOps`. `HolderLookup.RegistryLookup` carries per-registry lifecycle and filtering.
Related symbols/files: `net.minecraft.resources.RegistryOps`, `net.minecraft.core.LayeredRegistryAccess`, `net.minecraft.core.RegistrySetBuilder`, `net.minecraft.server.ReloadableServerRegistries`, `net.minecraft.resources.RegistryDataLoader`
Confidence: High
Unknowns: `RegistryAccess.Frozen` is an interface marker; the code does not state whether every frozen access is semantically identical beyond immutability.
Orchestrator verification needed: Confirm whether hook placement should target `RegistryAccess`-level lookup surfaces or the narrower `HolderLookup.RegistryLookup` surfaces.

## Evidence ID: EVID-REG-003
Concept: Registries
Worker: retrieval
Source artifact: `.research-src/common/net/minecraft/core/LayeredRegistryAccess.java`, `.research-src/common/net/minecraft/server/RegistryLayer.java`, `.research-src/client/net/minecraft/client/multiplayer/ClientRegistryLayer.java`, `.research-src/common/net/minecraft/server/WorldLoader.java`, `.research-src/common/net/minecraft/server/ReloadableServerRegistries.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/LayeredRegistryAccess.java`; `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/RegistryLayer.java`; `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/client/net/minecraft/client/multiplayer/ClientRegistryLayer.java`; `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/WorldLoader.java`; `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/ReloadableServerRegistries.java`
Class: `net.minecraft.core.LayeredRegistryAccess`; `net.minecraft.server.RegistryLayer`; `net.minecraft.client.multiplayer.ClientRegistryLayer`; `net.minecraft.server.WorldLoader`; `net.minecraft.server.ReloadableServerRegistries`
Member: `RegistryLayer.STATIC/WORLDGEN/DIMENSIONS/RELOADABLE`, `ClientRegistryLayer.STATIC/REMOTE`, `LayeredRegistryAccess.getAccessForLoading`, `getAccessFrom`, `replaceFrom`, `compositeAccess`, `WorldLoader.load`, `ReloadableServerRegistries.reload`, `ReloadableServerRegistries.createUpdatedRegistries`
Descriptor: `getAccessForLoading(T): Frozen`; `replaceFrom(T, Frozen...): LayeredRegistryAccess<T>`; `load(...): CompletableFuture<?>`; `reload(...): CompletableFuture<LoadResult>`
Line range or local reference: `LayeredRegistryAccess.java` 12-96; `RegistryLayer.java` 8-19; `ClientRegistryLayer.java` 9-18; `WorldLoader.java` 25-86; `ReloadableServerRegistries.java` 40-120
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/LayeredRegistryAccess.java | sed -n '1,220p' && printf '\n---FILE---\n' && nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/RegistryLayer.java | sed -n '1,120p' && printf '\n---FILE---\n' && nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/client/net/minecraft/client/multiplayer/ClientRegistryLayer.java | sed -n '1,120p' && printf '\n---FILE---\n' && nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/WorldLoader.java | sed -n '1,220p' && printf '\n---FILE---\n' && nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/ReloadableServerRegistries.java | sed -n '1,220p'`
Why this matched: These files define the layered registry boundaries used during world load, reload, and client sync.
Raw support: `RegistryLayer` declares four server phases: `STATIC`, `WORLDGEN`, `DIMENSIONS`, `RELOADABLE`. `ClientRegistryLayer` declares `STATIC` and `REMOTE`. `LayeredRegistryAccess` stores frozen layers, can return a prefix for loading via `getAccessForLoading`, can return a suffix via `getAccessFrom`, and can splice new frozen layers with `replaceFrom`. `WorldLoader.load(...)` seeds `STATIC`, loads `WORLDGEN`, then `DIMENSIONS`, then replaces the `WORLDGEN` layer with loaded worldgen and dimension registries. `ReloadableServerRegistries.reload(...)` builds a loading context from the pre-`RELOADABLE` layers, loads JSON-backed registries, and then replaces the `RELOADABLE` layer.
Related symbols/files: `net.minecraft.resources.RegistryDataLoader`, `net.minecraft.tags.TagLoader`, `net.minecraft.server.ReloadableServerResources`, `net.minecraft.client.multiplayer.RegistryDataCollector`, `net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl`
Confidence: High
Unknowns: The enum names describe phase boundaries, but the code does not explicitly label them as lifecycle stages beyond layer names.
Orchestrator verification needed: Confirm whether Spindle should treat `STATIC`, `WORLDGEN`, `DIMENSIONS`, and `RELOADABLE` as the canonical server-side registry phase vocabulary.

## Evidence ID: EVID-REG-004
Concept: Registries
Worker: retrieval
Source artifact: `.research-src/common/net/minecraft/resources/RegistryDataLoader.java`, `.research-src/client/net/minecraft/client/multiplayer/RegistryDataCollector.java`, `.research-src/client/net/minecraft/client/multiplayer/ClientConfigurationPacketListenerImpl.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/resources/RegistryDataLoader.java`; `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/client/net/minecraft/client/multiplayer/RegistryDataCollector.java`; `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/client/net/minecraft/client/multiplayer/ClientConfigurationPacketListenerImpl.java`
Class: `net.minecraft.resources.RegistryDataLoader`; `net.minecraft.client.multiplayer.RegistryDataCollector`; `net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl`
Member: `WORLDGEN_REGISTRIES`, `DIMENSION_REGISTRIES`, `SYNCHRONIZED_REGISTRIES`, `load(...)`, `createContext(...)`, `NetworkedRegistryData`, `RegistryDataCollector.loadNewElementsAndTags`, `RegistryDataCollector.collectGameRegistries`, `ClientConfigurationPacketListenerImpl.filterRegistries`
Descriptor: `load(...): CompletableFuture<RegistryAccess.Frozen>`; `filterRegistries(Frozen, Stream<ResourceKey<? extends Registry<?>>>): Frozen`
Line range or local reference: `RegistryDataLoader.java` 164-245, 303-315; `RegistryDataCollector.java` 65-168; `ClientConfigurationPacketListenerImpl.java` 209-212
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/resources/RegistryDataLoader.java | sed -n '160,340p' && printf '\n---FILE---\n' && nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/client/net/minecraft/client/multiplayer/ClientConfigurationPacketListenerImpl.java | sed -n '190,220p'`
Why this matched: These are the concrete load paths for resource-backed registries and client-synchronized registry subsets.
Raw support: `RegistryDataLoader` groups registry families into `WORLDGEN_REGISTRIES`, `DIMENSION_REGISTRIES`, and `SYNCHRONIZED_REGISTRIES`, then loads tasks and returns a frozen `RegistryAccess`. `RegistryDataCollector.collectGameRegistries(...)` uses `RegistryDataLoader.load(...)` with `NetworkedRegistryData` to merge received registries and tags, then freezes the result. `ClientConfigurationPacketListenerImpl.filterRegistries(...)` materializes a frozen subset from an existing frozen access.
Related symbols/files: `net.minecraft.server.WorldLoader`, `net.minecraft.server.ReloadableServerRegistries`, `net.minecraft.tags.TagLoader`, `net.minecraft.core.RegistrySynchronization`, `net.minecraft.resources.RegistryOps`
Confidence: High
Unknowns: The exact split between networked and static registries is mediated by `RegistrySynchronization.isNetworkable(...)`; that policy was not traced further here.
Orchestrator verification needed: Confirm whether the loaded phase should be modeled as `RegistryDataLoader` output only or also as the client-side `RegistryDataCollector` merge step.

## Evidence ID: EVID-REG-005
Concept: Registries
Worker: retrieval
Source artifact: `.research-src/common/net/minecraft/core/RegistrySetBuilder.java`, `.research-src/common/net/minecraft/data/registries/VanillaRegistries.java`, `.research-src/common/net/minecraft/data/registries/RegistryPatchGenerator.java`, `.research-src/common/net/minecraft/data/registries/TradeRebalanceRegistries.java`, `.research-src/common/net/minecraft/data/Main.java`
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/RegistrySetBuilder.java`; `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/data/registries/VanillaRegistries.java`; `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/data/registries/RegistryPatchGenerator.java`; `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/data/registries/TradeRebalanceRegistries.java`; `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/data/Main.java`
Class: `net.minecraft.core.RegistrySetBuilder`; `net.minecraft.data.registries.VanillaRegistries`; `net.minecraft.data.registries.RegistryPatchGenerator`; `net.minecraft.data.registries.TradeRebalanceRegistries`; `net.minecraft.data.Main`
Member: `build`, `buildPatch`, `PatchedRegistries`, `BuildState.bootstrapContext`, `buildProviderWithContext`, `VanillaRegistries.createLookup`, `RegistryPatchGenerator.createLookup`, `TradeRebalanceRegistries.createLookup`, `Main.addServerDefinitionProviders`
Descriptor: `build(RegistryAccess): HolderLookup.Provider`; `buildPatch(RegistryAccess, HolderLookup.Provider, Cloner.Factory): PatchedRegistries`; `createLookup(): HolderLookup.Provider`
Line range or local reference: `RegistrySetBuilder.java` 26-220, 223-433; `VanillaRegistries.java` 63-140; `RegistryPatchGenerator.java` 17-34; `TradeRebalanceRegistries.java` 8-14; `Main.java` 101-162
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/RegistrySetBuilder.java | sed -n '1,340p' && printf '\n---FILE---\n' && nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/RegistrySetBuilder.java | sed -n '340,520p' && printf '\n---FILE---\n' && nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/data/registries/VanillaRegistries.java | sed -n '1,220p' && printf '\n---FILE---\n' && nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/data/registries/RegistryPatchGenerator.java | sed -n '1,220p' && printf '\n---FILE---\n' && nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/data/registries/TradeRebalanceRegistries.java | sed -n '1,120p' && printf '\n---FILE---\n' && nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/data/Main.java | sed -n '90,170p'`
Why this matched: This is the datagen/patch path that constructs lookup-oriented registry providers from built-in static registries and optional patches.
Raw support: `RegistrySetBuilder.build(...)` returns a `HolderLookup.Provider` and uses a `BootstrapContext` to collect registry values. `buildPatch(...)` returns `PatchedRegistries(full, patches)`, where `patches` is the patch-only lookup and `full` is the lazy full-patched lookup. `VanillaRegistries.createLookup()` seeds the builder from `RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)`, then validates biome features. `RegistryPatchGenerator.createLookup()` combines vanilla lookup with a builder patch set. `Main.addServerDefinitionProviders()` binds datagen providers to these lookup futures, including the patched trade-rebalance lookup.
Related symbols/files: `net.minecraft.core.RegistryAccess`, `net.minecraft.core.HolderLookup`, `net.minecraft.resources.RegistryOps`, `net.minecraft.data.registries.VanillaRegistries.validateThatAllBiomeFeaturesHaveBiomeFilter`, `net.minecraft.data.info.RegistryComponentsReport`
Confidence: High
Unknowns: The datagen lookup path is clearly separate from runtime world loading, but the exact Spindle relevance of patch-only versus full-patched lookups remains undecided here.
Orchestrator verification needed: Confirm whether patched registry lookups should be modeled as a separate registry phase or only as a datagen-only lookup variant.
