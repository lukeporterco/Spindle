package com.spindle.core.minecraft.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftSide;
import com.spindle.core.minecraft.concept.MinecraftTargetConceptCatalog;
import com.spindle.core.minecraft.lifecycle.MinecraftPlannedServerLifecycleDispatch;
import com.spindle.core.minecraft.lifecycle.MinecraftServerLifecycleDispatchMode;
import com.spindle.core.minecraft.lifecycle.MinecraftServerLifecycleDispatchPlan;
import com.spindle.core.minecraft.lifecycle.MinecraftServerLifecycleDispatchStatus;
import java.util.List;
import org.junit.jupiter.api.Test;

class MinecraftResourceReloadAnalyzerTest {
  private final MinecraftResourceReloadAnalyzer analyzer = new MinecraftResourceReloadAnalyzer();
  private final MinecraftTargetConceptCatalog conceptCatalog = new MinecraftTargetConceptCatalog();

  @Test
  void startingLifecycleDispatchAvailableProducesAvailableLifecycleAnchorOnly() {
    MinecraftResourceReloadAnalysis analysis = analyzer.analyze(conceptCatalog, plan(true));

    assertTrue(analysis.sourceLifecycleGatePassed());
    assertTrue(analysis.sourceLifecycleStartingDispatchAvailable());
    assertTrue(analysis.gatePassed());
    assertNull(analysis.gateFailureReason());
    assertEquals(1, analysis.availableBoundaryCount());
    assertEquals(6, analysis.declaredUnboundBoundaryCount());
    assertEquals(0, analysis.upstreamBlockedBoundaryCount());

    MinecraftAnalyzedResourceReloadBoundary anchor = analysis.boundaries().getFirst();
    assertEquals("minecraft.resources.lifecycle_anchor", anchor.boundaryId());
    assertEquals(1, anchor.order());
    assertEquals(MinecraftResourceReloadBoundaryStatus.AVAILABLE, anchor.status());
    assertEquals(
        MinecraftResourceReloadRepresentationKind.SERVER_LIFECYCLE_ANCHOR,
        anchor.representationKind());
    assertTrue(anchor.available());
    assertEquals("minecraft.server.lifecycle.starting", anchor.sourceLifecyclePhaseId());
    assertEquals(
        "target-12.minecraft.server.lifecycle.starting.dispatch",
        anchor.sourceLifecycleDispatchId());
    assertFalse(anchor.requiresSymbolDiscovery());
    assertFalse(anchor.requiresBindingStrategyAnalysis());
    assertFalse(anchor.requiresRuntimeResourceAccess());
    assertFalse(anchor.requiresOfflineGenerationDesign());
  }

  @Test
  void blockedLifecycleGateMarksAnchorUpstreamBlocked() {
    MinecraftResourceReloadAnalysis analysis = analyzer.analyze(conceptCatalog, plan(false));

    assertFalse(analysis.sourceLifecycleGatePassed());
    assertFalse(analysis.sourceLifecycleStartingDispatchAvailable());
    assertFalse(analysis.gatePassed());
    assertEquals(
        "Target-16 requires the Target-12 starting lifecycle dispatch anchor before resource/reload boundaries can be anchored.",
        analysis.gateFailureReason());
    assertEquals(0, analysis.availableBoundaryCount());
    assertEquals(6, analysis.declaredUnboundBoundaryCount());
    assertEquals(1, analysis.upstreamBlockedBoundaryCount());
    assertEquals(
        MinecraftResourceReloadBoundaryStatus.UPSTREAM_GATE_BLOCKED,
        analysis.boundaries().getFirst().status());
  }

  @Test
  void missingStartingDispatchMarksAnchorUpstreamBlocked() {
    MinecraftServerLifecycleDispatchPlan basePlan = plan(true);
    MinecraftServerLifecycleDispatchPlan missingStartingDispatchPlan =
        new MinecraftServerLifecycleDispatchPlan(
            basePlan.schema(),
            basePlan.milestoneName(),
            basePlan.target(),
            basePlan.minecraftVersion(),
            basePlan.side(),
            basePlan.conceptId(),
            basePlan.sourceBindingReportMilestone(),
            basePlan.analysisOnly(),
            basePlan.classLoadingOccurred(),
            basePlan.injectionOccurred(),
            basePlan.transformationOccurred(),
            basePlan.patchingOccurred(),
            basePlan.hookInstallationOccurred(),
            basePlan.runtimeDispatchOccurred(),
            basePlan.publicApiExposed(),
            basePlan.javaModExecutionSandboxed(),
            basePlan.sourceBindingGatePassed(),
            basePlan.gatePassed(),
            basePlan.gateFailureReason(),
            basePlan.lifecyclePhaseCount(),
            basePlan.dispatchCount() - 1,
            basePlan.plannedDispatchCount() - 1,
            basePlan.blockedDispatchCount(),
            basePlan.unsupportedDispatchCount(),
            basePlan.dispatches().subList(1, basePlan.dispatches().size()));

    MinecraftResourceReloadAnalysis analysis =
        analyzer.analyze(conceptCatalog, missingStartingDispatchPlan);

    assertFalse(analysis.gatePassed());
    assertFalse(analysis.sourceLifecycleStartingDispatchAvailable());
    assertEquals(
        MinecraftResourceReloadBoundaryStatus.UPSTREAM_GATE_BLOCKED,
        analysis.boundaries().getFirst().status());
    assertEquals(0, analysis.availableBoundaryCount());
    assertEquals(6, analysis.declaredUnboundBoundaryCount());
    assertEquals(1, analysis.upstreamBlockedBoundaryCount());
  }

  @Test
  void boundaryOrderIsDeterministic() {
    MinecraftResourceReloadAnalysis analysis = analyzer.analyze(conceptCatalog, plan(true));

    assertEquals(
        List.of(
            "minecraft.resources.lifecycle_anchor",
            "minecraft.resources.reload.discovery",
            "minecraft.resources.reload.window",
            "minecraft.resources.reload.apply",
            "minecraft.resources.datapack.view",
            "minecraft.resources.resource_manager.view",
            "minecraft.resources.future_data_generation"),
        analysis.boundaries().stream()
            .map(MinecraftAnalyzedResourceReloadBoundary::boundaryId)
            .toList());
    assertEquals(
        List.of(1, 2, 3, 4, 5, 6, 7),
        analysis.boundaries().stream()
            .map(MinecraftAnalyzedResourceReloadBoundary::order)
            .toList());
  }

  @Test
  void futureDataGenerationRequiresOfflineDesignOnly() {
    MinecraftAnalyzedResourceReloadBoundary boundary =
        analyzer.analyze(conceptCatalog, plan(true)).boundaries().get(6);

    assertEquals("minecraft.resources.future_data_generation", boundary.boundaryId());
    assertFalse(boundary.available());
    assertFalse(boundary.requiresSymbolDiscovery());
    assertFalse(boundary.requiresBindingStrategyAnalysis());
    assertFalse(boundary.requiresRuntimeResourceAccess());
    assertTrue(boundary.requiresOfflineGenerationDesign());
    assertEquals(MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND, boundary.status());
  }

  @Test
  void datapackAndResourceManagerViewsRequireRuntimeResourceAccessButRemainUnavailable() {
    MinecraftResourceReloadAnalysis analysis = analyzer.analyze(conceptCatalog, plan(true));

    MinecraftAnalyzedResourceReloadBoundary datapackView = analysis.boundaries().get(4);
    MinecraftAnalyzedResourceReloadBoundary resourceManagerView = analysis.boundaries().get(5);

    assertFalse(datapackView.available());
    assertTrue(datapackView.requiresRuntimeResourceAccess());
    assertEquals(MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND, datapackView.status());

    assertFalse(resourceManagerView.available());
    assertTrue(resourceManagerView.requiresRuntimeResourceAccess());
    assertEquals(
        MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND, resourceManagerView.status());
  }

  @Test
  void analysisFlagsRemainMutationFreeRuntimeFreeAndApiFree() {
    MinecraftResourceReloadAnalysis analysis = analyzer.analyze(conceptCatalog, plan(true));

    assertEquals(1, analysis.schema());
    assertEquals("Target-16", analysis.milestoneName());
    assertEquals("minecraft.concept.data_resources_reload", analysis.conceptId());
    assertTrue(analysis.analysisOnly());
    assertFalse(analysis.classLoadingOccurred());
    assertFalse(analysis.injectionOccurred());
    assertFalse(analysis.transformationOccurred());
    assertFalse(analysis.patchingOccurred());
    assertFalse(analysis.hookInstallationOccurred());
    assertFalse(analysis.runtimeDispatchOccurred());
    assertFalse(analysis.resourceReloadOccurred());
    assertFalse(analysis.resourceAccessOccurred());
    assertFalse(analysis.datapackAccessOccurred());
    assertFalse(analysis.dataGenerationOccurred());
    assertFalse(analysis.registryMutationOccurred());
    assertFalse(analysis.publicApiExposed());
    assertFalse(analysis.javaModExecutionSandboxed());
  }

  private MinecraftServerLifecycleDispatchPlan plan(boolean gatePassed) {
    return new MinecraftServerLifecycleDispatchPlan(
        1,
        "Target-12",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.server_lifecycle",
        "Target-11",
        true,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        gatePassed,
        gatePassed,
        gatePassed
            ? null
            : "Target-16 requires the Target-12 starting lifecycle dispatch anchor before resource/reload boundaries can be anchored.",
        6,
        6,
        gatePassed ? 1 : 0,
        gatePassed ? 0 : 1,
        5,
        List.of(
            new MinecraftPlannedServerLifecycleDispatch(
                "target-12.minecraft.server.lifecycle.starting.dispatch",
                "minecraft.server.lifecycle.starting",
                "Starting",
                "target-11.minecraft.server.lifecycle.starting",
                "minecraft.26_1_2.server.main.entrypoint",
                gatePassed
                    ? MinecraftServerLifecycleDispatchStatus.PLANNED
                    : MinecraftServerLifecycleDispatchStatus.BLOCKED,
                gatePassed
                    ? MinecraftServerLifecycleDispatchMode.INTERNAL_STATIC_DISPATCH_SYMBOLIC
                    : MinecraftServerLifecycleDispatchMode.NONE,
                gatePassed ? "BEFORE_MINECRAFT_SERVER_MAIN" : null,
                gatePassed
                    ? "com/spindle/core/minecraft/lifecycle/runtime/MinecraftServerLifecycleDispatcher"
                    : null,
                gatePassed ? "beforeMinecraftServerMain" : null,
                gatePassed ? "()V" : null,
                false,
                false,
                false,
                false,
                false,
                true,
                gatePassed
                    ? "Symbolic internal static dispatch planned before the Minecraft dedicated server main entrypoint."
                    : "Target-16 requires the Target-12 starting lifecycle dispatch anchor before resource/reload boundaries can be anchored."),
            futureDispatch("started", "Started"),
            futureDispatch("stopping", "Stopping"),
            futureDispatch("stopped", "Stopped"),
            futureDispatch("crashed", "Crashed"),
            futureDispatch("reload_requested", "Reload Requested")));
  }

  private MinecraftPlannedServerLifecycleDispatch futureDispatch(
      String phaseSuffix, String displayName) {
    return new MinecraftPlannedServerLifecycleDispatch(
        "target-12.minecraft.server.lifecycle." + phaseSuffix + ".dispatch",
        "minecraft.server.lifecycle." + phaseSuffix,
        displayName,
        "target-11.minecraft.server.lifecycle." + phaseSuffix,
        null,
        MinecraftServerLifecycleDispatchStatus.DECLARED_UNSUPPORTED,
        MinecraftServerLifecycleDispatchMode.NONE,
        null,
        null,
        null,
        null,
        false,
        false,
        false,
        false,
        false,
        true,
        "Future.");
  }
}
