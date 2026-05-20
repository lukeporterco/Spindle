# Research Rules

## Purpose

This file defines the working rules for Minecraft 26.1.2 internals research used by Spindle.

The goal is to produce trustworthy analysis, not to mirror Minecraft source code. Findings should be compact, evidence-backed, and useful for Spindle Target Layer and SteelHook planning.

## Allowed source material

Allowed:

- Class names
- Method names
- Field names
- Descriptors
- Access flags
- Call-chain summaries
- Generated report references
- Short behavioral summaries written in original words
- Runtime logs or dry-run observations

Not allowed:

- Pasted Minecraft source bodies
- Large copied decompiled snippets
- Decompiled source redistribution
- Unattributed guesses presented as facts

## Naming conventions

Use Yarn named mappings unless a finding explicitly says otherwise.

When a name is uncertain or comes from another namespace, mark it directly in the finding:

```text
Mapping namespace: official / intermediary / Yarn named / mixed
```

## Evidence standards

A usable candidate must include:

- Source artifact or report path
- Mapping namespace
- Class/member reference
- Descriptor when available
- Call-chain evidence or structural evidence
- Runtime/log evidence when the finding depends on timing
- Confidence reason
- Decision: keep, reject, or uncertain

## Confidence levels

Use these meanings:

- High: backed by clear static evidence and, when timing matters, runtime/log confirmation
- Medium: backed by static evidence, but timing or lifecycle fit still needs confirmation
- Low: plausible from names or structure only
- Unknown: placeholder or not yet researched

## Candidate decisions

Use `keep` only when the candidate has enough evidence to inform Spindle implementation or documentation.

Use `reject` when the candidate is wrong, too unstable, too low-level, or conflicts with Spindle's intended architecture.

Use `uncertain` when the candidate is plausible but missing evidence.

## Review checklist

Before treating a finding as handoff-ready, check:

- Is the Minecraft version stated?
- Is the mapping namespace stated?
- Is the evidence specific enough to re-check later?
- Is the candidate decision explicit?
- Are risks and uncertainty stated honestly?
- Does the finding avoid copying Minecraft source?
- Does the finding explain the Spindle implication?
