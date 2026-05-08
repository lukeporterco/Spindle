package com.mcmodloader.core.artifact;

import com.mcmodloader.core.diagnostics.DiagnosticEvent;
import com.mcmodloader.core.diagnostics.DiagnosticSink;
import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.launch.LaunchPhase;
import com.mcmodloader.core.minecraft.MinecraftInstallLocator;
import com.mcmodloader.core.minecraft.MinecraftMetadataResolver;
import com.mcmodloader.core.minecraft.MinecraftProviderConfig;
import com.mcmodloader.core.minecraft.MinecraftSide;
import com.mcmodloader.core.minecraft.MinecraftVersionManifest;
import com.mcmodloader.core.minecraft.MinecraftVersionManifestParser;
import com.mcmodloader.core.minecraft.MinecraftVersionMetadata;
import com.mcmodloader.core.minecraft.MinecraftVersionMetadataParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MinecraftArtifactResolver {
    private static final String DEFAULT_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json";

    private final MinecraftArtifactCache cache;
    private final MinecraftArtifactDownloader downloader;
    private final MinecraftArtifactVerifier verifier;
    private final MinecraftVersionManifestParser manifestParser;
    private final MinecraftVersionMetadataParser metadataParser;
    private final MinecraftArtifactCacheWriter cacheWriter;

    public MinecraftArtifactResolver(MinecraftArtifactCache cache) {
        this(
            cache,
            new MinecraftArtifactDownloader(),
            new MinecraftArtifactVerifier(),
            new MinecraftVersionManifestParser(),
            new MinecraftVersionMetadataParser(),
            new MinecraftArtifactCacheWriter()
        );
    }

    public MinecraftArtifactResolver(
        MinecraftArtifactCache cache,
        MinecraftArtifactDownloader downloader,
        MinecraftArtifactVerifier verifier,
        MinecraftVersionManifestParser manifestParser,
        MinecraftVersionMetadataParser metadataParser,
        MinecraftArtifactCacheWriter cacheWriter
    ) {
        this.cache = cache;
        this.downloader = downloader;
        this.verifier = verifier;
        this.manifestParser = manifestParser;
        this.metadataParser = metadataParser;
        this.cacheWriter = cacheWriter;
    }

    public Resolution resolve(Path workingDirectory, MinecraftProviderConfig config, DiagnosticSink diagnosticSink) throws LoaderException {
        recordResolveEvent(config, diagnosticSink, "minecraft.artifact_cache.resolve", "Minecraft artifact cache resolution started");
        if (config.offline()) {
            diagnosticSink.record(
                new DiagnosticEvent(
                    "minecraft.offline_check",
                    LaunchPhase.COMPLETE.name(),
                    0L,
                    "ok",
                    "Minecraft offline mode enabled",
                    details(config, null, null, null)
                )
            );
        }
        if (config.cacheRepair()) {
            diagnosticSink.record(
                new DiagnosticEvent(
                    "minecraft.cache_repair",
                    LaunchPhase.COMPLETE.name(),
                    0L,
                    "ok",
                    "Minecraft cache repair enabled",
                    details(config, null, null, null)
                )
            );
        }

        List<String> warnings = new ArrayList<>();
        List<MinecraftArtifactRecord> artifacts = new ArrayList<>();
        MinecraftVersionManifest manifest = null;
        MinecraftArtifactRecord manifestRecord = new MinecraftArtifactRecord(
            "version-manifest",
            ArtifactKind.METADATA,
            cache.manifestPath(),
            DEFAULT_MANIFEST_URL,
            null,
            null,
            null,
            false,
            false,
            false,
            ArtifactStatus.MISSING
        );

        ResolvedVersion resolvedVersion = resolveVersionJson(workingDirectory, config, diagnosticSink, warnings, artifacts, manifestRecord);
        artifacts = new ArrayList<>(resolvedVersion.artifacts());
        manifest = resolvedVersion.manifest();

        MinecraftVersionMetadata metadata = metadataParser.parse(
            resolvedVersion.resolvedVersionJson().json(),
            resolvedVersion.resolvedVersionJson().versionJsonPath().toString(),
            config.side()
        );

        ServerJarResolution serverJarResolution = resolveServerJar(config, diagnosticSink, metadata, warnings);
        artifacts.add(serverJarResolution.serverRecord());

        Path artifactLockPath = cache.artifactLockPath();
        verifyLockIfPresent(config, serverJarResolution.serverRecord(), artifactLockPath, warnings, diagnosticSink);
        if (serverJarResolution.serverJarPath() != null && serverJarResolution.serverRecord().verified()) {
            cacheWriter.writeServerLock(artifactLockPath, cache, metadata.id(), serverJarResolution.serverRecord());
            diagnosticSink.record(
                new DiagnosticEvent(
                    "minecraft.artifact_lock.write",
                    LaunchPhase.COMPLETE.name(),
                    0L,
                    "ok",
                    "Minecraft server artifact lock written",
                    details(config, serverJarResolution.serverRecord(), cache.artifactReportPath(), artifactLockPath)
                )
            );
        }

        MinecraftArtifactCacheReport report =
            new MinecraftArtifactCacheReport(
                1,
                metadata.id(),
                cache.displayPath(cache.cacheDirectory()),
                config.offline(),
                config.cacheInspect(),
                config.cacheRepair(),
                config.forceRedownload(),
                artifacts,
                warnings
            );
        cacheWriter.writeReport(cache.artifactReportPath(), cache, report);
        diagnosticSink.record(
            new DiagnosticEvent(
                "minecraft.artifact_cache.write",
                LaunchPhase.COMPLETE.name(),
                0L,
                "ok",
                "Minecraft artifact cache report written",
                details(config, null, cache.artifactReportPath(), artifactLockPath)
            )
        );

        return new Resolution(resolvedVersion.resolvedVersionJson(), metadata, serverJarResolution.serverJarPath(), serverJarResolution.serverJarSource(), report);
    }

    private ResolvedVersion resolveVersionJson(
        Path workingDirectory,
        MinecraftProviderConfig config,
        DiagnosticSink diagnosticSink,
        List<String> warnings,
        List<MinecraftArtifactRecord> artifacts,
        MinecraftArtifactRecord manifestRecord
    ) throws LoaderException {
        if (config.explicitVersionJson() != null) {
            Path explicitPath = config.explicitVersionJson();
            if (!Files.isRegularFile(explicitPath)) {
                throw new LoaderException("Minecraft version JSON does not exist: " + explicitPath);
            }
            artifacts.add(manifestRecord);
            MinecraftArtifactRecord versionRecord =
                buildPresentRecord("version-json", ArtifactKind.METADATA, explicitPath, null, null, false, false);
            return new ResolvedVersion(
                new MinecraftMetadataResolver.ResolvedVersionJson(config.requestedVersion(), explicitPath, readString(explicitPath), "explicit"),
                null,
                List.of(manifestRecord, versionRecord)
            );
        }

        MinecraftInstallLocator installLocator = new MinecraftInstallLocator();
        Path localVersionJson =
            config.minecraftDirectory() == null ? null : installLocator.versionJsonPath(config.minecraftDirectory(), config.requestedVersion());
        if (localVersionJson != null && Files.isRegularFile(localVersionJson)) {
            artifacts.add(manifestRecord);
            MinecraftArtifactRecord versionRecord =
                buildPresentRecord("version-json", ArtifactKind.METADATA, localVersionJson, null, null, false, false);
            return new ResolvedVersion(
                new MinecraftMetadataResolver.ResolvedVersionJson(config.requestedVersion(), localVersionJson, readString(localVersionJson), "local"),
                null,
                List.of(manifestRecord, versionRecord)
            );
        }

        Path cachedVersionJson = cache.versionJsonPath(config.requestedVersion());
        if (!config.forceRedownload() && Files.isRegularFile(cachedVersionJson)) {
            artifacts.add(manifestRecord.withPresence(Files.isRegularFile(manifestRecord.path()), Files.isRegularFile(manifestRecord.path()) ? ArtifactStatus.PRESENT : ArtifactStatus.MISSING));
            MinecraftArtifactRecord versionRecord =
                buildPresentRecord("version-json", ArtifactKind.METADATA, cachedVersionJson, null, null, false, false);
            return new ResolvedVersion(
                new MinecraftMetadataResolver.ResolvedVersionJson(config.requestedVersion(), cachedVersionJson, readString(cachedVersionJson), "cache"),
                Files.isRegularFile(manifestRecord.path()) ? manifestParser.parse(readString(manifestRecord.path()), manifestRecord.path().toString()) : null,
                List.of(
                    manifestRecord.withPresence(
                        Files.isRegularFile(manifestRecord.path()),
                        Files.isRegularFile(manifestRecord.path()) ? ArtifactStatus.PRESENT : ArtifactStatus.MISSING
                    ),
                    versionRecord
                )
            );
        }

        if (config.offline()) {
            throw new LoaderException(
                "Minecraft metadata for version " +
                config.requestedVersion() +
                " is unavailable in offline mode. Provide --minecraft-version-json, a local minecraftDir version JSON, or populate the cache before using --minecraft-offline."
            );
        }
        if (!config.fetchMetadata() && !config.downloadServer() && !config.cacheRepair()) {
            throw missingMetadataException(config.requestedVersion(), localVersionJson);
        }

        ManifestResolution manifestResolution = resolveManifest(config);
        MinecraftVersionManifest manifest = manifestResolution.manifest();
        MinecraftVersionManifest.VersionEntry versionEntry = manifest.findVersion(config.requestedVersion()).orElseThrow(() -> missingVersionInManifest(config.requestedVersion(), manifest));
        MinecraftArtifactRecord downloadedManifestRecord =
            manifestResolution.record().withPresence(true, manifestResolution.record().verified() ? ArtifactStatus.VERIFIED : ArtifactStatus.PRESENT);

        if (config.forceRedownload() || !Files.isRegularFile(cachedVersionJson)) {
            DownloadResult downloadResult =
                downloader.download(
                    java.net.URI.create(versionEntry.url()),
                    cachedVersionJson,
                    cache.tmpDirectory(),
                    versionEntry.sha1(),
                    null
                );
            diagnosticSink.record(
                new DiagnosticEvent(
                    "minecraft.artifact.download",
                    LaunchPhase.COMPLETE.name(),
                    downloadResult.durationMs(),
                    "ok",
                    "Minecraft artifact downloaded",
                    details(
                        config,
                        new MinecraftArtifactRecord(
                            "version-json",
                            ArtifactKind.METADATA,
                            cachedVersionJson,
                            versionEntry.url(),
                            versionEntry.sha1(),
                            downloadResult.sha256(),
                            downloadResult.size(),
                            true,
                            true,
                            downloadResult.verified(),
                            downloadResult.verified() ? ArtifactStatus.VERIFIED : ArtifactStatus.PRESENT
                        ),
                        cache.artifactReportPath(),
                        cache.artifactLockPath()
                    )
                )
            );
        }

        MinecraftArtifactRecord versionRecord = buildPresentRecord(
            "version-json",
            ArtifactKind.METADATA,
            cachedVersionJson,
            versionEntry.url(),
            versionEntry.sha1(),
            true,
            true
        );
        return new ResolvedVersion(
            new MinecraftMetadataResolver.ResolvedVersionJson(config.requestedVersion(), cachedVersionJson, readString(cachedVersionJson), "cache"),
            manifest,
            List.of(downloadedManifestRecord, versionRecord)
        );
    }

    private ManifestResolution resolveManifest(MinecraftProviderConfig config) throws LoaderException {
        if (config.manifestJson() != null) {
            if (!Files.isRegularFile(config.manifestJson())) {
                throw new LoaderException("Minecraft version manifest JSON does not exist: " + config.manifestJson());
            }
            MinecraftVersionManifest manifest = manifestParser.parse(readString(config.manifestJson()), config.manifestJson().toString());
            MinecraftArtifactRecord record = buildPresentRecord("version-manifest", ArtifactKind.METADATA, config.manifestJson(), null, null, false, false);
            return new ManifestResolution(manifest, record);
        }

        Path manifestPath = cache.manifestPath();
        if (!config.forceRedownload() && Files.isRegularFile(manifestPath)) {
            MinecraftVersionManifest manifest = manifestParser.parse(readString(manifestPath), manifestPath.toString());
            MinecraftArtifactRecord record = buildPresentRecord("version-manifest", ArtifactKind.METADATA, manifestPath, DEFAULT_MANIFEST_URL, null, false, false);
            return new ManifestResolution(manifest, record);
        }

        DownloadResult downloadResult = downloader.download(java.net.URI.create(DEFAULT_MANIFEST_URL), manifestPath, cache.tmpDirectory(), null, null);
        MinecraftVersionManifest manifest = manifestParser.parse(readString(manifestPath), manifestPath.toString());
        MinecraftArtifactRecord record =
            new MinecraftArtifactRecord(
                "version-manifest",
                ArtifactKind.METADATA,
                manifestPath,
                DEFAULT_MANIFEST_URL,
                null,
                downloadResult.sha256(),
                downloadResult.size(),
                true,
                true,
                downloadResult.verified(),
                downloadResult.verified() ? ArtifactStatus.VERIFIED : ArtifactStatus.PRESENT
            );
        return new ManifestResolution(manifest, record);
    }

    private ServerJarResolution resolveServerJar(
        MinecraftProviderConfig config,
        DiagnosticSink diagnosticSink,
        MinecraftVersionMetadata metadata,
        List<String> warnings
    ) throws LoaderException {
        MinecraftVersionMetadata.Download serverDownload = metadata.serverDownload();
        String expectedSha1 = serverDownload == null ? null : serverDownload.sha1();
        Long expectedSize = serverDownload == null || serverDownload.size() <= 0L ? null : serverDownload.size();

        if (!config.prefersCacheOrDownload()) {
            Path localServerJar = localServerJar(config, metadata.id());
            if (localServerJar != null) {
                MinecraftArtifactRecord localRecord =
                    buildPresentRecord("server-jar", ArtifactKind.SERVER, localServerJar, null, expectedSha1, false, false);
                return new ServerJarResolution(localServerJar, "local", localRecord);
            }
        }

        Path cachedServerJar = cache.serverJarPath(metadata.id());
        if (!config.forceRedownload() && Files.isRegularFile(cachedServerJar)) {
            MinecraftArtifactRecord cacheRecord =
                verifyExisting(
                    new MinecraftArtifactRecord(
                        "server-jar",
                        ArtifactKind.SERVER,
                        cachedServerJar,
                        serverDownload == null ? null : serverDownload.url(),
                        expectedSha1,
                        null,
                        null,
                        true,
                        false,
                        false,
                        ArtifactStatus.PRESENT
                    ),
                    expectedSize
                );
            return new ServerJarResolution(cachedServerJar, "cache", cacheRecord);
        }

        if (serverDownload == null || serverDownload.url() == null || serverDownload.url().isBlank()) {
            MinecraftArtifactRecord missingRecord =
                new MinecraftArtifactRecord(
                    "server-jar",
                    ArtifactKind.SERVER,
                    cachedServerJar,
                    null,
                    expectedSha1,
                    null,
                    null,
                    false,
                    false,
                    false,
                    ArtifactStatus.MISSING
                );
            warnings.add("Minecraft server jar metadata did not contain downloads.server.");
            return new ServerJarResolution(null, "missing", missingRecord);
        }
        if (config.offline()) {
            MinecraftArtifactRecord missingRecord =
                new MinecraftArtifactRecord(
                    "server-jar",
                    ArtifactKind.SERVER,
                    cachedServerJar,
                    serverDownload.url(),
                    expectedSha1,
                    null,
                    null,
                    false,
                    false,
                    false,
                    ArtifactStatus.MISSING
                );
            if (config.launch()) {
                throw new LoaderException(
                    "Minecraft server launch requires a cached or local server jar in offline mode. Populate " +
                    cachedServerJar.toString().replace('\\', '/') +
                    " before using --minecraft-offline."
                );
            }
            warnings.add("Minecraft server jar is missing in offline mode.");
            return new ServerJarResolution(null, "missing", missingRecord);
        }
        if (!config.downloadServer() && !config.cacheRepair()) {
            MinecraftArtifactRecord missingRecord =
                new MinecraftArtifactRecord(
                    "server-jar",
                    ArtifactKind.SERVER,
                    cachedServerJar,
                    serverDownload.url(),
                    expectedSha1,
                    null,
                    null,
                    false,
                    false,
                    false,
                    ArtifactStatus.MISSING
                );
            if (config.launch()) {
                Path expectedPath =
                    config.minecraftDirectory() == null
                        ? cachedServerJar
                        : new MinecraftInstallLocator().primaryServerJarPath(config.minecraftDirectory(), metadata.id());
                throw new LoaderException(
                    "Minecraft server launch requires a resolved server jar. Missing " +
                    expectedPath.toString().replace('\\', '/') +
                    ". Pass --minecraft-download-server to cache the vanilla server jar first."
                );
            }
            return new ServerJarResolution(null, "missing", missingRecord);
        }

        DownloadResult downloadResult =
            downloader.download(
                java.net.URI.create(serverDownload.url()),
                cachedServerJar,
                cache.tmpDirectory(),
                expectedSha1,
                expectedSize
            );
        MinecraftArtifactRecord downloadedRecord =
            new MinecraftArtifactRecord(
                "server-jar",
                ArtifactKind.SERVER,
                cachedServerJar,
                serverDownload.url(),
                expectedSha1,
                downloadResult.sha256(),
                downloadResult.size(),
                true,
                true,
                downloadResult.verified(),
                downloadResult.verified() ? ArtifactStatus.VERIFIED : ArtifactStatus.PRESENT
            );
        diagnosticSink.record(
            new DiagnosticEvent(
                "minecraft.artifact.download",
                LaunchPhase.COMPLETE.name(),
                downloadResult.durationMs(),
                "ok",
                "Minecraft artifact downloaded",
                details(config, downloadedRecord, cache.artifactReportPath(), cache.artifactLockPath())
            )
        );
        return new ServerJarResolution(cachedServerJar, "downloaded", downloadedRecord);
    }

    private MinecraftArtifactRecord verifyExisting(MinecraftArtifactRecord record, Long expectedSize) throws LoaderException {
        if (!Files.isRegularFile(record.path())) {
            return record.withPresence(false, ArtifactStatus.MISSING);
        }
        MinecraftArtifactVerifier.VerificationResult verification = verifier.verify(record.path(), record.sha1(), expectedSize);
        ArtifactStatus status = verification.verified() ? ArtifactStatus.VERIFIED : ArtifactStatus.PRESENT;
        return record.withVerification(verification.sha256(), verification.size(), verification.verified(), status).withPresence(true, status);
    }

    private void verifyLockIfPresent(
        MinecraftProviderConfig config,
        MinecraftArtifactRecord serverRecord,
        Path artifactLockPath,
        List<String> warnings,
        DiagnosticSink diagnosticSink
    ) throws LoaderException {
        if (serverRecord == null || !Files.isRegularFile(artifactLockPath)) {
            return;
        }
        JsonObject root = JsonParser.parseString(readString(artifactLockPath)).getAsJsonObject();
        JsonArray artifacts = root.getAsJsonArray("artifacts");
        if (artifacts == null || artifacts.isEmpty()) {
            return;
        }
        JsonObject artifact = artifacts.get(0).getAsJsonObject();
        String lockedSha1 = artifact.has("sha1") && !artifact.get("sha1").isJsonNull() ? artifact.get("sha1").getAsString() : null;
        String lockedSha256 = artifact.has("sha256") && !artifact.get("sha256").isJsonNull() ? artifact.get("sha256").getAsString() : null;
        Long lockedSize = artifact.has("size") && !artifact.get("size").isJsonNull() ? artifact.get("size").getAsLong() : null;

        boolean matches =
            Objects.equals(lockedSha1, serverRecord.sha1()) &&
            Objects.equals(lockedSha256, serverRecord.sha256()) &&
            Objects.equals(lockedSize, serverRecord.size());
        diagnosticSink.record(
            new DiagnosticEvent(
                "minecraft.artifact_lock.verify",
                LaunchPhase.COMPLETE.name(),
                0L,
                matches ? "ok" : "warning",
                matches ? "Minecraft server artifact lock verified" : "Minecraft server artifact lock mismatch",
                details(config, serverRecord, cache.artifactReportPath(), artifactLockPath)
            )
        );
        if (matches) {
            return;
        }

        String warning = "Minecraft server artifact lock does not match cached server jar.";
        if (config.cacheStrict()) {
            throw new LoaderException(warning);
        }
        warnings.add(warning);
    }

    private MinecraftArtifactRecord buildPresentRecord(
        String id,
        ArtifactKind kind,
        Path path,
        String sourceUrl,
        String expectedSha1,
        boolean downloaded,
        boolean tryVerify
    ) throws LoaderException {
        MinecraftArtifactRecord record =
            new MinecraftArtifactRecord(id, kind, path, sourceUrl, expectedSha1, null, null, Files.isRegularFile(path), downloaded, false, ArtifactStatus.MISSING);
        if (!Files.isRegularFile(path)) {
            return record.withPresence(false, ArtifactStatus.MISSING);
        }
        if (!tryVerify) {
            MinecraftArtifactVerifier.VerificationResult verification = verifier.verify(path, null, null);
            return record.withVerification(verification.sha256(), verification.size(), false, ArtifactStatus.PRESENT).withPresence(true, ArtifactStatus.PRESENT);
        }
        return verifyExisting(record, null);
    }

    private LoaderException missingMetadataException(String version, Path localVersionJson) {
        String localPath =
            localVersionJson == null ? "<minecraftDir>/versions/" + version + "/" + version + ".json" : localVersionJson.toString().replace('\\', '/');
        return new LoaderException(
            "Minecraft metadata for version " +
            version +
            " is unavailable. Provide --minecraft-version-json, place version metadata at " +
            localPath +
            ", or pass --minecraft-fetch-metadata."
        );
    }

    private LoaderException missingVersionInManifest(String requestedVersion, MinecraftVersionManifest manifest) {
        String latestRelease = manifest.latestRelease() == null ? "unknown" : manifest.latestRelease();
        String latestSnapshot = manifest.latestSnapshot() == null ? "unknown" : manifest.latestSnapshot();
        return new LoaderException(
            "Minecraft version " +
            requestedVersion +
            " was not found in the version manifest. Latest release: " +
            latestRelease +
            ". Latest snapshot: " +
            latestSnapshot +
            "."
        );
    }

    private Path localServerJar(MinecraftProviderConfig config, String version) {
        if (config.minecraftDirectory() == null) {
            return null;
        }
        MinecraftInstallLocator locator = new MinecraftInstallLocator();
        Path primary = locator.primaryServerJarPath(config.minecraftDirectory(), version);
        if (Files.isRegularFile(primary)) {
            return primary;
        }
        Path alternate = locator.alternateServerJarPath(config.minecraftDirectory(), version);
        return Files.isRegularFile(alternate) ? alternate : null;
    }

    private String readString(Path path) throws LoaderException {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new LoaderException("Failed to read artifact file " + path.toString().replace('\\', '/'), exception);
        }
    }

    private void recordResolveEvent(MinecraftProviderConfig config, DiagnosticSink diagnosticSink, String name, String message) {
        diagnosticSink.record(new DiagnosticEvent(name, LaunchPhase.COMPLETE.name(), 0L, "ok", message, details(config, null, null, null)));
    }

    private Map<String, String> details(
        MinecraftProviderConfig config,
        MinecraftArtifactRecord artifact,
        Path artifactReportOutputPath,
        Path artifactLockOutputPath
    ) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("minecraftVersion", config.requestedVersion());
        details.put("cacheDirectory", cache.displayPath(cache.cacheDirectory()));
        details.put("offline", Boolean.toString(config.offline()));
        details.put("strict", Boolean.toString(config.cacheStrict()));
        details.put("repair", Boolean.toString(config.cacheRepair()));
        details.put("forceRedownload", Boolean.toString(config.forceRedownload()));
        if (artifact != null) {
            details.put("artifactId", artifact.id());
            details.put("artifactKind", artifact.kind().id());
            details.put("downloaded", Boolean.toString(artifact.downloaded()));
            details.put("verified", Boolean.toString(artifact.verified()));
        }
        if (artifactReportOutputPath != null) {
            details.put("artifactReportOutputPath", cache.displayPath(artifactReportOutputPath));
        }
        if (artifactLockOutputPath != null) {
            details.put("artifactLockOutputPath", cache.displayPath(artifactLockOutputPath));
        }
        return details;
    }

    public record Resolution(
        MinecraftMetadataResolver.ResolvedVersionJson resolvedVersionJson,
        MinecraftVersionMetadata metadata,
        Path serverJarPath,
        String serverJarSource,
        MinecraftArtifactCacheReport report
    ) {
    }

    private record ResolvedVersion(
        MinecraftMetadataResolver.ResolvedVersionJson resolvedVersionJson,
        MinecraftVersionManifest manifest,
        List<MinecraftArtifactRecord> artifacts
    ) {
    }

    private record ManifestResolution(MinecraftVersionManifest manifest, MinecraftArtifactRecord record) {
    }

    private record ServerJarResolution(Path serverJarPath, String serverJarSource, MinecraftArtifactRecord serverRecord) {
    }
}
