# Target-36: SteelHook 0.4 Completion

Target-36 is the SteelHook 0.4 completion verifier. It does not add new primitives, transform bytecode, define classes, load classes, initialize classes, invoke transformed methods, execute wrappers, install hooks, execute `SteelHookDispatcher`, invoke Minecraft main, launch Minecraft, expose public APIs, or add runtime behavior.

## Inputs

- `minecraft-steelhook-0-4-primitive-boundary.json`
- `minecraft-steelhook-0-4-return-value-intercept-offline-proof.json`
- `minecraft-steelhook-0-4-invoke-redirect-wrap-offline-proof.json`
- `minecraft-steelhook-0-4-gated-runtime-proof.json`

Target-36 verifies persisted reports from the working directory so stale artifacts, schema drift, raw JSON payload leakage, and forbidden side-effect files can be rejected deterministically.

## Output

- Deterministic `minecraft-steelhook-0-4-report.json`

The report uses schema `1`, milestone `Target-36`, target `minecraft`, and `steelHookVersion: "0.4"`.

## What Target-36 verifies

- The Target-32 through Target-35 evidence chain is complete and coherent.
- The completed internal primitive set is exactly:
- `RETURN_VALUE_INTERCEPT`
- `INVOKE_REDIRECT`
- `INVOKE_WRAP`
- These remain internal SteelHook primitives, not public APIs.
- Target-32 still records only the approved primitive boundary.
- Target-33 still proves bounded `RETURN_VALUE_INTERCEPT` offline only.
- Target-34 still proves bounded `INVOKE_REDIRECT` and `INVOKE_WRAP` offline only.
- Target-35 still proves isolated gated runtime class definition only.

## Rejections

Target-36 rejects:

- missing or unparseable source reports
- schema mismatches
- wrong milestone, target, or `steelHookVersion`
- missing approved primitive evidence
- unsupported primitive leakage
- raw byte payload keys in Target-32 through Target-35 source reports or Target-36 output
- stale side-effect reports:
- `minecraft-hook-installation-result.json`
- `minecraft-server-bootstrap-result.json`
- `minecraft-fixture-transformation-result.json`
- `minecraft-hook-bootstrap-transformation-result.json`
- reports that imply execution beyond the Target-35 class-definition-only boundary

## Boundary preserved

Target-35 class definition evidence is allowed:

- `runtimeClassLoadingPathEnabled: true`
- `classLoadingOccurred: true`
- `targetClassDefinitionOccurred: true`

Target-36 still rejects:

- class initialization
- transformed method invocation
- wrapper execution
- dispatcher execution
- hook installation
- Minecraft execution
- public API exposure
- Java agent use
- Mixin use
- sandbox claims

Java mod execution is not sandboxed.

## Completion handoff

SteelHook 0.4 is complete only after Target-36 passes. A successful report writes:

- `status: "passed"`
- `handoffStatus: "steelhook-0-4-complete"`

If any verification fails, Target-36 writes `steelhook-0-4-incomplete` and points to the next restore direction instead of broadening runtime behavior.
