# Target Layer First-Wave Plan

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

This plan orders future Target Layer work by evidence strength, SteelHook readiness, and blast radius. It is documentation for a later implementation pass, not implementation work.

1. Tick events.
2. Limited lifecycle observations with explicit ready semantics.
3. Guarded world/level lookup and enumeration.
4. Registry lookup only.
5. Command registration prototype after constructor-tail insertion proof.
6. Reload/resource events as event-only or documentation until async/post-swap proof.
7. Networking phase events only if later selected.
8. Data generation/assets deferred as future API design.

## First-Wave Boundaries

- Ticking can start with server tick and level tick HEAD/RETURN events.
- Lifecycle must separate post-load, dedicated-started, first-tick/ready-observed, reload, stopping, and stopped semantics.
- World/level access is guarded lookup/enumeration only.
- Registries are lookup-only.
- Commands are not implementation-ready until exact constructor-tail insertion is proven.
- Resources/datapacks stay event-only or documentation until async completion and post-swap micro-order are proven.
- Networking remains phase-event-only if included; raw packet APIs are rejected for first wave.
- Data generation/assets are deferred and should remain a future API design/documentation bridge.

## Global Non-Claims

Dedicated runtime evidence is dedicated-server-only, integrated server parity is not confirmed, and Java mod execution is not sandboxed.
