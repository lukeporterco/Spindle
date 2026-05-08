package com.mcmodloader.core.minecraft;

import com.mcmodloader.core.diagnostics.LoaderException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MinecraftMetadataResolver {
    public static final String DEFAULT_MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

    private final MinecraftVersionManifestParser manifestParser = new MinecraftVersionManifestParser();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void validateAvailability(Path workingDirectory, MinecraftProviderConfig config) throws LoaderException {
        if (!config.dryRun()) {
            throw new LoaderException("Minecraft provider requires --minecraft-dry-run for managed metadata and runtime planning.");
        }
        if (config.requestedVersion() == null || config.requestedVersion().isBlank()) {
            throw new LoaderException("Minecraft provider requires --minecraft-version unless --minecraft-version-json contains an id");
        }
        if (config.cacheInspect()) {
            return;
        }

        if (config.explicitVersionJson() != null) {
            if (!Files.isRegularFile(config.explicitVersionJson())) {
                throw new LoaderException("Minecraft version JSON does not exist: " + config.explicitVersionJson());
            }
            return;
        }

        Path localVersionJson =
            config.minecraftDirectory() == null ? null : new MinecraftInstallLocator().versionJsonPath(config.minecraftDirectory(), config.requestedVersion());
        if (localVersionJson != null && Files.isRegularFile(localVersionJson)) {
            return;
        }

        Path cachedVersionJson =
            config.cacheDirectory() == null
                ? workingDirectory.resolve("minecraft-cache/metadata/versions").resolve(config.requestedVersion() + ".json").toAbsolutePath().normalize()
                : config.cacheDirectory().resolve("metadata/versions").resolve(config.requestedVersion() + ".json").toAbsolutePath().normalize();
        if (Files.isRegularFile(cachedVersionJson)) {
            return;
        }

        if (config.manifestJson() != null) {
            if (!Files.isRegularFile(config.manifestJson())) {
                throw new LoaderException("Minecraft version manifest JSON does not exist: " + config.manifestJson());
            }
            MinecraftVersionManifest manifest = manifestParser.parse(readString(config.manifestJson()), config.manifestJson().toString());
            manifest
                .findVersion(config.requestedVersion())
                .orElseThrow(() -> new LoaderException("Minecraft version " + config.requestedVersion() + " was not found in " + config.manifestJson()));
            if (!config.fetchMetadata() && !config.downloadServer() && !config.cacheRepair()) {
                throw missingMetadataException(workingDirectory, config.requestedVersion(), config.minecraftDirectory());
            }
            return;
        }

        if (config.offline()) {
            throw new LoaderException(
                "Minecraft metadata for version " +
                config.requestedVersion() +
                " is unavailable in offline mode. Provide --minecraft-version-json, a local minecraftDir version JSON, or populate the cache first."
            );
        }
        if (!config.fetchMetadata() && !config.downloadServer() && !config.cacheRepair()) {
            throw missingMetadataException(workingDirectory, config.requestedVersion(), config.minecraftDirectory());
        }
    }

    public ResolvedVersionJson resolve(Path workingDirectory, MinecraftProviderConfig config) throws LoaderException {
        if (config.explicitVersionJson() != null) {
            return new ResolvedVersionJson(
                config.requestedVersion(),
                config.explicitVersionJson(),
                readString(config.explicitVersionJson()),
                "explicit"
            );
        }

        MinecraftInstallLocator locator = new MinecraftInstallLocator();
        Path localVersionJson =
            config.minecraftDirectory() == null ? null : locator.versionJsonPath(config.minecraftDirectory(), config.requestedVersion());
        if (localVersionJson != null && Files.isRegularFile(localVersionJson)) {
            return new ResolvedVersionJson(config.requestedVersion(), localVersionJson, readString(localVersionJson), "local");
        }

        Path cachedVersionJsonPath =
            config.cacheDirectory().resolve("metadata/versions").resolve(config.requestedVersion() + ".json").toAbsolutePath().normalize();
        if (Files.isRegularFile(cachedVersionJsonPath)) {
            return new ResolvedVersionJson(config.requestedVersion(), cachedVersionJsonPath, readString(cachedVersionJsonPath), "cache");
        }

        if (config.manifestJson() != null) {
            MinecraftVersionManifest manifest = manifestParser.parse(readString(config.manifestJson()), config.manifestJson().toString());
            MinecraftVersionManifest.VersionEntry version = manifest
                .findVersion(config.requestedVersion())
                .orElseThrow(() -> new LoaderException("Minecraft version " + config.requestedVersion() + " was not found in " + config.manifestJson()));
            if (!config.fetchMetadata() && !config.downloadServer() && !config.cacheRepair()) {
                throw missingMetadataException(workingDirectory, config.requestedVersion(), config.minecraftDirectory());
            }
            Path fetchedVersionJson = cachePath(config, config.requestedVersion());
            String json = fetch(version.url(), "Minecraft version JSON");
            writeString(fetchedVersionJson, json);
            return new ResolvedVersionJson(config.requestedVersion(), fetchedVersionJson, json, "manifest");
        }

        if (config.offline()) {
            throw new LoaderException(
                "Minecraft metadata for version " + config.requestedVersion() + " is unavailable in offline mode."
            );
        }
        if (!config.fetchMetadata() && !config.downloadServer() && !config.cacheRepair()) {
            throw missingMetadataException(workingDirectory, config.requestedVersion(), config.minecraftDirectory());
        }

        String manifestJson = fetch(DEFAULT_MANIFEST_URL, "Minecraft version manifest");
        Path cachedManifest = manifestPath(config, workingDirectory);
        writeString(cachedManifest, manifestJson);
        MinecraftVersionManifest manifest = manifestParser.parse(manifestJson, DEFAULT_MANIFEST_URL);
        MinecraftVersionManifest.VersionEntry version = manifest
            .findVersion(config.requestedVersion())
            .orElseThrow(() -> new LoaderException("Minecraft version " + config.requestedVersion() + " was not found in fetched manifest"));
        Path cachedVersionJson = cachePath(config, config.requestedVersion());
        String json = fetch(version.url(), "Minecraft version JSON");
        writeString(cachedVersionJson, json);
        return new ResolvedVersionJson(config.requestedVersion(), cachedVersionJson, json, "fetched");
    }

    private Path cachePath(MinecraftProviderConfig config, String version) {
        return config.cacheDirectory().resolve("metadata/versions").resolve(version + ".json").toAbsolutePath().normalize();
    }

    private Path manifestPath(MinecraftProviderConfig config, Path workingDirectory) {
        if (config.cacheDirectory() != null) {
            return config.cacheDirectory().resolve("metadata/version-manifest.json").toAbsolutePath().normalize();
        }
        return workingDirectory.resolve("minecraft-cache/metadata/version-manifest.json").toAbsolutePath().normalize();
    }

    private String readString(Path path) throws LoaderException {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new LoaderException("Failed to read Minecraft metadata file: " + path, exception);
        }
    }

    private void writeString(Path path, String contents) throws LoaderException {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, contents, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new LoaderException("Failed to write Minecraft metadata cache: " + path, exception);
        }
    }

    private String fetch(String url, String label) throws LoaderException {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new LoaderException("Failed to fetch " + label + " from " + url + ": HTTP " + response.statusCode());
            }
            return response.body();
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new LoaderException("Failed to fetch " + label + " from " + url, exception);
        }
    }

    private LoaderException missingMetadataException(Path workingDirectory, String version, Path minecraftDirectory) {
        Path expectedLocalPath =
            minecraftDirectory == null
                ? workingDirectory.resolve("minecraft-cache/metadata/versions").resolve(version + ".json").toAbsolutePath().normalize()
                : new MinecraftInstallLocator().versionJsonPath(minecraftDirectory, version);
        return new LoaderException(
            "Minecraft metadata for version " +
            version +
            " is unavailable. Provide --minecraft-version-json, place version metadata at " +
            expectedLocalPath +
            ", provide --minecraft-manifest-json with --minecraft-fetch-metadata, or pass --minecraft-fetch-metadata."
        );
    }

    public record ResolvedVersionJson(String requestedVersion, Path versionJsonPath, String json, String metadataSource) {
        public ResolvedVersionJson {
            versionJsonPath = versionJsonPath.toAbsolutePath().normalize();
        }
    }
}
