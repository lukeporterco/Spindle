# Source Preservation Notes

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

This pass is additive.

The refined handoff under `internals-analysis/spindle-ready-data/` does not replace, delete, truncate, or rewrite original `internals-analysis/` evidence. Existing source notes, concept files, candidate files, evidence packets, matrices, runtime probe logs, JSONL output, generated summaries, contradictions, rejected candidates, and unresolved questions remain the source of record.

Future prompts should use this package as a Spindle-facing entry point, then return to the original files whenever a decision needs re-checking against raw evidence or uncertainty.

Preserved source areas include:

- `internals-analysis/concepts/`
- `internals-analysis/candidates/`
- `internals-analysis/evidence/`
- `internals-analysis/matrices/`
- `internals-analysis/notes/`
- `internals-analysis/spindle-translation/`

This folder intentionally does not clean up uncertain, contradictory, rejected, or messy research notes.
