# Runtime-1 Compiled Runtime Kernel

`Runtime-1: Compiled Runtime Kernel` turns `spindle.profile.json` from a Runtime-0 footprint into a deterministic runtime contract for Spindle's standard platform path.

Runtime-1 keeps the Foundation and Milestone 8 Minecraft bootstrap tracks intact. It does not add bytecode injection, remapping, Mixin compatibility, client launch, rendering, gameplay APIs, broad compatibility layers, or real Minecraft hooks.

## Metadata

`loader.mod.json` now supports both schema `1` and schema `2`.

Schema `1` remains the compatibility path for:

- `entrypoints.main` legacy `ModInitializer` startup
- `entrypoints.minecraftServer` Milestone 8 Minecraft bootstrap planning/execution

Schema `2` adds:

- explicit lifecycle declarations using `ClassName::methodName`
- deterministic storage declarations for config/data/cache/generated directories
- explicit permissions lists, currently recorded and reported but not granted

Lifecycle declaration strings are validated before classloading. Runtime-1 also validates schema `2` handler signatures before any handler invocation. Supported phases are:

- `BOOTSTRAP`
- `CONFIGURE`
- `PRE_SERVER_MAIN`

## Compiled Profile

`spindle.profile.json` now writes schema version `2` and includes:

- `inputFingerprint`
- cache status and cache reason
- metadata schema summary
- lifecycle phase order and planned handlers
- per-mod owned storage/context plans
- runtime package policy summaries
- quality summary counts and score

The profile fingerprint remains deterministic. It excludes timestamps, process ids, raw absolute machine paths, and cache hit/miss status.

## Profile Cache

Runtime-1 stores cached compiled profiles under:

`<workingDirectory>/.spindle/profile-cache/<inputFingerprint>/spindle.profile.json`

The input fingerprint is derived from stable pre-classload inputs such as loader version, Java major version, game provider identity, resolved mod ids/versions/paths/hashes, resolved order, classpath entries, lockfile fingerprint, strict runtime flags, and the compiled-profile schema version.

Equivalent repeated runs reuse the cached profile. Changed mod hashes, lockfile fingerprints, schema version changes, side changes, or runtime policy changes invalidate the cache.

Diagnostics now record `runtime.compiled_profile.cache` with `cacheStatus`, `cacheReason`, and `inputFingerprint`.

## Standard Runtime Kernel

For the standard non-Minecraft path, lifecycle execution is driven from the compiled profile instead of rereading metadata after classloading.

Runtime-1 adds:

- `com.spindle.api.ModContext`
- `com.spindle.api.lifecycle.LifecyclePhase`
- deterministic owned storage creation under `config/`, `mod-data/`, `cache/mods/`, and `generated/`
- precomputed mod contexts built from the compiled profile
- lifecycle execution reports written from planned and executed handlers

Schema `1` `main` entrypoints remain available as legacy `BOOTSTRAP` handlers through a compatibility shim. Milestone 8 `minecraftServer` entrypoints remain on the Minecraft bootstrap path.

## Package Policy and Reports

Runtime-1 records runtime package policy in the compiled profile and rejects protected package definitions before classloading on the standard runtime path.

Protected package coverage now includes at least:

- `com.spindle.core`
- `com.spindle.api.internal`
- `net.minecraft`
- `org.spongepowered.asm`

Runtime-1 also writes:

- `spindle.lifecycle-report.json`
- `spindle.quality-report.json`

These reports are deterministic and summarize planned lifecycle handlers, executed handlers, cache state, owned storage directories, duplicate resources, split packages, protected package findings, metadata findings, and quality score.

## Non-goals

Runtime-1 intentionally does not implement:

- SteelHook runtime
- injection
- bytecode transformation
- real Minecraft hooks
- client launch
- rendering
- gameplay APIs
- remapping
- Mixin compatibility
- broad compatibility with existing mod ecosystems
