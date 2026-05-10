# Loader API-0 Public Runtime API Boundary

`Loader API-0: Public Runtime API Boundary` stabilizes the runtime-facing `loader-api` surface that Spindle mods program against today.

This pass stabilizes:

- mod identity and runtime lifecycle context
- granted capability inspection
- owned storage access
- Runtime-4 config access
- Runtime-3 service access
- public API metadata and public unchecked API exceptions

This pass does not:

- seal the full Spindle mod loader API
- stabilize `com.spindle.api.minecraft.*`
- add Minecraft target integration
- add resource, registry, command, networking, event, hook, or data generation APIs

## Runtime Honesty

Loader API-0 does not change runtime honesty fields.

- `targetModel: "minecraft-as-target-not-foundation"`
- `runtimeExecutionIsolationMode: "in-process-unrestricted-java"`
- `sandboxed: false`
- `sandboxClaim: "not-sandboxed"`

Passing validation is still not a sandbox claim.

## Public Metadata

`com.spindle.api.LoaderApi` exposes stable metadata for the runtime-facing boundary:

- `RUNTIME_API_VERSION = 1`
- `API_STATUS = "runtime-api-stabilized"`
- `API_SCOPE = "runtime-facing-loader-api"`
- `TARGET_MODEL = "minecraft-as-target-not-foundation"`
- `SANDBOXED = false`
- `SANDBOX_CLAIM = "not-sandboxed"`

These fields are API metadata, not runtime feature flags.

## Public Exceptions

Loader API-0 adds a small unchecked public exception surface:

- `SpindleApiException`
- `CapabilityDeniedException`
- `ConfigAccessException`
- `ServiceAccessException`

These exceptions describe mod-facing API failures after Spindle runtime gates have already passed.

## Boundary Status

Runtime-5 prepared the loader API boundary inventory.

Loader API-0 updates that inventory to:

- `status: "runtime-api-stabilized"`
- `nextArc: "Loader API Hardening"`

The stabilized runtime-facing candidate list covers:

- `com.spindle.api.LoaderApi`
- `com.spindle.api.ModContext`
- `com.spindle.api.ModInitializer`
- `com.spindle.api.config.ModConfig`
- `com.spindle.api.exception.CapabilityDeniedException`
- `com.spindle.api.exception.ConfigAccessException`
- `com.spindle.api.exception.ServiceAccessException`
- `com.spindle.api.exception.SpindleApiException`
- `com.spindle.api.lifecycle.LifecyclePhase`
- `com.spindle.api.service.ServiceRegistry`

Deferred review remains:

- `com.spindle.api.minecraft.MinecraftServerModContext`
- `com.spindle.api.minecraft.MinecraftServerModInitializer`

## Non-Surfaces

Loader API-0 keeps these surfaces unavailable:

- `resource.declare`
- `resource.overlay`

Loader API-0 prepares the next pass to harden public API guarantees without expanding into target-specific Minecraft APIs.

## Hardening Follow-Up

Loader API Hardening keeps the same Runtime API version `1` and compiled profile schema `6` while adding compatibility tests, source-tree drift checks, Runtime-5 boundary/security shadowing sync checks, and clearer developer-facing failure documentation.
