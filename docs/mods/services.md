# Services

Runtime-3 services are declared in `loader.mod.json`.

They are Spindle-owned runtime APIs, not dependency injection and not a Java sandbox.

## Metadata

Schema `2` mods may declare:

```json
{
  "services": {
    "provides": [
      {
        "id": "sample:greeting",
        "type": "com.example.api.GreetingService",
        "implementation": "com.example.impl.DefaultGreetingService"
      }
    ],
    "consumes": [
      {
        "id": "sample:greeting",
        "type": "com.example.api.GreetingService",
        "required": true
      }
    ]
  }
}
```

Runtime-3 rules:

- Spindle does not scan classes for providers.
- Spindle does not instantiate providers during planning or validate-only runs.
- Spindle does not instantiate providers before the security gate.
- Spindle does not sandbox provider constructors.
- Service providers remain lazy singletons.
- Lazy singleton creation is thread-safe when multiple consumers resolve the same bound service concurrently.
- A mod can only access services it declares in `services.consumes`.

## Access

Lifecycle code uses `ModContext.services()`:

```java
GreetingService service =
    context.services().require("sample:greeting", GreetingService.class);
```

Behavior:

- `require(...)` returns the bound singleton or throws `ServiceAccessException` if the service is unavailable.
- `find(...)` returns `Optional.empty()` only for optional unbound declared services or the empty fallback registry.
- undeclared `require(...)` and `find(...)` calls fail clearly with `ServiceAccessException`
- `availableServiceIds()` reports only bound declared services in deterministic sorted order

Mod-facing service API failures use `ServiceAccessException`, including unavailable registries, undeclared services, required unbound services, `require(...)` on optional unbound services, provider conflicts, type mismatches, provider load failures, and provider instantiation failures.

## Binding Outcomes

- Duplicate providers are fatal in Runtime-3 because no priority model exists yet.
- Required consumers block execution when unbound.
- Optional consumers do not block execution when unbound.
- Type matching is exact string matching during planning.
- Runtime instantiation still verifies assignability before returning the instance.

This services hardening pass does not change Runtime API version `1`, compiled profile schema `6`, or Runtime-3 planning semantics beyond thread-safe lazy singleton creation.
