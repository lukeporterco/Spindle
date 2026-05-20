# Networking Evidence

Minecraft version: 26.1.2
Mapping namespace: Yarn named

## NET-001
Evidence ID: NET-001
Concept: Networking
Worker: retrieval-worker
Source artifact: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/network/Connection.java
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/network/Connection.java
Class: net.minecraft.network.Connection
Member: setupInboundProtocol(ProtocolInfo<T>, T), setupOutboundProtocol(ProtocolInfo<?>), setListenerForServerboundHandshake(PacketListener), configurePacketHandler(ChannelPipeline), handleDisconnection(), send(Packet<?>), tick()
Descriptor: Java source signatures only; descriptors not emitted in the source artifact
Line range or local reference: L59-L75, L150-L233, L260-L271, L301-L359, L454-L465, L573-L586
Exact command used: cd /Users/luke/Documents/MC_Research/v26.1.2 && nl -ba .research-src/common/net/minecraft/network/Connection.java | sed -n '1,260p' && nl -ba .research-src/common/net/minecraft/network/Connection.java | sed -n '260,620p'
Why this matched: This is the low-level owner of packet dispatch, channel setup, and protocol switching.
Raw support: The class stores a volatile packet listener plus a disconnect listener, validates listener flow/protocol before installing it, and swaps the pipeline with inbound/outbound configuration tasks. It also handles packet decode errors, send queue flushing, protocol setup, and disconnect callbacks.
Related symbols/files: net.minecraft.network.PacketListener, net.minecraft.network.ConnectionProtocol, net.minecraft.network.protocol.PacketUtils, net.minecraft.network.protocol.login.LoginProtocols, net.minecraft.network.protocol.configuration.ConfigurationProtocols, net.minecraft.network.protocol.game.GameProtocols
Confidence: High
Unknowns: The class is clearly a boundary, but this evidence does not by itself prove which parts should be surfaced through Spindle.
Orchestrator verification needed: Needs orchestrator review for whether this boundary should stay internal-only or be wrapped by a higher-level Spindle concept.

## NET-002
Evidence ID: NET-002
Concept: Networking
Worker: retrieval-worker
Source artifact: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/network/ServerConnectionListener.java
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/network/ServerConnectionListener.java
Class: net.minecraft.server.network.ServerConnectionListener
Member: startTcpServerListener(InetAddress, int), startMemoryChannel(), tick(), stop(), getConnections()
Descriptor: Java source signatures only; descriptors not emitted in the source artifact
Line range or local reference: L40-L85, L96-L121, L124-L171
Exact command used: cd /Users/luke/Documents/MC_Research/v26.1.2 && nl -ba .research-src/common/net/minecraft/server/network/ServerConnectionListener.java | sed -n '1,260p'
Why this matched: This is the server-side accept loop and connection lifecycle owner.
Raw support: It constructs a server bootstrap, creates a Connection, adds it to the tracked connection list, installs the initial server-bound handshake listener, and later ticks all tracked connections to drive packet processing and disconnection cleanup.
Related symbols/files: net.minecraft.network.Connection, net.minecraft.server.network.ServerHandshakePacketListenerImpl, net.minecraft.server.network.MemoryServerHandshakePacketListenerImpl, net.minecraft.network.RateKickingConnection
Confidence: High
Unknowns: The class shows lifecycle ownership, but not a mod-facing event surface.
Orchestrator verification needed: Needs orchestrator review for whether any hook should attach at accept-time, tick-time, or disconnect-time.

## NET-003
Evidence ID: NET-003
Concept: Networking
Worker: retrieval-worker
Source artifact: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/network/ServerCommonPacketListenerImpl.java
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/network/ServerCommonPacketListenerImpl.java
Class: net.minecraft.server.network.ServerCommonPacketListenerImpl
Member: constructor(Server, Connection, CommonListenerCookie), handleKeepAlive(ServerboundKeepAlivePacket), handleResourcePackResponse(ServerboundResourcePackPacket), handleCookieResponse(ServerboundCookieResponsePacket), send(Packet<?>), disconnect(DisconnectionDetails), createCookie(ClientInformation)
Descriptor: Java source signatures only; descriptors not emitted in the source artifact
Line range or local reference: L35-L58, L67-L118, L158-L206
Exact command used: cd /Users/luke/Documents/MC_Research/v26.1.2 && nl -ba .research-src/common/net/minecraft/server/network/ServerCommonPacketListenerImpl.java | sed -n '1,280p'
Why this matched: This is the shared server listener superclass for login, configuration, and play phases.
Raw support: It keeps latency and transfer state from CommonListenerCookie, enforces keepalive timeouts, routes resource-pack and cookie responses, sends packets with disconnect wrapping, and builds the cookie used when handing off to later listeners.
Related symbols/files: net.minecraft.server.network.CommonListenerCookie, net.minecraft.network.protocol.common.ServerboundKeepAlivePacket, net.minecraft.network.protocol.common.ServerboundResourcePackPacket, net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket
Confidence: High
Unknowns: The class is shared across multiple phases, so it does not on its own define a single lifecycle boundary.
Orchestrator verification needed: Needs orchestrator review for whether the shared superclass is a distinct networking concept or just implementation scaffolding.

## NET-004
Evidence ID: NET-004
Concept: Networking
Worker: retrieval-worker
Source artifact: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/network/ServerLoginPacketListenerImpl.java and /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/network/protocol/login/ServerLoginPacketListener.java
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/network/ServerLoginPacketListenerImpl.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/network/protocol/login/ServerLoginPacketListener.java
Class: net.minecraft.server.network.ServerLoginPacketListenerImpl; net.minecraft.network.protocol.login.ServerLoginPacketListener
Member: handleHello(ServerboundHelloPacket), handleKey(ServerboundKeyPacket), handleLoginAcknowledgement(ServerboundLoginAcknowledgedPacket), tick(), protocol()
Descriptor: Java source signatures only; descriptors not emitted in the source artifact
Line range or local reference: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/network/ServerLoginPacketListenerImpl.java L51-L74, L122-L170, L172-L251; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/network/protocol/login/ServerLoginPacketListener.java L6-L19
Exact command used: cd /Users/luke/Documents/MC_Research/v26.1.2 && nl -ba .research-src/common/net/minecraft/server/network/ServerLoginPacketListenerImpl.java | sed -n '120,260p' && nl -ba .research-src/common/net/minecraft/network/protocol/login/ServerLoginPacketListener.java | sed -n '1,220p'
Why this matched: This is the server login phase and the interface that pins it to the LOGIN protocol.
Raw support: The implementation tracks explicit login states, verifies the username and key exchange, sends login compression if needed, and on login acknowledgement installs a configuration outbound protocol, creates a CommonListenerCookie, installs the configuration listener as the inbound protocol, and starts configuration.
Related symbols/files: net.minecraft.network.ConnectionProtocol.LOGIN, net.minecraft.network.protocol.configuration.ConfigurationProtocols, net.minecraft.server.network.CommonListenerCookie, net.minecraft.server.network.ServerConfigurationPacketListenerImpl
Confidence: High
Unknowns: The evidence confirms state transitions, but not whether a Spindle callback would need pre-auth, post-auth, or post-ack granularity.
Orchestrator verification needed: Needs orchestrator review for whether login should be split from configuration in the Target Layer or kept as one lifecycle concept.

## NET-005
Evidence ID: NET-005
Concept: Networking
Worker: retrieval-worker
Source artifact: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/network/ServerConfigurationPacketListenerImpl.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/network/protocol/configuration/ServerConfigurationPacketListener.java
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/network/ServerConfigurationPacketListenerImpl.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/network/protocol/configuration/ServerConfigurationPacketListener.java
Class: net.minecraft.server.network.ServerConfigurationPacketListenerImpl; net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener
Member: startConfiguration(), handleSelectKnownPacks(ServerboundSelectKnownPacks), handleAcceptCodeOfConduct(ServerboundAcceptCodeOfConductPacket), handleConfigurationFinished(ServerboundFinishConfigurationPacket), protocol()
Descriptor: Java source signatures only; descriptors not emitted in the source artifact
Line range or local reference: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/network/ServerConfigurationPacketListenerImpl.java L46-L68, L91-L115, L138-L190, L213-L238; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/network/protocol/configuration/ServerConfigurationPacketListener.java L6-L17
Exact command used: cd /Users/luke/Documents/MC_Research/v26.1.2 && nl -ba .research-src/common/net/minecraft/server/network/ServerConfigurationPacketListenerImpl.java | sed -n '1,260p' && nl -ba .research-src/common/net/minecraft/network/protocol/configuration/ServerConfigurationPacketListener.java | sed -n '1,240p'
Why this matched: This is the configuration phase boundary between login and play.
Raw support: The listener starts configuration tasks, sends brand/server-links/features packets, negotiates known packs and code-of-conduct responses, and on configuration finish swaps the outbound protocol to GameProtocols CLIENTBOUND_TEMPLATE before validating the player and spawning into the world.
Related symbols/files: net.minecraft.network.protocol.configuration.ConfigurationProtocols, net.minecraft.network.protocol.game.GameProtocols, net.minecraft.server.network.config.SynchronizeRegistriesTask, net.minecraft.server.network.config.PrepareSpawnTask, net.minecraft.server.network.config.ServerCodeOfConductConfigurationTask, net.minecraft.server.network.config.ServerResourcePackConfigurationTask
Confidence: High
Unknowns: This shows a clear config-phase boundary, but the evidence does not prove which subtask should be exposed independently.
Orchestrator verification needed: Needs orchestrator review for whether Spindle should surface configuration as a first-class networking phase.

## NET-006
Evidence ID: NET-006
Concept: Networking
Worker: retrieval-worker
Source artifact: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/network/ServerGamePacketListenerImpl.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/network/protocol/game/ServerGamePacketListener.java
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/network/ServerGamePacketListenerImpl.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/network/protocol/game/ServerGamePacketListener.java
Class: net.minecraft.server.network.ServerGamePacketListenerImpl; net.minecraft.network.protocol.game.ServerGamePacketListener
Member: switchToConfig(), handleConfigurationAcknowledged(ServerboundConfigurationAcknowledgedPacket), protocol()
Descriptor: Java source signatures only; descriptors not emitted in the source artifact
Line range or local reference: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/network/ServerGamePacketListenerImpl.java L224-L229, L1746-L1751, L2095-L2104; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/network/protocol/game/ServerGamePacketListener.java L7-L128
Exact command used: cd /Users/luke/Documents/MC_Research/v26.1.2 && nl -ba .research-src/common/net/minecraft/server/network/ServerGamePacketListenerImpl.java | sed -n '1680,1765p' && nl -ba .research-src/common/net/minecraft/server/network/ServerGamePacketListenerImpl.java | sed -n '2088,2155p' && nl -ba .research-src/common/net/minecraft/network/protocol/game/ServerGamePacketListener.java | sed -n '1,260p'
Why this matched: This is the play-phase listener, including the path back into configuration.
Raw support: The play listener uses the PLAY protocol, can send ClientboundStartConfigurationPacket and move the connection back to ConfigurationProtocols.CLIENTBOUND when the server wants reconfiguration, and on client acknowledgement installs a fresh ServerConfigurationPacketListenerImpl on the inbound side.
Related symbols/files: net.minecraft.network.protocol.configuration.ConfigurationProtocols, net.minecraft.network.protocol.game.ClientboundStartConfigurationPacket, net.minecraft.network.protocol.game.ServerboundConfigurationAcknowledgedPacket, net.minecraft.server.network.ServerConfigurationPacketListenerImpl
Confidence: High
Unknowns: The evidence shows a play-to-config transition, but not whether this is common enough for a broad Spindle hook.
Orchestrator verification needed: Needs orchestrator review for whether play-state networking should expose a reconfiguration callback or stay implicit.

## NET-007
Evidence ID: NET-007
Concept: Networking
Worker: retrieval-worker
Source artifact: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/client/net/minecraft/client/multiplayer/ClientHandshakePacketListenerImpl.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/client/net/minecraft/client/multiplayer/ClientConfigurationPacketListenerImpl.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/client/net/minecraft/client/multiplayer/ClientPacketListener.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/client/net/minecraft/client/gui/screens/multiplayer/ServerReconfigScreen.java
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/client/net/minecraft/client/multiplayer/ClientHandshakePacketListenerImpl.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/client/net/minecraft/client/multiplayer/ClientConfigurationPacketListenerImpl.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/client/net/minecraft/client/multiplayer/ClientPacketListener.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/client/net/minecraft/client/gui/screens/multiplayer/ServerReconfigScreen.java
Class: net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl; net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl; net.minecraft.client.multiplayer.ClientPacketListener; net.minecraft.client.gui.screens.multiplayer.ServerReconfigScreen
Member: handleHello(ClientboundHelloPacket), handleLoginFinished(ClientboundLoginFinishedPacket), handleConfigurationFinished(ClientboundFinishConfigurationPacket), isAcceptingMessages(), tick(), getConnection(), tick() on ServerReconfigScreen
Descriptor: Java source signatures only; descriptors not emitted in the source artifact
Line range or local reference: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/client/net/minecraft/client/multiplayer/ClientHandshakePacketListenerImpl.java L57-L76, L114-L206; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/client/net/minecraft/client/multiplayer/ClientConfigurationPacketListenerImpl.java L47-L68, L163-L206; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/client/net/minecraft/client/multiplayer/ClientPacketListener.java L357-L357, L447-L482, L2441-L2447; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/client/net/minecraft/client/gui/screens/multiplayer/ServerReconfigScreen.java L13-L60
Exact command used: cd /Users/luke/Documents/MC_Research/v26.1.2 && nl -ba .research-src/client/net/minecraft/client/multiplayer/ClientHandshakePacketListenerImpl.java | sed -n '1,320p' && nl -ba .research-src/client/net/minecraft/client/multiplayer/ClientConfigurationPacketListenerImpl.java | sed -n '1,280p' && nl -ba .research-src/client/net/minecraft/client/multiplayer/ClientCommonPacketListenerImpl.java | sed -n '1,220p' && nl -ba .research-src/client/net/minecraft/client/multiplayer/ClientPacketListener.java | sed -n '1,120p' && nl -ba .research-src/client/net/minecraft/client/multiplayer/ClientPacketListener.java | sed -n '2400,2515p' && nl -ba .research-src/client/net/minecraft/client/gui/screens/multiplayer/ServerReconfigScreen.java | sed -n '1,220p'
Why this matched: These classes mirror the server-side connection lifecycle on the client and show the current user-visible reconfiguration path.
Raw support: The handshake listener switches through AUTHORIZING, ENCRYPTING, and JOINING before installing the client configuration listener; the configuration listener collects registry/data state and then installs ClientPacketListener on configuration finish; ClientPacketListener is the play listener with isAcceptingMessages() tied to connection state; ServerReconfigScreen keeps a live connection and ticks or disconnects it during server-driven reconfiguration.
Related symbols/files: net.minecraft.network.Connection, net.minecraft.network.protocol.configuration.ConfigurationProtocols, net.minecraft.network.protocol.game.GameProtocols, net.minecraft.client.gui.screens.dialog.DialogConnectionAccess
Confidence: Medium
Unknowns: This is the strongest client mirror for networking lifecycle, but it is still evidence for existing behavior, not a recommendation for Spindle surface area.
Orchestrator verification needed: Needs orchestrator review for whether a Spindle networking concept should include client-side handoff exposure or stay server-centered.

## NET-008
Evidence ID: NET-008
Concept: Networking
Worker: retrieval-worker
Source artifact: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/network/PacketListener.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/network/ConnectionProtocol.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/network/CommonListenerCookie.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/client/net/minecraft/client/multiplayer/CommonListenerCookie.java
Mapping namespace: Yarn named
File path: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/network/PacketListener.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/network/ConnectionProtocol.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/network/CommonListenerCookie.java; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/client/net/minecraft/client/multiplayer/CommonListenerCookie.java
Class: net.minecraft.network.PacketListener; net.minecraft.network.ConnectionProtocol; net.minecraft.server.network.CommonListenerCookie; net.minecraft.client.multiplayer.CommonListenerCookie
Member: flow(), protocol(), isAcceptingMessages(), onDisconnect(DisconnectionDetails), createInitial(GameProfile, boolean), record components
Descriptor: Java source signatures only; descriptors not emitted in the source artifact
Line range or local reference: /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/network/PacketListener.java L12-L42; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/network/ConnectionProtocol.java L3-L19; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/common/net/minecraft/server/network/CommonListenerCookie.java L6-L10; /Users/luke/Documents/MC_Research/v26.1.2/.research-src/client/net/minecraft/client/multiplayer/CommonListenerCookie.java L15-L31
Exact command used: cd /Users/luke/Documents/MC_Research/v26.1.2 && nl -ba .research-src/common/net/minecraft/network/PacketListener.java | sed -n '1,220p' && nl -ba .research-src/common/net/minecraft/network/ConnectionProtocol.java | sed -n '1,220p' && nl -ba .research-src/common/net/minecraft/server/network/CommonListenerCookie.java | sed -n '1,260p' && nl -ba .research-src/client/net/minecraft/client/multiplayer/CommonListenerCookie.java | sed -n '1,260p'
Why this matched: These are the generic protocol/phase contracts and the state bundle passed between listener handoffs.
Raw support: PacketListener carries the flow/protocol contract plus disconnect and packet-error hooks; ConnectionProtocol enumerates HANDSHAKING, PLAY, STATUS, LOGIN, and CONFIGURATION; the server and client CommonListenerCookie records ferry per-connection state across listener swaps, including profile, latency, client info, registries, features, server links, cookies, and seen-player state.
Related symbols/files: net.minecraft.network.Connection, net.minecraft.server.network.ServerLoginPacketListenerImpl, net.minecraft.server.network.ServerConfigurationPacketListenerImpl, net.minecraft.server.network.ServerGamePacketListenerImpl, net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl, net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl
Confidence: High
Unknowns: This evidence names the contracts, but it does not answer which of them should become a public Spindle type.
Orchestrator verification needed: Needs orchestrator review for whether these contracts are the correct abstraction seams or only supporting internals.
