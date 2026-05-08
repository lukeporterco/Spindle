# MC ModLoader

MC ModLoader currently targets a deterministic fake-game loader core. It discovers mod jars from `runtime/mods`, validates `loader.mod.json`, resolves dependencies against loader, Java, and Minecraft builtin participants, writes or verifies `loader.lock.json`, freezes the resolved mod graph, writes deterministic diagnostics/state outputs, and in normal mode launches only the sample game.

Minimum Java version: Java 25

First intended Minecraft target: 26.1.2

## Tasks

Normal launch:

```bash
./gradlew runMilestone0
```

Validation-only run:

```bash
./gradlew validateMilestone0
```

Validation plus explain summary:

```bash
./gradlew explainMilestone0
```

## Expected normal output

First successful run:

```text
[loader] discovered 1 mod
[loader] resolved 1 mod
[loader] wrote loader.lock.json
Sample mod initialized
Game starting
[loader] startup complete
```

Second successful run:

```text
[loader] discovered 1 mod
[loader] resolved 1 mod
[loader] verified loader.lock.json
Sample mod initialized
Game starting
[loader] startup complete
```

Validation mode resolves and verifies the modpack without loading mod classes, creating a mod class loader, invoking entrypoints, or launching the game. Successful validation writes:

- `runtime/modpack-state.json`: deterministic state output for the frozen resolved mod graph
- `runtime/dependency-graph.json`: diagnostic graph of builtin and mod dependencies
- `runtime/diagnostics/startup-trace.json`
- `runtime/diagnostics/startup-profile.json`

Use `--strict-resources` to fail on duplicate non-class resources, and `--strict-packages` to fail on split packages. Without those flags, duplicate resources and split packages are recorded as diagnostics only.

## Intentionally not implemented yet

- Minecraft launch
- Mixin
- Fabric compatibility
- Forge or NeoForge compatibility
- Optimization modules
- Broad gameplay APIs
- Support for older Java runtimes
- Support for Minecraft versions older than 26.1.2
