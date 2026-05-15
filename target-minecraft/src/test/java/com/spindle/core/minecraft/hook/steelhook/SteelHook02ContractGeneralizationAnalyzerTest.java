package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftSide;
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

class SteelHook02ContractGeneralizationAnalyzerTest {
  private final SteelHook02ContractGeneralizationAnalyzer analyzer =
      new SteelHook02ContractGeneralizationAnalyzer();

  @Test
  void validTarget23AnalysisPlusTarget7PatchPlanPassesTarget24() {
    SteelHook02ContractGeneralizationAnalysis analysis =
        analyzer.analyze(validPrimitiveBoundaryAnalysis(), validPatchPlan());

    assertTrue(analysis.gatePassed());
    assertNull(analysis.gateFailureReason());
    assertEquals("Target-24", analysis.milestoneName());
    assertEquals(
        SteelHook02ContractGeneralizationStatus.CONTRACT_GENERALIZATION_READY, analysis.status());
    assertEquals(
        SteelHook02ContractGeneralizationNextDirection
            .MOVE_TO_TARGET_25_RUNTIME_SAFE_METHOD_ENTRY_TRANSFORMER,
        analysis.nextDirection());
    assertNotNull(analysis.targetDescriptor());
    assertNotNull(analysis.dispatcherDescriptor());
    assertNotNull(analysis.primitiveContract());
    assertNotNull(analysis.generalizedPatchPlan());

    SteelHook02TargetDescriptor targetDescriptor = analysis.targetDescriptor();
    assertEquals("net/minecraft/server/Main", targetDescriptor.ownerInternalName());
    assertEquals("net.minecraft.server.Main", targetDescriptor.binaryName());
    assertEquals("net/minecraft/server/Main.class", targetDescriptor.classEntryName());
    assertEquals("main", targetDescriptor.memberName());
    assertEquals("([Ljava/lang/String;)V", targetDescriptor.descriptor());
    assertEquals(MinecraftSide.SERVER, targetDescriptor.side());
    assertEquals(0, targetDescriptor.insertionOffset());

    SteelHook02DispatcherDescriptor dispatcherDescriptor = analysis.dispatcherDescriptor();
    assertEquals(
        "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher",
        dispatcherDescriptor.ownerInternalName());
    assertEquals("beforeMinecraftServerMain", dispatcherDescriptor.methodName());
    assertEquals("()V", dispatcherDescriptor.descriptor());
    assertEquals("invokestatic", dispatcherDescriptor.opcodeMnemonic());
    assertEquals("b8", dispatcherDescriptor.opcodeHex());
    assertEquals(3, dispatcherDescriptor.instructionLength());
    assertEquals(0, dispatcherDescriptor.maxStackDelta());
    assertEquals(0, dispatcherDescriptor.maxLocalsDelta());

    SteelHook02PrimitiveContract primitiveContract = analysis.primitiveContract();
    assertEquals(
        SteelHook02PrimitiveKind.METHOD_ENTRY_STATIC_DISPATCH, primitiveContract.primitiveKind());
    assertEquals(
        MinecraftHookPatchMode.STEELHOOK_0_2_CONTRACT_GENERALIZED_STATIC_DISPATCH_INVOKESTATIC,
        primitiveContract.patchMode());
    assertEquals(
        MinecraftHookPatchEligibility.STEELHOOK_0_2_CONTRACT_READY_RUNTIME_CANDIDATE,
        primitiveContract.patchEligibility());
    assertFalse(primitiveContract.minecraftRuntimeTransformReady());

    SteelHook02GeneralizedPatchPlan generalizedPatchPlan = analysis.generalizedPatchPlan();
    assertTrue(generalizedPatchPlan.constantPoolRewriteRequired());
    assertTrue(generalizedPatchPlan.codeRewriteRequired());
    assertFalse(generalizedPatchPlan.maxStackRewriteRequired());
    assertFalse(generalizedPatchPlan.maxLocalsRewriteRequired());
    assertTrue(generalizedPatchPlan.exceptionTableRewriteRequired());
    assertTrue(generalizedPatchPlan.stackMapTableRewriteRequired());
    assertTrue(generalizedPatchPlan.nestedCodeAttributeRewriteRequired());
    assertTrue(generalizedPatchPlan.lineNumberTableRewriteRequired());
    assertTrue(generalizedPatchPlan.localVariableTableRewriteRequired());
    assertTrue(generalizedPatchPlan.branchOffsetRewriteRequired());
    assertTrue(generalizedPatchPlan.switchOffsetRewriteRequired());
    assertTrue(generalizedPatchPlan.eligibleForTarget25TransformerExtraction());
    assertFalse(generalizedPatchPlan.eligibleForTarget26RuntimeTransformation());
  }

  @Test
  void nullTarget23AnalysisFailsWithUpstreamGateBlocked() {
    SteelHook02ContractGeneralizationAnalysis analysis = analyzer.analyze(null, validPatchPlan());

    assertFalse(analysis.gatePassed());
    assertEquals(SteelHook02ContractGeneralizationStatus.UPSTREAM_GATE_BLOCKED, analysis.status());
  }

  @Test
  void failedTarget23AnalysisFailsWithUpstreamGateBlocked() {
    SteelHook02ContractGeneralizationAnalysis analysis =
        analyzer.analyze(
            primitiveBoundaryBuilder(validPrimitiveBoundaryAnalysis())
                .gatePassed(false)
                .gateFailureReason("Target-23 failed.")
                .build(),
            validPatchPlan());

    assertFalse(analysis.gatePassed());
    assertEquals(SteelHook02ContractGeneralizationStatus.UPSTREAM_GATE_BLOCKED, analysis.status());
  }

  @Test
  void zeroOrMultipleApprovedCandidatesFailTarget24() {
    SteelHook02ContractGeneralizationAnalysis zeroApproved =
        analyzer.analyze(
            primitiveBoundaryBuilder(validPrimitiveBoundaryAnalysis())
                .approvedCandidateCount(0)
                .candidates(
                    List.of(
                        candidateBuilder(validPrimitiveCandidate())
                            .candidateStatus(
                                SteelHook02PrimitiveCandidateStatus.REJECTED_UNSUPPORTED_SHAPE)
                            .build()))
                .build(),
            validPatchPlan());
    SteelHook02ContractGeneralizationAnalysis multipleApproved =
        analyzer.analyze(
            primitiveBoundaryBuilder(validPrimitiveBoundaryAnalysis())
                .approvedCandidateCount(2)
                .candidates(List.of(validPrimitiveCandidate(), validPrimitiveCandidate()))
                .build(),
            validPatchPlan());

    assertFalse(zeroApproved.gatePassed());
    assertFalse(multipleApproved.gatePassed());
    assertEquals(
        SteelHook02ContractGeneralizationStatus.UPSTREAM_GATE_BLOCKED, zeroApproved.status());
    assertEquals(
        SteelHook02ContractGeneralizationStatus.UPSTREAM_GATE_BLOCKED, multipleApproved.status());
  }

  @Test
  void candidatePatchIdMismatchFails() {
    SteelHook02ContractGeneralizationAnalysis analysis =
        analyzer.analyze(
            primitiveBoundaryBuilder(validPrimitiveBoundaryAnalysis())
                .candidates(
                    List.of(
                        candidateBuilder(validPrimitiveCandidate()).sourcePatchId("drift").build()))
                .build(),
            validPatchPlan());

    assertFalse(analysis.gatePassed());
    assertEquals(
        SteelHook02ContractGeneralizationStatus.CONTRACT_GENERALIZATION_BLOCKED, analysis.status());
  }

  @Test
  void candidateShapeMismatchFails() {
    assertShapeMismatch(
        candidateBuilder(validPrimitiveCandidate())
            .ownerInternalName("net/minecraft/server/Bootstrap")
            .build());
    assertShapeMismatch(
        candidateBuilder(validPrimitiveCandidate()).memberName("bootstrap").build());
    assertShapeMismatch(candidateBuilder(validPrimitiveCandidate()).descriptor("()V").build());
    assertShapeMismatch(candidateBuilder(validPrimitiveCandidate()).insertionOffset(4).build());
    assertShapeMismatch(
        candidateBuilder(validPrimitiveCandidate())
            .dispatcherDescriptor("(Ljava/lang/String;)V")
            .build());
  }

  @Test
  void nullOrFailedPatchPlanFailsWithUpstreamGateBlocked() {
    SteelHook02ContractGeneralizationAnalysis missing =
        analyzer.analyze(validPrimitiveBoundaryAnalysis(), null);
    SteelHook02ContractGeneralizationAnalysis failed =
        analyzer.analyze(
            validPrimitiveBoundaryAnalysis(),
            planBuilder(validPatchPlan()).gatePassed(false).gateFailureReason("failed").build());

    assertFalse(missing.gatePassed());
    assertFalse(failed.gatePassed());
    assertEquals(SteelHook02ContractGeneralizationStatus.UPSTREAM_GATE_BLOCKED, missing.status());
    assertEquals(SteelHook02ContractGeneralizationStatus.UPSTREAM_GATE_BLOCKED, failed.status());
  }

  @Test
  void zeroOrMultiplePlannedPatchesFail() {
    SteelHook02ContractGeneralizationAnalysis zero =
        analyzer.analyze(
            validPrimitiveBoundaryAnalysis(),
            planBuilder(validPatchPlan()).plannedPatches(List.of()).plannedPatchCount(0).build());
    SteelHook02ContractGeneralizationAnalysis multiple =
        analyzer.analyze(
            validPrimitiveBoundaryAnalysis(),
            planBuilder(validPatchPlan())
                .plannedPatches(List.of(validPatch(), validPatch()))
                .plannedPatchCount(2)
                .build());

    assertFalse(zero.gatePassed());
    assertFalse(multiple.gatePassed());
    assertEquals(SteelHook02ContractGeneralizationStatus.UPSTREAM_GATE_BLOCKED, zero.status());
    assertEquals(SteelHook02ContractGeneralizationStatus.UPSTREAM_GATE_BLOCKED, multiple.status());
  }

  @Test
  void runtimeReadinessOrAnalysisBoundaryDriftFails() {
    List<SteelHook02ContractGeneralizationAnalysis> analyses =
        List.of(
            analyzer.analyze(
                validPrimitiveBoundaryAnalysis(),
                planBuilder(validPatchPlan()).transformReadyForMinecraftRuntime(true).build()),
            analyzer.analyze(
                validPrimitiveBoundaryAnalysis(),
                planBuilder(validPatchPlan())
                    .plannedPatches(
                        List.of(
                            patchBuilder(validPatch())
                                .transformReadyForMinecraftRuntime(true)
                                .build()))
                    .build()),
            analyzer.analyze(
                validPrimitiveBoundaryAnalysis(),
                planBuilder(validPatchPlan()).injectionOccurred(true).build()),
            analyzer.analyze(
                validPrimitiveBoundaryAnalysis(),
                planBuilder(validPatchPlan()).transformationOccurred(true).build()),
            analyzer.analyze(
                validPrimitiveBoundaryAnalysis(),
                planBuilder(validPatchPlan()).patchingOccurred(true).build()),
            analyzer.analyze(
                validPrimitiveBoundaryAnalysis(),
                planBuilder(validPatchPlan()).bytecodeModified(true).build()),
            analyzer.analyze(
                validPrimitiveBoundaryAnalysis(),
                planBuilder(validPatchPlan()).mixinUsed(true).build()),
            analyzer.analyze(
                validPrimitiveBoundaryAnalysis(),
                planBuilder(validPatchPlan()).javaAgentUsed(true).build()),
            analyzer.analyze(
                validPrimitiveBoundaryAnalysis(),
                planBuilder(validPatchPlan()).publicApiExposed(true).build()),
            analyzer.analyze(
                validPrimitiveBoundaryAnalysis(),
                planBuilder(validPatchPlan()).javaModExecutionSandboxed(true).build()));

    analyses.forEach(
        analysis -> {
          assertFalse(analysis.gatePassed());
          assertEquals(
              SteelHook02ContractGeneralizationStatus.CONTRACT_GENERALIZATION_BLOCKED,
              analysis.status());
        });
  }

  private void assertShapeMismatch(SteelHook02PrimitiveCandidate candidate) {
    SteelHook02ContractGeneralizationAnalysis analysis =
        analyzer.analyze(
            primitiveBoundaryBuilder(validPrimitiveBoundaryAnalysis())
                .candidates(List.of(candidate))
                .build(),
            validPatchPlan());

    assertFalse(analysis.gatePassed());
    assertEquals(
        SteelHook02ContractGeneralizationStatus.CONTRACT_GENERALIZATION_BLOCKED, analysis.status());
  }

  private SteelHook02PrimitiveBoundaryAnalysis validPrimitiveBoundaryAnalysis() {
    return new SteelHook02PrimitiveBoundaryAnalysis(
        1,
        "Target-23",
        "minecraft",
        "0.2",
        "26.1.2",
        MinecraftSide.SERVER,
        "Target-7",
        "Target-10",
        "Target-22",
        true,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        1,
        1,
        0,
        0,
        true,
        null,
        SteelHook02PrimitiveBoundaryStatus.PRIMITIVE_BOUNDARY_SELECTED,
        SteelHook02NextDirection.MOVE_TO_TARGET_24_CONTRACT_AND_PATCH_PLAN_GENERALIZATION,
        "Move next to Target-24 contract and patch-plan generalization for the approved method-entry static-dispatch primitive.",
        List.of(validPrimitiveCandidate()),
        List.of(
            new SteelHook02PrimitiveFinding(
                "target-23.finding.001",
                "pass",
                SteelHook02PrimitiveFindingStatus.PASS,
                true,
                "pass",
                "pass")));
  }

  private SteelHook02PrimitiveCandidate validPrimitiveCandidate() {
    return new SteelHook02PrimitiveCandidate(
        "target-23.steelhook-0-2.primitive.candidate.001",
        SteelHook02PrimitiveKind.METHOD_ENTRY_STATIC_DISPATCH,
        SteelHook02PrimitiveCandidateStatus.APPROVED_FOR_TARGET_24_CONTRACT_GENERALIZATION,
        "target-7.minecraft.server.main.method-entry-dispatch-patch",
        "net/minecraft/server/Main",
        "main",
        "([Ljava/lang/String;)V",
        0,
        "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher",
        "beforeMinecraftServerMain",
        "()V",
        true,
        false,
        true,
        true,
        false,
        List.of("Approved only as a Target-24 planning candidate."));
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

  private PrimitiveBoundaryBuilder primitiveBoundaryBuilder(
      SteelHook02PrimitiveBoundaryAnalysis base) {
    return new PrimitiveBoundaryBuilder(base);
  }

  private PrimitiveCandidateBuilder candidateBuilder(SteelHook02PrimitiveCandidate base) {
    return new PrimitiveCandidateBuilder(base);
  }

  private PatchPlanBuilder planBuilder(MinecraftHookPatchPlan base) {
    return new PatchPlanBuilder(base);
  }

  private PlannedPatchBuilder patchBuilder(MinecraftPlannedHookPatch base) {
    return new PlannedPatchBuilder(base);
  }

  private static final class PrimitiveBoundaryBuilder {
    private final SteelHook02PrimitiveBoundaryAnalysis base;
    private boolean gatePassed;
    private String gateFailureReason;
    private int approvedCandidateCount;
    private List<SteelHook02PrimitiveCandidate> candidates;

    private PrimitiveBoundaryBuilder(SteelHook02PrimitiveBoundaryAnalysis base) {
      this.base = base;
      gatePassed = base.gatePassed();
      gateFailureReason = base.gateFailureReason();
      approvedCandidateCount = base.approvedCandidateCount();
      candidates = new ArrayList<>(base.candidates());
    }

    private PrimitiveBoundaryBuilder gatePassed(boolean value) {
      gatePassed = value;
      return this;
    }

    private PrimitiveBoundaryBuilder gateFailureReason(String value) {
      gateFailureReason = value;
      return this;
    }

    private PrimitiveBoundaryBuilder approvedCandidateCount(int value) {
      approvedCandidateCount = value;
      return this;
    }

    private PrimitiveBoundaryBuilder candidates(List<SteelHook02PrimitiveCandidate> value) {
      candidates = new ArrayList<>(value);
      return this;
    }

    private SteelHook02PrimitiveBoundaryAnalysis build() {
      return new SteelHook02PrimitiveBoundaryAnalysis(
          base.schema(),
          base.milestoneName(),
          base.target(),
          base.steelHookVersion(),
          base.minecraftVersion(),
          base.side(),
          base.sourcePatchPlanMilestone(),
          base.sourceSteelHookCompletionMilestone(),
          base.sourceRegistryHardeningMilestone(),
          base.analysisOnly(),
          base.classLoadingOccurred(),
          base.injectionOccurred(),
          base.transformationOccurred(),
          base.patchingOccurred(),
          base.hookInstallationOccurred(),
          base.runtimeDispatchOccurred(),
          base.publicApiExposed(),
          base.javaModExecutionSandboxed(),
          base.supportedPrimitiveCount(),
          approvedCandidateCount,
          base.deferredCandidateCount(),
          base.rejectedCandidateCount(),
          gatePassed,
          gateFailureReason,
          base.boundaryStatus(),
          base.nextDirection(),
          base.nextRecommendedAction(),
          candidates,
          base.findings());
    }
  }

  private static final class PrimitiveCandidateBuilder {
    private final SteelHook02PrimitiveCandidate base;
    private SteelHook02PrimitiveCandidateStatus candidateStatus;
    private String sourcePatchId;
    private String ownerInternalName;
    private String memberName;
    private String descriptor;
    private int insertionOffset;
    private String dispatcherDescriptor;

    private PrimitiveCandidateBuilder(SteelHook02PrimitiveCandidate base) {
      this.base = base;
      candidateStatus = base.candidateStatus();
      sourcePatchId = base.sourcePatchId();
      ownerInternalName = base.ownerInternalName();
      memberName = base.memberName();
      descriptor = base.descriptor();
      insertionOffset = base.insertionOffset();
      dispatcherDescriptor = base.dispatcherDescriptor();
    }

    private PrimitiveCandidateBuilder candidateStatus(SteelHook02PrimitiveCandidateStatus value) {
      candidateStatus = value;
      return this;
    }

    private PrimitiveCandidateBuilder sourcePatchId(String value) {
      sourcePatchId = value;
      return this;
    }

    private PrimitiveCandidateBuilder ownerInternalName(String value) {
      ownerInternalName = value;
      return this;
    }

    private PrimitiveCandidateBuilder memberName(String value) {
      memberName = value;
      return this;
    }

    private PrimitiveCandidateBuilder descriptor(String value) {
      descriptor = value;
      return this;
    }

    private PrimitiveCandidateBuilder insertionOffset(int value) {
      insertionOffset = value;
      return this;
    }

    private PrimitiveCandidateBuilder dispatcherDescriptor(String value) {
      dispatcherDescriptor = value;
      return this;
    }

    private SteelHook02PrimitiveCandidate build() {
      return new SteelHook02PrimitiveCandidate(
          base.id(),
          base.primitiveKind(),
          candidateStatus,
          sourcePatchId,
          ownerInternalName,
          memberName,
          descriptor,
          insertionOffset,
          base.dispatcherOwnerInternalName(),
          base.dispatcherMethodName(),
          dispatcherDescriptor,
          base.fixtureTransformReady(),
          base.minecraftRuntimeTransformReady(),
          base.eligibleForTarget24ContractGeneralization(),
          base.eligibleForTarget25TransformerExtraction(),
          base.eligibleForTarget26RuntimeTransformation(),
          base.notes());
    }
  }

  private static final class PatchPlanBuilder {
    private final MinecraftHookPatchPlan base;
    private boolean gatePassed;
    private String gateFailureReason;
    private int plannedPatchCount;
    private List<MinecraftPlannedHookPatch> plannedPatches;
    private boolean transformReadyForMinecraftRuntime;
    private boolean injectionOccurred;
    private boolean transformationOccurred;
    private boolean patchingOccurred;
    private boolean bytecodeModified;
    private boolean javaAgentUsed;
    private boolean mixinUsed;
    private boolean publicApiExposed;
    private boolean javaModExecutionSandboxed;

    private PatchPlanBuilder(MinecraftHookPatchPlan base) {
      this.base = base;
      gatePassed = base.gatePassed();
      gateFailureReason = base.gateFailureReason();
      plannedPatchCount = base.plannedPatchCount();
      plannedPatches = new ArrayList<>(base.plannedPatches());
      transformReadyForMinecraftRuntime = base.transformReadyForMinecraftRuntime();
      injectionOccurred = base.injectionOccurred();
      transformationOccurred = base.transformationOccurred();
      patchingOccurred = base.patchingOccurred();
      bytecodeModified = base.bytecodeModified();
      javaAgentUsed = base.javaAgentUsed();
      mixinUsed = base.mixinUsed();
      publicApiExposed = base.publicApiExposed();
      javaModExecutionSandboxed = base.javaModExecutionSandboxed();
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

    private PatchPlanBuilder injectionOccurred(boolean value) {
      injectionOccurred = value;
      return this;
    }

    private PatchPlanBuilder transformationOccurred(boolean value) {
      transformationOccurred = value;
      return this;
    }

    private PatchPlanBuilder patchingOccurred(boolean value) {
      patchingOccurred = value;
      return this;
    }

    private PatchPlanBuilder bytecodeModified(boolean value) {
      bytecodeModified = value;
      return this;
    }

    private PatchPlanBuilder javaAgentUsed(boolean value) {
      javaAgentUsed = value;
      return this;
    }

    private PatchPlanBuilder mixinUsed(boolean value) {
      mixinUsed = value;
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
          injectionOccurred,
          transformationOccurred,
          patchingOccurred,
          bytecodeModified,
          javaAgentUsed,
          mixinUsed,
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
    private boolean transformReadyForMinecraftRuntime;

    private PlannedPatchBuilder(MinecraftPlannedHookPatch base) {
      this.base = base;
      transformReadyForMinecraftRuntime = base.transformReadyForMinecraftRuntime();
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
          base.kind(),
          base.mode(),
          base.patchEligibility(),
          base.ownerInternalName(),
          base.memberName(),
          base.descriptor(),
          base.insertionOffset(),
          base.required(),
          base.codeInsertion(),
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
