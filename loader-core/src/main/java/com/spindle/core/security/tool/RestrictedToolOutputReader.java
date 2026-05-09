package com.spindle.core.security.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.security.SecurityLocation;
import com.spindle.core.security.risk.StaticRiskRuleId;
import com.spindle.core.security.risk.StaticRiskSeverity;
import com.spindle.core.security.risk.StaticRiskSignal;
import com.spindle.core.security.risk.StaticRiskSummary;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RestrictedToolOutputReader {
  public RestrictedToolReport read(Path outputPath, RestrictedToolRequest request)
      throws LoaderException {
    String rawJson = readJson(outputPath);
    rejectAbsolutePathLeakage(rawJson, request);
    try {
      JsonObject root = JsonParser.parseString(rawJson).getAsJsonObject();
      int schemaVersion = requiredInt(root, "schemaVersion");
      if (schemaVersion != RestrictedToolReport.SCHEMA_VERSION) {
        throw new LoaderException(
            "Restricted tool output uses unsupported schemaVersion `" + schemaVersion + "`.");
      }
      String reportKind = requiredString(root, "reportKind");
      if (!RestrictedToolReport.REPORT_KIND.equals(reportKind)) {
        throw new LoaderException(
            "Restricted tool output reportKind must be `"
                + RestrictedToolReport.REPORT_KIND
                + "`.");
      }
      String worker = requiredString(root, "worker");
      if (!request.worker().equals(worker)) {
        throw new LoaderException(
            "Restricted tool output worker `"
                + worker
                + "` did not match expected worker `"
                + request.worker()
                + "`.");
      }
      RestrictedToolExecutionMode mode =
          RestrictedToolExecutionMode.fromId(requiredString(root, "mode"));
      JsonObject summaryObject = requiredObject(root, "summary");
      StaticRiskSummary summary =
          new StaticRiskSummary(
              requiredInt(summaryObject, "signalCount"),
              requiredInt(summaryObject, "modCountWithSignals"));
      List<StaticRiskSignal> signals = parseSignals(requiredArray(root, "signals"), request);
      StaticRiskSummary recomputedSummary = StaticRiskSummary.from(signals);
      if (!summary.equals(recomputedSummary)) {
        throw new LoaderException(
            "Restricted tool output summary did not match the signal payload.");
      }
      return new RestrictedToolReport(
          schemaVersion, reportKind, worker, mode, recomputedSummary, signals);
    } catch (IllegalStateException | JsonParseException | IllegalArgumentException exception) {
      throw new LoaderException(
          "Restricted tool output was not valid deterministic JSON.", exception);
    }
  }

  private String readJson(Path outputPath) throws LoaderException {
    try {
      if (!Files.isRegularFile(outputPath)) {
        throw new LoaderException(
            "Restricted tool output file was not written: "
                + outputPath.toString().replace('\\', '/'));
      }
      return Files.readString(outputPath, StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to read restricted tool output " + outputPath.toString().replace('\\', '/'),
          exception);
    }
  }

  private void rejectAbsolutePathLeakage(String rawJson, RestrictedToolRequest request)
      throws LoaderException {
    Set<String> forbiddenPaths = new HashSet<>();
    forbiddenPaths.add(request.workingDirectory().toString().replace('\\', '/'));
    forbiddenPaths.add(request.outputPath().toString().replace('\\', '/'));
    for (RestrictedToolRequest.ModInput mod : request.mods()) {
      forbiddenPaths.add(mod.jarPath().toString().replace('\\', '/'));
    }
    for (String forbiddenPath : forbiddenPaths) {
      if (forbiddenPath != null && !forbiddenPath.isBlank() && rawJson.contains(forbiddenPath)) {
        throw new LoaderException("Restricted tool output leaked an absolute local path.");
      }
    }
  }

  private List<StaticRiskSignal> parseSignals(JsonArray array, RestrictedToolRequest request)
      throws LoaderException {
    Set<String> knownModIds =
        request.mods().stream()
            .map(RestrictedToolRequest.ModInput::modId)
            .collect(java.util.stream.Collectors.toSet());
    List<StaticRiskSignal> signals = new java.util.ArrayList<>();
    for (JsonElement element : array) {
      JsonObject object = element.getAsJsonObject();
      StaticRiskRuleId ruleId = StaticRiskRuleId.fromId(requiredString(object, "ruleId"));
      StaticRiskSeverity severity = StaticRiskSeverity.fromId(requiredString(object, "severity"));
      if (severity != StaticRiskSeverity.WARNING) {
        throw new LoaderException(
            "Restricted tool output may only contain warning static risk signals.");
      }
      String modId = optionalString(object, "modId");
      if (modId == null || !knownModIds.contains(modId)) {
        throw new LoaderException("Restricted tool output referenced an unknown mod id.");
      }
      SecurityLocation location = null;
      if (object.has("location") && !object.get("location").isJsonNull()) {
        JsonObject locationObject = object.getAsJsonObject("location");
        location =
            SecurityLocation.of(
                optionalString(locationObject, "kind"), optionalString(locationObject, "value"));
      }
      signals.add(
          new StaticRiskSignal(
              ruleId,
              severity,
              modId,
              location,
              optionalString(object, "evidence"),
              requiredString(object, "message"),
              requiredString(object, "fix")));
    }
    return signals;
  }

  private JsonObject requiredObject(JsonObject root, String fieldName) throws LoaderException {
    if (!root.has(fieldName) || !root.get(fieldName).isJsonObject()) {
      throw new LoaderException(
          "Restricted tool output is missing object field `" + fieldName + "`.");
    }
    return root.getAsJsonObject(fieldName);
  }

  private JsonArray requiredArray(JsonObject root, String fieldName) throws LoaderException {
    if (!root.has(fieldName) || !root.get(fieldName).isJsonArray()) {
      throw new LoaderException(
          "Restricted tool output is missing array field `" + fieldName + "`.");
    }
    return root.getAsJsonArray(fieldName);
  }

  private String requiredString(JsonObject root, String fieldName) throws LoaderException {
    String value = optionalString(root, fieldName);
    if (value == null) {
      throw new LoaderException(
          "Restricted tool output is missing string field `" + fieldName + "`.");
    }
    return value;
  }

  private String optionalString(JsonObject root, String fieldName) {
    if (!root.has(fieldName) || root.get(fieldName).isJsonNull()) {
      return null;
    }
    String value = root.get(fieldName).getAsString().trim();
    return value.isEmpty() ? null : value;
  }

  private int requiredInt(JsonObject root, String fieldName) throws LoaderException {
    if (!root.has(fieldName) || root.get(fieldName).isJsonNull()) {
      throw new LoaderException(
          "Restricted tool output is missing integer field `" + fieldName + "`.");
    }
    return root.get(fieldName).getAsInt();
  }
}
