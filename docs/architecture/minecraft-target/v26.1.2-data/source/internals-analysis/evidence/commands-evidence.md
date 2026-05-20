# Commands Evidence

Research target: command dispatcher lifecycle in Minecraft 26.1.2
Minecraft version: 26.1.2
Mapping namespace: Yarn named

## Evidence 1

Evidence ID: CMD-001
Concept: Commands
Worker: retrieval
Source artifact: `.research-src/common/net/minecraft/commands/Commands.java`
Mapping namespace: Yarn named
File path: `.research-src/common/net/minecraft/commands/Commands.java`
Class: `net.minecraft.commands.Commands`
Member: field `dispatcher`, constructor `Commands(CommandSelection, CommandBuildContext)`, methods `sendCommands`, `performCommand`, `getDispatcher`
Descriptor: `dispatcher : Lcom/mojang/brigadier/CommandDispatcher;`, constructor `(<CommandSelection>, <CommandBuildContext>)V`, `getDispatcher()Lcom/mojang/brigadier/CommandDispatcher;`
Line range or local reference: 190-305, 413-462
Exact command used: `nl -ba .research-src/common/net/minecraft/commands/Commands.java | sed -n '160,560p'`
Why this matched: This is the owner class that instantiates the Brigadier dispatcher and populates it with all vanilla command registrations.
Raw support: The class declares `private final CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();` and the constructor immediately calls the `register(...)` methods for the command set, then ends with `this.dispatcher.setConsumer(ExecutionCommandSource.resultConsumer());`. The same class exposes `getDispatcher()`, executes parsed commands in `performCommand(...)`, and builds a per-player filtered tree in `sendCommands(...)` via `fillUsableCommands(...)`.
Related symbols/files: `AdvancementCommands.register`, `AttributeCommand.register`, `ReloadCommand.register`, `ServerGamePacketListenerImpl.performUnsignedChatCommand`, `ServerFunctionLibrary.reload`
Confidence: High
Unknowns: This proves owner and population, but not whether any non-vanilla injection point mutates the dispatcher later.
Orchestrator verification needed: Confirm whether any later hooks or reload stages replace this dispatcher object rather than reusing it.

## Evidence 2

Evidence ID: CMD-002
Concept: Commands
Worker: retrieval
Source artifact: `.research-src/common/net/minecraft/server/ReloadableServerResources.java`
Mapping namespace: Yarn named
File path: `.research-src/common/net/minecraft/server/ReloadableServerResources.java`
Class: `net.minecraft.server.ReloadableServerResources`
Member: field `commands`, constructor `ReloadableServerResources(...)`, method `getCommands()`, method `loadResources(...)`
Descriptor: `commands : Lnet/minecraft/commands/Commands;`, `getCommands()Lnet/minecraft/commands/Commands;`
Line range or local reference: 34-50, 64-66, 76-109
Exact command used: `nl -ba .research-src/common/net/minecraft/server/ReloadableServerResources.java | sed -n '1,220p'`
Why this matched: This is the server-side resource owner that creates the command container during reload and keeps it as part of the reloadable resource bundle.
Raw support: The constructor creates `this.commands = new Commands(commandSelection, CommandBuildContext.simple(loadingContext, enabledFeatures));` and then passes `this.commands.getDispatcher()` into `new ServerFunctionLibrary(functionCompilationPermissions, ...)`. `getCommands()` returns the owned `Commands` instance.
Related symbols/files: `ServerFunctionLibrary`, `Commands.CommandSelection`, `CommandBuildContext.simple`
Confidence: High
Unknowns: The file shows construction during reload, but not the full reload trigger sequence by itself.
Orchestrator verification needed: Confirm whether every command-set refresh passes through this reload owner or whether any alternate bootstrap path exists.

## Evidence 3

Evidence ID: CMD-003
Concept: Commands
Worker: retrieval
Source artifact: `.research-src/common/net/minecraft/server/MinecraftServer.java`
Mapping namespace: Yarn named
File path: `.research-src/common/net/minecraft/server/MinecraftServer.java`
Class: `net.minecraft.server.MinecraftServer`
Member: method `reloadResources`, method `getCommands`
Descriptor: `reloadResources(Ljava/util/Collection;)Ljava/util/concurrent/CompletableFuture;`, `getCommands()Lnet/minecraft/commands/Commands;`
Line range or local reference: 1539-1583, 1710-1712
Exact command used: `nl -ba .research-src/common/net/minecraft/server/MinecraftServer.java | sed -n '1530,1735p'`
Why this matched: This is the server-level refresh path that swaps in a newly loaded resource bundle and makes the commands owner available to callers.
Raw support: `reloadResources(...)` calls `ReloadableServerResources.loadResources(...)`, then in the completion step closes the old resources, assigns `this.resources = newResources`, and immediately calls `this.resources.managers.getCommands()` indirectly through `getCommands()`. The same refresh step also calls `this.getPlayerList().reloadResources()` and `this.functionManager.replaceLibrary(this.resources.managers.getFunctionLibrary())`, showing command/function data are refreshed together at reload time.
Related symbols/files: `ReloadableServerResources.loadResources`, `PlayerList.reloadResources`, `ServerFunctionManager.replaceLibrary`
Confidence: High
Unknowns: The code proves replacement after reload, but not which listeners observe the swap first.
Orchestrator verification needed: Verify whether command registration is ever expected before `reloadResources(...)` completes.

## Evidence 4

Evidence ID: CMD-004
Concept: Commands
Worker: retrieval
Source artifact: `.research-src/common/net/minecraft/server/network/ServerGamePacketListenerImpl.java`
Mapping namespace: Yarn named
File path: `.research-src/common/net/minecraft/server/network/ServerGamePacketListenerImpl.java`
Class: `net.minecraft.server.network.ServerGamePacketListenerImpl`
Member: methods `parseCommand`, `performUnsignedChatCommand`, `performSignedChatCommand`, `handleCustomCommandSuggestions`
Descriptor: `parseCommand(Ljava/lang/String;)Lcom/mojang/brigadier/ParseResults;`
Line range or local reference: 1490-1524, 1583-1585, 585-596
Exact command used: `nl -ba .research-src/common/net/minecraft/server/network/ServerGamePacketListenerImpl.java | sed -n '1480,1605p'`
Why this matched: This is the live server execution and suggestion path that reaches the dispatcher through `server.getCommands().getDispatcher()`.
Raw support: `parseCommand(...)` does `CommandDispatcher<CommandSourceStack> commands = this.server.getCommands().getDispatcher();` and parses against `this.player.createCommandSourceStack()`. The same listener sends chat commands to `this.server.getCommands().performCommand(...)` for both unsigned and signed command packets, and uses the dispatcher again for completion suggestions.
Related symbols/files: `MinecraftServer.getCommands`, `Commands.performCommand`, `Commands.getDispatcher`
Confidence: High
Unknowns: This is execution/suggestion availability, not registration timing.
Orchestrator verification needed: Confirm whether any command execution path bypasses `Commands.performCommand(...)`.

## Evidence 5

Evidence ID: CMD-005
Concept: Commands
Worker: retrieval
Source artifact: `.research-src/common/net/minecraft/server/players/PlayerList.java`
Mapping namespace: Yarn named
File path: `.research-src/common/net/minecraft/server/players/PlayerList.java`
Class: `net.minecraft.server.players.PlayerList`
Member: method `sendPlayerPermissionLevel`, method `reloadResources`
Descriptor: `sendPlayerPermissionLevel(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/permissions/LevelBasedPermissionSet;)V`, `reloadResources()V`
Line range or local reference: 562-575, 841-856
Exact command used: `nl -ba .research-src/common/net/minecraft/server/players/PlayerList.java | sed -n '540,610p'` and `nl -ba .research-src/common/net/minecraft/server/players/PlayerList.java | sed -n '800,880p'`
Why this matched: This shows when the server republishes the usable command tree to a player and that the player list also participates in resource reload.
Raw support: After permission changes, `sendPlayerPermissionLevel(...)` calls `this.server.getCommands().sendCommands(player);`. Separately, `reloadResources()` updates advancements, tags, and recipes for all players, which is adjacent to but distinct from dispatcher refresh.
Related symbols/files: `Commands.sendCommands`, `ClientboundCommandsPacket`, `MinecraftServer.reloadResources`
Confidence: Medium
Unknowns: This proves player-facing command-tree publication, but not initial login timing in the same file excerpt.
Orchestrator verification needed: Check the login flow if exact first-send timing matters.

## Evidence 6

Evidence ID: CMD-006
Concept: Commands
Worker: retrieval
Source artifact: `.research-src/common/net/minecraft/server/dedicated/DedicatedServer.java`
Mapping namespace: Yarn named
File path: `.research-src/common/net/minecraft/server/dedicated/DedicatedServer.java`
Class: `net.minecraft.server.dedicated.DedicatedServer`
Member: methods `handleConsoleInputs`, `runCommand`
Descriptor: `handleConsoleInputs()V`, `runCommand(Ljava/lang/String;)Ljava/lang/String;`
Line range or local reference: 513-521, 778-781
Exact command used: `nl -ba .research-src/common/net/minecraft/server/dedicated/DedicatedServer.java | sed -n '510,535p'` and `nl -ba .research-src/common/net/minecraft/server/dedicated/DedicatedServer.java | sed -n '770,790p'`
Why this matched: This is the dedicated-server console and RCON execution surface that consumes the shared commands owner.
Raw support: The console input loop calls `this.getCommands().performPrefixedCommand(input.source, input.msg);` and the RCON path does the same through `this.executeBlocking(() -> this.getCommands().performPrefixedCommand(...))`.
Related symbols/files: `Commands.performPrefixedCommand`, `MinecraftServer.getCommands`
Confidence: High
Unknowns: This covers execution, not command registration.
Orchestrator verification needed: None for basic execution availability; use if console-specific timing matters.

## Evidence 7

Evidence ID: CMD-007
Concept: Commands
Worker: retrieval
Source artifact: `.research-src/common/net/minecraft/server/ServerFunctionLibrary.java`
Mapping namespace: Yarn named
File path: `.research-src/common/net/minecraft/server/ServerFunctionLibrary.java`
Class: `net.minecraft.server.ServerFunctionLibrary`
Member: field `dispatcher`, constructor `ServerFunctionLibrary(PermissionSet, CommandDispatcher<CommandSourceStack>)`, method `reload`
Descriptor: `dispatcher : Lcom/mojang/brigadier/CommandDispatcher;`, `reload(... )Ljava/util/concurrent/CompletableFuture;`
Line range or local reference: 33-46, 63-117
Exact command used: `nl -ba .research-src/common/net/minecraft/server/ServerFunctionLibrary.java | sed -n '1,180p'`
Why this matched: This is the function-compile path that reuses the command dispatcher while datapacks are reloaded.
Raw support: The library stores the dispatcher supplied by `ReloadableServerResources` and uses it in `CommandFunction.fromLines(id, this.dispatcher, compilationContext, lines)` during reload, so function parsing stays tied to the current command set.
Related symbols/files: `ReloadableServerResources`, `Commands.createCompilationContext`, `ServerFunctionManager.getDispatcher`
Confidence: Medium
Unknowns: This proves dispatcher reuse during function reload, but not whether function compilation can observe commands before the reload barrier completes.
Orchestrator verification needed: Confirm whether function reload should be treated as command refresh or only as a consumer of the refreshed dispatcher.

## Evidence 8

Evidence ID: CMD-008
Concept: Commands
Worker: retrieval
Source artifact: `.research-src/common/net/minecraft/commands/CommandSourceStack.java`
Mapping namespace: Yarn named
File path: `.research-src/common/net/minecraft/commands/CommandSourceStack.java`
Class: `net.minecraft.commands.CommandSourceStack`
Member: method `dispatcher`
Descriptor: `dispatcher()Lcom/mojang/brigadier/CommandDispatcher;`
Line range or local reference: 570-573
Exact command used: `nl -ba .research-src/common/net/minecraft/commands/CommandSourceStack.java | sed -n '560,580p'`
Why this matched: This shows the command source handed to command execution and function execution can retrieve the dispatcher from the server via the function manager.
Raw support: `dispatcher()` returns `this.getServer().getFunctions().getDispatcher();`, which resolves to the same dispatcher path used by server command execution and function parsing.
Related symbols/files: `ServerFunctionManager.getDispatcher`, `MinecraftServer.getCommands`
Confidence: Medium
Unknowns: This is an indirect access path and may be more relevant to function execution than registration.
Orchestrator verification needed: Use if you need the exact source object that command parsers consult during nested execution.
