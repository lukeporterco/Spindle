# SteelHook Primitive Roadmap

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

SteelHook remains internal Spindle machinery, not a public arbitrary bytecode API. This roadmap maps Minecraft concept needs to SteelHook capability planning without implementing primitives.

## Current Sufficient Or Nearly Sufficient

- method-entry static dispatch: server tick, level tick, shutdown start, networking phase entry if later selected.
- method-exit static dispatch: server tick, level tick, post-load lifecycle, command/lifecycle observations.
- return-value intercept: narrow observation or binding where returned values are sufficient and ownership is clear.
- invoke redirect: possible internal implementation tool for specific call boundaries, subject to separate SteelHook design.
- invoke wrap: possible internal implementation tool for future contribution-style APIs, subject to separate policy.
- direct lookup binding where no hook is needed: registry lookup and world/level enumeration after readiness.

## Still Needed Or Not Implementation-Proven

- constructor-tail for command registration.
- async continuation for reload/resource completion.
- post-swap reload micro-order proof.
- method-around/wrap policy for contribution-style APIs.
- integrated-server lifecycle parity probe.

## Rejected For First Wave

- raw packet interception.
- registry mutation primitives.
- storage/persistence hooks.
- datapack listener contribution without async/listener proof.

## Concept Mapping

| Concept | SteelHook implication |
|---|---|
| Ticking | method-entry static dispatch and method-exit static dispatch are enough for first-wave events. |
| Server lifecycle | method-exit after load boundaries and method-entry/exit around dedicated observations; integrated-server lifecycle parity probe remains required. |
| World and level | direct lookup binding after readiness; no storage/persistence hooks. |
| Registries | direct lookup binding and lookup-only wrappers; registry mutation primitives are rejected for first wave. |
| Commands | constructor-tail is the main required proof; async continuation may be needed if reload replay is selected. |
| Resources and datapacks | async continuation and post-swap reload micro-order proof are required before implementation beyond event-only observation. |
| Networking | phase events could use method-entry/exit if later selected; raw packet interception is rejected. |
| Data generation and assets | no runtime hook requirement for deferred offline/static generation design. |

## Boundary

This roadmap does not add SteelHook primitives. It also does not imply Java mod execution is sandboxed; it is not sandboxed.
