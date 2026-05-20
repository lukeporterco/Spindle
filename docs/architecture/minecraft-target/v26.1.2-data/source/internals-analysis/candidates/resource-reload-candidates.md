# Resource Reload Candidates

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

## Candidate: server-reloadresources-completion

Decision: keep
Confidence: medium
Source-backed reason: `MinecraftServer.reloadResources(...)` opens packs, loads server resources, swaps `this.resources`, then runs downstream updates before the returned future completes. Evidence: `RR-001`.
Spindle implication: Good reload-complete observation boundary only after downstream updates, not immediately at assignment.
SteelHook primitive implication: async continuation or method-exit/future-completion support.
What would change the decision: Runtime evidence that a later callback after returned-future completion is required for safety.

## Candidate: reloadable-server-resources-listener-barrier

Decision: keep
Confidence: medium
Source-backed reason: `ReloadableServerResources.loadResources(...)` waits on `SimpleReloadInstance.create(...).done()` over `listeners()`, and `SimpleReloadInstance` gates prepare/apply futures. Evidence: `RR-002`, `RR-003`, `RR-004`, `RR-006`.
Spindle implication: Correct place to reason about datapack listener composition, but contribution is SteelHook-gated.
SteelHook primitive implication: method-around/wrap or future reload-listener composition primitive.
What would change the decision: Proof that external data can be contributed purely through pack assets before reload without runtime listener insertion.

## Candidate: first-wave-server-data-systems

Decision: keep
Confidence: medium
Source-backed reason: `ReloadableServerResources.listeners()` directly includes recipes, functions, and advancements; reloadable registries/tags and post-swap recipe finalization are also source-backed. Evidence: `RR-006` through `RR-010`.
Spindle implication: Document recipes, functions, advancements, tags, loot-backed registries, structures/templates, and data components as separate data concerns; do not design APIs yet.
SteelHook primitive implication: mostly none for documentation; contribution varies by data system and likely needs reload composition.
What would change the decision: Deeper inspection proving a smaller or broader first-wave server-data set should be named.

## Candidate: generic-reloadable-resource-manager

Decision: reject
Confidence: high
Source-backed reason: `ReloadableResourceManager.createReload(...)` is generic pack/resource plumbing; server datapack semantics are expressed in `MinecraftServer.reloadResources(...)` and `ReloadableServerResources`. Evidence: `RR-001`, `RR-005`.
Spindle implication: Do not expose this as the first-pass server datapack boundary.
SteelHook primitive implication: none for first wave.
What would change the decision: A future client/resource-pack target could revisit it separately.
