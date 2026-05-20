# Spindle-Ready Minecraft 26.1.2 Research Data

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

This folder is a research-local Spindle handoff package. It is not a Spindle repository import, and this pass does not modify the Spindle repository.

Expected later import destination:

```text
docs/architecture/minecraft-target/v26.1.2-data/
```

The original `internals-analysis/` research data remains intact. This handoff is additive: it refines the existing concepts, candidates, evidence, matrices, notes, runtime probe output, and Spindle translation files into a smaller package for future Target Layer and SteelHook planning.

## Contents

- `import-manifest.md`: instructions for a later Spindle-repo documentation import.
- `source-preservation-notes.md`: preservation boundary for the original research data.
- `refined/`: Spindle-facing synthesis, decision matrices, runtime confirmation, SteelHook primitive requirements, unresolved questions, and implementation packets.

## Boundaries

The dedicated runtime probe evidence is included in the refined synthesis, but it is dedicated-server-only. Integrated server parity is not confirmed.

This data guides future Target Layer and SteelHook passes. It does not implement public Minecraft APIs, runtime hooks, SteelHook primitives, bytecode transformation behavior, or Minecraft execution behavior.

SteelHook remains internal machinery for Spindle implementation planning, not a public arbitrary bytecode API.
