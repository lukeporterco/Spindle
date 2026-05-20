# Registries Spindle Implications

## Summary

Registry lookup is safe enough for first-wave documentation and likely implementation. Registry mutation is not.

## First-wave Target Layer recommendation

include as lookup-only

## Why

Layered registry access, lookup providers, reloadable layer replacement, freeze behavior, and registry sync are source-backed (`RCF-001` through `RCF-007`). `MappedRegistry.freeze()` and `validateWrite()` make mutation timing sensitive (`RCF-004`).

## Boundary recommendation

No hook required for read-only lookup through `RegistryAccess`/`HolderLookup.Provider` after server resources are available.

## SteelHook requirement

none for lookup-only. Future registry contribution would need method-around/wrap or async continuation around reloadable registry loading.

## API caution

Do not promise built-in registry mutation or post-freeze writes.

## Required follow-up before implementation

Define a wrapper over lookup surfaces and separately research reloadable registry contribution with client sync consequences.

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted
