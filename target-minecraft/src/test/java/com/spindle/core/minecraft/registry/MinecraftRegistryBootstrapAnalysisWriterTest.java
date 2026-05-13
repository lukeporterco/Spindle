package com.spindle.core.minecraft.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftSide;
import com.spindle.core.minecraft.concept.MinecraftTargetConceptCatalog;
import com.spindle.core.minecraft.interpret.MinecraftArtifactInterpretation;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedClass;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedField;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedJar;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedMethod;
import com.spindle.core.minecraft.resource.MinecraftResourceReloadArcDecisionAnalysis;
import com.spindle.core.minecraft.resource.MinecraftResourceReloadArcDecisionFinding;
import com.spindle.core.minecraft.resource.MinecraftResourceReloadArcDecisionStatus;
import com.spindle.core.minecraft.resource.MinecraftResourceReloadNextDirection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinecraftRegistryBootstrapAnalysisWriterTest {
  @TempDir Path tempDirectory;

  private final MinecraftRegistryBootstrapAnalysisWriter writer =
      new MinecraftRegistryBootstrapAnalysisWriter();

  @Test
  void deterministicJsonOutputAcrossTwoOutputPaths() throws Exception {
    MinecraftRegistryBootstrapAnalysis analysis = sampleAnalysis();
    Path first = tempDirectory.resolve("a/minecraft-registry-bootstrap-analysis.json");
    Path second = tempDirectory.resolve("b/minecraft-registry-bootstrap-analysis.json");

    writer.write(first, analysis);
    writer.write(second, analysis);

    assertEquals(
        Files.readString(first, StandardCharsets.UTF_8),
        Files.readString(second, StandardCharsets.UTF_8));
  }

  @Test
  void serializesGateStatusNullsBoundariesCandidatesAndTokens() {
    String json = writer.toJson(sampleAnalysis()).toString();

    assertTrue(json.contains("\"gatePassed\":true"));
    assertTrue(json.contains("\"gateFailureReason\":null"));
    assertTrue(json.contains("\"discoveryStatus\":\"CANDIDATES_DISCOVERED\""));
    assertTrue(json.contains("\"bindingStatus\":\"BINDING_REQUIREMENTS_CLASSIFIED\""));
    assertTrue(json.contains("\"discoveryTokens\":[\"registry\""));
    assertTrue(
        json.contains("\"boundaryId\":\"minecraft.registries.resource_reload_arc_handoff\""));
    assertTrue(json.contains("\"memberName\":null"));
    assertTrue(json.contains("\"descriptor\":null"));
    assertTrue(json.contains("\"id\":\"target-21.minecraft.registries.candidate.001\""));
  }

  @Test
  void doesNotSerializeSensitivePayloads() {
    String json = writer.toJson(sampleAnalysis()).toString();

    assertFalse(json.contains("bytes"));
    assertFalse(json.contains("bytecode"));
    assertFalse(json.contains("file_contents"));
    assertFalse(json.contains("resource_contents"));
    assertFalse(json.contains("datapack_contents"));
    assertFalse(json.contains("generated_file_contents"));
    assertFalse(json.contains("registry_contents"));
    assertFalse(json.contains("mappings"));
    assertFalse(json.contains("decompiled_source"));
  }

  @Test
  void serializesCandidatesInDeterministicOrder() {
    String json = writer.toJson(sampleAnalysis()).toString();

    int first = json.indexOf("\"id\":\"target-21.minecraft.registries.candidate.001\"");
    int second = json.indexOf("\"id\":\"target-21.minecraft.registries.candidate.002\"");
    assertTrue(first < second);
  }

  private MinecraftRegistryBootstrapAnalysis sampleAnalysis() {
    return new MinecraftRegistryBootstrapAnalyzer()
        .analyze(new MinecraftTargetConceptCatalog(), interpretation(), target20());
  }

  private MinecraftArtifactInterpretation interpretation() {
    MinecraftInterpretedClass rejectedClass =
        interpretedClass("com/example/BuiltinRegistries", List.of(), List.of());
    MinecraftInterpretedClass selectableClass =
        interpretedClass(
            "net/minecraft/core/registries/BuiltInRegistries",
            List.of(
                new MinecraftInterpretedField(
                    "value", "Ljava/lang/Object;", 8, List.of("PUBLIC", "STATIC"))),
            List.of(
                new MinecraftInterpretedMethod(
                    "bootstrapContext", "()V", 9, List.of("PUBLIC", "STATIC"), false, true)));
    return new MinecraftArtifactInterpretation(
        1,
        "Target-1",
        "minecraft",
        "26.1.2",
        "server",
        true,
        false,
        false,
        false,
        false,
        false,
        "DRY_RUN",
        List.of(
            new MinecraftInterpretedJar(
                "server.jar",
                "MINECRAFT",
                "fixture",
                "sha",
                2,
                1,
                1,
                0,
                List.of(),
                List.of(rejectedClass, selectableClass))),
        0,
        2,
        1,
        1,
        0,
        List.of(),
        List.of());
  }

  private MinecraftInterpretedClass interpretedClass(
      String internalName,
      List<MinecraftInterpretedField> fields,
      List<MinecraftInterpretedMethod> methods) {
    return new MinecraftInterpretedClass(
        internalName.replace('/', '.'),
        internalName,
        internalName.contains("/") ? internalName.substring(0, internalName.lastIndexOf('/')) : "",
        "java/lang/Object",
        List.of(),
        1,
        List.of("PUBLIC"),
        fields,
        methods);
  }

  private MinecraftResourceReloadArcDecisionAnalysis target20() {
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
        "Next.",
        List.of(
            new MinecraftResourceReloadArcDecisionFinding(
                "target-20.resource.reload.arc.finding.001",
                "Target-20",
                "Registry bootstrap is next.",
                false,
                false,
                true,
                "Notes.")));
  }
}
