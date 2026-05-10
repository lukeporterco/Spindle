# Loader API Boundary Prep

Runtime-5 records a deterministic loader API boundary inventory inside `spindle.profile.json.runtimeClosure.loaderApiBoundary`.

This document describes the preparation step only, not the stabilized boundary that Loader API-0 later adds.

## Boundary Status

Runtime-5 records:

- `status: "prepared-not-sealed"`
- `nextArc: "Loader API Arc"`

That means Spindle now has a code-backed inventory of current public loader-api surfaces without promising that the inventory is the final public contract.

Loader API-0 later updates that inventory to `status: "runtime-api-stabilized"` with `nextArc: "Loader API Hardening"`.

## Stable Candidates

Runtime-5 records these stable candidates:

- `com.spindle.api.ModContext`
- `com.spindle.api.ModInitializer`
- `com.spindle.api.config.ModConfig`
- `com.spindle.api.lifecycle.LifecyclePhase`
- `com.spindle.api.service.ServiceRegistry`

These are candidates for the upcoming Loader API Arc. Runtime-5 does not rename or redesign them.

Loader API-0 later stabilizes the runtime-facing boundary and expands the stable list to include `LoaderApi` and the public API exception types.

## Deferred Review

Runtime-5 defers review of:

- `com.spindle.api.minecraft.MinecraftServerModContext`
- `com.spindle.api.minecraft.MinecraftServerModInitializer`

Those classes stay deferred because target-specific APIs should not be sealed accidentally before the Minecraft target adapter work is clearer.

## Internal Exclusions

Runtime-5 also records internal packages that must not leak into `loader-api`:

- `com.spindle.api.internal`
- `com.spindle.core`

The boundary inventory is explicit on purpose. Runtime-5 does not use reflection to discover public classes because reflective inventory would let the apparent API drift with unrelated source changes.
