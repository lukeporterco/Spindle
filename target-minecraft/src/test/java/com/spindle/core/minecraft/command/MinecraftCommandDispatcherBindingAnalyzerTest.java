package com.spindle.core.minecraft.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftSide;
import java.util.List;
import org.junit.jupiter.api.Test;

class MinecraftCommandDispatcherBindingAnalyzerTest {
  private final MinecraftCommandDispatcherBindingAnalyzer analyzer =
      new MinecraftCommandDispatcherBindingAnalyzer();

  @Test
  void upstreamTarget14GateBlockedMapsToUpstreamGateBlocked() {
    MinecraftCommandDispatcherBindingAnalysis analysis =
        analyzer.analyze(
            symbolAnalysis(
                MinecraftCommandDispatcherSymbolSelectionStatus.UPSTREAM_GATE_BLOCKED,
                false,
                false,
                null));

    assertEquals(
        MinecraftCommandDispatcherBindingStatus.UPSTREAM_GATE_BLOCKED, analysis.bindingStatus());
    assertEquals(MinecraftCommandDispatcherAccessStrategy.NONE, analysis.accessStrategy());
    assertFalse(analysis.gatePassed());
    assertEquals(
        "Target-14 requires an available Target-13 command lifecycle anchor.",
        analysis.gateFailureReason());
    assertNull(analysis.selectedCandidateId());
    assertFalse(analysis.minimalCommandRegistrationProofRecommended());
  }

  @Test
  void target14NoCandidatesMapsToNoSymbolTarget() {
    MinecraftCommandDispatcherBindingAnalysis analysis =
        analyzer.analyze(
            symbolAnalysis(
                MinecraftCommandDispatcherSymbolSelectionStatus.NO_CANDIDATES, true, false, null));

    assertEquals(
        MinecraftCommandDispatcherBindingStatus.NO_SYMBOL_TARGET, analysis.bindingStatus());
    assertEquals(MinecraftCommandDispatcherAccessStrategy.NONE, analysis.accessStrategy());
    assertTrue(analysis.gatePassed());
    assertNull(analysis.gateFailureReason());
    assertFalse(analysis.minimalCommandRegistrationProofRecommended());
  }

  @Test
  void target14AmbiguousCandidatesMapsToAmbiguousSymbolTargets() {
    MinecraftCommandDispatcherBindingAnalysis analysis =
        analyzer.analyze(
            symbolAnalysis(
                MinecraftCommandDispatcherSymbolSelectionStatus.AMBIGUOUS_CANDIDATES,
                true,
                false,
                null));

    assertEquals(
        MinecraftCommandDispatcherBindingStatus.AMBIGUOUS_SYMBOL_TARGETS, analysis.bindingStatus());
    assertEquals(MinecraftCommandDispatcherAccessStrategy.NONE, analysis.accessStrategy());
    assertTrue(analysis.gatePassed());
    assertFalse(analysis.minimalCommandRegistrationProofRecommended());
  }

  @Test
  void selectedStaticMethodMapsToMethodDescriptorReferenceOnly() {
    MinecraftCommandDispatcherBindingAnalysis analysis =
        analyzer.analyze(
            selectedAnalysis(
                new MinecraftCommandDispatcherSymbolCandidate(
                    "target-14.minecraft.commands.dispatcher.candidate.001",
                    MinecraftCommandDispatcherSymbolCandidateKind.METHOD_DESCRIPTOR_REFERENCE,
                    "net/minecraft/commands/Commands",
                    "buildDispatcher",
                    "(Lcom/mojang/brigadier/CommandDispatcher;)V",
                    true,
                    List.of("PUBLIC", "STATIC"),
                    true,
                    true,
                    null,
                    null)));

    assertEquals(
        MinecraftCommandDispatcherBindingStatus.SELECTED_SYMBOL_ANALYZED, analysis.bindingStatus());
    assertEquals(
        MinecraftCommandDispatcherAccessStrategy.METHOD_DESCRIPTOR_REFERENCE_ONLY,
        analysis.accessStrategy());
    assertTrue(analysis.requiresDispatcherValueCapture());
    assertFalse(analysis.requiresOwnerInstanceCapture());
    assertFalse(analysis.requiresFieldAccess());
    assertTrue(analysis.requiresFutureSteelHookPrimitive());
    assertFalse(analysis.currentSteelHookMethodEntryCompatible());
    assertEquals(
        "Plan a future value-capturing command dispatcher primitive before command registration.",
        analysis.nextRecommendedAction());
    assertTrue(analysis.notes().contains("SteelHook 0.1 method-entry dispatch"));
  }

  @Test
  void selectedInstanceMethodMapsToInstanceMethodReceiverCaptureRequired() {
    MinecraftCommandDispatcherBindingAnalysis analysis =
        analyzer.analyze(
            selectedAnalysis(
                new MinecraftCommandDispatcherSymbolCandidate(
                    "target-14.minecraft.commands.dispatcher.candidate.001",
                    MinecraftCommandDispatcherSymbolCandidateKind.METHOD_DESCRIPTOR_REFERENCE,
                    "net/minecraft/commands/Commands",
                    "register",
                    "(Lcom/mojang/brigadier/CommandDispatcher;)V",
                    false,
                    List.of("PUBLIC"),
                    true,
                    true,
                    null,
                    null)));

    assertEquals(
        MinecraftCommandDispatcherAccessStrategy.INSTANCE_METHOD_RECEIVER_CAPTURE_REQUIRED,
        analysis.accessStrategy());
    assertTrue(analysis.requiresDispatcherValueCapture());
    assertTrue(analysis.requiresOwnerInstanceCapture());
    assertFalse(analysis.requiresFieldAccess());
  }

  @Test
  void selectedStaticFieldMapsToStaticFieldAccessRequired() {
    MinecraftCommandDispatcherBindingAnalysis analysis =
        analyzer.analyze(
            selectedAnalysis(
                new MinecraftCommandDispatcherSymbolCandidate(
                    "target-14.minecraft.commands.dispatcher.candidate.001",
                    MinecraftCommandDispatcherSymbolCandidateKind.FIELD_DESCRIPTOR_REFERENCE,
                    "net/minecraft/server/CommandsHolder",
                    "DISPATCHER",
                    "Lcom/mojang/brigadier/CommandDispatcher;",
                    true,
                    List.of("static"),
                    true,
                    true,
                    null,
                    null)));

    assertEquals(
        MinecraftCommandDispatcherAccessStrategy.STATIC_FIELD_ACCESS_REQUIRED,
        analysis.accessStrategy());
    assertTrue(analysis.requiresDispatcherValueCapture());
    assertFalse(analysis.requiresOwnerInstanceCapture());
    assertTrue(analysis.requiresFieldAccess());
  }

  @Test
  void selectedInstanceFieldMapsToInstanceFieldOwnerCaptureRequired() {
    MinecraftCommandDispatcherBindingAnalysis analysis =
        analyzer.analyze(
            selectedAnalysis(
                new MinecraftCommandDispatcherSymbolCandidate(
                    "target-14.minecraft.commands.dispatcher.candidate.001",
                    MinecraftCommandDispatcherSymbolCandidateKind.FIELD_DESCRIPTOR_REFERENCE,
                    "net/minecraft/server/CommandsHolder",
                    "dispatcher",
                    "Lcom/mojang/brigadier/CommandDispatcher;",
                    false,
                    List.of("public"),
                    true,
                    true,
                    null,
                    null)));

    assertEquals(
        MinecraftCommandDispatcherAccessStrategy.INSTANCE_FIELD_OWNER_CAPTURE_REQUIRED,
        analysis.accessStrategy());
    assertTrue(analysis.requiresDispatcherValueCapture());
    assertTrue(analysis.requiresOwnerInstanceCapture());
    assertTrue(analysis.requiresFieldAccess());
  }

  @Test
  void selectedSymbolNeverRecommendsCommandRegistrationProofInThisPass() {
    MinecraftCommandDispatcherBindingAnalysis analysis =
        analyzer.analyze(
            selectedAnalysis(
                new MinecraftCommandDispatcherSymbolCandidate(
                    "target-14.minecraft.commands.dispatcher.candidate.001",
                    MinecraftCommandDispatcherSymbolCandidateKind.FIELD_DESCRIPTOR_REFERENCE,
                    "net/minecraft/server/CommandsHolder",
                    "dispatcher",
                    "Lcom/mojang/brigadier/CommandDispatcher;",
                    false,
                    List.of("public"),
                    true,
                    true,
                    null,
                    null)));

    assertFalse(analysis.minimalCommandRegistrationProofRecommended());
  }

  @Test
  void allMutationRuntimeApiAndSandboxFlagsRemainFalse() {
    MinecraftCommandDispatcherBindingAnalysis analysis =
        analyzer.analyze(
            symbolAnalysis(
                MinecraftCommandDispatcherSymbolSelectionStatus.NO_CANDIDATES, true, false, null));

    assertEquals(1, analysis.schema());
    assertEquals("Target-15", analysis.milestoneName());
    assertTrue(analysis.analysisOnly());
    assertFalse(analysis.classLoadingOccurred());
    assertFalse(analysis.injectionOccurred());
    assertFalse(analysis.transformationOccurred());
    assertFalse(analysis.patchingOccurred());
    assertFalse(analysis.hookInstallationOccurred());
    assertFalse(analysis.runtimeDispatchOccurred());
    assertFalse(analysis.commandRegistrationOccurred());
    assertFalse(analysis.commandExecutionOccurred());
    assertFalse(analysis.commandDispatcherAccessOccurred());
    assertFalse(analysis.publicApiExposed());
    assertFalse(analysis.javaModExecutionSandboxed());
  }

  private MinecraftCommandDispatcherSymbolAnalysis selectedAnalysis(
      MinecraftCommandDispatcherSymbolCandidate candidate) {
    return new MinecraftCommandDispatcherSymbolAnalysis(
        1,
        "Target-14",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.command_registration",
        "Target-13",
        "minecraft.commands.dispatcher.discovery",
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
        true,
        true,
        true,
        null,
        "Lcom/mojang/brigadier/CommandDispatcher;",
        1,
        candidate.kind()
                == MinecraftCommandDispatcherSymbolCandidateKind.METHOD_DESCRIPTOR_REFERENCE
            ? 1
            : 0,
        candidate.kind() == MinecraftCommandDispatcherSymbolCandidateKind.FIELD_DESCRIPTOR_REFERENCE
            ? 1
            : 0,
        0,
        1,
        1,
        MinecraftCommandDispatcherSymbolSelectionStatus.STABLE_TARGET_SELECTED,
        true,
        candidate.id(),
        List.of(candidate));
  }

  private MinecraftCommandDispatcherSymbolAnalysis symbolAnalysis(
      MinecraftCommandDispatcherSymbolSelectionStatus status,
      boolean gatePassed,
      boolean minimalProofEligible,
      String selectedCandidateId) {
    return new MinecraftCommandDispatcherSymbolAnalysis(
        1,
        "Target-14",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.command_registration",
        "Target-13",
        "minecraft.commands.dispatcher.discovery",
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
        gatePassed,
        gatePassed,
        gatePassed,
        gatePassed ? null : "Target-14 requires an available Target-13 command lifecycle anchor.",
        "Lcom/mojang/brigadier/CommandDispatcher;",
        0,
        0,
        0,
        0,
        0,
        selectedCandidateId == null ? 0 : 1,
        status,
        minimalProofEligible,
        selectedCandidateId,
        List.of());
  }
}
