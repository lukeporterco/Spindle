# Architecture Documentation

Spindle architecture documentation is organized by arc. Each arc folder owns its pass documents, durable reference documents, and a small README that states the current status and next handoff.

Normal pass documents should follow `templates/pass-document-template.md`. Durable reference documents, roadmaps, and public-facing mod docs may use their own structure when the pass template would make them less clear.

Arc folders:

- `foundation/`: early loader structure and hardening records.
- `runtime/`: Runtime-0 through Runtime-5 and the compiled standard-runtime contract.
- `loader-api/`: public Runtime API boundary preparation, stabilization, and hardening.
- `security/`: security posture and restricted tooling boundaries.
- `minecraft-target/`: Target Layer concept grounding and Target-* pass records.
- `steelhook/`: broad SteelHook strategy and future SteelHook versioning direction.
- `templates/`: reusable architecture documentation formats.

## Adding a New Architecture Arc

Create a new folder under `docs/architecture/` when a line of work becomes a named arc with more than one pass, a durable capability boundary, or a long-term handoff. Use a short lowercase folder name such as `platform/`, `simulation/`, or `modding-api/`.

Every new arc folder should include a `README.md` based on `templates/arc-readme-template.md`. The README should state the arc purpose, current status, important pass sequence, latest capability, blocked capabilities, and next handoff. New pass documents in that arc should follow `templates/pass-document-template.md` unless a roadmap, boundary reference, or developer-facing guide needs a different shape.

Do not create a new arc folder for a one-off note. Place one-off architecture notes in the closest existing arc until the work has a durable pass sequence or boundary of its own.

