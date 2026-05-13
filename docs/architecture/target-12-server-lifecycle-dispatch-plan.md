# Target-12: Server Lifecycle Dispatch Plan

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
