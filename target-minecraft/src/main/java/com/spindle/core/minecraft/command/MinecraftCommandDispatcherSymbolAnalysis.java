package com.spindle.core.minecraft.command;

import com.spindle.core.minecraft.MinecraftSide;
import java.util.List;

public record MinecraftCommandDispatcherSymbolAnalysis(
    int schema,
    String milestoneName,
    String target,
    String minecraftVersion,
    MinecraftSide side,
    String conceptId,
    String sourceCommandRegistrationAnalysisMilestone,
    String commandBoundaryId,
    boolean analysisOnly,
    boolean classLoadingOccurred,
    boolean injectionOccurred,
    boolean transformationOccurred,
    boolean patchingOccurred,
    boolean hookInstallationOccurred,
    boolean runtimeDispatchOccurred,
    boolean commandRegistrationOccurred,
    boolean commandExecutionOccurred,
    boolean commandDispatcherAccessOccurred,
    boolean publicApiExposed,
    boolean javaModExecutionSandboxed,
    boolean sourceCommandRegistrationGatePassed,
    boolean lifecycleAnchorAvailable,
    boolean gatePassed,
    String gateFailureReason,
    String commandDispatcherDescriptor,
    int candidateCount,
    int methodCandidateCount,
    int fieldCandidateCount,
    int libraryClassCandidateCount,
    int selectableCandidateCount,
    int selectedCandidateCount,
    MinecraftCommandDispatcherSymbolSelectionStatus selectionStatus,
    boolean minimalCommandRegistrationProofEligible,
    String selectedCandidateId,
    List<MinecraftCommandDispatcherSymbolCandidate> candidates) {
  public MinecraftCommandDispatcherSymbolAnalysis {
    candidates = List.copyOf(candidates == null ? List.of() : candidates);
  }
}
