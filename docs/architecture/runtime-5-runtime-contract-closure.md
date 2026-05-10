# Runtime-5 Runtime Contract Closure

`Runtime-5: Runtime Contract Closure and Loader API Boundary Prep` closes the Runtime Arc only. It does not seal the full Spindle mod loader API.

Runtime-5 moves the compiled profile from schema `5` to schema `6` and adds a top-level `runtimeClosure` section to `spindle.profile.json`.

## What Runtime-5 Adds

`runtimeClosure` is a deterministic compiled-profile contract. It is versioned, fingerprinted, and cache-sensitive.

The contract records:

- implemented runtime surfaces
- explicitly unavailable surfaces
- visibility-only disclosures
- fatal gate ordering around classloading
- the current loader-api boundary inventory

Runtime-5 builds this contract from explicit constants only. It does not scan the filesystem, reflect over classes, or infer API shape from unrelated source changes.

## Runtime Honesty

Runtime-5 does not change runtime honesty fields.

Standard Java mod execution remains:

- `runtimeExecutionIsolationMode: "in-process-unrestricted-java"`
- `sandboxed: false`
- `sandboxClaim: "not-sandboxed"`

Passing Runtime-5 validation is still not a sandbox claim.

## Target Model

Runtime-5 records Minecraft as a target, not the foundation:

- `targetModel: "minecraft-as-target-not-foundation"`

That keeps the Runtime Arc focused on Spindle-owned contracts while later arcs decide target adapters and public API ergonomics.

## Resource Surfaces

Runtime-5 keeps resource placeholders explicit:

- `resource.declare`
- `resource.overlay`

Both remain unavailable in the capability catalog and in `runtimeClosure`. Runtime-5 does not make either surface grantable and does not introduce a resource API.

## Boundary Follow-Up

Runtime-5 closes the runtime contract inventory so the next planned arc can focus on public API design:

- `nextArc: "Minecraft Target Arc"`

Loader API-0 is the follow-up pass that stabilizes the runtime-facing `loader-api` boundary, adds public API metadata and unchecked exceptions, and keeps `com.spindle.api.minecraft.*` deferred. Runtime-5 only prepares that boundary.

Later foundation hardening keeps this contract inventory fresh without changing Runtime closure contract version `2`, Runtime API version `1`, compiled profile schema `6`, or runtime honesty fields. Java mod execution remains `in-process-unrestricted-java`, `sandboxed` remains `false`, and `sandboxClaim` remains `not-sandboxed`.
