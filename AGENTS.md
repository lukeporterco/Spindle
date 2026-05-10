# AGENTS.md

## Project scope

Spindle is a forward-only Java 25 Minecraft mod loader for baseline Minecraft `26.1.2`.

It is not a Fabric, Forge, NeoForge, Quilt, Paper, Bukkit, Sponge, Mixin, remapping, access widener, or compatibility-layer project. Do not add compatibility features unless the task explicitly asks for them.

The current architecture favors:

- deterministic planning before execution
- explicit game providers
- frozen mod graphs
- verified artifact/cache behavior
- offline replay
- deterministic reports and lockfiles
- server-first Minecraft runtime ownership
- narrow child-JVM bootstrap execution for approved server mods
- no Minecraft class transformation or gameplay API exposure yet

Future work should move toward named architecture arcs, especially `Runtime-*`, `Platform-*`, `SteelHook-*`, and `Minecraft-*`, rather than endless generic milestone numbering.

## Repository map

- `spindle-loader-api/`
  - Public API only.
  - Keep this small, stable, and developer-readable.
  - Do not expose loader internals through this module.

- `spindle-loader-core/`
  - Target-neutral loader implementation.
  - Important package areas:
    - `classpath`: mod classloader and runtime classpath planning
    - `diagnostics`: structured diagnostics and loader exceptions
    - `discovery`: mod jar discovery
    - `game`: game provider boundary
    - `graph`: frozen mod graph and dependency/conflict model
    - `lockfile`: deterministic lockfile writing/verification
    - `metadata`: `loader.mod.json` parsing
    - `ownership`: class/package ownership indexes
    - `profile`: startup profiling
    - `resource`: resource conflict indexes
    - `resolve`: dependency resolution
    - `security`: trust-boundary validation, gate, and report writing
    - `state`: modpack state reports

- `spindle-loader-cli/`
  - Loader application entrypoint, CLI parsing, and provider-selection wiring.

- `target-minecraft/`
  - Partial Minecraft Target Layer.
  - Important package areas:
    - `artifact`: Minecraft artifact cache/download/verification
    - `baseline`: real vanilla server baseline acquisition/reporting
    - `mache`: optional reference scan tooling
    - `minecraft`: Minecraft runtime planning, boundary reports, integration plans, execution plans
    - `minecraft/bootstrap`: child-JVM bootstrap path
    - `process`: managed Minecraft server process launching

- `sample-game/`
  - Fake game provider fixture.

- `sample-mod/`
  - Basic non-Minecraft sample mod.

- `sample-server-fixture/`
  - Fake Minecraft server fixture.

- `sample-minecraft-mod/`
  - Approved server bootstrap mod fixture.

- `runtime/`
  - Generated output.
  - Ignored by git.
  - Do not treat runtime files as source.

## Required environment

Use Java 25.

Gradle is invoked through the wrapper:

```bash
./gradlew <task>
```

On Windows, use:

```bat
gradlew.bat <task>
```

## Default verification

For normal code changes, run:

```bash
./gradlew spotlessApply
./gradlew spotlessCheck
./gradlew :spindle-loader-core:test
```

For changes touching Minecraft runtime planning, boundary reports, preflight, reproducibility, bundled runtime behavior, integration planning, approved Minecraft server mod execution, bootstrap classloaders, plan fingerprints, child-JVM bootstrap, or bootstrap reports, also run:

```bash
./gradlew minecraftMegaMilestone7Check
./gradlew minecraftMilestone8Check
```

For a quick basic smoke check, run:

```bash
./gradlew validateMilestone0
```

Do not run real Mojang download or real server smoke tasks unless the user explicitly asks. These tasks may use network access, local cache state, or EULA-sensitive flows:

```bash
./gradlew minecraftRealServerAcquire
./gradlew minecraftRealServerSmoke
./gradlew minecraftRealServerEulaSmoke
./gradlew minecraftServerDownloadSmoke
```

## Coding rules

Use Java.

Keep public API names boring and obvious. Prefer names like `ModContext`, `LifecyclePhase`, `ServiceRegistry`, `ConfigView`, `ResourcePlan`, and `HookPermission`.

Do not leak implementation classes from `spindle-loader-core` into `spindle-loader-api`.

Keep deterministic behavior explicit:

- sort maps before writing reports
- sort lists when order is not semantically meaningful
- avoid filesystem traversal order as observable output
- avoid timestamps in reproducibility-sensitive reports
- normalize paths in reports
- avoid absolute path leakage unless the report is explicitly local/human-only
- preserve reproducible jar settings

Prefer small planner, verifier, writer, and model classes over adding more orchestration to `LoaderMain`.

When adding a report, add a model and writer. Do not build large JSON strings inline.

When adding a gate, fail closed. Rejected mods, fatal boundary violations, hash drift, plan drift, protected package violations, and unsupported side behavior should stop execution before mod classloading.

## Minecraft boundaries

Do not add any of the following unless the task explicitly asks for that capability:

- Mixin support
- ASM transformation as a general compatibility layer
- remapping
- access wideners
- Fabric/Forge/NeoForge/Quilt/Paper compatibility
- Minecraft client launch
- gameplay API exposure
- arbitrary Minecraft classpath injection
- mod jars on the real Minecraft runtime classpath
- real Minecraft hooks outside an explicit future `SteelHook-*` or `Minecraft-*` pass

Current Minecraft mod execution is bootstrap-only and server-only. It must remain guarded by execution plans, classloader policy, hash/fingerprint verification, and deterministic reports.

Standard Runtime-1 mod execution is also not sandboxed. New security work should describe that explicitly and should not claim arbitrary runtime mods are safe just because they pass Spindle validation.

## Metadata and generated files

Current mod metadata file:

```text
loader.mod.json
```

Current mod metadata supports `schema: 1` and `schema: 2`.

- `schema: 1` remains the compatibility path.
- `schema: 2` is the current Spindle-native Runtime-1 lifecycle and trust-boundary contract.

When adding schema features, preserve old schema behavior or reject with a clear diagnostic. Do not silently reinterpret older metadata.

Generated report names should remain stable and descriptive. Prefer the future `spindle.*.json` naming convention for new artifacts, but do not rename existing files unless the task asks for a migration.

`spindle.security-report.json` is a deterministic trust-boundary report, not a sandbox claim or a malware verdict.

Do not commit files from `runtime/`.

## Tests

Use JUnit 5.

Add or update tests when changing:

- metadata parsing
- dependency resolution
- lockfile behavior
- report contents
- ordering rules
- artifact/cache verification
- Minecraft runtime planning
- Minecraft boundary scanning
- preflight failure policy
- reproducibility checks
- bootstrap execution
- classloader policy
- package/class ownership
- process-launch behavior

Test deterministic output by comparing stable JSON, hashes, or repeated runs when practical.

For negative behavior, assert the rejection reason, not only that an exception occurred.

## Developer ergonomics

This project should be easy for future mod developers to understand.

Public API and metadata should use intuitive names. Avoid clever names, ambiguous abbreviations, and internal implementation terms in developer-facing surfaces.

Diagnostics should include:

- mod id when applicable
- file or class involved
- specific rule violated
- fatal vs warning severity
- what the developer should change when clear

Bad diagnostic style:

```text
Invalid mod.
```

Good diagnostic style:

```text
Mod `example_core` declares class `net.minecraft.server.ExamplePatch`, but package `net.minecraft` is protected. Mods may not define Minecraft packages.
```

## Direction for new architecture work

Prefer this sequence:

1. Runtime foundation before Minecraft hooks.
2. Platform systems before injection.
3. SteelHook registry and fixture dispatch before real Minecraft bytecode.
4. First real Minecraft hook only after hook contracts, permissions, risk model, and reports exist.

Good near-term systems:

- compiled modpack profile
- lifecycle declarations
- generated or precomputed mod contexts
- deterministic service registry
- config schema validation
- loader-owned config/data/cache/generated directories
- package sealing and classloader firewall
- resource overlay planner
- startup profiling
- modpack quality report

Avoid broad scope jumps. A pass should have clear non-goals.

## Before finalizing a change

Check:

- Did this preserve deterministic output?
- Did this avoid expanding Minecraft compatibility scope accidentally?
- Did this keep public API readable?
- Did this fail closed before mod classloading when safety checks fail?
- Did this add or update focused tests?
- Did this avoid committing generated `runtime/` files?
- Did the required Gradle verification task pass?
