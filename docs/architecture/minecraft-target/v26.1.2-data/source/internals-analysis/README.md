# Minecraft Internals Analysis: 26.1.2

This folder contains research notes for mapping Minecraft internals into Spindle Target Layer concepts.

It should not contain copied or redistributed Minecraft source code. Use summaries, class names, method names, descriptors, call-chain notes, generated report references, and conclusions only.

## Version

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted
Research workspace: ../fabric-workspace

## Main outputs

- concepts/: concept-level understanding
- candidates/: possible hook/binding points
- evidence/: source-backed evidence packets and confirmation evidence
- matrices/: distilled Spindle-facing decisions
- notes/: uncertainty, rejected paths, and version-specific oddities
- spindle-translation/: narrowed Spindle Target Layer and SteelHook implications
- research-index.md: research progress, queues, blocked questions, and handoff readiness
- research-rules.md: working rules for evidence, naming, and acceptance standards

## Folder tree

```text
internals-analysis/
  README.md
  research-index.md
  research-rules.md
  glossary.md
  concepts/
    concept-template.md
    server-lifecycle.md
    commands.md
    registries.md
    resources-and-datapacks.md
    world-and-level.md
    ticking.md
    networking.md
  candidates/
    candidate-template.md
    lifecycle-candidates.md
    command-candidates.md
    registry-candidates.md
    resource-reload-candidates.md
    world-level-candidates.md
    ticking-candidates.md
    networking-candidates.md
  evidence/
    lifecycle-confirmation-evidence.md
    commands-confirmation-evidence.md
    registry-confirmation-evidence.md
    resource-reload-confirmation-evidence.md
    world-level-confirmation-evidence.md
    ticking-confirmation-evidence.md
    networking-confirmation-evidence.md
    data-generation-and-assets-evidence.md
  matrices/
    matrix-template.md
    concept-binding-matrix.md
    steelhook-primitive-fit.md
    confidence-matrix.md
    target-layer-first-wave.md
    steelhook-gap-map.md
    runtime-confirmation-status.md
  notes/
    note-template.md
    open-questions.md
    rejected-candidates.md
    version-specific-weirdness.md
    runtime-probe-log.md
    contradictions-and-resolutions.md
  spindle-translation/
    README.md
    target-layer-first-wave-recommendation.md
    steelhook-primitive-requirements.md
```

## Use rules

- Keep findings short, source-backed, and decision-oriented.
- Prefer class names, method names, descriptors, call-chain summaries, and generated report references over copied source.
- Every kept candidate should explain why the hook or binding point is stable enough for Spindle to care about.
- Every uncertain candidate should state exactly what evidence is missing.
- Every rejected candidate should preserve the reason it was rejected so the same path is not re-litigated later.
- Translation files should separate source evidence from Spindle-facing recommendations.
