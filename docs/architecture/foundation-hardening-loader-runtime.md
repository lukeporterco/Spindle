# Foundation Hardening: Loader API and Runtime Safety Fixes

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
