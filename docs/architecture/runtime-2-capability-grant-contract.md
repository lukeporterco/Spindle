# Runtime-2 Capability Grant Contract

`Runtime-2: Capability Grant Contract` turns Runtime-1 permission recording into a deterministic grant plan for Spindle-owned APIs.

Runtime-2 is still not a Java sandbox.

- Standard runtime mods still run as in-process unrestricted Java.
- Capability grants only control whether Spindle exposes specific loader-owned API surfaces.
- Broad Java behaviors such as network, process, native loading, reflection, unsafe access, and broad filesystem claims remain visibility-only disclosures.

## Scope

Runtime-2 currently governs only these grantable capabilities:

- `storage.config`
- `storage.data`
- `storage.cache`
- `storage.generated`

These capabilities control `ModContext` directory access only.

If a schema `2` mod enables a matching `storage` flag in `loader.mod.json`, Spindle grants the matching storage capability even when the capability is not explicitly listed in `permissions`.

That preserves the Runtime-1 developer experience for mods that already relied on enabled `ModContext` storage.

## States

Runtime-2 compiles requested and derived capabilities into deterministic states:

- `granted`: Spindle exposes that specific API surface.
- `denied`: Spindle recognizes the capability, but the metadata does not satisfy the requirement.
- `unavailable`: Spindle reserves the capability for a future Runtime or Platform pass.
- `unknown`: Spindle does not recognize the string.
- `visibility-only`: Spindle records the declaration for review but does not enforce it.

## Profile And Reports

`spindle.profile.json` now writes schema version `3` and extends the `permissions` section with:

- `catalogVersion`
- `scope`
- `runtimeExecutionIsolationMode`
- `sandboxed`
- per-mod `grants`
- per-mod summaries
- global summary

`spindle.security-report.json` now includes a matching `capabilityGrants` section so the runtime posture stays honest.

The report still states:

- `executionIsolationMode: "in-process-unrestricted-java"`
- `runtimeExecutionIsolationMode: "in-process-unrestricted-java"`
- `sandboxed: false`
- `runtimeSandboxed: false`
- `sandboxClaim: "not-sandboxed"`

## Non-goals

Runtime-2 does not add:

- network sandboxing
- filesystem sandboxing
- process restrictions
- Java agent isolation
- SecurityManager usage
- bytecode instrumentation
- service registry behavior
- config schema behavior
- resource overlay behavior
- SteelHook
- Minecraft hooks
