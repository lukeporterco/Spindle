package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.MinecraftSide;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchEligibility;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchKind;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchMode;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchPlan;
import com.spindle.core.minecraft.hook.patch.MinecraftPlannedHookPatch;
import java.util.ArrayList;
import java.util.List;

public final class SteelHook02ContractGeneralizationAnalyzer {
  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-24";
  private static final String TARGET = "minecraft";
  private static final String STEELHOOK_VERSION = "0.2";
  private static final String SOURCE_PATCH_PLAN_MILESTONE = "Target-7";
  private static final String SOURCE_PRIMITIVE_BOUNDARY_MILESTONE = "Target-23";
  private static final String EXPECTED_OWNER = "net/minecraft/server/Main";
  private static final String EXPECTED_BINARY_NAME = "net.minecraft.server.Main";
  private static final String EXPECTED_CLASS_ENTRY_NAME = "net/minecraft/server/Main.class";
  private static final String EXPECTED_MEMBER = "main";
  private static final String EXPECTED_DESCRIPTOR = "([Ljava/lang/String;)V";
  private static final String EXPECTED_DISPATCHER_OWNER =
      "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher";
  private static final String EXPECTED_DISPATCHER_BINARY_NAME =
      "com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher";
  private static final String EXPECTED_DISPATCHER_METHOD = "beforeMinecraftServerMain";
  private static final String EXPECTED_DISPATCHER_DESCRIPTOR = "()V";
  private static final String TARGET_DESCRIPTOR_ID = "target-24.steelhook-0-2.target.001";
  private static final String DISPATCHER_DESCRIPTOR_ID = "target-24.steelhook-0-2.dispatcher.001";
  private static final String PRIMITIVE_CONTRACT_ID =
      "target-24.steelhook-0-2.primitive-contract.001";
  private static final String GENERALIZED_PATCH_PLAN_ID =
      "target-24.steelhook-0-2.generalized-patch-plan.001";
  private static final String UPSTREAM_ACTION =
      "Restore the Target-23 primitive boundary selection before generating Target-24 SteelHook 0.2 contract descriptors.";
  private static final String TARGET_23_RESTORE_ACTION =
      "Restore the exact Target-23 approved primitive candidate before proceeding to Target-25.";
  private static final String TARGET_7_RESTORE_ACTION =
      "Restore the exact Target-7 method-entry static-dispatch patch proof before proceeding to Target-25.";
  private static final String TARGET_25_ACTION =
      "Use the generalized Target-24 descriptors as bounded input for Target-25 runtime-safe method-entry transformer extraction.";

  public SteelHook02ContractGeneralizationAnalysis analyze(
      SteelHook02PrimitiveBoundaryAnalysis primitiveBoundaryAnalysis,
      MinecraftHookPatchPlan patchPlan) {
    List<SteelHook02ContractGeneralizationFinding> findings = new ArrayList<>();

    if (primitiveBoundaryAnalysis == null) {
      addFinding(
          findings,
          1,
          "Target-23 primitive boundary analysis exists.",
          SteelHook02ContractGeneralizationFindingStatus.FAIL,
          true,
          "Target-24 requires the upstream Target-23 primitive boundary report.",
          "No Target-23 analysis was provided.");
      return buildBlocked(
          null,
          MinecraftSide.SERVER,
          "Target-23 primitive boundary analysis is missing.",
          SteelHook02ContractGeneralizationStatus.UPSTREAM_GATE_BLOCKED,
          SteelHook02ContractGeneralizationNextDirection.RESTORE_TARGET_23_PRIMITIVE_BOUNDARY,
          UPSTREAM_ACTION,
          findings);
    }

    MinecraftSide side =
        primitiveBoundaryAnalysis.side() == null
            ? MinecraftSide.SERVER
            : primitiveBoundaryAnalysis.side();
    addFinding(
        findings,
        1,
        "Target-23 primitive boundary analysis exists.",
        SteelHook02ContractGeneralizationFindingStatus.PASS,
        true,
        "Target-24 received the upstream Target-23 primitive boundary analysis.",
        "Primitive boundary input is present.");

    boolean primitiveGatePassed = primitiveBoundaryAnalysis.gatePassed();
    addFinding(
        findings,
        2,
        "Target-23 gate passed.",
        primitiveGatePassed
            ? SteelHook02ContractGeneralizationFindingStatus.PASS
            : SteelHook02ContractGeneralizationFindingStatus.FAIL,
        true,
        primitiveGatePassed ? "Target-23 gate passed." : "Target-23 gate failed.",
        primitiveGatePassed
            ? "Target-24 can inspect the approved candidate."
            : "Upstream gate failure reason: " + primitiveBoundaryAnalysis.gateFailureReason());
    if (!primitiveGatePassed) {
      return buildBlocked(
          primitiveBoundaryAnalysis.minecraftVersion(),
          side,
          "Target-23 primitive boundary gate failed.",
          SteelHook02ContractGeneralizationStatus.UPSTREAM_GATE_BLOCKED,
          SteelHook02ContractGeneralizationNextDirection.RESTORE_TARGET_23_PRIMITIVE_BOUNDARY,
          UPSTREAM_ACTION,
          findings);
    }

    boolean correctBoundaryStatus =
        primitiveBoundaryAnalysis.boundaryStatus()
            == SteelHook02PrimitiveBoundaryStatus.PRIMITIVE_BOUNDARY_SELECTED;
    addFinding(
        findings,
        3,
        "Target-23 boundary status is PRIMITIVE_BOUNDARY_SELECTED.",
        correctBoundaryStatus
            ? SteelHook02ContractGeneralizationFindingStatus.PASS
            : SteelHook02ContractGeneralizationFindingStatus.FAIL,
        true,
        correctBoundaryStatus
            ? "Target-23 selected the approved primitive boundary."
            : "Target-23 boundary status drifted from the approved Target-24 handoff state.",
        "boundaryStatus=" + primitiveBoundaryAnalysis.boundaryStatus().name());
    if (!correctBoundaryStatus) {
      return buildBlocked(
          primitiveBoundaryAnalysis.minecraftVersion(),
          side,
          "Target-23 did not reach PRIMITIVE_BOUNDARY_SELECTED.",
          SteelHook02ContractGeneralizationStatus.UPSTREAM_GATE_BLOCKED,
          SteelHook02ContractGeneralizationNextDirection.RESTORE_TARGET_23_PRIMITIVE_BOUNDARY,
          UPSTREAM_ACTION,
          findings);
    }

    boolean correctNextDirection =
        primitiveBoundaryAnalysis.nextDirection()
            == SteelHook02NextDirection.MOVE_TO_TARGET_24_CONTRACT_AND_PATCH_PLAN_GENERALIZATION;
    addFinding(
        findings,
        4,
        "Target-23 next direction points to Target-24 contract and patch-plan generalization.",
        correctNextDirection
            ? SteelHook02ContractGeneralizationFindingStatus.PASS
            : SteelHook02ContractGeneralizationFindingStatus.FAIL,
        true,
        correctNextDirection
            ? "Target-23 hands off directly to Target-24."
            : "Target-23 no longer hands off to Target-24.",
        "nextDirection=" + primitiveBoundaryAnalysis.nextDirection().name());
    if (!correctNextDirection) {
      return buildBlocked(
          primitiveBoundaryAnalysis.minecraftVersion(),
          side,
          "Target-23 next direction does not hand off to Target-24.",
          SteelHook02ContractGeneralizationStatus.UPSTREAM_GATE_BLOCKED,
          SteelHook02ContractGeneralizationNextDirection.RESTORE_TARGET_23_PRIMITIVE_BOUNDARY,
          UPSTREAM_ACTION,
          findings);
    }

    boolean approvedCandidateCountMatches = primitiveBoundaryAnalysis.approvedCandidateCount() == 1;
    addFinding(
        findings,
        5,
        "Target-23 approved candidate count is exactly 1.",
        approvedCandidateCountMatches
            ? SteelHook02ContractGeneralizationFindingStatus.PASS
            : SteelHook02ContractGeneralizationFindingStatus.FAIL,
        true,
        approvedCandidateCountMatches
            ? "Target-23 recorded exactly one approved candidate."
            : "Target-23 approved candidate count is outside the bounded Target-24 contract shape.",
        "approvedCandidateCount=" + primitiveBoundaryAnalysis.approvedCandidateCount());
    if (!approvedCandidateCountMatches) {
      return buildBlocked(
          primitiveBoundaryAnalysis.minecraftVersion(),
          side,
          "Target-23 must report exactly one approved candidate.",
          SteelHook02ContractGeneralizationStatus.UPSTREAM_GATE_BLOCKED,
          SteelHook02ContractGeneralizationNextDirection.RESTORE_TARGET_23_PRIMITIVE_BOUNDARY,
          UPSTREAM_ACTION,
          findings);
    }

    List<SteelHook02PrimitiveCandidate> approvedCandidates =
        primitiveBoundaryAnalysis.candidates().stream()
            .filter(
                candidate ->
                    candidate.candidateStatus()
                        == SteelHook02PrimitiveCandidateStatus
                            .APPROVED_FOR_TARGET_24_CONTRACT_GENERALIZATION)
            .toList();
    boolean exactlyOneApprovedCandidate = approvedCandidates.size() == 1;
    addFinding(
        findings,
        6,
        "Exactly one Target-23 candidate is approved for Target-24 contract generalization.",
        exactlyOneApprovedCandidate
            ? SteelHook02ContractGeneralizationFindingStatus.PASS
            : SteelHook02ContractGeneralizationFindingStatus.FAIL,
        true,
        exactlyOneApprovedCandidate
            ? "Target-23 exposes one approved Target-24 handoff candidate."
            : "Target-23 approved candidate cardinality no longer matches the Target-24 contract.",
        "approvedForTarget24Count=" + approvedCandidates.size());
    if (!exactlyOneApprovedCandidate) {
      return buildBlocked(
          primitiveBoundaryAnalysis.minecraftVersion(),
          side,
          approvedCandidates.isEmpty()
              ? "Target-23 does not contain an approved Target-24 candidate."
              : "Target-23 contains multiple approved Target-24 candidates.",
          SteelHook02ContractGeneralizationStatus.UPSTREAM_GATE_BLOCKED,
          SteelHook02ContractGeneralizationNextDirection.RESTORE_TARGET_23_PRIMITIVE_BOUNDARY,
          UPSTREAM_ACTION,
          findings);
    }

    boolean primitiveAnalysisOnly =
        primitiveBoundaryAnalysis.analysisOnly()
            && !primitiveBoundaryAnalysis.classLoadingOccurred()
            && !primitiveBoundaryAnalysis.injectionOccurred()
            && !primitiveBoundaryAnalysis.transformationOccurred()
            && !primitiveBoundaryAnalysis.patchingOccurred()
            && !primitiveBoundaryAnalysis.hookInstallationOccurred()
            && !primitiveBoundaryAnalysis.runtimeDispatchOccurred()
            && !primitiveBoundaryAnalysis.publicApiExposed()
            && !primitiveBoundaryAnalysis.javaModExecutionSandboxed();
    addFinding(
        findings,
        7,
        "Target-23 remains analysis-only with no classloading, installation, dispatch, API, or sandbox drift.",
        primitiveAnalysisOnly
            ? SteelHook02ContractGeneralizationFindingStatus.PASS
            : SteelHook02ContractGeneralizationFindingStatus.FAIL,
        true,
        primitiveAnalysisOnly
            ? "Target-23 still preserves the expected analysis-only SteelHook 0.2 boundary."
            : "Target-23 analysis-only invariants drifted from the expected Target-24 handoff shape.",
        "analysisOnly="
            + primitiveBoundaryAnalysis.analysisOnly()
            + ", classLoadingOccurred="
            + primitiveBoundaryAnalysis.classLoadingOccurred()
            + ", injectionOccurred="
            + primitiveBoundaryAnalysis.injectionOccurred()
            + ", transformationOccurred="
            + primitiveBoundaryAnalysis.transformationOccurred()
            + ", patchingOccurred="
            + primitiveBoundaryAnalysis.patchingOccurred()
            + ", hookInstallationOccurred="
            + primitiveBoundaryAnalysis.hookInstallationOccurred()
            + ", runtimeDispatchOccurred="
            + primitiveBoundaryAnalysis.runtimeDispatchOccurred()
            + ", publicApiExposed="
            + primitiveBoundaryAnalysis.publicApiExposed()
            + ", javaModExecutionSandboxed="
            + primitiveBoundaryAnalysis.javaModExecutionSandboxed());
    if (!primitiveAnalysisOnly) {
      return buildBlocked(
          primitiveBoundaryAnalysis.minecraftVersion(),
          side,
          "Target-23 analysis-only invariants drifted from the Target-24 handoff boundary.",
          SteelHook02ContractGeneralizationStatus.CONTRACT_GENERALIZATION_BLOCKED,
          SteelHook02ContractGeneralizationNextDirection.RESTORE_TARGET_23_PRIMITIVE_BOUNDARY,
          TARGET_23_RESTORE_ACTION,
          findings);
    }

    if (patchPlan == null) {
      addFinding(
          findings,
          8,
          "Target-7 patch plan exists.",
          SteelHook02ContractGeneralizationFindingStatus.FAIL,
          true,
          "Target-24 requires the upstream Target-7 patch plan.",
          "No patch plan was provided.");
      return buildBlocked(
          primitiveBoundaryAnalysis.minecraftVersion(),
          side,
          "Target-7 patch plan is missing.",
          SteelHook02ContractGeneralizationStatus.UPSTREAM_GATE_BLOCKED,
          SteelHook02ContractGeneralizationNextDirection.RESTORE_TARGET_7_PATCH_PLAN,
          TARGET_7_RESTORE_ACTION,
          findings);
    }

    addFinding(
        findings,
        8,
        "Target-7 patch plan exists.",
        SteelHook02ContractGeneralizationFindingStatus.PASS,
        true,
        "Target-24 received the upstream Target-7 patch plan.",
        "Patch plan input is present.");
    boolean patchPlanGatePassed = patchPlan.gatePassed();
    addFinding(
        findings,
        9,
        "Target-7 patch plan gate passed.",
        patchPlanGatePassed
            ? SteelHook02ContractGeneralizationFindingStatus.PASS
            : SteelHook02ContractGeneralizationFindingStatus.FAIL,
        true,
        patchPlanGatePassed
            ? "Target-7 patch planning gate passed."
            : "Target-7 patch planning gate failed.",
        patchPlanGatePassed
            ? "Target-24 can compare the approved candidate to the source patch."
            : "Upstream gate failure reason: " + patchPlan.gateFailureReason());
    if (!patchPlanGatePassed) {
      return buildBlocked(
          primitiveBoundaryAnalysis.minecraftVersion(),
          side,
          "Target-7 patch plan gate failed.",
          SteelHook02ContractGeneralizationStatus.UPSTREAM_GATE_BLOCKED,
          SteelHook02ContractGeneralizationNextDirection.RESTORE_TARGET_7_PATCH_PLAN,
          TARGET_7_RESTORE_ACTION,
          findings);
    }

    boolean milestoneMatches = SOURCE_PATCH_PLAN_MILESTONE.equals(patchPlan.milestoneName());
    addFinding(
        findings,
        10,
        "Target-7 patch plan milestone name remains Target-7.",
        milestoneMatches
            ? SteelHook02ContractGeneralizationFindingStatus.PASS
            : SteelHook02ContractGeneralizationFindingStatus.FAIL,
        true,
        milestoneMatches
            ? "Target-24 is consuming the expected Target-7 patch plan."
            : "The upstream patch plan milestone drifted from Target-7.",
        "milestoneName=" + patchPlan.milestoneName());
    if (!milestoneMatches) {
      return buildBlocked(
          primitiveBoundaryAnalysis.minecraftVersion(),
          side,
          "Target-24 requires the Target-7 patch plan milestone.",
          SteelHook02ContractGeneralizationStatus.UPSTREAM_GATE_BLOCKED,
          SteelHook02ContractGeneralizationNextDirection.RESTORE_TARGET_7_PATCH_PLAN,
          TARGET_7_RESTORE_ACTION,
          findings);
    }

    boolean onePlannedPatch = patchPlan.plannedPatches().size() == 1;
    addFinding(
        findings,
        11,
        "Target-7 patch plan has exactly one planned patch.",
        onePlannedPatch
            ? SteelHook02ContractGeneralizationFindingStatus.PASS
            : SteelHook02ContractGeneralizationFindingStatus.FAIL,
        true,
        onePlannedPatch
            ? "Target-7 still exposes exactly one planned patch."
            : "The Target-7 patch plan is outside the bounded Target-24 proof shape.",
        "plannedPatchCount=" + patchPlan.plannedPatches().size());
    if (!onePlannedPatch) {
      return buildBlocked(
          primitiveBoundaryAnalysis.minecraftVersion(),
          side,
          "Target-24 requires exactly one Target-7 planned patch.",
          SteelHook02ContractGeneralizationStatus.UPSTREAM_GATE_BLOCKED,
          SteelHook02ContractGeneralizationNextDirection.RESTORE_TARGET_7_PATCH_PLAN,
          TARGET_7_RESTORE_ACTION,
          findings);
    }

    SteelHook02PrimitiveCandidate candidate = approvedCandidates.getFirst();
    MinecraftPlannedHookPatch patch = patchPlan.plannedPatches().getFirst();

    boolean candidateMatchesSourcePatchId = patch.id().equals(candidate.sourcePatchId());
    addFinding(
        findings,
        12,
        "Approved Target-23 candidate source patch id matches the Target-7 planned patch.",
        candidateMatchesSourcePatchId
            ? SteelHook02ContractGeneralizationFindingStatus.PASS
            : SteelHook02ContractGeneralizationFindingStatus.FAIL,
        true,
        candidateMatchesSourcePatchId
            ? "Candidate and source patch ids match."
            : "Candidate and source patch ids drifted.",
        "candidate.sourcePatchId=" + candidate.sourcePatchId() + ", patch.id=" + patch.id());
    if (!candidateMatchesSourcePatchId) {
      return buildBlocked(
          primitiveBoundaryAnalysis.minecraftVersion(),
          side,
          "Target-23 approved candidate no longer matches the Target-7 source patch id.",
          SteelHook02ContractGeneralizationStatus.CONTRACT_GENERALIZATION_BLOCKED,
          SteelHook02ContractGeneralizationNextDirection.RESTORE_TARGET_23_PRIMITIVE_BOUNDARY,
          TARGET_23_RESTORE_ACTION,
          findings);
    }

    boolean primitiveKindMatches =
        candidate.primitiveKind() == SteelHook02PrimitiveKind.METHOD_ENTRY_STATIC_DISPATCH
            && patch.kind() == MinecraftHookPatchKind.METHOD_ENTRY_STATIC_DISPATCH;
    boolean ownerMatches = patch.ownerInternalName().equals(candidate.ownerInternalName());
    boolean memberMatches = patch.memberName().equals(candidate.memberName());
    boolean descriptorMatches = patch.descriptor().equals(candidate.descriptor());
    boolean insertionOffsetMatches = patch.insertionOffset() == candidate.insertionOffset();
    boolean dispatcherMatches =
        patch.codeInsertion() != null
            && patch
                .codeInsertion()
                .dispatcherOwnerInternalName()
                .equals(candidate.dispatcherOwnerInternalName())
            && patch.codeInsertion().dispatcherMethodName().equals(candidate.dispatcherMethodName())
            && patch
                .codeInsertion()
                .dispatcherDescriptor()
                .equals(candidate.dispatcherDescriptor());
    boolean fixtureReadyMatches =
        patch.transformReadyForFixtureOnly() == candidate.fixtureTransformReady();
    boolean runtimeReadyMatches =
        patch.transformReadyForMinecraftRuntime() == candidate.minecraftRuntimeTransformReady();
    boolean shapeMatches =
        primitiveKindMatches
            && ownerMatches
            && memberMatches
            && descriptorMatches
            && insertionOffsetMatches
            && dispatcherMatches
            && fixtureReadyMatches
            && runtimeReadyMatches;
    addFinding(
        findings,
        13,
        "Approved Target-23 candidate matches the Target-7 planned patch shape.",
        shapeMatches
            ? SteelHook02ContractGeneralizationFindingStatus.PASS
            : SteelHook02ContractGeneralizationFindingStatus.FAIL,
        true,
        shapeMatches
            ? "Target-23 and Target-7 still describe the same approved primitive proof."
            : "Target-23 and Target-7 drifted apart.",
        "primitiveKindMatches="
            + primitiveKindMatches
            + ", ownerMatches="
            + ownerMatches
            + ", memberMatches="
            + memberMatches
            + ", descriptorMatches="
            + descriptorMatches
            + ", insertionOffsetMatches="
            + insertionOffsetMatches
            + ", dispatcherMatches="
            + dispatcherMatches
            + ", fixtureReadyMatches="
            + fixtureReadyMatches
            + ", runtimeReadyMatches="
            + runtimeReadyMatches);
    if (!shapeMatches) {
      return buildBlocked(
          primitiveBoundaryAnalysis.minecraftVersion(),
          side,
          "Target-23 approved candidate no longer matches the Target-7 planned patch shape.",
          SteelHook02ContractGeneralizationStatus.CONTRACT_GENERALIZATION_BLOCKED,
          SteelHook02ContractGeneralizationNextDirection.RESTORE_TARGET_23_PRIMITIVE_BOUNDARY,
          TARGET_23_RESTORE_ACTION,
          findings);
    }

    boolean targetShapeMatches =
        EXPECTED_OWNER.equals(patch.ownerInternalName())
            && EXPECTED_MEMBER.equals(patch.memberName())
            && EXPECTED_DESCRIPTOR.equals(patch.descriptor())
            && patch.insertionOffset() == 0;
    addFinding(
        findings,
        14,
        "Target remains net/minecraft/server/Main.main([Ljava/lang/String;)V at insertion offset 0.",
        targetShapeMatches
            ? SteelHook02ContractGeneralizationFindingStatus.PASS
            : SteelHook02ContractGeneralizationFindingStatus.FAIL,
        true,
        targetShapeMatches
            ? "Target-24 still generalizes the bounded Minecraft main entrypoint proof."
            : "The Target-7 patch target drifted from the approved Target-24 shape.",
        "owner="
            + patch.ownerInternalName()
            + ", member="
            + patch.memberName()
            + ", descriptor="
            + patch.descriptor()
            + ", insertionOffset="
            + patch.insertionOffset());
    if (!targetShapeMatches) {
      return buildBlocked(
          primitiveBoundaryAnalysis.minecraftVersion(),
          side,
          "Target-7 target shape drifted from the approved Target-24 contract.",
          SteelHook02ContractGeneralizationStatus.CONTRACT_GENERALIZATION_BLOCKED,
          SteelHook02ContractGeneralizationNextDirection.RESTORE_TARGET_7_PATCH_PLAN,
          TARGET_7_RESTORE_ACTION,
          findings);
    }

    boolean dispatcherShapeMatches =
        patch.codeInsertion() != null
            && EXPECTED_DISPATCHER_OWNER.equals(patch.codeInsertion().dispatcherOwnerInternalName())
            && EXPECTED_DISPATCHER_METHOD.equals(patch.codeInsertion().dispatcherMethodName())
            && EXPECTED_DISPATCHER_DESCRIPTOR.equals(patch.codeInsertion().dispatcherDescriptor());
    addFinding(
        findings,
        15,
        "Dispatcher remains SteelHookDispatcher.beforeMinecraftServerMain:()V.",
        dispatcherShapeMatches
            ? SteelHook02ContractGeneralizationFindingStatus.PASS
            : SteelHook02ContractGeneralizationFindingStatus.FAIL,
        true,
        dispatcherShapeMatches
            ? "Target-24 still generalizes the approved invokestatic dispatcher proof."
            : "The dispatcher target drifted from the approved Target-24 contract.",
        patch.codeInsertion() == null
            ? "Patch code insertion is missing."
            : "dispatcherOwner="
                + patch.codeInsertion().dispatcherOwnerInternalName()
                + ", dispatcherMethod="
                + patch.codeInsertion().dispatcherMethodName()
                + ", dispatcherDescriptor="
                + patch.codeInsertion().dispatcherDescriptor());
    if (!dispatcherShapeMatches) {
      return buildBlocked(
          primitiveBoundaryAnalysis.minecraftVersion(),
          side,
          "Target-7 dispatcher shape drifted from the approved Target-24 contract.",
          SteelHook02ContractGeneralizationStatus.CONTRACT_GENERALIZATION_BLOCKED,
          SteelHook02ContractGeneralizationNextDirection.RESTORE_TARGET_7_PATCH_PLAN,
          TARGET_7_RESTORE_ACTION,
          findings);
    }

    boolean patchModeMatches =
        patch.mode() == MinecraftHookPatchMode.DRY_RUN_STATIC_DISPATCH_INVOKESTATIC;
    boolean patchEligibilityMatches =
        patch.patchEligibility() == MinecraftHookPatchEligibility.FIXTURE_ONLY_FUTURE_TRANSFORM;
    boolean runtimeReadyFalse =
        !patch.transformReadyForMinecraftRuntime()
            && !patchPlan.transformReadyForMinecraftRuntime();
    boolean planAnalysisOnly =
        !patchPlan.injectionOccurred()
            && !patchPlan.transformationOccurred()
            && !patchPlan.patchingOccurred()
            && !patchPlan.bytecodeModified();
    boolean noAgentOrMixin = !patchPlan.javaAgentUsed() && !patchPlan.mixinUsed();
    boolean noApiOrSandbox =
        !patchPlan.publicApiExposed() && !patchPlan.javaModExecutionSandboxed();
    boolean invariantsHold =
        patch.kind() == MinecraftHookPatchKind.METHOD_ENTRY_STATIC_DISPATCH
            && patchModeMatches
            && patchEligibilityMatches
            && runtimeReadyFalse
            && planAnalysisOnly
            && noAgentOrMixin
            && noApiOrSandbox;
    addFinding(
        findings,
        16,
        "Target-24 boundary invariants remain analysis-only and runtime-blocked.",
        invariantsHold
            ? SteelHook02ContractGeneralizationFindingStatus.PASS
            : SteelHook02ContractGeneralizationFindingStatus.FAIL,
        true,
        invariantsHold
            ? "Target-24 preserves the analysis-only SteelHook 0.2 boundary."
            : "Target-24 invariants were violated by upstream patch-plan state.",
        "patchMode="
            + patch.mode()
            + ", patchEligibility="
            + patch.patchEligibility()
            + ", patch.transformReadyForMinecraftRuntime="
            + patch.transformReadyForMinecraftRuntime()
            + ", plan.transformReadyForMinecraftRuntime="
            + patchPlan.transformReadyForMinecraftRuntime()
            + ", injectionOccurred="
            + patchPlan.injectionOccurred()
            + ", transformationOccurred="
            + patchPlan.transformationOccurred()
            + ", patchingOccurred="
            + patchPlan.patchingOccurred()
            + ", bytecodeModified="
            + patchPlan.bytecodeModified()
            + ", javaAgentUsed="
            + patchPlan.javaAgentUsed()
            + ", mixinUsed="
            + patchPlan.mixinUsed()
            + ", publicApiExposed="
            + patchPlan.publicApiExposed()
            + ", javaModExecutionSandboxed="
            + patchPlan.javaModExecutionSandboxed());
    if (!invariantsHold) {
      return buildBlocked(
          primitiveBoundaryAnalysis.minecraftVersion(),
          side,
          "Target-24 invariants were violated by the Target-7 patch plan.",
          SteelHook02ContractGeneralizationStatus.CONTRACT_GENERALIZATION_BLOCKED,
          SteelHook02ContractGeneralizationNextDirection.RESTORE_TARGET_7_PATCH_PLAN,
          TARGET_7_RESTORE_ACTION,
          findings);
    }

    SteelHook02TargetDescriptor targetDescriptor =
        new SteelHook02TargetDescriptor(
            TARGET_DESCRIPTOR_ID,
            EXPECTED_OWNER,
            EXPECTED_BINARY_NAME,
            EXPECTED_CLASS_ENTRY_NAME,
            EXPECTED_MEMBER,
            EXPECTED_DESCRIPTOR,
            side,
            primitiveBoundaryAnalysis.minecraftVersion(),
            patch.sourceContractId(),
            patch.sourcePlacementId(),
            patch.id(),
            patch.insertionOffset(),
            true);
    SteelHook02DispatcherDescriptor dispatcherDescriptor =
        new SteelHook02DispatcherDescriptor(
            DISPATCHER_DESCRIPTOR_ID,
            EXPECTED_DISPATCHER_OWNER,
            EXPECTED_DISPATCHER_BINARY_NAME,
            EXPECTED_DISPATCHER_METHOD,
            EXPECTED_DISPATCHER_DESCRIPTOR,
            "invokestatic",
            "b8",
            3,
            0,
            0,
            true,
            false);
    SteelHook02PrimitiveContract primitiveContract =
        new SteelHook02PrimitiveContract(
            PRIMITIVE_CONTRACT_ID,
            SteelHook02PrimitiveKind.METHOD_ENTRY_STATIC_DISPATCH,
            candidate.id(),
            targetDescriptor.id(),
            dispatcherDescriptor.id(),
            MinecraftHookPatchKind.METHOD_ENTRY_STATIC_DISPATCH,
            MinecraftHookPatchMode.STEELHOOK_0_2_CONTRACT_GENERALIZED_STATIC_DISPATCH_INVOKESTATIC,
            MinecraftHookPatchEligibility.STEELHOOK_0_2_CONTRACT_READY_RUNTIME_CANDIDATE,
            "METHOD_ENTRY_OFFSET_ZERO_ONLY",
            true,
            patch.transformReadyForFixtureOnly(),
            false,
            false,
            false);
    SteelHook02GeneralizedPatchPlan generalizedPatchPlan =
        new SteelHook02GeneralizedPatchPlan(
            GENERALIZED_PATCH_PLAN_ID,
            SOURCE_PATCH_PLAN_MILESTONE,
            patch.id(),
            candidate.id(),
            targetDescriptor.id(),
            dispatcherDescriptor.id(),
            MinecraftHookPatchKind.METHOD_ENTRY_STATIC_DISPATCH,
            MinecraftHookPatchMode.STEELHOOK_0_2_CONTRACT_GENERALIZED_STATIC_DISPATCH_INVOKESTATIC,
            MinecraftHookPatchEligibility.STEELHOOK_0_2_CONTRACT_READY_RUNTIME_CANDIDATE,
            patch.requiredConstantPoolEntries().size(),
            patch.constantPoolRewriteRequired(),
            patch.codeRewriteRequired(),
            patch.maxStackRewriteRequired(),
            patch.maxLocalsRewriteRequired(),
            patch.exceptionTableRewriteRequired(),
            patch.stackMapTableRewriteRequired(),
            patch.nestedCodeAttributeRewriteRequired(),
            patch.lineNumberTableRewriteRequired(),
            patch.localVariableTableRewriteRequired(),
            patch.branchOffsetRewriteRequired(),
            patch.switchOffsetRewriteRequired(),
            patch.transformReadyForFixtureOnly(),
            false,
            true,
            false,
            List.of(
                "Target-24 generalizes the approved Target-23 method-entry static-dispatch candidate into bounded SteelHook 0.2 descriptors.",
                "This report is analysis-only and does not transform class bytes or install hooks.",
                "Runtime-safe transformer extraction remains blocked until Target-25."));

    addFinding(
        findings,
        17,
        "Target-24 generalized descriptors were created without enabling runtime transformation.",
        SteelHook02ContractGeneralizationFindingStatus.PASS,
        true,
        "Target-24 produced bounded target, dispatcher, primitive contract, and generalized patch-plan descriptors.",
        "eligibleForTarget25TransformerExtraction=true, eligibleForTarget26RuntimeTransformation=false");

    return new SteelHook02ContractGeneralizationAnalysis(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        STEELHOOK_VERSION,
        primitiveBoundaryAnalysis.minecraftVersion(),
        side,
        SOURCE_PATCH_PLAN_MILESTONE,
        SOURCE_PRIMITIVE_BOUNDARY_MILESTONE,
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
        true,
        true,
        false,
        true,
        false,
        true,
        null,
        SteelHook02ContractGeneralizationStatus.CONTRACT_GENERALIZATION_READY,
        SteelHook02ContractGeneralizationNextDirection
            .MOVE_TO_TARGET_25_RUNTIME_SAFE_METHOD_ENTRY_TRANSFORMER,
        TARGET_25_ACTION,
        targetDescriptor,
        dispatcherDescriptor,
        primitiveContract,
        generalizedPatchPlan,
        findings);
  }

  private SteelHook02ContractGeneralizationAnalysis buildBlocked(
      String minecraftVersion,
      MinecraftSide side,
      String gateFailureReason,
      SteelHook02ContractGeneralizationStatus status,
      SteelHook02ContractGeneralizationNextDirection nextDirection,
      String nextRecommendedAction,
      List<SteelHook02ContractGeneralizationFinding> findings) {
    return new SteelHook02ContractGeneralizationAnalysis(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        STEELHOOK_VERSION,
        minecraftVersion,
        side,
        SOURCE_PATCH_PLAN_MILESTONE,
        SOURCE_PRIMITIVE_BOUNDARY_MILESTONE,
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
        gateFailureReason,
        status,
        nextDirection,
        nextRecommendedAction,
        null,
        null,
        null,
        null,
        findings);
  }

  private static void addFinding(
      List<SteelHook02ContractGeneralizationFinding> findings,
      int sequence,
      String checkName,
      SteelHook02ContractGeneralizationFindingStatus status,
      boolean blocking,
      String summary,
      String notes) {
    findings.add(
        new SteelHook02ContractGeneralizationFinding(
            "target-24.steelhook-0-2.contract-generalization.finding.%03d".formatted(sequence),
            checkName,
            status,
            blocking,
            summary,
            notes));
  }
}
