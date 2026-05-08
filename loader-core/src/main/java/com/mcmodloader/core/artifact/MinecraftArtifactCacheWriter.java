package com.mcmodloader.core.artifact;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mcmodloader.core.diagnostics.LoaderException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public final class MinecraftArtifactCacheWriter {
    private final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

    public void writeReport(Path outputPath, MinecraftArtifactCache cache, MinecraftArtifactCacheReport report) throws LoaderException {
        JsonObject root = new JsonObject();
        root.addProperty("schema", report.schema());
        root.addProperty("projectTargetMinecraft", report.projectTargetMinecraft());
        root.addProperty("minecraftVersion", report.minecraftVersion());
        if (report.baselineMinecraft() == null || report.baselineMinecraft().isBlank()) {
            root.add("baselineMinecraft", JsonNull.INSTANCE);
        } else {
            root.addProperty("baselineMinecraft", report.baselineMinecraft());
        }
        root.addProperty("cacheDirectory", cache.displayPath(cache.cacheDirectory()));
        root.addProperty("offline", report.offline());
        root.addProperty("inspectOnly", report.inspectOnly());
        root.addProperty("repair", report.repair());
        root.addProperty("forceRedownload", report.forceRedownload());
        root.addProperty("networkRequestCount", report.networkRequestCount());

        JsonArray artifacts = new JsonArray();
        List<MinecraftArtifactRecord> sortedArtifacts =
            report.artifacts().stream().sorted(Comparator.comparing((MinecraftArtifactRecord record) -> record.kind().id()).thenComparing(MinecraftArtifactRecord::id)).toList();
        for (MinecraftArtifactRecord record : sortedArtifacts) {
            JsonObject entry = new JsonObject();
            entry.addProperty("id", record.id());
            entry.addProperty("kind", record.kind().id());
            entry.addProperty("path", cache.displayPath(record.path()));
            if (record.sourceUrl() == null || record.sourceUrl().isBlank()) {
                entry.add("sourceUrl", JsonNull.INSTANCE);
            } else {
                entry.addProperty("sourceUrl", record.sourceUrl());
            }
            if (record.sha1() == null || record.sha1().isBlank()) {
                entry.add("sha1", JsonNull.INSTANCE);
            } else {
                entry.addProperty("sha1", record.sha1());
            }
            if (record.sha256() == null || record.sha256().isBlank()) {
                entry.add("sha256", JsonNull.INSTANCE);
            } else {
                entry.addProperty("sha256", record.sha256());
            }
            if (record.size() == null) {
                entry.add("size", JsonNull.INSTANCE);
            } else {
                entry.addProperty("size", record.size());
            }
            entry.addProperty("present", record.present());
            entry.addProperty("downloaded", record.downloaded());
            entry.addProperty("verified", record.verified());
            entry.addProperty("status", record.status().id());
            artifacts.add(entry);
        }
        root.add("artifacts", artifacts);

        JsonArray warnings = new JsonArray();
        for (String warning : report.warnings()) {
            warnings.add(warning);
        }
        root.add("warnings", warnings);

        writeJson(outputPath, root, "artifact cache report");
    }

    public void writeServerLock(
        Path outputPath,
        MinecraftArtifactCache cache,
        String projectTargetMinecraft,
        String baselineMinecraft,
        MinecraftArtifactRecord serverJar,
        String verifiedAt
    ) throws LoaderException {
        JsonObject root = new JsonObject();
        root.addProperty("schema", 1);
        root.addProperty("projectTargetMinecraft", projectTargetMinecraft);
        root.addProperty("baselineMinecraft", baselineMinecraft);
        root.addProperty("createdBy", "MCModLoader");
        JsonArray artifacts = new JsonArray();
        JsonObject entry = new JsonObject();
        entry.addProperty("id", serverJar.id());
        entry.addProperty("kind", serverJar.kind().id());
        entry.addProperty("path", cache.displayPath(serverJar.path()));
        if (serverJar.sourceUrl() == null || serverJar.sourceUrl().isBlank()) {
            entry.add("sourceUrl", JsonNull.INSTANCE);
        } else {
            entry.addProperty("sourceUrl", serverJar.sourceUrl());
        }
        if (serverJar.sha1() == null || serverJar.sha1().isBlank()) {
            entry.add("sha1", JsonNull.INSTANCE);
        } else {
            entry.addProperty("sha1", serverJar.sha1());
        }
        if (serverJar.sha256() == null || serverJar.sha256().isBlank()) {
            entry.add("sha256", JsonNull.INSTANCE);
        } else {
            entry.addProperty("sha256", serverJar.sha256());
        }
        if (serverJar.size() == null) {
            entry.add("size", JsonNull.INSTANCE);
        } else {
            entry.addProperty("size", serverJar.size());
        }
        if (verifiedAt == null || verifiedAt.isBlank()) {
            entry.add("verifiedAt", JsonNull.INSTANCE);
        } else {
            entry.addProperty("verifiedAt", verifiedAt);
        }
        artifacts.add(entry);
        root.add("artifacts", artifacts);

        writeJson(outputPath, root, "artifact lock");
    }

    private void writeJson(Path outputPath, JsonObject root, String label) throws LoaderException {
        try {
            Path parent = outputPath.toAbsolutePath().normalize().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
                gson.toJson(root, writer);
            }
        } catch (IOException exception) {
            throw new LoaderException("Failed to write " + label + " " + outputPath.getFileName(), exception);
        }
    }
}
