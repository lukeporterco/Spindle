# Foundation-Caboose

`LoaderMain` used to own CLI parsing, argument resolution, modpack planning, standard launch, Minecraft planning and launch flows, diagnostics timing helpers, path display helpers, startup profiling, and child-process output handling. This pass splits those responsibilities so the entrypoint stays readable and future Runtime Arc work has a cleaner attachment point.

Current structure:

- CLI parsing and launch argument resolution live in `loader-core/src/main/java/com/mcmodloader/core/cli/`.
- Top-level application orchestration lives in `loader-core/src/main/java/com/mcmodloader/core/app/LoaderApplication.java`.
- Normal modpack planning lives in `loader-core/src/main/java/com/mcmodloader/core/pipeline/`.
- Standard non-Minecraft execution lives in `loader-core/src/main/java/com/mcmodloader/core/execution/`.
- Minecraft dry-run, baseline, launch, bootstrap, preflight, and reproducibility orchestration live in `loader-core/src/main/java/com/mcmodloader/core/minecraft/flow/`.
- Shared timing, diagnostic detail, display-path, startup-profile, and process-output helpers live in `loader-core/src/main/java/com/mcmodloader/core/report/` and `loader-core/src/main/java/com/mcmodloader/core/io/`.

The intended Runtime Arc seam is the modpack planning result returned by `ModpackPlanningPipeline`. Future `Runtime-1: Compiled Modpack Runtime` work should attach there by compiling the deterministic planning outputs into a reusable runtime profile before classloading or execution.

This pass does not add Runtime Arc features, compiled profiles, SteelHook, injection, fixture-dispatch expansion, or real Minecraft hooks. It is structural hardening only.
