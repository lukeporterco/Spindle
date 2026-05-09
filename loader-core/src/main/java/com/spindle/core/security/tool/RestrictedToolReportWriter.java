package com.spindle.core.security.tool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.security.risk.StaticRiskSignal;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RestrictedToolReportWriter {
  private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, RestrictedToolReport report) throws LoaderException {
    JsonObject root = new JsonObject();
    root.addProperty("schemaVersion", report.schemaVersion());
    root.addProperty("reportKind", report.reportKind());
    root.addProperty("worker", report.worker());
    root.addProperty("mode", report.mode().id());

    JsonObject summary = new JsonObject();
    summary.addProperty("signalCount", report.staticRiskSummary().signalCount());
    summary.addProperty("modCountWithSignals", report.staticRiskSummary().modCountWithSignals());
    root.add("summary", summary);

    JsonArray signals = new JsonArray();
    for (StaticRiskSignal signal : report.staticRiskSignals()) {
      JsonObject signalObject = new JsonObject();
      signalObject.addProperty("ruleId", signal.ruleId().id());
      signalObject.addProperty("severity", signal.severity().id());
      if (signal.modId() != null) {
        signalObject.addProperty("modId", signal.modId());
      }
      if (signal.location() != null) {
        JsonObject location = new JsonObject();
        if (signal.location().kind() != null) {
          location.addProperty("kind", signal.location().kind());
        }
        if (signal.location().value() != null) {
          location.addProperty("value", signal.location().value());
        }
        signalObject.add("location", location);
      }
      signalObject.addProperty("evidence", signal.evidence());
      signalObject.addProperty("message", signal.message());
      signalObject.addProperty("fix", signal.fix());
      signals.add(signalObject);
    }
    root.add("signals", signals);

    try {
      Files.createDirectories(outputPath.getParent());
      try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
        gson.toJson(root, writer);
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write restricted tool report " + outputPath.getFileName(), exception);
    }
  }
}
