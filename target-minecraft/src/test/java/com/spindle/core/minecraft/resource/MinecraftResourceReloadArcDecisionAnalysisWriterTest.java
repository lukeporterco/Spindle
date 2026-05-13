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

class MinecraftResourceReloadArcDecisionAnalysisWriterTest {
  @TempDir Path tempDirectory;

  private final MinecraftResourceReloadArcDecisionAnalysisWriter writer =
      new MinecraftResourceReloadArcDecisionAnalysisWriter();

  @Test
  void deterministicJsonOutputAcrossTwoOutputPaths() throws Exception {
    MinecraftResourceReloadArcDecisionAnalysis analysis = caboosedAnalysis();
    Path first = tempDirectory.resolve("a/minecraft-resource-reload-arc-decision.json");
    Path second = tempDirectory.resolve("b/minecraft-resource-reload-arc-decision.json");

    writer.write(first, analysis);
    writer.write(second, analysis);

    String firstJson = Files.readString(first, StandardCharsets.UTF_8);
    String secondJson = Files.readString(second, StandardCharsets.UTF_8);

    assertEquals(firstJson, secondJson);
    assertTrue(firstJson.contains("\"schema\": 1"));
    assertTrue(firstJson.contains("\"milestoneName\": \"Target-20\""));
    assertTrue(firstJson.contains("\"sourceResourceReloadAnalysisMilestone\": \"Target-16\""));
    assertTrue(
        firstJson.contains("\"sourceResourceReloadSymbolAnalysisMilestone\": \"Target-17\""));
    assertTrue(
        firstJson.contains("\"sourceResourceReloadBindingAnalysisMilestone\": \"Target-18\""));
    assertTrue(
        firstJson.contains(
            "\"sourceResourceVisibilityGenerationAnalysisMilestone\": \"Target-19\""));
  }

  @Test
  void serializesDecisionFieldsNullsAndFindingsInOrder() {
    String caboosedJson = writer.toJson(caboosedAnalysis()).toString();
    String blockedJson = writer.toJson(blockedAnalysis()).toString();

    assertTrue(caboosedJson.contains("\"decisionStatus\":\"RESOURCE_RELOAD_ARC_CABOOSED\""));
    assertTrue(blockedJson.contains("\"decisionStatus\":\"UPSTREAM_GATE_BLOCKED\""));
    assertTrue(caboosedJson.contains("\"nextDirection\":\"MOVE_TO_REGISTRY_BOOTSTRAP\""));
    assertTrue(blockedJson.contains("\"nextDirection\":\"UNDECIDED_UPSTREAM_BLOCKED\""));
    assertTrue(caboosedJson.contains("\"registryBootstrapRecommended\":true"));
    assertTrue(caboosedJson.contains("\"resourceReloadArcCompleteForNow\":true"));
    assertTrue(caboosedJson.contains("\"gateFailureReason\":null"));
    assertTrue(blockedJson.contains("\"gateFailureReason\":"));

    int first = caboosedJson.indexOf("\"id\":\"target-20.resource.reload.arc.finding.001\"");
    int second = caboosedJson.indexOf("\"id\":\"target-20.resource.reload.arc.finding.002\"");
    int third = caboosedJson.indexOf("\"id\":\"target-20.resource.reload.arc.finding.003\"");
    int fourth = caboosedJson.indexOf("\"id\":\"target-20.resource.reload.arc.finding.004\"");
    int fifth = caboosedJson.indexOf("\"id\":\"target-20.resource.reload.arc.finding.005\"");
    assertTrue(first < second);
    assertTrue(second < third);
    assertTrue(third < fourth);
    assertTrue(fourth < fifth);
  }

  @Test
  void doesNotSerializeSensitivePayloads() {
    String json = writer.toJson(caboosedAnalysis()).toString();

    assertFalse(json.contains("bytes"));
    assertFalse(json.contains("bytecode"));
    assertFalse(json.contains("file_contents"));
    assertFalse(json.contains("resource_contents"));
    assertFalse(json.contains("datapack_contents"));
    assertFalse(json.contains("generated_json_contents"));
    assertFalse(json.contains("generated_files"));
    assertFalse(json.contains("registry_contents"));
    assertFalse(json.contains("command_tree_contents"));
    assertFalse(json.contains("mappings"));
    assertFalse(json.contains("decompiled_source"));
  }

  private MinecraftResourceReloadArcDecisionAnalysis caboosedAnalysis() {
    return new MinecraftResourceReloadArcDecisionAnalysis(
        1,
        "Target-20",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.data_resources_reload",
        "Target-16",
        "Target-17",
        "Target-18",
        "Target-19",
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
        false,
        true,
        true,
        "CANDIDATES_DISCOVERED",
        true,
        "BINDING_REQUIREMENTS_CLASSIFIED",
        false,
        false,
        true,
        "SEPARATION_CLASSIFIED",
        true,
        true,
        true,
        true,
        null,
        MinecraftResourceReloadArcDecisionStatus.RESOURCE_RELOAD_ARC_CABOOSED,
        MinecraftResourceReloadNextDirection.MOVE_TO_REGISTRY_BOOTSTRAP,
        true,
        false,
        false,
        false,
        false,
        false,
        true,
        "minecraft.concept.registry_bootstrap",
        "Target-21",
        "Registry Bootstrap Boundary Analysis",
        "Move to Registry Bootstrap and Content Registration boundary analysis next; do not design a new SteelHook primitive until registry concept grounding adds more evidence.",
        findings());
  }

  private MinecraftResourceReloadArcDecisionAnalysis blockedAnalysis() {
    return new MinecraftResourceReloadArcDecisionAnalysis(
        1,
        "Target-20",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.data_resources_reload",
        "Target-16",
        "Target-17",
        "Target-18",
        "Target-19",
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
        false,
        false,
        true,
        "UPSTREAM_GATE_BLOCKED",
        false,
        "UPSTREAM_GATE_BLOCKED",
        false,
        false,
        false,
        "UPSTREAM_GATE_BLOCKED",
        true,
        true,
        true,
        false,
        "Target-20 requires passed Target-16, Target-17, Target-18, and Target-19 resource/reload analyses before recording the registry handoff decision.",
        MinecraftResourceReloadArcDecisionStatus.UPSTREAM_GATE_BLOCKED,
        MinecraftResourceReloadNextDirection.UNDECIDED_UPSTREAM_BLOCKED,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        "minecraft.concept.registry_bootstrap",
        "Target-21",
        "Registry Bootstrap Boundary Analysis",
        "Restore the Target-16 through Target-19 resource/reload analysis chain before recording a registry handoff decision.",
        findings());
  }

  private List<MinecraftResourceReloadArcDecisionFinding> findings() {
    return List.of(
        new MinecraftResourceReloadArcDecisionFinding(
            "target-20.resource.reload.arc.finding.001",
            "Target-16",
            "Resource/reload boundaries were named and anchored only to a coarse lifecycle boundary.",
            false,
            false,
            true,
            "The lifecycle anchor is not a reload hook."),
        new MinecraftResourceReloadArcDecisionFinding(
            "target-20.resource.reload.arc.finding.002",
            "Target-17",
            "Resource/reload metadata candidates were discovered without selecting a stable reload target.",
            false,
            false,
            true,
            "Candidate discovery does not imply reload readiness."),
        new MinecraftResourceReloadArcDecisionFinding(
            "target-20.resource.reload.arc.finding.003",
            "Target-18",
            "Resource/reload binding and access requirements were classified without recommending a reload proof.",
            false,
            false,
            true,
            "Classified requirements are not SteelHook primitive design."),
        new MinecraftResourceReloadArcDecisionFinding(
            "target-20.resource.reload.arc.finding.004",
            "Target-19",
            "Runtime reload timing, runtime resource visibility, and future offline data generation were separated.",
            false,
            false,
            true,
            "Runtime resource visibility is not an API, and offline data generation is not implemented."),
        new MinecraftResourceReloadArcDecisionFinding(
            "target-20.resource.reload.arc.finding.005",
            "Target-20",
            "The next target concept direction is Registry Bootstrap and Content Registration.",
            false,
            true,
            true,
            "Move to registry boundary analysis next instead of designing a new SteelHook primitive now."));
  }
}
