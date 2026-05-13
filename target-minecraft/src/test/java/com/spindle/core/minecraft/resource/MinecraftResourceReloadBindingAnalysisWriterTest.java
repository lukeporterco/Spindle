package com.spindle.core.minecraft.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftSide;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinecraftResourceReloadBindingAnalysisWriterTest {
  @TempDir Path tempDirectory;

  private final MinecraftResourceReloadBindingAnalysisWriter writer =
      new MinecraftResourceReloadBindingAnalysisWriter();

  @Test
  void writerSerializesDeterministicJsonAcrossTwoOutputPaths() throws Exception {
    MinecraftResourceReloadBindingAnalysis analysis = classifiedAnalysis();
    Path first = tempDirectory.resolve("a/minecraft-resource-reload-binding-analysis.json");
    Path second = tempDirectory.resolve("b/minecraft-resource-reload-binding-analysis.json");

    writer.write(first, analysis);
    writer.write(second, analysis);

    String firstJson = Files.readString(first, StandardCharsets.UTF_8);
    String secondJson = Files.readString(second, StandardCharsets.UTF_8);

    assertEquals(firstJson, secondJson);
    assertTrue(firstJson.contains("\"schema\": 1"));
    assertTrue(firstJson.contains("\"milestoneName\": \"Target-18\""));
    assertTrue(
        firstJson.contains("\"sourceResourceReloadSymbolAnalysisMilestone\": \"Target-17\""));
  }

  @Test
  void writerSerializesBindingStatusAccessStrategyCandidateAndAggregateCounts() {
    String json = writer.toJson(classifiedAnalysis()).toString();

    assertTrue(json.contains("\"bindingStatus\":\"BINDING_REQUIREMENTS_CLASSIFIED\""));
    assertTrue(json.contains("\"accessStrategy\":\"CLASS_REFERENCE_ONLY\""));
    assertTrue(
        json.contains(
            "\"sourceCandidateId\":\"target-17.minecraft.resources.reload.candidate.001\""));
    assertTrue(json.contains("\"sourceCandidateKind\":\"CLASS_NAME_REFERENCE\""));
    assertTrue(json.contains("\"classReferenceOnlyCount\":1"));
    assertTrue(json.contains("\"methodBoundaryAnalysisRequiredCount\":1"));
    assertTrue(json.contains("\"fieldAccessRequiredCount\":0"));
  }

  @Test
  void writerSerializesNullsForClassCandidatesAndRejectedCandidates() {
    String classifiedJson = writer.toJson(classifiedAnalysis()).toString();
    String rejectedJson = writer.toJson(rejectedAnalysis()).toString();

    assertTrue(classifiedJson.contains("\"memberName\":null"));
    assertTrue(classifiedJson.contains("\"descriptor\":null"));
    assertTrue(rejectedJson.contains("\"selectable\":false"));
    assertTrue(
        rejectedJson.contains(
            "\"sourceRejectionReason\":\"Only net/minecraft/* owners are selectable resource/reload symbol candidates in Target-17.\""));
    assertTrue(rejectedJson.contains("\"accessStrategy\":\"NONE\""));
  }

  @Test
  void writerSerializesEveryBindingStatus() {
    assertTrue(
        writer
            .toJson(classifiedAnalysis())
            .toString()
            .contains("\"bindingStatus\":\"BINDING_REQUIREMENTS_CLASSIFIED\""));
    assertTrue(
        writer
            .toJson(noCandidatesAnalysis())
            .toString()
            .contains("\"bindingStatus\":\"NO_SYMBOL_CANDIDATES\""));
    assertTrue(
        writer
            .toJson(rejectedAnalysis())
            .toString()
            .contains("\"bindingStatus\":\"ONLY_REJECTED_SYMBOL_CANDIDATES\""));
    assertTrue(
        writer
            .toJson(blockedAnalysis())
            .toString()
            .contains("\"bindingStatus\":\"UPSTREAM_GATE_BLOCKED\""));
  }

  @Test
  void writerDoesNotSerializeByteArraysBytecodeOrSensitivePayloads() {
    String json = writer.toJson(classifiedAnalysis()).toString();

    assertFalse(json.contains("bytes"));
    assertFalse(json.contains("bytecode"));
    assertFalse(json.contains("file_contents"));
    assertFalse(json.contains("resource_contents"));
    assertFalse(json.contains("datapack_contents"));
    assertFalse(json.contains("generated_json_contents"));
    assertFalse(json.contains("registry_contents"));
    assertFalse(json.contains("command_tree_contents"));
    assertFalse(json.contains("mappings"));
    assertFalse(json.contains("decompiled_source"));
  }

  private MinecraftResourceReloadBindingAnalysis classifiedAnalysis() {
    return new MinecraftResourceReloadBindingAnalysis(
        1,
        "Target-18",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.data_resources_reload",
        "Target-17",
        "minecraft.resources.reload.discovery",
        true,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        true,
        "CANDIDATES_DISCOVERED",
        true,
        true,
        null,
        MinecraftResourceReloadBindingStatus.BINDING_REQUIREMENTS_CLASSIFIED,
        2,
        2,
        2,
        0,
        1,
        1,
        0,
        1,
        1,
        false,
        false,
        "Do not implement resource reload handling yet; use these classified requirements as input to future resource visibility and SteelHook primitive decisions.",
        List.of(
            new MinecraftResourceReloadBindingCandidate(
                "target-17.minecraft.resources.reload.candidate.001",
                "CLASS_NAME_REFERENCE",
                "minecraft.resources.reload.discovery",
                "net/minecraft/server/ServerResources",
                null,
                null,
                false,
                List.of("reload", "resource"),
                true,
                null,
                MinecraftResourceReloadAccessStrategy.CLASS_REFERENCE_ONLY,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                "Class/package discovery is useful evidence but does not identify a callable reload boundary or accessible resource value."),
            new MinecraftResourceReloadBindingCandidate(
                "target-17.minecraft.resources.reload.candidate.002",
                "METHOD_NAME_REFERENCE",
                "minecraft.resources.reload.discovery",
                "net/minecraft/server/ReloadState",
                "reload",
                "()V",
                false,
                List.of("reload"),
                true,
                null,
                MinecraftResourceReloadAccessStrategy.INSTANCE_METHOD_RECEIVER_CAPTURE_REQUIRED,
                true,
                true,
                true,
                false,
                false,
                true,
                true,
                true,
                false,
                false,
                "Instance method metadata may identify a future reload boundary, but receiver/value capture and reload semantics remain unresolved.")));
  }

  private MinecraftResourceReloadBindingAnalysis noCandidatesAnalysis() {
    return new MinecraftResourceReloadBindingAnalysis(
        1,
        "Target-18",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.data_resources_reload",
        "Target-17",
        "minecraft.resources.reload.discovery",
        true,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        true,
        "NO_CANDIDATES",
        false,
        true,
        null,
        MinecraftResourceReloadBindingStatus.NO_SYMBOL_CANDIDATES,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        false,
        false,
        "Do not implement resource reload handling yet; no resource/reload symbol candidates were discovered.",
        List.of());
  }

  private MinecraftResourceReloadBindingAnalysis rejectedAnalysis() {
    return new MinecraftResourceReloadBindingAnalysis(
        1,
        "Target-18",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.data_resources_reload",
        "Target-17",
        "minecraft.resources.reload.discovery",
        true,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        true,
        "ONLY_REJECTED_CANDIDATES",
        false,
        true,
        null,
        MinecraftResourceReloadBindingStatus.ONLY_REJECTED_SYMBOL_CANDIDATES,
        1,
        1,
        0,
        1,
        0,
        0,
        0,
        0,
        0,
        false,
        false,
        "Do not implement resource reload handling yet; only rejected non-net/minecraft candidates were discovered.",
        List.of(
            new MinecraftResourceReloadBindingCandidate(
                "target-17.minecraft.resources.reload.candidate.001",
                "CLASS_NAME_REFERENCE",
                "minecraft.resources.reload.discovery",
                "com/example/ReloadState",
                null,
                null,
                false,
                List.of("reload"),
                false,
                "Only net/minecraft/* owners are selectable resource/reload symbol candidates in Target-17.",
                MinecraftResourceReloadAccessStrategy.NONE,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                "Rejected source candidate carried forward; non-net/minecraft candidates are not resource/reload binding targets.")));
  }

  private MinecraftResourceReloadBindingAnalysis blockedAnalysis() {
    return new MinecraftResourceReloadBindingAnalysis(
        1,
        "Target-18",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.data_resources_reload",
        "Target-17",
        "minecraft.resources.reload.discovery",
        true,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        "UPSTREAM_GATE_BLOCKED",
        false,
        false,
        "Target-17 gate failure reason.",
        MinecraftResourceReloadBindingStatus.UPSTREAM_GATE_BLOCKED,
        1,
        1,
        1,
        0,
        1,
        0,
        0,
        0,
        0,
        false,
        false,
        "Restore the Target-16 and Target-17 upstream gates before classifying resource/reload binding requirements.",
        List.of(
            new MinecraftResourceReloadBindingCandidate(
                "target-17.minecraft.resources.reload.candidate.001",
                "CLASS_NAME_REFERENCE",
                "minecraft.resources.reload.discovery",
                "net/minecraft/server/ServerResources",
                null,
                null,
                false,
                List.of("reload"),
                true,
                null,
                MinecraftResourceReloadAccessStrategy.CLASS_REFERENCE_ONLY,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                "Class/package discovery is useful evidence but does not identify a callable reload boundary or accessible resource value.")));
  }
}
