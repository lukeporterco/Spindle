package com.mcmodloader.core.classpath;

import java.nio.file.Path;
import java.util.List;

public record RuntimeClasspathPlan(List<Path> modJars, List<Path> gameClasspathEntries, List<Path> apiClasspathEntries) {
    public RuntimeClasspathPlan {
        modJars = normalize(modJars);
        gameClasspathEntries = normalize(gameClasspathEntries);
        apiClasspathEntries = normalize(apiClasspathEntries);
    }

    public List<String> modJarDisplayPaths(Path workingDirectory) {
        return displayPaths(workingDirectory, modJars);
    }

    public List<String> gameClasspathDisplayPaths(Path workingDirectory) {
        return displayPaths(workingDirectory, gameClasspathEntries);
    }

    public List<String> apiClasspathDisplayPaths(Path workingDirectory) {
        return displayPaths(workingDirectory, apiClasspathEntries);
    }

    private static List<Path> normalize(List<Path> paths) {
        return paths.stream().map(path -> path.toAbsolutePath().normalize()).toList();
    }

    private static List<String> displayPaths(Path workingDirectory, List<Path> paths) {
        Path normalizedWorkingDirectory = workingDirectory.toAbsolutePath().normalize();
        return paths
            .stream()
            .map(path -> {
                try {
                    return normalizedWorkingDirectory.relativize(path).toString().replace('\\', '/');
                } catch (IllegalArgumentException exception) {
                    return path.toString().replace('\\', '/');
                }
            })
            .toList();
    }
}
