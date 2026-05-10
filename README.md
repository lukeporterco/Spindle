# Spindle

Spindle is a custom Minecraft mod loader project built around a simple long-term rule: Minecraft is the target, not the foundation.

The current repository is the Spindle Loader. It is the backend spine of the ecosystem. It discovers mods, validates metadata and trust state, resolves dependencies, compiles deterministic runtime contracts, prepares runtime state, creates classloaders, executes lifecycle handlers, and hands off toward Minecraft target integration.

Spindle is not currently a full Minecraft gameplay API, a replacement for Fabric API, a performance mod suite, or a custom injection framework. Those belong to future ecosystem layers that should build around the loader rather than being folded into the loader core.

Minimum Java version: Java 25

First intended Minecraft target: 26.1.2

## Current status

The loader spine is considered complete for the current scope. The Minecraft target layer has a partial foundation. The broad developer-facing API layer, performance layer, and real third-party ecosystem are future work.

```text
Spindle Ecosystem
 |-- Spindle Loader [DONE]
 |   |-- Mod Intake [DONE]
 |   |-- Resolution and Planning [DONE]
 |   |-- Runtime Contract System [DONE]
 |   |-- Security and Diagnostics [DONE]
 |   |-- Runtime Execution [DONE]
 |   `-- Runtime-facing Loader API [DONE]
 |
 |-- Minecraft Target Layer [PART]
 |   |-- Minecraft Target Model [PART]
 |   |-- Artifact, Version, and Mapping Integration [PART]
 |   |-- Target Launch and Attachment [PART]
 |   |-- Target Lifecycle Bridge [PART]
 |   |-- Target Hook System [TODO]
 |   |-- Target Diagnostics [PART]
 |   `-- Target API Bridge [PART]
 |
 |-- Spindle API Layer [TODO]
 |   |-- API Contract Foundation [TODO]
 |   |-- Event API [TODO]
 |   |-- Registry API [TODO]
 |   |-- Resource and Data API [TODO]
 |   |-- Networking API [TODO]
 |   |-- Command API [TODO]
 |   |-- Client API [TODO]
 |   |-- World and Gameplay API [TODO]
 |   `-- Developer Utilities [PART]
 |
 |-- Spindle Performance Layer [TODO]
 |   |-- Performance Control Plane [TODO]
 |   |-- Renderer Optimizations [TODO]
 |   |-- Chunk and World Optimizations [TODO]
 |   |-- Lighting Optimizations [TODO]
 |   |-- Entity and Tick Optimizations [TODO]
 |   |-- Networking Optimizations [TODO]
 |   `-- Memory Optimizations [TODO]
 |
 `-- Third-Party Mods [PART]
     |-- Runtime-only mods [PART]
     |-- Minecraft API mods [TODO]
     |-- Resource/data mods [TODO]
     |-- Client-side mods [TODO]
     |-- Server-side mods [TODO]
     |-- Optimization-dependent mods [TODO]
     `-- Full gameplay/content mods [TODO]
```

Status labels:

```text
[DONE] The subsystem is implemented for the current intended scope.
[PART] A foundation exists, but the subsystem is not complete.
[TODO] The subsystem is not implemented yet.
```

## What this repository is

This repository is the Spindle Loader.

Its job is to provide a deterministic backend runtime for mods:

- discover mod artifacts
- parse and validate `loader.mod.json`
- resolve dependencies and version requirements
- produce a frozen mod graph
- plan classpaths and ownership indexes
- collect trust, risk, quality, and diagnostics reports
- compile a deterministic runtime profile
- enforce runtime contract gates before lifecycle execution
- materialize mod-owned storage and config
- expose a stable runtime-facing loader API
- execute loader-controlled lifecycle handlers
- prepare the boundary that future Minecraft target work consumes

The loader should stay narrow. It should not absorb broad gameplay APIs, Minecraft registries, event systems, networking APIs, commands, resource overlays, performance optimizations, ECS, threading systems, or custom injection behavior.

## What this repository is not

Spindle currently does not provide a complete Minecraft modding ecosystem.

Not implemented yet:

- full Minecraft gameplay API
- event API
- registry API
- command API
- networking API
- resource and data overlay API
- client API
- world and gameplay API
- renderer, chunk, lighting, entity, networking, or memory optimization modules
- general-purpose target hook system
- custom injection hooker
- Mixin, ASM, access widener, Fabric, Forge, NeoForge, Quilt, Bukkit, Paper, or Sponge compatibility

Spindle Java mod execution is not sandboxed. The current runtime honesty contract is:

```text
execution: in-process-unrestricted-java
sandboxed: false
sandboxClaim: not-sandboxed
```

Security features in this repository are validation, reporting, trust classification, warning-only static risk signals, restricted child-JVM static tooling, and fatal gates for declared runtime contracts. They are not a Java sandbox.

## Ecosystem direction

Spindle should grow around the loader, not by bloating it.

The intended long-term split is:

```text
Spindle Loader
  Owns discovery, validation, resolution, runtime contracts, classloading,
  lifecycle execution, security diagnostics, and the runtime-facing loader API.

Minecraft Target Layer
  Owns Minecraft launch integration, version and mapping handling, target
  lifecycle bridging, target diagnostics, and future target hook installation.

Spindle API Layer
  Owns developer-facing Minecraft APIs such as events, registries, resources,
  networking, commands, client APIs, world hooks, and developer utilities.

Spindle Performance Layer
  Owns first-party optimization modules. These modules may integrate deeply with
  the target and API layers, but should not become loader-core responsibilities.

Third-Party Mods
  Depend on stable Spindle contracts and API surfaces. Mods should declare their
  needs clearly rather than relying on hidden Minecraft internals.
```

## Repository layout

```text
.
|-- loader-api
|   `-- Public runtime-facing loader API used by mods today.
|
|-- loader-core
|   `-- Loader implementation, runtime contracts, diagnostics, security,
       classloading, lifecycle execution, and Minecraft target foundation.
|
|-- sample-mod
|   `-- Basic sample mod for the sample provider.
|
|-- sample-runtime-mod
|   `-- Sample mod that exercises runtime-facing config, services, storage,
       and capability behavior.
|
|-- sample-minecraft-mod
|   `-- Sample Minecraft-targeted bootstrap mod. This uses deferred
       Minecraft-facing API surfaces and is not a stable Minecraft API example.
|
|-- sample-game
|   `-- Fake sample game provider used for deterministic loader smoke tests.
|
|-- sample-server-fixture
|   `-- Fake server fixture used by Minecraft server planning and bootstrap tests.
|
|-- docs
|   |-- architecture
|   `-- mods
|
|-- backlog
|   `-- Longer-term direction notes.
|
`-- runtime
    `-- Generated local runtime outputs. This directory is ignored by Git.
```

## Loader subsystem breakdown

The current repository should be evaluated as these completed loader categories.

### Mod Intake

Handles the input side of mod loading:

- mod discovery
- metadata parsing
- artifact identity
- artifact trust state
- static risk signal collection

### Resolution and Planning

Turns discovered artifacts into deterministic loader plans:

- dependency resolution
- version requirement handling
- frozen mod graph generation
- classpath planning
- ownership indexing
- resource conflict indexing

### Runtime Contract System

Compiles mod declarations into deterministic runtime behavior:

- compiled runtime profile
- capability grants
- storage grants
- config schema runtime
- deterministic service registry
- lifecycle contract
- runtime closure contract
- target handoff boundary

### Security and Diagnostics

Reports loader trust and failure state without pretending Java execution is sandboxed:

- trust boundary report
- security gates
- warning-only risk signals
- restricted static tooling
- runtime honesty disclosures
- developer and user diagnostics

### Runtime Execution

Executes loader-controlled runtime behavior after validation gates:

- runtime config materialization
- runtime service planning
- classloader creation
- `ModContext` materialization
- lifecycle execution
- failure translation into public API exceptions

### Runtime-facing Loader API

Provides the stable public API surface for loader runtime behavior:

- `LoaderApi`
- `ModInitializer`
- `ModContext`
- capability checks
- storage access
- config access
- service access
- public loader exceptions

## Stable Loader API

Loader API-0 stabilizes only the runtime-facing `loader-api` surface.

Stable today:

- `com.spindle.api.LoaderApi`
- `com.spindle.api.ModContext`
- `com.spindle.api.ModInitializer`
- `com.spindle.api.config.ModConfig`
- `com.spindle.api.exception.*`
- `com.spindle.api.lifecycle.LifecyclePhase`
- `com.spindle.api.service.ServiceRegistry`

Deferred:

- `com.spindle.api.minecraft.*`

The public API metadata currently reports:

```text
RUNTIME_API_VERSION = 1
API_STATUS = runtime-api-stabilized
API_SCOPE = runtime-facing-loader-api
TARGET_MODEL = minecraft-as-target-not-foundation
SANDBOXED = false
SANDBOX_CLAIM = not-sandboxed
```

`com.spindle.api.minecraft.*` exists only as a deferred Minecraft bootstrap surface. It is not part of the stabilized runtime-facing Loader API.

## Runtime contracts

The current compiled profile schema is schema `6`.

The runtime contract system includes:

- capability grant contract
- mod-owned storage grants
- config schema runtime
- deterministic service registry
- runtime closure contract
- runtime honesty fields
- unavailable resource capability declarations
- deterministic gate order

Current resource capabilities remain intentionally unavailable:

```text
resource.declare
resource.overlay
```

Those belong to future Resource and Data API work, not the completed loader spine.

## Capabilities

The current grantable runtime capabilities include loader-owned runtime surfaces such as:

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

Broad Java behaviors such as process execution, native access, network access, and reflection are visibility or risk-reporting concerns. They are not treated as sandbox-enforced grants in the current loader.

## Config

Mods may declare flat schema-2 config entries in `loader.mod.json`. The runtime config system supports primitive config types:

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

## Services

Mods may declare deterministic service providers and service consumers in metadata. The loader compiles those declarations into the runtime service contract and exposes per-mod service views through `context.services()`.

Service providers are planned before classloading gates and instantiated lazily after fatal gates pass. Mods only see services they declared as consumed.

## Security posture

Spindle security is explicit about what it does and does not do.

It does:

- validate metadata and runtime contracts
- report artifact trust state
- emit warning-only static risk signals
- run restricted static tooling in child JVMs
- gate standard runtime lifecycle execution on fatal validation failures
- document runtime honesty fields
- expose developer and user diagnostics

It does not:

- sandbox Java mod execution
- claim process, network, native, or reflection access is blocked
- transform arbitrary Java code into safe code
- guarantee third-party mod safety
- make warning-only static risk signals fatal unless a future contract explicitly does so

## Minecraft target foundation

The repository contains a partial Minecraft target foundation under `loader-core`.

Current target work includes:

- Minecraft version planning
- vanilla server artifact handling
- cache inspection and repair flows
- runtime boundary reports
- mod integration planning
- preflight reports
- reproducibility checks
- server bootstrap fixture tests
- a narrow server-side bootstrap path for approved Spindle Minecraft server entrypoints

This is not yet a completed Minecraft Target Layer. In particular, the target hook system and custom injection hooker are not implemented. Full Minecraft gameplay APIs are also not implemented.

The target layer should eventually become a first-class subsystem around the loader. It should consume loader contracts and lower them into Minecraft-specific behavior without making loader-core responsible for every Minecraft concern.

## Build and verification

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

Run focused loader smoke flows:

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

Run current runtime and loader API tests through the normal Gradle test task:

```bash
./gradlew :loader-core:test
./gradlew :loader-api:test
```

## Common tasks

Sample launch:

```bash
./gradlew runMilestone0
```

Validation-only sample run:

```bash
./gradlew validateMilestone0
```

Validation with explain summary:

```bash
./gradlew explainMilestone0
```

Minecraft client dry run:

```bash
./gradlew minecraftDryRun
```

Minecraft server dry run:

```bash
./gradlew minecraftServerDryRun
```

Minecraft server runtime plan:

```bash
./gradlew minecraftServerRuntimePlan
```

Minecraft runtime boundary report:

```bash
./gradlew minecraftServerRuntimeBoundary
```

Minecraft mod integration plan:

```bash
./gradlew minecraftModIntegrationPlan
```

Minecraft preflight:

```bash
./gradlew minecraftPreflight
```

Minecraft reproducibility check:

```bash
./gradlew minecraftReproducibilityCheck
```

Milestone 8 Minecraft mod execution plan:

```bash
./gradlew minecraftModExecutionPlan
```

Milestone 8 bootstrap classloader graph:

```bash
./gradlew minecraftBootstrapClassloaderGraph
```

Milestone 8 fake Minecraft bootstrap smoke:

```bash
./gradlew minecraftServerBootstrapFakeSmoke
```

Milestone 8 approved Minecraft server mod execution smoke:

```bash
./gradlew minecraftServerModExecutionFakeSmoke
```

Milestone 8 offline bootstrap replay smoke:

```bash
./gradlew minecraftServerModExecutionOfflineReplay
```

Real vanilla server baseline acquire:

```bash
./gradlew minecraftRealServerAcquire -PmcRealVersion=latest-release
```

Real vanilla server smoke:

```bash
./gradlew minecraftRealServerSmoke -PmcRealVersion=latest-release
```

Real vanilla server offline replay:

```bash
./gradlew minecraftRealServerOfflineReplay -PmcRealVersion=latest-release
```

Optional real vanilla server EULA smoke:

```bash
./gradlew minecraftRealServerEulaSmoke -PmcRealVersion=latest-release
```

Optional Mache reference scan:

```bash
./gradlew macheReferenceScan -PmacheDir=C:\path\to\mache
```

If `-PmacheDir` is not provided, `macheReferenceScan` prints a skip message and does nothing else.

## Generated outputs

Typical validation and runtime flows write reports under `runtime/`.

Common loader outputs:

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

Common Minecraft target foundation outputs:

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

`runtime/` is generated local state and is ignored by Git.

## CLI notes

Use `--game-provider sample` for the fake sample provider and `--game-provider minecraft` for Minecraft planning and target foundation flows.

Useful Minecraft flags include:

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

Use `--strict-resources` to fail on duplicate non-class resources. Use `--strict-packages` to fail on split packages. Without those flags, duplicate resources and split packages are recorded as diagnostics only.

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

## Documentation

Architecture docs:

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
docs/architecture/foundation-hardening-loader-runtime.md
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

## Development guidance

Keep loader changes small and local. The current loader spine is intended to be stable enough to build around.

Prefer future work to land in the correct layer:

```text
Loader problem
  Put it in Spindle Loader.

Minecraft integration problem
  Put it in Minecraft Target Layer.

Developer-facing gameplay API problem
  Put it in Spindle API Layer.

Optimization problem
  Put it in Spindle Performance Layer.
```

Do not add unrelated ECS, threading, injection, simulation, compatibility, or optimization features to the loader core. Do not imply Java mod execution is sandboxed. Do not stabilize `com.spindle.api.minecraft.*` until the Minecraft Target Layer and API Layer are intentionally ready for that boundary.

## License

See `LICENSE`.
