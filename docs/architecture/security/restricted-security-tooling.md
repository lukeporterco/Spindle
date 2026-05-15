# Restricted Security Tooling: Static Risk Worker Boundary

This is a security tooling implementation document. It records the narrow restricted child-JVM worker used for Spindle-owned analysis that treats mod jars as data and does not execute mods.

## Inputs

- Security-3 static-risk scanning requirements.
- Planned mod ids, jar paths, hashes, working directory, and deterministic output path.

## Output

- The `static-risk-scan` worker in `restricted-child-jvm` mode.
- Deterministic output at `.spindle/security-tools/static-risk-scan/output.json`.
- Fatal `SEC-TOOL-001` behavior when worker execution or output validation fails.

## Capability Added Or Recorded

- Enables process-separated static risk scanning for data-only jar analysis.
- Keeps worker inputs and outputs deterministic.

### Preserved Source Notes

Security-3 adds a narrow restricted execution layer for Spindle-owned analysis tooling that can run without executing mods.

Current worker:

- `static-risk-scan`

Current mode:

- `restricted-child-jvm`

### What The Worker Does

The worker receives:

- a working directory
- a deterministic output path
- mod ids, relative jar paths, hashes, and absolute jar paths as data inputs

The worker then:

- opens mod jars directly as data
- scans jar entries and class constant-pool UTF-8 strings
- writes deterministic JSON to `.spindle/security-tools/static-risk-scan/output.json`

### What The Worker Does Not Do

The worker does not:

- put mod jars on its classpath
- classload mod classes
- execute lifecycle handlers
- execute arbitrary mod code
- claim OS-level sandboxing
- use `SecurityManager`

This is process separation with controlled inputs and deterministic output. It is not a runtime sandbox for arbitrary mods.

### Fail-Closed Behavior

If the worker exits nonzero, times out, fails to write output, or produces invalid output, Spindle writes fatal `SEC-TOOL-001` and blocks Runtime-1 lifecycle execution.

Likely fixes:

- rerun Spindle
- inspect concise diagnostics
- clear `.spindle/security-tools`
- report a Spindle bug if the failure is reproducible

## Boundaries Preserved

- Does not put mod jars on the worker classpath, classload mod classes, execute lifecycle handlers, execute arbitrary mod code, claim OS-level sandboxing, or use `SecurityManager`.
- This is not a runtime sandbox for arbitrary mods.

## Follow-On Direction

- Future security tooling can reuse the data-input, deterministic-output pattern while preserving fail-closed behavior before Runtime-1 lifecycle execution.
