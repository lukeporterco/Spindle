# Spindle

Spindle is a forward-only Java 25 mod loader project with Minecraft treated as a target layer, not as the loader's foundation.

Today the repository contains a stable runtime-facing loader API, a target-neutral loader core, CLI wiring, and a partial Minecraft Target Layer for baseline Minecraft `26.1.2`.

## Status

- Loader runtime: stable for the current Runtime API-0 / Runtime-1 scope.
- Minecraft Target Layer: partial and intentionally narrow.
- SteelHook: internal only, with bounded proofs completed through SteelHook `0.4`.
- Public Minecraft modding API: not implemented yet.

Spindle is not currently a Fabric/Forge/NeoForge/Quilt compatibility layer, a gameplay API, a client launcher, a remapping stack, or a Java sandbox.

## Repository Layout

```text
Spindle repo
├─ spindle-loader-api/
│  └─ Stable runtime-facing public loader API
│
├─ spindle-loader-core/
│  └─ Target-neutral loader planning, diagnostics, security, classloading, lifecycle
│
├─ spindle-loader-cli/
│  └─ CLI entrypoint and provider wiring
│
├─ target-minecraft/
│  └─ Minecraft Target Layer, planning, bootstrap, SteelHook internals
│
├─ sample-game/
│  └─ Fake game provider fixture
│
├─ sample-mod/
│  └─ Basic sample mod
│
├─ sample-runtime-mod/
│  └─ Runtime API sample
│
├─ sample-server-fixture/
│  └─ Fake Minecraft server fixture
│
├─ sample-minecraft-mod/
│  └─ Guarded Minecraft bootstrap fixture mod
│
├─ docs/
│  └─ Architecture and mod-facing documentation
│
└─ backlog/
   └─ Longer-term notes
```

## What Works Today

The loader side currently supports:

- `loader.mod.json` parsing for schema `1` and `2`
- deterministic discovery, dependency resolution, frozen graphs, and lockfiles
- runtime contract validation and fail-closed gates before mod classloading
- deterministic diagnostics, trust, and quality reports
- loader-owned config, data, cache, and generated storage
- runtime config and deterministic service registry support
- stable public runtime-facing APIs in `com.spindle.api.*`

The Minecraft Target Layer currently supports:

- Minecraft artifact planning, cache/verification, and runtime boundary reporting
- deterministic runtime, integration, preflight, and reproducibility planning
- guarded fake-server bootstrap and offline replay flows
- concept-grounding architecture work for lifecycle, commands, resources/reload, and registry bootstrap
- internal SteelHook proof arcs through `Target-36` / SteelHook `0.4`

## What Is Still Intentionally Missing

- public Minecraft gameplay APIs
- registry, command, networking, resource, world, entity, or client modding APIs
- general-purpose Minecraft runtime transformation
- compatibility shims for other loader ecosystems
- Java sandboxing for mods

`com.spindle.api.minecraft.*` remains a deferred placeholder area used by guarded bootstrap fixtures. It is not part of the stabilized loader API.

## Build And Verification

Use Java 25 and the Gradle wrapper:

```bash
./gradlew spotlessApply
./gradlew spotlessCheck
./gradlew :spindle-loader-core:test
```

For changes touching Minecraft runtime planning, boundary reports, bootstrap execution, reproducibility, integration planning, or SteelHook/bootstrap behavior, also run:

```bash
./gradlew minecraftMegaMilestone7Check
./gradlew minecraftMilestone8Check
```

For a quick smoke check:

```bash
./gradlew validateMilestone0
```

On Windows, use `gradlew.bat`.

Do not run the real Mojang download or real server smoke tasks unless you explicitly want those networked/EULA-sensitive flows.

## Key Docs

- [Architecture Overview](docs/architecture/README.md)
- [Minecraft Target Layer Architecture](docs/architecture/minecraft-target/README.md)
- [Minecraft Target Concept Roadmap](docs/architecture/minecraft-target/minecraft-target-concept-roadmap.md)
- [SteelHook Strategy](docs/architecture/steelhook/README.md)
- [Loader API Docs](docs/mods/loader-api.md)
- [Security And Trust Boundaries](docs/mods/security-and-trust-boundaries.md)

## Current Direction

Near-term work is still about strengthening deterministic runtime foundations and carefully grounding Minecraft-facing concepts before any public Minecraft modding API is exposed.

That means:

- keep loader-core target-neutral
- keep Minecraft-specific work in `target-minecraft`
- treat SteelHook as internal machinery, not a public API
- preserve deterministic reports, lockfiles, and fail-closed validation

## License

Spindle is licensed under the Mozilla Public License 2.0. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
