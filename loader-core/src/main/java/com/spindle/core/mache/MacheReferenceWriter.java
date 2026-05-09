package com.spindle.core.mache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.spindle.core.diagnostics.LoaderException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MacheReferenceWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MacheReferenceReport report) throws LoaderException {
    JsonObject root = new JsonObject();
    root.addProperty("schema", report.schema());
    root.addProperty("macheDirectory", report.macheDirectory());
    if (report.requestedVersion() == null) {
      root.add("requestedVersion", JsonNull.INSTANCE);
    } else {
      root.addProperty("requestedVersion", report.requestedVersion());
    }
    JsonArray detectedVersions = new JsonArray();
    for (String version : report.detectedVersionDirectories()) {
      detectedVersions.add(version);
    }
    root.add("detectedVersionDirectories", detectedVersions);
    root.addProperty("hasRequestedVersionDirectory", report.hasRequestedVersionDirectory());
    if (report.branchHint() == null) {
      root.add("branchHint", JsonNull.INSTANCE);
    } else {
      root.addProperty("branchHint", report.branchHint());
    }

    JsonObject files = new JsonObject();
    files.addProperty("settingsGradle", report.files().settingsGradle());
    files.addProperty("gradleProperties", report.files().gradleProperties());
    files.addProperty("readme", report.files().readme());
    files.addProperty("license", report.files().license());
    root.add("files", files);

    JsonArray warnings = new JsonArray();
    for (String warning : report.warnings()) {
      warnings.add(warning);
    }
    root.add("warnings", warnings);

    try {
      Files.createDirectories(outputPath.getParent());
      try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
        gson.toJson(root, writer);
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write Mache reference report " + outputPath.getFileName(), exception);
    }
  }
}
