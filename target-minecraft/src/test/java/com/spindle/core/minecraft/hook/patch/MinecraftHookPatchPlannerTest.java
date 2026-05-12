package com.spindle.core.minecraft.hook.patch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftModExecutionPlan;
import com.spindle.core.minecraft.MinecraftRuntimeFile;
import com.spindle.core.minecraft.MinecraftServerRuntimePlan;
import com.spindle.core.minecraft.hook.bytecode.MinecraftCodeNestedAttributeSummary;
import com.spindle.core.minecraft.hook.bytecode.MinecraftDecodedBranchTarget;
import com.spindle.core.minecraft.hook.bytecode.MinecraftDecodedExceptionHandler;
import com.spindle.core.minecraft.hook.bytecode.MinecraftDecodedInstruction;
import com.spindle.core.minecraft.hook.bytecode.MinecraftDecodedInstructionKind;
import com.spindle.core.minecraft.hook.bytecode.MinecraftHookBytecodeAnalysisReport;
import com.spindle.core.minecraft.hook.place.MinecraftHookPlacementKind;
import com.spindle.core.minecraft.hook.place.MinecraftHookPlacementMode;
import com.spindle.core.minecraft.hook.place.MinecraftHookPlacementPlan;
import com.spindle.core.minecraft.hook.place.MinecraftMethodCodeSummary;
import com.spindle.core.minecraft.hook.place.MinecraftPlannedHookPlacement;
import java.util.List;
import org.junit.jupiter.api.Test;

class MinecraftHookPatchPlannerTest {
  private final MinecraftHookPatchPlanner planner = new MinecraftHookPatchPlanner();

  @Test
  void validTargetSixReportProducesDeterministicDryRunPatchPlan() {
    MinecraftHookPatchPlan plan =
        planner.plan(
            validBytecodeReport(true, true, true, true),
            validPlacementPlan(),
            executionPlan("net.minecraft.server.Main"),
            runtimePlan());

    assertTrue(plan.gatePassed());
    assertTrue(plan.patchPlanningSucceeded());
    assertTrue(plan.patchPlanned());
    assertEquals(1, plan.plannedPatchCount());
    assertEquals(
        "target-7.minecraft.server.main.method-entry-dispatch-patch",
        plan.plannedPatches().getFirst().id());
    assertEquals(0, plan.insertionOffset());
    assertTrue(plan.insertionInstructionBoundary());
    assertEquals("b8 ?? ??", plan.insertedInstructionHex());
    assertEquals(3, plan.codeLengthDelta());
    assertEquals(34, plan.plannedCodeLength());
    assertTrue(plan.transformReadyForFixtureOnly());
    assertFalse(plan.transformReadyForMinecraftRuntime());
    assertTrue(plan.stackMapTableRewriteRequired());
    assertTrue(plan.branchOffsetRewriteRequired());
    assertTrue(plan.switchOffsetRewriteRequired());
    assertTrue(plan.exceptionTableRewriteRequired());
    assertTrue(plan.nestedCodeAttributeRewriteRequired());

    MinecraftPlannedHookPatch patch = plan.plannedPatches().getFirst();
    assertEquals(MinecraftHookPatchKind.METHOD_ENTRY_STATIC_DISPATCH, patch.kind());
    assertEquals("dry-run-static-dispatch-invokestatic", patch.mode().id());
    assertEquals(0, patch.insertionOffset());
    assertEquals("invokestatic", patch.codeInsertion().plannedOpcode());
    assertEquals("b8", patch.codeInsertion().plannedOpcodeHex());
    assertEquals(3, patch.codeInsertion().plannedInstructionLength());
    assertEquals(0, patch.codeInsertion().plannedStackDelta());
    assertEquals(0, patch.codeInsertion().requiredMaxStackIncrease());
    assertEquals(
        "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher",
        patch.codeInsertion().dispatcherOwnerInternalName());
    assertEquals("beforeMinecraftServerMain", patch.codeInsertion().dispatcherMethodName());
    assertEquals("()V", patch.codeInsertion().dispatcherDescriptor());
    assertEquals(
        List.of(
            "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher",
            "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher",
            "beforeMinecraftServerMain",
            "()V",
            "beforeMinecraftServerMain:()V",
            "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher.beforeMinecraftServerMain:()V"),
        patch.requiredConstantPoolEntries().stream()
            .map(MinecraftPatchConstantPoolRequirement::symbolicValue)
            .toList());
    assertEquals(1, patch.branchTargetAdjustmentSummary().adjustedTargetCount());
    assertEquals(List.of(1), patch.branchTargetAdjustmentSummary().sourceInstructionOffsets());
    assertEquals(List.of(12), patch.branchTargetAdjustmentSummary().adjustedTargetOffsets());
    assertEquals(3, patch.switchTargetAdjustmentSummary().adjustedTargetCount());
    assertEquals(List.of(8), patch.switchTargetAdjustmentSummary().sourceInstructionOffsets());
    assertEquals(
        List.of(20, 24, 28), patch.switchTargetAdjustmentSummary().adjustedTargetOffsets());
    assertEquals(3, patch.exceptionTableImpact().adjustedFieldCount());
    assertEquals(
        List.of("LineNumberTable", "LocalVariableTable", "LocalVariableTypeTable", "StackMapTable"),
        patch.nestedAttributeImpact().presentAttributeNames());
    assertTrue(patch.transformReadyForFixtureOnly());
    assertFalse(patch.transformReadyForMinecraftRuntime());
  }

  @Test
  void noStackMapTableLeavesStackMapRewriteRequirementFalse() {
    MinecraftHookPatchPlan plan =
        planner.plan(
            validBytecodeReport(false, true, true, true),
            validPlacementPlan(),
            executionPlan("net.minecraft.server.Main"),
            runtimePlan());

    assertTrue(plan.gatePassed());
    assertFalse(plan.stackMapTableRewriteRequired());
    assertFalse(plan.plannedPatches().getFirst().stackMapImpact().futureRewriteRequired());
  }

  @Test
  void targetSixGateFailureProducesFailedReportWithoutThrowing() {
    MinecraftHookPatchPlan plan =
        planner.plan(
            withGate(validBytecodeReport(true, false, true, true), false, "Target-6 failed."),
            validPlacementPlan(),
            executionPlan("net.minecraft.server.Main"),
            runtimePlan());

    assertFalse(plan.gatePassed());
    assertFalse(plan.patchPlanningSucceeded());
    assertFalse(plan.patchPlanned());
    assertEquals(0, plan.plannedPatchCount());
    assertEquals("Target-6 hook bytecode analysis gate failed.", plan.gateFailureReason());
    assertNull(plan.insertedInstructionHex());
  }

  @Test
  void targetSixBranchValidationFailureFailsGate() {
    MinecraftHookPatchPlan plan =
        planner.plan(
            validBytecodeReport(true, true, false, true),
            validPlacementPlan(),
            executionPlan("net.minecraft.server.Main"),
            runtimePlan());

    assertFalse(plan.gatePassed());
    assertEquals("Target-6 branch target validation failed.", plan.gateFailureReason());
  }

  @Test
  void targetSixSwitchValidationFailureFailsGate() {
    MinecraftHookPatchPlan plan =
        planner.plan(
            validBytecodeReport(true, true, true, false),
            validPlacementPlan(),
            executionPlan("net.minecraft.server.Main"),
            runtimePlan());

    assertFalse(plan.gatePassed());
    assertEquals("Target-6 switch target validation failed.", plan.gateFailureReason());
  }

  @Test
  void targetSixExceptionTableValidationFailureFailsGate() {
    MinecraftHookPatchPlan plan =
        planner.plan(
            withExceptionValidation(validBytecodeReport(true, true, true, true), false),
            validPlacementPlan(),
            executionPlan("net.minecraft.server.Main"),
            runtimePlan());

    assertFalse(plan.gatePassed());
    assertEquals("Target-6 exception-table validation failed.", plan.gateFailureReason());
  }

  @Test
  void unsupportedPlacementIdFailsGate() {
    MinecraftHookPatchPlan plan =
        planner.plan(
            validBytecodeReport(true, true, true, true),
            placementPlan("unsupported-placement"),
            executionPlan("net.minecraft.server.Main"),
            runtimePlan());

    assertFalse(plan.gatePassed());
    assertTrue(plan.gateFailureReason().contains("Unsupported hook placement id"));
  }

  @Test
  void mainClassMismatchFailsGate() {
    MinecraftHookPatchPlan plan =
        planner.plan(
            validBytecodeReport(true, true, true, true),
            validPlacementPlan(),
            executionPlan("com.example.NotMain"),
            runtimePlan());

    assertFalse(plan.gatePassed());
    assertTrue(plan.gateFailureReason().contains("main class"));
  }

  private MinecraftHookPlacementPlan validPlacementPlan() {
    return placementPlan("target-5.minecraft.server.main.method-entry-placement");
  }

  private MinecraftHookPlacementPlan placementPlan(String placementId) {
    return new MinecraftHookPlacementPlan(
        1,
        "Target-5",
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
        1,
        List.of(
            new MinecraftPlannedHookPlacement(
                placementId,
                "minecraft.26_1_2.server.main.entrypoint",
                "minecraft-26.1.2-server-known-symbols",
                MinecraftHookPlacementKind.METHOD_ENTRY,
                "net/minecraft/server/Main",
                "main",
                "([Ljava/lang/String;)V",
                0,
                MinecraftHookPlacementMode.METHOD_ENTRY_ANALYSIS_ONLY,
                true,
                new MinecraftMethodCodeSummary(3, 2, 31, "abc123", 1, 4, true, false, 0))),
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
        false,
        false);
  }

  private MinecraftHookBytecodeAnalysisReport validBytecodeReport(
      boolean stackMapTablePresent,
      boolean gatePassed,
      boolean branchValidationPassed,
      boolean switchValidationPassed) {
    List<MinecraftCodeNestedAttributeSummary> nestedAttributes =
        stackMapTablePresent
            ? List.of(
                new MinecraftCodeNestedAttributeSummary("LineNumberTable", 10, null),
                new MinecraftCodeNestedAttributeSummary("LocalVariableTable", 12, 1),
                new MinecraftCodeNestedAttributeSummary("LocalVariableTypeTable", 12, 1),
                new MinecraftCodeNestedAttributeSummary("StackMapTable", 8, 2))
            : List.of(
                new MinecraftCodeNestedAttributeSummary("LineNumberTable", 10, null),
                new MinecraftCodeNestedAttributeSummary("LocalVariableTable", 12, 1),
                new MinecraftCodeNestedAttributeSummary("LocalVariableTypeTable", 12, 1));
    return new MinecraftHookBytecodeAnalysisReport(
        1,
        "Target-6",
        "minecraft",
        "26.1.2",
        "server",
        "minecraft-26.1.2-server-known-symbols",
        true,
        0,
        "net.minecraft.server.Main",
        "target-5.minecraft.server.main.method-entry-placement",
        "minecraft.26_1_2.server.main.entrypoint",
        "net/minecraft/server/Main",
        "main",
        "([Ljava/lang/String;)V",
        0,
        gatePassed,
        gatePassed ? null : "Target-6 failed.",
        gatePassed,
        true,
        true,
        true,
        true,
        branchValidationPassed,
        switchValidationPassed,
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
        8,
        0,
        28,
        31,
        "abc123",
        stackMapTablePresent,
        stackMapTablePresent ? 2 : null,
        nestedAttributes.size(),
        1,
        3,
        0,
        0,
        1,
        1,
        0,
        0,
        0,
        true,
        List.of(
            new MinecraftDecodedInstruction(
                0,
                42,
                "aload_0",
                1,
                MinecraftDecodedInstructionKind.LOCAL_VARIABLE,
                "",
                List.of(),
                null,
                List.of(),
                null),
            new MinecraftDecodedInstruction(
                1,
                153,
                "ifeq",
                3,
                MinecraftDecodedInstructionKind.BRANCH,
                "000b",
                List.of(12),
                null,
                List.of(),
                null),
            new MinecraftDecodedInstruction(
                4,
                43,
                "aload_1",
                1,
                MinecraftDecodedInstructionKind.LOCAL_VARIABLE,
                "",
                List.of(),
                null,
                List.of(),
                null),
            new MinecraftDecodedInstruction(
                8,
                171,
                "lookupswitch",
                12,
                MinecraftDecodedInstructionKind.SWITCH,
                "",
                List.of(),
                20,
                List.of(
                    new MinecraftDecodedBranchTarget(0, 24),
                    new MinecraftDecodedBranchTarget(1, 28)),
                null),
            new MinecraftDecodedInstruction(
                12,
                177,
                "return",
                1,
                MinecraftDecodedInstructionKind.RETURN,
                "",
                List.of(),
                null,
                List.of(),
                null),
            new MinecraftDecodedInstruction(
                20,
                177,
                "return",
                1,
                MinecraftDecodedInstructionKind.RETURN,
                "",
                List.of(),
                null,
                List.of(),
                null),
            new MinecraftDecodedInstruction(
                24,
                177,
                "return",
                1,
                MinecraftDecodedInstructionKind.RETURN,
                "",
                List.of(),
                null,
                List.of(),
                null),
            new MinecraftDecodedInstruction(
                28,
                177,
                "return",
                1,
                MinecraftDecodedInstructionKind.RETURN,
                "",
                List.of(),
                null,
                List.of(),
                null)),
        List.of(new MinecraftDecodedExceptionHandler(0, 20, 24, 9)),
        nestedAttributes);
  }

  private MinecraftModExecutionPlan executionPlan(String mainClass) {
    return new MinecraftModExecutionPlan(
        1,
        "Milestone 8",
        "26.1.2",
        "25",
        "server",
        null,
        null,
        null,
        List.of(),
        List.of(),
        List.of(),
        null,
        mainClass,
        List.of(),
        List.of(),
        null,
        null);
  }

  private MinecraftServerRuntimePlan runtimePlan() {
    return new MinecraftServerRuntimePlan(
        1,
        "Mega-Milestone 7",
        "25",
        "26.1.2",
        "26.1.2",
        "26.1.2",
        "test",
        "local",
        "local",
        "runtime/server.jar",
        "local",
        "sha1",
        "sha256",
        10L,
        "simple-jar",
        "test",
        "net.minecraft.server.Main",
        List.of(),
        List.<MinecraftRuntimeFile>of(),
        List.of(),
        List.of(),
        ".",
        "java",
        List.of("java"),
        "minecraft-cache",
        "runtime-cache",
        true,
        true,
        0,
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
        null);
  }

  private MinecraftHookBytecodeAnalysisReport withGate(
      MinecraftHookBytecodeAnalysisReport report, boolean gatePassed, String gateFailureReason) {
    return new MinecraftHookBytecodeAnalysisReport(
        report.schema(),
        report.milestoneName(),
        report.target(),
        report.minecraftVersion(),
        report.side(),
        report.catalogId(),
        report.sourceContractValidationPassed(),
        report.sourceContractErrorCount(),
        report.minecraftMainClass(),
        report.placementId(),
        report.sourceContractId(),
        report.ownerInternalName(),
        report.memberName(),
        report.descriptor(),
        report.bytecodeOffset(),
        gatePassed,
        gateFailureReason,
        gatePassed,
        report.codeAttributeParsed(),
        report.instructionInspectionOccurred(),
        report.instructionStreamDecoded(),
        report.instructionBoundaryValidationPassed(),
        report.branchTargetValidationPassed(),
        report.switchTargetValidationPassed(),
        report.exceptionTableValidationPassed(),
        report.injectionOccurred(),
        report.transformationOccurred(),
        report.patchingOccurred(),
        report.bytecodeModified(),
        report.javaAgentUsed(),
        report.mixinUsed(),
        report.remappingOccurred(),
        report.publicApiExposed(),
        report.javaModExecutionSandboxed(),
        report.instructionCount(),
        report.firstInstructionOffset(),
        report.lastInstructionOffset(),
        report.codeLength(),
        report.codeSha256(),
        report.stackMapTablePresent(),
        report.stackMapTableEntryCount(),
        report.nestedCodeAttributeCount(),
        report.exceptionTableCount(),
        report.returnInstructionCount(),
        report.throwInstructionCount(),
        report.invokeInstructionCount(),
        report.branchInstructionCount(),
        report.switchInstructionCount(),
        report.wideInstructionCount(),
        report.reservedOpcodeCount(),
        report.unsupportedOpcodeCount(),
        report.methodEntryInstructionBoundary(),
        report.decodedInstructions(),
        report.exceptionHandlers(),
        report.nestedCodeAttributes());
  }

  private MinecraftHookBytecodeAnalysisReport withExceptionValidation(
      MinecraftHookBytecodeAnalysisReport report, boolean exceptionTableValidationPassed) {
    return new MinecraftHookBytecodeAnalysisReport(
        report.schema(),
        report.milestoneName(),
        report.target(),
        report.minecraftVersion(),
        report.side(),
        report.catalogId(),
        report.sourceContractValidationPassed(),
        report.sourceContractErrorCount(),
        report.minecraftMainClass(),
        report.placementId(),
        report.sourceContractId(),
        report.ownerInternalName(),
        report.memberName(),
        report.descriptor(),
        report.bytecodeOffset(),
        report.gatePassed(),
        exceptionTableValidationPassed ? report.gateFailureReason() : "exception validation failed",
        report.bytecodeAnalysisSucceeded(),
        report.codeAttributeParsed(),
        report.instructionInspectionOccurred(),
        report.instructionStreamDecoded(),
        report.instructionBoundaryValidationPassed(),
        report.branchTargetValidationPassed(),
        report.switchTargetValidationPassed(),
        exceptionTableValidationPassed,
        report.injectionOccurred(),
        report.transformationOccurred(),
        report.patchingOccurred(),
        report.bytecodeModified(),
        report.javaAgentUsed(),
        report.mixinUsed(),
        report.remappingOccurred(),
        report.publicApiExposed(),
        report.javaModExecutionSandboxed(),
        report.instructionCount(),
        report.firstInstructionOffset(),
        report.lastInstructionOffset(),
        report.codeLength(),
        report.codeSha256(),
        report.stackMapTablePresent(),
        report.stackMapTableEntryCount(),
        report.nestedCodeAttributeCount(),
        report.exceptionTableCount(),
        report.returnInstructionCount(),
        report.throwInstructionCount(),
        report.invokeInstructionCount(),
        report.branchInstructionCount(),
        report.switchInstructionCount(),
        report.wideInstructionCount(),
        report.reservedOpcodeCount(),
        report.unsupportedOpcodeCount(),
        report.methodEntryInstructionBoundary(),
        report.decodedInstructions(),
        report.exceptionHandlers(),
        report.nestedCodeAttributes());
  }
}
