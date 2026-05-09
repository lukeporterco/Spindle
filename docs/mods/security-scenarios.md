# Security Scenarios

These examples show how Runtime-1 trust-boundary validation and Security-1 artifact trust behave today.

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

## Developer Requests Unsupported Permissions

A developer declares permissions such as:

- `filesystem.write`
- `network.outbound`

Result:

- Spindle writes `SEC-PERM-001` warnings
- the report makes the request visible
- execution still proceeds if no fatal findings exist

Important:

- Spindle is not granting or enforcing those permissions yet

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
