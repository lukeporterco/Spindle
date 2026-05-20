# Resources And Datapacks Implementation Packet

Minecraft version: 26.1.2
Status: `event-only-or-documentation`

## Purpose

Keep resources/datapacks as reload observation planning and documentation until async/post-swap proof exists.

## Source Evidence To Inspect

- `internals-analysis/concepts/resources-and-datapacks.md`
- `internals-analysis/candidates/resource-reload-candidates.md`
- `internals-analysis/evidence/resources-and-datapacks-evidence.md`
- `internals-analysis/evidence/resource-reload-confirmation-evidence.md`
- `internals-analysis/spindle-translation/resources-and-datapacks-spindle-implications.md`

## Runtime Evidence To Inspect

- `internals-analysis/notes/runtime-probe-dedicated-2026-05-19.jsonl`
- `internals-analysis/notes/runtime-probe-results.md`
- `internals-analysis/notes/runtime-probe-log.md`
- `internals-analysis/matrices/runtime-confirmation-status.md`

## Confirmed Touchpoints

- `MinecraftServer.reloadResources(...)` HEAD, RETURN, and returned future completion.
- `ReloadableServerResources.loadResources(...)` HEAD, RETURN, and future completion.
- `ReloadableServerResources` and `Commands` reconstruction during startup and reload.

## Target Layer Meaning

Allow reload start/complete observation planning. Keep datapack and resource contribution out of first wave.

## SteelHook Requirements

async continuation for reload/resource completion. post-swap reload micro-order proof. method-around/wrap policy for contribution-style APIs only in a future design.

## First Implementation Shape

Event-only or documentation. A future event could observe reload start and returned future completion after async continuation support is proven.

## Tests Future Implementation Should Require

- Async completion callback ordering test.
- Post-swap `this.resources` assignment and downstream update order probe.
- Negative API surface test excluding datapack contribution/listener composition.

## Documentation Requirements For Future Implementation

Document that reload post-swap micro-order remains not fully proven because exact `this.resources` assignment was not instrumented.

## Non-Goals

- Datapack contribution.
- Listener composition.
- Generated asset APIs.
- Immediate post-swap safety claims.
- Java sandbox promises; Java mod execution is not sandboxed.

## Risks

Returned future completion is useful but does not prove all contribution-style APIs are safe. Treating reload observation as contribution safety would overstate the evidence.

## Open Questions

- What is the exact order around `this.resources` assignment and post-swap updates?
- Which datapack-backed systems, if any, deserve separate Target concepts later?
