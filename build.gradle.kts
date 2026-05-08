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
    setExecutable(System.getenv("JAVA_HOME")?.let { "$it\\bin\\java.exe" } ?: "java")

    doFirst {
        workingDir.mkdirs()
    }
}
