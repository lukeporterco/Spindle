Evidence ID: RR-001
Concept: MinecraftServer.reloadResources orchestrates datapack reload, closes old resources, swaps in new ReloadableResources, then runs downstream updates after the swap.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: MinecraftServer.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java
Class: MinecraftServer
Member: reloadResources(Collection<String>)
Descriptor if available: public CompletableFuture<Void> reloadResources(final Collection<String> packsToEnable)
Line range or local reference: 1539-1588
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '1528,1588p'
Why this matched: The method name and body contain the requested resource reload entry point, pack opening, TagLoader usage, ReloadableServerResources.loadResources, and post-swap calls.
Raw support: lines 1539-1567 build pack resources and call ReloadableServerResources.loadResources; lines 1570-1583 close old resources, assign newResources, update components/tags, finalize recipes, reload player resources, replace the function library, refresh structure templates, and recompute fuel values.
Related symbols/files: ReloadableServerResources.loadResources; TagLoader.loadTagsForExistingRegistries; PlayerList.reloadResources; ServerFunctionManager.replaceLibrary; structureTemplateManager.onResourceManagerReload; FuelValues.vanillaBurnTimes
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: Confirm whether downstream updates listed here are the complete post-swap set for this version.
Decision impact: Direct confirmation of reloadResources as the top-level datapack reload coordinator and of the downstream update order.

Evidence ID: RR-002
Concept: ReloadableServerResources.loadResources performs registry/tag preparation first, then builds data component initializers, constructs the reloadable server resource bundle, and waits for SimpleReloadInstance completion before returning it.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: ReloadableServerResources.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/ReloadableServerResources.java
Class: ReloadableServerResources
Member: loadResources(...)
Descriptor if available: public static CompletableFuture<ReloadableServerResources> loadResources(...)
Line range or local reference: 76-110
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/ReloadableServerResources.java | sed -n '1,180p'
Why this matched: The method name is the requested loadResources path, and the body shows the nested completion chain around registry reload, pending component build, SimpleReloadInstance.create, and done().
Raw support: lines 86-89 call ReloadableServerRegistries.reload; lines 88-107 chain a supplyAsync build of BuiltInRegistries.DATA_COMPONENT_INITIALIZERS, construct ReloadableServerResources, invoke SimpleReloadInstance.create(...).done(), and thenApply(ignore -> result).
Related symbols/files: DATA_RELOAD_INITIAL_TASK; listeners(); updateComponentsAndStaticRegistryTags(); ReloadableServerRegistries.reload; BuiltInRegistries.DATA_COMPONENT_INITIALIZERS; SimpleReloadInstance.create
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: Confirm whether the loadResources chain is the only server-side resource bootstrap path for this version.
Decision impact: Confirms the completion-future boundary around server resource loading and the placement of the reload listeners.

Evidence ID: RR-003
Concept: SimpleReloadInstance sequences per-listener preparation before apply, and uses completion futures/barriers to gate listener execution and final done() completion.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: SimpleReloadInstance.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/packs/resources/SimpleReloadInstance.java
Class: SimpleReloadInstance
Member: prepareTasks(...), createBarrierForListener(...), done()
Descriptor if available: protected CompletableFuture<List<S>> prepareTasks(...); private PreparableReloadListener.PreparationBarrier createBarrierForListener(...); public CompletableFuture<?> done()
Line range or local reference: 57-120
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/packs/resources/SimpleReloadInstance.java | sed -n '1,260p'
Why this matched: The class is the requested SimpleReloadInstance, and the methods show the prepare/apply pipeline through futures, barriers, and allDone.
Raw support: lines 79-90 create sharedState, call prepareSharedState on every listener, then create each listener state via a barrier; lines 105-113 remove the listener from preparingListeners, complete allPreparations when the set is empty, and return allPreparations.thenCombine(previousBarrier, ...); lines 118-120 return the allDone future.
Related symbols/files: PreparableReloadListener.SharedState; PreparableReloadListener.PreparationBarrier; Util.sequenceFailFast; StateFactory.SIMPLE; prepareSharedState
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: Confirm whether allPreparations is the only completion gate that matters for the reload pipeline.
Decision impact: Confirms the prepare/apply gating mechanism and the completion-future handoff used by reload listeners.

Evidence ID: RR-004
Concept: SimplePreparableReloadListener implements the generic prepare-then-wait-then-apply pattern explicitly.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: SimplePreparableReloadListener.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/packs/resources/SimplePreparableReloadListener.java
Class: SimplePreparableReloadListener
Member: reload(...)
Descriptor if available: public final CompletableFuture<Void> reload(...)
Line range or local reference: 10-24
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/packs/resources/SimplePreparableReloadListener.java | sed -n '1,220p'
Why this matched: The class is a direct implementation of PreparableReloadListener and the method body names prepare and apply in the requested order.
Raw support: lines 17-19 call prepare(...), then preparationBarrier::wait, then apply(..., reloadExecutor); lines 22-24 declare abstract prepare and apply methods.
Related symbols/files: PreparableReloadListener; ResourceManager; ProfilerFiller
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: Confirm whether any server reload listeners bypass this pattern in the current version.
Decision impact: Direct evidence for prepare/apply ordering in the shared reload abstraction.

Evidence ID: RR-005
Concept: ReloadableResourceManager closes the old resource set, installs the new pack set, and creates a SimpleReloadInstance over the registered listeners.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: ReloadableResourceManager.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/packs/resources/ReloadableResourceManager.java
Class: ReloadableResourceManager
Member: createReload(...)
Descriptor if available: public ReloadInstance createReload(...)
Line range or local reference: 39-46
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/packs/resources/ReloadableResourceManager.java | sed -n '1,180p'
Why this matched: The class name and method name are direct hits for the requested resource manager reload path.
Raw support: lines 43-45 close this.resources, assign a new MultiPackResourceManager, and return SimpleReloadInstance.create(this.resources, this.listeners, backgroundExecutor, mainThreadExecutor, initialTask, ...).
Related symbols/files: MultiPackResourceManager; PreparableReloadListener; PackResources; PackType
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: Confirm whether this is the only ReloadableResourceManager reload path in 26.1.2.
Decision impact: Confirms the resource-manager handoff into SimpleReloadInstance.

Evidence ID: RR-006
Concept: ReloadableServerResources bundles the first-wave server data systems as reload listeners: RecipeManager, ServerFunctionLibrary, and ServerAdvancementManager.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: ReloadableServerResources.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/ReloadableServerResources.java
Class: ReloadableServerResources
Member: constructor, listeners()
Descriptor if available: private ReloadableServerResources(...); public List<PreparableReloadListener> listeners()
Line range or local reference: 34-74
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/ReloadableServerResources.java | sed -n '1,180p'
Why this matched: The constructor instantiates the named server data systems and listeners() exposes them to the reload pipeline.
Raw support: lines 46-49 construct RecipeManager, Commands, ServerAdvancementManager, and ServerFunctionLibrary; lines 72-74 return List.of(this.recipes, this.functionLibrary, this.advancements).
Related symbols/files: RecipeManager; Commands; ServerAdvancementManager; ServerFunctionLibrary; ReloadableServerRegistries.Holder
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: Confirm whether these are intended as the only first-wave server data systems for this reload path.
Decision impact: Confirms which server-side data systems are participating in the initial reload wave.

Evidence ID: RR-007
Concept: ServerFunctionLibrary reloads tags and function sources asynchronously, waits on the preparation barrier, then applies both loaded functions and built tags on the reload executor.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: ServerFunctionLibrary.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/ServerFunctionLibrary.java
Class: ServerFunctionLibrary
Member: reload(...)
Descriptor if available: public CompletableFuture<Void> reload(...)
Line range or local reference: 68-117
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/ServerFunctionLibrary.java | sed -n '1,260p'
Why this matched: The class is the named function library and the method body contains the requested CompletableFuture, preparation barrier, and apply step.
Raw support: lines 76-97 build tags and function futures with supplyAsync; line 99 calls preparationBarrier::wait; lines 100-116 run the apply block on reloadExecutor to assign this.functions and this.tags.
Related symbols/files: TagLoader; CommandFunction; CommandDispatcher; MinecraftServer.reloadResources
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: Confirm whether function tag application timing relative to other reload listeners matters for the target.
Decision impact: Confirms one of the first-wave data systems and its own prepare/apply sequencing.

Evidence ID: RR-008
Concept: RecipeManager uses a prepare/apply split, and finalizeRecipeLoading performs the post-reload recipe curation step.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: RecipeManager.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/world/item/crafting/RecipeManager.java
Class: RecipeManager
Member: prepare(...), apply(...), finalizeRecipeLoading(...)
Descriptor if available: protected RecipeMap prepare(final ResourceManager manager, final ProfilerFiller profiler); protected void apply(final RecipeMap recipes, final ResourceManager manager, final ProfilerFiller profiler); public void finalizeRecipeLoading(final FeatureFlagSet enabledFlags)
Line range or local reference: 73-122
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/world/item/crafting/RecipeManager.java | sed -n '1,260p'
Why this matched: The class and methods are directly named in the target terms and appear in the first-wave server resources bundle.
Raw support: lines 73-88 show prepare building a RecipeMap from scanned JSON resources and apply assigning this.recipes; lines 90-122 show finalizeRecipeLoading deriving property sets, stonecutter recipes, allDisplays, and recipeToDisplay.
Related symbols/files: SimpleJsonResourceReloadListener.scanDirectory; FeatureFlagSet; SelectableRecipe; RecipeDisplayInfo
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: Confirm whether finalizeRecipeLoading is always called only after the reload swap in this version.
Decision impact: Confirms the recipe manager's post-reload follow-up step.

Evidence ID: RR-009
Concept: ServerAdvancementManager applies loaded advancements into an AdvancementTree and repositions root nodes with displays.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: ServerAdvancementManager.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/ServerAdvancementManager.java
Class: ServerAdvancementManager
Member: apply(...)
Descriptor if available: protected void apply(final Map<Identifier, Advancement> preparations, final ResourceManager manager, final ProfilerFiller profiler)
Line range or local reference: 23-51
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/ServerAdvancementManager.java | sed -n '1,260p'
Why this matched: The advancement manager is one of the listed first-wave data systems and the apply method directly mentions AdvancementTree.
Raw support: lines 34-50 build AdvancementHolder values, assign this.advancements, create a new AdvancementTree, addAll, run TreeNodePosition on root nodes with displays, and assign this.tree.
Related symbols/files: AdvancementTree; AdvancementHolder; TreeNodePosition; SimpleJsonResourceReloadListener
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: Confirm whether any additional advancement post-processing occurs elsewhere in the reload path.
Decision impact: Confirms the advancement tree is rebuilt during apply and not inferred from surrounding code.

Evidence ID: RR-010
Concept: ReloadableServerRegistries.reload uses TagLoader to build updated registry lookups and load registry tags during the reloadable registry bootstrap path.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: ReloadableServerRegistries.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/ReloadableServerRegistries.java
Class: ReloadableServerRegistries
Member: reload(...)
Descriptor if available: public static CompletableFuture<ReloadableServerRegistries.LoadResult> reload(...)
Line range or local reference: 40-66
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/ReloadableServerRegistries.java | sed -n '1,180p' && nl -ba v26.1.2/.research-src/common/net/minecraft/tags/TagLoader.java | sed -n '140,210p'
Why this matched: The reload method directly calls the tag loader helpers named in the target terms.
Raw support: lines 46-53 call TagLoader.buildUpdatedLookups, sequence registry loads, and return the validated load result; lines 62-64 run TagLoader.loadTagsForRegistry(manager, registry) inside each registry load.
Related symbols/files: TagLoader.loadTagsForExistingRegistries; TagLoader.buildUpdatedLookups; TagLoader.loadTagsForRegistry; Registry.PendingTags; HolderLookup.RegistryLookup; LootDataType
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: Confirm whether separate TagLoader.loadTagsForExistingRegistries evidence is needed for the target packet.
Decision impact: Confirms the tag-loading phase and its connection to reloadable registry creation.
