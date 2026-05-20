# Networking Candidates

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

## Candidate: server-connection-handshake-phase

Decision: keep
Confidence: medium
Source-backed reason: handshake listeners route into login/status/transfer and install login protocols. Evidence: `NET-01`, `NET-02`, `NET-03`.
Spindle implication: Good documentation boundary; first-wave event only if networking is included.
SteelHook primitive implication: method-entry/exit around handshake handler if observed.
What would change the decision: Client-side or proxy requirements that need deeper transport research.

## Candidate: login-to-configuration-handoff

Decision: keep
Confidence: high
Source-backed reason: `ServerLoginPacketListenerImpl.handleLoginAcknowledgement(...)` switches outbound/inbound protocols to configuration and starts configuration. Evidence: `NET-04`, `NET-05`.
Spindle implication: Safe source-backed phase event candidate, not a packet API.
SteelHook primitive implication: method-entry/exit around the handoff.
What would change the decision: Runtime confirmation showing multiple alternate login-to-config paths.

## Candidate: configuration-to-play-handoff

Decision: keep
Confidence: high
Source-backed reason: `ServerConfigurationPacketListenerImpl.handleConfigurationFinished(...)` finishes join task, switches outbound protocol to play, and spawns the player. Evidence: `NET-07`.
Spindle implication: Strongest networking phase event if first-wave networking is included.
SteelHook primitive implication: method-entry/exit around the handoff.
What would change the decision: Evidence of a separate post-spawn event that is safer for public player-ready semantics.

## Candidate: play-reconfiguration-handoff

Decision: uncertain
Confidence: medium
Source-backed reason: `ServerGamePacketListenerImpl.switchToConfig()` and `handleConfigurationAcknowledged(...)` move play back toward configuration. Evidence: `NET-11`, `NET-12`.
Spindle implication: Important to document, but probably too detailed for first-wave Target scope.
SteelHook primitive implication: method-entry/exit if exposed as a phase event.
What would change the decision: A first-wave requirement for resource-pack/registry reconfiguration events.

## Candidate: raw-connection-packet-send

Decision: reject
Confidence: high
Source-backed reason: `Connection.send(...)` is broad transport-level channel writing across protocols. Evidence: `NET-03`.
Spindle implication: Do not expose raw packet interception first wave.
SteelHook primitive implication: future method-around/wrap packet interception only if a later networking design explicitly chooses it.
What would change the decision: A new explicit Spindle requirement for low-level networking and a separate safety/design pass.
