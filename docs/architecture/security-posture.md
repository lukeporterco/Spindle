# Security Posture

Spindle runs mods as executable Java code.

Current Runtime-1 standard mod execution is:

- in process
- unrestricted Java
- not sandboxed

Passing Spindle security validation does not mean a mod is safe. It means the mod passed Spindle's current trust-boundary checks for the current non-invasive Runtime-1 contract.

## Current Scope

Security-0 adds deterministic trust-boundary validation before schema `2` standard lifecycle execution.

Security-1 adds deterministic artifact trust reporting for local jars, lockfile hash identity, and optional detached Ed25519 sidecars.

Security-2 adds warning-only static risk signals for resolved mod jars without classloading or executing mod code.

It writes `spindle.security-report.json` with:

- stable rule ids
- fatal findings that block standard lifecycle execution
- warning findings that remain report-only
- explicit runtime posture fields
- an `artifactTrust` section with per-artifact trust state and summary counts
- a `riskSignals` section with warning-only static jar and constant-pool evidence

The report always states:

- `executionIsolationMode: "in-process-unrestricted-java"`
- `sandboxed: false`
- `sandboxClaim: "not-sandboxed"`

## Validated Surfaces

Runtime-1 validates only narrow Spindle-native boundaries:

- artifact lockfile identity
- artifact trust sidecar shape and Ed25519 verification
- loader-owned package ownership
- protected platform and compatibility packages
- known Spindle API/core class shadowing
- schema `2` lifecycle declaration shape and handler signatures
- planned `ModContext` path boundaries
- compiled profile cache rebuild visibility
- requested permissions visibility
- runtime and compiled profile identity fingerprints
- static jar risk signals from constant-pool UTF-8 strings, native files, service-provider files, and nested jars

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
- `SEC-TRUST-001`: artifact is local unsigned
- `SEC-TRUST-002`: signature sidecar is malformed, unsupported, or unreadable
- `SEC-TRUST-003`: signature artifact hash does not match the jar
- `SEC-TRUST-004`: signature verification failed
- `SEC-TRUST-005`: artifact is locked by hash but has no publisher identity
- `SEC-TRUST-006`: provenance is not present
- `SEC-METADATA-001`: metadata schema or security-relevant metadata field is invalid
- `SEC-RUNTIME-001`: runtime policy or compiled profile identity mismatch
- `RISK-PROCESS-001`: process execution APIs are referenced
- `RISK-NATIVE-001`: native loading APIs or bundled native files are present
- `RISK-NETWORK-001`: network APIs are referenced
- `RISK-REFLECTION-001`: reflection or method-handle APIs are referenced
- `RISK-UNSAFE-001`: unsafe, internal, or foreign-memory APIs are referenced
- `RISK-DYNAMIC-CLASSLOAD-001`: dynamic classloading APIs are referenced
- `RISK-SCRIPT-001`: script execution APIs are referenced
- `RISK-SERVICE-001`: service-provider entries are present
- `RISK-EMBEDDED-JAR-001`: nested jar entries are present
- `RISK-CLASSFILE-001`: a class entry could not be statically scanned

## Non-goals

Security-2 still does not add:

- registry-backed publisher identity
- human review
- sandboxing
- restricted child-JVM execution for arbitrary runtime mods
- compatibility-layer claims

The current sidecar model is intentionally local and loader-native. A valid sidecar proves that a specific signer key signed a specific jar hash and the signed mod id/version. It does not claim ecosystem review, malware analysis, or platform endorsement.

The static risk scanner is intentionally limited. It reads jar entries and UTF-8 constant-pool strings only. It does not classload mods, execute mod code, or claim that a warning proves malicious intent.

Milestone 8 Minecraft bootstrap remains a separate server-only path. Its bootstrap checks still apply, but Security-0 does not claim that bootstrap-approved Minecraft mods are sandboxed or generally safe either.
