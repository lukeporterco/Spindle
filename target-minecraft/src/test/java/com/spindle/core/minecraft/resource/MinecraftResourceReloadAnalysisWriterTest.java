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

class MinecraftResourceReloadAnalysisWriterTest {
  @TempDir Path tempDirectory;

  private final MinecraftResourceReloadAnalysisWriter writer =
      new MinecraftResourceReloadAnalysisWriter();

  @Test
  void writerSerializesDeterministicallyAcrossOutputPaths() throws Exception {
    MinecraftResourceReloadAnalysis analysis = analysis();
    Path first = tempDirectory.resolve("a/minecraft-resource-reload-analysis.json");
    Path second = tempDirectory.resolve("b/minecraft-resource-reload-analysis.json");

    writer.write(first, analysis);
    writer.write(second, analysis);

    String firstJson = Files.readString(first, StandardCharsets.UTF_8);
    String secondJson = Files.readString(second, StandardCharsets.UTF_8);

    assertEquals(firstJson, secondJson);
    assertTrue(firstJson.contains("\"schema\": 1"));
    assertTrue(firstJson.contains("\"milestoneName\": \"Target-16\""));
    assertTrue(firstJson.contains("\"sourceServerLifecycleDispatchPlanMilestone\": \"Target-12\""));
  }

  @Test
  void writerSerializesAllSevenBoundaryIdsInOrder() {
    String json = writer.toJson(analysis()).toString();

    int lifecycle = json.indexOf("\"boundaryId\":\"minecraft.resources.lifecycle_anchor\"");
    int discovery = json.indexOf("\"boundaryId\":\"minecraft.resources.reload.discovery\"");
    int window = json.indexOf("\"boundaryId\":\"minecraft.resources.reload.window\"");
    int apply = json.indexOf("\"boundaryId\":\"minecraft.resources.reload.apply\"");
    int datapack = json.indexOf("\"boundaryId\":\"minecraft.resources.datapack.view\"");
    int resourceManager =
        json.indexOf("\"boundaryId\":\"minecraft.resources.resource_manager.view\"");
    int generation = json.indexOf("\"boundaryId\":\"minecraft.resources.future_data_generation\"");

    assertTrue(lifecycle < discovery);
    assertTrue(discovery < window);
    assertTrue(window < apply);
    assertTrue(apply < datapack);
    assertTrue(datapack < resourceManager);
    assertTrue(resourceManager < generation);
  }

  @Test
  void writerSerializesNullsAndStatuses() {
    String blockedJson = writer.toJson(blockedAnalysis()).toString();
    String availableJson = writer.toJson(analysis()).toString();

    assertTrue(blockedJson.contains("\"gateFailureReason\":"));
    assertTrue(blockedJson.contains("\"sourceLifecyclePhaseId\":null"));
    assertTrue(blockedJson.contains("\"sourceLifecycleDispatchId\":null"));
    assertTrue(blockedJson.contains("\"status\":\"UPSTREAM_GATE_BLOCKED\""));
    assertTrue(availableJson.contains("\"status\":\"AVAILABLE\""));
    assertTrue(availableJson.contains("\"status\":\"DECLARED_UNBOUND\""));
  }

  @Test
  void writerSerializesOfflineDataGenerationBoundary() {
    String json = writer.toJson(analysis()).toString();

    assertTrue(json.contains("\"boundaryId\":\"minecraft.resources.future_data_generation\""));
    assertTrue(json.contains("\"representationKind\":\"OFFLINE_DATA_GENERATION_BOUNDARY\""));
    assertTrue(json.contains("\"requiresOfflineGenerationDesign\":true"));
  }

  @Test
  void writerDoesNotSerializeByteArraysOrResourceLikeContents() {
    String json = writer.toJson(analysis()).toString();

    assertFalse(json.contains("bytes"));
    assertFalse(json.contains("bytecode"));
    assertFalse(json.contains("resource_contents"));
    assertFalse(json.contains("datapack_contents"));
    assertFalse(json.contains("generated_json_contents"));
    assertFalse(json.contains("registry_contents"));
    assertFalse(json.contains("command_tree_contents"));
  }

  private MinecraftResourceReloadAnalysis analysis() {
    return new MinecraftResourceReloadAnalysis(
        1,
        "Target-16",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.data_resources_reload",
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
        "Target-12",
        true,
        true,
        true,
        null,
        1,
        6,
        0,
        List.of(
            boundary(
                "minecraft.resources.lifecycle_anchor",
                "Lifecycle Anchor",
                1,
                MinecraftResourceReloadBoundaryStatus.AVAILABLE,
                MinecraftResourceReloadRepresentationKind.SERVER_LIFECYCLE_ANCHOR,
                true,
                "minecraft.server.lifecycle.starting",
                "target-12.minecraft.server.lifecycle.starting.dispatch",
                false,
                false,
                false,
                false,
                "Available only as a coarse lifecycle anchor; this is not a Minecraft resource reload hook."),
            boundary(
                "minecraft.resources.reload.discovery",
                "Reload Discovery",
                2,
                MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND,
                MinecraftResourceReloadRepresentationKind.RESOURCE_RELOAD_SYMBOL_BOUNDARY,
                false,
                null,
                null,
                true,
                false,
                false,
                false,
                "Declared for a future Target-17-style resource/reload symbol discovery pass."),
            boundary(
                "minecraft.resources.reload.window",
                "Reload Window",
                3,
                MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND,
                MinecraftResourceReloadRepresentationKind.RESOURCE_RELOAD_TIMING_BOUNDARY,
                false,
                null,
                null,
                true,
                true,
                false,
                false,
                "Declared for future reload timing analysis; no runtime reload window is exposed in Target-16."),
            boundary(
                "minecraft.resources.reload.apply",
                "Reload Apply",
                4,
                MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND,
                MinecraftResourceReloadRepresentationKind.RESOURCE_RELOAD_TIMING_BOUNDARY,
                false,
                null,
                null,
                true,
                true,
                false,
                false,
                "Declared for future reload-application analysis; no reload application behavior is implemented in Target-16."),
            boundary(
                "minecraft.resources.datapack.view",
                "Datapack View",
                5,
                MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND,
                MinecraftResourceReloadRepresentationKind.RUNTIME_RESOURCE_VIEW_BOUNDARY,
                false,
                null,
                null,
                true,
                true,
                true,
                false,
                "Declared as a future read/visibility boundary for datapack state, not mutation."),
            boundary(
                "minecraft.resources.resource_manager.view",
                "Resource Manager View",
                6,
                MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND,
                MinecraftResourceReloadRepresentationKind.RUNTIME_RESOURCE_VIEW_BOUNDARY,
                false,
                null,
                null,
                true,
                true,
                true,
                false,
                "Declared as a future read/visibility boundary for resource manager access, not mutation."),
            boundary(
                "minecraft.resources.future_data_generation",
                "Future Data Generation",
                7,
                MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND,
                MinecraftResourceReloadRepresentationKind.OFFLINE_DATA_GENERATION_BOUNDARY,
                false,
                null,
                null,
                false,
                false,
                false,
                true,
                "Declared as a future offline/generated-data concept, intentionally separate from runtime resource reload.")));
  }

  private MinecraftResourceReloadAnalysis blockedAnalysis() {
    return new MinecraftResourceReloadAnalysis(
        1,
        "Target-16",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.data_resources_reload",
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
        "Target-12",
        false,
        false,
        false,
        "Target-16 requires the Target-12 starting lifecycle dispatch anchor before resource/reload boundaries can be anchored.",
        0,
        6,
        1,
        List.of(
            boundary(
                "minecraft.resources.lifecycle_anchor",
                "Lifecycle Anchor",
                1,
                MinecraftResourceReloadBoundaryStatus.UPSTREAM_GATE_BLOCKED,
                MinecraftResourceReloadRepresentationKind.SERVER_LIFECYCLE_ANCHOR,
                false,
                null,
                null,
                false,
                false,
                false,
                false,
                "Target-16 requires the Target-12 starting lifecycle dispatch anchor before resource/reload boundaries can be anchored.")));
  }

  private MinecraftAnalyzedResourceReloadBoundary boundary(
      String boundaryId,
      String displayName,
      int order,
      MinecraftResourceReloadBoundaryStatus status,
      MinecraftResourceReloadRepresentationKind representationKind,
      boolean available,
      String sourceLifecyclePhaseId,
      String sourceLifecycleDispatchId,
      boolean requiresSymbolDiscovery,
      boolean requiresBindingStrategyAnalysis,
      boolean requiresRuntimeResourceAccess,
      boolean requiresOfflineGenerationDesign,
      String notes) {
    return new MinecraftAnalyzedResourceReloadBoundary(
        boundaryId,
        displayName,
        order,
        status,
        representationKind,
        available,
        sourceLifecyclePhaseId,
        sourceLifecycleDispatchId,
        requiresSymbolDiscovery,
        requiresBindingStrategyAnalysis,
        requiresRuntimeResourceAccess,
        requiresOfflineGenerationDesign,
        notes);
  }
}
