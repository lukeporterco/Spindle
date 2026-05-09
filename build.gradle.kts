import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import java.security.MessageDigest
import java.util.HexFormat
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

plugins {
    base
    id("com.diffplug.spotless") version "6.25.0"
}

allprojects {
    group = "com.mcmodloader"
    version = "0.1.0"
}

spotless {
    ratchetFrom("HEAD")

    format("repoFiles") {
        target("*.md", "docs/**/*.md", "backlog/**/*.md", "*.gradle.kts", ".gitignore")
        targetExclude("**/build/**", "**/.gradle/**", "**/runtime/**")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

subprojects {
    apply(plugin = "com.diffplug.spotless")

    plugins.withId("java") {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(25))
            }
        }

        tasks.withType<Jar>().configureEach {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
        }
    }

    extensions.configure<SpotlessExtension> {
        ratchetFrom("HEAD")

        java {
            target("src/*/java/**/*.java")
            targetExclude("**/build/**", "**/runtime/**")
            googleJavaFormat("1.33.0")
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

val prepareMilestone0Mods by tasks.registering(Copy::class) {
    dependsOn(":sample-mod:jar")
    from(project(":sample-mod").tasks.named<Jar>("jar").flatMap { it.archiveFile })
    into(layout.projectDirectory.dir("runtime/mods"))
    rename { "sample-mod.jar" }

    includeEmptyDirs = false
}

val prepareMinecraftFixture by tasks.registering {
    dependsOn(prepareMilestone0Mods)

    doLast {
        val fixtureDirectory = layout.projectDirectory.dir("runtime/fixture-minecraft").asFile
        val versionDirectory = fixtureDirectory.resolve("versions/26.1.2")
        versionDirectory.mkdirs()
        versionDirectory.resolve("26.1.2.json").writeText(
            """
            {
              "id": "26.1.2",
              "type": "release",
              "mainClass": "net.minecraft.client.main.Main",
              "assets": "legacy",
              "assetIndex": {
                "id": "legacy",
                "url": "https://example.invalid/assets/indexes/legacy.json",
                "sha1": "asset-index-sha1",
                "size": 321
              },
              "downloads": {
                "client": {
                  "url": "https://example.invalid/client.jar",
                  "sha1": "client-sha1",
                  "size": 111
                },
                "server": {
                  "url": "https://example.invalid/server.jar",
                  "sha1": "server-sha1",
                  "size": 222
                }
              },
              "libraries": [
                {
                  "name": "com.example:alpha:1.0",
                  "downloads": {
                    "artifact": {
                      "path": "com/example/alpha/1.0/alpha-1.0.jar",
                      "url": "https://example.invalid/libs/alpha-1.0.jar",
                      "sha1": "alpha-sha1",
                      "size": 10
                    }
                  }
                },
                {
                  "name": "com.example:beta:2.0",
                  "downloads": {
                    "artifact": {
                      "path": "com/example/beta/2.0/beta-2.0.jar",
                      "url": "https://example.invalid/libs/beta-2.0.jar",
                      "sha1": "beta-sha1",
                      "size": 20
                    }
                  },
                  "rules": [
                    {
                      "action": "disallow",
                      "os": {
                        "name": "linux"
                      }
                    },
                    {
                      "action": "allow",
                      "os": {
                        "name": "windows"
                      }
                    }
                  ]
                },
                {
                  "name": "com.example:gamma:3.0",
                  "downloads": {
                    "artifact": {
                      "path": "com/example/gamma/3.0/gamma-3.0.jar",
                      "url": "https://example.invalid/libs/gamma-3.0.jar",
                      "sha1": "gamma-sha1",
                      "size": 30
                    },
                    "classifiers": {
                      "natives-windows": {
                        "path": "com/example/gamma/3.0/gamma-3.0-natives-windows.jar",
                        "url": "https://example.invalid/libs/gamma-3.0-natives-windows.jar",
                        "sha1": "gamma-native-win",
                        "size": 31
                      }
                    }
                  },
                  "natives": {
                    "windows": "natives-windows"
                  }
                }
              ],
              "arguments": {
                "game": [
                  "--username",
                  "${'$'}{auth_player_name}",
                  "--version",
                  "${'$'}{version_name}",
                  "--accessToken",
                  "${'$'}{auth_access_token}"
                ],
                "jvm": [
                  "-Djava.library.path=${'$'}{natives_directory}",
                  "-cp",
                  "${'$'}{classpath}"
                ]
              }
            }
            """.trimIndent()
        )
    }
}

fun minecraftRuntimeClasspath() =
    project(":loader-core").the<SourceSetContainer>()["main"].runtimeClasspath +
        project(":loader-api").the<SourceSetContainer>()["main"].output

fun JavaExec.ensureRuntimeWorkingDir() {
    doFirst {
        workingDir.mkdirs()
    }
}

val prepareMinecraftServerLaunchFixture by tasks.registering {
    dependsOn(":sample-server-fixture:jar", prepareMilestone0Mods)

    doLast {
        val fixtureDirectory = layout.projectDirectory.dir("runtime/fixture-minecraft-server-launch").asFile
        val versionDirectory = fixtureDirectory.resolve("versions/26.1.2")
        versionDirectory.mkdirs()
        versionDirectory.resolve("26.1.2.json").writeText(
            """
            {
              "id": "26.1.2",
              "type": "release",
              "downloads": {
                "server": {
                  "url": "https://example.invalid/fake-server.jar",
                  "sha1": "fake-server-sha1",
                  "size": 123
                }
              },
              "libraries": [],
              "arguments": {
                "game": [],
                "jvm": []
              }
            }
            """.trimIndent()
        )

        val sourceJar = project(":sample-server-fixture").tasks.named<Jar>("jar").get().archiveFile.get().asFile
        sourceJar.copyTo(versionDirectory.resolve("26.1.2-server.jar"), overwrite = true)
    }
}

val prepareMinecraftBundledServerFixture by tasks.registering {
    dependsOn(":sample-server-fixture:jar", prepareMilestone0Mods)

    doLast {
        val fixtureDirectory = layout.projectDirectory.dir("runtime/fixture-minecraft-bundled-server").asFile
        val versionDirectory = fixtureDirectory.resolve("versions/26.1.2")
        versionDirectory.mkdirs()
        versionDirectory.resolve("26.1.2.json").writeText(
            """
            {
              "id": "26.1.2",
              "type": "release",
              "downloads": {
                "server": {
                  "url": "https://example.invalid/fake-bundled-server.jar",
                  "sha1": "fake-bundled-server-sha1",
                  "size": 123
                }
              },
              "libraries": [],
              "arguments": {
                "game": [],
                "jvm": []
              }
            }
            """.trimIndent()
        )

        val nestedServerJar = project(":sample-server-fixture").tasks.named<Jar>("jar").get().archiveFile.get().asFile
        val outerJar = versionDirectory.resolve("26.1.2-server.jar")
        val nestedBytes = nestedServerJar.readBytes()
        val nestedSha1 = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(nestedBytes))
        JarOutputStream(outerJar.outputStream()).use { jar ->
            fun entry(name: String, bytes: ByteArray) {
                jar.putNextEntry(JarEntry(name))
                jar.write(bytes)
                jar.closeEntry()
            }
            entry("META-INF/main-class", "com.mcmodloader.sampleserverfixture.FakeMinecraftServerMain\n".toByteArray())
            entry("META-INF/versions.list", "${nestedSha1}\tfixture\tfixture-server.jar\n".toByteArray())
            entry("META-INF/libraries.list", "${nestedSha1}\tfixture-lib\tfixture-lib.jar\n".toByteArray())
            entry("META-INF/versions/fixture-server.jar", nestedBytes)
            entry("META-INF/libraries/fixture-lib.jar", nestedBytes)
        }
    }
}

tasks.register<JavaExec>("runMilestone0") {
    group = "application"
    description = "Builds the sample mod and runs the Milestone 0 deterministic entrypoint loader."
    dependsOn(":loader-core:classes", ":sample-game:classes", ":loader-api:classes", prepareMilestone0Mods)

    val loaderCoreSourceSets = project(":loader-core").the<SourceSetContainer>()
    val sampleGameSourceSets = project(":sample-game").the<SourceSetContainer>()
    classpath =
        loaderCoreSourceSets["main"].runtimeClasspath +
            sampleGameSourceSets["main"].output

    mainClass.set("com.mcmodloader.core.LoaderMain")
    workingDir = layout.projectDirectory.dir("runtime").asFile
    args("--game-main", "com.mcmodloader.samplegame.SampleGameMain")

    ensureRuntimeWorkingDir()
}

tasks.register<JavaExec>("validateMilestone0") {
    group = "application"
    description = "Builds the sample mod and validates the frozen Milestone 0 modpack without launching the game."
    dependsOn(":loader-core:classes", ":sample-game:classes", ":loader-api:classes", prepareMilestone0Mods)

    val loaderCoreSourceSets = project(":loader-core").the<SourceSetContainer>()
    val sampleGameSourceSets = project(":sample-game").the<SourceSetContainer>()
    classpath =
        loaderCoreSourceSets["main"].runtimeClasspath +
            sampleGameSourceSets["main"].output

    mainClass.set("com.mcmodloader.core.LoaderMain")
    workingDir = layout.projectDirectory.dir("runtime").asFile
    args("--game-main", "com.mcmodloader.samplegame.SampleGameMain", "--validate-only")

    ensureRuntimeWorkingDir()
}

tasks.register<JavaExec>("explainMilestone0") {
    group = "application"
    description = "Builds the sample mod and prints an explain summary for the frozen Milestone 0 modpack."
    dependsOn(":loader-core:classes", ":sample-game:classes", ":loader-api:classes", prepareMilestone0Mods)

    val loaderCoreSourceSets = project(":loader-core").the<SourceSetContainer>()
    val sampleGameSourceSets = project(":sample-game").the<SourceSetContainer>()
    classpath =
        loaderCoreSourceSets["main"].runtimeClasspath +
            sampleGameSourceSets["main"].output

    mainClass.set("com.mcmodloader.core.LoaderMain")
    workingDir = layout.projectDirectory.dir("runtime").asFile
    args("--game-main", "com.mcmodloader.samplegame.SampleGameMain", "--validate-only", "--explain")

    ensureRuntimeWorkingDir()
}

tasks.register<JavaExec>("minecraftDryRun") {
    group = "application"
    description = "Builds the sample mod and runs the Minecraft client dry-run planner."
    dependsOn(":loader-core:classes", ":loader-api:classes", prepareMinecraftFixture)

    classpath = minecraftRuntimeClasspath()
    mainClass.set("com.mcmodloader.core.LoaderMain")
    workingDir = layout.projectDirectory.dir("runtime").asFile
    args(
        "--game-main",
        "unused.for.minecraft.DryRun",
        "--game-provider",
        "minecraft",
        "--minecraft-version",
        "26.1.2",
        "--minecraft-dir",
        "fixture-minecraft",
        "--minecraft-side",
        "client",
        "--minecraft-dry-run"
    )
    ensureRuntimeWorkingDir()
}

tasks.register<JavaExec>("minecraftServerDryRun") {
    group = "application"
    description = "Builds the sample mod and runs the Minecraft server dry-run planner."
    dependsOn(":loader-core:classes", ":loader-api:classes", prepareMinecraftFixture)

    classpath = minecraftRuntimeClasspath()
    mainClass.set("com.mcmodloader.core.LoaderMain")
    workingDir = layout.projectDirectory.dir("runtime").asFile
    args(
        "--game-main",
        "unused.for.minecraft.DryRun",
        "--game-provider",
        "minecraft",
        "--minecraft-version",
        "26.1.2",
        "--minecraft-dir",
        "fixture-minecraft",
        "--minecraft-side",
        "server",
        "--minecraft-dry-run"
    )
    ensureRuntimeWorkingDir()
}

tasks.register<JavaExec>("macheReferenceScan") {
    group = "application"
    description = "Runs the optional read-only Mache reference scan when -PmacheDir is provided."
    dependsOn(":loader-core:classes", ":loader-api:classes", prepareMinecraftFixture)

    classpath = minecraftRuntimeClasspath()
    mainClass.set("com.mcmodloader.core.LoaderMain")
    workingDir = layout.projectDirectory.dir("runtime").asFile
    onlyIf {
        val macheDir = providers.gradleProperty("macheDir").orNull
        if (macheDir.isNullOrBlank()) {
            println("No -PmacheDir provided; skipping Mache reference scan.")
            false
        } else {
            true
        }
    }

    doFirst {
        workingDir.mkdirs()
        val macheDir = providers.gradleProperty("macheDir").get()
        args(
            "--game-main",
            "unused.for.minecraft.DryRun",
            "--game-provider",
            "minecraft",
            "--minecraft-version",
            "26.1.2",
            "--minecraft-dir",
            "fixture-minecraft",
            "--minecraft-side",
            "server",
            "--minecraft-dry-run",
            "--mache-dir",
            macheDir,
            "--mache-version",
            "26.1.2",
            "--mache-reference-scan"
        )
    }
}

tasks.register<JavaExec>("minecraftServerLaunchFakeSmoke") {
    group = "application"
    description = "Runs the fake managed Minecraft server launch smoke task without Mojang jars."
    dependsOn(":loader-core:classes", ":loader-api:classes", prepareMinecraftServerLaunchFixture)

    classpath = minecraftRuntimeClasspath()
    mainClass.set("com.mcmodloader.core.LoaderMain")
    workingDir = layout.projectDirectory.dir("runtime").asFile
    args(
        "--game-main",
        "unused.for.minecraft.FakeServerLaunch",
        "--game-provider",
        "minecraft",
        "--minecraft-version",
        "26.1.2",
        "--minecraft-dir",
        "fixture-minecraft-server-launch",
        "--minecraft-side",
        "server",
        "--minecraft-dry-run",
        "--minecraft-verify-files",
        "--minecraft-launch",
        "--minecraft-launch-timeout-seconds",
        "10",
        "--minecraft-stop-after-ready",
        "--minecraft-ready-timeout-seconds",
        "5",
        "--minecraft-accept-eula-for-test",
        "--minecraft-output-plan",
        "minecraft-launch-plan.json"
    )
    ensureRuntimeWorkingDir()
}

tasks.register<JavaExec>("minecraftServerLaunchDrySmoke") {
    group = "application"
    description = "Launches a real local vanilla Minecraft server jar in managed dry-smoke mode when -PminecraftDir is provided."
    dependsOn(":loader-core:classes", ":loader-api:classes")
    classpath = minecraftRuntimeClasspath()
    mainClass.set("com.mcmodloader.core.LoaderMain")
    workingDir = layout.projectDirectory.dir("runtime").asFile
    onlyIf {
        val minecraftDir = providers.gradleProperty("minecraftDir").orNull
        if (minecraftDir.isNullOrBlank()) {
            println("No -PminecraftDir provided; skipping minecraftServerLaunchDrySmoke.")
            false
        } else {
            true
        }
    }

    doFirst {
        workingDir.mkdirs()
        val minecraftDir = providers.gradleProperty("minecraftDir").get()
        args(
            "--game-main",
            "unused.for.minecraft.ServerLaunch",
            "--game-provider",
            "minecraft",
            "--minecraft-version",
            "26.1.2",
            "--minecraft-dir",
            minecraftDir,
            "--minecraft-side",
            "server",
            "--minecraft-dry-run",
            "--minecraft-verify-files",
            "--minecraft-launch",
            "--minecraft-launch-timeout-seconds",
            "30",
            "--minecraft-server-arg",
            "nogui",
            "--minecraft-output-plan",
            "minecraft-launch-plan.json"
        )
    }
}

tasks.register<JavaExec>("minecraftServerCacheInspect") {
    group = "application"
    description = "Inspects the managed vanilla server artifact cache without launching Minecraft."
    dependsOn(":loader-core:classes", ":loader-api:classes")

    classpath = minecraftRuntimeClasspath()
    mainClass.set("com.mcmodloader.core.LoaderMain")
    workingDir = layout.projectDirectory.dir("runtime").asFile
    args(
        "--game-main",
        "unused.for.minecraft.CacheInspect",
        "--game-provider",
        "minecraft",
        "--minecraft-version",
        "26.1.2",
        "--minecraft-side",
        "server",
        "--minecraft-dry-run",
        "--minecraft-cache-inspect"
    )
    ensureRuntimeWorkingDir()
}

tasks.register<JavaExec>("minecraftServerOfflineCacheCheck") {
    group = "application"
    description = "Verifies the managed vanilla server artifact cache in offline strict mode."
    dependsOn(":loader-core:classes", ":loader-api:classes")

    classpath = minecraftRuntimeClasspath()
    mainClass.set("com.mcmodloader.core.LoaderMain")
    workingDir = layout.projectDirectory.dir("runtime").asFile
    args(
        "--game-main",
        "unused.for.minecraft.OfflineCacheCheck",
        "--game-provider",
        "minecraft",
        "--minecraft-version",
        "26.1.2",
        "--minecraft-side",
        "server",
        "--minecraft-dry-run",
        "--minecraft-offline",
        "--minecraft-verify-files",
        "--minecraft-cache-strict"
    )
    ensureRuntimeWorkingDir()
}

tasks.register<JavaExec>("minecraftServerCacheRepair") {
    group = "application"
    description = "Repairs cached vanilla server metadata and the server jar when explicitly allowed."
    dependsOn(":loader-core:classes", ":loader-api:classes")

    classpath = minecraftRuntimeClasspath()
    mainClass.set("com.mcmodloader.core.LoaderMain")
    workingDir = layout.projectDirectory.dir("runtime").asFile
    args(
        "--game-main",
        "unused.for.minecraft.CacheRepair",
        "--game-provider",
        "minecraft",
        "--minecraft-version",
        "26.1.2",
        "--minecraft-side",
        "server",
        "--minecraft-dry-run",
        "--minecraft-fetch-metadata",
        "--minecraft-download-server",
        "--minecraft-cache-repair",
        "--minecraft-verify-files"
    )
    ensureRuntimeWorkingDir()
}

tasks.register<JavaExec>("minecraftServerDownloadSmoke") {
    group = "application"
    description = "Downloads and verifies the vanilla Minecraft server artifact, then attempts a managed launch."
    dependsOn(":loader-core:classes", ":loader-api:classes")

    classpath = minecraftRuntimeClasspath()
    mainClass.set("com.mcmodloader.core.LoaderMain")
    workingDir = layout.projectDirectory.dir("runtime").asFile
    args(
        "--game-main",
        "unused.for.minecraft.ServerDownloadSmoke",
        "--game-provider",
        "minecraft",
        "--minecraft-version",
        "26.1.2",
        "--minecraft-side",
        "server",
        "--minecraft-dry-run",
        "--minecraft-fetch-metadata",
        "--minecraft-download-server",
        "--minecraft-verify-files",
        "--minecraft-launch",
        "--minecraft-launch-timeout-seconds",
        "30",
        "--minecraft-server-arg",
        "nogui",
        "--minecraft-output-plan",
        "minecraft-launch-plan.json"
    )
    ensureRuntimeWorkingDir()
}

fun JavaExec.configureRealBaselineTask(
    taskMain: String,
    includeFetch: Boolean,
    includeDownloadServer: Boolean,
    includeLaunch: Boolean,
    extraArgs: List<String> = emptyList()
) {
    group = "application"
    dependsOn(":loader-core:classes", ":loader-api:classes")
    classpath = minecraftRuntimeClasspath()
    mainClass.set("com.mcmodloader.core.LoaderMain")
    workingDir = layout.projectDirectory.dir("runtime").asFile

    doFirst {
        val mcRealVersion = providers.gradleProperty("mcRealVersion").orElse("latest-release").get()
        val argsList =
            mutableListOf(
                "--game-main",
                taskMain,
                "--game-provider",
                "minecraft",
                "--minecraft-side",
                "server",
                "--minecraft-dry-run",
                "--minecraft-baseline-server",
                "--minecraft-baseline-version",
                mcRealVersion,
                "--minecraft-verify-files",
                "--minecraft-real-smoke"
            )
        if (includeFetch) {
            argsList += "--minecraft-fetch-metadata"
        }
        if (includeDownloadServer) {
            argsList += "--minecraft-download-server"
        }
        if (includeLaunch) {
            argsList += "--minecraft-launch"
        }
        argsList += extraArgs
        setArgs(argsList)
    }
    ensureRuntimeWorkingDir()
}

tasks.register<JavaExec>("minecraftRealServerAcquire") {
    description = "Explicitly resolves, downloads, verifies, and reports a real official vanilla server baseline without launching it."
    configureRealBaselineTask(
        "unused.for.minecraft.RealAcquire",
        includeFetch = true,
        includeDownloadServer = true,
        includeLaunch = false
    )
}

tasks.register<JavaExec>("minecraftRealServerSmoke") {
    description = "Explicitly resolves a real official vanilla server baseline and attempts a managed server launch."
    configureRealBaselineTask(
        "unused.for.minecraft.RealSmoke",
        includeFetch = true,
        includeDownloadServer = true,
        includeLaunch = true,
        extraArgs =
            listOf(
                "--minecraft-launch-timeout-seconds",
                "30",
                "--minecraft-server-arg",
                "nogui"
            )
    )
}

tasks.register<JavaExec>("minecraftRealServerEulaSmoke") {
    description =
        "Local-only real vanilla server smoke that writes eula=true, waits for readiness, and stops the server. Run only if you accept Mojang's EULA."
    configureRealBaselineTask(
        "unused.for.minecraft.RealEulaSmoke",
        includeFetch = true,
        includeDownloadServer = true,
        includeLaunch = true,
        extraArgs =
            listOf(
                "--minecraft-accept-eula-for-test",
                "--minecraft-stop-after-ready",
                "--minecraft-require-ready",
                "--minecraft-ready-timeout-seconds",
                "60",
                "--minecraft-launch-timeout-seconds",
                "90",
                "--minecraft-server-arg",
                "nogui"
            )
    )
}

tasks.register<JavaExec>("minecraftRealServerOfflineReplay") {
    description = "Explicit offline replay of the verified real vanilla server baseline cache with zero network requests."
    configureRealBaselineTask(
        "unused.for.minecraft.OfflineReplay",
        includeFetch = false,
        includeDownloadServer = false,
        includeLaunch = false,
        extraArgs =
            listOf(
                "--minecraft-offline",
                "--minecraft-offline-replay",
                "--minecraft-cache-strict"
            )
    )
}

fun JavaExec.configureMinecraftServerFixtureTask(taskMain: String, fixtureTask: TaskProvider<*>, fixtureDir: String, extraArgs: List<String>) {
    group = "application"
    dependsOn(":loader-core:classes", ":loader-api:classes", prepareMilestone0Mods, fixtureTask)
    classpath = minecraftRuntimeClasspath()
    mainClass.set("com.mcmodloader.core.LoaderMain")
    workingDir = layout.projectDirectory.dir("runtime").asFile
    args(
        listOf(
            "--game-main",
            taskMain,
            "--game-provider",
            "minecraft",
            "--minecraft-version",
            "26.1.2",
            "--minecraft-dir",
            fixtureDir,
            "--minecraft-side",
            "server",
            "--minecraft-dry-run",
            "--minecraft-verify-files"
        ) + extraArgs
    )
    ensureRuntimeWorkingDir()
}

tasks.register<JavaExec>("minecraftServerRuntimePlan") {
    description = "Writes the Mega-Milestone 7 deterministic Minecraft server runtime plan for a simple fixture server."
    configureMinecraftServerFixtureTask(
        "unused.for.minecraft.RuntimePlan",
        prepareMinecraftServerLaunchFixture,
        "fixture-minecraft-server-launch",
        listOf("--minecraft-runtime-plan")
    )
}

tasks.register<JavaExec>("minecraftServerBundledFixtureSmoke") {
    description = "Writes the Mega-Milestone 7 runtime plan for a fake bundled server fixture without network."
    configureMinecraftServerFixtureTask(
        "unused.for.minecraft.BundledRuntimePlan",
        prepareMinecraftBundledServerFixture,
        "fixture-minecraft-bundled-server",
        listOf("--minecraft-runtime-plan", "--minecraft-boundary-report")
    )
}

tasks.register<JavaExec>("minecraftServerRuntimeBoundary") {
    description = "Writes the Mega-Milestone 7 Minecraft runtime boundary report."
    configureMinecraftServerFixtureTask(
        "unused.for.minecraft.RuntimeBoundary",
        prepareMinecraftServerLaunchFixture,
        "fixture-minecraft-server-launch",
        listOf("--minecraft-runtime-plan", "--minecraft-boundary-report")
    )
}

tasks.register<JavaExec>("minecraftModIntegrationPlan") {
    description = "Writes the Mega-Milestone 7 analysis-only Minecraft mod integration plan."
    configureMinecraftServerFixtureTask(
        "unused.for.minecraft.ModIntegrationPlan",
        prepareMinecraftServerLaunchFixture,
        "fixture-minecraft-server-launch",
        listOf("--minecraft-runtime-plan", "--minecraft-boundary-report", "--minecraft-integration-plan")
    )
}

tasks.register<JavaExec>("minecraftModBoundaryExplain") {
    description = "Prints Mega-Milestone 7 mod and boundary explain output."
    configureMinecraftServerFixtureTask(
        "unused.for.minecraft.ModBoundaryExplain",
        prepareMinecraftServerLaunchFixture,
        "fixture-minecraft-server-launch",
        listOf("--minecraft-runtime-plan", "--minecraft-boundary-report", "--minecraft-integration-plan", "--minecraft-explain-boundary", "--minecraft-explain-mods")
    )
}

tasks.register<JavaExec>("minecraftRuntimeExplain") {
    description = "Prints Mega-Milestone 7 runtime explain output."
    configureMinecraftServerFixtureTask(
        "unused.for.minecraft.RuntimeExplain",
        prepareMinecraftServerLaunchFixture,
        "fixture-minecraft-server-launch",
        listOf("--minecraft-runtime-plan", "--minecraft-explain-runtime")
    )
}

tasks.register<JavaExec>("minecraftPreflight") {
    description = "Runs the Mega-Milestone 7 Minecraft-aware preflight without launching Minecraft."
    configureMinecraftServerFixtureTask(
        "unused.for.minecraft.Preflight",
        prepareMinecraftServerLaunchFixture,
        "fixture-minecraft-server-launch",
        listOf("--minecraft-preflight")
    )
}

tasks.register<JavaExec>("minecraftPreflightStrictSmoke") {
    description = "Runs the Mega-Milestone 7 strict Minecraft-aware preflight smoke."
    configureMinecraftServerFixtureTask(
        "unused.for.minecraft.PreflightStrict",
        prepareMinecraftServerLaunchFixture,
        "fixture-minecraft-server-launch",
        listOf("--minecraft-preflight", "--minecraft-strict-boundary", "--minecraft-strict-runtime-conflicts", "--minecraft-strict-side", "--minecraft-strict-class-versions")
    )
}

tasks.register<JavaExec>("minecraftPreflightOfflineReplay") {
    description = "Runs the Mega-Milestone 7 offline preflight using only cached fixture inputs."
    configureMinecraftServerFixtureTask(
        "unused.for.minecraft.PreflightOfflineReplay",
        prepareMinecraftServerLaunchFixture,
        "fixture-minecraft-server-launch",
        listOf("--minecraft-offline-preflight")
    )
}

tasks.register<JavaExec>("minecraftReproducibilityCheck") {
    description = "Writes the Mega-Milestone 7 reproducibility check report for deterministic planning outputs."
    configureMinecraftServerFixtureTask(
        "unused.for.minecraft.Reproducibility",
        prepareMinecraftServerLaunchFixture,
        "fixture-minecraft-server-launch",
        listOf("--minecraft-reproducibility-check")
    )
}

tasks.register<JavaExec>("minecraftRealServerRuntimeAcquire") {
    description = "Explicitly resolves and reports a real official vanilla server runtime plan without launching it."
    configureRealBaselineTask(
        "unused.for.minecraft.RealRuntimeAcquire",
        includeFetch = true,
        includeDownloadServer = true,
        includeLaunch = false,
        extraArgs = listOf("--minecraft-runtime-plan", "--minecraft-boundary-report")
    )
}

tasks.register<JavaExec>("minecraftRealServerRuntimeSmoke") {
    description = "Explicitly resolves a real official vanilla server runtime plan and attempts managed launch."
    configureRealBaselineTask(
        "unused.for.minecraft.RealRuntimeSmoke",
        includeFetch = true,
        includeDownloadServer = true,
        includeLaunch = true,
        extraArgs = listOf("--minecraft-runtime-plan", "--minecraft-launch-timeout-seconds", "30", "--minecraft-server-arg", "nogui")
    )
}

tasks.register<JavaExec>("minecraftRealServerRuntimeOfflineReplay") {
    description = "Explicit offline replay for the Mega-Milestone 7 real server runtime plan."
    configureRealBaselineTask(
        "unused.for.minecraft.RealRuntimeOfflineReplay",
        includeFetch = false,
        includeDownloadServer = false,
        includeLaunch = false,
        extraArgs = listOf("--minecraft-offline", "--minecraft-offline-replay", "--minecraft-cache-strict", "--minecraft-runtime-plan", "--minecraft-boundary-report")
    )
}

tasks.register("minecraftMegaMilestone7Check") {
    group = "verification"
    description = "Runs focused Mega-Milestone 7 fixture checks without network."
    dependsOn(
        "minecraftServerRuntimePlan",
        "minecraftServerBundledFixtureSmoke",
        "minecraftServerRuntimeBoundary",
        "minecraftModIntegrationPlan",
        "minecraftPreflight",
        "minecraftReproducibilityCheck",
        ":loader-core:test"
    )
}

val prepareMinecraftMilestone8Fixture by tasks.registering {
    dependsOn(":sample-server-fixture:jar", ":sample-minecraft-mod:jar")

    doLast {
        val runtimeDirectory = layout.projectDirectory.dir("runtime/milestone8").asFile
        val versionDirectory = runtimeDirectory.resolve("minecraft/versions/26.1.2")
        val modsDirectory = runtimeDirectory.resolve("mods")
        versionDirectory.mkdirs()
        modsDirectory.mkdirs()
        versionDirectory.resolve("26.1.2.json").writeText(
            """
            {
              "id": "26.1.2",
              "type": "release",
              "downloads": {
                "server": {
                  "url": "https://example.invalid/fake-server.jar",
                  "sha1": "fake-server-sha1",
                  "size": 123
                }
              },
              "libraries": [],
              "arguments": {
                "game": [],
                "jvm": []
              }
            }
            """.trimIndent()
        )
        val serverJar = project(":sample-server-fixture").tasks.named<Jar>("jar").get().archiveFile.get().asFile
        val modJar = project(":sample-minecraft-mod").tasks.named<Jar>("jar").get().archiveFile.get().asFile
        serverJar.copyTo(versionDirectory.resolve("26.1.2-server.jar"), overwrite = true)
        modJar.copyTo(modsDirectory.resolve("sample-minecraft-mod.jar"), overwrite = true)
    }
}

fun JavaExec.configureMinecraftMilestone8Task(taskMain: String, extraArgs: List<String>) {
    group = "application"
    dependsOn(":loader-core:classes", ":loader-api:classes", prepareMinecraftMilestone8Fixture)
    classpath = minecraftRuntimeClasspath()
    mainClass.set("com.mcmodloader.core.LoaderMain")
    workingDir = layout.projectDirectory.dir("runtime/milestone8").asFile
    args(
        listOf(
            "--game-main",
            taskMain,
            "--game-provider",
            "minecraft",
            "--minecraft-version",
            "26.1.2",
            "--minecraft-dir",
            "minecraft",
            "--minecraft-side",
            "server",
            "--minecraft-dry-run",
            "--minecraft-verify-files",
            "--minecraft-deny-loader-internals",
            "--minecraft-verify-plan-fingerprints"
        ) + extraArgs
    )
    ensureRuntimeWorkingDir()
}

tasks.register<JavaExec>("minecraftModExecutionPlan") {
    description = "Writes the Milestone 8 deterministic Minecraft mod execution plan."
    configureMinecraftMilestone8Task(
        "unused.for.minecraft.Milestone8ExecutionPlan",
        listOf("--minecraft-execution-plan")
    )
}

tasks.register<JavaExec>("minecraftBootstrapClassloaderGraph") {
    description = "Writes the Milestone 8 deterministic Minecraft bootstrap classloader graph."
    configureMinecraftMilestone8Task(
        "unused.for.minecraft.Milestone8ClassloaderGraph",
        listOf("--minecraft-bootstrap-classloader-graph")
    )
}

tasks.register<JavaExec>("minecraftServerBootstrapFakeSmoke") {
    description = "Runs the Milestone 8 fake Minecraft bootstrap smoke."
    configureMinecraftMilestone8Task(
        "unused.for.minecraft.Milestone8BootstrapFake",
        listOf("--minecraft-bootstrap-server", "--minecraft-bootstrap-fake-server", "--minecraft-server-arg", "--bootstrap-marker", "--minecraft-server-arg", "fake-server-main.marker")
    )
}

tasks.register<JavaExec>("minecraftServerModExecutionFakeSmoke") {
    description = "Runs the Milestone 8 approved Minecraft server mod execution smoke."
    configureMinecraftMilestone8Task(
        "unused.for.minecraft.Milestone8ModExecutionFake",
        listOf("--minecraft-bootstrap-server", "--minecraft-bootstrap-fake-server", "--minecraft-server-arg", "--bootstrap-marker", "--minecraft-server-arg", "fake-server-main.marker")
    )
}

tasks.register<JavaExec>("minecraftServerModExecutionOfflineReplay") {
    description = "Runs the Milestone 8 offline bootstrap replay smoke."
    configureMinecraftMilestone8Task(
        "unused.for.minecraft.Milestone8OfflineReplay",
        listOf("--minecraft-bootstrap-server", "--minecraft-bootstrap-offline", "--minecraft-bootstrap-fake-server", "--minecraft-server-arg", "--bootstrap-marker", "--minecraft-server-arg", "fake-server-main.marker")
    )
}

fun registerMilestone8FocusedTest(taskName: String, includePattern: String, descriptionText: String) {
    tasks.register<Test>(taskName) {
        group = "verification"
        description = descriptionText
        dependsOn(":loader-core:testClasses")
        testClassesDirs = project(":loader-core").the<SourceSetContainer>()["test"].output.classesDirs
        classpath = project(":loader-core").the<SourceSetContainer>()["test"].runtimeClasspath
        useJUnitPlatform()
        binaryResultsDirectory.set(layout.buildDirectory.dir("milestone8-test-results/$taskName/binary"))
        reports.html.outputLocation.set(layout.buildDirectory.dir("reports/$taskName"))
        reports.junitXml.outputLocation.set(layout.buildDirectory.dir("test-results/$taskName"))
        filter {
            includeTestsMatching(includePattern)
        }
    }
}

registerMilestone8FocusedTest(
    "minecraftServerModCanarySmoke",
    "com.mcmodloader.core.Milestone8MinecraftBootstrapExecutionTest.canary*",
    "Runs the Milestone 8 canary smoke proving preflight stays non-executing."
)

registerMilestone8FocusedTest(
    "minecraftServerBootstrapDriftSmoke",
    "com.mcmodloader.core.Milestone8MinecraftBootstrapExecutionTest.*Drift*",
    "Runs the Milestone 8 plan drift smoke tests."
)

registerMilestone8FocusedTest(
    "minecraftServerBootstrapStrictSmoke",
    "com.mcmodloader.core.Milestone8MinecraftBootstrapExecutionTest.strictExecutionFailsOnWarningsPromotedToFatal",
    "Runs the Milestone 8 strict execution smoke tests."
)

tasks.register("minecraftMilestone8Check") {
    group = "verification"
    description = "Runs focused Milestone 8 execution planning, bootstrap, and bootstrap-guard checks."
    dependsOn(
        "minecraftModExecutionPlan",
        "minecraftBootstrapClassloaderGraph",
        "minecraftServerBootstrapFakeSmoke",
        "minecraftServerModExecutionFakeSmoke",
        "minecraftServerModExecutionOfflineReplay",
        "minecraftServerModCanarySmoke",
        "minecraftServerBootstrapDriftSmoke",
        "minecraftServerBootstrapStrictSmoke",
        "minecraftMegaMilestone7Check",
        ":loader-core:test"
    )
}
