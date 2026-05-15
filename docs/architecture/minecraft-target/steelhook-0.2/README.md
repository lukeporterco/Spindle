# SteelHook 0.2 Target Passes

This folder records the opening SteelHook 0.2 Target passes after SteelHook 0.1 completion and Target-22 registry arc hardening.

Current status: SteelHook 0.2 starts with one approved primitive-boundary analysis path. It is about selecting one bounded real-runtime primitive direction, not about arbitrary bytecode mutation.

Target-23 is analysis-only. It selects the first approved primitive boundary from the existing Target-7 method-entry static-dispatch proof, writes a deterministic report, and explicitly keeps runtime transformation unimplemented.

SteelHook 0.2 still does not transform real Minecraft classes, expose a public API, or claim Java mod execution sandboxing.
