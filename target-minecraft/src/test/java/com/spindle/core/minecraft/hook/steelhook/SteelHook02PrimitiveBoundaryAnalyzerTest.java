package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchEligibility;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchKind;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchMode;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchPlan;
import com.spindle.core.minecraft.hook.patch.MinecraftPatchCodeInsertion;
import com.spindle.core.minecraft.hook.patch.MinecraftPatchConstantPoolRequirement;
import com.spindle.core.minecraft.hook.patch.MinecraftPlannedHookPatch;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SteelHook02PrimitiveBoundaryAnalyzerTest {
  private final SteelHook02PrimitiveBoundaryAnalyzer analyzer =
      new SteelHook02PrimitiveBoundaryAnalyzer();

  @Test
  void validTargetSevenMethodEntryStaticDispatchPatchPlanPassesTarget23() {
    SteelHook02PrimitiveBoundaryAnalysis analysis = analyzer.analyze(validPatchPlan());

    assertTrue(analysis.gatePassed());
    assertNull(analysis.gateFailureReason());
    assertEquals(
        SteelHook02PrimitiveBoundaryStatus.PRIMITIVE_BOUNDARY_SELECTED, analysis.boundaryStatus());
    assertEquals(
        SteelHook02NextDirection.MOVE_TO_TARGET_24_CONTRACT_AND_PATCH_PLAN_GENERALIZATION,
        analysis.nextDirection());
    assertEquals(1, analysis.candidates().size());

    SteelHook02PrimitiveCandidate candidate = analysis.candidates().getFirst();
    assertEquals(
        SteelHook02PrimitiveCandidateStatus.APPROVED_FOR_TARGET_24_CONTRACT_GENERALIZATION,
        candidate.candidateStatus());
    assertEquals(SteelHook02PrimitiveKind.METHOD_ENTRY_STATIC_DISPATCH, candidate.primitiveKind());
    assertTrue(candidate.fixtureTransformReady());
    assertFalse(candidate.minecraftRuntimeTransformReady());
    assertTrue(candidate.eligibleForTarget24ContractGeneralization());
    assertTrue(candidate.eligibleForTarget25TransformerExtraction());
    assertFalse(candidate.eligibleForTarget26RuntimeTransformation());
  }

  @Test
  void missingPatchPlanFailsWithUpstreamGateBlocked() {
    SteelHook02PrimitiveBoundaryAnalysis analysis = analyzer.analyze(null);

    assertFalse(analysis.gatePassed());
    assertEquals(
        SteelHook02PrimitiveBoundaryStatus.UPSTREAM_GATE_BLOCKED, analysis.boundaryStatus());
  }

  @Test
  void failedUpstreamPatchPlanFailsWithUpstreamGateBlocked() {
    SteelHook02PrimitiveBoundaryAnalysis analysis =
        analyzer.analyze(
            planBuilder(validPatchPlan())
                .gatePassed(false)
                .gateFailureReason("Target-7 gate failed.")
                .build());

    assertFalse(analysis.gatePassed());
    assertEquals(
        SteelHook02PrimitiveBoundaryStatus.UPSTREAM_GATE_BLOCKED, analysis.boundaryStatus());
  }

  @Test
  void zeroPlannedPatchesRequiresMoreUpstreamEvidence() {
    SteelHook02PrimitiveBoundaryAnalysis analysis =
        analyzer.analyze(
            planBuilder(validPatchPlan()).plannedPatchCount(0).plannedPatches(List.of()).build());

    assertFalse(analysis.gatePassed());
    assertEquals(
        SteelHook02PrimitiveBoundaryStatus.MORE_UPSTREAM_EVIDENCE_REQUIRED,
        analysis.boundaryStatus());
    assertTrue(analysis.candidates().isEmpty());
  }

  @Test
  void moreThanOnePlannedPatchBlocksGate() {
    MinecraftPlannedHookPatch patch = validPatch();
    SteelHook02PrimitiveBoundaryAnalysis analysis =
        analyzer.analyze(
            planBuilder(validPatchPlan())
                .plannedPatchCount(2)
                .plannedPatches(List.of(patch, patch))
                .build());

    assertFalse(analysis.gatePassed());
    assertEquals(
        SteelHook02PrimitiveBoundaryStatus.PRIMITIVE_BOUNDARY_BLOCKED, analysis.boundaryStatus());
  }

  @Test
  void unsupportedPatchKindIsRejected() {
    SteelHook02PrimitiveBoundaryAnalysis analysis =
        analyzer.analyze(
            planBuilder(validPatchPlan())
                .plannedPatches(List.of(patchBuilder(validPatch()).kind(null).build()))
                .build());

    assertFalse(analysis.gatePassed());
    assertEquals(
        SteelHook02PrimitiveCandidateStatus.DEFERRED_UNSUPPORTED_PRIMITIVE,
        analysis.candidates().getFirst().candidateStatus());
  }

  @Test
  void wrongTargetOrDispatcherShapeIsRejected() {
    SteelHook02PrimitiveBoundaryAnalysis analysis =
        analyzer.analyze(
            planBuilder(validPatchPlan())
                .plannedPatches(
                    List.of(
                        patchBuilder(validPatch())
                            .ownerInternalName("net/minecraft/server/Bootstrap")
                            .build()))
                .build());

    assertFalse(analysis.gatePassed());
    assertEquals(
        SteelHook02PrimitiveCandidateStatus.REJECTED_UNSUPPORTED_SHAPE,
        analysis.candidates().getFirst().candidateStatus());
  }

  @Test
  void wrongMemberIsRejected() {
    assertRejectedUnsupportedShape(patchBuilder(validPatch()).memberName("bootstrap").build());
  }

  @Test
  void wrongDescriptorIsRejected() {
    assertRejectedUnsupportedShape(patchBuilder(validPatch()).descriptor("()V").build());
  }

  @Test
  void wrongInsertionOffsetIsRejected() {
    assertRejectedUnsupportedShape(patchBuilder(validPatch()).insertionOffset(4).build());
  }

  @Test
  void wrongDispatcherOwnerIsRejected() {
    assertRejectedUnsupportedShape(
        patchBuilder(validPatch())
            .dispatcherOwnerInternalName("com/example/Dispatcher")
            .build());
  }

  @Test
  void wrongDispatcherMethodIsRejected() {
    assertRejectedUnsupportedShape(
        patchBuilder(validPatch()).dispatcherMethodName("beforeMain").build());
  }

  @Test
  void wrongDispatcherDescriptorIsRejected() {
    assertRejectedUnsupportedShape(
        patchBuilder(validPatch()).dispatcherDescriptor("(Ljava/lang/String;)V").build());
  }

  @Test
  void runtimeReadinessTrueFailsBoundary() {
    SteelHook02PrimitiveBoundaryAnalysis analysis =
        analyzer.analyze(
            planBuilder(validPatchPlan())
                .transformReadyForMinecraftRuntime(true)
                .plannedPatches(
                    List.of(
                        patchBuilder(validPatch()).transformReadyForMinecraftRuntime(true).build()))
                .build());

    assertFalse(analysis.gatePassed());
    assertEquals(
        SteelHook02PrimitiveBoundaryStatus.PRIMITIVE_BOUNDARY_BLOCKED, analysis.boundaryStatus());
  }

  @Test
  void publicApiOrSandboxClaimsFailBoundary() {
    SteelHook02PrimitiveBoundaryAnalysis apiAnalysis =
        analyzer.analyze(planBuilder(validPatchPlan()).publicApiExposed(true).build());
    SteelHook02PrimitiveBoundaryAnalysis sandboxAnalysis =
        analyzer.analyze(planBuilder(validPatchPlan()).javaModExecutionSandboxed(true).build());

    assertFalse(apiAnalysis.gatePassed());
    assertFalse(sandboxAnalysis.gatePassed());
  }

  @Test
  void bytecodeModifiedTrueFailsBoundary() {
    SteelHook02PrimitiveBoundaryAnalysis analysis =
        analyzer.analyze(planBuilder(validPatchPlan()).bytecodeModified(true).build());

    assertFalse(analysis.gatePassed());
    assertEquals(
        SteelHook02PrimitiveBoundaryStatus.PRIMITIVE_BOUNDARY_BLOCKED, analysis.boundaryStatus());
  }

  private void assertRejectedUnsupportedShape(MinecraftPlannedHookPatch patch) {
    SteelHook02PrimitiveBoundaryAnalysis analysis =
        analyzer.analyze(planBuilder(validPatchPlan()).plannedPatches(List.of(patch)).build());

    assertFalse(analysis.gatePassed());
    assertEquals(
        SteelHook02PrimitiveCandidateStatus.REJECTED_UNSUPPORTED_SHAPE,
        analysis.candidates().getFirst().candidateStatus());
  }

  private MinecraftHookPatchPlan validPatchPlan() {
    return new MinecraftHookPatchPlan(
        1,
        "Target-7",
        "minecraft",
        "26.1.2",
        "server",
        "minecraft-26.1.2-server-known-symbols",
        true,
        0,
        "net.minecraft.server.Main",
        true,
        null,
        true,
        true,
        1,
        MinecraftHookPatchEligibility.FIXTURE_ONLY_FUTURE_TRANSFORM,
        "target-5.minecraft.server.main.method-entry-placement",
        1,
        "Target-6",
        "net/minecraft/server/Main",
        "main",
        "([Ljava/lang/String;)V",
        31,
        34,
        3,
        "abc123",
        0,
        true,
        0,
        "b8 ?? ??",
        List.of(
            new MinecraftPatchConstantPoolRequirement(
                "Utf8", "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher")),
        true,
        true,
        false,
        false,
        true,
        true,
        true,
        true,
        true,
        true,
        true,
        true,
        false,
        true,
        true,
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
        null,
        null,
        null,
        null,
        null,
        List.of(validPatch()));
  }

  private MinecraftPlannedHookPatch validPatch() {
    return new MinecraftPlannedHookPatch(
        "target-7.minecraft.server.main.method-entry-dispatch-patch",
        "target-5.minecraft.server.main.method-entry-placement",
        "minecraft.26_1_2.server.main.entrypoint",
        "Target-6",
        "minecraft-26.1.2-server-known-symbols",
        MinecraftHookPatchKind.METHOD_ENTRY_STATIC_DISPATCH,
        MinecraftHookPatchMode.DRY_RUN_STATIC_DISPATCH_INVOKESTATIC,
        MinecraftHookPatchEligibility.FIXTURE_ONLY_FUTURE_TRANSFORM,
        "net/minecraft/server/Main",
        "main",
        "([Ljava/lang/String;)V",
        0,
        true,
        new MinecraftPatchCodeInsertion(
            "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher",
            "beforeMinecraftServerMain",
            "()V",
            "invokestatic",
            "b8",
            3,
            0,
            0,
            "b8 ?? ??"),
        List.of(
            new MinecraftPatchConstantPoolRequirement(
                "Utf8", "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher")),
        true,
        true,
        false,
        false,
        true,
        true,
        true,
        true,
        true,
        true,
        true,
        null,
        null,
        null,
        null,
        null,
        true,
        false);
  }

  private PatchPlanBuilder planBuilder(MinecraftHookPatchPlan base) {
    return new PatchPlanBuilder(base);
  }

  private PlannedPatchBuilder patchBuilder(MinecraftPlannedHookPatch base) {
    return new PlannedPatchBuilder(base);
  }

  private static final class PatchPlanBuilder {
    private final MinecraftHookPatchPlan base;
    private boolean gatePassed;
    private String gateFailureReason;
    private int plannedPatchCount;
    private List<MinecraftPlannedHookPatch> plannedPatches;
    private boolean transformReadyForMinecraftRuntime;
    private boolean publicApiExposed;
    private boolean javaModExecutionSandboxed;
    private boolean bytecodeModified;

    private PatchPlanBuilder(MinecraftHookPatchPlan base) {
      this.base = base;
      gatePassed = base.gatePassed();
      gateFailureReason = base.gateFailureReason();
      plannedPatchCount = base.plannedPatchCount();
      plannedPatches = new ArrayList<>(base.plannedPatches());
      transformReadyForMinecraftRuntime = base.transformReadyForMinecraftRuntime();
      publicApiExposed = base.publicApiExposed();
      javaModExecutionSandboxed = base.javaModExecutionSandboxed();
      bytecodeModified = base.bytecodeModified();
    }

    private PatchPlanBuilder gatePassed(boolean value) {
      gatePassed = value;
      return this;
    }

    private PatchPlanBuilder gateFailureReason(String value) {
      gateFailureReason = value;
      return this;
    }

    private PatchPlanBuilder plannedPatchCount(int value) {
      plannedPatchCount = value;
      return this;
    }

    private PatchPlanBuilder plannedPatches(List<MinecraftPlannedHookPatch> value) {
      plannedPatches = new ArrayList<>(value);
      return this;
    }

    private PatchPlanBuilder transformReadyForMinecraftRuntime(boolean value) {
      transformReadyForMinecraftRuntime = value;
      return this;
    }

    private PatchPlanBuilder publicApiExposed(boolean value) {
      publicApiExposed = value;
      return this;
    }

    private PatchPlanBuilder javaModExecutionSandboxed(boolean value) {
      javaModExecutionSandboxed = value;
      return this;
    }

    private PatchPlanBuilder bytecodeModified(boolean value) {
      bytecodeModified = value;
      return this;
    }

    private MinecraftHookPatchPlan build() {
      return new MinecraftHookPatchPlan(
          base.schema(),
          base.milestoneName(),
          base.target(),
          base.minecraftVersion(),
          base.side(),
          base.catalogId(),
          base.sourceContractValidationPassed(),
          base.sourceContractErrorCount(),
          base.minecraftMainClass(),
          gatePassed,
          gateFailureReason,
          base.patchPlanningSucceeded(),
          !plannedPatches.isEmpty(),
          plannedPatchCount,
          base.patchEligibility(),
          base.selectedPlacementId(),
          base.selectedBytecodeAnalysisSchema(),
          base.selectedBytecodeAnalysisMilestone(),
          base.targetClass(),
          base.targetMethod(),
          base.targetDescriptor(),
          base.originalCodeLength(),
          base.plannedCodeLength(),
          base.codeLengthDelta(),
          base.originalCodeSha256(),
          base.insertionOffset(),
          base.insertionInstructionBoundary(),
          base.insertBeforeOriginalInstructionOffset(),
          base.insertedInstructionHex(),
          base.requiredConstantPoolEntries(),
          base.constantPoolRewriteRequired(),
          base.codeRewriteRequired(),
          base.maxStackRewriteRequired(),
          base.maxLocalsRewriteRequired(),
          base.exceptionTableRewriteRequired(),
          base.stackMapTableRewriteRequired(),
          base.nestedCodeAttributeRewriteRequired(),
          base.lineNumberTableRewriteRequired(),
          base.localVariableTableRewriteRequired(),
          base.branchOffsetRewriteRequired(),
          base.switchOffsetRewriteRequired(),
          base.transformReadyForFixtureOnly(),
          transformReadyForMinecraftRuntime,
          base.codeAttributeParsed(),
          base.instructionInspectionOccurred(),
          base.patchPlanningOccurred(),
          base.injectionOccurred(),
          base.transformationOccurred(),
          base.patchingOccurred(),
          bytecodeModified,
          base.javaAgentUsed(),
          base.mixinUsed(),
          base.remappingOccurred(),
          publicApiExposed,
          javaModExecutionSandboxed,
          base.branchTargetAdjustmentSummary(),
          base.switchTargetAdjustmentSummary(),
          base.exceptionTableImpact(),
          base.stackMapImpact(),
          base.nestedAttributeImpact(),
          plannedPatches);
    }
  }

  private static final class PlannedPatchBuilder {
    private final MinecraftPlannedHookPatch base;
    private MinecraftHookPatchKind kind;
    private String ownerInternalName;
    private String memberName;
    private String descriptor;
    private int insertionOffset;
    private String dispatcherOwnerInternalName;
    private String dispatcherMethodName;
    private String dispatcherDescriptor;
    private boolean transformReadyForMinecraftRuntime;

    private PlannedPatchBuilder(MinecraftPlannedHookPatch base) {
      this.base = base;
      kind = base.kind();
      ownerInternalName = base.ownerInternalName();
      memberName = base.memberName();
      descriptor = base.descriptor();
      insertionOffset = base.insertionOffset();
      dispatcherOwnerInternalName = base.codeInsertion().dispatcherOwnerInternalName();
      dispatcherMethodName = base.codeInsertion().dispatcherMethodName();
      dispatcherDescriptor = base.codeInsertion().dispatcherDescriptor();
      transformReadyForMinecraftRuntime = base.transformReadyForMinecraftRuntime();
    }

    private PlannedPatchBuilder kind(MinecraftHookPatchKind value) {
      kind = value;
      return this;
    }

    private PlannedPatchBuilder ownerInternalName(String value) {
      ownerInternalName = value;
      return this;
    }

    private PlannedPatchBuilder memberName(String value) {
      memberName = value;
      return this;
    }

    private PlannedPatchBuilder descriptor(String value) {
      descriptor = value;
      return this;
    }

    private PlannedPatchBuilder insertionOffset(int value) {
      insertionOffset = value;
      return this;
    }

    private PlannedPatchBuilder dispatcherOwnerInternalName(String value) {
      dispatcherOwnerInternalName = value;
      return this;
    }

    private PlannedPatchBuilder dispatcherMethodName(String value) {
      dispatcherMethodName = value;
      return this;
    }

    private PlannedPatchBuilder dispatcherDescriptor(String value) {
      dispatcherDescriptor = value;
      return this;
    }

    private PlannedPatchBuilder transformReadyForMinecraftRuntime(boolean value) {
      transformReadyForMinecraftRuntime = value;
      return this;
    }

    private MinecraftPlannedHookPatch build() {
      return new MinecraftPlannedHookPatch(
          base.id(),
          base.sourcePlacementId(),
          base.sourceContractId(),
          base.sourceBytecodeAnalysisMilestone(),
          base.catalogId(),
          kind,
          base.mode(),
          base.patchEligibility(),
          ownerInternalName,
          memberName,
          descriptor,
          insertionOffset,
          base.required(),
          new MinecraftPatchCodeInsertion(
              dispatcherOwnerInternalName,
              dispatcherMethodName,
              dispatcherDescriptor,
              base.codeInsertion().plannedOpcode(),
              base.codeInsertion().plannedOpcodeHex(),
              base.codeInsertion().plannedInstructionLength(),
              base.codeInsertion().plannedStackDelta(),
              base.codeInsertion().requiredMaxStackIncrease(),
              base.codeInsertion().insertedInstructionHex()),
          base.requiredConstantPoolEntries(),
          base.constantPoolRewriteRequired(),
          base.codeRewriteRequired(),
          base.maxStackRewriteRequired(),
          base.maxLocalsRewriteRequired(),
          base.exceptionTableRewriteRequired(),
          base.stackMapTableRewriteRequired(),
          base.nestedCodeAttributeRewriteRequired(),
          base.lineNumberTableRewriteRequired(),
          base.localVariableTableRewriteRequired(),
          base.branchOffsetRewriteRequired(),
          base.switchOffsetRewriteRequired(),
          base.branchTargetAdjustmentSummary(),
          base.switchTargetAdjustmentSummary(),
          base.exceptionTableImpact(),
          base.stackMapImpact(),
          base.nestedAttributeImpact(),
          base.transformReadyForFixtureOnly(),
          transformReadyForMinecraftRuntime);
    }
  }
}
