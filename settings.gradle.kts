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
    "loader-api",
    "loader-core",
    "sample-game",
    "sample-mod",
    "sample-runtime-mod",
    "sample-server-fixture",
    "sample-minecraft-mod")
