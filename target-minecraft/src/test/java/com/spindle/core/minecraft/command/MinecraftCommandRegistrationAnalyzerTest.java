package com.spindle.core.minecraft.command;

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

class MinecraftCommandRegistrationAnalyzerTest {
  private final MinecraftCommandRegistrationAnalyzer analyzer =
      new MinecraftCommandRegistrationAnalyzer();
  private final MinecraftTargetConceptCatalog conceptCatalog = new MinecraftTargetConceptCatalog();

  @Test
  void plannedLifecycleDispatchMakesLifecycleAnchorAvailable() {
    MinecraftCommandRegistrationAnalysis analysis = analyzer.analyze(conceptCatalog, plan(true));

    assertTrue(analysis.sourceLifecycleDispatchGatePassed());
    assertTrue(analysis.gatePassed());
    assertEquals(1, analysis.anchoredBoundaryCount());
    assertEquals(4, analysis.unboundBoundaryCount());
    assertEquals(0, analysis.blockedBoundaryCount());

    MinecraftAnalyzedCommandRegistrationBoundary anchor = analysis.boundaries().getFirst();
    assertEquals("target-13.minecraft.commands.lifecycle_anchor", anchor.id());
    assertEquals("minecraft.commands.lifecycle_anchor", anchor.boundaryId());
    assertEquals(MinecraftCommandRegistrationBoundaryStatus.ANCHOR_AVAILABLE, anchor.status());
    assertEquals(
        MinecraftCommandRegistrationRepresentationKind.UPSTREAM_LIFECYCLE_DISPATCH,
        anchor.representationKind());
    assertEquals("minecraft.concept.server_lifecycle", anchor.upstreamConceptId());
    assertEquals(
        "target-12.minecraft.server.lifecycle.starting.dispatch", anchor.upstreamDispatchId());
    assertEquals("minecraft.server.lifecycle.starting", anchor.sourceLifecyclePhaseId());
    assertFalse(anchor.minecraftSymbolKnown());
    assertFalse(anchor.requiresFutureMinecraftSymbol());
    assertFalse(anchor.requiresFutureSteelHookPrimitive());
    assertFalse(anchor.implementedInThisPass());
    assertTrue(anchor.analysisOnly());
    assertNull(analysis.gateFailureReason());
  }

  @Test
  void failedLifecycleDispatchGateBlocksLifecycleAnchor() {
    MinecraftCommandRegistrationAnalysis analysis = analyzer.analyze(conceptCatalog, plan(false));

    assertFalse(analysis.sourceLifecycleDispatchGatePassed());
    assertFalse(analysis.gatePassed());
    assertEquals(0, analysis.anchoredBoundaryCount());
    assertEquals(4, analysis.unboundBoundaryCount());
    assertEquals(1, analysis.blockedBoundaryCount());
    assertEquals(
        "Target-13 requires a planned Target-12 starting lifecycle dispatch.",
        analysis.gateFailureReason());
    assertEquals(
        MinecraftCommandRegistrationBoundaryStatus.BLOCKED,
        analysis.boundaries().getFirst().status());
  }

  @Test
  void allFiveCommandBoundariesAreAlwaysReported() {
    MinecraftCommandRegistrationAnalysis analysis = analyzer.analyze(conceptCatalog, plan(true));

    assertEquals(5, analysis.boundaryCount());
    assertEquals(
        List.of(
            "target-13.minecraft.commands.lifecycle_anchor",
            "target-13.minecraft.commands.dispatcher.discovery",
            "target-13.minecraft.commands.registration.window",
            "target-13.minecraft.commands.registration.apply",
            "target-13.minecraft.commands.reload.reapply"),
        analysis.boundaries().stream()
            .map(MinecraftAnalyzedCommandRegistrationBoundary::id)
            .toList());
  }

  @Test
  void futureCommandBoundariesRemainDeclaredUnbound() {
    MinecraftCommandRegistrationAnalysis analysis = analyzer.analyze(conceptCatalog, plan(true));

    List<MinecraftAnalyzedCommandRegistrationBoundary> futureBoundaries =
        analysis.boundaries().subList(1, 5);
    for (MinecraftAnalyzedCommandRegistrationBoundary boundary : futureBoundaries) {
      assertEquals(MinecraftCommandRegistrationBoundaryStatus.DECLARED_UNBOUND, boundary.status());
      assertFalse(boundary.minecraftSymbolKnown());
      assertNull(boundary.ownerInternalName());
      assertNull(boundary.memberName());
      assertNull(boundary.descriptor());
      assertTrue(boundary.requiresFutureMinecraftSymbol());
      assertFalse(boundary.requiresFutureSteelHookPrimitive());
      assertFalse(boundary.implementedInThisPass());
      assertTrue(boundary.analysisOnly());
      assertTrue(
          boundary
              .notes()
              .contains("No Minecraft command dispatcher symbol is known in this pass"));
    }
  }

  @Test
  void analysisFlagsRemainMutationFreeAndCommandFree() {
    MinecraftCommandRegistrationAnalysis analysis = analyzer.analyze(conceptCatalog, plan(true));

    assertEquals(1, analysis.schema());
    assertEquals("Target-13", analysis.milestoneName());
    assertTrue(analysis.analysisOnly());
    assertFalse(analysis.classLoadingOccurred());
    assertFalse(analysis.injectionOccurred());
    assertFalse(analysis.transformationOccurred());
    assertFalse(analysis.patchingOccurred());
    assertFalse(analysis.hookInstallationOccurred());
    assertFalse(analysis.runtimeDispatchOccurred());
    assertFalse(analysis.commandRegistrationOccurred());
    assertFalse(analysis.commandExecutionOccurred());
    assertFalse(analysis.publicApiExposed());
    assertFalse(analysis.javaModExecutionSandboxed());
    assertEquals(0, analysis.boundMinecraftSymbolCount());
    assertEquals(0, analysis.implementedBoundaryCount());
  }

  @Test
  void analysisUsesCommandRegistrationConceptOrderAndDisplayName() {
    MinecraftCommandRegistrationAnalysis analysis = analyzer.analyze(conceptCatalog, plan(true));

    assertEquals("minecraft.concept.command_registration", analysis.conceptId());
    assertEquals(2, analysis.conceptOrder());
    assertEquals("Command Registration", analysis.conceptDisplayName());
    assertEquals("minecraft.concept.server_lifecycle", analysis.upstreamConceptId());
    assertEquals("Target-12", analysis.sourceLifecycleDispatchPlanMilestone());
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
        gatePassed ? null : "Target-13 requires a planned Target-12 starting lifecycle dispatch.",
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
                    : "Target-13 requires a planned Target-12 starting lifecycle dispatch."),
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
