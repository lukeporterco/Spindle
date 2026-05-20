# Networking

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted
Status: confirmed static-source pass complete
Confidence: medium
Handoff readiness: documentation-only for first wave
First-wave classification: first-wave limited

## What this concept means in Minecraft 26.1.2

Networking has distinct protocol phases and listener classes: handshake, login, configuration, play, and reconfiguration back to configuration. `Connection` starts with handshake protocol state. `ServerHandshakePacketListenerImpl` routes external handshakes into login/status/transfer. `ServerLoginPacketListenerImpl.handleLoginAcknowledgement(...)` switches to configuration protocols and creates `ServerConfigurationPacketListenerImpl`. The configuration listener queues registry sync, known-pack negotiation, optional resource-pack tasks, spawn preparation, and join-world tasks. `handleConfigurationFinished(...)` switches outbound protocol to play and spawns the player. `ServerGamePacketListenerImpl.switchToConfig()` starts reconfiguration from play.

Raw packet send is low-level: `Connection.send(...)` writes packets to the channel. That is evidence for a transport boundary, not a first-wave public Target API.

## Important source boundaries

| Boundary | Source | Evidence |
|---|---|---|
| Handshake routing | `ServerHandshakePacketListenerImpl`, `MemoryServerHandshakePacketListenerImpl` | `NET-01`, `NET-02` |
| Connection protocol and raw send | `net.minecraft.network.Connection` | `NET-03` |
| Login to configuration | `ServerLoginPacketListenerImpl.handleLoginAcknowledgement` | `NET-04` |
| Shared cookie | `CommonListenerCookie` | `NET-05` |
| Configuration protocol table | `ConfigurationProtocols` | `NET-06` |
| Configuration task queue and play handoff | `ServerConfigurationPacketListenerImpl.startConfiguration`, `handleConfigurationFinished` | `NET-07` |
| Known-pack and registry sync | `SynchronizeRegistriesTask` | `NET-08`, `RCF-007` |
| Resource pack and keepalive/common handling | `ServerResourcePackConfigurationTask`, `ServerCommonPacketListenerImpl` | `NET-09`, `NET-10` |
| Play/reconfiguration | `GameProtocols`, `ServerGamePacketListenerImpl.switchToConfig`, `handleConfigurationAcknowledged` | `NET-11`, `NET-12` |

## Confirmed lifecycle or timing behavior

- Handshake, login, configuration, and play are distinct source-level phases.
- Configuration owns registry sync, known-pack negotiation, optional resource-pack tasking, prepare-spawn, and join-world transition.
- Reconfiguration from play is source-backed through `switchToConfig()` and the configuration acknowledged packet.
- Keepalive and resource-pack responses live in common listener behavior.
- Raw `Connection.send(...)` is broad and low-level.

## Candidate Spindle binding direction

Networking should be first-wave limited at most: server-side phase-event documentation or narrow phase observations. Raw packet interception remains rejected for first wave. Client-side networking should be carried as a future question unless a specific Target need emerges.

## SteelHook primitive implications

- Phase events can use method-entry/exit around login/config/play handoff methods.
- General packet interception would need method-around/wrap around low-level send/handle paths and is rejected for first wave.
- Registry/known-pack sync observation may share configuration-phase hooks with registry documentation.

## Rejected or deferred paths

- Raw `Connection.send(...)` packet interception is rejected for first wave.
- Client-side networking exposure is deferred.
- Packet mutation/cancellation is deferred.

## Open questions

- Should first-wave networking be included as server phase events or remain documentation-only?
- Are login/config/play handoffs enough, or does Spindle need disconnect/resource-pack/keepalive phase observations?
- What client-side networking evidence is required before any client Target scope?
