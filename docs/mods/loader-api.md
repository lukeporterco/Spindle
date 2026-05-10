# Loader API

Loader API-0 stabilizes the runtime-facing `loader-api` surface only.

Stable today:

- `com.spindle.api.LoaderApi`
- `com.spindle.api.ModContext`
- `com.spindle.api.ModInitializer`
- `com.spindle.api.config.ModConfig`
- `com.spindle.api.exception.*`
- `com.spindle.api.lifecycle.LifecyclePhase`
- `com.spindle.api.service.ServiceRegistry`

Deferred:

- `com.spindle.api.minecraft.*`

Spindle keeps Minecraft as a target, not the foundation. Java mod execution also remains `in-process-unrestricted-java`, with `sandboxed = false` and `sandboxClaim = "not-sandboxed"`.

Loader API Hardening keeps Runtime API version `1` and compiled profile schema `6`. It adds compatibility checks and documentation only; it does not add new API features, seal the full mod loader API, or stabilize `com.spindle.api.minecraft.*`.

## ModContext

Lifecycle handlers receive `ModContext` in schema `2` runtime flows.

Use it to inspect identity, capabilities, config, services, and owned directories:

```java
context.modId();
context.grantedCapabilities();
context.config();
context.services();
```

Use `requireCapability(...)` before capability-gated access when you want an explicit assertion:

```java
context.requireCapability("storage.generated");
Path generated = context.generatedDirectory();
```

If the capability was not granted, Spindle throws `CapabilityDeniedException`.

Owned storage access remains limited to:

- `storage.config`
- `storage.data`
- `storage.cache`
- `storage.generated`

`resource.declare` and `resource.overlay` remain unavailable.

## Failure Behavior

| Exception | Raised for |
| --- | --- |
| `CapabilityDeniedException` | Missing capability grants on `requireCapability(...)` and storage access. |
| `ConfigAccessException` | Unavailable config, undeclared keys, type mismatch, denied writes, `runtimeWrites` false, invalid setter values, and config persistence failure. |
| `ServiceAccessException` | Unavailable registry, undeclared service, required unbound service, `require(...)` on optional unbound service, provider conflict, type mismatch, provider load failure, and provider instantiation failure. |

`ModConfig.empty()` throws `ConfigAccessException` with `modId() == "unavailable"`.

`ServiceRegistry.empty()` throws `ServiceAccessException` with `modId() == "unavailable"` from `require(...)`. `find(...)` on an empty registry returns `Optional.empty()`.

## Config

`ModConfig` exposes only declared flat schema-2 config entries.

```java
boolean enabled = context.config().getBoolean("enabled");
String mode = context.config().getString("mode");
```

`keys()` returns deterministic sorted order when provided by Spindle.

Config access throws `ConfigAccessException` for:

- undeclared keys
- wrong getter or setter type
- denied writes
- writes when `runtimeWrites` is false
- invalid values

## Services

`ServiceRegistry` exposes only declared consumed services.

```java
GreetingService service =
    context.services().require("sample:greeting", GreetingService.class);
```

`availableServiceIds()` returns deterministic sorted order when provided by Spindle.

Service access throws `ServiceAccessException` for:

- undeclared service access
- required unbound services
- `require(...)` on optional unbound services
- provider conflict or type mismatch access failures

`find(...)` returns `Optional.empty()` only for optional unbound declared services.
