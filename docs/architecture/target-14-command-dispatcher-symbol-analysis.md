# Target-14: Command Dispatcher Symbol Analysis

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

## What Target-14 Decides

Target-14 answers whether a future minimal command registration proof is eligible to proceed.

That future proof may be considered eligible only when exactly one selectable command dispatcher target is discovered from interpreted metadata and the Target-13 upstream gate remains available.

Target-14 does not guess from names alone, does not inspect bytecode instructions, and does not use mappings, decompiled source, or live classes as a source of truth.

## What Target-14 Does Not Add

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
