import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "spindle"

include(
    "spindle-loader-api",
    "spindle-loader-core",
    "spindle-loader-cli",
    "target-minecraft",
    "sample-game",
    "sample-mod",
    "sample-runtime-mod",
    "sample-server-fixture",
    "sample-minecraft-mod")
