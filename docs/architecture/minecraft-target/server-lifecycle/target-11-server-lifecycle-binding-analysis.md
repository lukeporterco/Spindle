# Target-11: Server Lifecycle Binding Analysis

This is an analysis-only lifecycle binding pass document for the Minecraft Target Layer. It records what Target-11 adds or decides while preserving the current Minecraft, SteelHook, and public API boundaries.

## Inputs

- Minecraft Target Concept Roadmap.
- Known server main entrypoint contract from SteelHook 0.1 grounding.

## Output

- Deterministic server lifecycle binding analysis report.
- `minecraft-server-lifecycle-bindings.json`.

## Capability Added Or Recorded

- Binds only `minecraft.server.lifecycle.starting` to the known server entrypoint contract.
- Leaves later lifecycle phases declared but unbound.

### Preserved Source Notes

Target-11 is the first real grounding pass for the Minecraft Target Layer concept roadmap.

This pass is analysis-only.

It reads the `minecraft.concept.server_lifecycle` concept and binds only one currently supportable lifecycle phase to existing repository machinery.

### Bound Phase In This Pass

Only `minecraft.server.lifecycle.starting` is bound in this pass.

That binding is to the existing Target-3 Minecraft `26.1.2` dedicated server main entrypoint contract:

- `minecraft.26_1_2.server.main.entrypoint`
- owner: `net/minecraft/server/Main`
- member: `main`
- descriptor: `([Ljava/lang/String;)V`

In this pass, `minecraft.server.lifecycle.starting` means Spindle can identify the dedicated server main entrypoint as a startup boundary. It does not mean the server is ready, accepting players, loaded worlds, or fully initialized.

### Declared But Unbound

The following phases are declared but unbound:

- `minecraft.server.lifecycle.started`
- `minecraft.server.lifecycle.stopping`
- `minecraft.server.lifecycle.stopped`
- `minecraft.server.lifecycle.crashed`
- `minecraft.server.lifecycle.reload_requested`

### Output

Target-11 writes one deterministic report:

- `minecraft-server-lifecycle-bindings.json`

### What This Pass Does Not Add

No runtime lifecycle callback exists yet.
No public Modding API exists yet.
No new SteelHook primitive is added.
Java mod execution is not sandboxed.

This pass does not add:

- runtime lifecycle callbacks
- lifecycle dispatch
- `SteelHookDispatcher` integration
- `MinecraftHookRuntimeBridge` calls
- method-exit hooks
- server started, stopping, stopped, crashed, or reload runtime detection
- Target-4 through Target-10 behavior changes
- real Minecraft runtime transformation
- `StackMapTable` rewriting
- `com.spindle.api.minecraft.*`
- command registration
- data generation tooling
- registry/content registration
- tick scheduling
- networking
- client support

## Boundaries Preserved

- Does not add runtime callbacks, listener APIs, command registration, registry/content registration, gameplay hooks, or sandboxing.

## Follow-On Direction

- Target-12 can plan symbolic dispatch above the coarse lifecycle anchor.
