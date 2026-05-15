# Target-15: Command Dispatcher Binding Analysis

This is an analysis-only binding analysis pass document for the Minecraft Target Layer. It records what Target-15 adds or decides while preserving the current Minecraft, SteelHook, and public API boundaries.

## Inputs

- Target-14 command dispatcher symbol analysis.
- Command registration concept boundary from Target-13.

## Output

- Deterministic command dispatcher binding analysis report.
- `minecraft-command-dispatcher-binding-analysis.json`.

## Capability Added Or Recorded

- Classifies selected command dispatcher metadata as a candidate, not as dispatcher access or registration readiness.

### Preserved Source Notes

Target-15 is analysis-only.

This pass consumes Target-14. It reads the in-memory `MinecraftCommandDispatcherSymbolAnalysis` result and writes one deterministic report:

- `minecraft-command-dispatcher-binding-analysis.json`

### What Target-15 Decides

Target-15 explains what a selected Target-14 dispatcher symbol candidate would mean for future binding work.

It classifies:

- method descriptor references
- static fields
- instance fields
- blocked upstream states
- no-candidate states
- ambiguous-candidate states

The key distinction is that unique symbol selection is not the same thing as command registration readiness. A selected metadata candidate still does not provide live dispatcher access.

SteelHook 0.1 method-entry dispatch is not enough to access a dispatcher value. The current proof chain cannot pass Minecraft values into a dispatcher callback, capture receivers, or read dispatcher fields.

### What Target-15 Does Not Add

Target-15 does not register commands.
Target-15 does not add Brigadier.
Target-15 does not add public APIs.
Target-15 does not add SteelHook primitives.
Target-15 does not read, expose, or mutate a command tree.
Target-15 does not add runtime transformation behavior.
Java mod execution is not sandboxed.

## Boundaries Preserved

- Does not add Brigadier integration, public command APIs, runtime callbacks, new SteelHook primitives, real Minecraft transformation, command execution, or sandboxing.

## Follow-On Direction

- Future command passes remain blocked until dispatcher access and registration timing are actually proven.
