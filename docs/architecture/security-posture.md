# Security Posture

Spindle runs mods as executable Java code.

Current Runtime-1 standard mod execution is:

- in process
- unrestricted Java
- not sandboxed

Passing Spindle security validation does not mean a mod is safe. It means the mod passed Spindle's current trust-boundary checks for the non-invasive Runtime-1 contract.

## Current Scope

Security-0 adds deterministic trust-boundary validation before schema `2` standard lifecycle execution.

It writes `spindle.security-report.json` with:

- stable rule ids
- fatal findings that block standard lifecycle execution
- warning findings that remain report-only
- explicit runtime posture fields

The report always states:

- `executionIsolationMode: "in-process-unrestricted-java"`
- `sandboxed: false`
- `sandboxClaim: "not-sandboxed"`

## Validated Surfaces

Runtime-1 validates only narrow Spindle-native boundaries:

- loader-owned package ownership
- protected platform and compatibility packages
- known Spindle API/core class shadowing
- schema `2` lifecycle declaration shape and handler signatures
- planned `ModContext` path boundaries
- compiled profile cache rebuild visibility
- requested permissions visibility
- runtime and compiled profile identity fingerprints

## Current Rules

- `SEC-PACKAGE-001`: schema `2` mod defines loader-owned package
- `SEC-PACKAGE-002`: schema `2` mod defines protected platform or compatibility package
- `SEC-CLASS-001`: mod shadows known Spindle API/core class
- `SEC-LIFECYCLE-001`: lifecycle declaration shape is invalid
- `SEC-LIFECYCLE-002`: lifecycle handler signature is invalid
- `SEC-PATH-001`: planned owned path escapes the working directory
- `SEC-PATH-002`: logical relative path contract is violated
- `SEC-CACHE-001`: cached compiled profile was rejected and rebuilt
- `SEC-PERM-001`: mod requests permissions Spindle records but does not enforce
- `SEC-ARTIFACT-001`: lockfile or artifact identity mismatch
- `SEC-METADATA-001`: metadata schema or security-relevant metadata field is invalid
- `SEC-RUNTIME-001`: runtime policy or compiled profile identity mismatch

## Non-goals

Security-0 does not add:

- signing
- provenance
- sandboxing
- restricted child-JVM execution for arbitrary runtime mods
- static bytecode risk scanning
- compatibility-layer claims

Milestone 8 Minecraft bootstrap remains a separate server-only path. Its bootstrap checks still apply, but Security-0 does not claim that bootstrap-approved Minecraft mods are sandboxed or generally safe either.
