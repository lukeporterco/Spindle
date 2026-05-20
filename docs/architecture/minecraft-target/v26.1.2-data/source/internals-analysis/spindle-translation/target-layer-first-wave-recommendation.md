# Target Layer First Wave Recommendation

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

## Concepts safe to implement first

- Ticking: before/after server tick and level tick from `TC-001` and `TC-003`.
- Commands: limited registration-time/reload-aware support from `CMD-CONF-001` through `CMD-CONF-003`, pending exact hook insertion.

## Concepts safe only in limited form

- Server lifecycle: event-only with dedicated/shared split; no single universal ready event.
- Registries: lookup-only through `RegistryAccess`/`HolderLookup`.
- World and level: guarded lookup/enumeration after readiness.
- Networking: phase-event-only if included; otherwise documentation-only.

## Concepts that should stay documentation-only for now

- Detailed server data systems under datapacks.
- Registry sync internals.
- Storage/persistence APIs.
- Client-side networking.

## Concepts blocked by SteelHook primitive gaps

- Datapack listener contribution: reload-listener composition and async continuation.
- Command reload contribution if constructor-tail is insufficient.
- Registry contribution through reloadable layers.

## Concepts blocked by missing runtime confirmation

- Public lifecycle ready semantics.
- Reload completion safety after downstream updates.
- Integrated server parity for first-wave lifecycle events.

## Concepts rejected for first wave

- Raw packet interception.
- Scheduled block/fluid tick Target events.
- Runtime world storage APIs.
- Built-in registry mutation.
- Generic resource manager as datapack boundary.

## Minimal next Spindle Target pass sequence

1. Implement tick event model around server and level ticks.
2. Implement lifecycle observations with explicit readiness caveats.
3. Add world/level lookup wrappers after readiness.
4. Add registry lookup wrappers.
5. Prototype command registration/reload replay once hook insertion is decided.
6. Keep datapack contribution and networking packet APIs out of scope.

## What not to build yet

- Final public API names for datapack contribution.
- Built-in registry mutation.
- Low-level packet APIs.
- Storage/persistence APIs.
- Security or sandbox promises for Java mod execution.
