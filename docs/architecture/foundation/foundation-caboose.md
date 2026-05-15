# Foundation-Caboose: Foundation Arc Closure

This is a structural caboose document for the foundation arc. It records the split of `LoaderMain` responsibilities and the runtime seam that later Runtime passes build on.

## Inputs

- Existing `LoaderMain` orchestration responsibilities.
- Current loader-core CLI, planning, execution, Minecraft flow, report, and IO code.

## Output

- Clear responsibility split across CLI, application orchestration, planning, execution, Minecraft flow, report, and IO packages.
- A named attachment point for future Runtime Arc work at the `ModpackPlanningPipeline` result.

## Capability Added Or Recorded

- Records the foundation structure after the entrypoint split.
- Makes the post-planning seam explicit for compiled runtime profile work.

### Preserved Source Notes

`LoaderMain` used to own CLI parsing, argument resolution, modpack planning, standard launch, Minecraft planning and launch flows, diagnostics timing helpers, path display helpers, startup profiling, and child-process output handling. This pass splits those responsibilities so the entrypoint stays readable and future Runtime Arc work has a cleaner attachment point.

Current structure:

- CLI parsing and launch argument resolution live in `loader-core/src/main/java/com/spindle/core/cli/`.
- Top-level application orchestration lives in `loader-core/src/main/java/com/spindle/core/app/LoaderApplication.java`.
- Normal modpack planning lives in `loader-core/src/main/java/com/spindle/core/pipeline/`.
- Standard non-Minecraft execution lives in `loader-core/src/main/java/com/spindle/core/execution/`.
- Minecraft dry-run, baseline, launch, bootstrap, preflight, and reproducibility orchestration live in `loader-core/src/main/java/com/spindle/core/minecraft/flow/`.
- Shared timing, diagnostic detail, display-path, startup-profile, and process-output helpers live in `loader-core/src/main/java/com/spindle/core/report/` and `loader-core/src/main/java/com/spindle/core/io/`.

The intended Runtime Arc seam is the modpack planning result returned by `ModpackPlanningPipeline`. Future `Runtime-1: Compiled Modpack Runtime` work should attach there by compiling the deterministic planning outputs into a reusable runtime profile before classloading or execution.

This pass does not add Runtime Arc features, compiled profiles, SteelHook, injection, fixture-dispatch expansion, or real Minecraft hooks. It is structural hardening only.

## Boundaries Preserved

- Does not add Runtime Arc features, compiled profiles, SteelHook, injection, fixture-dispatch expansion, or real Minecraft hooks.

## Follow-On Direction

- Runtime work attaches after deterministic modpack planning and before classloading or execution.
