package com.mcmodloader.core.artifact;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mcmodloader.core.LoaderMain;
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
import com.mcmodloader.core.minecraft.MinecraftVersionSelection;
import com.mcmodloader.core.minecraft.MinecraftVersionSelector;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MinecraftArtifactResolver {
    private final MinecraftArtifactCache cache;
    private final MinecraftArtifactDownloader downloader;
    private final MinecraftArtifactVerifier verifier;
    private final MinecraftVersionManifestParser manifestParser;
    private final MinecraftVersionMetadataParser metadataParser;
    private final MinecraftArtifactCacheWriter cacheWriter;
    private final MinecraftVersionSelector versionSelector;

    public MinecraftArtifactResolver(MinecraftArtifactCache cache) {
        this(
            cache,
            new MinecraftArtifactDownloader(),
            new MinecraftArtifactVerifier(),
            new MinecraftVersionManifestParser(),
            new MinecraftVersionMetadataParser(),
            new MinecraftArtifactCacheWriter(),
            new MinecraftVersionSelector()
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
        this(cache, downloader, verifier, manifestParser, metadataParser, cacheWriter, new MinecraftVersionSelector());
    }

    public MinecraftArtifactResolver(
        MinecraftArtifactCache cache,
        MinecraftArtifactDownloader downloader,
        MinecraftArtifactVerifier verifier,
        MinecraftVersionManifestParser manifestParser,
        MinecraftVersionMetadataParser metadataParser,
        MinecraftArtifactCacheWriter cacheWriter,
        MinecraftVersionSelector versionSelector
    ) {
        this.cache = cache;
        this.downloader = downloader;
        this.verifier = verifier;
        this.manifestParser = manifestParser;
        this.metadataParser = metadataParser;
        this.cacheWriter = cacheWriter;
        this.versionSelector = versionSelector;
    }

    public Resolution resolve(Path workingDirectory, MinecraftProviderConfig config, DiagnosticSink diagnosticSink) throws LoaderException {
        recordResolveEvent(config, diagnosticSink, "minecraft.artifact_cache.resolve", "Minecraft artifact cache resolution started");

        List<String> warnings = new ArrayList<>();
        VersionContext versionContext = resolveVersionContext(config, diagnosticSink, warnings);
        List<MinecraftArtifactRecord> artifacts = new ArrayList<>();
        artifacts.add(versionContext.manifestRecord());
        artifacts.add(versionContext.versionRecord());

        MinecraftVersionMetadata metadata = metadataParser.parse(
            versionContext.resolvedVersionJson().json(),
            versionContext.resolvedVersionJson().versionJsonPath().toString(),
            config.side()
        );

        ServerJarResolution serverJarResolution = resolveServerJar(config, diagnosticSink, metadata, warnings);
        artifacts.add(serverJarResolution.serverRecord());

        Path artifactLockPath = cache.artifactLockPath();
        verifyLockIfPresent(config, serverJarResolution.serverRecord(), artifactLockPath, warnings, diagnosticSink);
        if (serverJarResolution.serverJarPath() != null && serverJarResolution.serverRecord().verified()) {
            cacheWriter.writeServerLock(
                artifactLockPath,
                cache,
                LoaderMain.TARGET_MINECRAFT_VERSION,
                config.baselineServerEnabled() ? metadata.id() : metadata.id(),
                serverJarResolution.serverRecord(),
                Instant.now().toString()
            );
            diagnosticSink.record(
                new DiagnosticEvent(
                    "minecraft.artifact_lock.write",
                    LaunchPhase.COMPLETE.name(),
                    0L,
                    "ok",
                    "Minecraft server artifact lock written",
                    details(config, versionContext.versionSelection(), serverJarResolution.serverRecord(), cache.artifactReportPath(), artifactLockPath)
                )
            );
        }

        MinecraftArtifactCacheReport report =
            new MinecraftArtifactCacheReport(
                1,
                LoaderMain.TARGET_MINECRAFT_VERSION,
                metadata.id(),
                config.baselineServerEnabled() ? metadata.id() : null,
                cache.displayPath(cache.cacheDirectory()),
                config.offline(),
                config.cacheInspect(),
                config.cacheRepair(),
                config.forceRedownload(),
                downloader.networkRequestCount(),
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
                details(config, versionContext.versionSelection(), null, cache.artifactReportPath(), artifactLockPath)
            )
        );

        return new Resolution(
            versionContext.resolvedVersionJson(),
            metadata,
            serverJarResolution.serverJarPath(),
            serverJarResolution.serverJarSource(),
            report,
            versionContext.manifest(),
            versionContext.versionSelection(),
            versionContext.manifestRecord(),
            versionContext.versionRecord(),
            serverJarResolution.serverRecord(),
            downloader.networkRequestCount()
        );
    }

    private VersionContext resolveVersionContext(MinecraftProviderConfig config, DiagnosticSink diagnosticSink, List<String> warnings)
        throws LoaderException {
        String requestedVersion = selectedRequest(config);
        MinecraftArtifactRecord missingManifestRecord =
            new MinecraftArtifactRecord(
                "version-manifest",
                ArtifactKind.METADATA,
                config.manifestJson() == null ? cache.manifestPath() : config.manifestJson(),
                manifestUrl(config),
                null,
                null,
                null,
                false,
                false,
                false,
                ArtifactStatus.MISSING
            );

        if (config.explicitVersionJson() != null) {
            if (!Files.isRegularFile(config.explicitVersionJson())) {
                throw new LoaderException("Minecraft version JSON does not exist: " + config.explicitVersionJson());
            }
            MinecraftVersionMetadata metadata = metadataParser.parse(
                readString(config.explicitVersionJson()),
                config.explicitVersionJson().toString(),
                config.side()
            );
            MinecraftVersionSelection selection = versionSelector.select(requestedVersion, null, true);
            selection = new MinecraftVersionSelection(selection.requested(), metadata.id(), selection.source());
            MinecraftArtifactRecord versionRecord =
                buildMetadataRecord("version-json", config.explicitVersionJson(), null, null, false, null);
            return new VersionContext(
                new MinecraftMetadataResolver.ResolvedVersionJson(metadata.id(), config.explicitVersionJson(), readString(config.explicitVersionJson()), "explicit"),
                null,
                selection,
                missingManifestRecord,
                versionRecord
            );
        }

        ManifestContext manifestContext = resolveManifestIfNeeded(config, requestedVersion, warnings, diagnosticSink);
        MinecraftVersionSelection selection =
            manifestContext.manifest() == null
                ? new MinecraftVersionSelection(requestedVersion, requestedVersion, "cache")
                : versionSelector.select(requestedVersion, manifestContext.manifest(), config.explicitVersionJson() != null);
        String resolvedVersion = selection.resolved();

        Path localVersionJson = localVersionJson(config, resolvedVersion);
        if (!config.offlineReplay() && localVersionJson != null && Files.isRegularFile(localVersionJson) && !config.forceRedownload()) {
            MinecraftArtifactRecord versionRecord = buildMetadataRecord("version-json", localVersionJson, null, null, false, null);
            return new VersionContext(
                new MinecraftMetadataResolver.ResolvedVersionJson(resolvedVersion, localVersionJson, readString(localVersionJson), "local"),
                manifestContext.manifest(),
                selection,
                manifestContext.record(),
                versionRecord
            );
        }

        Path cachedVersionJson = cache.versionJsonPath(resolvedVersion);
        MinecraftVersionManifest.VersionEntry versionEntry =
            manifestContext.manifest() == null ? null : manifestContext.manifest().findVersion(resolvedVersion).orElse(null);
        if (!config.forceRedownload() && Files.isRegularFile(cachedVersionJson)) {
            try {
                MinecraftArtifactRecord versionRecord =
                    buildMetadataRecord(
                        "version-json",
                        cachedVersionJson,
                        versionEntry == null ? null : versionEntry.url(),
                        versionEntry == null ? null : versionEntry.sha1(),
                        false,
                        null
                    );
                metadataParser.parse(readString(cachedVersionJson), cachedVersionJson.toString(), config.side());
                return new VersionContext(
                    new MinecraftMetadataResolver.ResolvedVersionJson(resolvedVersion, cachedVersionJson, readString(cachedVersionJson), "cache"),
                    manifestContext.manifest(),
                    selection,
                    manifestContext.record(),
                    versionRecord
                );
            } catch (LoaderException exception) {
                if (!config.cacheRepair()) {
                    throw exception;
                }
                warnings.add("Cached Minecraft version metadata was invalid and will be repaired: " + cache.displayPath(cachedVersionJson));
            }
        }

        if (config.offline()) {
            throw missingCachedVersionJson(config, resolvedVersion);
        }
        if (!config.fetchMetadata() && !config.downloadServer() && !config.cacheRepair()) {
            throw missingMetadataException(config, resolvedVersion);
        }
        if (versionEntry == null || versionEntry.url() == null || versionEntry.url().isBlank()) {
            throw new LoaderException("Minecraft version " + resolvedVersion + " does not provide version JSON metadata in the manifest.");
        }

        DownloadResult downloadResult =
            downloadWithDiagnostics(diagnosticSink, config, "version-json", URI.create(versionEntry.url()), cachedVersionJson, versionEntry.sha1(), null);
        MinecraftArtifactRecord versionRecord =
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
            );
        return new VersionContext(
            new MinecraftMetadataResolver.ResolvedVersionJson(resolvedVersion, cachedVersionJson, readString(cachedVersionJson), "cache"),
            manifestContext.manifest(),
            selection,
            manifestContext.record(),
            versionRecord
        );
    }

    private ManifestContext resolveManifestIfNeeded(
        MinecraftProviderConfig config,
        String requestedVersion,
        List<String> warnings,
        DiagnosticSink diagnosticSink
    ) throws LoaderException {
        boolean manifestRequired = needsManifest(requestedVersion) || config.manifestJson() != null || !canResolveWithoutManifest(config, requestedVersion);
        if (!manifestRequired) {
            return new ManifestContext(null, new MinecraftArtifactRecord("version-manifest", ArtifactKind.METADATA, cache.manifestPath(), manifestUrl(config), null, null, null, false, false, false, ArtifactStatus.MISSING));
        }

        if (config.manifestJson() != null) {
            if (!Files.isRegularFile(config.manifestJson())) {
                throw new LoaderException("Minecraft version manifest JSON does not exist: " + config.manifestJson());
            }
            MinecraftArtifactRecord manifestRecord = buildMetadataRecord("version-manifest", config.manifestJson(), manifestUrl(config), null, false, null);
            return new ManifestContext(
                manifestParser.parse(readString(config.manifestJson()), config.manifestJson().toString()),
                manifestRecord
            );
        }

        Path cachedManifest = cache.manifestPath();
        if (!config.forceRedownload() && Files.isRegularFile(cachedManifest)) {
            try {
                MinecraftArtifactRecord manifestRecord = buildMetadataRecord("version-manifest", cachedManifest, manifestUrl(config), null, false, null);
                return new ManifestContext(parseManifest(cachedManifest), manifestRecord);
            } catch (LoaderException exception) {
                if (!config.cacheRepair()) {
                    throw exception;
                }
                warnings.add("Cached Minecraft version manifest was invalid and will be repaired: " + cache.displayPath(cachedManifest));
            }
        }

        if (config.offline()) {
            throw missingCachedManifest(config);
        }
        if (!config.fetchMetadata() && !config.downloadServer() && !config.cacheRepair()) {
            throw new LoaderException(
                "Minecraft version manifest is unavailable. Provide --minecraft-manifest-json or pass --minecraft-fetch-metadata."
            );
        }

        DownloadResult downloadResult =
            downloadWithDiagnostics(diagnosticSink, config, "version-manifest", URI.create(manifestUrl(config)), cachedManifest, null, null);
        MinecraftArtifactRecord manifestRecord =
            new MinecraftArtifactRecord(
                "version-manifest",
                ArtifactKind.METADATA,
                cachedManifest,
                manifestUrl(config),
                null,
                downloadResult.sha256(),
                downloadResult.size(),
                true,
                true,
                false,
                ArtifactStatus.PRESENT
            );
        return new ManifestContext(parseManifest(cachedManifest), manifestRecord);
    }

    private ServerJarResolution resolveServerJar(
        MinecraftProviderConfig config,
        DiagnosticSink diagnosticSink,
        MinecraftVersionMetadata metadata,
        List<String> warnings
    ) throws LoaderException {
        if (config.side() != MinecraftSide.SERVER) {
            MinecraftArtifactRecord missingRecord =
                new MinecraftArtifactRecord(
                    "server-jar",
                    ArtifactKind.SERVER,
                    cache.serverJarPath(metadata.id()),
                    null,
                    null,
                    null,
                    null,
                    false,
                    false,
                    false,
                    ArtifactStatus.MISSING
                );
            return new ServerJarResolution(null, "missing", missingRecord);
        }

        MinecraftVersionMetadata.Download serverDownload = metadata.serverDownload();
        if (serverDownload == null) {
            throw new LoaderException("Version " + metadata.id() + " does not provide a server download in metadata.");
        }
        String expectedSha1 = serverDownload.sha1();
        Long expectedSize = serverDownload.size() > 0L ? serverDownload.size() : null;
        Path cachedServerJar = cache.serverJarPath(metadata.id());

        if (!config.offlineReplay() && !config.baselineServerEnabled() && !config.prefersCacheOrDownload()) {
            Path localServerJar = localServerJar(config, metadata.id());
            if (localServerJar != null) {
                MinecraftArtifactRecord localRecord = buildLocalServerRecord(localServerJar);
                return new ServerJarResolution(localServerJar, "local", localRecord);
            }
        }

        if (!config.forceRedownload() && Files.isRegularFile(cachedServerJar)) {
            try {
                MinecraftArtifactRecord cacheRecord = buildServerRecord(cachedServerJar, serverDownload.url(), expectedSha1, false, expectedSize);
                diagnosticSink.record(
                    new DiagnosticEvent(
                        "minecraft.baseline.server_jar.verify",
                        LaunchPhase.COMPLETE.name(),
                        0L,
                        "ok",
                        "Minecraft server jar verified",
                        details(config, null, cacheRecord, cache.artifactReportPath(), cache.artifactLockPath())
                    )
                );
                return new ServerJarResolution(cachedServerJar, "cache", cacheRecord);
            } catch (LoaderException exception) {
                if (!config.cacheRepair()) {
                    throw exception;
                }
                warnings.add("Cached Minecraft server jar was invalid and will be repaired: " + cache.displayPath(cachedServerJar));
            }
        }

        if (config.offline()) {
            if (!config.baselineServerEnabled() && config.launch()) {
                throw new LoaderException(
                    "Minecraft server launch requires a cached or local server jar in offline mode. Populate " +
                    cachedServerJar.toString().replace('\\', '/') +
                    " before using --minecraft-offline."
                );
            }
            throw missingCachedServerJar(config, metadata.id());
        }
        if (!config.downloadServer() && !config.cacheRepair()) {
            if (config.launch() || config.baselineServerEnabled()) {
                if (!config.baselineServerEnabled()) {
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
                throw new LoaderException(
                    "Minecraft server jar is missing for version " +
                    metadata.id() +
                    ". Pass --minecraft-download-server or run the explicit cache repair/download smoke task first."
                );
            }
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
            return new ServerJarResolution(null, "missing", missingRecord);
        }

        DownloadResult downloadResult =
            downloadWithDiagnostics(diagnosticSink, config, "server-jar", URI.create(serverDownload.url()), cachedServerJar, expectedSha1, expectedSize);
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
                "minecraft.baseline.server_jar.verify",
                LaunchPhase.COMPLETE.name(),
                0L,
                "ok",
                "Minecraft server jar verified",
                details(config, null, downloadedRecord, cache.artifactReportPath(), cache.artifactLockPath())
            )
        );
        return new ServerJarResolution(cachedServerJar, "downloaded", downloadedRecord);
    }

    private DownloadResult downloadWithDiagnostics(
        DiagnosticSink diagnosticSink,
        MinecraftProviderConfig config,
        String artifactId,
        URI uri,
        Path targetPath,
        String expectedSha1,
        Long expectedSize
    ) throws LoaderException {
        if (config.offline()) {
            throw new LoaderException("--minecraft-offline forbids network download attempts for " + artifactId + ".");
        }
        int before = downloader.networkRequestCount();
        DownloadResult result = downloader.download(uri, targetPath, cache.tmpDirectory(), expectedSha1, expectedSize);
        if (downloader.networkRequestCount() > before) {
            diagnosticSink.record(
                new DiagnosticEvent(
                    "minecraft.network.request",
                    LaunchPhase.COMPLETE.name(),
                    result.durationMs(),
                    "ok",
                    "Minecraft network request completed",
                    details(config, null, null, cache.artifactReportPath(), cache.artifactLockPath(), artifactId, uri.toString())
                )
            );
        }
        diagnosticSink.record(
            new DiagnosticEvent(
                "minecraft.artifact.download",
                LaunchPhase.COMPLETE.name(),
                result.durationMs(),
                "ok",
                "Minecraft artifact downloaded",
                details(
                    config,
                    null,
                    new MinecraftArtifactRecord(
                        artifactId,
                        "server-jar".equals(artifactId) ? ArtifactKind.SERVER : ArtifactKind.METADATA,
                        targetPath,
                        uri.toString(),
                        expectedSha1,
                        result.sha256(),
                        result.size(),
                        true,
                        true,
                        result.verified(),
                        result.verified() ? ArtifactStatus.VERIFIED : ArtifactStatus.PRESENT
                    ),
                    cache.artifactReportPath(),
                    cache.artifactLockPath()
                )
            )
        );
        return result;
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
        String lockedSha1 = stringOrNull(artifact, "sha1");
        String lockedSha256 = stringOrNull(artifact, "sha256");
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
                details(config, null, serverRecord, cache.artifactReportPath(), artifactLockPath)
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

    private MinecraftArtifactRecord buildMetadataRecord(
        String id,
        Path path,
        String sourceUrl,
        String expectedSha1,
        boolean downloaded,
        Long expectedSize
    ) throws LoaderException {
        if (!Files.isRegularFile(path)) {
            return new MinecraftArtifactRecord(id, ArtifactKind.METADATA, path, sourceUrl, expectedSha1, null, null, false, downloaded, false, ArtifactStatus.MISSING);
        }
        MinecraftArtifactVerifier.VerificationResult verification = verifier.verify(path, expectedSha1, expectedSize);
        ArtifactStatus status = verification.verified() ? ArtifactStatus.VERIFIED : ArtifactStatus.PRESENT;
        return new MinecraftArtifactRecord(
            id,
            ArtifactKind.METADATA,
            path,
            sourceUrl,
            expectedSha1,
            verification.sha256(),
            verification.size(),
            true,
            downloaded,
            verification.verified(),
            status
        );
    }

    private MinecraftArtifactRecord buildServerRecord(Path path, String sourceUrl, String expectedSha1, boolean downloaded, Long expectedSize)
        throws LoaderException {
        if (!Files.isRegularFile(path)) {
            return new MinecraftArtifactRecord("server-jar", ArtifactKind.SERVER, path, sourceUrl, expectedSha1, null, null, false, downloaded, false, ArtifactStatus.MISSING);
        }
        MinecraftArtifactVerifier.VerificationResult verification = verifier.verify(path, expectedSha1, expectedSize);
        ArtifactStatus status = verification.verified() ? ArtifactStatus.VERIFIED : ArtifactStatus.PRESENT;
        return new MinecraftArtifactRecord(
            "server-jar",
            ArtifactKind.SERVER,
            path,
            sourceUrl,
            verification.sha1(),
            verification.sha256(),
            verification.size(),
            true,
            downloaded,
            verification.verified(),
            status
        );
    }

    private MinecraftArtifactRecord buildLocalServerRecord(Path path) throws LoaderException {
        MinecraftArtifactVerifier.VerificationResult verification = verifier.verify(path, null, null);
        return new MinecraftArtifactRecord(
            "server-jar",
            ArtifactKind.SERVER,
            path,
            null,
            null,
            verification.sha256(),
            verification.size(),
            true,
            false,
            false,
            ArtifactStatus.PRESENT
        );
    }

    private String selectedRequest(MinecraftProviderConfig config) {
        return config.baselineServerEnabled() ? config.requestedVersionOrBaseline() : config.requestedVersion();
    }

    private boolean needsManifest(String requestedVersion) {
        return "latest-release".equals(requestedVersion) || "latest-snapshot".equals(requestedVersion);
    }

    private boolean canResolveWithoutManifest(MinecraftProviderConfig config, String requestedVersion) {
        Path localVersionJson = localVersionJson(config, requestedVersion);
        return config.explicitVersionJson() != null ||
        (localVersionJson != null && Files.isRegularFile(localVersionJson)) ||
        (requestedVersion != null && !requestedVersion.isBlank() && Files.isRegularFile(cache.versionJsonPath(requestedVersion)));
    }

    private Path localVersionJson(MinecraftProviderConfig config, String version) {
        if (config.offlineReplay() || config.minecraftDirectory() == null || version == null || version.isBlank()) {
            return null;
        }
        return new MinecraftInstallLocator().versionJsonPath(config.minecraftDirectory(), version);
    }

    private Path localServerJar(MinecraftProviderConfig config, String version) {
        if (config.minecraftDirectory() == null || version == null || version.isBlank()) {
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

    private String manifestUrl(MinecraftProviderConfig config) {
        return config.manifestUrl() == null || config.manifestUrl().isBlank()
            ? MinecraftMetadataResolver.DEFAULT_MANIFEST_URL
            : config.manifestUrl();
    }

    private MinecraftVersionManifest parseManifest(Path manifestPath) throws LoaderException {
        return manifestParser.parse(readString(manifestPath), manifestPath.toString());
    }

    private String readString(Path path) throws LoaderException {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new LoaderException("Failed to read artifact file " + path.toString().replace('\\', '/'), exception);
        }
    }

    private LoaderException missingMetadataException(MinecraftProviderConfig config, String version) {
        return new LoaderException(
            "Minecraft metadata for version " +
            version +
            " is unavailable. Provide --minecraft-version-json, populate " +
            cache.displayPath(cache.versionJsonPath(version)) +
            ", or pass --minecraft-fetch-metadata."
        );
    }

    private LoaderException missingCachedManifest(MinecraftProviderConfig config) {
        String hint =
            config.baselineServerEnabled()
                ? "Run minecraftRealServerAcquire or minecraftServerCacheRepair to populate the cache."
                : "Populate the cache first or provide --minecraft-manifest-json.";
        return new LoaderException(
            "Missing cached Minecraft version manifest at " + cache.displayPath(cache.manifestPath()) + " for offline mode. " + hint
        );
    }

    private LoaderException missingCachedVersionJson(MinecraftProviderConfig config, String version) {
        String hint =
            config.baselineServerEnabled()
                ? "Run minecraftRealServerAcquire or minecraftServerCacheRepair to populate the cache."
                : "Populate the cache first, provide --minecraft-version-json, or disable --minecraft-offline.";
        return new LoaderException(
            "Missing cached Minecraft version JSON at " + cache.displayPath(cache.versionJsonPath(version)) + " for version " + version + ". " + hint
        );
    }

    private LoaderException missingCachedServerJar(MinecraftProviderConfig config, String version) {
        String hint =
            config.baselineServerEnabled()
                ? "Run minecraftRealServerAcquire, minecraftRealServerSmoke, or minecraftServerCacheRepair first."
                : "Pass --minecraft-download-server or populate the cache first.";
        return new LoaderException(
            "Missing cached Minecraft server jar at " + cache.displayPath(cache.serverJarPath(version)) + " for version " + version + ". " + hint
        );
    }

    private void recordResolveEvent(MinecraftProviderConfig config, DiagnosticSink diagnosticSink, String name, String message) {
        diagnosticSink.record(new DiagnosticEvent(name, LaunchPhase.COMPLETE.name(), 0L, "ok", message, details(config, null, null, null, null)));
    }

    private String stringOrNull(JsonObject object, String memberName) {
        return object.has(memberName) && !object.get(memberName).isJsonNull() ? object.get(memberName).getAsString() : null;
    }

    private Map<String, String> details(
        MinecraftProviderConfig config,
        MinecraftVersionSelection versionSelection,
        MinecraftArtifactRecord artifact,
        Path artifactReportOutputPath,
        Path artifactLockOutputPath
    ) {
        return details(config, versionSelection, artifact, artifactReportOutputPath, artifactLockOutputPath, null, null);
    }

    private Map<String, String> details(
        MinecraftProviderConfig config,
        MinecraftVersionSelection versionSelection,
        MinecraftArtifactRecord artifact,
        Path artifactReportOutputPath,
        Path artifactLockOutputPath,
        String artifactId,
        String sourceUrl
    ) {
        Map<String, String> details = new LinkedHashMap<>();
        put(details, "projectTargetMinecraft", LoaderMain.TARGET_MINECRAFT_VERSION);
        put(details, "requestedBaselineVersion", config.baselineServerEnabled() ? config.requestedVersionOrBaseline() : null);
        put(details, "resolvedBaselineVersion", versionSelection == null ? null : versionSelection.resolved());
        put(details, "versionSelectionSource", versionSelection == null ? null : versionSelection.source());
        put(details, "minecraftVersion", config.requestedVersion());
        put(details, "cacheDirectory", cache.displayPath(cache.cacheDirectory()));
        put(details, "offline", Boolean.toString(config.offline()));
        put(details, "strict", Boolean.toString(config.cacheStrict()));
        put(details, "repair", Boolean.toString(config.cacheRepair()));
        put(details, "forceRedownload", Boolean.toString(config.forceRedownload()));
        put(details, "networkRequests", Integer.toString(downloader.networkRequestCount()));
        if (artifact != null) {
            put(details, "artifactId", artifact.id());
            put(details, "artifactKind", artifact.kind().id());
            put(details, "serverJar", "server-jar".equals(artifact.id()) ? cache.displayPath(artifact.path()) : null);
            put(details, "downloaded", Boolean.toString(artifact.downloaded()));
            put(details, "verified", Boolean.toString(artifact.verified()));
        }
        if (artifactId != null) {
            put(details, "artifactId", artifactId);
        }
        if (sourceUrl != null) {
            put(details, "sourceUrl", sourceUrl);
        }
        if (artifactReportOutputPath != null) {
            put(details, "artifactReportOutputPath", cache.displayPath(artifactReportOutputPath));
        }
        if (artifactLockOutputPath != null) {
            put(details, "artifactLockOutputPath", cache.displayPath(artifactLockOutputPath));
        }
        return details;
    }

    private void put(Map<String, String> details, String key, String value) {
        if (value != null && !value.isBlank()) {
            details.put(key, value);
        }
    }

    public record Resolution(
        MinecraftMetadataResolver.ResolvedVersionJson resolvedVersionJson,
        MinecraftVersionMetadata metadata,
        Path serverJarPath,
        String serverJarSource,
        MinecraftArtifactCacheReport report,
        MinecraftVersionManifest manifest,
        MinecraftVersionSelection versionSelection,
        MinecraftArtifactRecord manifestRecord,
        MinecraftArtifactRecord versionRecord,
        MinecraftArtifactRecord serverRecord,
        int networkRequestCount
    ) {
    }

    private record VersionContext(
        MinecraftMetadataResolver.ResolvedVersionJson resolvedVersionJson,
        MinecraftVersionManifest manifest,
        MinecraftVersionSelection versionSelection,
        MinecraftArtifactRecord manifestRecord,
        MinecraftArtifactRecord versionRecord
    ) {
    }

    private record ManifestContext(MinecraftVersionManifest manifest, MinecraftArtifactRecord record) {
    }

    private record ServerJarResolution(Path serverJarPath, String serverJarSource, MinecraftArtifactRecord serverRecord) {
    }
}
