# Security Scenarios

These examples show how Runtime-1 trust-boundary validation behaves today.

## Clean Schema `2` Developer

A developer declares:

- schema `2`
- lifecycle handlers with `ClassName::methodName`
- `public static void methodName(com.spindle.api.ModContext)`
- mod-owned package names
- `ModContext` storage usage

Result:

- `spindle.security-report.json` is written
- `state` is `validated`
- standard lifecycle execution proceeds

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

## Local Unsigned Mod Still Runs

A developer builds a local unsigned schema `2` mod that stays inside the trust-boundary rules.

Result:

- Spindle can run the mod
- the security report still says `sandboxed: false`
- the security report still says `sandboxClaim: "not-sandboxed"`

Interpretation:

- the mod passed Spindle boundary validation
- the mod is not declared safe
- the mod is still unrestricted in-process Java code
