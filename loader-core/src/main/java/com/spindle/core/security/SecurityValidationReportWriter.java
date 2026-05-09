package com.spindle.core.security;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.security.risk.StaticRiskSignal;
import com.spindle.core.security.trust.ArtifactTrustEntry;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SecurityValidationReportWriter {
  private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, SecurityValidationReport report) throws LoaderException {
    JsonObject root = new JsonObject();
    root.addProperty("schemaVersion", report.schemaVersion());
    root.addProperty("reportKind", report.reportKind());
    root.addProperty("state", report.state());

    JsonObject loader = new JsonObject();
    loader.addProperty("id", report.loader().id());
    loader.addProperty("version", report.loader().version());
    root.add("loader", loader);

    JsonObject game = new JsonObject();
    game.addProperty("id", report.game().id());
    game.addProperty("version", report.game().version());
    game.addProperty("side", report.game().side());
    root.add("game", game);

    root.addProperty("profileFingerprint", report.profileFingerprint());
    root.addProperty("inputFingerprint", report.inputFingerprint());
    root.addProperty("runtimePolicyFingerprint", report.runtimePolicyFingerprint());
    root.addProperty("securityPolicyFingerprint", report.securityPolicyFingerprint());
    root.addProperty("executionIsolationMode", report.executionIsolationMode());
    root.addProperty("sandboxed", report.sandboxed());
    root.addProperty("sandboxClaim", report.sandboxClaim());
    root.addProperty("fatalCount", report.fatalCount());
    root.addProperty("warningCount", report.warningCount());

    JsonArray validatedSurfaces = new JsonArray();
    for (String validatedSurface : report.validatedSurfaces()) {
      validatedSurfaces.add(validatedSurface);
    }
    root.add("validatedSurfaces", validatedSurfaces);

    JsonObject artifactTrust = new JsonObject();
    JsonArray artifactTrustEntries = new JsonArray();
    for (ArtifactTrustEntry entry : report.artifactTrustEntries()) {
      JsonObject entryObject = new JsonObject();
      entryObject.addProperty("modId", entry.modId());
      entryObject.addProperty("version", entry.version());
      entryObject.addProperty("path", entry.path());
      entryObject.addProperty("sha256", entry.sha256());
      entryObject.addProperty("trustState", entry.trustState().id());
      entryObject.addProperty("trustTier", entry.trustTier().id());
      if (entry.signerId() != null) {
        entryObject.addProperty("signerId", entry.signerId());
      }
      if (entry.signatureKind() != null) {
        entryObject.addProperty("signatureKind", entry.signatureKind());
      }
      entryObject.addProperty("provenanceState", entry.provenanceState().id());
      artifactTrustEntries.add(entryObject);
    }
    artifactTrust.add("entries", artifactTrustEntries);
    JsonObject artifactTrustSummary = new JsonObject();
    artifactTrustSummary.addProperty(
        "localUnsignedCount", report.artifactTrustSummary().localUnsignedCount());
    artifactTrustSummary.addProperty(
        "lockedHashCount", report.artifactTrustSummary().lockedHashCount());
    artifactTrustSummary.addProperty(
        "signedArtifactCount", report.artifactTrustSummary().signedArtifactCount());
    artifactTrustSummary.addProperty(
        "invalidSignatureCount", report.artifactTrustSummary().invalidSignatureCount());
    artifactTrust.add("summary", artifactTrustSummary);
    root.add("artifactTrust", artifactTrust);

    JsonObject riskSignals = new JsonObject();
    JsonObject riskSignalSummary = new JsonObject();
    riskSignalSummary.addProperty("signalCount", report.staticRiskSummary().signalCount());
    riskSignalSummary.addProperty(
        "modCountWithSignals", report.staticRiskSummary().modCountWithSignals());
    riskSignals.add("summary", riskSignalSummary);
    JsonArray signalEntries = new JsonArray();
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
      signalEntries.add(signalObject);
    }
    riskSignals.add("signals", signalEntries);
    root.add("riskSignals", riskSignals);

    JsonArray findings = new JsonArray();
    for (SecurityFinding finding : report.findings()) {
      JsonObject findingObject = new JsonObject();
      findingObject.addProperty("ruleId", finding.ruleId().id());
      findingObject.addProperty("severity", finding.severity().id());
      if (finding.modId() != null) {
        findingObject.addProperty("modId", finding.modId());
      }
      if (finding.location() != null) {
        JsonObject location = new JsonObject();
        if (finding.location().kind() != null) {
          location.addProperty("kind", finding.location().kind());
        }
        if (finding.location().value() != null) {
          location.addProperty("value", finding.location().value());
        }
        findingObject.add("location", location);
      }
      findingObject.addProperty("message", finding.message());
      findingObject.addProperty("fix", finding.fix());
      findings.add(findingObject);
    }
    root.add("findings", findings);

    try {
      Files.createDirectories(outputPath.getParent());
      try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
        gson.toJson(root, writer);
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write security validation report " + outputPath.getFileName(), exception);
    }
  }
}
