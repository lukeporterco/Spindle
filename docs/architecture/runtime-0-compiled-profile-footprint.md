# Runtime-0 Compiled Profile Footprint

`Runtime-0: First Compiled Profile Footprint` adds a small deterministic runtime artifact at the post-planning seam introduced by Foundation-Caboose.

The loader now writes `spindle.profile.json` in the working directory beside `spindle.lock.json`, `spindle.report.json`, and `spindle.graph.json`.

The profile is built from the existing `ModpackPlanningResult`. It currently records:

- `schemaVersion: 1` and `profileKind: compiled-modpack`
- loader id/version
- game provider id/version/side
- resolved mods with ids, versions, normalized relative paths, and hashes
- resolved mod order
- mod runtime classpath entries with owning mod ids
- ownership summary counts for classes, packages, and duplicate resources
- lockfile path plus a stable lockfile fingerprint

The compiled profile fingerprint is deterministic. It is derived from stable planning outputs such as loader/game identity, resolved mods, resolved order, classpath ownership, ownership summary counts, and the lockfile fingerprint. It does not include timestamps, process ids, or raw machine-specific absolute paths.

Runtime-0 does not execute from the compiled profile, skip planning, or reuse a cached profile. Existing execution still runs from the current planning result and current execution flows.

Runtime-1 now attaches at this artifact. The compiled profile is authoritative for standard runtime lifecycle execution, owned storage planning, package policy summaries, quality/lifecycle reports, and deterministic profile cache reuse. See [runtime-1-compiled-runtime-kernel.md](./runtime-1-compiled-runtime-kernel.md).

This pass intentionally does not add schema v2 metadata, lifecycle declarations, services, config schemas, permissions, hook ids, SteelHook, injection, or real Minecraft hooks.
