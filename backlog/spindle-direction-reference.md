# Spindle Direction Reference

## Purpose

Spindle should become a forward-only, server-first, deterministic Java 25 mod platform for baseline Minecraft `26.1.2`. It should not try to become a Fabric, Forge, NeoForge, or Paper compatibility layer. Its core identity should be a small, deterministic loader that compiles a modpack into a verified runtime contract before any mod class is touched.

The recommended direction is:

> Spindle is a deterministic modpack runtime compiler first, a lifecycle/service/config/resource platform second, and a hook-capable Minecraft mod loader third.

This direction keeps the project ambitious without jumping straight into the riskiest part of Minecraft modding. The loader should become strong outside Minecraft first, so that future Minecraft integration is narrow, explicit, verified, and explainable.

## Core Thesis

The project should stop thinking in terms of an endless sequence of numbered milestones and start thinking in terms of named architectural arcs.

A numbered milestone is useful early, but eventually `Milestone 37` or `Milestone 67` stops communicating anything. The project needs named arcs, readable pass names, stable schema versions, and clear release versions.

The key design principle is that every runtime action should come from a prior contract.

A mod should not be loaded because it happens to be on the classpath. A handler should not run because reflection found it. A resource should not override another resource by accident. A hook should not exist because a mod found an internal class name. The loader should plan, validate, freeze, report, and only then execute.

## Recommended Roadmap Model

### Foundation Arc

Status: mostly complete.

This arc established control. It covered fake-game loading, provider boundaries, frozen graphs, conflict diagnostics, Minecraft metadata dry runs, managed vanilla server launch, verified artifact caching, offline replay, runtime planning, bundled runtime extraction, reproducibility reports, preflight reports, and the Milestone 8 bootstrap gate.

The Foundation Arc proved that the loader can own planning, reports, cache behavior, lockfiles, runtime provenance, and bootstrap execution without modifying Minecraft classes.

### Runtime Arc

Status: Runtime-1 implemented; further Runtime passes remain.

This arc turns Spindle into a deterministic modpack runtime compiler.

The loader now compiles mod metadata into a reusable runtime profile before standard runtime execution. The current profile includes dependency graph inputs, classpath ownership, lifecycle plans, owned storage directories, package policy summaries, quality summaries, and stable fingerprints. Service registries, config schemas, and resource overlay planning remain future Runtime or Platform work.

Completed first pass:

`Runtime-1: Compiled Runtime Kernel`

Delivered:

Spindle compiles a deterministic runtime contract and uses it for standard-runtime lifecycle execution, mod context creation, storage ownership, package policy reporting, lifecycle/quality reports, and deterministic profile cache reuse while preserving schema 1 and Milestone 8 behavior.

Included:

- `loader.mod.json` schema v2
- `spindle.profile.json` schema v2
- deterministic lifecycle declarations
- deterministic lifecycle execution ordering
- generated or precomputed mod context objects
- loader-owned config, data, cache, and generated-output directories
- classloader and package ownership validation
- startup profiling categories
- profile cache reuse when inputs match
- clear invalidation when inputs change
- readable lifecycle, quality, execution, and reproducibility reports

Excluded:

- real Minecraft hooks
- bytecode injection
- client launch
- rendering systems
- optimization modules unrelated to startup/runtime planning
- gameplay APIs
- remapping
- Mixin compatibility
- broad compatibility with existing mod ecosystems

### Platform Arc

Status: should follow the first Runtime Arc passes.

This arc should make Spindle useful and ergonomic as its own ecosystem.

Key systems:

- deterministic service registry
- service provider and consumer contracts
- generated config schema system
- resource ownership and overlay planner
- modpack quality scoring
- developer diagnostics
- clearer Gradle tasks
- sample mods
- devtools and templates

This arc should make a modpack explainable. A user or developer should be able to see which mods provide services, which mods consume services, which resources conflict, which packages are forbidden, which lifecycle handlers are slow, and which permissions increase risk.

### SteelHook Arc

Status: future, after Runtime and Platform foundations.

SteelHook should not be custom Mixin. It should be a loader-owned hook contract system.

Its first goal should not be aggressive Minecraft patching. Its first goal should be proving the hook registry, hook declarations, permission model, generated dispatch tables, deterministic ordering, conflict policy, hot-path budgets, and reports.

Suggested passes:

- `SteelHook-0: Hook Registry`
- `SteelHook-1: Fixture Dispatch`
- `SteelHook-2: Permissioned Hook Runtime`

The first SteelHook work should use fixture-only or fake-server injection targets. Real Minecraft bytecode should wait until the contract system is boring and stable.

### Minecraft Surface Arc

Status: future, after SteelHook foundation.

This is where the loader starts touching Minecraft in narrow, version-pinned, well-verified ways.

Suggested first pass:

`Minecraft-1: First Server Hook`

The first real Minecraft hook should be low-risk, server-only, version-pinned to baseline `26.1.2`, and backed by strict verification. It should produce a report showing the target class, target method, expected fingerprint, hook ID, dispatch table, permission class, and failure behavior.

It should fail closed.

This arc should avoid gameplay, worldgen, entity ticking, rendering, broad patching, and hot-path hooks until the loader has a strong hook risk model and profiling system.

### Optimization Arc

Status: future, except for startup and planning optimizations.

Optimization should not mean adding random performance patches. It should mean loader-owned optimization systems that are explainable, measurable, and compatible with the runtime contract.

Allowed now:

- compiled profile caching
- static metadata compilation
- deterministic dispatch arrays
- startup profiling
- classpath planning
- resource indexing
- service table generation

Deferred:

- gameplay optimization modules
- rendering optimization
- tick optimization
- worldgen optimization
- memory optimization
- invasive server patches

## Naming and Versioning

The project should use three separate naming/versioning layers.

### 1. Arc and Pass Names

Pass names describe work. They are not release versions.

Format:

`ArcName-N: Human Readable Pass Name`

Examples:

- `Runtime-1: Compiled Modpack Runtime`
- `Runtime-2: Lifecycle Kernel`
- `Runtime-3: Mod Context and Owned Storage`
- `Platform-1: Services and Config`
- `Platform-2: Resource Overlay Planner`
- `Platform-3: Developer Kit`
- `SteelHook-0: Hook Registry`
- `SteelHook-1: Fixture Dispatch`
- `SteelHook-2: Permissioned Hook Runtime`
- `Minecraft-1: First Server Hook`

This is more readable than endlessly increasing milestone numbers. It also makes clear whether a pass is architectural, developer-facing, experimental, or Minecraft-facing.

### 2. Loader Release Versions

Loader releases should use ordinary semantic-style versions.

Examples:

- `0.1.0`: first usable bootstrap/runtime prototype
- `0.2.0`: compiled profiles and lifecycle support
- `0.3.0`: services, config, and resource planning
- `0.4.0`: SteelHook registry foundation
- `0.5.0`: first narrow Minecraft server hook

Release versions describe what users receive.

### 3. Schema Versions

Schema versions should be explicit and separate from loader releases.

Examples:

- `loader.mod.json schema v1`
- `loader.mod.json schema v2`
- `compiled-profile schema v1`
- `service-registry schema v1`
- `config-schema schema v1`
- `resource-overlay schema v1`
- `hook-registry schema v1`

A loader release may support multiple schema versions. Schema compatibility should be obvious and deterministic.

## Why the Non-Minecraft Foundation Comes First

The loader should become excellent outside Minecraft before it touches Minecraft internals.

Before injection or real hooks, the loader should already know:

- which mods exist
- which packages each mod owns
- which dependencies and conflicts exist
- which lifecycle phases each mod participates in
- which services each mod provides
- which services each mod consumes
- which config schemas each mod declares
- which resources each mod owns, overlays, appends, or conflicts with
- which directories each mod can write to
- which permissions each mod requested
- which handlers run in what order
- which startup costs were measured
- which risks exist in the modpack
- whether the compiled runtime profile matches the lockfile and inputs

Once this exists, Minecraft integration becomes much safer.

Without this foundation, a custom injector risks becoming a different name for the same chaos: arbitrary target classes, fragile patches, hidden ordering behavior, runtime surprises, and unclear errors.

With this foundation, a future Minecraft hook becomes a controlled extension of an existing verified contract:

> This verified modpack profile is allowed to attach these declared handlers to these official hook IDs under these permissions for this Minecraft version.

That is the difference between a controlled platform and a pile of patches.

## Developer Ergonomics

Developer ease of use should be a core design rule, not polish added later.

The loader can be deterministic, secure, and fast while still being miserable to develop for. That would hurt the ecosystem before it starts.

The rule should be:

> Every system should have a simple common path, a strict advanced path, and clear diagnostics when something goes wrong.

### Metadata Ergonomics

A simple mod should not require deep loader knowledge.

A basic `loader.mod.json` should let a developer declare:

- mod id
- mod version
- target side
- dependencies
- lifecycle handlers
- config schema
- provided services
- consumed services
- permissions

The loader should validate and explain the rest.

### API Naming

Public API names should be obvious and boring.

Good public-facing names:

- `ModContext`
- `LifecyclePhase`
- `LifecycleHandler`
- `ConfigView`
- `ConfigSchema`
- `ServiceRegistry`
- `ServiceKey`
- `ResourcePlan`
- `HookId`
- `HookPermission`
- `StartupProfile`
- `DiagnosticSink`
- `ModDataDirectory`

Avoid exposing overly internal names to normal mod developers. Names like `BootstrapNodeResolver`, `RuntimeGraphEnvelope`, or `ExecutionPlanMaterializer` may be acceptable internally, but they should not be part of the simple developer path.

### Lifecycle Naming

Lifecycle phase names should describe intent, not implementation details.

Suggested initial phases:

- `BOOTSTRAP`
- `CONFIGURE`
- `PRE_SERVER_MAIN`
- `SERVER_STARTING`
- `SERVER_READY`
- `SERVER_STOPPING`

Avoid names like `CLASSLOAD_PREPARE_2`, `INTERNAL_HANDOFF`, or anything that forces a mod developer to understand loader internals.

### Error Messages

Diagnostics should explain the cause, location, and fix.

Bad:

> Invalid mod.

Better:

> Mod `example_tools` declares lifecycle handler `com.example.Tools::start`, but the method is not public static and does not accept `ModContext`.

Bad:

> Service error.

Better:

> Mod `example_worldgen` requires service `com.example.api.WorldgenService`, but no enabled mod provides that service. Add a provider mod or mark the service dependency as optional.

Bad:

> Package violation.

Better:

> Mod `example_core` contains class `net.minecraft.server.ExamplePatch`, but package `net.minecraft` is protected. Mods may not define Minecraft packages.

Bad:

> Config failed.

Better:

> Config key `maxMachines` expected an integer in range `1..64`, but found string value `"fast"`.

### Gradle Task Naming

Gradle tasks should be grouped around developer intent.

Suggested names:

- `spindleValidateMod`
- `spindleCompileProfile`
- `spindleRunServer`
- `spindleRunServerOffline`
- `spindlePrintProfile`
- `spindleExplainModpack`
- `spindleCheckReproducibility`
- `spindleGenerateModTemplate`

Avoid task names that expose internal implementation details unless they are specifically for maintainers.

### Generated File Naming

Generated files should have consistent names and clear identity.

Suggested names:

- `loader.mod.json`
- `spindle.lock.json`
- `spindle.profile.json`
- `spindle.lifecycle-report.json`
- `spindle.quality-report.json`
- `spindle.execution-report.json`
- `spindle.reproducibility-report.json`
- `spindle.preflight-report.json`

The `spindle.` prefix makes generated loader artifacts easy to identify in build output and reports.

### Public Package Naming

Public API packages should be stable and predictable.

Suggested structure:

- `com.spindle.api`
- `com.spindle.api.lifecycle`
- `com.spindle.api.config`
- `com.spindle.api.service`
- `com.spindle.api.resource`
- `com.spindle.api.hook`
- `com.spindle.api.diagnostic`

Internal packages can be more specific and less ergonomic, but public packages should feel stable.

## Custom Systems Worth Prioritizing

The following custom systems fit the project direction and are realistic for one person plus AI assistance.

### Static Mod Metadata Compiler

Compile `loader.mod.json`, dependencies, lifecycle declarations, services, config schemas, resource declarations, and permissions into a compact validated profile.

Benefit:

- faster startup
- fewer runtime surprises
- better diagnostics
- natural extension of existing lockfile work

### Compiled Modpack Profile Cache

Store a fingerprinted runtime profile that can be reused when inputs match.

Benefit:

- faster repeated launches
- deterministic invalidation
- strong reproducibility story

### Loader-Owned Lifecycle Phases

Let mods declare lifecycle participation through metadata.

Benefit:

- useful structure before Minecraft hooks
- deterministic execution
- clear diagnostics
- safe mod execution model

### Deterministic Service Registry

Mods provide and consume services declaratively.

Benefit:

- no classpath scanning
- clear inter-mod contracts
- better startup
- easier diagnostics

### Generated Config Schema System

Mods declare config schema, and the loader validates values, writes defaults, and provides typed access.

Benefit:

- fewer runtime crashes
- easier mod development
- better user-facing errors

### Package Sealing and Classloader Firewall

Each mod package should belong to one mod. Protected packages should be rejected. Cross-mod package collisions should be fatal unless explicitly allowed.

Benefit:

- fewer classpath bugs
- better isolation
- stronger security posture

### Resource Index and Overlay Planner

Resources should be planned before runtime, with explicit replace, layer, append, or conflict behavior.

Benefit:

- fewer hidden resource conflicts
- faster lookup
- better modpack diagnostics

### Startup Profiling

Track planning time, verification time, classloader creation time, lifecycle handler time, dispatch build time, and Minecraft handoff time.

Benefit:

- performance visibility
- easier regression detection
- clearer quality reports

### Conflict Budget and Quality Scoring

Generate deterministic quality reports covering duplicate resources, split packages, slow handlers, broad permissions, protected package attempts, conflicting services, unstable ordering risks, and overloaded lifecycle phases.

Benefit:

- makes bad modpacks understandable
- gives the loader a polished identity
- supports strict mode

### Versioned Hook Registry

The loader owns official hook IDs and their supported Minecraft versions.

Benefit:

- essential foundation for SteelHook
- avoids mods depending on raw internals
- makes hook behavior explainable

## Recommended Next Pass

Name:

`Runtime-2: Services, Config, and Resource Overlay Planning`

Goal:

Extend the Runtime-1 compiled profile with service registry plans, config schema validation, and resource overlay planning without expanding into Minecraft hooks or compatibility-layer behavior.

Primary deliverables:

- service declaration and consumption model
- config schema declaration and validation
- resource ownership and overlay planning
- compiled service/resource/config sections in `spindle.profile.json`
- stronger quality findings around unsupported permissions and service/resource conflicts
- focused tests for deterministic service/config/resource compilation

Non-goals:

- no injection
- no SteelHook runtime yet
- no real Minecraft hooks
- no client launch
- no rendering
- no gameplay API
- no remapping
- no Mixin compatibility
- no broad existing-mod compatibility

Acceptance criteria:

1. A sample server mod declares lifecycle handlers in metadata.
2. The loader compiles a deterministic runtime profile before classloading the mod.
3. A second run reuses the compiled profile when inputs match.
4. A changed mod jar invalidates the compiled profile.
5. Lifecycle handlers execute in deterministic order.
6. A generated or precomputed `ModContext` is passed to handlers.
7. Each mod receives loader-owned config, data, cache, and generated-output directories.
8. Package collisions and protected package definitions are rejected before classloading.
9. Invalid lifecycle declarations are rejected with clear diagnostics.
10. Reports show what ran, why it ran, in what order, under what permissions, and what profile authorized it.

## Strategic Summary

The project should not expand broadly into Minecraft yet.

It should not build SteelHook immediately.

It should not continue endlessly hardening old milestones.

It should build the platform layer that makes future injection controlled rather than desperate.

The strategic path is:

1. Foundation proved control.
2. Runtime turns control into a reusable platform.
3. Platform makes that system useful and ergonomic.
4. SteelHook becomes safe because the surrounding contract exists.
5. Minecraft integration becomes gradual instead of chaotic.

That is the path most likely to produce a stable, ambitious, real, and meaningful mod loader.
