# Security And Trust Boundaries

Spindle mods are Java code loaded into the runtime process.

Spindle does not currently sandbox arbitrary runtime mods. A mod that passes validation is still executable code with the same broad process-level access that normal in-process Java code has.

## What Spindle Validates

For schema `2` standard runtime mods, Spindle validates a narrow non-invasive contract before lifecycle execution:

- local artifact hash identity from `spindle.lock.json`
- optional detached artifact signature sidecars
- warning-only static jar risk signals
- lifecycle declaration shape
- lifecycle handler signature
- loader-owned and protected package ownership
- shadowing of key Spindle API/core classes
- owned `ModContext` path planning
- runtime/profile/cache identity

If any fatal rule fails, Spindle writes `spindle.security-report.json` and blocks standard lifecycle execution before invoking handlers.

Warnings remain visible in the report but do not block execution.

## What Spindle Does Not Validate

Spindle does not currently claim:

- sandboxing
- malware detection
- registry or ecosystem provenance
- human review
- network or filesystem permission enforcement

Requested permissions are currently documentation and reporting signals only.

Static risk signals are also visibility signals only. A warning about reflection, networking, native libraries, or process APIs does not mean the mod is malware. It means the mod references something users and pack builders may want to review and document.

Spindle does verify a small loader-native signature format when a `.spindle-signature.json` sidecar is present. That is artifact identity verification, not a broader safety verdict.

## Schema `2` Contract

Use schema `2` when building Spindle-native Runtime-1 mods.

The intended shape is:

- lifecycle handlers declared as `ClassName::methodName`
- handler methods declared as `public static void methodName(com.spindle.api.ModContext)`
- mod-owned Java packages
- `ModContext` directories used for config, data, cache, and generated output
- logical relative storage paths that stay under the working directory

Preferred package direction:

- good: `com.example.mymod`
- bad: `com.spindle.core`
- bad: `com.spindle.api`
- bad: `net.minecraft`
- bad: `org.spongepowered.asm`

Preferred storage direction:

- good: `config/my_mod`
- good: `mod-data/my_mod`
- bad: `../outside`
- bad: absolute paths

## Hard Failures And Warnings

Hard failures:

- malformed, unsupported, or invalid claimed artifact signatures
- protected or loader-owned package definitions
- shadowed Spindle API/core classes
- invalid schema `2` lifecycle shape or signature
- escaped or non-logical owned paths
- runtime/profile identity drift

Warnings:

- unsigned local jars
- lockfile-hash identity without publisher identity
- provenance not present
- cache rebuilds after cached-profile validation failure
- requested permissions that Spindle records but does not yet enforce
- static risk signals for suspicious APIs, native files, service-provider files, nested jars, or malformed class entries

## Developer Ergonomics

Unsigned local mods remain easy to build and run. Spindle does not require signing for local Runtime-1 development in this pass.

If you place a sidecar beside a jar, Spindle treats that as an explicit trust claim and verifies it. Invalid claimed trust is fatal because the artifact asserted publisher identity and failed validation.

That convenience does not mean the mod is sandboxed. The report is explicit about the trust model so local development stays ergonomic without overstating security guarantees.

The Security-2 scanner reads jar entries and class constant-pool UTF-8 strings only. It does not add mod jars to the scanner classpath, does not use reflection to inspect mod classes, and does not execute mod code while producing these warnings.

Prefer `ModContext` directories over ad hoc file paths. They keep the runtime contract readable, deterministic, and within the working-directory boundary that Spindle can validate.
