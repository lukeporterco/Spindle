# Foundation Hardening: Loader API and Runtime Safety Fixes

This is a hardening pass document. It records edge-case fixes inside existing Loader API and runtime contracts without changing public contract versions or adding new capabilities.

## Inputs

- Existing Runtime API version `1`, compiled profile schema `6`, runtime closure contract version `2`, capability catalog version `2`, security policy version `8`, and permission policy version `5`.
- Existing config, service, restricted tooling, Minecraft version-id, and static-risk scanning behavior.

## Output

- Corrected config numeric behavior, deterministic restricted-tool request passing, safe Minecraft version path components, bounded static-risk scanning, and concurrent lazy singleton service behavior.

## Capability Added Or Recorded

- Hardens stale or unsafe edge behavior within the existing contracts.
- Keeps runtime honesty unchanged.

### Preserved Source Notes

This pass fixes edge cases only. It does not add new Loader API features, target integration, metadata fields, or new runtime capabilities.

The hardening work keeps existing public/runtime contract versions unchanged:

- Runtime API version remains `1`.
- Compiled profile schema remains `6`.
- Runtime closure contract version remains `2`.
- Capability catalog version remains `2`.
- Security policy version remains `8`.
- Permission policy version remains `5`.

Runtime honesty is unchanged:

- Java mod execution remains `in-process-unrestricted-java`.
- `sandboxed` remains `false`.
- `sandboxClaim` remains `not-sandboxed`.

This pass hardens:

- schema-2 config integer handling so `integer` always means signed 32-bit
- runtime config writes so non-finite `setNumber(...)` values are rejected
- lazy singleton service creation so concurrent consumers still get exactly one instance
- restricted static-risk tooling input by switching worker requests to deterministic JSON
- Minecraft version ids so cache paths only use safe path components
- static risk scanning so oversized class entries are warned and skipped instead of read without a cap

No schema versions were bumped because these changes correct stale or unsafe edge behavior inside the existing contracts.

## Boundaries Preserved

- Does not add Loader API features, target integration, metadata fields, runtime capabilities, or schema bumps.
- Java mod execution remains `in-process-unrestricted-java`, `sandboxed: false`, and `not-sandboxed`.

## Follow-On Direction

- Future passes can rely on these corrected edge semantics without treating them as new API or new runtime capability.
