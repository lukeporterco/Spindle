package com.spindle.core.minecraft.hook.patch;

import com.spindle.core.minecraft.MinecraftModExecutionPlan;
import com.spindle.core.minecraft.MinecraftServerRuntimePlan;
import com.spindle.core.minecraft.hook.bytecode.MinecraftCodeNestedAttributeSummary;
import com.spindle.core.minecraft.hook.bytecode.MinecraftDecodedInstruction;
import com.spindle.core.minecraft.hook.bytecode.MinecraftDecodedInstructionKind;
import com.spindle.core.minecraft.hook.bytecode.MinecraftHookBytecodeAnalysisReport;
import com.spindle.core.minecraft.hook.place.MinecraftHookPlacementPlan;
import com.spindle.core.minecraft.hook.place.MinecraftHookPlacementPlanner;
import com.spindle.core.minecraft.hook.place.MinecraftPlannedHookPlacement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class MinecraftHookPatchPlanner {
  public static final String MILESTONE_NAME = "Target-7";
  public static final String TARGET = "minecraft";
  public static final String SUPPORTED_PATCH_ID =
      "target-7.minecraft.server.main.method-entry-dispatch-patch";
  public static final String DISPATCHER_OWNER_INTERNAL_NAME =
      "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher";
  public static final String DISPATCHER_METHOD_NAME = "beforeMinecraftServerMain";
  public static final String DISPATCHER_DESCRIPTOR = "()V";
  private static final int SCHEMA = 1;
  private static final int INSERTION_OFFSET = 0;
  private static final int INSTRUCTION_LENGTH = 3;
  private static final int OFFSET_DELTA = 3;
  private static final String INSERTED_INSTRUCTION_HEX = "b8 ?? ??";

  public MinecraftHookPatchPlan plan(
      MinecraftHookBytecodeAnalysisReport bytecodeAnalysisReport,
      MinecraftHookPlacementPlan placementPlan,
      MinecraftModExecutionPlan executionPlan,
      MinecraftServerRuntimePlan runtimePlan) {
    if (bytecodeAnalysisReport == null) {
      return failedPlan(
          null,
          placementPlan,
          executionPlan,
          runtimePlan,
          "Target-6 hook bytecode analysis report is missing.");
    }
    if (!bytecodeAnalysisReport.gatePassed()) {
      return failedPlan(
          bytecodeAnalysisReport,
          placementPlan,
          executionPlan,
          runtimePlan,
          "Target-6 hook bytecode analysis gate failed.");
    }
    if (!bytecodeAnalysisReport.bytecodeAnalysisSucceeded()) {
      return failedPlan(
          bytecodeAnalysisReport,
          placementPlan,
          executionPlan,
          runtimePlan,
          "Target-6 hook bytecode analysis did not succeed.");
    }
    if (!bytecodeAnalysisReport.instructionBoundaryValidationPassed()) {
      return failedPlan(
          bytecodeAnalysisReport,
          placementPlan,
          executionPlan,
          runtimePlan,
          "Target-6 instruction boundary validation failed.");
    }
    if (!bytecodeAnalysisReport.branchTargetValidationPassed()) {
      return failedPlan(
          bytecodeAnalysisReport,
          placementPlan,
          executionPlan,
          runtimePlan,
          "Target-6 branch target validation failed.");
    }
    if (!bytecodeAnalysisReport.switchTargetValidationPassed()) {
      return failedPlan(
          bytecodeAnalysisReport,
          placementPlan,
          executionPlan,
          runtimePlan,
          "Target-6 switch target validation failed.");
    }
    if (!bytecodeAnalysisReport.exceptionTableValidationPassed()) {
      return failedPlan(
          bytecodeAnalysisReport,
          placementPlan,
          executionPlan,
          runtimePlan,
          "Target-6 exception-table validation failed.");
    }
    if (placementPlan == null) {
      return failedPlan(
          bytecodeAnalysisReport,
          null,
          executionPlan,
          runtimePlan,
          "Target-5 hook placement plan is missing.");
    }
    if (!placementPlan.gatePassed()) {
      return failedPlan(
          bytecodeAnalysisReport,
          placementPlan,
          executionPlan,
          runtimePlan,
          "Target-5 hook placement gate failed.");
    }
    if (!placementPlan.placementPlanned()) {
      return failedPlan(
          bytecodeAnalysisReport,
          placementPlan,
          executionPlan,
          runtimePlan,
          "Target-5 hook placement plan did not produce a planned placement.");
    }
    if (placementPlan.plannedPlacementCount() != 1
        || placementPlan.plannedPlacements().size() != 1) {
      return failedPlan(
          bytecodeAnalysisReport,
          placementPlan,
          executionPlan,
          runtimePlan,
          "Target-7 requires exactly one planned hook placement.");
    }
    MinecraftPlannedHookPlacement placement = placementPlan.plannedPlacements().getFirst();
    if (!MinecraftHookPlacementPlanner.SUPPORTED_PLACEMENT_ID.equals(placement.id())) {
      return failedPlan(
          bytecodeAnalysisReport,
          placementPlan,
          executionPlan,
          runtimePlan,
          "Unsupported hook placement id: " + placement.id());
    }
    if (!matchesSupportedMethod(
        placement.ownerInternalName(), placement.memberName(), placement.descriptor())) {
      return failedPlan(
          bytecodeAnalysisReport,
          placementPlan,
          executionPlan,
          runtimePlan,
          "Target-7 requires net/minecraft/server/Main.main([Ljava/lang/String;)V.");
    }
    if (executionPlan == null
        || !MinecraftHookPlacementPlanner.SUPPORTED_MAIN_CLASS.equals(
            executionPlan.minecraftMainClass())) {
      return failedPlan(
          bytecodeAnalysisReport,
          placementPlan,
          executionPlan,
          runtimePlan,
          "Minecraft execution plan main class must be net.minecraft.server.Main.");
    }
    if (runtimePlan == null) {
      return failedPlan(
          bytecodeAnalysisReport,
          placementPlan,
          executionPlan,
          null,
          "Minecraft runtime plan is missing.");
    }
    if (!matchesSupportedMethod(
            bytecodeAnalysisReport.ownerInternalName(),
            bytecodeAnalysisReport.memberName(),
            bytecodeAnalysisReport.descriptor())
        || bytecodeAnalysisReport.bytecodeOffset() == null
        || bytecodeAnalysisReport.bytecodeOffset() != INSERTION_OFFSET) {
      return failedPlan(
          bytecodeAnalysisReport,
          placementPlan,
          executionPlan,
          runtimePlan,
          "Target-7 requires Target-6 analysis for net/minecraft/server/Main.main([Ljava/lang/String;)V at bytecode offset 0.");
    }
    boolean insertionInstructionBoundary =
        bytecodeAnalysisReport.methodEntryInstructionBoundary()
            && bytecodeAnalysisReport.decodedInstructions().stream()
                .anyMatch(instruction -> instruction.offset() == INSERTION_OFFSET);
    if (!insertionInstructionBoundary) {
      return failedPlan(
          bytecodeAnalysisReport,
          placementPlan,
          executionPlan,
          runtimePlan,
          "Target-7 requires bytecode offset 0 to be a decoded instruction boundary.");
    }

    List<MinecraftPatchConstantPoolRequirement> constantPoolRequirements =
        List.of(
            new MinecraftPatchConstantPoolRequirement("Utf8", DISPATCHER_OWNER_INTERNAL_NAME),
            new MinecraftPatchConstantPoolRequirement("Class", DISPATCHER_OWNER_INTERNAL_NAME),
            new MinecraftPatchConstantPoolRequirement("Utf8", DISPATCHER_METHOD_NAME),
            new MinecraftPatchConstantPoolRequirement("Utf8", DISPATCHER_DESCRIPTOR),
            new MinecraftPatchConstantPoolRequirement(
                "NameAndType", DISPATCHER_METHOD_NAME + ":" + DISPATCHER_DESCRIPTOR),
            new MinecraftPatchConstantPoolRequirement(
                "Methodref",
                DISPATCHER_OWNER_INTERNAL_NAME
                    + "."
                    + DISPATCHER_METHOD_NAME
                    + ":"
                    + DISPATCHER_DESCRIPTOR));
    MinecraftPatchCodeInsertion codeInsertion =
        new MinecraftPatchCodeInsertion(
            DISPATCHER_OWNER_INTERNAL_NAME,
            DISPATCHER_METHOD_NAME,
            DISPATCHER_DESCRIPTOR,
            "invokestatic",
            "b8",
            INSTRUCTION_LENGTH,
            0,
            0,
            INSERTED_INSTRUCTION_HEX);
    MinecraftPatchOffsetAdjustmentSummary branchSummary =
        branchTargetAdjustmentSummary(bytecodeAnalysisReport.decodedInstructions());
    MinecraftPatchOffsetAdjustmentSummary switchSummary =
        switchTargetAdjustmentSummary(bytecodeAnalysisReport.decodedInstructions());
    MinecraftPatchExceptionTableImpact exceptionTableImpact =
        exceptionTableImpact(bytecodeAnalysisReport.exceptionHandlers());
    MinecraftPatchStackMapImpact stackMapImpact = stackMapImpact(bytecodeAnalysisReport);
    MinecraftPatchNestedAttributeImpact nestedAttributeImpact =
        nestedAttributeImpact(bytecodeAnalysisReport.nestedCodeAttributes());
    int originalCodeLength = safeInt(bytecodeAnalysisReport.codeLength());
    int plannedCodeLength = originalCodeLength + OFFSET_DELTA;
    boolean exceptionTableRewriteRequired =
        bytecodeAnalysisReport.exceptionTableCount() > 0
            && !bytecodeAnalysisReport.exceptionHandlers().isEmpty();
    boolean stackMapTableRewriteRequired = stackMapImpact.futureRewriteRequired();
    boolean lineNumberTableRewriteRequired = nestedAttributeImpact.lineNumberTableRewriteRequired();
    boolean localVariableTableRewriteRequired =
        nestedAttributeImpact.localVariableTableRewriteRequired();
    boolean branchOffsetRewriteRequired = branchSummary.futureRewriteRequired();
    boolean switchOffsetRewriteRequired = switchSummary.futureRewriteRequired();
    boolean nestedCodeAttributeRewriteRequired = nestedAttributeImpact.futureRewriteRequired();
    MinecraftPlannedHookPatch plannedPatch =
        new MinecraftPlannedHookPatch(
            SUPPORTED_PATCH_ID,
            placement.id(),
            placement.sourceContractId(),
            bytecodeAnalysisReport.milestoneName(),
            placement.catalogId(),
            MinecraftHookPatchKind.METHOD_ENTRY_STATIC_DISPATCH,
            MinecraftHookPatchMode.DRY_RUN_STATIC_DISPATCH_INVOKESTATIC,
            MinecraftHookPatchEligibility.FIXTURE_ONLY_FUTURE_TRANSFORM,
            placement.ownerInternalName(),
            placement.memberName(),
            placement.descriptor(),
            INSERTION_OFFSET,
            true,
            codeInsertion,
            constantPoolRequirements,
            true,
            true,
            false,
            false,
            exceptionTableRewriteRequired,
            stackMapTableRewriteRequired,
            nestedCodeAttributeRewriteRequired,
            lineNumberTableRewriteRequired,
            localVariableTableRewriteRequired,
            branchOffsetRewriteRequired,
            switchOffsetRewriteRequired,
            branchSummary,
            switchSummary,
            exceptionTableImpact,
            stackMapImpact,
            nestedAttributeImpact,
            true,
            false);
    return new MinecraftHookPatchPlan(
        SCHEMA,
        MILESTONE_NAME,
        TARGET,
        version(bytecodeAnalysisReport, executionPlan, runtimePlan),
        side(bytecodeAnalysisReport, executionPlan),
        placement.catalogId(),
        placementPlan.sourceContractValidationPassed(),
        placementPlan.sourceContractErrorCount(),
        executionPlan.minecraftMainClass(),
        true,
        null,
        true,
        true,
        1,
        MinecraftHookPatchEligibility.FIXTURE_ONLY_FUTURE_TRANSFORM,
        placement.id(),
        bytecodeAnalysisReport.schema(),
        bytecodeAnalysisReport.milestoneName(),
        placement.ownerInternalName(),
        placement.memberName(),
        placement.descriptor(),
        originalCodeLength,
        plannedCodeLength,
        OFFSET_DELTA,
        bytecodeAnalysisReport.codeSha256(),
        INSERTION_OFFSET,
        true,
        INSERTION_OFFSET,
        INSERTED_INSTRUCTION_HEX,
        constantPoolRequirements,
        true,
        true,
        false,
        false,
        exceptionTableRewriteRequired,
        stackMapTableRewriteRequired,
        nestedCodeAttributeRewriteRequired,
        lineNumberTableRewriteRequired,
        localVariableTableRewriteRequired,
        branchOffsetRewriteRequired,
        switchOffsetRewriteRequired,
        true,
        false,
        bytecodeAnalysisReport.codeAttributeParsed(),
        bytecodeAnalysisReport.instructionInspectionOccurred(),
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
        branchSummary,
        switchSummary,
        exceptionTableImpact,
        stackMapImpact,
        nestedAttributeImpact,
        List.of(plannedPatch));
  }

  private MinecraftHookPatchPlan failedPlan(
      MinecraftHookBytecodeAnalysisReport bytecodeAnalysisReport,
      MinecraftHookPlacementPlan placementPlan,
      MinecraftModExecutionPlan executionPlan,
      MinecraftServerRuntimePlan runtimePlan,
      String reason) {
    MinecraftPlannedHookPlacement placement =
        placementPlan == null || placementPlan.plannedPlacements().isEmpty()
            ? null
            : placementPlan.plannedPlacements().getFirst();
    return new MinecraftHookPatchPlan(
        SCHEMA,
        MILESTONE_NAME,
        TARGET,
        version(bytecodeAnalysisReport, executionPlan, runtimePlan),
        side(bytecodeAnalysisReport, executionPlan),
        placementPlan != null ? placementPlan.catalogId() : catalogId(bytecodeAnalysisReport),
        placementPlan != null
            ? placementPlan.sourceContractValidationPassed()
            : bytecodeAnalysisReport != null
                && bytecodeAnalysisReport.sourceContractValidationPassed(),
        placementPlan != null
            ? placementPlan.sourceContractErrorCount()
            : bytecodeAnalysisReport == null
                ? 0
                : bytecodeAnalysisReport.sourceContractErrorCount(),
        executionPlan == null ? null : executionPlan.minecraftMainClass(),
        false,
        reason,
        false,
        false,
        0,
        MinecraftHookPatchEligibility.NOT_ELIGIBLE,
        placement != null ? placement.id() : reportPlacementId(bytecodeAnalysisReport),
        bytecodeAnalysisReport == null ? null : bytecodeAnalysisReport.schema(),
        bytecodeAnalysisReport == null ? null : bytecodeAnalysisReport.milestoneName(),
        placement != null
            ? placement.ownerInternalName()
            : bytecodeAnalysisReport == null ? null : bytecodeAnalysisReport.ownerInternalName(),
        placement != null
            ? placement.memberName()
            : bytecodeAnalysisReport == null ? null : bytecodeAnalysisReport.memberName(),
        placement != null
            ? placement.descriptor()
            : bytecodeAnalysisReport == null ? null : bytecodeAnalysisReport.descriptor(),
        bytecodeAnalysisReport == null ? null : bytecodeAnalysisReport.codeLength(),
        null,
        0,
        bytecodeAnalysisReport == null ? null : bytecodeAnalysisReport.codeSha256(),
        INSERTION_OFFSET,
        bytecodeAnalysisReport != null && bytecodeAnalysisReport.methodEntryInstructionBoundary(),
        bytecodeAnalysisReport == null ? null : bytecodeAnalysisReport.bytecodeOffset(),
        null,
        List.of(),
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
        bytecodeAnalysisReport != null && bytecodeAnalysisReport.codeAttributeParsed(),
        bytecodeAnalysisReport != null && bytecodeAnalysisReport.instructionInspectionOccurred(),
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
        null,
        null,
        null,
        null,
        null,
        List.of());
  }

  private static MinecraftPatchOffsetAdjustmentSummary branchTargetAdjustmentSummary(
      List<MinecraftDecodedInstruction> instructions) {
    List<Integer> sourceOffsets = new ArrayList<>();
    List<Integer> targetOffsets = new ArrayList<>();
    for (MinecraftDecodedInstruction instruction : instructions) {
      if (instruction.kind() != MinecraftDecodedInstructionKind.BRANCH) {
        continue;
      }
      sourceOffsets.add(instruction.offset());
      targetOffsets.addAll(instruction.branchTargetOffsets());
    }
    sourceOffsets.sort(Comparator.naturalOrder());
    targetOffsets.sort(Comparator.naturalOrder());
    return new MinecraftPatchOffsetAdjustmentSummary(
        "branch-targets",
        OFFSET_DELTA,
        targetOffsets.size(),
        sourceOffsets,
        targetOffsets,
        !targetOffsets.isEmpty(),
        "Future transform must add +3 to every original branch target offset greater than or equal to 0.");
  }

  private static MinecraftPatchOffsetAdjustmentSummary switchTargetAdjustmentSummary(
      List<MinecraftDecodedInstruction> instructions) {
    List<Integer> sourceOffsets = new ArrayList<>();
    List<Integer> targetOffsets = new ArrayList<>();
    for (MinecraftDecodedInstruction instruction : instructions) {
      if (instruction.kind() != MinecraftDecodedInstructionKind.SWITCH) {
        continue;
      }
      sourceOffsets.add(instruction.offset());
      if (instruction.switchDefaultTargetOffset() != null) {
        targetOffsets.add(instruction.switchDefaultTargetOffset());
      }
      instruction.switchMatchTargetPairs().stream()
          .map(target -> target.targetOffset())
          .forEach(targetOffsets::add);
    }
    sourceOffsets.sort(Comparator.naturalOrder());
    targetOffsets.sort(Comparator.naturalOrder());
    return new MinecraftPatchOffsetAdjustmentSummary(
        "switch-targets",
        OFFSET_DELTA,
        targetOffsets.size(),
        sourceOffsets,
        targetOffsets,
        !targetOffsets.isEmpty(),
        "Future transform must add +3 to every original switch target offset greater than or equal to 0.");
  }

  private static MinecraftPatchExceptionTableImpact exceptionTableImpact(
      List<com.spindle.core.minecraft.hook.bytecode.MinecraftDecodedExceptionHandler> handlers) {
    List<String> adjustedFields = new ArrayList<>();
    for (int index = 0; index < handlers.size(); index++) {
      var handler = handlers.get(index);
      adjustedFields.add(
          "entry[" + index + "].startPc " + handler.startPc() + "->" + (handler.startPc() + 3));
      adjustedFields.add(
          "entry["
              + index
              + "].endPc "
              + handler.endPc()
              + "->"
              + (handler.endPc() + OFFSET_DELTA));
      adjustedFields.add(
          "entry["
              + index
              + "].handlerPc "
              + handler.handlerPc()
              + "->"
              + (handler.handlerPc() + OFFSET_DELTA));
    }
    return new MinecraftPatchExceptionTableImpact(
        !handlers.isEmpty(),
        handlers.size(),
        OFFSET_DELTA,
        !handlers.isEmpty(),
        adjustedFields.size(),
        adjustedFields);
  }

  private static MinecraftPatchStackMapImpact stackMapImpact(
      MinecraftHookBytecodeAnalysisReport report) {
    boolean rewriteRequired = report.stackMapTablePresent();
    return new MinecraftPatchStackMapImpact(
        report.stackMapTablePresent(),
        report.stackMapTableEntryCount(),
        rewriteRequired,
        rewriteRequired
            ? "StackMapTable frames reference original code offsets and would require a future rewrite after insertion at bytecode offset 0."
            : null);
  }

  private static MinecraftPatchNestedAttributeImpact nestedAttributeImpact(
      List<MinecraftCodeNestedAttributeSummary> nestedAttributes) {
    List<String> names =
        nestedAttributes.stream().map(MinecraftCodeNestedAttributeSummary::name).sorted().toList();
    boolean stackMapTablePresent = names.contains("StackMapTable");
    boolean lineNumberTablePresent = names.contains("LineNumberTable");
    boolean localVariableTablePresent = names.contains("LocalVariableTable");
    boolean localVariableTypeTablePresent = names.contains("LocalVariableTypeTable");
    boolean futureRewriteRequired =
        stackMapTablePresent
            || lineNumberTablePresent
            || localVariableTablePresent
            || localVariableTypeTablePresent;
    return new MinecraftPatchNestedAttributeImpact(
        nestedAttributes.size(),
        stackMapTablePresent,
        lineNumberTablePresent,
        localVariableTablePresent,
        localVariableTypeTablePresent,
        stackMapTablePresent,
        lineNumberTablePresent,
        localVariableTablePresent,
        localVariableTypeTablePresent,
        futureRewriteRequired,
        names);
  }

  private static boolean matchesSupportedMethod(
      String ownerInternalName, String memberName, String descriptor) {
    return MinecraftHookPlacementPlanner.SUPPORTED_OWNER_INTERNAL_NAME.equals(ownerInternalName)
        && MinecraftHookPlacementPlanner.SUPPORTED_MEMBER_NAME.equals(memberName)
        && MinecraftHookPlacementPlanner.SUPPORTED_DESCRIPTOR.equals(descriptor);
  }

  private static String version(
      MinecraftHookBytecodeAnalysisReport report,
      MinecraftModExecutionPlan executionPlan,
      MinecraftServerRuntimePlan runtimePlan) {
    if (report != null && report.minecraftVersion() != null) {
      return report.minecraftVersion();
    }
    if (executionPlan != null) {
      return executionPlan.resolvedMinecraftVersion();
    }
    return runtimePlan == null ? null : runtimePlan.resolvedMinecraftVersion();
  }

  private static String side(
      MinecraftHookBytecodeAnalysisReport report, MinecraftModExecutionPlan executionPlan) {
    if (report != null && report.side() != null) {
      return report.side();
    }
    return executionPlan == null ? null : executionPlan.side();
  }

  private static String reportPlacementId(MinecraftHookBytecodeAnalysisReport report) {
    return report == null ? null : report.placementId();
  }

  private static String catalogId(MinecraftHookBytecodeAnalysisReport report) {
    return report == null ? null : report.catalogId();
  }

  private static int safeInt(Integer value) {
    return value == null ? 0 : value;
  }
}
