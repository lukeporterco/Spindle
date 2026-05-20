# Import Manifest

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

This manifest describes a future Spindle-repo import pass. It is not that pass.

## Source Package

Research-local source package:

```text
/Users/luke/Documents/MC_Research/v26.1.2/internals-analysis/spindle-ready-data/
```

Expected later Spindle destination:

```text
docs/architecture/minecraft-target/v26.1.2-data/
```

## Future Import Steps

1. Copy this handoff package, or only its `refined/` contents, into the Spindle repository destination above.
2. Preserve the original research evidence separately if the Spindle repo needs a fuller audit trail.
3. Update Spindle Minecraft Target architecture docs to point at the imported data.
4. Update SteelHook docs to point at `refined/steelhook-primitive-roadmap.md`.
5. Keep API implementation, hook implementation, runtime hook installation, and real Minecraft class transformation out of the import pass unless a separate implementation plan explicitly authorizes them.

## Import Restrictions

- Do not treat this package as already imported.
- Do not create public `com.spindle.api.minecraft.*` behavior during import.
- Do not claim Fabric, Mixin, Forge, NeoForge, Quilt, Paper, Bukkit, or Sponge compatibility from this research.
- Do not imply Java mod execution is sandboxed.
- Do not turn dedicated-server-only observations into integrated or universal guarantees.
