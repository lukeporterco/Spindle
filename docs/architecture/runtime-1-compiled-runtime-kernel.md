# Runtime-1 Compiled Runtime Kernel

`Runtime-1: Compiled Runtime Kernel` is the source of truth for Spindle's compiled standard-runtime contract. It turns `spindle.profile.json` from a Runtime-0 footprint into a deterministic runtime contract for the standard platform path.

Runtime-1 keeps the Foundation and Milestone 8 Minecraft bootstrap tracks intact. It does not add SteelHook, hooks, injection, bytecode transformation, remapping, Mixins, access wideners, client launch, rendering, gameplay APIs, broad compatibility layers, or real Minecraft hooks.

## Metadata

`loader.mod.json` now supports both schema `1` and schema `2`.

Schema `1` remains the compatibility path for:

- `entrypoints.main` legacy `ModInitializer` startup
- `entrypoints.minecraftServer` Milestone 8 Minecraft bootstrap planning/execution
- protected-package compatibility fixtures that are still validated on older paths rather than the Runtime-1 schema-2 lifecycle path

Schema `2` adds:

- explicit lifecycle declarations using `ClassName::methodName`
- deterministic storage declarations for config/data/cache/generated directories
- `ModContext` delivery to lifecycle handlers
- explicit permissions lists, now compiled into Runtime-2 capability grants for Spindle-owned APIs only

Lifecycle declaration strings are validated before classloading. Runtime-1 also validates schema `2` handler signatures before any handler invocation. Supported phases are:

- `BOOTSTRAP`
- `CONFIGURE`
- `PRE_SERVER_MAIN`

## Compiled Profile

`spindle.profile.json` now writes schema version `3` and includes:

- `fingerprint`
- `inputFingerprint`
- `runtimePolicyFingerprint`
- cache status and cache reason
- metadata schema summary
- lockfile `mode` plus per-build `action` when available
- requested permissions plus compiled capability grants and summaries per mod
- lifecycle phase order and planned handlers
- per-mod owned storage/context plans
- runtime package policy summaries
- quality summary counts and score

The fingerprint terms are intentionally distinct:

- `fingerprint`: hash of the compiled profile contract itself
- `inputFingerprint`: hash of stable pre-classload inputs used to decide cache reuse
- `runtimePolicyFingerprint`: hash of the Runtime policy surface such as protected-package policy, capability catalog version, strict flags, and lifecycle phase set

The profile fingerprint remains deterministic. It excludes timestamps, process ids, raw absolute machine paths, cache hit/miss status, and other per-run cache metadata.

## Profile Cache

Runtime-1 stores cached compiled profiles under:

`<workingDirectory>/.spindle/profile-cache/<inputFingerprint>/spindle.profile.json`

The input fingerprint is derived from stable pre-classload inputs such as loader version, Java major version, game provider identity, resolved mod ids/versions/paths/hashes, resolved order, classpath entries, lockfile fingerprint, strict runtime flags, and the compiled-profile schema version. The cache directory key is the input fingerprint, not the full compiled profile fingerprint.

Equivalent repeated runs reuse the cached profile. Changed mod hashes, lockfile fingerprints, schema version changes, side changes, loader identity changes, or runtime policy changes invalidate the cache.

Cached profile reads validate schema version, profile kind, loader identity, game identity, input fingerprint, stored profile fingerprint, and runtime policy fingerprint before reuse. Cache outcomes are reported as deterministic `cache.status` and `cache.reason` values in the compiled profile and lifecycle report.

Diagnostics now record `runtime.compiled_profile.cache` with deterministic reasons such as:

- `missing profile`
- `unreadable profile`
- `schema mismatch`
- `profile kind mismatch`
- `loader mismatch`
- `game mismatch`
- `input fingerprint mismatch`
- `profile fingerprint mismatch`
- `runtime policy fingerprint mismatch`
- `cache hit`

## Standard Runtime Kernel

For the standard non-Minecraft path, lifecycle execution is driven from the compiled profile instead of rereading metadata after classloading.

Runtime-1 adds:

- `com.spindle.api.ModContext`
- `com.spindle.api.lifecycle.LifecyclePhase`
- deterministic owned storage planning under `config/`, `mod-data/`, `cache/mods/`, and `generated/`
- capability-aware `ModContext` storage access for granted storage surfaces only
- precomputed mod contexts built from the compiled profile
- lifecycle execution reports written from planned and executed handlers
- deterministic security validation reports written before standard lifecycle execution

Schema `1` `main` entrypoints remain available as legacy `BOOTSTRAP` handlers through a compatibility shim. Milestone 8 `minecraftServer` entrypoints remain on the Minecraft bootstrap path.

## Package Policy and Reports

Runtime-1 records runtime package policy in the compiled profile and writes `spindle.security-report.json` before standard lifecycle execution. Fatal Security-0 findings block schema `2` standard lifecycle execution before handler invocation. Legacy schema `1` fixtures remain on the compatibility path.

Protected package coverage now includes at least:

- `com.spindle.core`
- `com.spindle.api.internal`
- `net.minecraft`
- `org.spongepowered.asm`

Runtime-1 also writes:

- `spindle.security-report.json`
- `spindle.lifecycle-report.json`
- `spindle.quality-report.json`

These reports are deterministic and summarize planned lifecycle handlers, executed handlers, cache state, owned storage directories, duplicate resources, split packages, protected package findings, metadata findings, security findings, and quality score.

`spindle.security-report.json` is the Runtime trust-boundary report for the standard non-Minecraft path. It includes:

- `profileFingerprint`
- `inputFingerprint`
- `runtimePolicyFingerprint`
- `securityPolicyFingerprint`
- `executionIsolationMode`
- `runtimeExecutionIsolationMode`
- `sandboxed`
- `runtimeSandboxed`
- `sandboxClaim`
- `capabilityGrants`
- fatal and warning counts
- deterministic validated surfaces
- deterministic findings ordered by severity, rule id, mod id, and location

The report is explicit that Runtime-2 capability grants control Spindle-owned APIs only. Standard mod execution still remains `in-process-unrestricted-java` and `not-sandboxed`. Passing validation means the mod passed current Spindle boundary checks, not that the mod is generally safe.

`duplicateClasses` may be empty in the compiled profile when duplicate-class situations fail earlier as fatal ownership/package policy violations before a profile is written.

`spindle.lifecycle-report.json` now carries an explicit deterministic `state`:

- `planned`: planning completed and a lifecycle plan was written, but standard runtime execution has not completed
- `executed`: standard runtime lifecycle execution completed and attempted/successful/failed handler lists reflect that run

`spindle.quality-report.json` is an early deterministic signal, not a certification system. Runtime-2 now warns on non-granted requested capabilities while leaving granted storage surfaces quiet.

Milestone 8 Minecraft bootstrap remains separate. Security-0 does not claim that approved bootstrap mods are sandboxed, and not every Runtime-1 schema `2` rule applies to that bootstrap-only path yet.

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
- access wideners
- broad compatibility with existing mod ecosystems
