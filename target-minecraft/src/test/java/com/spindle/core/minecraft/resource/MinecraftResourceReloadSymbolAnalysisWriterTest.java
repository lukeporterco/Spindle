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

class MinecraftResourceReloadSymbolAnalysisWriterTest {
  @TempDir Path tempDirectory;

  private final MinecraftResourceReloadSymbolAnalysisWriter writer =
      new MinecraftResourceReloadSymbolAnalysisWriter();

  @Test
  void writerSerializesDeterministicJsonAcrossTwoOutputPaths() throws Exception {
    MinecraftResourceReloadSymbolAnalysis analysis = discoveredAnalysis();
    Path first = tempDirectory.resolve("a/minecraft-resource-reload-symbol-analysis.json");
    Path second = tempDirectory.resolve("b/minecraft-resource-reload-symbol-analysis.json");

    writer.write(first, analysis);
    writer.write(second, analysis);

    String firstJson = Files.readString(first, StandardCharsets.UTF_8);
    String secondJson = Files.readString(second, StandardCharsets.UTF_8);

    assertEquals(firstJson, secondJson);
    assertTrue(firstJson.contains("\"schema\": 1"));
    assertTrue(firstJson.contains("\"milestoneName\": \"Target-17\""));
    assertTrue(firstJson.contains("\"sourceResourceReloadAnalysisMilestone\": \"Target-16\""));
  }

  @Test
  void writerSerializesDiscoveryTokensCountsAndMatchedTokens() {
    String json = writer.toJson(discoveredAnalysis()).toString();

    assertTrue(json.contains("\"discoveryTokens\":[\"reload\",\"resource\""));
    assertTrue(json.contains("\"candidateCount\":1"));
    assertTrue(json.contains("\"selectableCandidateCount\":1"));
    assertTrue(json.contains("\"rejectedCandidateCount\":0"));
    assertTrue(
        json.contains(
            "\"matchedTokens\":[\"reload\",\"resource\",\"resources\",\"serverresources\"]"));
  }

  @Test
  void writerSerializesSelectableRejectedAndNullClassFields() {
    String discoveredJson = writer.toJson(discoveredAnalysis()).toString();
    String rejectedJson = writer.toJson(rejectedAnalysis()).toString();

    assertTrue(discoveredJson.contains("\"selectable\":true"));
    assertTrue(discoveredJson.contains("\"memberName\":null"));
    assertTrue(discoveredJson.contains("\"descriptor\":null"));
    assertTrue(rejectedJson.contains("\"selectable\":false"));
    assertTrue(
        rejectedJson.contains(
            "\"rejectionReason\":\"Only net/minecraft/* owners are selectable resource/reload symbol candidates in Target-17.\""));
  }

  @Test
  void writerSerializesEachDiscoveryStatus() {
    assertTrue(
        writer
            .toJson(noCandidatesAnalysis())
            .toString()
            .contains("\"discoveryStatus\":\"NO_CANDIDATES\""));
    assertTrue(
        writer
            .toJson(rejectedAnalysis())
            .toString()
            .contains("\"discoveryStatus\":\"ONLY_REJECTED_CANDIDATES\""));
    assertTrue(
        writer
            .toJson(discoveredAnalysis())
            .toString()
            .contains("\"discoveryStatus\":\"CANDIDATES_DISCOVERED\""));
    assertTrue(
        writer
            .toJson(blockedAnalysis())
            .toString()
            .contains("\"discoveryStatus\":\"UPSTREAM_GATE_BLOCKED\""));
  }

  @Test
  void writerDoesNotSerializeByteArraysBytecodeOrContentPayloads() {
    String json = writer.toJson(discoveredAnalysis()).toString();

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

  private MinecraftResourceReloadSymbolAnalysis discoveredAnalysis() {
    return new MinecraftResourceReloadSymbolAnalysis(
        1,
        "Target-17",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.data_resources_reload",
        "Target-16",
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
        true,
        true,
        true,
        null,
        List.of(
            "reload",
            "resource",
            "resources",
            "datapack",
            "data_pack",
            "packresources",
            "resourcemanager",
            "resource_manager",
            "preparablereloadlistener",
            "reloadableresourcemanager",
            "serverresources",
            "reloadinstance",
            "server/packs",
            "server/packs/resources"),
        1,
        1,
        0,
        0,
        0,
        0,
        1,
        0,
        MinecraftResourceReloadSymbolDiscoveryStatus.CANDIDATES_DISCOVERED,
        true,
        List.of(
            new MinecraftResourceReloadSymbolCandidate(
                "target-17.minecraft.resources.reload.candidate.001",
                MinecraftResourceReloadSymbolCandidateKind.CLASS_NAME_REFERENCE,
                "minecraft.resources.reload.discovery",
                "net/minecraft/server/ServerResources",
                null,
                null,
                false,
                List.of("PUBLIC"),
                List.of("reload", "resource", "resources", "serverresources"),
                true,
                null,
                "Class or package name matched resource/reload discovery tokens.")));
  }

  private MinecraftResourceReloadSymbolAnalysis noCandidatesAnalysis() {
    return new MinecraftResourceReloadSymbolAnalysis(
        1,
        "Target-17",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.data_resources_reload",
        "Target-16",
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
        true,
        true,
        true,
        null,
        List.of("reload"),
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        MinecraftResourceReloadSymbolDiscoveryStatus.NO_CANDIDATES,
        false,
        List.of());
  }

  private MinecraftResourceReloadSymbolAnalysis rejectedAnalysis() {
    return new MinecraftResourceReloadSymbolAnalysis(
        1,
        "Target-17",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.data_resources_reload",
        "Target-16",
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
        true,
        true,
        true,
        null,
        List.of("reload"),
        1,
        1,
        0,
        0,
        0,
        0,
        0,
        1,
        MinecraftResourceReloadSymbolDiscoveryStatus.ONLY_REJECTED_CANDIDATES,
        false,
        List.of(
            new MinecraftResourceReloadSymbolCandidate(
                "target-17.minecraft.resources.reload.candidate.001",
                MinecraftResourceReloadSymbolCandidateKind.CLASS_NAME_REFERENCE,
                "minecraft.resources.reload.discovery",
                "com/example/ReloadStuff",
                null,
                null,
                false,
                List.of("PUBLIC"),
                List.of("reload"),
                false,
                "Only net/minecraft/* owners are selectable resource/reload symbol candidates in Target-17.",
                "Class or package name matched resource/reload discovery tokens.")));
  }

  private MinecraftResourceReloadSymbolAnalysis blockedAnalysis() {
    return new MinecraftResourceReloadSymbolAnalysis(
        1,
        "Target-17",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.data_resources_reload",
        "Target-16",
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
        false,
        true,
        false,
        "Target-17 requires Target-16 resource/reload analysis with an available lifecycle anchor and declared reload discovery boundary.",
        List.of("reload"),
        1,
        1,
        0,
        0,
        0,
        0,
        1,
        0,
        MinecraftResourceReloadSymbolDiscoveryStatus.UPSTREAM_GATE_BLOCKED,
        false,
        List.of(
            new MinecraftResourceReloadSymbolCandidate(
                "target-17.minecraft.resources.reload.candidate.001",
                MinecraftResourceReloadSymbolCandidateKind.CLASS_NAME_REFERENCE,
                "minecraft.resources.reload.discovery",
                "net/minecraft/server/ServerResources",
                null,
                null,
                false,
                List.of("PUBLIC"),
                List.of("reload"),
                true,
                null,
                "Class or package name matched resource/reload discovery tokens.")));
  }
}
