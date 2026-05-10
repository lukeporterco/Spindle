# Target-1: Minecraft Artifact Interpretation

Target-1 adds the first internal Minecraft artifact interpretation scaffold inside `target-minecraft`.

Its job is narrow: read planned server-side Minecraft runtime jars as bytecode artifacts and write a deterministic `minecraft-artifact-interpretation.json` report.

## Source Of Truth

The Minecraft artifact is the source of truth.

Online documentation, mappings, and Mache-style references may be useful supporting context for future work, but they are not the implementation input for Target-1.

Target-1 reads class-file structure directly from the runtime jars that Spindle plans to use.

## What Target-1 Does

Target-1:

- parses `.class` entries directly from jars
- reads class-file structure without class loading
- reports packages, classes, fields, methods, descriptors, and ownership layers
- preserves runtime artifact source and hash context
- writes a deterministic analysis-only report

The pass is intentionally limited to structure:

- class names
- package names
- super types
- interfaces
- field descriptors
- method descriptors
- access flags

This pass parses class-file structure, not source code.

## What Target-1 Does Not Do

Target-1 does not:

- install hooks
- identify hook points
- transform Minecraft
- patch bytecode
- load Minecraft classes
- execute Minecraft
- expose a public Modding API
- imply sandboxing

Hook contracts and hook installation remain future passes.

Target-2 is the first of those future passes. It consumes Target-1 interpreted symbols to validate explicit hook point contracts without installing hooks.

## Boundary Position

Target-1 is the bridge between runtime artifact planning and the future hook-contract and hook-installation work.

It answers:

> What classes, fields, methods, descriptors, packages, ownership layers, and runtime artifact sources does this Minecraft target contain?

It does not answer:

> Where should we inject?
>
> How do we inject?

Those questions belong to future hook-contract and hook-installation passes.
