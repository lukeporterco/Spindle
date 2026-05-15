# Target-22: Registry Arc Hardening and Synthesis

This is a hardening synthesis pass document for the Minecraft Target Layer. It records what Target-22 adds or decides while preserving the current Minecraft, SteelHook, and public API boundaries.

## Inputs

- Target-21 registry bootstrap/content registration analysis.
- Existing registry concept-family direction from the roadmap.

## Output

- Registry arc hardening and synthesis architecture record.
- `minecraft-registry-arc-hardening.json`.

## Capability Added Or Recorded

- Hardens the registry arc boundary and records the current synthesis state.

### Preserved Source Notes

Target-22 is analysis-only.

It consumes Target-20 and Target-21.

It writes `minecraft-registry-arc-hardening.json`.

It validates Target-21 invariants instead of doing more discovery.

It keeps registry implementation blocked.

It may recommend SteelHook 0.2 primitive design only as the next design activity.

It does not implement SteelHook 0.2.

It does not implement registries, content registration, public APIs, callbacks, hooks, runtime mutation, data generation, resource access, datapack access, mappings, decompiled source inspection, real Minecraft transformation, or sandboxing.

## Boundaries Preserved

- Does not make registry behavior implemented, public, or runtime-ready.
- Does not add content registration APIs, new SteelHook primitives, real Minecraft transformation, or sandboxing.

## Follow-On Direction

- Future registry work can proceed from the clarified boundary without treating registry behavior as implemented.
