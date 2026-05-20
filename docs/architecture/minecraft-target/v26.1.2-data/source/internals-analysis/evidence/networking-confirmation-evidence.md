## EVIDENCE-01
Evidence ID: NET-01
Concept: Handshake branches into LOGIN, STATUS, and TRANSFER; LOGIN and STATUS/TRANSFER paths call outbound protocol setup, inbound protocol setup, send, and disconnect.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: ServerHandshakePacketListenerImpl.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/server/network/ServerHandshakePacketListenerImpl.java
Class: ServerHandshakePacketListenerImpl
Member: handleIntention(ClientIntentionPacket) and beginLogin(ClientIntentionPacket, boolean)
Descriptor if available: n/a (source)
Line range or local reference: 26-70
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/network/ServerHandshakePacketListenerImpl.java | sed -n '1,220p'
Why this matched: Contains the handshake `switch` over `ClientIntent`, with explicit LOGIN/STATUS/TRANSFER routing and protocol setup.
Raw support: `case LOGIN:`, `case STATUS:`, `case TRANSFER:`, `this.connection.setupOutboundProtocol(LoginProtocols.CLIENTBOUND);`, `this.connection.send(new ClientboundLoginDisconnectPacket(reason));`, `this.connection.setupInboundProtocol(LoginProtocols.SERVERBOUND, new ServerLoginPacketListenerImpl(...));`
Related symbols/files: LoginProtocols, StatusProtocols, Connection
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: confirm whether this is the only handshake listener path used in 26.1.2.
Decision impact: evidence only

## EVIDENCE-02
Evidence ID: NET-02
Concept: Memory handshake accepts LOGIN only and immediately installs login protocols.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: MemoryServerHandshakePacketListenerImpl.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/server/network/MemoryServerHandshakePacketListenerImpl.java
Class: MemoryServerHandshakePacketListenerImpl
Member: handleIntention(ClientIntentionPacket)
Descriptor if available: n/a (source)
Line range or local reference: 20-27
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/network/MemoryServerHandshakePacketListenerImpl.java | sed -n '1,220p'
Why this matched: The method rejects any non-LOGIN intent and then installs login inbound/outbound protocols directly.
Raw support: `if (packet.intention() != ClientIntent.LOGIN)`, `this.connection.setupInboundProtocol(LoginProtocols.SERVERBOUND, new ServerLoginPacketListenerImpl(...));`, `this.connection.setupOutboundProtocol(LoginProtocols.CLIENTBOUND);`
Related symbols/files: LoginProtocols, ServerLoginPacketListenerImpl
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: confirm whether this memory path is used only for localhost/in-memory connections.
Decision impact: evidence only

## EVIDENCE-03
Evidence ID: NET-03
Concept: Connection establishes handshake as the initial inbound protocol and switches login disconnect behavior based on outbound protocol; raw packet writes are direct channel writes.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: Connection.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/network/Connection.java
Class: Connection
Member: INITIAL_PROTOCOL, setupOutboundProtocol(ProtocolInfo<?>), exceptionCaught(ChannelHandlerContext, Throwable), send(Packet<?>, ChannelFutureListener, boolean)
Descriptor if available: n/a (source)
Line range or local reference: 66-68, 220-233, 131-145, 275-319
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/network/Connection.java | sed -n '1,360p'
Why this matched: Shows handshake as the initial serverbound protocol, toggles `sendLoginDisconnect` when outbound protocol is LOGIN, and uses `channel.writeAndFlush` / `channel.write` for send.
Raw support: `INITIAL_PROTOCOL = HandshakeProtocols.SERVERBOUND`, `boolean isLoginProtocol = protocol.id() == ConnectionProtocol.LOGIN;`, `this.sendLoginDisconnect = isLoginProtocol`, `Packet<?> packet = ... ? new ClientboundLoginDisconnectPacket(reason) : new ClientboundDisconnectPacket(reason)`, `this.channel.writeAndFlush(packet, ...)`
Related symbols/files: HandshakeProtocols, LoginProtocols, ClientboundLoginDisconnectPacket, ClientboundDisconnectPacket
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: confirm whether any other connection wrapper alters these boundaries.
Decision impact: evidence only

## EVIDENCE-04
Evidence ID: NET-04
Concept: Login transitions into configuration after login acknowledgement, and the login listener sends login-finished / login-disconnect packets directly.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: ServerLoginPacketListenerImpl.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/server/network/ServerLoginPacketListenerImpl.java
Class: ServerLoginPacketListenerImpl
Member: disconnect(Component), handleLoginAcknowledgement(ServerboundLoginAcknowledgedPacket)
Descriptor if available: n/a (source)
Line range or local reference: 97-101, 243-252
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/network/ServerLoginPacketListenerImpl.java | sed -n '1,360p'
Why this matched: The login listener emits login packets during the login phase and installs the configuration listener only after acknowledgement.
Raw support: `this.connection.send(new ClientboundLoginDisconnectPacket(component));`, `this.connection.send(new ClientboundLoginFinishedPacket(gameProfile));`, `this.connection.setupOutboundProtocol(ConfigurationProtocols.CLIENTBOUND);`, `this.connection.setupInboundProtocol(ConfigurationProtocols.SERVERBOUND, configPacketListener);`, `configPacketListener.startConfiguration();`
Related symbols/files: ClientboundLoginFinishedPacket, ConfigurationProtocols, CommonListenerCookie, ServerConfigurationPacketListenerImpl
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: confirm whether the `PROTOCOL_SWITCHING` state is the sole login-to-configuration handoff state.
Decision impact: evidence only

## EVIDENCE-05
Evidence ID: NET-05
Concept: CommonListenerCookie is the cookie object carried across login-to-configuration and into play spawn setup.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: CommonListenerCookie.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/server/network/CommonListenerCookie.java
Class: CommonListenerCookie
Member: record declaration and createInitial(GameProfile, boolean)
Descriptor if available: n/a (source)
Line range or local reference: 6-9
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/network/CommonListenerCookie.java | sed -n '1,160p'
Why this matched: The record stores `GameProfile`, latency, `ClientInformation`, and `transferred`, and the initial factory supplies default client information.
Raw support: `record CommonListenerCookie(GameProfile gameProfile, int latency, ClientInformation clientInformation, boolean transferred)`, `new CommonListenerCookie(gameProfile, 0, ClientInformation.createDefault(), transferred)`
Related symbols/files: ServerCommonPacketListenerImpl.createCookie, ServerLoginPacketListenerImpl.handleLoginAcknowledgement, ServerConfigurationPacketListenerImpl
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: confirm how broadly this cookie is reused outside the configuration path.
Decision impact: evidence only

## EVIDENCE-06
Evidence ID: NET-06
Concept: Configuration protocol tables define the CONFIGURATION phase packet set, including finish, keepalive, resource pack, known packs, and cookie packets.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: ConfigurationProtocols.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/network/protocol/configuration/ConfigurationProtocols.java
Class: ConfigurationProtocols
Member: SERVERBOUND_TEMPLATE and CLIENTBOUND_TEMPLATE
Descriptor if available: n/a (source)
Line range or local reference: 32-70
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/network/protocol/configuration/ConfigurationProtocols.java | sed -n '1,260p'
Why this matched: The protocol builder is explicitly keyed to `ConnectionProtocol.CONFIGURATION` and enumerates the configuration-phase packet registrations.
Raw support: `ConnectionProtocol.CONFIGURATION`, `SERVERBOUND_FINISH_CONFIGURATION`, `SERVERBOUND_SELECT_KNOWN_PACKS`, `SERVERBOUND_KEEP_ALIVE`, `SERVERBOUND_RESOURCE_PACK`, `CLIENTBOUND_FINISH_CONFIGURATION`, `CLIENTBOUND_KEEP_ALIVE`, `CLIENTBOUND_RESOURCE_PACK_PUSH`, `CLIENTBOUND_SELECT_KNOWN_PACKS`
Related symbols/files: ServerConfigurationPacketListenerImpl, ServerboundFinishConfigurationPacket, ServerboundSelectKnownPacks, ServerboundResourcePackPacket
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: confirm whether all configuration-phase packets are captured here or only the server-facing set relevant to this path.
Decision impact: evidence only

## EVIDENCE-07
Evidence ID: NET-07
Concept: Configuration listener starts the configuration task queue, pushes known-pack selection, and advances to play via finish-configuration handling.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: ServerConfigurationPacketListenerImpl.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/server/network/ServerConfigurationPacketListenerImpl.java
Class: ServerConfigurationPacketListenerImpl
Member: startConfiguration(), handleSelectKnownPacks(ServerboundSelectKnownPacks), handleConfigurationFinished(ServerboundFinishConfigurationPacket)
Descriptor if available: n/a (source)
Line range or local reference: 91-109, 152-190
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/network/ServerConfigurationPacketListenerImpl.java | sed -n '1,420p'
Why this matched: Shows the configuration bootstrap packets, known-pack task installation, and the outbound protocol switch to game when configuration finishes.
Raw support: `this.send(new ClientboundCustomPayloadPacket(...BrandPayload...))`, `this.synchronizeRegistriesTask = new SynchronizeRegistriesTask(knownPacks, registries);`, `this.configurationTasks.add(this.synchronizeRegistriesTask);`, `this.synchronizeRegistriesTask.handleResponse(packet.knownPacks(), this::send);`, `this.connection.setupOutboundProtocol(GameProtocols.CLIENTBOUND_TEMPLATE.bind(...));`
Related symbols/files: SynchronizeRegistriesTask, JoinWorldTask, ServerResourcePackConfigurationTask, CommonListenerCookie
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: confirm whether `handleConfigurationFinished` is the only exit from configuration to play in this version.
Decision impact: evidence only

## EVIDENCE-08
Evidence ID: NET-08
Concept: Known-pack negotiation is explicit and conditional on the accepted pack list matching the requested pack list.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: SynchronizeRegistriesTask.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/server/network/config/SynchronizeRegistriesTask.java
Class: SynchronizeRegistriesTask
Member: start(Consumer<Packet<?>>), handleResponse(List<KnownPack>, Consumer<Packet<?>>)
Descriptor if available: n/a (source)
Line range or local reference: 30-52
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/network/config/SynchronizeRegistriesTask.java | sed -n '1,220p'
Why this matched: The task sends known-pack choices and then branches on equality between `acceptedPacks` and `requestedPacks` before sending registry/tag data.
Raw support: `connection.accept(new ClientboundSelectKnownPacks(this.requestedPacks));`, `if (acceptedPacks.equals(this.requestedPacks))`, `this.sendRegistries(connection, Set.copyOf(this.requestedPacks));`, `this.sendRegistries(connection, Set.of());`
Related symbols/files: ClientboundSelectKnownPacks, KnownPack, ClientboundRegistryDataPacket, ClientboundUpdateTagsPacket
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: confirm whether pack negotiation affects any path outside the configuration task queue.
Decision impact: evidence only

## EVIDENCE-09
Evidence ID: NET-09
Concept: Resource-pack configuration is pushed during configuration, and a declined required pack disconnects the client.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: ServerResourcePackConfigurationTask.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/server/network/config/ServerResourcePackConfigurationTask.java
Class: ServerResourcePackConfigurationTask
Member: start(Consumer<Packet<?>>)
Descriptor if available: n/a (source)
Line range or local reference: 18-23
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/network/config/ServerResourcePackConfigurationTask.java | sed -n '1,220p'
Why this matched: The task emits a clientbound resource-pack push packet with id, url, hash, required flag, and optional prompt.
Raw support: `new ClientboundResourcePackPushPacket(this.info.id(), this.info.url(), this.info.hash(), this.info.isRequired(), Optional.ofNullable(this.info.prompt()))`
Related symbols/files: ClientboundResourcePackPushPacket, ServerCommonPacketListenerImpl.handleResourcePackResponse, ServerboundResourcePackPacket
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: confirm whether resource-pack push is only configuration-phase or can appear in play in other contexts.
Decision impact: evidence only

## EVIDENCE-10
Evidence ID: NET-10
Concept: Common packet listener owns keepalive send/timeout behavior and resource-pack response disconnect behavior.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: ServerCommonPacketListenerImpl.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/server/network/ServerCommonPacketListenerImpl.java
Class: ServerCommonPacketListenerImpl
Member: handleKeepAlive(ServerboundKeepAlivePacket), keepConnectionAlive(), disconnect(DisconnectionDetails), send(Packet<?>)
Descriptor if available: n/a (source)
Line range or local reference: 81-90, 107-130, 158-187
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/network/ServerCommonPacketListenerImpl.java | sed -n '1,280p'
Why this matched: The common listener sends keepalives, updates latency on matching keepalive responses, and uses direct packet sends for disconnect.
Raw support: `this.send(new ClientboundKeepAlivePacket(this.keepAliveChallenge));`, `if (this.keepAlivePending && packet.getId() == this.keepAliveChallenge)`, `this.disconnect(TIMEOUT_DISCONNECTION_MESSAGE);`, `this.connection.send(new ClientboundDisconnectPacket(details.reason()), PacketSendListener.thenRun(() -> this.connection.disconnect(details)));`, `if (packet.action() == ServerboundResourcePackPacket.Action.DECLINED && this.server.isResourcePackRequired())`
Related symbols/files: ClientboundKeepAlivePacket, ServerboundKeepAlivePacket, ClientboundDisconnectPacket, ServerboundResourcePackPacket
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: confirm whether keepalive and disconnect handling differ in subclasses beyond the shared implementation.
Decision impact: evidence only

## EVIDENCE-11
Evidence ID: NET-11
Concept: Game protocol tables define PLAY and explicitly include configuration-acknowledge and start-configuration packets, along with common keepalive/resource-pack packets.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: GameProtocols.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/network/protocol/game/GameProtocols.java
Class: GameProtocols
Member: SERVERBOUND_TEMPLATE and CLIENTBOUND_TEMPLATE
Descriptor if available: n/a (source)
Line range or local reference: 59-130, 131-274
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/network/protocol/game/GameProtocols.java | sed -n '1,320p'
Why this matched: The protocol builder is explicitly keyed to `ConnectionProtocol.PLAY` and lists the packets that carry the play/configuration edge behavior.
Raw support: `ConnectionProtocol.PLAY`, `SERVERBOUND_CONFIGURATION_ACKNOWLEDGED`, `CLIENTBOUND_START_CONFIGURATION`, `SERVERBOUND_KEEP_ALIVE`, `CLIENTBOUND_KEEP_ALIVE`, `SERVERBOUND_RESOURCE_PACK`, `CLIENTBOUND_RESOURCE_PACK_PUSH`
Related symbols/files: ServerGamePacketListenerImpl, ServerboundConfigurationAcknowledgedPacket, ClientboundStartConfigurationPacket
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: confirm whether these protocol tables are exhaustive for play-state packet coverage in 26.1.2.
Decision impact: evidence only

## EVIDENCE-12
Evidence ID: NET-12
Concept: Play listener explicitly initiates reconfiguration and handles the configuration-acknowledged return path.
Worker: GPT-5.4-Mini retrieval worker
Source artifact: ServerGamePacketListenerImpl.java
Minecraft version: 26.1.2
Mapping namespace: Yarn named
File path: v26.1.2/.research-src/common/net/minecraft/server/network/ServerGamePacketListenerImpl.java
Class: ServerGamePacketListenerImpl
Member: switchToConfig(), handleConfigurationAcknowledged(ServerboundConfigurationAcknowledgedPacket)
Descriptor if available: n/a (source)
Line range or local reference: 1746-1751, 2095-2104
Exact command used: nl -ba v26.1.2/.research-src/common/net/minecraft/server/network/ServerGamePacketListenerImpl.java | sed -n '1728,2115p'
Why this matched: The play listener marks the switch-to-config flag, removes the player from the world, sends the start-configuration packet, and reinstalls the configuration listener after acknowledgement.
Raw support: `this.waitingForSwitchToConfig = true;`, `this.removePlayerFromWorld();`, `this.send(ClientboundStartConfigurationPacket.INSTANCE);`, `this.connection.setupOutboundProtocol(ConfigurationProtocols.CLIENTBOUND);`, `this.connection.setupInboundProtocol(ConfigurationProtocols.SERVERBOUND, new ServerConfigurationPacketListenerImpl(...))`
Related symbols/files: ClientboundStartConfigurationPacket, ServerboundConfigurationAcknowledgedPacket, ConfigurationProtocols, ServerConfigurationPacketListenerImpl
Confidence: high
Unknowns: needs orchestrator review
Orchestrator verification: confirm whether this is the only reconfiguration entry point in play.
Decision impact: evidence only
