# Target-3: Non-Invasive Known Minecraft Symbol Hook Validation

Target-3 adds the first internal known-symbol hook contract catalog inside `target-minecraft`.

Its scope is intentionally narrow. This pass proves that Spindle can select a tiny version-specific catalog, validate it against Target-1 artifact interpretation, and write a deterministic `minecraft-hook-contracts.json` report without loading Minecraft classes or performing hook installation work.

## Why Target-3 Exists

Target-2 established the internal hook contract model and validator, but it still validated an empty catalog for every supported baseline.

Target-3 makes that path real for the first time:

- it selects a known-symbol catalog by Minecraft version and side
- it validates those contracts against interpreted class-file structure
- it records which catalog was selected in the hook contract report

This keeps the work diagnostic and analysis-only while proving that version-specific contract validation works end to end.

## Catalog Selection

Target-3 selects contracts by exact Minecraft version and side.

The only supported catalog in this pass is:

- Minecraft version `26.1.2`
- side `server`
- catalog id `minecraft-26.1.2-server-known-symbols`

Any unsupported version or side uses the empty catalog instead. In that case the report records that no matching contracts were declared for the selected combination.

## Exact Target-3 Contracts

The initial catalog is intentionally tiny. It contains only these two required contracts, in this order:

1. Class contract
   `id`: `minecraft.26_1_2.server.main.class`
   `ownerInternalName`: `net/minecraft/server/Main`
2. Method contract
   `id`: `minecraft.26_1_2.server.main.entrypoint`
   `ownerInternalName`: `net/minecraft/server/Main`
   `memberName`: `main`
   `descriptor`: `([Ljava/lang/String;)V`

The catalog deliberately does not include `MinecraftServer`, `DedicatedServer`, bootstrap helpers, tick loops, fields, registries, commands, networking, world symbols, or gameplay-facing contracts.

## Source Of Truth

Target-1 artifact interpretation remains the validation source of truth.

Target-3 does not use online documentation, mappings, decompiled source, Mache references, or inferred names as runtime validation input. It validates only against the interpreted symbols extracted from the planned Minecraft runtime jars.

## Report Behavior

Target-3 still writes `minecraft-hook-contracts.json`, but the report schema is now `2`.

The report now includes:

- catalog id
- catalog description
- catalog Minecraft version
- catalog side
- contract counts and diagnostics
- analysis-only execution flags

If a required known symbol is missing, the report records an error diagnostic and marks validation as failed for the report. That failure is nonfatal in this pass. It does not stop dry-run planning, launch Minecraft, install hooks, or become a runtime gate yet.

## What Target-3 Does Not Do

Target-3 does not:

- parse method bytecode or `Code` attributes
- inspect instructions or callsites
- classify hook candidates
- install hooks
- transform or patch Minecraft classes
- load Minecraft classes
- execute Minecraft as part of hook validation
- expose a public Modding API
- imply Java mod sandboxing

This pass is analysis-only. It validates a tiny internal known-symbol catalog and nothing more.
