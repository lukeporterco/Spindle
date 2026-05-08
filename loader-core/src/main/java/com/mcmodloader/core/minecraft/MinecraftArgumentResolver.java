package com.mcmodloader.core.minecraft;

import com.mcmodloader.core.LoaderMain;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MinecraftArgumentResolver {
    public ResolvedArguments resolve(
        MinecraftProviderConfig config,
        MinecraftVersionMetadata metadata,
        Path minecraftDirectory,
        Path assetsRoot,
        Path nativesDirectory,
        List<Path> classpathEntries
    ) {
        if (config.side() == MinecraftSide.SERVER) {
            return resolveServer(metadata, minecraftDirectory, assetsRoot, nativesDirectory, classpathEntries);
        }
        return resolveClient(config, metadata, minecraftDirectory, assetsRoot, nativesDirectory, classpathEntries);
    }

    private ResolvedArguments resolveClient(
        MinecraftProviderConfig config,
        MinecraftVersionMetadata metadata,
        Path minecraftDirectory,
        Path assetsRoot,
        Path nativesDirectory,
        List<Path> classpathEntries
    ) {
        String classpathValue = classpathEntries.stream().map(path -> path.toString()).reduce((left, right) -> left + File.pathSeparator + right).orElse("");
        Map<String, String> substitutions = new LinkedHashMap<>();
        substitutions.put("${auth_player_name}", "offline_player");
        substitutions.put("${version_name}", metadata.id());
        substitutions.put("${game_directory}", safePath(minecraftDirectory));
        substitutions.put("${assets_root}", safePath(assetsRoot));
        substitutions.put("${assets_index_name}", metadata.assetIndex() == null ? "" : metadata.assetIndex().id());
        substitutions.put("${auth_uuid}", new UUID(0L, 0L).toString());
        substitutions.put("${auth_access_token}", "REDACTED");
        substitutions.put("${clientid}", "");
        substitutions.put("${auth_xuid}", "");
        substitutions.put("${user_type}", "legacy");
        substitutions.put("${version_type}", metadata.type() == null || metadata.type().isBlank() ? "release" : metadata.type());
        substitutions.put("${natives_directory}", safePath(nativesDirectory));
        substitutions.put("${launcher_name}", "MCModLoader");
        substitutions.put("${launcher_version}", LoaderMain.LOADER_VERSION);
        substitutions.put("${classpath}", classpathValue);

        List<String> jvmArguments = resolveArgumentEntries(metadata.arguments().jvm(), substitutions);
        List<String> gameArguments = resolveGameArguments(metadata, substitutions);
        List<String> commandPreview = new ArrayList<>();
        commandPreview.add("java");
        commandPreview.addAll(jvmArguments);
        commandPreview.add(metadata.mainClass());
        commandPreview.addAll(gameArguments);
        return new ResolvedArguments(jvmArguments, gameArguments, commandPreview);
    }

    private ResolvedArguments resolveServer(
        MinecraftVersionMetadata metadata,
        Path minecraftDirectory,
        Path assetsRoot,
        Path nativesDirectory,
        List<Path> classpathEntries
    ) {
        String classpathValue = classpathEntries.stream().map(path -> path.toString()).reduce((left, right) -> left + File.pathSeparator + right).orElse("");
        Map<String, String> substitutions = new LinkedHashMap<>();
        substitutions.put("${version_name}", metadata.id());
        substitutions.put("${game_directory}", safePath(minecraftDirectory));
        substitutions.put("${assets_root}", safePath(assetsRoot));
        substitutions.put("${assets_index_name}", metadata.assetIndex() == null ? "" : metadata.assetIndex().id());
        substitutions.put("${version_type}", metadata.type() == null || metadata.type().isBlank() ? "release" : metadata.type());
        substitutions.put("${natives_directory}", safePath(nativesDirectory));
        substitutions.put("${launcher_name}", "MCModLoader");
        substitutions.put("${launcher_version}", LoaderMain.LOADER_VERSION);
        substitutions.put("${classpath}", classpathValue);

        List<String> jvmArguments = resolveArgumentEntries(metadata.arguments().jvm(), substitutions);
        List<String> gameArguments = List.of("nogui");
        Path serverJarPath = classpathEntries.isEmpty() ? null : classpathEntries.getLast();
        List<String> commandPreview = new ArrayList<>();
        commandPreview.add("java");
        commandPreview.addAll(jvmArguments);
        if (serverJarPath != null) {
            commandPreview.add("-jar");
            commandPreview.add(serverJarPath.toString());
        } else if (metadata.mainClass() != null && !metadata.mainClass().isBlank()) {
            commandPreview.add(metadata.mainClass());
        }
        commandPreview.addAll(gameArguments);
        return new ResolvedArguments(jvmArguments, gameArguments, commandPreview);
    }

    private String safePath(Path path) {
        return path == null ? "" : path.toString();
    }

    private List<String> resolveGameArguments(MinecraftVersionMetadata metadata, Map<String, String> substitutions) {
        if (!metadata.arguments().game().isEmpty()) {
            return resolveArgumentEntries(metadata.arguments().game(), substitutions);
        }
        if (metadata.legacyMinecraftArguments() == null || metadata.legacyMinecraftArguments().isBlank()) {
            return List.of();
        }
        return tokenizeLegacyArguments(substitute(metadata.legacyMinecraftArguments(), substitutions));
    }

    private List<String> resolveArgumentEntries(List<MinecraftVersionMetadata.Argument> arguments, Map<String, String> substitutions) {
        List<String> values = new ArrayList<>();
        MinecraftLibrarySelector.OperatingSystem operatingSystem = MinecraftLibrarySelector.OperatingSystem.detect();
        for (MinecraftVersionMetadata.Argument argument : arguments) {
            if (!MinecraftLibrarySelector.allows(argument.rules(), operatingSystem)) {
                continue;
            }
            for (String value : argument.values()) {
                values.add(substitute(value, substitutions));
            }
        }
        return values;
    }

    private String substitute(String value, Map<String, String> substitutions) {
        String resolved = value;
        for (Map.Entry<String, String> entry : substitutions.entrySet()) {
            resolved = resolved.replace(entry.getKey(), entry.getValue());
        }
        return resolved;
    }

    private List<String> tokenizeLegacyArguments(String value) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (character == '"') {
                quoted = !quoted;
                continue;
            }
            if (Character.isWhitespace(character) && !quoted) {
                if (!current.isEmpty()) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
                continue;
            }
            current.append(character);
        }
        if (!current.isEmpty()) {
            tokens.add(current.toString());
        }
        return tokens;
    }

    public record ResolvedArguments(List<String> jvmArguments, List<String> gameArguments, List<String> commandPreview) {
        public ResolvedArguments {
            jvmArguments = List.copyOf(jvmArguments);
            gameArguments = List.copyOf(gameArguments);
            commandPreview = List.copyOf(commandPreview);
        }
    }
}
