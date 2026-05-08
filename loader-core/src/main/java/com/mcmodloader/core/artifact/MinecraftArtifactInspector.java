package com.mcmodloader.core.artifact;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mcmodloader.core.diagnostics.DiagnosticEvent;
import com.mcmodloader.core.diagnostics.DiagnosticSink;
import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.launch.LaunchPhase;
import com.mcmodloader.core.minecraft.MinecraftProviderConfig;
import com.mcmodloader.core.minecraft.MinecraftVersionMetadata;
import com.mcmodloader.core.minecraft.MinecraftVersionMetadataParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MinecraftArtifactInspector {
    private final MinecraftArtifactCache cache;
    private final MinecraftArtifactVerifier verifier;
    private final MinecraftVersionMetadataParser metadataParser;
    private final MinecraftArtifactCacheWriter writer;

    public MinecraftArtifactInspector(MinecraftArtifactCache cache) {
        this(cache, new MinecraftArtifactVerifier(), new MinecraftVersionMetadataParser(), new MinecraftArtifactCacheWriter());
    }

    public MinecraftArtifactInspector(
        MinecraftArtifactCache cache,
        MinecraftArtifactVerifier verifier,
        MinecraftVersionMetadataParser metadataParser,
        MinecraftArtifactCacheWriter writer
    ) {
        this.cache = cache;
        this.verifier = verifier;
        this.metadataParser = metadataParser;
        this.writer = writer;
    }

    public MinecraftArtifactCacheReport inspect(MinecraftProviderConfig config, DiagnosticSink diagnosticSink) throws LoaderException {
        diagnosticSink.record(
            new DiagnosticEvent(
                "minecraft.artifact_cache.inspect",
                LaunchPhase.COMPLETE.name(),
                0L,
                "ok",
                "Minecraft artifact cache inspection started",
                details(config, null, null)
            )
        );

        List<String> warnings = new ArrayList<>();
        List<MinecraftArtifactRecord> artifacts = new ArrayList<>();

        MinecraftArtifactRecord manifestRecord = inspectPresent(
            "version-manifest",
            ArtifactKind.METADATA,
            cache.manifestPath(),
            null,
            null,
            config.cacheStrict(),
            warnings
        );
        artifacts.add(manifestRecord);

        Path versionJsonPath = cache.versionJsonPath(config.requestedVersion());
        MinecraftArtifactRecord versionRecord =
            inspectPresent("version-json", ArtifactKind.METADATA, versionJsonPath, null, null, config.cacheStrict(), warnings);
        artifacts.add(versionRecord);

        MinecraftVersionMetadata metadata = null;
        if (Files.isRegularFile(versionJsonPath)) {
            metadata = metadataParser.parse(readString(versionJsonPath), versionJsonPath.toString(), config.side());
        }

        String expectedSha1 = metadata == null || metadata.serverDownload() == null ? null : metadata.serverDownload().sha1();
        Long expectedSize =
            metadata == null || metadata.serverDownload() == null || metadata.serverDownload().size() <= 0L ? null : metadata.serverDownload().size();
        Path serverJarPath = cache.serverJarPath(config.requestedVersion());
        MinecraftArtifactRecord serverRecord =
            inspectPresent("server-jar", ArtifactKind.SERVER, serverJarPath, expectedSha1, expectedSize, config.cacheStrict(), warnings);
        artifacts.add(serverRecord);

        verifyLockIfPresent(config, serverRecord, warnings, diagnosticSink);

        if (!Files.isRegularFile(versionJsonPath)) {
            warnings.add("Cached version JSON is missing.");
        }
        if (!Files.isRegularFile(serverJarPath)) {
            warnings.add("Cached server jar is missing.");
        }

        MinecraftArtifactCacheReport report =
            new MinecraftArtifactCacheReport(
                1,
                config.requestedVersion(),
                cache.displayPath(cache.cacheDirectory()),
                config.offline(),
                true,
                config.cacheRepair(),
                config.forceRedownload(),
                artifacts,
                warnings
            );
        writer.writeReport(cache.artifactReportPath(), cache, report);
        diagnosticSink.record(
            new DiagnosticEvent(
                "minecraft.artifact_cache.write",
                LaunchPhase.COMPLETE.name(),
                0L,
                "ok",
                "Minecraft artifact cache report written",
                details(config, cache.artifactReportPath(), cache.artifactLockPath())
            )
        );
        diagnosticSink.record(
            new DiagnosticEvent(
                "minecraft.cache_inspect.complete",
                LaunchPhase.COMPLETE.name(),
                0L,
                "ok",
                "Minecraft cache inspection complete",
                details(config, cache.artifactReportPath(), cache.artifactLockPath())
            )
        );
        return report;
    }

    private MinecraftArtifactRecord inspectPresent(
        String id,
        ArtifactKind kind,
        Path path,
        String expectedSha1,
        Long expectedSize,
        boolean strict,
        List<String> warnings
    ) throws LoaderException {
        if (!Files.isRegularFile(path)) {
            return new MinecraftArtifactRecord(id, kind, path, null, expectedSha1, null, null, false, false, false, ArtifactStatus.MISSING);
        }

        try {
            MinecraftArtifactVerifier.VerificationResult verification = verifier.verify(path, expectedSha1, expectedSize);
            ArtifactStatus status = verification.verified() ? ArtifactStatus.VERIFIED : ArtifactStatus.UNVERIFIABLE;
            if (!verification.verified() && kind == ArtifactKind.SERVER) {
                warnings.add("Cached server jar is present but expected SHA-1 or size metadata was unavailable.");
            }
            return new MinecraftArtifactRecord(
                id,
                kind,
                path,
                null,
                expectedSha1,
                verification.sha256(),
                verification.size(),
                true,
                false,
                verification.verified(),
                status
            );
        } catch (LoaderException exception) {
            if (strict) {
                throw exception;
            }
            warnings.add(exception.getMessage());
            return new MinecraftArtifactRecord(id, kind, path, null, expectedSha1, null, null, true, false, false, ArtifactStatus.INVALID);
        }
    }

    private void verifyLockIfPresent(
        MinecraftProviderConfig config,
        MinecraftArtifactRecord serverRecord,
        List<String> warnings,
        DiagnosticSink diagnosticSink
    ) throws LoaderException {
        Path lockPath = cache.artifactLockPath();
        if (!Files.isRegularFile(lockPath) || !serverRecord.present()) {
            return;
        }
        JsonObject root = JsonParser.parseString(readString(lockPath)).getAsJsonObject();
        JsonArray artifacts = root.getAsJsonArray("artifacts");
        if (artifacts == null || artifacts.isEmpty()) {
            return;
        }

        JsonObject entry = artifacts.get(0).getAsJsonObject();
        String sha1 = entry.has("sha1") && !entry.get("sha1").isJsonNull() ? entry.get("sha1").getAsString() : null;
        String sha256 = entry.has("sha256") && !entry.get("sha256").isJsonNull() ? entry.get("sha256").getAsString() : null;
        Long size = entry.has("size") && !entry.get("size").isJsonNull() ? entry.get("size").getAsLong() : null;
        boolean matches =
            Objects.equals(sha1, serverRecord.sha1()) &&
            Objects.equals(sha256, serverRecord.sha256()) &&
            Objects.equals(size, serverRecord.size());
        diagnosticSink.record(
            new DiagnosticEvent(
                "minecraft.artifact_lock.verify",
                LaunchPhase.COMPLETE.name(),
                0L,
                matches ? "ok" : "warning",
                matches ? "Minecraft server artifact lock verified" : "Minecraft server artifact lock mismatch",
                details(config, cache.artifactReportPath(), lockPath)
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

    private Map<String, String> details(MinecraftProviderConfig config, Path reportPath, Path lockPath) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("minecraftVersion", config.requestedVersion());
        details.put("cacheDirectory", cache.displayPath(cache.cacheDirectory()));
        details.put("offline", Boolean.toString(config.offline()));
        details.put("strict", Boolean.toString(config.cacheStrict()));
        details.put("repair", Boolean.toString(config.cacheRepair()));
        details.put("forceRedownload", Boolean.toString(config.forceRedownload()));
        if (reportPath != null) {
            details.put("artifactReportOutputPath", cache.displayPath(reportPath));
        }
        if (lockPath != null) {
            details.put("artifactLockOutputPath", cache.displayPath(lockPath));
        }
        return details;
    }

    private String readString(Path path) throws LoaderException {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new LoaderException("Failed to read artifact file " + path.toString().replace('\\', '/'), exception);
        }
    }
}
