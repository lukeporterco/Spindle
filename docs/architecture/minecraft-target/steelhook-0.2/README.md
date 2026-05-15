# SteelHook 0.2 Target Passes

This folder records the opening SteelHook 0.2 Target passes after SteelHook 0.1 completion and Target-22 registry arc hardening.

Current status: SteelHook 0.2 starts with one approved primitive-boundary analysis path. It is about selecting one bounded real-runtime primitive direction, not about arbitrary bytecode mutation.

Target-23 is analysis-only. It selects the first approved primitive boundary from the existing Target-7 method-entry static-dispatch proof, writes a deterministic report, and explicitly keeps runtime transformation unimplemented.

Target-24 is also analysis-only. It generalizes that one approved Target-23 candidate into explicit target, dispatcher, primitive-contract, and generalized patch-plan descriptors for Target-25, while still keeping runtime transformation blocked.

Target-25 extracts the reusable method-entry bytecode transformer and proves it through local/offline class-byte transformation. It still does not classload transformed bytes into Minecraft or make runtime transformation ready.

SteelHook 0.2 still does not transform real Minecraft classes, expose a public API, or claim Java mod execution sandboxing.
