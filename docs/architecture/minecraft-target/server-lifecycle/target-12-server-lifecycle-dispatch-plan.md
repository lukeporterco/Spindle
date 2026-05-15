# Target-12: Server Lifecycle Dispatch Plan

This is an analysis-only symbolic dispatch plan pass document for the Minecraft Target Layer. It records what Target-12 adds or decides while preserving the current Minecraft, SteelHook, and public API boundaries.

## Inputs

- Target-11 lifecycle binding analysis.
- Coarse `minecraft.server.lifecycle.starting` anchor.

## Output

- Deterministic lifecycle dispatch planning report.
- `minecraft-server-lifecycle-bindings.json`.
- `minecraft-server-lifecycle-dispatch-plan.json`.

## Capability Added Or Recorded

- Plans symbolic dispatch for the coarse server starting phase without executing callbacks.

### Preserved Source Notes

Target-12 adds the next analysis-only layer above Target-11.

It reads `minecraft-server-lifecycle-bindings.json` semantics through `MinecraftServerLifecycleBindingReport` and writes one deterministic `minecraft-server-lifecycle-dispatch-plan.json` report.

Target-12 plans exactly one symbolic internal dispatch for `minecraft.server.lifecycle.starting`.

That planned dispatch is symbolic only. It does not implement `MinecraftServerLifecycleDispatcher`, does not call a dispatcher, does not execute runtime lifecycle callbacks, does not expose public listener registration, and does not execute mod callbacks.

The planned starting dispatch is non-cancellable and cannot replace results.

The other five lifecycle phases remain declared unsupported for dispatch in this pass:

- `minecraft.server.lifecycle.started`
- `minecraft.server.lifecycle.stopping`
- `minecraft.server.lifecycle.stopped`
- `minecraft.server.lifecycle.crashed`
- `minecraft.server.lifecycle.reload_requested`

Target-12 does not add a runtime lifecycle callback, a public Modding API, a new SteelHook primitive, real Minecraft runtime transformation, `StackMapTable` rewriting, command registration, data generation, registry/content registration, tick scheduling, networking, or client support.

Java mod execution is still not sandboxed.

## Boundaries Preserved

- Does not implement runtime callback dispatch, public listeners, real Minecraft hooks, or sandboxing.

## Follow-On Direction

- Future lifecycle work can bind additional phases only after concrete Minecraft hook surfaces are discovered.
