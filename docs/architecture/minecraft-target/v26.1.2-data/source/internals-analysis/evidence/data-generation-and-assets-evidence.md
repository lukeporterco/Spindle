## Evidence Packet

### E-001
Evidence ID: E-001
Concept: Fabric skeleton shape: split source sets, mod metadata entrypoints, and base initializer.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: Fabric workspace skeleton
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/fabric-workspace/build.gradle; /Users/luke/Documents/MC_Research/v26.1.2/fabric-workspace/src/main/resources/fabric.mod.json; /Users/luke/Documents/MC_Research/v26.1.2/fabric-workspace/src/main/java/com/example/ExampleMod.java
Class: n/a
Member: n/a
Descriptor if available: n/a
Line range or local reference: build.gradle:17-24, 40-61; fabric.mod.json:17-37; ExampleMod.java:8-23
Exact command used: nl -ba v26.1.2/fabric-workspace/build.gradle | sed -n '1,220p'; nl -ba v26.1.2/fabric-workspace/src/main/resources/fabric.mod.json | sed -n '1,220p'; nl -ba v26.1.2/fabric-workspace/src/main/java/com/example/ExampleMod.java | sed -n '1,220p'
Why this matched: Required first files; shows Loom split source sets, main/client entrypoints, and the baseline ModInitializer skeleton.
Raw support: `splitEnvironmentSourceSets()`, `"main"`, `"client"`, `implements ModInitializer`
Related symbols/files: project.mod_version, net.fabricmc.fabric-loom, com.example.client.ExampleModClient
Confidence: high
Unknowns: Whether any later workspace files override the skeleton wiring.
Orchestrator verification: needs orchestrator review
Decision impact: evidence only

### E-002
Evidence ID: E-002
Concept: DataGenerator skeleton and vanilla PackOutput wiring for generated assets.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: net.minecraft.data.DataGenerator
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/data/DataGenerator.java
Class: net.minecraft.data.DataGenerator
Member: constructor, getVanillaPack, getBuiltinDatapack, inner PackGenerator, inner Cached/Uncached
Descriptor if available: n/a
Line range or local reference: 17-36, 42-123
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/data/DataGenerator.java | sed -n '1,260p'
Why this matched: Contains the required `DataGenerator` symbol and the PackOutput-backed pack generator entrypoints.
Raw support: `protected final PackOutput vanillaPackOutput`, `getVanillaPack(final boolean toRun)`, `getBuiltinDatapack(...)`
Related symbols/files: net.minecraft.data.PackOutput, net.minecraft.data.DataProvider, net.minecraft.server.Bootstrap
Confidence: high
Unknowns: No Fabric-side datagen entrypoint was inspected beyond the skeleton project files.
Orchestrator verification: needs orchestrator review
Decision impact: evidence only

### E-003
Evidence ID: E-003
Concept: PackOutput directories for data pack, resource pack, and registry JSON paths.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: net.minecraft.data.PackOutput
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/data/PackOutput.java
Class: net.minecraft.data.PackOutput
Member: getOutputFolder(PackOutput.Target), createPathProvider, createRegistryElementsPathProvider, createRegistryTagsPathProvider, Target enum, PathProvider.json
Descriptor if available: n/a
Line range or local reference: 10-73
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/data/PackOutput.java | sed -n '1,320p'
Why this matched: Contains the `PackOutput` symbol and the JSON path layout for `data`, `assets`, and registry tag/elements paths.
Raw support: `DATA_PACK("data")`, `RESOURCE_PACK("assets")`, `createRegistryTagsPathProvider`, `json(final Identifier element)`
Related symbols/files: Registries.elementsDirPath, Registries.tagsDirPath, ResourceKey<? extends Registry<?>>
Confidence: high
Unknowns: No separate Fabric adapter for PackOutput was located in the inspected files.
Orchestrator verification: needs orchestrator review
Decision impact: evidence only

### E-004
Evidence ID: E-004
Concept: Recipe JSON generation shape uses PackOutput registry paths and RecipeOutput-backed builders.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: net.minecraft.data.recipes.RecipeProvider; net.minecraft.data.recipes.packs.VanillaRecipeProvider
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/data/recipes/RecipeProvider.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/data/recipes/packs/VanillaRecipeProvider.java
Class: net.minecraft.data.recipes.RecipeProvider; net.minecraft.data.recipes.packs.VanillaRecipeProvider
Member: RecipeProvider.buildRecipes, oneToOneConversionRecipe, oreCooking, planksFromLog/planksFromLogs; VanillaRecipeProvider.buildRecipes
Descriptor if available: n/a
Line range or local reference: RecipeProvider.java:65-209, 240-249; VanillaRecipeProvider.java:55-76
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/data/recipes/RecipeProvider.java | sed -n '1,260p'; nl -ba v26.1.2/.research-src/common/net/minecraft/data/recipes/packs/VanillaRecipeProvider.java | sed -n '1,260p'
Why this matched: Required `Recipe` term and the vanilla recipe provider entrypoint that writes recipe definitions.
Raw support: `protected abstract void buildRecipes()`, `save(this.output, ...)`, `this.output.includeRootAdvancement()`
Related symbols/files: RecipeOutput, RecipeCategory, TagKey<Item>, ShapedRecipeBuilder, ShapelessRecipeBuilder
Confidence: high
Unknowns: The exact Fabric datagen hookup that instantiates the provider was not inspected here.
Orchestrator verification: needs orchestrator review
Decision impact: evidence only

### E-005
Evidence ID: E-005
Concept: Loot table JSON generation shape uses registry-keyed output paths and vanilla subproviders.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: net.minecraft.data.loot.LootTableProvider; net.minecraft.data.loot.packs.VanillaLootTableProvider
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/data/loot/LootTableProvider.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/data/loot/packs/VanillaLootTableProvider.java
Class: net.minecraft.data.loot.LootTableProvider; net.minecraft.data.loot.packs.VanillaLootTableProvider
Member: LootTableProvider constructor, run, sequenceIdForLootTable, getName; VanillaLootTableProvider.create
Descriptor if available: n/a
Line range or local reference: LootTableProvider.java:38-95, 99-119; VanillaLootTableProvider.java:11-31
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/data/loot/LootTableProvider.java | sed -n '1,260p'; nl -ba v26.1.2/.research-src/common/net/minecraft/data/loot/packs/VanillaLootTableProvider.java | sed -n '1,240p'
Why this matched: Required `LootTable` term and the provider path that saves loot tables through `PackOutput.PathProvider`.
Raw support: `output.createRegistryElementsPathProvider(Registries.LOOT_TABLE)`, `DataProvider.saveStable(...)`, `List.of(new LootTableProvider.SubProviderEntry(...))`
Related symbols/files: LootTable.DIRECT_CODEC, BuiltInLootTables.all(), LootContextParamSets
Confidence: high
Unknowns: No separate datagen bootstrap for the loot provider was inspected.
Orchestrator verification: needs orchestrator review
Decision impact: evidence only

### E-006
Evidence ID: E-006
Concept: Tag JSON generation shape and validation path for registry tag files.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: net.minecraft.data.tags.TagsProvider; net.minecraft.tags.TagLoader; net.minecraft.tags.TagFile; net.minecraft.tags.TagBuilder
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/data/tags/TagsProvider.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/tags/TagLoader.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/tags/TagFile.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/tags/TagBuilder.java
Class: net.minecraft.data.tags.TagsProvider; net.minecraft.tags.TagLoader; net.minecraft.tags.TagFile; net.minecraft.tags.TagBuilder
Member: TagsProvider.run, getOrCreateRawBuilder, createContentsProvider; TagLoader.load, build, loadTagsForRegistry; TagFile.CODEC; TagBuilder.create/build/setReplace/addElement/addTag
Descriptor if available: n/a
Line range or local reference: TagsProvider.java:30-139; TagLoader.java:38-65, 107-175; TagFile.java:8-12; TagBuilder.java:7-47
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/data/tags/TagsProvider.java | sed -n '1,260p'; nl -ba v26.1.2/.research-src/common/net/minecraft/tags/TagLoader.java | sed -n '1,260p'; nl -ba v26.1.2/.research-src/common/net/minecraft/tags/TagFile.java | sed -n '1,220p'; nl -ba v26.1.2/.research-src/common/net/minecraft/tags/TagBuilder.java | sed -n '1,260p'
Why this matched: Required `Tag` term and the exact tag-file codec plus registry-path save/load path.
Raw support: `pathProvider.json(id)`, `TagFile.CODEC`, `Registries.tagsDirPath(registryKey)`, `loadTagsForRegistry(...)`
Related symbols/files: TagKey, TagEntry, ResourceManager, FileToIdConverter.json(this.directory)
Confidence: high
Unknowns: The specific tag provider subclasses for each registry were not read individually.
Orchestrator verification: needs orchestrator review
Decision impact: evidence only

### E-007
Evidence ID: E-007
Concept: PackResources and MultiPackResourceManager are the pack ingestion layer used by resource managers.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: net.minecraft.server.packs.PackResources; net.minecraft.server.packs.resources.MultiPackResourceManager; net.minecraft.server.packs.resources.ReloadableResourceManager
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/packs/PackResources.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/packs/resources/MultiPackResourceManager.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/packs/resources/ReloadableResourceManager.java
Class: net.minecraft.server.packs.PackResources; net.minecraft.server.packs.resources.MultiPackResourceManager; net.minecraft.server.packs.resources.ReloadableResourceManager
Member: PackResources.getResource, listResources, getNamespaces; MultiPackResourceManager constructor, getResource, listResources, listPacks, close; ReloadableResourceManager.createReload
Descriptor if available: n/a
Line range or local reference: PackResources.java:14-45; MultiPackResourceManager.java:18-123; ReloadableResourceManager.java:20-46
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/packs/PackResources.java | sed -n '1,260p'; nl -ba v26.1.2/.research-src/common/net/minecraft/server/packs/resources/MultiPackResourceManager.java | sed -n '1,320p'; nl -ba v26.1.2/.research-src/common/net/minecraft/server/packs/resources/ReloadableResourceManager.java | sed -n '1,180p'
Why this matched: Required `PackResources` and `MultiPackResourceManager` terms; shows pack aggregation and delegated resource listing.
Raw support: `getResource(PackType type, Identifier location)`, `new MultiPackResourceManager(this.type, resourcePacks)`, `listResources(final String directory, final Predicate<Identifier> filter)`
Related symbols/files: FallbackResourceManager, ResourceManager, PackType, ResourceFilterSection
Confidence: high
Unknowns: The concrete pack implementation classes were not expanded beyond their manager interfaces.
Orchestrator verification: needs orchestrator review
Decision impact: evidence only

### E-008
Evidence ID: E-008
Concept: ServerPacksSource is the server-data pack repository entrypoint for vanilla and builtin packs.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: net.minecraft.server.packs.repository.ServerPacksSource
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/packs/repository/ServerPacksSource.java
Class: net.minecraft.server.packs.repository.ServerPacksSource
Member: createVanillaPackSource, createVanillaPack, createBuiltinPack, createPackRepository, createVanillaTrustedRepository
Descriptor if available: n/a
Line range or local reference: 24-85
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/packs/repository/ServerPacksSource.java | sed -n '1,260p'
Why this matched: Required `ServerPacksSource` term and shows the server-data repository wiring around vanilla pack creation.
Raw support: `super(PackType.SERVER_DATA, createVanillaPackSource(), PACKS_DIR, validator)`, `createVanillaPackSource()`, `createPackRepository(final Path datapackDir, ...)`
Related symbols/files: VanillaPackResourcesBuilder, PackType.SERVER_DATA, FolderRepositorySource
Confidence: high
Unknowns: Exact pack contents are defined elsewhere in the vanilla pack resources builder.
Orchestrator verification: needs orchestrator review
Decision impact: evidence only

### E-009
Evidence ID: E-009
Concept: ReloadableServerRegistries is the tag-and-JSON ingestion bridge before server reload completes.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: net.minecraft.server.ReloadableServerRegistries
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/ReloadableServerRegistries.java
Class: net.minecraft.server.ReloadableServerRegistries
Member: reload, scheduleRegistryLoad, createAndValidateFullContext, validateLootRegistries, Holder.getLootTable
Descriptor if available: n/a
Line range or local reference: 40-77, 84-102, 104-117
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/ReloadableServerRegistries.java | sed -n '1,220p'
Why this matched: Contains the required `ReloadableServerResources` companion path via registry reload and the tag/loot validation handoff.
Raw support: `TagLoader.buildUpdatedLookups(...)`, `SimpleJsonResourceReloadListener.scanDirectory(manager, type.registryKey(), ops, type.codec(), elements)`, `TagLoader.loadTagsForRegistry(manager, registry)`
Related symbols/files: LootDataType.values(), RegistryOps<JsonElement>, ValidationContextSource
Confidence: high
Unknowns: The precise set of JSON-backed loot registry types is defined by LootDataType.values().
Orchestrator verification: needs orchestrator review
Decision impact: evidence only

### E-010
Evidence ID: E-010
Concept: ReloadableServerResources wires the loaded registries into RecipeManager and the server reload listeners.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: net.minecraft.server.ReloadableServerResources
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/ReloadableServerResources.java
Class: net.minecraft.server.ReloadableServerResources
Member: constructor, getRecipeManager, listeners, loadResources, updateComponentsAndStaticRegistryTags
Descriptor if available: n/a
Line range or local reference: 23-115
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/ReloadableServerResources.java | sed -n '1,360p'
Why this matched: Required `ReloadableServerResources` term and the server reload wrapper that instantiates RecipeManager and runs reload listeners.
Raw support: `this.recipes = new RecipeManager(loadingContext)`, `return List.of(this.recipes, this.functionLibrary, this.advancements)`, `SimpleReloadInstance.create(...)`
Related symbols/files: ServerAdvancementManager, ServerFunctionLibrary, ReloadableServerRegistries.Holder
Confidence: high
Unknowns: The individual listeners' internal JSON paths were inspected separately.
Orchestrator verification: needs orchestrator review
Decision impact: evidence only
