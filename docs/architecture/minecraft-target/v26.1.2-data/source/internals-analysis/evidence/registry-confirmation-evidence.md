# Registry Confirmation Evidence Packet

Minecraft version: 26.1.2
Mapping namespace: Yarn named
Worker: GPT-5.4-Mini retrieval worker

Evidence ID: RCF-001
Concept: Registry layer enum and static registry access seed
Worker: GPT-5.4-Mini retrieval worker
Source artifact: `.research-src/common/net/minecraft/server/RegistryLayer.java`
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/RegistryLayer.java`
Class: `net.minecraft.server.RegistryLayer`
Member: `STATIC`, `WORLDGEN`, `DIMENSIONS`, `RELOADABLE`, `createRegistryAccess`
Descriptor if available: `createRegistryAccess(): LayeredRegistryAccess<RegistryLayer>`
Line range or local reference: 8-19
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/RegistryLayer.java | sed -n '1,200p'`
Why this matched: The file defines the server registry layer enum and the initial layered access constructor.
Raw support: `RegistryLayer` declares four layers and `createRegistryAccess()` returns a new `LayeredRegistryAccess<>(VALUES).replaceFrom(STATIC, STATIC_ACCESS)`, where `STATIC_ACCESS` is seeded from `RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)`.
Related symbols/files: `net.minecraft.core.LayeredRegistryAccess`, `net.minecraft.core.RegistryAccess`, `net.minecraft.core.registries.BuiltInRegistries`
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: Confirm whether this layer list is the canonical server-side phase vocabulary for the current research pass.
Decision impact: Registry layer naming and the static seed access are directly evidenced here.

Evidence ID: RCF-002
Concept: Layered registry access composition and replacement
Worker: GPT-5.4-Mini retrieval worker
Source artifact: `.research-src/common/net/minecraft/core/LayeredRegistryAccess.java`
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/LayeredRegistryAccess.java`
Class: `net.minecraft.core.LayeredRegistryAccess`
Member: `getLayer`, `getAccessForLoading`, `getAccessFrom`, `replaceFrom`, `compositeAccess`
Descriptor if available: `getAccessForLoading(T): RegistryAccess.Frozen`; `getAccessFrom(T): RegistryAccess.Frozen`; `replaceFrom(T, RegistryAccess.Frozen...): LayeredRegistryAccess<T>`
Line range or local reference: 12-96
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/LayeredRegistryAccess.java | sed -n '1,260p'`
Why this matched: The class owns layered frozen registry storage, prefix/suffix access extraction, and replacement from a named layer onward.
Raw support: The constructor freezes a composite access from all stored layers. `getAccessForLoading()` returns a composite built from layers before the requested layer. `getAccessFrom()` returns a composite built from the requested layer onward. `replaceFrom()` keeps earlier layers and swaps in new frozen layers from the target index.
Related symbols/files: `net.minecraft.server.RegistryLayer`, `net.minecraft.server.ReloadableServerRegistries`, `net.minecraft.core.RegistryAccess`
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: Confirm whether the prefix/suffix split should be treated as a behavioral boundary or only as a data-access convenience.
Decision impact: This is direct evidence for layered replacement behavior and access slicing.

Evidence ID: RCF-003
Concept: Reloadable server registries rebuild the reloadable layer
Worker: GPT-5.4-Mini retrieval worker
Source artifact: `.research-src/common/net/minecraft/server/ReloadableServerRegistries.java`
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/ReloadableServerRegistries.java`
Class: `net.minecraft.server.ReloadableServerRegistries`
Member: `reload`, `scheduleRegistryLoad`, `createAndValidateFullContext`, `createUpdatedRegistries`, `concatenateLookups`, `validateLootRegistries`
Descriptor if available: `reload(LayeredRegistryAccess<RegistryLayer>, List<Registry.PendingTags<?>>, ResourceManager, Executor): CompletableFuture<LoadResult>`; `createUpdatedRegistries(...): LayeredRegistryAccess<RegistryLayer>`
Line range or local reference: 40-95
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/ReloadableServerRegistries.java | sed -n '1,260p'`
Why this matched: The reload path explicitly reads the pre-reload context, loads new writable registries, then replaces the reloadable layer with frozen results.
Raw support: `reload()` calls `context.getAccessForLoading(RegistryLayer.RELOADABLE)` before building updated tag lookups. `scheduleRegistryLoad()` creates a `MappedRegistry` for each loot data type and registers parsed entries. `createUpdatedRegistries()` returns `context.replaceFrom(RegistryLayer.RELOADABLE, new RegistryAccess.ImmutableRegistryAccess(registries).freeze())`.
Related symbols/files: `net.minecraft.core.MappedRegistry`, `net.minecraft.core.HolderLookup`, `net.minecraft.core.RegistryAccess`, `net.minecraft.tags.TagLoader`, `net.minecraft.world.level.storage.loot.LootDataType`
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: Confirm whether the reloadable layer replacement is the only runtime mutation path for server-side layered registries.
Decision impact: This is direct evidence for reloadable layer replacement and post-load validation.

Evidence ID: RCF-004
Concept: Mapped registry freeze behavior
Worker: GPT-5.4-Mini retrieval worker
Source artifact: `.research-src/common/net/minecraft/core/MappedRegistry.java`
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/MappedRegistry.java`
Class: `net.minecraft.core.MappedRegistry`
Member: `freeze`, `validateWrite`, `bindTags`, `prepareTagReload`
Descriptor if available: `freeze(): Registry<T>`
Line range or local reference: 76-120, 277-319, 350-469
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/MappedRegistry.java | sed -n '1,320p' && printf '\n---FILE---\n' && nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/MappedRegistry.java | sed -n '320,500p'`
Why this matched: The class exposes the freeze gate and the registry tag-binding path used before and after freezing.
Raw support: `validateWrite()` throws if the registry is already frozen. `freeze()` sets the frozen flag, binds values, rejects unbound entries and tags, initializes component lookup, and returns the registry. `prepareTagReload()` is only allowed after freezing and returns a `Registry.PendingTags` view whose `apply()` updates tag state.
Related symbols/files: `net.minecraft.core.WritableRegistry`, `net.minecraft.core.Registry`, `net.minecraft.core.HolderLookup.RegistryLookup`
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: Confirm whether `freeze()` is the only state transition that should be treated as a registry boundary for the research corpus.
Decision impact: This is direct evidence for registry freeze semantics.

Evidence ID: RCF-005
Concept: Writable registry registration lookup and bootstrap lookup access
Worker: GPT-5.4-Mini retrieval worker
Source artifact: `.research-src/common/net/minecraft/core/MappedRegistry.java`, `.research-src/common/net/minecraft/core/WritableRegistry.java`, `.research-src/common/net/minecraft/core/registries/BuiltInRegistries.java`
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/MappedRegistry.java`; `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/WritableRegistry.java`; `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/registries/BuiltInRegistries.java`
Class: `net.minecraft.core.MappedRegistry`; `net.minecraft.core.WritableRegistry`; `net.minecraft.core.registries.BuiltInRegistries`
Member: `createRegistrationLookup`, `WritableRegistry.createRegistrationLookup`, `BuiltInRegistries.acquireBootstrapRegistrationLookup`
Descriptor if available: `createRegistrationLookup(): HolderGetter<T>`; `acquireBootstrapRegistrationLookup(Registry<T>): HolderGetter<T>`
Line range or local reference: `MappedRegistry.java` 373-400; `WritableRegistry.java` 8-16; `BuiltInRegistries.java` 423-425
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/MappedRegistry.java | sed -n '320,500p' && printf '\n---FILE---\n' && nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/WritableRegistry.java | sed -n '1,80p' && printf '\n---FILE---\n' && nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/registries/BuiltInRegistries.java | sed -n '360,440p'`
Why this matched: The registration lookup surface is explicit on writable registries and is re-exposed by the built-in registry helper.
Raw support: `WritableRegistry` declares `createRegistrationLookup()`. `MappedRegistry.createRegistrationLookup()` validates the write state and returns a `HolderGetter` whose `getOrThrow()` routes to `getOrCreateHolderOrThrow()` and whose tag lookup routes to `getOrCreateTagForRegistration()`. `BuiltInRegistries.acquireBootstrapRegistrationLookup()` delegates to `((WritableRegistry)registry).createRegistrationLookup()`.
Related symbols/files: `net.minecraft.resources.RegistryLoadTask`, `net.minecraft.server.RegistryLayer`, `net.minecraft.server.ReloadableServerRegistries`, `net.minecraft.core.HolderGetter`
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: Confirm whether bootstrap registration lookup is the intended terminology for this access surface.
Decision impact: This is direct evidence for the registration-lookup path used while registries are still writable.

Evidence ID: RCF-006
Concept: Holder lookup provider and registry access surfaces
Worker: GPT-5.4-Mini retrieval worker
Source artifact: `.research-src/common/net/minecraft/core/RegistryAccess.java`, `.research-src/common/net/minecraft/core/HolderLookup.java`, `.research-src/common/net/minecraft/core/HolderGetter.java`
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/RegistryAccess.java`; `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/HolderLookup.java`; `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/HolderGetter.java`
Class: `net.minecraft.core.RegistryAccess`; `net.minecraft.core.HolderLookup`; `net.minecraft.core.HolderGetter`
Member: `RegistryAccess.lookup`, `RegistryAccess.lookupOrThrow`, `RegistryAccess.freeze`, `RegistryAccess.fromRegistryOfRegistries`, `HolderLookup.Provider.create`, `HolderLookup.Provider.lookupOrThrow`, `HolderLookup.Provider.listRegistries`, `HolderLookup.RegistryLookup`, `HolderGetter.Provider.lookup`, `HolderGetter.Provider.lookupOrThrow`
Descriptor if available: `lookup(ResourceKey<Registry<? extends E>>): Optional<Registry<E>>`; `create(Stream<RegistryLookup<?>>): HolderLookup.Provider`
Line range or local reference: `RegistryAccess.java` 15-67, 67-110; `HolderLookup.java` 17-135; `HolderGetter.java` 8-47
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/RegistryAccess.java | sed -n '1,280p' && printf '\n---FILE---\n' && nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/HolderLookup.java | sed -n '1,320p' && printf '\n---FILE---\n' && nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/HolderGetter.java | sed -n '1,220p'`
Why this matched: These interfaces define the lookup surfaces used by frozen registries, registry providers, and element/tag access.
Raw support: `RegistryAccess` extends `HolderLookup.Provider`, exposes `lookup`/`lookupOrThrow`, and defines `freeze()` and `fromRegistryOfRegistries(...)`. `HolderLookup.Provider.create(...)` builds a provider from a stream of registry lookups, and `createSerializationContext(...)` wraps it for `RegistryOps`. `HolderGetter.Provider` provides `lookup`, `lookupOrThrow`, and element/tag convenience accessors.
Related symbols/files: `net.minecraft.resources.RegistryOps`, `net.minecraft.core.LayeredRegistryAccess`, `net.minecraft.server.ReloadableServerRegistries`, `net.minecraft.resources.RegistryDataLoader`
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: Confirm whether the research model should distinguish `HolderGetter`, `HolderLookup`, and `RegistryAccess` as separate surfaces or one combined lookup stack.
Decision impact: This is direct evidence for holder lookup surfaces and their provider constructors.

Evidence ID: RCF-007
Concept: Registry sync task uses layered access and networked registry packing
Worker: GPT-5.4-Mini retrieval worker
Source artifact: `.research-src/common/net/minecraft/server/network/config/SynchronizeRegistriesTask.java`, `.research-src/common/net/minecraft/core/RegistrySynchronization.java`
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/network/config/SynchronizeRegistriesTask.java`; `/Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/RegistrySynchronization.java`
Class: `net.minecraft.server.network.config.SynchronizeRegistriesTask`; `net.minecraft.core.RegistrySynchronization`
Member: `start`, `sendRegistries`, `handleResponse`, `RegistrySynchronization.packRegistries`, `networkedRegistries`, `networkSafeRegistries`, `isNetworkable`
Descriptor if available: `sendRegistries(Consumer<Packet<?>>, Set<KnownPack>): void`; `packRegistries(DynamicOps<Tag>, RegistryAccess, Set<KnownPack>, BiConsumer<...>): void`
Line range or local reference: `SynchronizeRegistriesTask.java` 20-57; `RegistrySynchronization.java` 21-99
Exact command used: `nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/network/config/SynchronizeRegistriesTask.java | sed -n '1,260p' && printf '\n---FILE---\n' && nl -ba /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/core/RegistrySynchronization.java | sed -n '1,240p'`
Why this matched: The task sends registry data during configuration and delegates packing to the registry synchronization helper.
Raw support: `SynchronizeRegistriesTask.sendRegistries()` uses `registries.compositeAccess().createSerializationContext(NbtOps.INSTANCE)` and `registries.getAccessFrom(RegistryLayer.WORLDGEN)` before calling `RegistrySynchronization.packRegistries(...)`. `RegistrySynchronization.networkSafeRegistries()` combines `registries.getLayer(RegistryLayer.STATIC).registries()` with `networkedRegistries(registries)`, and `packRegistries(...)` only emits registries from `RegistryDataLoader.SYNCHRONIZED_REGISTRIES`.
Related symbols/files: `net.minecraft.server.RegistryLayer`, `net.minecraft.core.LayeredRegistryAccess`, `net.minecraft.resources.RegistryDataLoader`, `net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket`, `net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket`
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: Confirm whether this task is the canonical registry sync entry point for the configuration connection path.
Decision impact: This is direct evidence for registry sync task behavior and the layered access used to build packets.
