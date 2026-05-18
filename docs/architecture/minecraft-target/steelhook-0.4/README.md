# SteelHook 0.4 Target Passes

This folder records the SteelHook 0.4 Target arc beginning with Target-32.

Current status: SteelHook 0.4 is not complete. Target-32 begins the arc as an analysis-only primitive-boundary pass over the completed Target-31 SteelHook 0.3 handoff. It approves only planned internal primitive families and does not transform bytecode, define transformed classes, install hooks, execute dispatchers, invoke Minecraft, launch a server, expose public APIs, or claim Java mod execution sandboxing.

Planned arc:

- Target-32: primitive boundary definition
- Target-33: bounded return-value interception offline evidence
- Target-34: bounded invoke redirect and invoke wrap offline evidence
- Target-35: isolated gated runtime class-definition proof for approved primitives
- Target-36: SteelHook 0.4 completion verification
