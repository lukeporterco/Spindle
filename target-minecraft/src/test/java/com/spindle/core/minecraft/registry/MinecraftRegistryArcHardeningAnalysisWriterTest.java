package com.spindle.core.minecraft.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftSide;
import com.spindle.core.minecraft.resource.MinecraftResourceReloadNextDirection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinecraftRegistryArcHardeningAnalysisWriterTest {
  @TempDir Path tempDirectory;

  private final MinecraftRegistryArcHardeningAnalysisWriter writer =
      new MinecraftRegistryArcHardeningAnalysisWriter();

  @Test
  void deterministicJsonOutputAcrossTwoOutputPaths() throws Exception {
    MinecraftRegistryArcHardeningAnalysis analysis = sampleAnalysis();
    Path first = tempDirectory.resolve("a/minecraft-registry-arc-hardening.json");
    Path second = tempDirectory.resolve("b/minecraft-registry-arc-hardening.json");

    writer.write(first, analysis);
    writer.write(second, analysis);

    assertEquals(
        Files.readString(first, StandardCharsets.UTF_8),
        Files.readString(second, StandardCharsets.UTF_8));
  }

  @Test
  void serializesStatusesNextDirectionSourceCountsAndFindings() {
    String json = writer.toJson(sampleAnalysis()).toString();

    assertTrue(json.contains("\"hardeningStatus\":\"REGISTRY_ARC_HARDENED_FOR_STEELHOOK_0_2\""));
    assertTrue(json.contains("\"nextDirection\":\"MOVE_TO_STEELHOOK_0_2_PRIMITIVE_DESIGN\""));
    assertTrue(json.contains("\"sourceRegistryCandidateCount\":3"));
    assertTrue(json.contains("\"sourceRegistrySelectableCandidateCount\":2"));
    assertTrue(json.contains("\"sourceRegistryFutureSteelHookPrimitiveRequiredCount\":1"));
    assertTrue(json.contains("\"sourceRegistryDiscoveryStatus\":\"CANDIDATES_DISCOVERED\""));
    assertTrue(
        json.contains(
            "\"sourceResourceReloadArcDecisionNextDirection\":\"MOVE_TO_REGISTRY_BOOTSTRAP\""));
    assertTrue(json.contains("\"id\":\"target-22.registry.arc.finding.001\""));
  }

  @Test
  void serializesNullGateFailureReason() {
    String json = writer.toJson(sampleAnalysis()).toString();

    assertTrue(json.contains("\"gateFailureReason\":null"));
  }

  @Test
  void doesNotSerializeTarget21CandidatePayloads() {
    String json = writer.toJson(sampleAnalysis()).toString();

    assertFalse(json.contains("\"candidates\""));
    assertFalse(json.contains("\"boundaries\""));
    assertFalse(json.contains("\"ownerInternalName\""));
    assertFalse(json.contains("\"descriptor\""));
    assertFalse(json.contains("\"bytecode\""));
    assertFalse(json.contains("\"registry_contents\""));
  }

  private MinecraftRegistryArcHardeningAnalysis sampleAnalysis() {
    return new MinecraftRegistryArcHardeningAnalysis(
        1,
        "Target-22",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.registry_bootstrap",
        "Target-20",
        "Target-21",
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
        true,
        true,
        MinecraftResourceReloadNextDirection.MOVE_TO_REGISTRY_BOOTSTRAP,
        true,
        MinecraftRegistryDiscoveryStatus.CANDIDATES_DISCOVERED,
        MinecraftRegistryBindingStatus.BINDING_REQUIREMENTS_CLASSIFIED,
        3,
        2,
        1,
        1,
        true,
        null,
        MinecraftRegistryArcHardeningStatus.REGISTRY_ARC_HARDENED_FOR_STEELHOOK_0_2,
        MinecraftRegistryArcNextDirection.MOVE_TO_STEELHOOK_0_2_PRIMITIVE_DESIGN,
        true,
        false,
        false,
        false,
        true,
        false,
        0,
        0,
        2,
        "Move next to SteelHook 0.2 primitive design; do not implement registry behavior until the primitive design pass defines bounded value capture and mutation semantics.",
        List.of(
            new MinecraftRegistryArcHardeningFinding(
                "target-22.registry.arc.finding.001",
                "Target-20",
                "Target-20 handoff points to registry bootstrap.",
                MinecraftRegistryArcHardeningFindingStatus.PASS,
                true,
                "Target-20 preserves the registry bootstrap handoff for Target-21.",
                "Notes."),
            new MinecraftRegistryArcHardeningFinding(
                "target-22.registry.arc.finding.002",
                "Target-21",
                "Target-21 analysisOnly is true.",
                MinecraftRegistryArcHardeningFindingStatus.PASS,
                true,
                "Target-21 remains analysis-only.",
                "Notes.")));
  }
}
