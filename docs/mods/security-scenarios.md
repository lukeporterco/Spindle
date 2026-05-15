# Security Scenarios

These examples show how Runtime-4 capability grants, deterministic config and service contracts, trust-boundary validation, artifact trust, Security-2 static risk signals, and Security-3 restricted tooling behave today.

## Clean Lifecycle-Only Mod

A schema `2` mod declares lifecycle handlers, uses only `ModContext` storage, and does not reference the current static risk rules.

Result:

- `spindle.security-report.json` is written
- `state` is `validated`
- `riskSignals.summary.signalCount` is `0`
- `toolIsolation.status` is `passed`
- standard lifecycle execution proceeds

Interpretation:

- the mod still runs as unrestricted in-process Java code
- Spindle is not claiming sandboxing or safety
- the current static scanner did not find warning signals
- the static scanner still ran, but in a restricted child JVM that treated the jar as data

## Ada Builds A Local Unsigned Development Jar

Ada declares:

- schema `2`
- lifecycle handlers with `ClassName::methodName`
- `public static void methodName(com.spindle.api.ModContext)`
- mod-owned package names
- `ModContext` storage usage

Result:

- `spindle.security-report.json` is written
- `state` is `validated`
- `artifactTrust.entries[*].trustState` is `local-unsigned`
- Spindle writes warning `SEC-TRUST-001`
- Spindle writes warning `SEC-TRUST-006`
- standard lifecycle execution proceeds

Interpretation:

- local development stays ergonomic
- the jar is runnable
- Spindle is not claiming publisher identity or sandboxing

## Developer Accidentally Uses `net.minecraft`

A developer puts mod code in `net.minecraft.example`.

Result:

- Spindle writes a fatal `SEC-PACKAGE-002`
- the report explains that `net.minecraft` is protected
- standard lifecycle execution is blocked before handler invocation

Likely fix:

- move the code into a mod-owned package such as `com.example.mymod`

## Developer Shadows `com.spindle.api.ModContext`

A developer bundles a class named `com.spindle.api.ModContext`.

Result:

- Spindle writes a fatal `SEC-CLASS-001`
- the report explains that the class shadows a known Spindle API/core class
- standard lifecycle execution is blocked

Likely fix:

- rename the class and use the real Spindle API type instead of redefining it

## Developer Requests Unsupported Capabilities

A developer declares permissions such as:

- `filesystem.write`
- `network.outbound`

Result:

- Spindle writes `SEC-PERM-001` warnings
- the report marks the requests as `visibility-only`
- execution still proceeds if no fatal findings exist

Important:

- Spindle is not sandboxing or enforcing those Java behaviors

## Developer Enables Storage And Receives A Grant

A developer declares:

- `permissions: ["storage.data"]`
- `storage.data: true`

Result:

- `spindle.profile.json` records `storage.data` as `granted`
- `ModContext.dataDirectory()` is available at runtime
- Spindle does not emit `SEC-PERM-001` for that capability

Important:

- this grant controls a Spindle-owned API surface only
- the mod still runs as unrestricted in-process Java

## Developer Declares Read-Only Config

A developer declares:

- `storage.config: true`
- one or more `config.entries`
- `permissions: ["config.read"]`

Result:

- `spindle.profile.json` records `config.read` as `granted`
- Spindle writes `config/<modId>/config.json` if it is missing
- lifecycle code can read only the declared keys through `ModContext.config()`

Important:

- this is a Spindle-owned API contract only
- the mod still runs as unrestricted in-process Java

## Developer Declares Invalid Config

A config file contains the wrong type, an out-of-range number, or a string outside `allowed`.

Result:

- `spindle.quality-report.json` records a fatal config finding
- standard lifecycle execution is blocked before classloading
- Spindle does not rewrite the invalid file

Important:

- this is a runtime contract failure, not a sandbox claim

## Developer Declares An Optional Service Consumer

A developer declares:

- `services.consumes` with `required: false`
- no matching provider is present

Result:

- `spindle.profile.json` records `optional-unbound`
- `spindle.quality-report.json` records warning `service.optional_unbound`
- standard lifecycle execution still proceeds

Important:

- optional consumers do not block execution
- the mod still runs as unrestricted in-process Java

## Duplicate Providers Are Rejected

Two mods both declare the same service id in `services.provides`.

Result:

- `spindle.profile.json` records provider state `conflict`
- consumer bindings for that id record `provider-conflict`
- standard lifecycle execution is blocked before handler invocation

Important:

- Runtime-3 has no provider priority model yet
- this is a runtime contract failure, not sandboxing

## Networked Mod Uses HTTP Intentionally

A mod checks a remote update feed or downloads pack metadata and therefore references `java.net` or `java.net.http`.

Result:

- Spindle writes warning `RISK-NETWORK-001`
- the detailed `riskSignals.signals[*]` entry points at the class entry and evidence string
- `toolIsolation.mode` is `restricted-child-jvm`
- execution still proceeds if no fatal findings exist

Likely follow-up:

- document which endpoints the mod contacts
- remove unused networking code if the dependency is accidental

## Utility Mod Uses Reflection For Serialization

A utility mod uses reflection for JSON binding or serialization glue.

Result:

- Spindle writes warning `RISK-REFLECTION-001`
- the warning does not claim the mod is malicious
- execution still proceeds if no fatal findings exist

Likely follow-up:

- document why reflection is needed
- remove unused reflective helpers if they are no longer required

## Mod Bundles A Native Library

A mod ships `.dll`, `.so`, `.dylib`, or `.jnilib` files, or it references `System.load` or `System.loadLibrary`.

Result:

- Spindle writes warning `RISK-NATIVE-001`
- the report treats this as a stronger warning because native code sits outside the normal Java boundary
- execution still proceeds if no fatal findings exist

Likely follow-up:

- avoid native bundling when possible
- document supported platforms and why native code is required

## Ben Ships A Signed Release

Ben ships `mods/example.jar` with `mods/example.jar.spindle-signature.json`.

Result:

- the sidecar is parsed and verified with Ed25519
- `artifactTrust.entries[*].trustState` is `signed-artifact`
- the report records `signerId`
- execution proceeds if no other fatal findings exist

Interpretation:

- Spindle verified a loader-native publisher claim for that exact jar hash
- this still does not imply malware review or sandboxing

## Cora Tampers With A Signed Jar

Cora changes the jar bytes after the sidecar was generated.

Result:

- Spindle writes fatal `SEC-TRUST-003` if the sidecar hash no longer matches the jar
- Spindle writes fatal `SEC-TRUST-004` if the claimed signed fields or signature no longer verify
- standard lifecycle execution is blocked before handler invocation

Likely fix:

- restore the original jar
- or regenerate the sidecar for the exact artifact being shipped

## Drew Uses A Platform Download Without A Spindle Signature

Drew downloads a jar from Modrinth or CurseForge, but it does not include a Spindle sidecar.

Result:

- first local run usually reports `local-unsigned`
- once the lockfile is established and verified on later runs, the report can show `locked-hash`
- Spindle does not invent a fake platform trust claim

Interpretation:

- repeatable hash identity is useful
- publisher identity is still absent unless the jar is separately signed for Spindle

## Erin Relies On `spindle.lock.json` For Repeatable Artifact Identity

Erin builds a modpack and keeps `spindle.lock.json` under source control.

Result:

- verified runs keep reporting current hash identity
- unsigned artifacts can move from `local-unsigned` to `locked-hash`
- Spindle writes warning `SEC-TRUST-005`
- Spindle still warns with `SEC-TRUST-006` because publisher provenance is not present

Interpretation:

- lockfiles are good for deterministic artifact identity
- lockfiles are not the same as publisher signatures

## Local Unsigned Mod Still Runs

A developer builds a local unsigned schema `2` mod that stays inside the trust-boundary rules.

Result:

- Spindle can run the mod
- the report can show `local-unsigned` on the first write and `locked-hash` after later verified runs
- the security report still says `sandboxed: false`
- the security report still says `sandboxClaim: "not-sandboxed"`

Interpretation:

- the mod passed Spindle boundary validation
- the mod is not declared safe
- the mod is still unrestricted in-process Java code

## Suspicious-Looking Mod Uses `ProcessBuilder` And Bundles A Nested Jar

A mod references `ProcessBuilder` and also ships `libs/payload.jar` inside its jar.

Result:

- Spindle writes warning `RISK-PROCESS-001`
- Spindle writes warning `RISK-EMBEDDED-JAR-001`
- the report makes both signals visible in `riskSignals.signals`
- execution still proceeds if no fatal findings exist

Important:

- multiple warnings can look concerning
- Spindle still does not claim that these warnings prove malware
- users and pack builders should review and document why the mod needs those behaviors

## Worker Fails Before Producing Trusted Output

A user hits a reproducible static-tooling failure, such as invalid worker output or a broken local child-JVM invocation.

Result:

- Spindle writes fatal `SEC-TOOL-001`
- `toolIsolation.status` is `failed`
- `toolIsolation.worker` is `static-risk-scan`
- standard lifecycle execution is blocked before handler invocation

Likely fix:

- rerun Spindle
- inspect concise diagnostics
- clear `.spindle/security-tools`
- report a Spindle bug if the failure is reproducible

Important:

- this is not runtime sandboxing
- Spindle fails closed because silently skipping security tooling would be misleading
