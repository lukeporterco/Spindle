# Target-14: Command Dispatcher Symbol Analysis

This is an analysis-only symbol analysis pass document for the Minecraft Target Layer. It records what Target-14 adds or decides while preserving the current Minecraft, SteelHook, and public API boundaries.

## Inputs

- Target-13 command boundary analysis.
- Target-1 interpreted class, field, and method metadata.

## Output

- Deterministic command dispatcher symbol analysis report.
- `minecraft-command-dispatcher-symbol-analysis.json`.

## Capability Added Or Recorded

- Selects command dispatcher metadata candidates from interpreted symbols.
- Records whether candidate evidence is unique without claiming binding readiness.

### Preserved Source Notes

Target-14 is analysis-only.

This pass exists because Target-13 identified the future command registration boundary but did not bind a Minecraft command dispatcher symbol.

Target-14 scans only Target-1 interpreted metadata for Brigadier `CommandDispatcher` descriptor references:

- class internal names
- field descriptors
- method descriptors
- field names
- method names
- access flags

It writes one deterministic report:

- `minecraft-command-dispatcher-symbol-analysis.json`

### What Target-14 Decides

Target-14 answers whether a future minimal command registration proof is eligible to proceed.

That future proof may be considered eligible only when exactly one selectable command dispatcher target is discovered from interpreted metadata and the Target-13 upstream gate remains available.

Target-14 does not guess from names alone, does not inspect bytecode instructions, and does not use mappings, decompiled source, or live classes as a source of truth.

### What Target-14 Does Not Add

No command registration occurs.
No command execution occurs.
No command tree is read or mutated.
No Brigadier dependency is added.
No hook contract is added for command classes.
No public command API exists.
No public Modding API exists.
No new SteelHook primitive is added.
No runtime callback is added.
Java mod execution is not sandboxed.

This pass does not add:

- Brigadier adapters
- dispatcher mutation
- command contribution models
- public `com.spindle.api.minecraft.*`
- hook placement
- bytecode analysis
- patch planning
- hook installation
- bootstrap behavior
- runtime transformation
- SteelHook completion behavior
- registry/content registration
- command reload behavior
- client support

## Boundaries Preserved

- Does not add Brigadier integration, public command APIs, runtime callbacks, new SteelHook primitives, real Minecraft transformation, command execution, or sandboxing.

## Follow-On Direction

- Target-15 can classify whether the selected metadata candidate is enough for binding.
