# MC ModLoader

MC ModLoader currently provides a deterministic loader core with two provider paths:

- `sample`: the existing fake-game provider used for Milestone 0-2 launches
- `minecraft`: a Milestone 3 dry-run provider that parses Minecraft metadata, plans client or server launches, verifies expected local files, and optionally scans a local PaperMC/mache checkout as a read-only reference

Minimum Java version: Java 25

First intended Minecraft target: 26.1.2

## Tasks

Normal sample launch:

```bash
./gradlew runMilestone0
```

Validation-only sample run:

```bash
./gradlew validateMilestone0
```

Validation plus explain summary:

```bash
./gradlew explainMilestone0
```

Minecraft client dry run:

```bash
./gradlew minecraftDryRun
```

Minecraft server dry run:

```bash
./gradlew minecraftServerDryRun
```

Optional Mache reference scan:

```bash
./gradlew macheReferenceScan -PmacheDir=C:\path\to\mache
```

If `-PmacheDir` is not provided, `macheReferenceScan` prints a skip message and does nothing else.

## Expected sample output

First successful sample run:

```text
[loader] discovered 1 mod
[loader] resolved 1 mod
[loader] wrote loader.lock.json
Sample mod initialized
Game starting
[loader] startup complete
```

Second successful sample run:

```text
[loader] discovered 1 mod
[loader] resolved 1 mod
[loader] verified loader.lock.json
Sample mod initialized
Game starting
[loader] startup complete
```

## Validation and dry-run outputs

Validation mode resolves and verifies the modpack without loading mod classes, creating a mod class loader, invoking entrypoints, or launching the game. Successful validation writes:

- `runtime/modpack-state.json`
- `runtime/dependency-graph.json`
- `runtime/diagnostics/startup-trace.json`
- `runtime/diagnostics/startup-profile.json`

Minecraft dry run keeps the same deterministic mod-resolution pipeline, then parses Minecraft version metadata and writes:

- `runtime/minecraft-launch-plan.json`
- `runtime/modpack-state.json`
- `runtime/dependency-graph.json`
- `runtime/diagnostics/startup-trace.json`
- `runtime/diagnostics/startup-profile.json`

If Mache reference scanning is enabled, it also writes:

- `runtime/mache-reference-report.json`

Minecraft launch is not implemented yet. Milestone 3 does not launch Minecraft, inject mods into Minecraft, patch entrypoints, transform classes, remap classes, or perform authentication.

Use `--strict-resources` to fail on duplicate non-class resources, and `--strict-packages` to fail on split packages. Without those flags, duplicate resources and split packages are recorded as diagnostics only.

## Minecraft CLI flags

Use these with `--game-provider minecraft`:

- `--minecraft-version <version>`
- `--minecraft-dir <path>`
- `--minecraft-version-json <path>`
- `--minecraft-manifest-json <path>`
- `--minecraft-side client|server`
- `--minecraft-dry-run`
- `--minecraft-verify-files`
- `--minecraft-fetch-metadata`
- `--minecraft-output-plan <path>`

Behavior notes:

- `--game-provider sample` remains the default.
- `--game-provider minecraft` requires `--minecraft-dry-run`.
- `--minecraft-version-json` overrides local version JSON discovery.
- `--minecraft-manifest-json` is used only for metadata resolution.
- `--minecraft-fetch-metadata` may fetch only the version manifest and version JSON.
- `--minecraft-dir` defaults to the standard user Minecraft directory only when it can be determined safely for the current OS.

## Mache reference scan

Optional Mache flags:

- `--mache-dir <path>`
- `--mache-version <version>`
- `--mache-reference-scan`

The Mache scan is reference-only. It does not clone Mache, compile Mache, read source snippets, copy Mache code, or add any dependency on Mache. It only scans repository layout and metadata. If the detected license text looks LGPL, the generated report includes a warning that Mache must remain reference-only and must not be copied into this MIT project.

## Intentionally not implemented yet

- actual Minecraft launch
- mod injection into Minecraft
- Mixin
- class transformation
- remapping
- Fabric compatibility
- Forge or NeoForge compatibility
- Paper compatibility
- optimization modules
- broad gameplay APIs
- older Java support
- older Minecraft support
