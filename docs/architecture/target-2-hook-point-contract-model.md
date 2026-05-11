# Target-2: Hook Point Contract Model

Target-2 adds the first internal hook point contract model inside `target-minecraft`.

Its job is narrow: validate explicit symbol-level hook contracts against the Target-1 interpreted Minecraft artifact structure and write a deterministic `minecraft-hook-contracts.json` report.

## Source Of Truth

Target-2 is grounded directly in Target-1 artifact interpretation.

Target-2 does not treat online documentation, mappings, Mache references, decompiled source, or inferred class names as the implementation input. It validates only against the interpreted symbols that Spindle already extracted from the planned Minecraft runtime jars.

## What Target-2 Does

Target-2:

- defines explicit internal contracts for class, method, constructor, and field symbols
- validates exact owner internal names, member names, descriptors, sides, and requirement levels
- reports whether each contract is valid, malformed, missing, optional, required, or side-incompatible
- writes a deterministic analysis-only `minecraft-hook-contracts.json` report
- uses an empty default hook contract catalog in this pass

This pass answers:

> Does an explicitly declared symbol-level hook contract match the Target-1 interpreted Minecraft artifact?

## What Target-2 Does Not Do

Target-2 does not:

- discover hook points automatically
- parse method instructions
- inspect callsites
- install hooks
- transform Minecraft
- patch bytecode
- load Minecraft classes
- execute Minecraft
- expose a public API
- imply sandboxing

Target-2 defines validation-only hook contracts. It does not decide where hooks should be placed inside bytecode and it does not perform installation behavior.

## Boundary Position

Target-2 sits directly after Target-1 artifact interpretation.

Target-1 reads class-file structure from the planned runtime jars.

Target-2 validates explicit hook contracts against that interpreted structure.

Future passes may add:

- real internal contract catalogs
- hook candidate classification
- bytecode-level placement validation
- hook installation behavior

Those future passes remain separate from the public Target Layer API and from any future Modding API work.

Target-3 builds directly on this model by selecting the first version-specific internal known-symbol catalog and validating it against the same Target-1 interpretation output.
