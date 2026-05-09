# Runtime-2 Capability Grant Contract

`Runtime-2: Capability Grant Contract` introduced deterministic capability planning for Spindle-owned APIs. Runtime-3 keeps that contract and extends it with real service capabilities.

Capability grants still are not a Java sandbox:

- standard runtime mods still run as `in-process-unrestricted-java`
- grants only control Spindle-owned API surfaces
- broad Java behaviors remain visibility-only disclosures

## Current Grantable Capabilities

Runtime-3 can grant:

- `storage.config`
- `storage.data`
- `storage.cache`
- `storage.generated`
- `service.provide`
- `service.consume`

Storage grants come from matching `storage` booleans.

Service grants come from matching `services.provides` and `services.consumes` declarations, even when the capability is not explicitly listed in `permissions`.

## States

Capability states remain:

- `granted`
- `denied`
- `unavailable`
- `unknown`
- `visibility-only`

`service.provide` is denied when requested without any `services.provides` entries.

`service.consume` is denied when requested without any `services.consumes` entries.

## Profile And Reports

`spindle.profile.json` now writes schema version `4`.

The `permissions` section still records:

- `catalogVersion`
- `scope`
- `runtimeExecutionIsolationMode`
- `sandboxed`
- per-mod `grants`
- per-mod summaries
- global summary

Runtime-3 also adds a top-level `services` section for the deterministic service contract.

`spindle.security-report.json` still includes `capabilityGrants`, and the runtime posture fields remain explicit:

- `executionIsolationMode: "in-process-unrestricted-java"`
- `runtimeExecutionIsolationMode: "in-process-unrestricted-java"`
- `sandboxed: false`
- `runtimeSandboxed: false`
- `sandboxClaim: "not-sandboxed"`

## Non-goals

The capability contract still does not add:

- network sandboxing
- filesystem sandboxing
- process restrictions
- Java agent isolation
- bytecode instrumentation
- classpath scanning for services
- dependency injection
- config schema behavior
- resource overlay behavior
- SteelHook
- Minecraft hooks
