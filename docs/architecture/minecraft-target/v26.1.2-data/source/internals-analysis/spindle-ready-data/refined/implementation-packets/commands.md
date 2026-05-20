# Commands Implementation Packet

Minecraft version: 26.1.2
Status: `ready-for-steelhook-primitive-planning`

## Purpose

Plan command registration support only after SteelHook proves the exact insertion point.

## Source Evidence To Inspect

- `internals-analysis/concepts/commands.md`
- `internals-analysis/candidates/command-candidates.md`
- `internals-analysis/evidence/commands-evidence.md`
- `internals-analysis/evidence/commands-confirmation-evidence.md`
- `internals-analysis/spindle-translation/commands-spindle-implications.md`

## Runtime Evidence To Inspect

- `internals-analysis/notes/runtime-probe-results.md`
- `internals-analysis/matrices/runtime-confirmation-status.md`

## Confirmed Touchpoints

- `Commands` constructor HEAD/RETURN.
- `Commands.getDispatcher()` HEAD/RETURN.
- `ReloadableServerResources` constructor and `loadResources(...)`.
- Startup and reload observed `Commands` construction before function-library dispatcher capture.

## Target Layer Meaning

Command registration is plausible but not fully ready. A prototype may follow only after constructor-tail insertion proof.

## SteelHook Requirements

constructor-tail for command registration. Async continuation may be required if command replay happens during reload. method-around/wrap policy may be needed if constructor-tail is insufficient.

## First Implementation Shape

Primitive proof first, then a limited command registration prototype. Do not declare command registration fully ready until exact hook insertion is proven.

## Tests Future Implementation Should Require

- Constructor-tail insertion proof test showing contributed commands are visible before function-library dispatcher capture.
- Reload replay decision test if commands should re-register during reload.
- Player command sync behavior test before public API release.
- Negative tests showing accessor/sync hooks are not registration semantics.

## Documentation Requirements For Future Implementation

Document constructor-tail proof, reload replay behavior, and dedicated-server-only runtime evidence. State that integrated server parity is not confirmed.

## Non-Goals

- Fully ready command registration in this pass.
- Late mutation through `MinecraftServer.getCommands()`.
- `Commands.sendCommands(...)` as registration.
- Reload replay guarantee before design approval.
- Java sandbox promises; Java mod execution is not sandboxed.

## Risks

The observed order makes constructor-tail plausible, but not proven as an implementation insertion point. Reload may require replay semantics and async coordination.

## Open Questions

- Can constructor-tail safely insert before function compilation and player sync?
- Should command registration replay during reload?
