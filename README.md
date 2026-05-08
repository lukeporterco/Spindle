# MC ModLoader

Milestone 0 implements a deterministic entrypoint loader prototype. It discovers mod jars from `runtime/mods`, validates `loader.mod.json`, resolves a minimal dependency set against loader, Java, and Minecraft baselines, writes or verifies a deterministic lockfile, invokes mod entrypoints in an isolated class loader, launches a fake game, and writes startup diagnostics.

Minimum Java version: Java 25

First intended Minecraft target: 26.1.2

## How to run

```bash
./gradlew runMilestone0
```

## Expected output

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

## Intentionally not implemented yet

- Minecraft launch
- Mixin
- Fabric compatibility
- Forge or NeoForge compatibility
- Optimization modules
- Broad gameplay APIs
- Support for older Java runtimes
- Support for Minecraft versions older than 26.1.2
