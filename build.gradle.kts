import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType

plugins {
    base
}

allprojects {
    group = "com.mcmodloader"
    version = "0.1.0"
}

subprojects {
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

val prepareMinecraftServerLaunchFixture by tasks.registering {
    dependsOn(":sample-server-fixture:jar")

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

tasks.register<JavaExec>("runMilestone0") {
    group = "application"
    description = "Builds the sample mod and runs the Milestone 0 deterministic entrypoint loader."
    dependsOn(":loader-core:classes", ":sample-game:classes", ":loader-api:classes", prepareMilestone0Mods)

    val loaderCoreSourceSets = project(":loader-core").the<SourceSetContainer>()
    val sampleGameSourceSets = project(":sample-game").the<SourceSetContainer>()
    val loaderApiSourceSets = project(":loader-api").the<SourceSetContainer>()

    classpath =
        loaderCoreSourceSets["main"].runtimeClasspath +
            sampleGameSourceSets["main"].output +
            loaderApiSourceSets["main"].output

    mainClass.set("com.mcmodloader.core.LoaderMain")
    workingDir = layout.projectDirectory.dir("runtime").asFile
    args("--game-main", "com.mcmodloader.samplegame.SampleGameMain")

    doFirst {
        workingDir.mkdirs()
    }
}

tasks.register<JavaExec>("validateMilestone0") {
    group = "application"
    description = "Builds the sample mod and validates the frozen Milestone 0 modpack without launching the game."
    dependsOn(":loader-core:classes", ":sample-game:classes", ":loader-api:classes", prepareMilestone0Mods)

    val loaderCoreSourceSets = project(":loader-core").the<SourceSetContainer>()
    val sampleGameSourceSets = project(":sample-game").the<SourceSetContainer>()
    val loaderApiSourceSets = project(":loader-api").the<SourceSetContainer>()

    classpath =
        loaderCoreSourceSets["main"].runtimeClasspath +
            sampleGameSourceSets["main"].output +
            loaderApiSourceSets["main"].output

    mainClass.set("com.mcmodloader.core.LoaderMain")
    workingDir = layout.projectDirectory.dir("runtime").asFile
    args("--game-main", "com.mcmodloader.samplegame.SampleGameMain", "--validate-only")

    doFirst {
        workingDir.mkdirs()
    }
}

tasks.register<JavaExec>("explainMilestone0") {
    group = "application"
    description = "Builds the sample mod and prints an explain summary for the frozen Milestone 0 modpack."
    dependsOn(":loader-core:classes", ":sample-game:classes", ":loader-api:classes", prepareMilestone0Mods)

    val loaderCoreSourceSets = project(":loader-core").the<SourceSetContainer>()
    val sampleGameSourceSets = project(":sample-game").the<SourceSetContainer>()
    val loaderApiSourceSets = project(":loader-api").the<SourceSetContainer>()

    classpath =
        loaderCoreSourceSets["main"].runtimeClasspath +
            sampleGameSourceSets["main"].output +
            loaderApiSourceSets["main"].output

    mainClass.set("com.mcmodloader.core.LoaderMain")
    workingDir = layout.projectDirectory.dir("runtime").asFile
    args("--game-main", "com.mcmodloader.samplegame.SampleGameMain", "--validate-only", "--explain")

    doFirst {
        workingDir.mkdirs()
    }
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

    doFirst {
        workingDir.mkdirs()
    }
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

    doFirst {
        workingDir.mkdirs()
    }
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
        workingDir.mkdirs()
    }
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
