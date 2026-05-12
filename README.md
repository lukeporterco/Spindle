# Spindle

Spindle is a forward-only Java 25 Minecraft mod loader project. Its current architecture keeps Minecraft as a target layer, not as the foundation of the loader itself.

The repository contains four main implementation areas:

1. `spindle-loader-api`, the stable runtime-facing Loader API available to mods today.
2. `spindle-loader-core`, the target-neutral loader runtime, planning, diagnostics, security, classloading, and lifecycle implementation.
3. `spindle-loader-cli`, the executable entrypoint and provider wiring.
4. `target-minecraft`, the Minecraft Target Layer, including runtime planning, bootstrap execution, and the internal SteelHook hook subsystem work.

Spindle is not currently a complete Minecraft gameplay API, a Fabric API replacement, a general compatibility layer, or a finished performance mod suite. Those belong to later ecosystem layers built on top of the loader and target foundation.

Minimum Java version: Java 25

Current baseline Minecraft target: 26.1.2

## Current status

The loader spine is complete for the current scope. The Minecraft Target Layer is partially implemented and now includes a narrow internal SteelHook proof chain through fake-server bootstrap class transformation.

```text
Spindle Ecosystem
|
|-- Spindle Loader [DONE]
|   |-- Mod discovery and metadata intake [DONE]
|   |-- Dependency resolution and frozen planning [DONE]
|   |-- Runtime contract system [DONE]
|   |-- Security diagnostics and fatal gates [DONE]
|   |-- Runtime classloading and lifecycle execution [DONE]
|   `-- Runtime-facing Loader API [DONE]
|
|-- Minecraft Target Layer [PART]
|   |-- Minecraft artifact and version planning [PART]
|   |-- Server runtime planning and boundary reports [PART]
|   |-- Bootstrap child-JVM execution [PART]
|   |-- Fake-server bootstrap proof path [PART]
|   |-- SteelHook internal hook subsystem [PART]
|   `-- Target Layer API boundary [DESIGNED]
|
|-- Minecraft Modding API [TODO]
|   |-- Events [TODO]
|   |-- Registries [TODO]
|   |-- Commands [TODO]
|   |-- Networking [TODO]
|   |-- Resources and data [TODO]
|   |-- Client APIs [TODO]
|   `-- World and gameplay abstractions [TODO]
|
|-- Performance Layer [TODO]
|   |-- Renderer, chunk, lighting, entity, tick, networking, and memory work [TODO]
|
`-- Third-Party Mod Ecosystem [PART]
    |-- Runtime-only mods [PART]
    |-- Minecraft-targeted bootstrap fixtures [PART]
    `-- Full Minecraft gameplay/content mods [TODO]
```

Status labels:

```text
[DONE] Implemented for the current intended scope.
[PART] Foundation exists, but the subsystem is not complete.
[DESIGNED] Architecture boundary exists, implementation is still constrained.
[TODO] Not implemented yet.
```

## What Spindle is today

Spindle currently provides a deterministic loader runtime and a growing Minecraft target foundation.

The loader side can:

- discover mod artifacts
- parse and validate `loader.mod.json`
- resolve dependencies and version requirements
- produce frozen mod graphs and lockfiles
- plan classpaths and ownership indexes
- collect trust, risk, quality, and diagnostics reports
- compile deterministic runtime profiles
- enforce runtime contract gates before lifecycle execution
- materialize mod-owned storage and config
- expose the stable runtime-facing Loader API
- execute loader-controlled lifecycle handlers

The Minecraft target side can:

- plan Minecraft launch and runtime artifacts
- inspect and verify server-side runtime files
- produce boundary, integration, preflight, reproducibility, and execution reports
- run fake-server bootstrap smoke flows
- validate known Minecraft hook symbols
- analyze selected method bytecode for hook placement
- plan one internal method-entry patch without modifying classes
- transform fixture class bytes in tests
- apply that transform through bootstrap classloading for fake-server execution only

## What Spindle is not yet

Spindle does not yet provide:

- a full Minecraft gameplay API
- event, registry, command, networking, resource, client, world, block, item, entity, or packet APIs
- a public Minecraft hook API
- a general-purpose bytecode transformer
- arbitrary injection points
- StackMapTable rewriting
- real Minecraft runtime transformation readiness
- Fabric, Forge, NeoForge, Quilt, Paper, Bukkit, Sponge, Mixin, access widener, remapping, or compatibility-layer support
- Java sandboxing for mods

Java mod execution is explicitly not sandboxed:

```text
execution: in-process-unrestricted-java
sandboxed: false
sandboxClaim: not-sandboxed
```

Spindle security currently means validation, deterministic reports, trust classification, warning-only static risk signals, restricted child-JVM static tooling, and fatal gates for declared runtime contracts. It is not a Java security sandbox.

## Architecture split

Spindle is intentionally layered.

```text
Spindle Loader
  Owns target-neutral mod discovery, metadata validation, resolution,
  runtime contracts, diagnostics, classloading, lifecycle execution, and
  the stable runtime-facing Loader API.

Minecraft Target Layer
  Owns Minecraft-specific artifact planning, runtime boundaries, bootstrap
  execution, target diagnostics, and internal SteelHook machinery.

Target Layer API
  Future low-abstraction Minecraft-facing escape hatch. This should expose
  target facts and target operations, not ergonomic gameplay APIs.

Modding API
  Future developer-facing Minecraft API surface for events, registries,
  commands, resources, networking, client behavior, and gameplay features.

Performance Layer
  Future first-party optimization modules. These may integrate deeply with
  target and API layers, but should not become loader-core responsibilities.
```

The stable Loader API remains loader-focused. `com.spindle.api.minecraft.*` remains deferred and intentionally excluded from the stable Loader API.

## SteelHook status

SteelHook is Spindle's internal custom injection hook subsystem inside `target-minecraft`. It is not a public hook API and not a Mixin replacement.

SteelHook currently has a narrow proof chain:

```text
Target-1: Artifact interpretation
  Reads planned server-side Minecraft runtime jars as class files and writes
  minecraft-artifact-interpretation.json.

Target-2: Hook contract diagnostics
  Validates explicit hook contracts against interpreted symbols and writes
  minecraft-hook-contracts.json.

Target-3: Known-symbol hook catalog
  Selects the first internal Minecraft 26.1.2 server known-symbol catalog for
  net/minecraft/server/Main.main([Ljava/lang/String;)V.

Target-4: Launch-boundary hook installation proof
  Installs one internal launch-boundary wrapper around Main.main and writes
  minecraft-hook-installation-plan.json plus minecraft-hook-installation-result.json.

Target-5: Hook placement analysis scaffold
  Reads the selected method Code attribute as opaque bytes and writes
  minecraft-hook-placement-plan.json.

Target-6: Instruction-aware bytecode model
  Decodes selected method bytecode, validates instruction, branch, switch, and
  exception-table boundaries, and writes minecraft-hook-bytecode-analysis.json.

Target-7: Injection patch planning dry-run
  Plans one symbolic method-entry invokestatic dispatcher patch and writes
  minecraft-hook-patch-plan.json.

Target-8: Fixture-only bytecode transformation
  Produces transformed fixture class bytes in tests only.

Target-9: Bootstrap class transformation path
  Applies the validated transform through fake-server bootstrap classloading and
  writes minecraft-hook-bootstrap-transformation-result.json.
```

Target-9 remains fake-server only. It does not transform real Minecraft runtime artifacts, does not rewrite `StackMapTable`, does not expose public APIs, does not add gameplay hooks, does not use Java agents or Mixin, and does not imply Java mod execution is sandboxed.

## Repository layout

```text
.
|-- spindle-loader-api
|   `-- Stable runtime-facing public Loader API.
|
|-- spindle-loader-core
|   `-- Target-neutral loader implementation, runtime contracts, diagnostics,
|       security gates, classloading, and lifecycle execution.
|
|-- spindle-loader-cli
|   `-- Loader entrypoint, CLI parsing, and provider wiring.
|
|-- target-minecraft
|   `-- Minecraft Target Layer implementation and internal SteelHook work.
|
|-- sample-mod
|   `-- Basic non-Minecraft sample mod.
|
|-- sample-runtime-mod
|   `-- Runtime API sample for config, services, storage, and capabilities.
|
|-- sample-minecraft-mod
|   `-- Minecraft-targeted bootstrap mod fixture. Not a stable gameplay API example.
|
|-- sample-game
|   `-- Fake game provider used for deterministic loader smoke tests.
|
|-- sample-server-fixture
|   `-- Fake server fixture used by Minecraft planning and bootstrap tests.
|
|-- docs
|   |-- architecture
|   `-- mods
|
|-- backlog
|   `-- Longer-term direction notes.
|
`-- runtime
    `-- Generated local runtime outputs. Ignored by Git.
```

## Public API today

The stabilized runtime-facing Loader API lives in `spindle-loader-api`.

Stable today:

```text
com.spindle.api.LoaderApi
com.spindle.api.ModContext
com.spindle.api.ModInitializer
com.spindle.api.config.ModConfig
com.spindle.api.exception.*
com.spindle.api.lifecycle.LifecyclePhase
com.spindle.api.service.ServiceRegistry
```

Deferred:

```text
com.spindle.api.minecraft.*
```

Public API metadata currently reports:

```text
RUNTIME_API_VERSION = 1
API_STATUS = spindle-loader-runtime-api-stabilized
API_SCOPE = runtime-facing-spindle-loader-api
TARGET_MODEL = minecraft-as-target-not-foundation
SANDBOXED = false
SANDBOX_CLAIM = not-sandboxed
```

## Runtime contracts

The current compiled profile schema is schema `6`.

The runtime contract system includes:

- capability grants
- mod-owned storage grants
- config schema runtime
- deterministic service registry
- runtime closure contract
- runtime honesty fields
- unavailable resource capability declarations
- deterministic gate order

Grantable runtime capabilities include:

```text
storage.config
storage.data
storage.cache
storage.generated
config.read
config.write
service.provide
service.consume
```

Current resource capabilities remain intentionally unavailable:

```text
resource.declare
resource.overlay
```

Broad Java behaviors such as process execution, native access, network access, and reflection are visibility or risk-reporting concerns. They are not sandbox-enforced grants in the current loader.

## Config and services

Mods may declare flat schema-2 config entries in `loader.mod.json`. Supported primitive config types are:

```text
boolean
integer
number
string
```

Config is materialized under mod-owned config storage:

```text
runtime/config/<modId>/config.json
```

The public `ModConfig` API exposes declared keys only. Writes require the appropriate runtime grant and must pass schema validation. Integer config is signed 32-bit end-to-end.

Mods may also declare deterministic service providers and consumers in metadata. The loader compiles those declarations into the runtime service contract and exposes per-mod service views through `context.services()`. Service providers are planned before classloading gates and instantiated lazily after fatal gates pass.

## Build and verification

Use the Gradle wrapper.

Run all tests:

```bash
./gradlew test
```

Run formatting checks:

```bash
./gradlew spotlessCheck
```

Apply formatting:

```bash
./gradlew spotlessApply
```

Run focused module tests:

```bash
./gradlew :spindle-loader-api:test
./gradlew :spindle-loader-core:test
./gradlew :target-minecraft:test
```

Run current loader smoke flows:

```bash
./gradlew runMilestone0
./gradlew validateMilestone0
./gradlew explainMilestone0
```

Run focused Minecraft planning and bootstrap checks:

```bash
./gradlew minecraftMegaMilestone7Check
./gradlew minecraftMilestone8Check
```

Run focused SteelHook test groups:

```bash
./gradlew :target-minecraft:test --tests "*MinecraftHookContract*"
./gradlew :target-minecraft:test --tests "*MinecraftHookPlacement*"
./gradlew :target-minecraft:test --tests "*MinecraftHookBytecodeAnalysis*"
./gradlew :target-minecraft:test --tests "*MinecraftHookPatch*"
./gradlew :target-minecraft:test --tests "*MinecraftFixture*"
./gradlew :target-minecraft:test --tests "*MinecraftBootstrapHook*"
./gradlew :target-minecraft:test --tests "*MinecraftRuntimeClassLoaderTransformationTest"
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Common Gradle tasks

Loader sample flows:

```bash
./gradlew runMilestone0
./gradlew validateMilestone0
./gradlew explainMilestone0
```

Minecraft planning flows:

```bash
./gradlew minecraftDryRun
./gradlew minecraftServerDryRun
./gradlew minecraftServerRuntimePlan
./gradlew minecraftServerRuntimeBoundary
./gradlew minecraftModIntegrationPlan
./gradlew minecraftPreflight
./gradlew minecraftReproducibilityCheck
```

Minecraft bootstrap fixture flows:

```bash
./gradlew minecraftModExecutionPlan
./gradlew minecraftBootstrapClassloaderGraph
./gradlew minecraftServerBootstrapFakeSmoke
./gradlew minecraftServerModExecutionFakeSmoke
./gradlew minecraftServerModExecutionOfflineReplay
```

Real vanilla server helper flows:

```bash
./gradlew minecraftRealServerAcquire -PmcRealVersion=latest-release
./gradlew minecraftRealServerSmoke -PmcRealVersion=latest-release
./gradlew minecraftRealServerOfflineReplay -PmcRealVersion=latest-release
./gradlew minecraftRealServerEulaSmoke -PmcRealVersion=latest-release
```

Optional Mache reference scan:

```bash
./gradlew macheReferenceScan -PmacheDir=C:\path\to\mache
```

When `-PmacheDir` is omitted, `macheReferenceScan` prints a skip message.

## Useful Minecraft CLI flags

Use `--game-provider sample` for the fake sample provider and `--game-provider minecraft` for Minecraft target flows.

Core Minecraft flags:

```text
--minecraft-version <version>
--minecraft-side <client|server>
--minecraft-server
--minecraft-dry-run
--minecraft-plan-runtime
--minecraft-runtime-boundary
--minecraft-plan-mods
--minecraft-preflight
--minecraft-reproducibility-check
--strict-resources
--strict-packages
--offline
```

SteelHook report flags:

```text
--minecraft-interpret-artifact
--minecraft-hook-contracts
--minecraft-hook-installation-plan
--minecraft-install-hooks
--minecraft-hook-placement-plan
--minecraft-hook-bytecode-analysis
--minecraft-hook-patch-plan
--minecraft-bootstrap-transform-hooks
```

Target-9 bootstrap transformation requires:

```text
--minecraft-bootstrap-transform-hooks
--minecraft-bootstrap-fake-server
```

Target-9 intentionally rejects combination with:

```text
--minecraft-install-hooks
```

Use `--strict-resources` to fail on duplicate non-class resources. Use `--strict-packages` to fail on split packages. Without those flags, duplicate resources and split packages are recorded as diagnostics only.

## Generated outputs

Typical loader outputs:

```text
runtime/spindle.profile.json
runtime/spindle.report.json
runtime/spindle.graph.json
runtime/spindle.lock.json
runtime/spindle.security-report.json
runtime/spindle.lifecycle-report.json
runtime/spindle.quality-report.json
runtime/diagnostics/startup-trace.json
runtime/diagnostics/startup-profile.json
```

Minecraft target outputs:

```text
runtime/minecraft-launch-plan.json
runtime/minecraft-artifacts.json
runtime/minecraft-server-runtime-plan.json
runtime/minecraft-runtime-boundary.json
runtime/minecraft-mod-integration-plan.json
runtime/spindle.preflight.json
runtime/minecraft-runtime-provenance.json
runtime/minecraft-reproducibility-check.json
runtime/minecraft-mod-execution-plan.json
runtime/minecraft-bootstrap-classloader-graph.json
runtime/minecraft-server-bootstrap-result.json
runtime/minecraft-mod-execution-result.json
```

SteelHook outputs:

```text
runtime/minecraft-artifact-interpretation.json
runtime/minecraft-hook-contracts.json
runtime/minecraft-hook-installation-plan.json
runtime/minecraft-hook-installation-result.json
runtime/minecraft-hook-placement-plan.json
runtime/minecraft-hook-bytecode-analysis.json
runtime/minecraft-hook-patch-plan.json
runtime/minecraft-hook-bootstrap-transformation-result.json
```

`runtime/` is generated local state and is ignored by Git.

## Expected sample output

First successful sample run:

```text
[spindle] discovered 1 mod
[spindle] resolved 1 mod
[spindle] wrote spindle.lock.json
Sample mod initialized
Game starting
[spindle] startup complete
```

Second successful sample run:

```text
[spindle] discovered 1 mod
[spindle] resolved 1 mod
[spindle] verified spindle.lock.json
Sample mod initialized
Game starting
[spindle] startup complete
```

## Documentation map

Important architecture docs:

```text
docs/architecture/security-posture.md
docs/architecture/restricted-security-tooling.md
docs/architecture/runtime-0-compiled-profile-footprint.md
docs/architecture/runtime-1-compiled-runtime-kernel.md
docs/architecture/runtime-2-capability-grant-contract.md
docs/architecture/runtime-3-deterministic-service-registry.md
docs/architecture/runtime-4-config-schema-runtime.md
docs/architecture/runtime-5-runtime-contract-closure.md
docs/architecture/loader-api-0-public-runtime-api-boundary.md
docs/architecture/loader-api-hardening.md
docs/architecture/target-layer-api-boundary.md
docs/architecture/target-1-minecraft-artifact-interpretation.md
docs/architecture/target-2-hook-point-contract-model.md
docs/architecture/target-3-known-symbol-hook-validation.md
docs/architecture/target-4-minimal-hook-installation-proof.md
docs/architecture/target-5-hook-placement-analysis-scaffold.md
docs/architecture/target-6-instruction-aware-bytecode-model.md
docs/architecture/target-7-injection-patch-planning-dry-run.md
docs/architecture/target-8-fixture-only-bytecode-transformation.md
docs/architecture/target-9-bootstrap-class-transformation-path.md
```

Mod-facing docs:

```text
docs/mods/loader-api.md
docs/mods/capabilities.md
docs/mods/config.md
docs/mods/services.md
docs/mods/artifact-trust.md
docs/mods/static-risk-signals.md
docs/mods/security-and-trust-boundaries.md
docs/mods/security-scenarios.md
```

Module READMEs:

```text
spindle-loader-api/README.md
spindle-loader-core/README.md
spindle-loader-cli/README.md
target-minecraft/README.md
```

## Development guidance

Keep changes in the right layer.

```text
Loader problem
  Put it in Spindle Loader.

Minecraft integration problem
  Put it in the Minecraft Target Layer.

Internal hook machinery problem
  Put it under target-minecraft, usually under com.spindle.core.minecraft.hook.

Developer-facing gameplay API problem
  Put it in a future Modding API layer, not in loader-core.

Optimization problem
  Put it in a future Performance Layer.
```

Do not add unrelated ECS, threading, simulation, compatibility, or optimization features to the loader core. Do not imply Java mod execution is sandboxed. Do not stabilize `com.spindle.api.minecraft.*` until the Minecraft Target Layer and future Modding API boundary are intentionally ready.

## License

See `LICENSE`.
