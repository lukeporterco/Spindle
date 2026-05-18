package com.spindle.core.minecraft.hook.steelhook;

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
import java.util.List;

public final class SteelHook04PrimitiveBoundaryReportWriter {
  public static final String REPORT_FILE_NAME = "minecraft-steelhook-0-4-primitive-boundary.json";

  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, SteelHook04PrimitiveBoundaryReport report)
      throws LoaderException {
    try {
      Path parent = outputPath.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
        gson.toJson(toJson(report), writer);
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write SteelHook 0.4 primitive boundary report " + outputPath, exception);
    }
  }

  JsonObject toJson(SteelHook04PrimitiveBoundaryReport report) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", report.schema());
    addString(root, "milestoneName", report.milestoneName());
    addString(root, "target", report.target());
    addString(root, "steelHookVersion", report.steelHookVersion());
    addString(root, "sourceSteelHook03Milestone", report.sourceSteelHook03Milestone());
    addString(root, "sourceSteelHook03Status", report.sourceSteelHook03Status());
    root.addProperty("sourceSteelHook03CompletionReady", report.sourceSteelHook03CompletionReady());
    addString(root, "sourceSteelHook03HandoffStatus", report.sourceSteelHook03HandoffStatus());
    root.addProperty("gatePassed", report.gatePassed());
    addString(root, "gateFailureReason", report.gateFailureReason());
    addString(root, "boundaryStatus", report.boundaryStatus().name());
    addString(root, "boundaryStatusId", report.boundaryStatus().id());
    addString(root, "nextDirection", report.nextDirection().name());
    addString(root, "nextDirectionId", report.nextDirection().id());
    addString(root, "nextRecommendedAction", report.nextRecommendedAction());
    root.addProperty("analysisOnly", report.analysisOnly());
    root.addProperty("bytecodeModified", report.bytecodeModified());
    root.addProperty("transformedClassBytesProduced", report.transformedClassBytesProduced());
    root.addProperty("runtimeClassLoadingPathEnabled", report.runtimeClassLoadingPathEnabled());
    root.addProperty("classLoadingOccurred", report.classLoadingOccurred());
    root.addProperty("serverLaunchOccurred", report.serverLaunchOccurred());
    root.addProperty("minecraftMainInvoked", report.minecraftMainInvoked());
    root.addProperty("hookInstallationOccurred", report.hookInstallationOccurred());
    root.addProperty("runtimeDispatchOccurred", report.runtimeDispatchOccurred());
    root.addProperty("publicApiExposed", report.publicApiExposed());
    root.addProperty("javaAgentUsed", report.javaAgentUsed());
    root.addProperty("mixinUsed", report.mixinUsed());
    root.addProperty("javaModExecutionSandboxed", report.javaModExecutionSandboxed());
    root.addProperty("approvedPrimitiveCount", report.approvedPrimitiveCount());
    root.add("candidates", candidates(report.candidates()));
    root.add("allowedFixtureShapes", fixtureShapes(report.allowedFixtureShapes()));
    root.add("unsupportedFixtureShapes", fixtureShapes(report.unsupportedFixtureShapes()));
    root.add("rejectionTaxonomy", rejectionTaxonomy(report.rejectionTaxonomy()));
    root.add("evidenceRequirements", evidenceRequirements(report.evidenceRequirements()));
    root.add("findings", findings(report.findings()));
    return root;
  }

  private JsonArray candidates(List<SteelHook04PrimitiveCandidate> candidates) {
    JsonArray array = new JsonArray();
    for (SteelHook04PrimitiveCandidate candidate : candidates) {
      JsonObject object = new JsonObject();
      addString(object, "id", candidate.id());
      addString(object, "primitiveKind", candidate.primitiveKind().id());
      addString(object, "candidateStatus", candidate.candidateStatus().id());
      object.addProperty("internalOnly", candidate.internalOnly());
      object.addProperty("publicApiExposed", candidate.publicApiExposed());
      object.addProperty("nonPublicApi", candidate.nonPublicApi());
      object.addProperty("runtimeReady", candidate.runtimeReady());
      object.addProperty("gatedRuntimeReady", candidate.gatedRuntimeReady());
      object.addProperty("implementedInTarget32", candidate.implementedInTarget32());
      addString(object, "targetFollowOnPass", candidate.targetFollowOnPass());
      addString(object, "fixtureShapeSummary", candidate.fixtureShapeSummary());
      addString(object, "evidenceSummary", candidate.evidenceSummary());
      object.add("allowedFixtureShapes", fixtureShapes(candidate.allowedFixtureShapes()));
      object.add("notes", notes(candidate.notes()));
      array.add(object);
    }
    return array;
  }

  private JsonArray fixtureShapes(List<SteelHook04FixtureShape> fixtureShapes) {
    JsonArray array = new JsonArray();
    for (SteelHook04FixtureShape fixtureShape : fixtureShapes) {
      array.add(fixtureShape.id());
    }
    return array;
  }

  private JsonArray rejectionTaxonomy(List<SteelHook04RejectionReason> rejectionReasons) {
    JsonArray array = new JsonArray();
    for (SteelHook04RejectionReason rejectionReason : rejectionReasons) {
      array.add(rejectionReason.id());
    }
    return array;
  }

  private JsonArray evidenceRequirements(List<SteelHook04EvidenceRequirement> requirements) {
    JsonArray array = new JsonArray();
    for (SteelHook04EvidenceRequirement requirement : requirements) {
      JsonObject object = new JsonObject();
      addString(object, "id", requirement.id());
      addString(object, "targetPass", requirement.targetPass());
      object.add("primitiveKinds", primitiveKinds(requirement.primitiveKinds()));
      addString(object, "summary", requirement.summary());
      object.add("requiredRejections", rejectionTaxonomy(requirement.requiredRejections()));
      array.add(object);
    }
    return array;
  }

  private JsonArray primitiveKinds(List<SteelHook04PrimitiveKind> primitiveKinds) {
    JsonArray array = new JsonArray();
    for (SteelHook04PrimitiveKind primitiveKind : primitiveKinds) {
      array.add(primitiveKind.id());
    }
    return array;
  }

  private JsonArray findings(List<SteelHook04PrimitiveFinding> findings) {
    JsonArray array = new JsonArray();
    for (SteelHook04PrimitiveFinding finding : findings) {
      JsonObject object = new JsonObject();
      addString(object, "id", finding.id());
      addString(object, "checkName", finding.checkName());
      addString(object, "status", finding.status().id());
      object.addProperty("blocking", finding.blocking());
      addString(object, "summary", finding.summary());
      addString(object, "details", finding.details());
      array.add(object);
    }
    return array;
  }

  private JsonArray notes(List<String> notes) {
    JsonArray array = new JsonArray();
    for (String note : notes) {
      if (note == null) {
        array.add(JsonNull.INSTANCE);
      } else {
        array.add(note);
      }
    }
    return array;
  }

  private void addString(JsonObject object, String name, String value) {
    if (value == null) {
      object.add(name, JsonNull.INSTANCE);
    } else {
      object.addProperty(name, value);
    }
  }
}
