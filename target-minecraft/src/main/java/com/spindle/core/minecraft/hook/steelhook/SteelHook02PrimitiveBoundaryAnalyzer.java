package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.MinecraftSide;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchKind;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchPlan;
import com.spindle.core.minecraft.hook.patch.MinecraftPlannedHookPatch;
import java.util.ArrayList;
import java.util.List;

public final class SteelHook02PrimitiveBoundaryAnalyzer {
  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-23";
  private static final String TARGET = "minecraft";
  private static final String STEELHOOK_VERSION = "0.2";
  private static final String SOURCE_PATCH_PLAN_MILESTONE = "Target-7";
  private static final String SOURCE_STEELHOOK_COMPLETION_MILESTONE = "Target-10";
  private static final String SOURCE_REGISTRY_HARDENING_MILESTONE = "Target-22";
  private static final int SUPPORTED_PRIMITIVE_COUNT = 1;
  private static final String EXPECTED_OWNER = "net/minecraft/server/Main";
  private static final String EXPECTED_MEMBER = "main";
  private static final String EXPECTED_DESCRIPTOR = "([Ljava/lang/String;)V";
  private static final String EXPECTED_DISPATCHER_OWNER =
      "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher";
  private static final String EXPECTED_DISPATCHER_METHOD = "beforeMinecraftServerMain";
  private static final String EXPECTED_DISPATCHER_DESCRIPTOR = "()V";
  private static final String UPSTREAM_BLOCKED_ACTION =
      "Restore the Target-7 patch-plan chain before selecting a SteelHook 0.2 primitive boundary.";
  private static final String MORE_EVIDENCE_ACTION =
      "Produce exactly one supported Target-7 planned patch before selecting a SteelHook 0.2 primitive boundary.";
  private static final String BOUNDARY_BLOCKED_ACTION =
      "Keep SteelHook 0.2 analysis-only and restore the exact Target-7 method-entry static-dispatch proof shape before Target-24.";
  private static final String TARGET_24_ACTION =
      "Move next to Target-24 contract and patch-plan generalization for the approved method-entry static-dispatch primitive.";

  public SteelHook02PrimitiveBoundaryAnalysis analyze(MinecraftHookPatchPlan patchPlan) {
    List<SteelHook02PrimitiveFinding> findings = new ArrayList<>();

    if (patchPlan == null) {
      addFinding(
          findings,
          1,
          "Target-7 patch plan exists.",
          SteelHook02PrimitiveFindingStatus.FAIL,
          true,
          "Target-23 requires the upstream Target-7 patch plan report.",
          "No patch plan was provided to the Target-23 analyzer.");
      return buildBlocked(
          null,
          MinecraftSide.SERVER,
          false,
          "Target-7 patch plan is missing.",
          SteelHook02PrimitiveBoundaryStatus.UPSTREAM_GATE_BLOCKED,
          SteelHook02NextDirection.RESTORE_UPSTREAM_STEELHOOK_0_1_CHAIN,
          UPSTREAM_BLOCKED_ACTION,
          List.of(),
          findings);
    }

    addFinding(
        findings,
        1,
        "Target-7 patch plan exists.",
        SteelHook02PrimitiveFindingStatus.PASS,
        true,
        "Target-23 received an upstream Target-7 patch plan.",
        "Patch plan input is present.");
    addFinding(
        findings,
        2,
        "Target-7 patch plan gate passed.",
        patchPlan.gatePassed()
            ? SteelHook02PrimitiveFindingStatus.PASS
            : SteelHook02PrimitiveFindingStatus.FAIL,
        true,
        patchPlan.gatePassed()
            ? "Target-7 patch planning gate passed."
            : "Target-7 patch planning gate failed.",
        patchPlan.gatePassed()
            ? "Target-23 can inspect the upstream plan shape."
            : "Upstream gate failure reason: " + patchPlan.gateFailureReason());
    if (!SOURCE_PATCH_PLAN_MILESTONE.equals(patchPlan.milestoneName())) {
      throw new IllegalArgumentException("Target-23 input must have milestoneName \"Target-7\".");
    }
    addFinding(
        findings,
        3,
        "Target-7 milestone name matches the expected source pass.",
        SteelHook02PrimitiveFindingStatus.PASS,
        true,
        "Target-23 is consuming the expected Target-7 patch-plan milestone.",
        "sourcePatchPlanMilestone remains Target-7.");

    if (!patchPlan.gatePassed()) {
      return buildBlocked(
          patchPlan.minecraftVersion(),
          parseSide(patchPlan.side()),
          false,
          "Target-7 patch plan gate failed.",
          SteelHook02PrimitiveBoundaryStatus.UPSTREAM_GATE_BLOCKED,
          SteelHook02NextDirection.RESTORE_UPSTREAM_STEELHOOK_0_1_CHAIN,
          UPSTREAM_BLOCKED_ACTION,
          List.of(),
          findings);
    }

    int plannedPatchCount = patchPlan.plannedPatches().size();
    SteelHook02PrimitiveFindingStatus countStatus =
        plannedPatchCount == 1
            ? SteelHook02PrimitiveFindingStatus.PASS
            : (plannedPatchCount == 0
                ? SteelHook02PrimitiveFindingStatus.WARNING
                : SteelHook02PrimitiveFindingStatus.FAIL);
    addFinding(
        findings,
        4,
        "Target-7 produced exactly one planned patch.",
        countStatus,
        true,
        plannedPatchCount == 1
            ? "Target-7 produced exactly one candidate patch."
            : "Target-7 did not produce exactly one candidate patch.",
        "plannedPatchCount=" + plannedPatchCount);
    if (plannedPatchCount == 0) {
      return buildBlocked(
          patchPlan.minecraftVersion(),
          parseSide(patchPlan.side()),
          false,
          "Target-7 produced no planned patch for SteelHook 0.2 candidate selection.",
          SteelHook02PrimitiveBoundaryStatus.MORE_UPSTREAM_EVIDENCE_REQUIRED,
          SteelHook02NextDirection.CONTINUE_TARGET_LAYER_CONCEPT_ANALYSIS,
          MORE_EVIDENCE_ACTION,
          List.of(),
          findings);
    }
    if (plannedPatchCount != 1) {
      return buildBlocked(
          patchPlan.minecraftVersion(),
          parseSide(patchPlan.side()),
          false,
          "Target-23 supports exactly one upstream planned patch candidate.",
          SteelHook02PrimitiveBoundaryStatus.PRIMITIVE_BOUNDARY_BLOCKED,
          SteelHook02NextDirection.RESTORE_UPSTREAM_STEELHOOK_0_1_CHAIN,
          BOUNDARY_BLOCKED_ACTION,
          List.of(),
          findings);
    }

    MinecraftPlannedHookPatch patch = patchPlan.plannedPatches().getFirst();
    boolean matchesKind = patch.kind() == MinecraftHookPatchKind.METHOD_ENTRY_STATIC_DISPATCH;
    addFinding(
        findings,
        5,
        "Patch kind is METHOD_ENTRY_STATIC_DISPATCH.",
        matchesKind
            ? SteelHook02PrimitiveFindingStatus.PASS
            : SteelHook02PrimitiveFindingStatus.FAIL,
        true,
        matchesKind
            ? "Target-7 uses the approved method-entry static-dispatch primitive kind."
            : "Target-7 patch kind is outside the approved SteelHook 0.2 primitive boundary.",
        "patchKind=" + patch.kind());

    boolean matchesTargetShape =
        EXPECTED_OWNER.equals(patch.ownerInternalName())
            && EXPECTED_MEMBER.equals(patch.memberName())
            && EXPECTED_DESCRIPTOR.equals(patch.descriptor());
    addFinding(
        findings,
        6,
        "Patch target is net/minecraft/server/Main.main([Ljava/lang/String;)V.",
        matchesTargetShape
            ? SteelHook02PrimitiveFindingStatus.PASS
            : SteelHook02PrimitiveFindingStatus.FAIL,
        true,
        matchesTargetShape
            ? "Target-7 still points to the approved Minecraft main entrypoint shape."
            : "Target-7 no longer matches the approved Minecraft main entrypoint shape.",
        "owner="
            + patch.ownerInternalName()
            + ", member="
            + patch.memberName()
            + ", descriptor="
            + patch.descriptor());

    boolean matchesInsertionOffset = patch.insertionOffset() == 0;
    addFinding(
        findings,
        7,
        "Insertion offset is 0.",
        matchesInsertionOffset
            ? SteelHook02PrimitiveFindingStatus.PASS
            : SteelHook02PrimitiveFindingStatus.FAIL,
        true,
        matchesInsertionOffset
            ? "Target-7 still plans method-entry insertion at bytecode offset 0."
            : "Target-7 insertion offset drifted away from the approved method-entry proof.",
        "insertionOffset=" + patch.insertionOffset());

    boolean matchesDispatcher =
        patch.codeInsertion() != null
            && EXPECTED_DISPATCHER_OWNER.equals(patch.codeInsertion().dispatcherOwnerInternalName())
            && EXPECTED_DISPATCHER_METHOD.equals(patch.codeInsertion().dispatcherMethodName())
            && EXPECTED_DISPATCHER_DESCRIPTOR.equals(patch.codeInsertion().dispatcherDescriptor());
    addFinding(
        findings,
        8,
        "Dispatcher target is SteelHookDispatcher.beforeMinecraftServerMain:()V.",
        matchesDispatcher
            ? SteelHook02PrimitiveFindingStatus.PASS
            : SteelHook02PrimitiveFindingStatus.FAIL,
        true,
        matchesDispatcher
            ? "Target-7 still uses the approved SteelHook dispatcher proof target."
            : "Target-7 dispatcher target drifted outside the approved SteelHook 0.2 boundary.",
        patch.codeInsertion() == null
            ? "Patch code insertion is missing."
            : "dispatcherOwner="
                + patch.codeInsertion().dispatcherOwnerInternalName()
                + ", dispatcherMethod="
                + patch.codeInsertion().dispatcherMethodName()
                + ", dispatcherDescriptor="
                + patch.codeInsertion().dispatcherDescriptor());

    boolean fixtureReady = patch.transformReadyForFixtureOnly();
    addFinding(
        findings,
        9,
        "Fixture transformation readiness remains true.",
        fixtureReady
            ? SteelHook02PrimitiveFindingStatus.PASS
            : SteelHook02PrimitiveFindingStatus.FAIL,
        true,
        fixtureReady
            ? "The approved primitive remains ready only for the fixture proof path."
            : "Target-23 requires the fixture proof to remain ready before generalization.",
        "transformReadyForFixtureOnly=" + patch.transformReadyForFixtureOnly());

    boolean runtimeReadyFalse =
        !patch.transformReadyForMinecraftRuntime()
            && !patchPlan.transformReadyForMinecraftRuntime();
    addFinding(
        findings,
        10,
        "Minecraft runtime transformation readiness remains false.",
        runtimeReadyFalse
            ? SteelHook02PrimitiveFindingStatus.PASS
            : SteelHook02PrimitiveFindingStatus.FAIL,
        true,
        runtimeReadyFalse
            ? "Target-23 preserves analysis-only scope and does not claim runtime readiness."
            : "Target-23 must not report real Minecraft runtime transformation readiness.",
        "patch.transformReadyForMinecraftRuntime="
            + patch.transformReadyForMinecraftRuntime()
            + ", plan.transformReadyForMinecraftRuntime="
            + patchPlan.transformReadyForMinecraftRuntime());

    boolean noMixinOrAgent = !patchPlan.mixinUsed() && !patchPlan.javaAgentUsed();
    addFinding(
        findings,
        11,
        "No Java agent or Mixin path is claimed.",
        noMixinOrAgent
            ? SteelHook02PrimitiveFindingStatus.PASS
            : SteelHook02PrimitiveFindingStatus.FAIL,
        true,
        noMixinOrAgent
            ? "Target-23 still excludes Java agent and Mixin paths."
            : "Target-23 must not broaden into Java agent or Mixin-backed transformation claims.",
        "mixinUsed=" + patchPlan.mixinUsed() + ", javaAgentUsed=" + patchPlan.javaAgentUsed());

    boolean noApi = !patchPlan.publicApiExposed();
    addFinding(
        findings,
        12,
        "No public API is exposed.",
        noApi ? SteelHook02PrimitiveFindingStatus.PASS : SteelHook02PrimitiveFindingStatus.FAIL,
        true,
        noApi
            ? "Target-23 keeps SteelHook internal."
            : "Target-23 must not expose SteelHook as a public API.",
        "publicApiExposed=" + patchPlan.publicApiExposed());

    boolean noSandboxClaim = !patchPlan.javaModExecutionSandboxed();
    addFinding(
        findings,
        13,
        "Java mod execution is not claimed to be sandboxed.",
        noSandboxClaim
            ? SteelHook02PrimitiveFindingStatus.PASS
            : SteelHook02PrimitiveFindingStatus.FAIL,
        true,
        noSandboxClaim
            ? "Target-23 preserves the existing no-sandbox posture."
            : "Target-23 must not claim Java mod execution sandboxing.",
        "javaModExecutionSandboxed=" + patchPlan.javaModExecutionSandboxed());

    boolean analysisOnly =
        !patchPlan.injectionOccurred()
            && !patchPlan.transformationOccurred()
            && !patchPlan.patchingOccurred();
    addFinding(
        findings,
        14,
        "Target-23 remains analysis-only.",
        analysisOnly
            ? SteelHook02PrimitiveFindingStatus.PASS
            : SteelHook02PrimitiveFindingStatus.FAIL,
        true,
        analysisOnly
            ? "The upstream patch plan remains analysis-only."
            : "Target-23 must not observe injection, transformation, or patching activity.",
        "injectionOccurred="
            + patchPlan.injectionOccurred()
            + ", transformationOccurred="
            + patchPlan.transformationOccurred()
            + ", patchingOccurred="
            + patchPlan.patchingOccurred());

    boolean bytecodeUnmodified = !patchPlan.bytecodeModified();
    addFinding(
        findings,
        15,
        "Target-23 does not observe bytecode modification.",
        bytecodeUnmodified
            ? SteelHook02PrimitiveFindingStatus.PASS
            : SteelHook02PrimitiveFindingStatus.FAIL,
        true,
        bytecodeUnmodified
            ? "The upstream patch plan still reports no bytecode modification."
            : "Target-23 must not observe bytecode modification in an analysis-only pass.",
        "bytecodeModified=" + patchPlan.bytecodeModified());

    boolean boundaryPassed =
        matchesKind
            && matchesTargetShape
            && matchesInsertionOffset
            && matchesDispatcher
            && fixtureReady
            && runtimeReadyFalse
            && noMixinOrAgent
            && noApi
            && noSandboxClaim
            && analysisOnly
            && bytecodeUnmodified;

    List<SteelHook02PrimitiveCandidate> candidates =
        List.of(
            new SteelHook02PrimitiveCandidate(
                "target-23.steelhook-0-2.primitive.candidate.001",
                SteelHook02PrimitiveKind.METHOD_ENTRY_STATIC_DISPATCH,
                boundaryPassed
                    ? SteelHook02PrimitiveCandidateStatus
                        .APPROVED_FOR_TARGET_24_CONTRACT_GENERALIZATION
                    : (matchesKind
                        ? SteelHook02PrimitiveCandidateStatus.REJECTED_UNSUPPORTED_SHAPE
                        : SteelHook02PrimitiveCandidateStatus.DEFERRED_UNSUPPORTED_PRIMITIVE),
                patch.id(),
                patch.ownerInternalName(),
                patch.memberName(),
                patch.descriptor(),
                patch.insertionOffset(),
                patch.codeInsertion() == null
                    ? null
                    : patch.codeInsertion().dispatcherOwnerInternalName(),
                patch.codeInsertion() == null ? null : patch.codeInsertion().dispatcherMethodName(),
                patch.codeInsertion() == null ? null : patch.codeInsertion().dispatcherDescriptor(),
                patch.transformReadyForFixtureOnly(),
                patch.transformReadyForMinecraftRuntime(),
                boundaryPassed,
                boundaryPassed,
                false,
                boundaryPassed
                    ? List.of(
                        "Approved only as a Target-24 planning candidate.",
                        "Runtime transformation readiness remains false.",
                        "Target-23 does not install hooks, transform real Minecraft classes, or expose public API.")
                    : List.of(
                        "Observed Target-7 candidate shape drifted outside the approved Target-23 boundary.",
                        "Runtime transformation readiness remains false and must be implemented later.")));

    if (!boundaryPassed) {
      return buildBlocked(
          patchPlan.minecraftVersion(),
          parseSide(patchPlan.side()),
          false,
          "Target-7 patch shape is outside the approved SteelHook 0.2 primitive boundary.",
          SteelHook02PrimitiveBoundaryStatus.PRIMITIVE_BOUNDARY_BLOCKED,
          SteelHook02NextDirection.RESTORE_UPSTREAM_STEELHOOK_0_1_CHAIN,
          BOUNDARY_BLOCKED_ACTION,
          candidates,
          findings);
    }

    return new SteelHook02PrimitiveBoundaryAnalysis(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        STEELHOOK_VERSION,
        patchPlan.minecraftVersion(),
        parseSide(patchPlan.side()),
        SOURCE_PATCH_PLAN_MILESTONE,
        SOURCE_STEELHOOK_COMPLETION_MILESTONE,
        SOURCE_REGISTRY_HARDENING_MILESTONE,
        true,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        SUPPORTED_PRIMITIVE_COUNT,
        1,
        0,
        0,
        true,
        null,
        SteelHook02PrimitiveBoundaryStatus.PRIMITIVE_BOUNDARY_SELECTED,
        SteelHook02NextDirection.MOVE_TO_TARGET_24_CONTRACT_AND_PATCH_PLAN_GENERALIZATION,
        TARGET_24_ACTION,
        candidates,
        findings);
  }

  private SteelHook02PrimitiveBoundaryAnalysis buildBlocked(
      String minecraftVersion,
      MinecraftSide side,
      boolean gatePassed,
      String gateFailureReason,
      SteelHook02PrimitiveBoundaryStatus boundaryStatus,
      SteelHook02NextDirection nextDirection,
      String nextRecommendedAction,
      List<SteelHook02PrimitiveCandidate> candidates,
      List<SteelHook02PrimitiveFinding> findings) {
    int approvedCount =
        (int)
            candidates.stream()
                .filter(
                    candidate ->
                        candidate.candidateStatus()
                            == SteelHook02PrimitiveCandidateStatus
                                .APPROVED_FOR_TARGET_24_CONTRACT_GENERALIZATION)
                .count();
    int deferredCount =
        (int)
            candidates.stream()
                .filter(
                    candidate ->
                        candidate.candidateStatus()
                            == SteelHook02PrimitiveCandidateStatus.DEFERRED_UNSUPPORTED_PRIMITIVE)
                .count();
    int rejectedCount =
        (int)
            candidates.stream()
                .filter(
                    candidate ->
                        candidate.candidateStatus()
                            == SteelHook02PrimitiveCandidateStatus.REJECTED_UNSUPPORTED_SHAPE)
                .count();
    return new SteelHook02PrimitiveBoundaryAnalysis(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        STEELHOOK_VERSION,
        minecraftVersion,
        side,
        SOURCE_PATCH_PLAN_MILESTONE,
        SOURCE_STEELHOOK_COMPLETION_MILESTONE,
        SOURCE_REGISTRY_HARDENING_MILESTONE,
        true,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        SUPPORTED_PRIMITIVE_COUNT,
        approvedCount,
        deferredCount,
        rejectedCount,
        gatePassed,
        gateFailureReason,
        boundaryStatus,
        nextDirection,
        nextRecommendedAction,
        candidates,
        findings);
  }

  private void addFinding(
      List<SteelHook02PrimitiveFinding> findings,
      int sequence,
      String checkName,
      SteelHook02PrimitiveFindingStatus status,
      boolean blocking,
      String summary,
      String notes) {
    findings.add(
        new SteelHook02PrimitiveFinding(
            "target-23.steelhook-0-2.primitive.finding.%03d".formatted(sequence),
            checkName,
            status,
            blocking,
            summary,
            notes));
  }

  private MinecraftSide parseSide(String side) {
    if (side == null || side.isBlank() || "server".equalsIgnoreCase(side)) {
      return MinecraftSide.SERVER;
    }
    if ("client".equalsIgnoreCase(side)) {
      return MinecraftSide.CLIENT;
    }
    return MinecraftSide.SERVER;
  }
}
