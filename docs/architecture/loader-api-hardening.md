# Loader API Hardening

`Loader API Hardening: Runtime API Compatibility and Developer Experience` hardens the Loader API-0 runtime-facing boundary without adding new loader features.

This pass preserves the existing public contract:

- Runtime API version remains `1`.
- Compiled profile schema remains `6`.
- Java mod execution remains `in-process-unrestricted-java`.
- `sandboxed` remains `false`.
- `sandboxClaim` remains `not-sandboxed`.
- `resource.declare` and `resource.overlay` remain unavailable.

## What It Verifies

Loader API Hardening adds compatibility tests for the stable Runtime API-0 surface:

- public Loader API metadata constants
- public unchecked exception inheritance, fields, and cause preservation
- predictable `ModContext` default helper behavior
- safe empty fallback behavior for `ModConfig` and `ServiceRegistry`
- deterministic and unmodifiable fallback collections
- explicit source-tree drift checks for stable non-Minecraft `loader-api` files

It also verifies that the Runtime-5 `loaderApiBoundary.stableCandidates` list stays synchronized with `SecurityPolicy.standard().knownShadowedClasses()`.

## What It Does Not Add

Loader API Hardening does not add new API features and does not seal the full mod loader API.

Minecraft-specific API under `com.spindle.api.minecraft.*` remains deferred. This pass does not add Minecraft target integration and does not stabilize target APIs.

The source-tree boundary test is a test-time drift check only. Runtime closure contracts remain explicit planner data; they are not generated from source scanning or reflection.
