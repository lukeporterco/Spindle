# Data Generation And Assets Implementation Packet

Minecraft version: 26.1.2
Status: `deferred`

## Purpose

Treat data generation and assets as a future API design/documentation bridge, not runtime internals implementation.

## Source Evidence To Inspect

- `internals-analysis/evidence/data-generation-and-assets-evidence.md`
- `internals-analysis/spindle-translation/data-generation-and-assets-spindle-implications.md`
- `internals-analysis/notes/open-questions.md`

## Runtime Evidence To Inspect

- No runtime probe applies to this first-wave decision.

## Confirmed Touchpoints

- Generated JSON asset shapes and pack ingestion paths are documented as research evidence.
- Static/offline generation does not require a runtime hook by itself.

## Target Layer Meaning

Defer public API design. Use current research only to explain how generated assets may become datapack inputs later.

## SteelHook Requirements

None for offline/static generated assets. Runtime pack contribution would require future research and is not first wave.

## First Implementation Shape

No implementation. Documentation bridge only.

## Tests Future Implementation Should Require

- Future API design tests only after scope is approved.
- If offline generation is chosen later, validate generated pack contents rather than runtime hooks.
- If runtime contribution is chosen later, require reload/resource proof first.

## Documentation Requirements For Future Implementation

Document that data generation/assets are deferred and should not be treated as runtime internals implementation.

## Non-Goals

- Public data generation API in this pass.
- Generated asset APIs.
- Runtime datapack contribution.
- Fabric datagen architecture claims.
- Java sandbox promises; Java mod execution is not sandboxed.

## Risks

Fabric workspace evidence can be mistaken for Spindle architecture. Keep this as future design input only.

## Open Questions

- Does Spindle want offline data generation, runtime pack contribution, or both?
- Which generated asset families should be documented first?
