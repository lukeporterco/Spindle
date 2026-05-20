# Registry Candidates

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

## Candidate: registry-lookup-surface

Decision: keep
Confidence: high
Source-backed reason: `RegistryAccess`, `HolderLookup.Provider`, and `HolderGetter.Provider` provide lookup/listing surfaces over frozen registry state. Evidence: `RCF-006`.
Spindle implication: First-wave registry support should be lookup-only.
SteelHook primitive implication: none; direct lookup binding after readiness.
What would change the decision: None expected; this remains safe even if mutation is deferred.

## Candidate: reloadable-registry-layer-replacement

Decision: keep
Confidence: medium
Source-backed reason: `ReloadableServerRegistries.reload(...)` rebuilds reloadable registries and `LayeredRegistryAccess.replaceFrom(...)` replaces the reloadable layer. Evidence: `RCF-002`, `RCF-003`, `RR-010`.
Spindle implication: Good documentation boundary and possible future contribution boundary, but not yet first-wave mutation.
SteelHook primitive implication: method-around/wrap or async continuation if used for contribution.
What would change the decision: Runtime/source proof that external entries can be composed safely with tag validation and client sync.

## Candidate: builtin-registry-bootstrap-mutation

Decision: uncertain
Confidence: medium
Source-backed reason: writable registration lookup and freeze behavior are real, but source evidence also shows freeze rejects writes after the transition. Evidence: `RCF-004`, `RCF-005`.
Spindle implication: Keep documentation of the phase, but do not expose built-in mutation first wave.
SteelHook primitive implication: future bootstrap/class-load/constructor-tail primitive if ever pursued.
What would change the decision: Exact source-backed bootstrap timing and proof that mutation composes before freeze without destabilizing vanilla or sync.

## Candidate: registry-configuration-sync-event

Decision: keep
Confidence: medium
Source-backed reason: `SynchronizeRegistriesTask` packs registry/tag data during configuration using layered access. Evidence: `RCF-007`, `NET-08`.
Spindle implication: Useful for documenting registry-client sync and possible phase-event observation.
SteelHook primitive implication: method-entry/exit around configuration sync task if observation is needed.
What would change the decision: If networking is deferred entirely, keep this documentation-only rather than a Target event.
