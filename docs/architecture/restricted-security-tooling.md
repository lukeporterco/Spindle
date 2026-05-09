# Restricted Security Tooling

Security-3 adds a narrow restricted execution layer for Spindle-owned analysis tooling that can run without executing mods.

Current worker:

- `static-risk-scan`

Current mode:

- `restricted-child-jvm`

## What The Worker Does

The worker receives:

- a working directory
- a deterministic output path
- mod ids, relative jar paths, hashes, and absolute jar paths as data inputs

The worker then:

- opens mod jars directly as data
- scans jar entries and class constant-pool UTF-8 strings
- writes deterministic JSON to `.spindle/security-tools/static-risk-scan/output.json`

## What The Worker Does Not Do

The worker does not:

- put mod jars on its classpath
- classload mod classes
- execute lifecycle handlers
- execute arbitrary mod code
- claim OS-level sandboxing
- use `SecurityManager`

This is process separation with controlled inputs and deterministic output. It is not a runtime sandbox for arbitrary mods.

## Fail-Closed Behavior

If the worker exits nonzero, times out, fails to write output, or produces invalid output, Spindle writes fatal `SEC-TOOL-001` and blocks Runtime-1 lifecycle execution.

Likely fixes:

- rerun Spindle
- inspect concise diagnostics
- clear `.spindle/security-tools`
- report a Spindle bug if the failure is reproducible
