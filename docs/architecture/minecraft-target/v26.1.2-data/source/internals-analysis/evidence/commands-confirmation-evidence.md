# Commands Confirmation Evidence

Research target: Commands confirmation: dispatcher ownership/creation, reload replacement, function library ordering, sendCommands sync-only behavior
Minecraft version: 26.1.2
Mapping namespace: Yarn named

## Evidence 1

Evidence ID: CMD-CONF-001
Concept: Commands
Worker: GPT-5.4-Mini retrieval worker
Source artifact: `v26.1.2/.research-src/common/net/minecraft/commands/Commands.java`
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: `v26.1.2/.research-src/common/net/minecraft/commands/Commands.java`
Class: `net.minecraft.commands.Commands`
Member: field `dispatcher`, constructor `Commands(final Commands.CommandSelection, final CommandBuildContext)`, methods `performCommand`, `sendCommands`, `getDispatcher`
Descriptor if available: `dispatcher : Lcom/mojang/brigadier/CommandDispatcher;`, `getDispatcher()Lcom/mojang/brigadier/CommandDispatcher;`
Line range or local reference: 190-305, 322-333, 413-462
Exact command used: `rg -n "new Commands|getDispatcher|register\\(|sendCommands|performCommand|ServerFunctionLibrary|reloadResources|ReloadableServerResources" v26.1.2/.research-src/common/net/minecraft/commands/Commands.java v26.1.2/.research-src/common/net/minecraft/server/ReloadableServerResources.java v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java v26.1.2/.research-src/common/net/minecraft/server/players/PlayerList.java v26.1.2/.research-src/common/net/minecraft/server/network/ServerGamePacketListenerImpl.java` and `nl -ba v26.1.2/.research-src/common/net/minecraft/commands/Commands.java | sed -n '180,340p'`; `nl -ba v26.1.2/.research-src/common/net/minecraft/commands/Commands.java | sed -n '400,540p'`
Why this matched: This class declares and owns the Brigadier dispatcher and exposes the accessor used by later command and function paths.
Raw support: `private final CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();` appears at field declaration. The constructor fills it by calling the many `...register(this.dispatcher, ...)` methods and ends with `this.dispatcher.setConsumer(ExecutionCommandSource.resultConsumer());`. `performCommand(...)` executes parsed input, `sendCommands(...)` builds a filtered root tree and sends `new ClientboundCommandsPacket(...)`, and `getDispatcher()` returns the dispatcher.
Related symbols/files: `ServerGamePacketListenerImpl.performUnsignedChatCommand`, `ServerGamePacketListenerImpl.performSignedChatCommand`, `ServerFunctionLibrary.reload`
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: confirm whether any later stage mutates or replaces this dispatcher object outside the source shown here.
Decision impact: confirms source-level dispatcher ownership and execution/sync entry points.

## Evidence 2

Evidence ID: CMD-CONF-002
Concept: Commands
Worker: GPT-5.4-Mini retrieval worker
Source artifact: `v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java; v26.1.2/.research-src/common/net/minecraft/server/ReloadableServerResources.java`
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: `v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java`
Class: `net.minecraft.server.MinecraftServer`
Member: method `reloadResources(final Collection<String>)`
Descriptor if available: `reloadResources(Ljava/util/Collection;)Ljava/util/concurrent/CompletableFuture;`
Line range or local reference: 1539-1583
Exact command used: `nl -ba v26.1.2/.research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '1520,1595p'`; `nl -ba v26.1.2/.research-src/common/net/minecraft/server/ReloadableServerResources.java | sed -n '1,120p'`
Why this matched: This is the server reload path that constructs a fresh reloadable resource bundle and then swaps it into the server.
Raw support: `reloadResources(...)` calls `ReloadableServerResources.loadResources(...)`, then in the completion step closes the old resources, assigns `this.resources = newResources`, calls `this.getPlayerList().reloadResources()`, and calls `this.functionManager.replaceLibrary(this.resources.managers.getFunctionLibrary())`. In `ReloadableServerResources`, the constructor creates a new `Commands` instance and passes `this.commands.getDispatcher()` into `new ServerFunctionLibrary(...)`.
Related symbols/files: `ReloadableServerResources.loadResources`, `PlayerList.reloadResources`, `ServerFunctionManager.replaceLibrary`
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: confirm whether the replacement boundary is intended to be observed before or after player reload callbacks.
Decision impact: confirms reload-time replacement of the command/function resource bundle.

## Evidence 3

Evidence ID: CMD-CONF-003
Concept: Commands
Worker: GPT-5.4-Mini retrieval worker
Source artifact: `v26.1.2/.research-src/common/net/minecraft/server/ReloadableServerResources.java`
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: `v26.1.2/.research-src/common/net/minecraft/server/ReloadableServerResources.java`
Class: `net.minecraft.server.ReloadableServerResources`
Member: constructor `ReloadableServerResources(...)`, method `listeners()`
Descriptor if available: source signature only
Line range or local reference: 34-50, 72-74
Exact command used: `nl -ba v26.1.2/.research-src/common/net/minecraft/server/ReloadableServerResources.java | sed -n '1,220p'`
Why this matched: This is the reload container that wires the command dispatcher into the function library and exposes the reload listener ordering.
Raw support: The constructor creates `this.commands = new Commands(commandSelection, CommandBuildContext.simple(loadingContext, enabledFeatures));` and then `this.functionLibrary = new ServerFunctionLibrary(functionCompilationPermissions, this.commands.getDispatcher());`. `listeners()` returns `List.of(this.recipes, this.functionLibrary, this.advancements);`, placing the function library between recipes and advancements in the reload listener list.
Related symbols/files: `ServerFunctionLibrary`, `Commands.getDispatcher`, `SimpleReloadInstance.create`
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: confirm whether listener order is semantically important or only incidental to this implementation.
Decision impact: confirms the function library's reload-list placement and dispatcher binding.

## Evidence 4

Evidence ID: CMD-CONF-004
Concept: Commands
Worker: GPT-5.4-Mini retrieval worker
Source artifact: `v26.1.2/.research-src/common/net/minecraft/commands/Commands.java; v26.1.2/.research-src/common/net/minecraft/server/players/PlayerList.java`
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: `v26.1.2/.research-src/common/net/minecraft/commands/Commands.java`
Class: `net.minecraft.commands.Commands`
Member: method `sendCommands(final ServerPlayer)`
Descriptor if available: source signature only
Line range or local reference: 413-419
Exact command used: `nl -ba v26.1.2/.research-src/common/net/minecraft/commands/Commands.java | sed -n '400,540p'`; `nl -ba v26.1.2/.research-src/common/net/minecraft/server/players/PlayerList.java | sed -n '520,620p'`
Why this matched: This is the player-facing command-tree publication path and the only direct call site found in the inspected player list file.
Raw support: `sendCommands(...)` creates a `RootCommandNode<CommandSourceStack>`, filters usable nodes with `fillUsableCommands(...)`, and sends `new ClientboundCommandsPacket(root, COMMAND_NODE_INSPECTOR)` to the player's connection. In `PlayerList.sendPlayerPermissionLevel(...)`, the server calls `this.server.getCommands().sendCommands(player);` after sending the permission-level entity event packet.
Related symbols/files: `ClientboundCommandsPacket`, `fillUsableCommands`, `PlayerList.sendPlayerPermissionLevel`
Confidence: medium
Unknowns: needs orchestrator review
Orchestrator verification: confirm initial-login send timing if the first command-tree sync matters.
Decision impact: confirms command-tree sync uses packet publication rather than command execution.

## Evidence 5

Evidence ID: CMD-CONF-005
Concept: Commands
Worker: GPT-5.4-Mini retrieval worker
Source artifact: `v26.1.2/.research-src/common/net/minecraft/server/network/ServerGamePacketListenerImpl.java`
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: `v26.1.2/.research-src/common/net/minecraft/server/network/ServerGamePacketListenerImpl.java`
Class: `net.minecraft.server.network.ServerGamePacketListenerImpl`
Member: methods `performUnsignedChatCommand`, `performSignedChatCommand`, `parseCommand`
Descriptor if available: `parseCommand(Ljava/lang/String;)Lcom/mojang/brigadier/ParseResults;`
Line range or local reference: 1490-1525, 1583-1585
Exact command used: `nl -ba v26.1.2/.research-src/common/net/minecraft/server/network/ServerGamePacketListenerImpl.java | sed -n '1460,1605p'`
Why this matched: This is the live player command handling path that resolves the server command dispatcher and then delegates execution back through `Commands`.
Raw support: `parseCommand(...)` does `CommandDispatcher<CommandSourceStack> commands = this.server.getCommands().getDispatcher();` and parses against `this.player.createCommandSourceStack()`. Both `performUnsignedChatCommand(...)` and `performSignedChatCommand(...)` call `this.server.getCommands().performCommand(...)` with the parsed command, and the same listener uses the dispatcher again for completion suggestions.
Related symbols/files: `Commands.getDispatcher`, `Commands.performCommand`, `ClientboundCommandSuggestionsPacket`
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: confirm whether any command path bypasses `Commands.performCommand(...)` elsewhere in the server.
Decision impact: confirms the dispatcher-backed execution path used by player commands.
