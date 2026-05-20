# Command Candidates

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

## Candidate: commands-constructor-registration-window

Decision: keep
Confidence: medium
Source-backed reason: `Commands` owns and fills the Brigadier dispatcher in its constructor; `ReloadableServerResources` constructs `Commands` before `ServerFunctionLibrary`. Evidence: `CMD-CONF-001`, `CMD-CONF-003`.
Spindle implication: Best first-pass command registration window, but must be reload-aware.
SteelHook primitive implication: constructor-tail or method-around/wrap if registration must compose before function compilation.
What would change the decision: Proof of a higher-level vanilla registration callback or a reload listener boundary that accepts external registrations safely.

## Candidate: reloadable-server-resources-command-replacement

Decision: keep
Confidence: medium
Source-backed reason: `MinecraftServer.reloadResources(...)` constructs and swaps a new `ReloadableServerResources`, and that bundle owns a fresh `Commands` instance. Evidence: `CMD-CONF-002`, `RR-001`, `RR-002`.
Spindle implication: Command support must account for `/reload`; one-time startup registration is insufficient if dispatcher replacement occurs.
SteelHook primitive implication: method-around/wrap or async continuation around server resource load/reload.
What would change the decision: Source proof that command dispatcher identity is preserved across reload or that vanilla command registrations are replayed elsewhere.

## Candidate: minecraftserver-getcommands-accessor

Decision: reject
Confidence: high
Source-backed reason: `getCommands()` is an accessor used by execution and sync paths; it does not provide registration timing. Evidence: `CMD-CONF-005`.
Spindle implication: Direct lookup is useful to execute or inspect after readiness, but not a hook boundary.
SteelHook primitive implication: none.
What would change the decision: Nothing short of new source evidence that the accessor performs registration or replacement side effects.

## Candidate: commands-sendcommands-player-sync

Decision: reject
Confidence: high
Source-backed reason: `Commands.sendCommands(...)` builds a filtered tree and sends `ClientboundCommandsPacket` to a player; `PlayerList` calls it during permission sync. Evidence: `CMD-CONF-004`.
Spindle implication: Useful for command tree resend observation only, not registration.
SteelHook primitive implication: method-entry/exit only if a future sync event is needed.
What would change the decision: Evidence that vanilla uses `sendCommands(...)` to mutate the dispatcher, which current source does not show.
