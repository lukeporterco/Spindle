package com.mcmodloader.core.minecraft;

import java.util.List;

public record MinecraftLaunchPlan(
    int schema,
    String provider,
    String projectTargetMinecraft,
    String baselineMinecraft,
    String minecraftVersion,
    String side,
    String mainClass,
    String minecraftDirectory,
    String serverJarSource,
    String versionJson,
    String gameJar,
    String serverJar,
    AssetIndex assetIndex,
    List<Library> libraries,
    List<Library> nativeLibraries,
    List<String> classpath,
    List<String> jvmArguments,
    List<String> gameArguments,
    List<String> commandPreview,
    List<String> missingFiles,
    boolean modJarsOnMinecraftClasspath,
    Metadata metadata
) {
    public MinecraftLaunchPlan {
        libraries = List.copyOf(libraries);
        nativeLibraries = List.copyOf(nativeLibraries);
        classpath = List.copyOf(classpath);
        jvmArguments = List.copyOf(jvmArguments);
        gameArguments = List.copyOf(gameArguments);
        commandPreview = List.copyOf(commandPreview);
        missingFiles = List.copyOf(missingFiles);
    }

    public MinecraftLaunchPlan withMissingFiles(List<String> missingFiles) {
        return new MinecraftLaunchPlan(
            schema,
            provider,
            projectTargetMinecraft,
            baselineMinecraft,
            minecraftVersion,
            side,
            mainClass,
            minecraftDirectory,
            serverJarSource,
            versionJson,
            gameJar,
            serverJar,
            assetIndex,
            libraries,
            nativeLibraries,
            classpath,
            jvmArguments,
            gameArguments,
            commandPreview,
            missingFiles,
            modJarsOnMinecraftClasspath,
            metadata
        );
    }

    public record AssetIndex(String id, String path, String sha1, long size) {
    }

    public record Library(String name, String path, String sha1, long size, boolean nativeLibrary) {
    }

    public record Metadata(String type, String assets) {
    }
}
