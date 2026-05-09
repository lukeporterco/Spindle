# Runtime-3 Deterministic Service Registry

`Runtime-3: Deterministic Service Registry` turns `service.provide` and `service.consume` into real Spindle-owned runtime APIs.

Runtime-3 is not dependency injection, annotation scanning, parameter injection, Minecraft hooks, or sandboxing. Runtime Java execution still remains `in-process-unrestricted-java`.

## Contract

Schema `2` mods may now declare:

- `services.provides`
- `services.consumes`

Spindle compiles a deterministic service contract before lifecycle execution:

- providers are planned without classloading mod code
- consumers bind to exactly one usable provider or remain unbound with an explicit state
- duplicate providers are fatal because Runtime-3 does not define priorities
- required unbound consumers are fatal
- optional unbound consumers are warnings only

`spindle.profile.json` now writes schema version `4` and adds a top-level `services` section with:

- `contractVersion`
- `scope`
- `providerInstantiation`
- per-mod provider and consumer plans
- deterministic `bindings`
- a summary with fatal and warning counts

## Planning Rules

Provider states:

- `available`
- `conflict`
- `implementation-missing`
- `implementation-not-owned`

Consumer and binding states:

- `bound`
- `optional-unbound`
- `required-unbound`
- `provider-conflict`
- `type-mismatch`

Planning is deterministic:

- mods sort by `modId`
- providers sort by service id, type, then implementation
- consumers sort by service id, type, then required flag
- bindings sort by service id, consumer mod id, then provider mod id

Planning does not load classes and does not instantiate provider code.

## Runtime Behavior

At lifecycle time, mods access services through `ModContext.services()`.

Runtime-3 guarantees:

- a mod can only access services it declared in `services.consumes`
- undeclared access throws a clear `IllegalStateException`
- bound services instantiate lazily as singletons after the security gate and after `ModClassLoader.create(...)`
- provider constructors are runtime mod code execution and are not sandboxed
- providers must use a public no-arg constructor in Runtime-3

Validate-only runs still write:

- `spindle.profile.json`
- `spindle.quality-report.json`
- `spindle.lifecycle-report.json`
- `spindle.security-report.json`

Standard execution adds a Runtime-3 contract gate before lifecycle execution. If `profile.services().summary().fatalCount() > 0`, Spindle blocks startup with a runtime contract failure instead of a sandbox claim.
