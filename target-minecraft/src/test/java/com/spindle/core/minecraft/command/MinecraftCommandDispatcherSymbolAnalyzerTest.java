package com.spindle.core.minecraft.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftSide;
import com.spindle.core.minecraft.interpret.MinecraftArtifactInterpretation;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedClass;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedField;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedJar;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedMethod;
import java.util.List;
import org.junit.jupiter.api.Test;

class MinecraftCommandDispatcherSymbolAnalyzerTest {
  private static final String DISPATCHER_DESCRIPTOR = "Lcom/mojang/brigadier/CommandDispatcher;";

  private final MinecraftCommandDispatcherSymbolAnalyzer analyzer =
      new MinecraftCommandDispatcherSymbolAnalyzer();

  @Test
  void singleDispatcherMethodCandidateSelectsStableTarget() {
    MinecraftCommandDispatcherSymbolAnalysis analysis =
        analyzer.analyze(
            interpretation(
                interpretedClass(
                    "net/minecraft/commands/Commands",
                    List.of(),
                    List.of(
                        new MinecraftInterpretedMethod(
                            "buildDispatcher",
                            "(Lcom/mojang/brigadier/CommandDispatcher;)V",
                            9,
                            List.of("PUBLIC", "STATIC"),
                            false,
                            true)))),
            commandRegistrationAnalysis(true));

    assertTrue(analysis.gatePassed());
    assertEquals(
        MinecraftCommandDispatcherSymbolSelectionStatus.STABLE_TARGET_SELECTED,
        analysis.selectionStatus());
    assertTrue(analysis.minimalCommandRegistrationProofEligible());
    assertEquals(1, analysis.selectedCandidateCount());
    assertEquals(
        "target-14.minecraft.commands.dispatcher.candidate.001", analysis.selectedCandidateId());
    assertEquals(1, analysis.methodCandidateCount());
    assertEquals(0, analysis.fieldCandidateCount());
    assertEquals(1, analysis.selectableCandidateCount());

    MinecraftCommandDispatcherSymbolCandidate candidate = analysis.candidates().getFirst();
    assertTrue(candidate.selectable());
    assertTrue(candidate.selected());
    assertEquals(
        MinecraftCommandDispatcherSymbolCandidateKind.METHOD_DESCRIPTOR_REFERENCE,
        candidate.kind());
    assertEquals(List.of("PUBLIC", "STATIC"), candidate.accessFlags());
  }

  @Test
  void singleDispatcherFieldCandidateSelectsStableTarget() {
    MinecraftCommandDispatcherSymbolAnalysis analysis =
        analyzer.analyze(
            interpretation(
                interpretedClass(
                    "net/minecraft/server/CommandsHolder",
                    List.of(
                        new MinecraftInterpretedField(
                            "dispatcher", DISPATCHER_DESCRIPTOR, 8, List.of("static"))),
                    List.of())),
            commandRegistrationAnalysis(true));

    assertEquals(
        MinecraftCommandDispatcherSymbolSelectionStatus.STABLE_TARGET_SELECTED,
        analysis.selectionStatus());
    assertEquals(1, analysis.fieldCandidateCount());
    assertEquals(0, analysis.methodCandidateCount());
    assertEquals("dispatcher", analysis.candidates().getFirst().memberName());
    assertEquals(List.of("static"), analysis.candidates().getFirst().accessFlags());
    assertTrue(analysis.candidates().getFirst().staticMember());
  }

  @Test
  void nonStaticDispatcherFieldCandidateReportsStaticMemberFalse() {
    MinecraftCommandDispatcherSymbolAnalysis analysis =
        analyzer.analyze(
            interpretation(
                interpretedClass(
                    "net/minecraft/server/CommandsHolder",
                    List.of(
                        new MinecraftInterpretedField(
                            "dispatcher", DISPATCHER_DESCRIPTOR, 1, List.of("public"))),
                    List.of())),
            commandRegistrationAnalysis(true));

    assertEquals(
        MinecraftCommandDispatcherSymbolSelectionStatus.STABLE_TARGET_SELECTED,
        analysis.selectionStatus());
    assertFalse(analysis.candidates().getFirst().staticMember());
  }

  @Test
  void noDispatcherCandidatesMarksNoCandidates() {
    MinecraftCommandDispatcherSymbolAnalysis analysis =
        analyzer.analyze(
            interpretation(
                interpretedClass(
                    "net/minecraft/server/Main",
                    List.of(new MinecraftInterpretedField("value", "I", 1, List.of("PUBLIC"))),
                    List.of(
                        new MinecraftInterpretedMethod(
                            "run", "()V", 1, List.of("PUBLIC"), false, false)))),
            commandRegistrationAnalysis(true));

    assertEquals(
        MinecraftCommandDispatcherSymbolSelectionStatus.NO_CANDIDATES, analysis.selectionStatus());
    assertFalse(analysis.minimalCommandRegistrationProofEligible());
    assertEquals(0, analysis.selectedCandidateCount());
    assertNull(analysis.selectedCandidateId());
    assertEquals(0, analysis.candidateCount());
  }

  @Test
  void multipleSelectableCandidatesMarksAmbiguous() {
    MinecraftCommandDispatcherSymbolAnalysis analysis =
        analyzer.analyze(
            interpretation(
                interpretedClass(
                    "net/minecraft/commands/Commands",
                    List.of(
                        new MinecraftInterpretedField(
                            "dispatcher", DISPATCHER_DESCRIPTOR, 1, List.of("PUBLIC"))),
                    List.of(
                        new MinecraftInterpretedMethod(
                            "create",
                            "(Lcom/mojang/brigadier/CommandDispatcher;)V",
                            1,
                            List.of("PUBLIC"),
                            false,
                            false)))),
            commandRegistrationAnalysis(true));

    assertEquals(
        MinecraftCommandDispatcherSymbolSelectionStatus.AMBIGUOUS_CANDIDATES,
        analysis.selectionStatus());
    assertFalse(analysis.minimalCommandRegistrationProofEligible());
    assertEquals(2, analysis.selectableCandidateCount());
    assertEquals(0, analysis.selectedCandidateCount());
    assertTrue(
        analysis.candidates().stream()
            .noneMatch(MinecraftCommandDispatcherSymbolCandidate::selected));
  }

  @Test
  void upstreamCommandAnalysisGateFailureBlocksAnalysis() {
    MinecraftCommandDispatcherSymbolAnalysis analysis =
        analyzer.analyze(
            interpretation(
                interpretedClass(
                    "net/minecraft/commands/Commands",
                    List.of(),
                    List.of(
                        new MinecraftInterpretedMethod(
                            "create",
                            "(Lcom/mojang/brigadier/CommandDispatcher;)V",
                            1,
                            List.of("PUBLIC"),
                            false,
                            false)))),
            commandRegistrationAnalysis(false));

    assertFalse(analysis.gatePassed());
    assertEquals(
        MinecraftCommandDispatcherSymbolSelectionStatus.UPSTREAM_GATE_BLOCKED,
        analysis.selectionStatus());
    assertFalse(analysis.minimalCommandRegistrationProofEligible());
    assertEquals(
        "Target-14 requires an available Target-13 command lifecycle anchor.",
        analysis.gateFailureReason());
    assertEquals(0, analysis.selectedCandidateCount());
    assertNull(analysis.selectedCandidateId());
  }

  @Test
  void brigadierLibraryClassIsReportedButNotSelectable() {
    MinecraftCommandDispatcherSymbolAnalysis analysis =
        analyzer.analyze(
            interpretation(
                interpretedClass("com/mojang/brigadier/CommandDispatcher", List.of(), List.of())),
            commandRegistrationAnalysis(true));

    assertEquals(1, analysis.libraryClassCandidateCount());
    MinecraftCommandDispatcherSymbolCandidate candidate = analysis.candidates().getFirst();
    assertEquals(
        MinecraftCommandDispatcherSymbolCandidateKind.BRIGADIER_LIBRARY_CLASS, candidate.kind());
    assertFalse(candidate.selectable());
    assertFalse(candidate.selected());
    assertEquals("com/mojang/brigadier/CommandDispatcher", candidate.ownerInternalName());
    assertEquals("Brigadier library class presence is metadata only.", candidate.notes());
  }

  @Test
  void spindleOwnedCandidateIsRejected() {
    MinecraftCommandDispatcherSymbolAnalysis analysis =
        analyzer.analyze(
            interpretation(
                interpretedClass(
                    "com/spindle/core/minecraft/FakeDispatcherOwner",
                    List.of(),
                    List.of(
                        new MinecraftInterpretedMethod(
                            "create",
                            "(Lcom/mojang/brigadier/CommandDispatcher;)V",
                            9,
                            List.of("PUBLIC", "STATIC"),
                            false,
                            true)))),
            commandRegistrationAnalysis(true));

    MinecraftCommandDispatcherSymbolCandidate candidate = analysis.candidates().getFirst();
    assertFalse(candidate.selectable());
    assertEquals(
        "Spindle classes are not Minecraft command registration targets.",
        candidate.rejectionReason());
    assertEquals(
        MinecraftCommandDispatcherSymbolSelectionStatus.NO_CANDIDATES, analysis.selectionStatus());
  }

  @Test
  void analysisFlagsRemainMutationFreeCommandFreeAndSandboxFalse() {
    MinecraftCommandDispatcherSymbolAnalysis analysis =
        analyzer.analyze(
            interpretation(
                interpretedClass(
                    "net/minecraft/server/CommandsHolder",
                    List.of(
                        new MinecraftInterpretedField(
                            "dispatcher", DISPATCHER_DESCRIPTOR, 8, List.of("static"))),
                    List.of())),
            commandRegistrationAnalysis(true));

    assertEquals(1, analysis.schema());
    assertEquals("Target-14", analysis.milestoneName());
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

  private MinecraftArtifactInterpretation interpretation(MinecraftInterpretedClass... classes) {
    int fieldCount = 0;
    int methodCount = 0;
    for (MinecraftInterpretedClass interpretedClass : classes) {
      fieldCount += interpretedClass.fields().size();
      methodCount +=
          interpretedClass.methods().stream().filter(method -> !method.constructor()).count();
    }
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
                classes.length,
                fieldCount,
                methodCount,
                0,
                List.of(),
                List.of(classes))),
        0,
        classes.length,
        fieldCount,
        methodCount,
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

  private MinecraftCommandRegistrationAnalysis commandRegistrationAnalysis(boolean gatePassed) {
    MinecraftCommandRegistrationBoundaryStatus anchorStatus =
        gatePassed
            ? MinecraftCommandRegistrationBoundaryStatus.ANCHOR_AVAILABLE
            : MinecraftCommandRegistrationBoundaryStatus.BLOCKED;
    return new MinecraftCommandRegistrationAnalysis(
        1,
        "Target-13",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.command_registration",
        2,
        "Command Registration",
        "minecraft.concept.server_lifecycle",
        "Target-12",
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
        gatePassed,
        gatePassed,
        gatePassed ? null : "Target-14 requires an available Target-13 command lifecycle anchor.",
        5,
        gatePassed ? 1 : 0,
        4,
        gatePassed ? 0 : 1,
        0,
        0,
        List.of(
            new MinecraftAnalyzedCommandRegistrationBoundary(
                "target-13.minecraft.commands.lifecycle_anchor",
                "minecraft.commands.lifecycle_anchor",
                "Lifecycle Anchor",
                anchorStatus,
                MinecraftCommandRegistrationRepresentationKind.UPSTREAM_LIFECYCLE_DISPATCH,
                "minecraft.concept.server_lifecycle",
                "target-12.minecraft.server.lifecycle.starting.dispatch",
                "minecraft.server.lifecycle.starting",
                false,
                null,
                null,
                null,
                false,
                false,
                false,
                true,
                gatePassed
                    ? "Anchored to the symbolic Target-12 starting lifecycle dispatch without binding a Minecraft command dispatcher symbol."
                    : "Target-14 requires an available Target-13 command lifecycle anchor."),
            futureBoundary("dispatcher.discovery"),
            futureBoundary("registration.window"),
            futureBoundary("registration.apply"),
            futureBoundary("reload.reapply")));
  }

  private MinecraftAnalyzedCommandRegistrationBoundary futureBoundary(String suffix) {
    return new MinecraftAnalyzedCommandRegistrationBoundary(
        "target-13.minecraft.commands." + suffix,
        "minecraft.commands." + suffix,
        suffix,
        MinecraftCommandRegistrationBoundaryStatus.DECLARED_UNBOUND,
        MinecraftCommandRegistrationRepresentationKind.FUTURE_DISPATCHER_SYMBOL,
        "minecraft.concept.server_lifecycle",
        "target-12.minecraft.server.lifecycle.starting.dispatch",
        "minecraft.server.lifecycle.starting",
        false,
        null,
        null,
        null,
        true,
        false,
        false,
        true,
        "Future.");
  }
}
