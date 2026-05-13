# Target-20: Resource/Reload Arc Caboose and Registry Handoff Decision

Target-20 is analysis-only.

It consumes Target-16, Target-17, Target-18, and Target-19.

It closes the resource/reload arc for now.

It records the decision to move next to Registry Bootstrap and Content Registration.

It writes `minecraft-resource-reload-arc-decision.json`.

## What Target-20 Records

Target-20 adds one deterministic caboose decision report for the data, resources, reload, and future data generation concept family.

The pass confirms that:

- Target-16 named resource/reload boundaries and anchored them only to a coarse lifecycle boundary
- Target-17 discovered resource/reload metadata candidates without selecting a stable reload target
- Target-18 classified binding/access requirements without recommending a reload proof
- Target-19 separated runtime reload timing, runtime resource visibility, and future offline data generation
- the next concept direction should move to Registry Bootstrap and Content Registration rather than more resource/reload analysis or immediate SteelHook primitive design

## Decision Boundary

When Target-16 through Target-19 all pass, Target-20 records:

- `decisionStatus: RESOURCE_RELOAD_ARC_CABOOSED`
- `nextDirection: MOVE_TO_REGISTRY_BOOTSTRAP`
- `recommendedNextConceptId: "minecraft.concept.registry_bootstrap"`
- `recommendedNextMilestoneName: "Target-21"`
- `recommendedNextPassTitle: "Registry Bootstrap Boundary Analysis"`

When any upstream gate is blocked, Target-20 records an upstream-blocked decision and does not choose a new implementation path.

## What Target-20 Does Not Do

Target-20 does not implement registries.

Target-20 does not discover registry symbols.

Target-20 does not design registry APIs.

Target-20 does not mutate registries.

Target-20 does not design a new SteelHook primitive.

Target-20 does not implement reload handling.

Target-20 does not access resources or datapacks.

Target-20 does not generate data.

Target-20 does not write generated files.

Target-20 does not expose public APIs.

Target-20 does not add SteelHook primitives.

Target-20 does not transform real Minecraft runtime artifacts.

Java mod execution is not sandboxed.

## Follow-On Direction

Target-20 chooses registry bootstrap/content registration as the next concept family.

Target-21 may begin Registry Bootstrap and Content Registration boundary analysis.
