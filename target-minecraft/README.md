# Target Minecraft

`target-minecraft` is the current partial Minecraft Target Layer.

It owns Minecraft artifact handling, runtime planning, artifact interpretation reports, boundary reports, integration plans, bootstrap execution, and managed server-process helpers. It consumes the completed Spindle Loader subsystem but does not redefine the Spindle ecosystem brand or expose Minecraft behavior through the stable loader API.

## Future Target Layer API boundary

The next boundary-prep arc is documented in [Target Layer API Boundary](../docs/architecture/target-layer-api-boundary.md).

The named concept vocabulary for future Target Layer, SteelHook, and Modding API planning is documented in [Minecraft Target Concept Roadmap](../docs/architecture/minecraft-target-concept-roadmap.md). That roadmap and its matching internal catalog are documentation/model-only in this pass. They do not add runtime hooks, public APIs, real Minecraft runtime transformation, `StackMapTable` rewriting, command registration, registry/content registration, data generation, networking, client support, or sandboxing.

Target-11 now adds the first analysis-only concept grounding report on top of that roadmap. It writes `minecraft-server-lifecycle-bindings.json`, binds only `minecraft.server.lifecycle.starting` to the existing Target-3 Minecraft `26.1.2` dedicated server main entrypoint contract, and leaves `started`, `stopping`, `stopped`, `crashed`, and `reload_requested` declared but unbound. It does not add runtime lifecycle callbacks, public APIs, new SteelHook primitives, real runtime transformation, or sandboxing.

Target-12 now adds the next analysis-only layer above Target-11. It writes `minecraft-server-lifecycle-dispatch-plan.json`, plans exactly one symbolic internal static dispatch for `minecraft.server.lifecycle.starting`, leaves the other five lifecycle phases declared unsupported for dispatch in this pass, does not implement or call a dispatcher, does not expose public listener registration, and does not imply sandboxing.

Target-13 now adds the next analysis-only layer above Target-12. It writes `minecraft-command-registration-analysis.json`, uses the symbolic Target-12 starting lifecycle dispatch as the only current upstream command-registration anchor, declares four future command boundaries unbound, binds no Minecraft command dispatcher symbol, adds no Brigadier adapter, performs no command registration or command execution, reads or mutates no command tree, exposes no public command API or Modding API, adds no runtime callback, and does not imply sandboxing.

Target-14 now adds the next analysis-only layer above Target-13. It writes `minecraft-command-dispatcher-symbol-analysis.json`, scans only Target-1 interpreted metadata for Brigadier `CommandDispatcher` descriptor references, and may declare a future minimal command registration proof eligible only when exactly one selectable non-library symbol target is discovered. It does not register commands, execute commands, read or mutate a command tree, add Brigadier dependencies, add hook contracts for command classes, expose public command APIs or a public Modding API, add runtime callbacks, and does not imply sandboxing.

The first planned subsystem in that arc is the Injection Hook Subsystem, which remains inside `target-minecraft` and is not a standalone public API.

## Target-1 artifact interpretation

Target-1 adds an internal artifact interpretation scaffold before any hook contracts or hook installation work.

It reads planned server-side Minecraft runtime jars as bytecode artifacts, parses class-file structure without class loading, and writes `minecraft-artifact-interpretation.json`.

Target-1 is analysis-only:

- it does not install hooks
- it does not identify hook points
- it does not transform Minecraft
- it does not imply sandboxing

See [Target-1: Minecraft Artifact Interpretation](../docs/architecture/target-1-minecraft-artifact-interpretation.md).

## Target-2 hook contract diagnostics

Target-2 adds an internal hook point contract model and validation scaffold on top of Target-1.

It validates explicit class, method, constructor, and field contracts against interpreted server-side Minecraft symbols and writes `minecraft-hook-contracts.json`.

Target-2 is analysis-only:

- it does not discover hook points automatically
- it does not parse method instructions or inspect callsites
- it does not install hooks
- it does not transform Minecraft
- it does not expose a public API

The default Target-2 contract catalog is intentionally empty in this pass. The report still records schema, target, side, version, analysis-only flags, and diagnostics explaining that no hook contracts are declared yet.

See [Target-2: Hook Point Contract Model](../docs/architecture/target-2-hook-point-contract-model.md).

## Target-3 known-symbol hook validation

Target-3 adds the first internal known-symbol hook contract catalog on top of Target-2.

It selects a tiny catalog by Minecraft version and side, validates those symbols against Target-1 artifact interpretation, and writes `minecraft-hook-contracts.json` schema `2` with catalog metadata.

The only supported catalog in this pass is Minecraft `26.1.2` on the server side, and it contains only `net/minecraft/server/Main` plus `main([Ljava/lang/String;)V`.

Target-3 remains analysis-only and nonfatal:

- it does not parse method bytecode or inspect callsites
- it does not install hooks
- it does not transform Minecraft
- it does not expose a public API
- it does not imply sandboxing

See [Target-3: Non-Invasive Known Minecraft Symbol Hook Validation](../docs/architecture/target-3-known-symbol-hook-validation.md).

## Target-5 hook placement analysis scaffold

Target-5 adds the first internal hook placement analysis layer after Target-4.

It reuses the validated Minecraft `26.1.2` server entrypoint contract, locates `net.minecraft.server.Main.main(String[])`, reads that selected method's `Code` attribute as opaque bytecode, fingerprints the method body, and writes one deterministic `minecraft-hook-placement-plan.json` candidate at bytecode offset `0`.

Target-5 remains analysis-only:

- it does not decode instructions
- it does not inspect callsites
- it does not modify bytecode
- it does not install hooks
- it does not expose a public hook API
- it does not add gameplay hooks
- it does not use Mixin or Java agents
- it does not imply Java mod execution is sandboxed

See [Target-5: Hook Placement Analysis Scaffold](../docs/architecture/target-5-hook-placement-analysis-scaffold.md).

## Target-6 instruction-aware bytecode model

Target-6 adds the first instruction-aware bytecode analysis layer after Target-5.

It reuses the validated Minecraft `26.1.2` server entrypoint placement, decodes the selected `net.minecraft.server.Main.main(String[])` method body into an internal instruction model, validates instruction, branch, switch, and exception-table boundaries, preserves nested `Code` attribute metadata, and writes `minecraft-hook-bytecode-analysis.json`.

Target-6 remains analysis-only:

- it does not modify bytecode
- it does not update stack maps
- it does not compute full control flow
- it does not generate patches
- it does not install hooks
- it does not expose a public hook API
- it does not add gameplay hooks
- it does not use Mixin or Java agents
- it does not imply Java mod execution is sandboxed

See [Target-6: Instruction-Aware Bytecode Model](../docs/architecture/target-6-instruction-aware-bytecode-model.md).

## Target-7 injection patch planning dry-run

Target-7 adds the first internal injection patch-planning layer after Target-6.

It reuses the validated Minecraft `26.1.2` server `net.minecraft.server.Main.main(String[])` Target-5 placement and Target-6 bytecode analysis, plans one internal method-entry static-dispatch `invokestatic`, records symbolic constant-pool requirements plus rewrite impacts, and writes `minecraft-hook-patch-plan.json`.

Target-7 remains planning-only:

- it does not generate transformed class bytes
- it does not rewrite the constant pool
- it does not rewrite `Code`
- it does not update `StackMapTable`
- it does not install hooks
- it does not expose a public hook API
- it does not add gameplay hooks
- it does not use Mixin or Java agents
- it does not imply Java mod execution is sandboxed

See [Target-7: Injection Patch Planning Dry-Run](../docs/architecture/target-7-injection-patch-planning-dry-run.md).

## Target-8 fixture-only bytecode transformation

Target-8 adds the first real transformed-class proof after Target-7.

It reuses the validated Target-7 patch plan, applies exactly one method-entry `invokestatic` dispatcher insertion to fixture `net.minecraft.server.Main.main(String[])` class bytes in tests, appends the required constant-pool entries, updates `Code` length metadata, shifts exception-table offsets by `+3`, and returns deterministic transformation results.

Target-8 remains fixture-only:

- it does not transform real Minecraft runtime artifacts
- it does not wire transformation into bootstrap or runtime classloading
- it does not update `StackMapTable`
- it does not install hooks in production
- it does not expose a public hook API
- it does not add gameplay hooks
- it does not use Mixin or Java agents
- it does not imply Java mod execution is sandboxed

See [Target-8: Fixture-Only Bytecode Transformation](../docs/architecture/target-8-fixture-only-bytecode-transformation.md).

## Target-9 bootstrap class transformation path

Target-9 wires the validated SteelHook method-entry transform into bootstrap classloading for fake-server execution only.

It reuses the Target-7 patch plan plus the Target-8 rewrite proof, passes `minecraft-hook-patch-plan.json` into the bootstrap child JVM, transforms exactly `net.minecraft.server.Main` before definition, invokes `Main.main(String[])`, and writes `minecraft-hook-bootstrap-transformation-result.json`.

Target-9 remains intentionally narrow:

- it requires `--minecraft-bootstrap-transform-hooks --minecraft-bootstrap-fake-server`
- it does not transform real Minecraft runtime artifacts
- it does not rewrite `StackMapTable`
- it does not use Java agents or Mixin
- it does not expose a public hook API
- it does not add gameplay hooks
- it does not imply Java mod execution is sandboxed

See [Target-9: Bootstrap Class Transformation Path](../docs/architecture/target-9-bootstrap-class-transformation-path.md).

## Target-10 SteelHook 0.1 completion verification

Target-10 completes SteelHook 0.1 only in the narrow sense of proving the existing internal hook spine:

```text
known contract
-> method-entry placement
-> instruction-aware bytecode analysis
-> dry-run patch planning
-> fixture transform primitive
-> fake-server bootstrap transformation
-> dispatcher invocation
-> completion verification
```

It adds `--minecraft-steelhook-0-1-check`, `--minecraft-explain-steelhook-0-1-check`, and the deterministic `minecraft-steelhook-0.1-report.json` completion report.

Target-10 does not add new hook kinds, real Minecraft runtime transformation, `StackMapTable` rewriting, public APIs, gameplay hooks, Mixin, Java agents, or sandbox claims.

See [Target-10: SteelHook Hardening Caboose](../docs/architecture/target-10-steelhook-hardening-caboose.md) and [SteelHook 0.1 Capability Boundary](../docs/architecture/steelhook-0.1-capability-boundary.md).

## Target-11 server lifecycle binding analysis

Target-11 is the first Minecraft Target Layer concept grounding pass after the roadmap.

It reads the internal concept catalog plus Target-3 hook contract validation and writes `minecraft-server-lifecycle-bindings.json`.

Target-11 remains analysis-only:

- only `minecraft.server.lifecycle.starting` is bound
- that binding points to `minecraft.26_1_2.server.main.entrypoint`
- `started`, `stopping`, `stopped`, `crashed`, and `reload_requested` remain declared but unbound
- no runtime lifecycle callback exists yet
- no public Modding API exists yet
- no new SteelHook primitive is added
- Java mod execution is not sandboxed

See [Target-11: Server Lifecycle Binding Analysis](../docs/architecture/target-11-server-lifecycle-binding-analysis.md).

## Target-12 server lifecycle dispatch plan

Target-12 is the next Minecraft Target Layer concept grounding pass after Target-11.

It reads the Target-11 binding report and writes `minecraft-server-lifecycle-dispatch-plan.json`.

Target-12 remains analysis-only:

- it plans one symbolic internal static dispatch for `minecraft.server.lifecycle.starting`
- that dispatch is symbolic only and does not implement or call a dispatcher
- it is non-cancellable and cannot replace results
- it does not add public listener registration or mod callback execution
- `started`, `stopping`, `stopped`, `crashed`, and `reload_requested` remain declared unsupported for dispatch
- no runtime lifecycle callback exists yet
- no public Modding API exists yet
- no new SteelHook primitive is added
- Java mod execution is not sandboxed

See [Target-12: Server Lifecycle Dispatch Plan](../docs/architecture/target-12-server-lifecycle-dispatch-plan.md).

## Target-4 minimal hook installation proof

Target-4 adds the first internal launch-boundary hook installation proof.

It plans and installs exactly one wrapper around Minecraft `26.1.2` server `net.minecraft.server.Main.main(String[])`, writes `minecraft-hook-installation-plan.json`, passes that plan into the bootstrap child JVM, invokes Minecraft main through an internal bridge, and writes `minecraft-hook-installation-result.json`.

Target-4 remains intentionally narrow:

- no bytecode parsing or callsite inspection
- no transformation, patching, remapping, or Java agents
- no public hook API
- no gameplay hooks
- no sandbox claim for Java mod execution

See [Target-4: Minimal Launch-Boundary Hook Installation Proof](../docs/architecture/target-4-minimal-hook-installation-proof.md).
