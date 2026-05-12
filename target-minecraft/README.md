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

## Target-2 hook contract diagnostics

Target-2 adds an internal hook point contract model and validation scaffold on top of Target-1.

It validates explicit class, method, constructor, and field contracts against interpreted server-side Minecraft symbols and writes `minecraft-hook-contracts.json`.

Target-2 is analysis-only:

- it does not discover hook points automatically
- it does not parse method instructions or inspect callsites
- it does not install hooks
- it does not transform Minecraft
- it does not expose a public API

The default Target-2 contract catalog is intentionally empty in this pass. The report still records schema, target, side, version, analysis-only flags, and diagnostics explaining that no hook contracts are declared yet.

See [Target-2: Hook Point Contract Model](../docs/architecture/target-2-hook-point-contract-model.md).

## Target-3 known-symbol hook validation

Target-3 adds the first internal known-symbol hook contract catalog on top of Target-2.

It selects a tiny catalog by Minecraft version and side, validates those symbols against Target-1 artifact interpretation, and writes `minecraft-hook-contracts.json` schema `2` with catalog metadata.

The only supported catalog in this pass is Minecraft `26.1.2` on the server side, and it contains only `net/minecraft/server/Main` plus `main([Ljava/lang/String;)V`.

Target-3 remains analysis-only and nonfatal:

- it does not parse method bytecode or inspect callsites
- it does not install hooks
- it does not transform Minecraft
- it does not expose a public API
- it does not imply sandboxing

See [Target-3: Non-Invasive Known Minecraft Symbol Hook Validation](../docs/architecture/target-3-known-symbol-hook-validation.md).

## Target-4 minimal hook installation proof

Target-4 adds the first internal launch-boundary hook installation proof.

It plans and installs exactly one wrapper around Minecraft `26.1.2` server `net.minecraft.server.Main.main(String[])`, writes `minecraft-hook-installation-plan.json`, passes that plan into the bootstrap child JVM, invokes Minecraft main through an internal bridge, and writes `minecraft-hook-installation-result.json`.

Target-4 remains intentionally narrow:

- no bytecode parsing or callsite inspection
- no transformation, patching, remapping, or Java agents
- no public hook API
- no gameplay hooks
- no sandbox claim for Java mod execution

See [Target-4: Minimal Launch-Boundary Hook Installation Proof](../docs/architecture/target-4-minimal-hook-installation-proof.md).
