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

class MinecraftResourceVisibilityGenerationAnalysisWriterTest {
  @TempDir Path tempDirectory;

  private final MinecraftResourceVisibilityGenerationAnalysisWriter writer =
      new MinecraftResourceVisibilityGenerationAnalysisWriter();

  @Test
  void deterministicJsonOutputAcrossTwoOutputPaths() throws Exception {
    MinecraftResourceVisibilityGenerationAnalysis analysis = classifiedAnalysis();
    Path first = tempDirectory.resolve("a/minecraft-resource-visibility-generation-analysis.json");
    Path second = tempDirectory.resolve("b/minecraft-resource-visibility-generation-analysis.json");

    writer.write(first, analysis);
    writer.write(second, analysis);

    String firstJson = Files.readString(first, StandardCharsets.UTF_8);
    String secondJson = Files.readString(second, StandardCharsets.UTF_8);

    assertEquals(firstJson, secondJson);
    assertTrue(firstJson.contains("\"schema\": 1"));
    assertTrue(firstJson.contains("\"milestoneName\": \"Target-19\""));
    assertTrue(firstJson.contains("\"sourceResourceReloadAnalysisMilestone\": \"Target-16\""));
    assertTrue(
        firstJson.contains("\"sourceResourceReloadBindingAnalysisMilestone\": \"Target-18\""));
  }

  @Test
  void serializesStatusLaneSurfaceOrderNullsAndAggregateCounts() {
    String classifiedJson = writer.toJson(classifiedAnalysis()).toString();
    String blockedJson = writer.toJson(blockedAnalysis()).toString();

    assertTrue(classifiedJson.contains("\"separationStatus\":\"SEPARATION_CLASSIFIED\""));
    assertTrue(blockedJson.contains("\"separationStatus\":\"UPSTREAM_GATE_BLOCKED\""));
    assertTrue(classifiedJson.contains("\"lane\":\"SERVER_LIFECYCLE_ANCHOR\""));
    assertTrue(classifiedJson.contains("\"runtimeFacingSurfaceCount\":5"));
    assertTrue(classifiedJson.contains("\"offlineGenerationSurfaceCount\":1"));
    assertTrue(classifiedJson.contains("\"gateFailureReason\":null"));
    assertTrue(blockedJson.contains("\"gateFailureReason\":"));

    int lifecycle =
        classifiedJson.indexOf("\"boundaryId\":\"minecraft.resources.lifecycle_anchor\"");
    int discovery =
        classifiedJson.indexOf("\"boundaryId\":\"minecraft.resources.reload.discovery\"");
    int window = classifiedJson.indexOf("\"boundaryId\":\"minecraft.resources.reload.window\"");
    int apply = classifiedJson.indexOf("\"boundaryId\":\"minecraft.resources.reload.apply\"");
    int datapack = classifiedJson.indexOf("\"boundaryId\":\"minecraft.resources.datapack.view\"");
    int resourceManager =
        classifiedJson.indexOf("\"boundaryId\":\"minecraft.resources.resource_manager.view\"");
    int generation =
        classifiedJson.indexOf("\"boundaryId\":\"minecraft.resources.future_data_generation\"");

    assertTrue(lifecycle < discovery);
    assertTrue(discovery < window);
    assertTrue(window < apply);
    assertTrue(apply < datapack);
    assertTrue(datapack < resourceManager);
    assertTrue(resourceManager < generation);
  }

  @Test
  void doesNotSerializeSensitivePayloads() {
    String json = writer.toJson(classifiedAnalysis()).toString();

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

  private MinecraftResourceVisibilityGenerationAnalysis classifiedAnalysis() {
    return new MinecraftResourceVisibilityGenerationAnalysis(
        1,
        "Target-19",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.data_resources_reload",
        "Target-16",
        "Target-18",
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
        "BINDING_REQUIREMENTS_CLASSIFIED",
        false,
        false,
        true,
        null,
        MinecraftResourceVisibilityGenerationStatus.SEPARATION_CLASSIFIED,
        5,
        1,
        2,
        2,
        1,
        0,
        4,
        true,
        true,
        true,
        false,
        false,
        "Keep runtime resource visibility separate from future offline data generation; use this separation in the Target-20 caboose or registry/content planning.",
        surfaces(MinecraftResourceReloadBoundaryStatus.AVAILABLE));
  }

  private MinecraftResourceVisibilityGenerationAnalysis blockedAnalysis() {
    return new MinecraftResourceVisibilityGenerationAnalysis(
        1,
        "Target-19",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.data_resources_reload",
        "Target-16",
        "Target-18",
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
        "UPSTREAM_GATE_BLOCKED",
        false,
        false,
        false,
        "Target-19 requires passed Target-16 resource/reload analysis and passed Target-18 binding analysis before separating runtime visibility from future data generation.",
        MinecraftResourceVisibilityGenerationStatus.UPSTREAM_GATE_BLOCKED,
        5,
        1,
        2,
        2,
        1,
        0,
        4,
        true,
        true,
        true,
        false,
        false,
        "Restore Target-16 and Target-18 upstream gates before using resource visibility/data generation separation.",
        surfaces(MinecraftResourceReloadBoundaryStatus.UPSTREAM_GATE_BLOCKED));
  }

  private List<MinecraftResourceVisibilityGenerationSurface> surfaces(
      MinecraftResourceReloadBoundaryStatus anchorStatus) {
    return List.of(
        new MinecraftResourceVisibilityGenerationSurface(
            "minecraft.resources.lifecycle_anchor",
            "Lifecycle Anchor",
            1,
            MinecraftResourceVisibilityGenerationLane.SERVER_LIFECYCLE_ANCHOR,
            true,
            false,
            anchorStatus == MinecraftResourceReloadBoundaryStatus.AVAILABLE,
            false,
            false,
            false,
            false,
            false,
            false,
            anchorStatus,
            "Coarse server lifecycle anchor only; not a reload hook, resource view, or data generation surface."),
        new MinecraftResourceVisibilityGenerationSurface(
            "minecraft.resources.reload.discovery",
            "Reload Discovery",
            2,
            MinecraftResourceVisibilityGenerationLane.SYMBOL_DISCOVERY,
            false,
            false,
            false,
            false,
            false,
            false,
            true,
            false,
            false,
            MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND,
            "Metadata discovery lane only; not runtime resource visibility and not data generation."),
        new MinecraftResourceVisibilityGenerationSurface(
            "minecraft.resources.reload.window",
            "Reload Window",
            3,
            MinecraftResourceVisibilityGenerationLane.RESOURCE_RELOAD_TIMING,
            true,
            false,
            false,
            true,
            false,
            false,
            true,
            true,
            false,
            MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND,
            "Runtime reload timing lane; Target-19 does not identify a reload window or install a hook."),
        new MinecraftResourceVisibilityGenerationSurface(
            "minecraft.resources.reload.apply",
            "Reload Apply",
            4,
            MinecraftResourceVisibilityGenerationLane.RESOURCE_RELOAD_TIMING,
            true,
            false,
            false,
            true,
            false,
            false,
            true,
            true,
            false,
            MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND,
            "Runtime reload apply lane; Target-19 does not decide apply semantics or mutate server state."),
        new MinecraftResourceVisibilityGenerationSurface(
            "minecraft.resources.datapack.view",
            "Datapack View",
            5,
            MinecraftResourceVisibilityGenerationLane.RUNTIME_RESOURCE_VISIBILITY,
            true,
            false,
            false,
            false,
            true,
            false,
            true,
            true,
            false,
            MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND,
            "Runtime datapack visibility lane; Target-19 does not access, expose, or mutate datapack state."),
        new MinecraftResourceVisibilityGenerationSurface(
            "minecraft.resources.resource_manager.view",
            "Resource Manager View",
            6,
            MinecraftResourceVisibilityGenerationLane.RUNTIME_RESOURCE_VISIBILITY,
            true,
            false,
            false,
            false,
            true,
            false,
            true,
            true,
            false,
            MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND,
            "Runtime resource manager visibility lane; Target-19 does not access, expose, or mutate resource manager state."),
        new MinecraftResourceVisibilityGenerationSurface(
            "minecraft.resources.future_data_generation",
            "Future Data Generation",
            7,
            MinecraftResourceVisibilityGenerationLane.OFFLINE_DATA_GENERATION,
            false,
            true,
            false,
            false,
            false,
            true,
            false,
            false,
            false,
            MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND,
            "Future offline data generation lane; intentionally separate from runtime reload, runtime resource access, and registry mutation."));
  }
}
