# Registries

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted
Status: confirmed static-source pass complete
Confidence: medium
Handoff readiness: partially ready
First-wave classification: first-wave limited

## What this concept means in Minecraft 26.1.2

Registry state is layered. `RegistryLayer` defines `STATIC`, `WORLDGEN`, `DIMENSIONS`, and `RELOADABLE`, with `STATIC` seeded from `BuiltInRegistries.REGISTRY`. `LayeredRegistryAccess` stores frozen registry access layers, supports access slices for loading and runtime use, and replaces layers from a named point forward.

Mutation is constrained by `MappedRegistry.freeze()` and `validateWrite()`. Static source proves writable registration lookups exist, but it also proves freeze is a hard state transition. The confirmation pass does not prove registry mutation is safe as a first-wave Target feature.

Reloadable server registries rebuild the reloadable layer through `ReloadableServerRegistries.reload(...)`, tag loading, validation, and `context.replaceFrom(RegistryLayer.RELOADABLE, ...)`. Client sync during configuration uses `SynchronizeRegistriesTask` and `RegistrySynchronization` to pack registry data from layered access.

## Important source boundaries

| Boundary | Source | Evidence |
|---|---|---|
| Layer vocabulary | `net.minecraft.server.RegistryLayer` | `RCF-001` |
| Layer replacement/access slicing | `net.minecraft.core.LayeredRegistryAccess` | `RCF-002` |
| Reloadable registry rebuild | `net.minecraft.server.ReloadableServerRegistries.reload` | `RCF-003`, `RR-010` |
| Freeze/write gate | `net.minecraft.core.MappedRegistry.freeze`, `validateWrite` | `RCF-004` |
| Registration lookup | `WritableRegistry.createRegistrationLookup`, `MappedRegistry.createRegistrationLookup` | `RCF-005` |
| Lookup surfaces | `RegistryAccess`, `HolderLookup`, `HolderGetter` | `RCF-006` |
| Configuration sync | `SynchronizeRegistriesTask`, `RegistrySynchronization` | `RCF-007`, `NET-08` |

## Confirmed lifecycle or timing behavior

- Registry layer names and replacement behavior are static-source confirmed.
- Reloadable registries are rebuilt asynchronously during server resource loading and replace the reloadable layer with frozen results.
- `MappedRegistry.validateWrite()` rejects writes after freeze.
- Configuration-phase registry sync is a distinct network task using layered registry access.
- The pass confirms lookup surfaces, not safe public mutation points.

## Candidate Spindle binding direction

Use registries as first-wave limited, lookup-only Target support. Expose stable lookup concepts only after server resources/registries are available. Treat built-in mutation and reloadable contribution as research-gated until lifecycle safety, freeze timing, and sync consequences are proven.

## SteelHook primitive implications

- Lookup-only access needs no hook; direct lookup binding is enough.
- Reloadable registry contribution would likely need method-around/wrap or async continuation support around `ReloadableServerRegistries.reload(...)`.
- Built-in registry mutation would need constructor/class-bootstrap or registration-phase support and is not justified for first wave.

## Rejected or deferred paths

- Built-in registry mutation is deferred.
- Treating `BuiltInRegistries` bootstrap as a public Target extension point is deferred.
- Raw mutation after freeze is rejected.

## Open questions

- Is there a narrow reloadable data registry contribution path that composes with tag loading and client sync?
- Should Spindle distinguish `RegistryAccess`, `HolderLookup.Provider`, and `HolderGetter.Provider`, or wrap them as one lookup stack?
- Which registry layers must be synchronized to clients for any future contribution feature?
