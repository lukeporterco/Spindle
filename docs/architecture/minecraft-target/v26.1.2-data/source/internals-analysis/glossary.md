# Glossary

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

## Terms

- `Server lifecycle`: Bootstrap, construction, resource/world loading, started/running, stopping, and stopped/exit phases for a Minecraft server.
- `WorldStem`: Boot-time handoff record containing resource manager, reloadable server resources, layered registries, and world data/settings.
- `ReloadableServerResources`: Server data resource owner for recipes, commands, advancements, functions, reloadable registries, and post-reload tag/component application.
- `RegistryLayer`: Server registry layer enum with `STATIC`, `WORLDGEN`, `DIMENSIONS`, and `RELOADABLE` in Minecraft 26.1.2.
- `HolderLookup.Provider`: Lookup-oriented registry provider used by codecs, commands, resources, and datagen.
- `Server tick`: Once-per-server cadence rooted at `MinecraftServer.tickServer(BooleanSupplier)`.
- `Level tick`: Per-loaded-level cadence rooted at `ServerLevel.tick(BooleanSupplier)` and called from `MinecraftServer.tickChildren(...)`.
- `Configuration phase`: Networking phase between login and play that handles feature, pack, code-of-conduct, registry synchronization, and spawn preparation tasks.
- `First-wave stable`: Source-backed enough to drive early Spindle Target Layer implementation in a narrow form.
- `First-wave limited`: Useful early, but only with explicit constraints such as lookup-only, event-only, or dedicated/shared split.
- `SteelHook-gated`: Useful concept whose implementation depends on a hook primitive or composition capability not proven ready in this research pass.
- `Research-gated`: Concept that needs runtime confirmation or more source follow-up before implementation planning.
- `Direct lookup binding`: Spindle binding that can read through an existing Minecraft owner/accessor after readiness without a hook.
- `Async continuation`: Future SteelHook capability to observe or compose a `CompletableFuture` completion boundary.
- `Evidence packet`: Compact source-backed research unit stored in `internals-analysis/evidence/`.
- `Decision`: Candidate status of `keep`, `reject`, or `uncertain` under `research-rules.md`.
