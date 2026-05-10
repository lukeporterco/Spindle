# Target Minecraft

`target-minecraft` is the current partial Minecraft Target Layer.

It owns Minecraft artifact handling, runtime planning, artifact interpretation reports, boundary reports, integration plans, bootstrap execution, and managed server-process helpers. It consumes the completed Spindle Loader subsystem but does not redefine the Spindle ecosystem brand or expose Minecraft behavior through the stable loader API.

## Future Target Layer API boundary

The next boundary-prep arc is documented in [Target Layer API Boundary](../docs/architecture/target-layer-api-boundary.md).

The first planned subsystem in that arc is the Injection Hook Subsystem, which remains inside `target-minecraft` and is not a standalone public API.

## Target-1 artifact interpretation

Target-1 adds an internal artifact interpretation scaffold before any hook contracts or hook installation work.

It reads planned server-side Minecraft runtime jars as bytecode artifacts, parses class-file structure without class loading, and writes `minecraft-artifact-interpretation.json`.

Target-1 is analysis-only:

- it does not install hooks
- it does not identify hook points
- it does not transform Minecraft
- it does not imply sandboxing

See [Target-1: Minecraft Artifact Interpretation](../docs/architecture/target-1-minecraft-artifact-interpretation.md).
