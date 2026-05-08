# MC ModLoader

MC ModLoader currently provides a deterministic loader core with two provider paths:

- `sample`: the existing fake-game provider used for Milestone 0-2 launches
- `minecraft`: a Mega-Milestone 7 provider that runs deterministic dry-run planning, owns a server runtime capsule, writes pre-mod boundary reports, and, only when explicitly requested, can inspect, repair, cache, verify, baseline, replay, and launch a managed vanilla Minecraft server process

Minimum Java version: Java 25

First intended Minecraft target: 26.1.2

Mega-Milestone 7 keeps `26.1.2` as the project target Minecraft version for loader metadata and dependency validation. Real vanilla server smoke tasks use a separate official baseline version selected from Mojang metadata, for example `latest-release` or an exact version such as `1.21.8`.

Mega-Milestone 7 turns MC ModLoader into a deterministic Minecraft server runtime owner with a frozen, inspectable, replayable, and explainable pre-mod integration boundary. It still deliberately stops before real Minecraft mod loading: no Minecraft mod classes are loaded, no Minecraft entrypoints are invoked, no mod jars are placed on the real Minecraft runtime classpath, and no Mixin, remapping, bytecode transformation, patching, Fabric/Forge/NeoForge/Quilt/Paper/Bukkit/Sponge compatibility, or injection exists.

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

Managed fake server launch smoke:

```bash
./gradlew minecraftServerLaunchFakeSmoke
```

Managed server cache inspect:

```bash
./gradlew minecraftServerCacheInspect
```

Managed server cache repair:

```bash
./gradlew minecraftServerCacheRepair
```

Managed server offline cache check:

```bash
./gradlew minecraftServerOfflineCacheCheck
```

Managed server download smoke:

```bash
./gradlew minecraftServerDownloadSmoke
```

Real vanilla server baseline acquire:

```bash
./gradlew minecraftRealServerAcquire -PmcRealVersion=latest-release
```

Real vanilla server smoke:

```bash
./gradlew minecraftRealServerSmoke -PmcRealVersion=latest-release
```

Real vanilla server offline replay:

```bash
./gradlew minecraftRealServerOfflineReplay -PmcRealVersion=latest-release
```

Optional real vanilla server EULA smoke:

```bash
./gradlew minecraftRealServerEulaSmoke -PmcRealVersion=latest-release
```

Managed real local server dry smoke:

```bash
./gradlew minecraftServerLaunchDrySmoke -PminecraftDir=C:\path\to\.minecraft
```

Mega-Milestone 7 runtime plan:

```bash
./gradlew minecraftServerRuntimePlan
```

Mega-Milestone 7 fake bundled server runtime smoke:

```bash
./gradlew minecraftServerBundledFixtureSmoke
```

Mega-Milestone 7 runtime boundary report:

```bash
./gradlew minecraftServerRuntimeBoundary
```

Mega-Milestone 7 mod integration plan:

```bash
./gradlew minecraftModIntegrationPlan
```

Mega-Milestone 7 preflight:

```bash
./gradlew minecraftPreflight
```

Mega-Milestone 7 reproducibility check:

```bash
./gradlew minecraftReproducibilityCheck
```

Mega-Milestone 7 fixture check suite:

```bash
./gradlew minecraftMegaMilestone7Check
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
- `runtime/minecraft-artifacts.json`
- `runtime/modpack-state.json`
- `runtime/dependency-graph.json`
- `runtime/diagnostics/startup-trace.json`
- `runtime/diagnostics/startup-profile.json`

If Mache reference scanning is enabled, it also writes:

- `runtime/mache-reference-report.json`

Milestone 5 adds an explicit verified vanilla server artifact cache under `runtime/minecraft-cache/`. Milestone 6 extends that into a real official vanilla server baseline flow. The loader can resolve an official Mojang server version, cache version metadata, cache only the vanilla server jar, verify cached server artifacts, write `runtime/minecraft-artifacts.json`, write `runtime/minecraft-cache/versions/<version>/server-artifacts.lock.json`, and write `runtime/minecraft-server-baseline.json` after a successful baseline resolution.

Milestone 6 also adds an explicit offline replay path. Offline replay uses only cached manifest metadata, cached version JSON, and a cached verified server jar. It performs zero network requests, writes the normal artifact and launch-plan reports, and writes a deterministic baseline report.

Mega-Milestone 7 adds a server runtime planning layer between artifact resolution and process launch. Server planning writes `runtime/minecraft-server-runtime-plan.json`, selects simple-jar or bundled-server mode, records the structured launch command preview, materializes bundled runtime libraries under `runtime/minecraft-cache/versions/<version>/server-runtime/`, verifies cache-owned runtime files, and proves analysis-only behavior.

Mega-Milestone 7 also writes:

- `runtime/minecraft-runtime-boundary.json`
- `runtime/minecraft-mod-integration-plan.json`
- `runtime/minecraft-preflight-result.json`
- `runtime/minecraft-runtime-provenance.json`
- `runtime/minecraft-reproducibility-check.json`

The runtime boundary report indexes Minecraft runtime packages, resources, services, module-info presence, multi-release jars, native libraries, duplicate resources, split packages, and classpath ownership by layer. It is analysis-only and defines boundaries future mods must not cross.

The mod integration plan discovers MC ModLoader mod jars intended for Minecraft server usage, parses `loader.mod.json`, validates loader, Java, Minecraft, side, dependency, and breaks metadata, scans jar bytes, and freezes a would-load order. It never defines, reflects, initializes, or loads scanned classes and never invokes `ModInitializer`.

Downloads remain explicit and limited to Minecraft version metadata plus the vanilla server jar. This milestone does not download client jars, client assets, client libraries, client natives, mappings, or source.

Managed Minecraft behavior remains server-only. Minecraft client launch is still not implemented. The loader does not inject mods into Minecraft, put mod jars on the Minecraft server classpath, create a mod classloader for Minecraft server launch, invoke mod entrypoints on the Minecraft server launch path, patch entrypoints, transform classes, remap classes, or perform authentication.

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
- `--minecraft-download-server`
- `--minecraft-cache-dir <path>`
- `--minecraft-offline`
- `--minecraft-cache-inspect`
- `--minecraft-cache-repair`
- `--minecraft-cache-strict`
- `--minecraft-force-redownload`
- `--minecraft-output-plan <path>`
- `--minecraft-launch`
- `--minecraft-baseline-server`
- `--minecraft-baseline-version <version|latest-release|latest-snapshot>`
- `--minecraft-baseline-report <path>`
- `--minecraft-offline-replay`
- `--minecraft-require-ready`
- `--minecraft-real-smoke`
- `--minecraft-server-dir <path>`
- `--minecraft-server-jvm-arg <arg>` repeatable
- `--minecraft-server-arg <arg>` repeatable
- `--minecraft-launch-timeout-seconds <seconds>`
- `--minecraft-stop-after-ready`
- `--minecraft-ready-timeout-seconds <seconds>`
- `--minecraft-accept-eula-for-test`
- `--minecraft-runtime-plan`
- `--minecraft-plan-mods`
- `--minecraft-integration-plan`
- `--minecraft-boundary-report`
- `--minecraft-preflight`
- `--minecraft-offline-preflight`
- `--minecraft-strict-boundary`
- `--minecraft-strict-runtime-conflicts`
- `--minecraft-strict-side`
- `--minecraft-strict-class-versions`
- `--minecraft-explain-boundary`
- `--minecraft-explain-runtime`
- `--minecraft-explain-mods`
- `--minecraft-reproducibility-check`

Behavior notes:

- `--game-provider sample` remains the default.
- `--game-provider minecraft` requires `--minecraft-dry-run`.
- `--minecraft-launch` is server-only, requires `--minecraft-side server`, and also requires `--minecraft-verify-files`.
- `--minecraft-baseline-server` enables the real official vanilla server baseline flow without changing the project target `26.1.2`.
- `--minecraft-baseline-version` selects the real official server version to resolve. Use `latest-release`, `latest-snapshot`, or an exact Mojang version id. The Gradle real-smoke tasks use `-PmcRealVersion=latest-release` by default.
- `--minecraft-baseline-report` defaults to `runtime/minecraft-server-baseline.json`.
- `--minecraft-offline-replay` requires `--minecraft-baseline-server` and `--minecraft-offline`.
- `--minecraft-require-ready` makes readiness detection mandatory for success when launch is attempted.
- `--minecraft-real-smoke` is an explicit marker for real baseline acquisition and smoke tasks. It requires `--game-provider minecraft`, `--minecraft-side server`, and `--minecraft-dry-run`.
- `--minecraft-launch` still writes `minecraft-launch-plan.json` before process launch.
- `--minecraft-server-dir` defaults to `runtime/minecraft-server/<version>`.
- Real baseline server launch defaults to `runtime/minecraft-server-baseline/<resolvedVersion>`.
- `--minecraft-server-jvm-arg` values are inserted before the planned launch mode.
- `--minecraft-server-arg` values are inserted after the planned server main target. If none are provided, the managed launch defaults to `nogui`.
- `--minecraft-launch-timeout-seconds` defaults to `30`.
- `--minecraft-ready-timeout-seconds` defaults to `20`.
- `--minecraft-stop-after-ready` watches for a conservative ready line and then sends `stop`.
- `--minecraft-accept-eula-for-test` writes `eula=true` in the managed server directory before launch. Use this only for local testing and only if you accept Mojang's EULA.
- `--minecraft-version-json` overrides local version JSON discovery.
- `--minecraft-manifest-json` is used only for metadata resolution.
- `--minecraft-fetch-metadata` may fetch only the version manifest and version JSON.
- `--minecraft-download-server` explicitly allows caching the vanilla server jar.
- `--minecraft-cache-dir` defaults to `minecraft-cache` relative to the runtime working directory, which maps to `runtime/minecraft-cache` for Gradle tasks and avoids `runtime/runtime/minecraft-cache`.
- `--minecraft-offline` disables all network fetches and downloads.
- `--minecraft-offline` forbids all network fetches and downloads.
- `--minecraft-cache-inspect` inspects cache state, writes `runtime/minecraft-artifacts.json`, writes diagnostics/profile output, and does not launch Minecraft.
- `--minecraft-cache-repair` may repair missing or invalid cached metadata and the cached server jar, but only when downloads are otherwise allowed.
- `--minecraft-cache-strict` promotes cache warnings such as lock mismatches to failures.
- `--minecraft-force-redownload` refreshes allowed cache artifacts even when a cached copy already exists.
- `--minecraft-dir` defaults to the standard user Minecraft directory only when it can be determined safely for the current OS.
- `--minecraft-download-server` by itself never downloads anything except metadata needed to identify the server jar and the vanilla server jar itself.
- `--minecraft-runtime-plan` writes `runtime/minecraft-server-runtime-plan.json` and `runtime/minecraft-runtime-provenance.json`.
- `--minecraft-boundary-report` writes `runtime/minecraft-runtime-boundary.json`.
- `--minecraft-integration-plan` writes `runtime/minecraft-mod-integration-plan.json`.
- `--minecraft-preflight` runs runtime planning, boundary reporting, and mod integration planning, then exits before launch or class loading.
- `--minecraft-offline-preflight` combines preflight with offline cache-only behavior.
- `--minecraft-reproducibility-check` writes `runtime/minecraft-reproducibility-check.json`.
- `--minecraft-strict-runtime-conflicts`, `--minecraft-strict-side`, and `--minecraft-strict-class-versions` promote selected Mega-Milestone 7 warnings to fatal planning issues.
- `--minecraft-explain-runtime`, `--minecraft-explain-boundary`, and `--minecraft-explain-mods` print deterministic CI-friendly reasons for runtime mode, boundary ownership, and mod acceptance/rejection.

When `--minecraft-launch` is used, the loader also writes `runtime/minecraft-server-launch-result.json` with the managed process command preview, launch outcome, timing, and bounded stdout/stderr tails.

`minecraftServerLaunchDrySmoke` does not require a real Minecraft server jar by default. If `-PminecraftDir` is not provided, it prints a clear skip message and exits successfully.

`minecraftServerLaunchFakeSmoke` builds a tiny fake server jar, launches it through the managed Minecraft server path, detects readiness, sends `stop`, and writes `runtime/minecraft-server-launch-result.json`.

`minecraftServerCacheInspect` does not require network and prints:

```text
[loader] minecraft cache inspection complete
```

`minecraftServerCacheRepair` is the explicit network-enabled repair path for metadata plus the vanilla server jar.

`minecraftServerOfflineCacheCheck` verifies that a previously populated cache is complete enough to run offline. It is allowed to fail when the cache has not been populated yet.

`minecraftServerDownloadSmoke` is the explicit network-enabled smoke path. It fetches metadata if needed, fetches and verifies the vanilla server jar if needed, writes `runtime/minecraft-artifacts.json`, writes `runtime/minecraft-cache/versions/<version>/server-artifacts.lock.json`, writes `runtime/minecraft-launch-plan.json`, and then attempts a managed vanilla server launch. It does not pass `--minecraft-accept-eula-for-test` by default.

`minecraftRealServerAcquire` is the explicit real-baseline acquisition path. It resolves the real official server version from Mojang metadata, fetches only manifest metadata, version metadata, and the vanilla server jar, verifies those artifacts, writes `runtime/minecraft-artifacts.json`, writes `runtime/minecraft-launch-plan.json`, writes `runtime/minecraft-server-baseline.json`, and does not launch the server.

`minecraftRealServerSmoke` is the explicit real-baseline launch smoke path. It uses official Mojang metadata and a verified vanilla server jar, launches only the server side, writes `runtime/minecraft-server-launch-result.json` when the process starts, and does not inject mods.

`minecraftRealServerOfflineReplay` is the explicit cache-only replay path. It uses `--minecraft-offline` plus `--minecraft-offline-replay`, performs zero network requests, verifies cached metadata and the cached verified server jar, writes the same reports, and may fail clearly if the cache is incomplete.

`minecraftRealServerEulaSmoke` is local-only and should be run only if you explicitly accept Mojang's EULA. It adds `--minecraft-accept-eula-for-test`, waits for readiness, sends `stop`, and requires readiness before treating the run as successful.

`minecraftRealServerRuntimeAcquire`, `minecraftRealServerRuntimeSmoke`, and `minecraftRealServerRuntimeOfflineReplay` are the real-server Mega-Milestone 7 runtime-plan variants. They preserve the Milestone 6 real-baseline behavior while adding runtime capsule and boundary reports.

## Mega-Milestone 7 severity model

Mega-Milestone 7 diagnostics use four severities:

- `info`: deterministic context or proof statements
- `warning`: allowed now, but usually fatal before a future injection milestone
- `error`: invalid input or metadata that rejects a mod candidate
- `fatal`: execution cannot continue, or strict mode promoted the issue

Boundary reports also label issues as fatal now, warning now but fatal before injection, informational only, or strict-mode fatal.

## Remaining gap before real Minecraft mod loading

Before real Minecraft mod loading can happen, MC ModLoader still needs a safe Minecraft classloader attachment design, injection lifecycle, verified entrypoint execution boundary, compatibility and conflict policy, and explicit user-facing safety gates. Mega-Milestone 7 prepares those decisions by owning the Minecraft runtime plan and freezing the pre-mod boundary, but it does not cross into injection.

## Mache reference scan

Optional Mache flags:

- `--mache-dir <path>`
- `--mache-version <version>`
- `--mache-reference-scan`

The Mache scan is reference-only. It does not clone Mache, compile Mache, read source snippets, copy Mache code, or add any dependency on Mache. It only scans repository layout and metadata. If the detected license text looks LGPL, the generated report includes a warning that Mache must remain reference-only and must not be copied into this MIT project.

## Intentionally not implemented yet

- Minecraft client launch
- mod injection into Minecraft
- Mixin
- class transformation
- remapping
- patching
- Fabric compatibility
- Forge or NeoForge compatibility
- Paper compatibility
- Mache compatibility
- Vulkan renderer integration
- optimization modules
- broad gameplay APIs
- older Java support
- older Minecraft support
