# Target Layer API Boundary

This pass defines the next Minecraft-facing boundary before any injection hook implementation lands.

Target-1 now adds artifact interpretation before hook contracts. That pass remains analysis-only and stays inside `target-minecraft`.

Target-2 now adds explicit hook point contract validation on top of Target-1 interpretation. That pass also remains analysis-only and stays inside `target-minecraft`.

Target-3 now selects the first internal known-symbol catalog and validates it non-invasively against Target-1 interpretation. That pass also remains analysis-only and stays inside `target-minecraft`.

Target-5 now adds an internal hook placement analysis scaffold on top of Target-3 plus the existing runtime and execution planning path. That pass reads one selected method `Code` attribute as opaque bytecode, fingerprints that method body, and writes one deterministic method-entry placement candidate without modifying Minecraft.

Target-6 now adds an internal instruction-aware bytecode model on top of Target-5. That pass decodes the selected method `Code` bytes into a deterministic instruction stream, validates instruction, branch, switch, and exception-table boundaries, preserves nested `Code` attribute metadata, and writes one deterministic bytecode analysis report without modifying Minecraft.

Target-7 now adds an internal injection patch-planning dry-run on top of Target-6. That pass plans exactly one internal method-entry static-dispatch `invokestatic` insertion for `net.minecraft.server.Main.main(String[])`, records symbolic constant-pool requirements and rewrite impacts, and writes one deterministic patch-plan report without modifying Minecraft.

Target-8 now adds the first internal transformed-class proof on top of Target-7. That pass applies the single planned `invokestatic` patch only to fixture class bytes in tests, returns deterministic transformed class bytes plus transformation metadata, and still does not touch real Minecraft runtime artifacts or bootstrap wiring.

Target-9 now wires that single validated SteelHook transform into bootstrap classloading for fake-server execution only. That pass transforms exactly `net.minecraft.server.Main` inside the bootstrap runtime classloader, records dispatcher invocation, writes a deterministic bootstrap transformation result, and still does not transform real Minecraft runtime artifacts, rewrite `StackMapTable`, use Java agents or Mixin, expose public APIs, add gameplay hooks, or imply Java mod execution is sandboxed.

Target-10 now hardens that chain with one explicit SteelHook 0.1 completion check. That pass does not add new hook kinds or new mutation modes. It reuses the existing fake-server transformation path, verifies the report chain from Target-3 through Target-9, writes `minecraft-steelhook-0.1-report.json`, and proves the current spine is deterministic, bounded, fake-server-only, and not a public API.

Target-11 now adds the first concept-grounding pass above that chain. It reads the Target Layer concept catalog plus Target-3 hook contract validation, writes `minecraft-server-lifecycle-bindings.json`, and binds only `minecraft.server.lifecycle.starting` to the existing Minecraft `26.1.2` dedicated server main entrypoint contract. It remains analysis-only and does not add runtime lifecycle callbacks, public APIs, new SteelHook primitives, real runtime transformation support, or sandboxing.

Target-12 now adds the next concept-grounding pass above Target-11. It reads the Target-11 binding report, writes `minecraft-server-lifecycle-dispatch-plan.json`, plans exactly one symbolic internal static dispatch target for `minecraft.server.lifecycle.starting`, and leaves the other five lifecycle phases declared unsupported for dispatch. It remains analysis-only and does not implement or call a dispatcher, add public listener registration, add mod callback execution, add new SteelHook primitives, add real runtime transformation support, or add sandboxing.

Target-13 now adds the second concept-grounding pass above Target-12. It reads the Target-12 lifecycle dispatch plan, writes `minecraft-command-registration-analysis.json`, treats the symbolic `minecraft.server.lifecycle.starting` dispatch as the only current upstream anchor, and leaves future command dispatcher discovery, registration timing, apply timing, and reload reapplication boundaries declared but unbound. It remains analysis-only and does not bind a Minecraft command dispatcher symbol, add Brigadier adapters, perform command registration, execute commands, expose a public command API or Modding API, add runtime callbacks, add new SteelHook primitives, add real runtime transformation support, or add sandboxing.

Target-14 now adds the next concept-grounding pass above Target-13. It reads the Target-1 artifact interpretation plus the Target-13 command registration analysis, writes `minecraft-command-dispatcher-symbol-analysis.json`, scans only interpreted metadata for Brigadier `CommandDispatcher` descriptor references, and may declare a future minimal command registration proof eligible only when exactly one selectable target is discovered. It remains analysis-only and does not register commands, execute commands, read or mutate a command tree, add Brigadier dependencies, add hook contracts for command classes, expose public command APIs or a public Modding API, add runtime callbacks, add new SteelHook primitives, add real runtime transformation support, or imply sandboxing.

Target-15 now adds the next concept-grounding pass above Target-14. It reads the Target-14 command dispatcher symbol analysis, writes `minecraft-command-dispatcher-binding-analysis.json`, and classifies what future binding or access strategy a selected dispatcher candidate would require. It remains analysis-only and makes explicit that SteelHook 0.1 method-entry dispatch does not provide live dispatcher value access. It does not register commands, add Brigadier dependencies, expose public command APIs or a public Modding API, add runtime callbacks, add new SteelHook primitives, add real runtime transformation support, or imply sandboxing.

Target-16 now adds the first concept-grounding pass for data, resources, reload, and future data generation. It reads only the Target-12 lifecycle dispatch plan, writes `minecraft-resource-reload-analysis.json`, treats the symbolic `minecraft.server.lifecycle.starting` dispatch as a coarse lifecycle anchor only, and leaves reload discovery, reload timing, reload apply timing, datapack visibility, resource-manager visibility, and future offline data generation declared unbound. It remains analysis-only and does not discover Minecraft reload symbols, access resources, access datapacks, generate data, mutate registries, expose public APIs, add new SteelHook primitives, add runtime transformation support, or imply sandboxing.

Target-17 now adds the next concept-grounding pass for data, resources, reload, and future data generation. It reads Target-1 artifact interpretation plus Target-16 resource/reload analysis, writes `minecraft-resource-reload-symbol-analysis.json`, scans only interpreted class/package names plus field and method names/descriptors for fixed resource/reload discovery tokens, and reports candidate metadata symbols only. It remains analysis-only and does not select a stable reload target, bind reload timing or apply behavior, access resources or datapacks, generate data, mutate registries, expose public APIs, add new SteelHook primitives, add runtime transformation support, or imply sandboxing.

Target-18 now adds the next concept-grounding pass for data, resources, reload, and future data generation. It reads Target-17 resource/reload symbol analysis, writes `minecraft-resource-reload-binding-analysis.json`, and classifies binding/access requirements only for discovered candidates. It remains analysis-only, does not select a stable reload target, does not make reload implementation ready, does not bind reload timing or apply behavior, does not access resources or datapacks, does not generate data, does not mutate registries, does not expose public APIs, does not add new SteelHook primitives, and does not imply sandboxing. Target-19 may separate runtime resource visibility from future offline data generation design.

Target-19 now adds the next concept-grounding pass for data, resources, reload, and future data generation. It reads Target-16 resource/reload boundary analysis plus Target-18 binding analysis, writes `minecraft-resource-visibility-generation-analysis.json`, and separates runtime reload timing, runtime resource visibility, and future offline data generation lanes only. It remains analysis-only, does not inspect Target-1 metadata, does not discover new symbols, does not select a stable reload target, does not bind reload timing or apply behavior, does not access resources or datapacks, does not generate data, does not write generated files, does not mutate registries, does not expose public APIs, does not add new SteelHook primitives, and does not imply sandboxing. Runtime resource visibility is not an API yet, and offline data generation is not implemented yet.

## Injection Hook Subsystem

The Injection Hook Subsystem is the low-level subsystem inside the Minecraft Target Layer.

It communicates with Minecraft internals through hook points, mapped symbols, classloading boundaries, and deterministic diagnostics.

It is not a standalone public API and it is not the ergonomic modding surface.

The first concrete internal contract in that subsystem is now the Target-2 hook point contract model under `com.spindle.core.minecraft.hook`.

That model progresses through:

1. Target-1 reads class-file structure from planned Minecraft runtime jars.
2. Target-2 defines validation-only hook contracts.
3. Target-3 selects the first internal known-symbol catalog and validates it non-invasively.
4. Target-4 installs one minimal launch-boundary wrapper around `net.minecraft.server.Main.main(String[])`.
5. Target-5 reads one selected method `Code` attribute as opaque bytes and plans one method-entry placement candidate.
6. Target-6 decodes that selected method into an internal instruction model and validates boundary metadata.
7. Target-7 plans one internal method-entry static-dispatch patch candidate without rewriting class bytes.
8. Target-8 rewrites fixture-only class bytes for one internal method-entry proof and rejects `StackMapTable`.
9. Target-9 applies that same proof inside fake-server bootstrap classloading for one exact class target.
10. Target-10 verifies the full fake-server-only SteelHook 0.1 chain.
11. Future passes may expand runtime-safe bytecode rewriting and broader hook families.

SteelHook 0.1 now means the internal chain is proven through completion verification:

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

It still does not transform real Minecraft runtime artifacts, rewrite `StackMapTable`, install hooks in production outside Target-4, expose a public API, add gameplay hooks, use Mixin or Java agents, or imply Java mod execution is sandboxed.

## Target Layer API

The Target Layer API is the readable but low-abstraction Minecraft-facing substrate.

It should expose target facts and target operations, not developer-friendly modding workflows.

Advanced developers may eventually use it as an escape hatch when they need direct target behavior.

The ordered concept vocabulary for that future layer now lives in [Minecraft Target Concept Roadmap](minecraft-target-concept-roadmap.md). Target-11 now adds the first analysis-only grounding report for that vocabulary by binding only `minecraft.server.lifecycle.starting` to the known Minecraft `26.1.2` server entrypoint contract. Target-12 then adds an analysis-only symbolic dispatch plan for that same starting phase without implementing runtime callbacks or public listeners. The boundary still does not add runtime callbacks, public APIs, real Minecraft runtime transformation, `StackMapTable` rewriting, command registration, registry/content registration, data generation tooling, networking support, client support, or sandboxing.

Target-13 then adds an analysis-only command registration concept report above that symbolic lifecycle anchor. It names the future boundaries around dispatcher discovery, registration timing, application timing, and reload reapplication while keeping every command-facing boundary unbound except for the upstream lifecycle anchor itself. The boundary still does not bind a Minecraft command dispatcher symbol, add Brigadier integration, register commands, execute commands, read or mutate a command tree, expose public command APIs, expose a public Modding API, add runtime callbacks, add real Minecraft runtime transformation, or imply sandboxing.

Target-15 then narrows the interpretation of Target-14 selection. A unique metadata candidate is still only a selected symbol candidate, not proof of dispatcher access or command registration readiness. The boundary still does not add Brigadier integration, public command APIs, runtime callbacks, new SteelHook primitives, real Minecraft runtime transformation, or sandboxing.

Target-16 then adds the first analysis-only boundary report for resources and reload. It keeps the lifecycle anchor explicitly coarse, separates runtime resource visibility from future offline data generation, and does not add reload handling, datapack access, resource manager access, generated output, registry mutation, public APIs, runtime callbacks, or sandboxing.

Target-17 then adds analysis-only symbol discovery on top of that coarse boundary report. It discovers candidate metadata symbols only, may feed a future Target-18 binding/access strategy analysis, and still does not make reload implementation ready by itself.

## Modding API

The Modding API is the future ergonomic API built on top of the Target Layer API.

It should express developer intent such as events, registries, commands, resources, networking, world helpers, and gameplay abstractions.

It should not be shaped like internal hook plumbing and it should not inherit `.target` naming.

## Naming Direction

Future internal hook implementation should live under `target-minecraft`, likely below `com.spindle.core.minecraft.hook`.

Future low-level target escape hatch API, if exposed publicly, should live under a deferred Minecraft namespace such as `com.spindle.api.minecraft.target.*`.

Future ergonomic modding APIs should not use `.target` names.

SteelHook remains internal machinery here. It is not a public arbitrary bytecode mutation API.

## Boundary Intent

This document is a boundary-prep note only.

It names the first planned Minecraft Target Layer subsystem, the Injection Hook Subsystem, without implementing it.

Target-2 and Target-3 remain analysis-only scaffolding inside that boundary. Target-4 adds one internal launch-boundary installation proof. Target-5 adds one internal method-entry placement analysis scaffold. Target-6 adds one internal instruction-aware decode layer. Target-7 adds one internal dry-run patch-planning layer. Target-8 adds one fixture-only transformed-class proof. Target-9 adds one fake-server-only bootstrap classloading application path. Target-10 verifies that whole narrow chain without crossing into real Minecraft runtime transformation, public hook APIs, gameplay-facing modding surfaces, or sandbox claims.
