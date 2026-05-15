package com.spindle.core.minecraft.hook.transform;

import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchMode;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchPlan;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchPlanner;
import com.spindle.core.minecraft.hook.patch.MinecraftPlannedHookPatch;

public final class MinecraftFixtureBytecodeTransformer {
  public static final String MILESTONE_NAME = "Target-8";
  public static final String TRANSFORMATION_SCOPE = "fixture-only";

  private static final int INSERTION_OFFSET = 0;
  private static final int INSERTION_LENGTH = 3;

  private final SteelHookMethodEntryClassFileRewriter rewriter =
      new SteelHookMethodEntryClassFileRewriter();

  public MinecraftFixtureTransformationResult transformFixtureClass(
      byte[] originalClassBytes, MinecraftHookPatchPlan patchPlan) {
    byte[] immutableOriginal = originalClassBytes == null ? null : originalClassBytes.clone();
    MinecraftFixtureTransformationGate gate = evaluateGate(patchPlan);
    if (!gate.passed()) {
      return failedResult(
          immutableOriginal,
          patchPlan,
          gate,
          MinecraftFixtureTransformationStatus.PATCH_PLAN_GATE_FAILED,
          gate.failureReason(),
          null);
    }
    if (immutableOriginal == null || immutableOriginal.length == 0) {
      return failedResult(
          null,
          patchPlan,
          gate,
          MinecraftFixtureTransformationStatus.REJECTED,
          "Fixture class bytes are required for Target-8 transformation.",
          null);
    }

    MinecraftPlannedHookPatch plannedPatch = patchPlan.plannedPatches().getFirst();
    SteelHookMethodEntryRewriteRequest request =
        new SteelHookMethodEntryRewriteRequest(
            plannedPatch.id(),
            "Target-8 fixture transformation",
            plannedPatch.id(),
            plannedPatch.sourcePlacementId(),
            plannedPatch.sourceContractId(),
            plannedPatch.ownerInternalName(),
            "net.minecraft.server.Main",
            "net/minecraft/server/Main.class",
            plannedPatch.memberName(),
            plannedPatch.descriptor(),
            INSERTION_OFFSET,
            MinecraftHookPatchPlanner.DISPATCHER_OWNER_INTERNAL_NAME,
            "com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher",
            MinecraftHookPatchPlanner.DISPATCHER_METHOD_NAME,
            MinecraftHookPatchPlanner.DISPATCHER_DESCRIPTOR,
            "invokestatic",
            "b8",
            INSERTION_LENGTH,
            false,
            false,
            false,
            false);
    SteelHookMethodEntryRewriteResult rewriteResult = rewriter.rewrite(request, immutableOriginal);
    if (rewriteResult.status() != SteelHookMethodEntryRewriteStatus.TRANSFORMED) {
      return failedResult(
          immutableOriginal,
          patchPlan,
          gate,
          MinecraftFixtureTransformationStatus.REJECTED,
          translateFailureReason(rewriteResult.failureReason()),
          rewriteResult);
    }

    return new MinecraftFixtureTransformationResult(
        1,
        MILESTONE_NAME,
        TRANSFORMATION_SCOPE,
        MinecraftFixtureTransformationStatus.TRANSFORMED,
        gate,
        plannedPatch.id(),
        plannedPatch.sourcePlacementId(),
        plannedPatch.sourceContractId(),
        plannedPatch.ownerInternalName(),
        plannedPatch.memberName(),
        plannedPatch.descriptor(),
        INSERTION_OFFSET,
        rewriteResult.insertedInstructionHex(),
        rewriteResult.originalClassSha256(),
        rewriteResult.transformedClassSha256(),
        rewriteResult.originalCodeSha256(),
        rewriteResult.transformedCodeSha256(),
        rewriteResult.originalCodeLength(),
        rewriteResult.transformedCodeLength(),
        rewriteResult.constantPoolCountBefore(),
        rewriteResult.constantPoolCountAfter(),
        rewriteResult.methodrefIndex(),
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
        null,
        toFixtureConstantPoolPatch(rewriteResult.constantPoolPatch()),
        toFixtureCodePatchResult(rewriteResult.codePatch()),
        toFixtureTransformedClass(rewriteResult.transformedClass()));
  }

  private String translateFailureReason(String failureReason) {
    if (failureReason == null) {
      return null;
    }
    return switch (failureReason) {
      case "Target method is missing a Code attribute." ->
          "Target-8 fixture method is missing a Code attribute.";
      case "Target method is abstract or native." ->
          "Target-8 cannot transform an abstract or native fixture method.";
      case "Target method has multiple Code attributes." ->
          "Target-8 fixture method has multiple Code attributes.";
      case "StackMapTable rewriting is not supported." ->
          "Target-8 fixture transformation rejects methods with StackMapTable.";
      case "Code length exceeds the JVM limit after insertion." ->
          "Target-8 fixture transformation exceeds the maximum Code length.";
      case "Constant pool count exceeds the JVM limit after insertion." ->
          "Target-8 fixture transformation exceeds the maximum constant_pool_count.";
      default -> {
        if (failureReason.startsWith("Target class internal name mismatch:")) {
          yield "Target-8 requires fixture class net/minecraft/server/Main.";
        }
        if (failureReason.startsWith("Target method not found exactly once:")) {
          yield "Target-8 requires exactly one net/minecraft/server/Main.main([Ljava/lang/String;)V method.";
        }
        yield failureReason;
      }
    };
  }

  private MinecraftFixtureConstantPoolPatch toFixtureConstantPoolPatch(
      SteelHookMethodEntryConstantPoolPatch patch) {
    if (patch == null) {
      return null;
    }
    return new MinecraftFixtureConstantPoolPatch(
        patch.constantPoolCountBefore(),
        patch.constantPoolCountAfter(),
        patch.appendedEntryCount(),
        patch.dispatcherOwnerUtf8Index(),
        patch.dispatcherClassIndex(),
        patch.dispatcherMethodNameUtf8Index(),
        patch.dispatcherDescriptorUtf8Index(),
        patch.dispatcherNameAndTypeIndex(),
        patch.dispatcherMethodrefIndex());
  }

  private MinecraftFixtureCodePatchResult toFixtureCodePatchResult(
      SteelHookMethodEntryCodePatchResult patch) {
    if (patch == null) {
      return null;
    }
    return new MinecraftFixtureCodePatchResult(
        patch.originalCodeLength(),
        patch.transformedCodeLength(),
        patch.originalCodeSha256(),
        patch.transformedCodeSha256(),
        patch.maxStackBefore(),
        patch.maxStackAfter(),
        patch.maxLocalsBefore(),
        patch.maxLocalsAfter(),
        patch.exceptionTableCount(),
        patch.exceptionTableShiftApplied(),
        patch.insertedInstructionHex());
  }

  private MinecraftFixtureTransformedClass toFixtureTransformedClass(
      SteelHookMethodEntryTransformedClass transformedClass) {
    if (transformedClass == null) {
      return null;
    }
    return new MinecraftFixtureTransformedClass(
        transformedClass.internalName(),
        transformedClass.classBytes(),
        transformedClass.classSha256());
  }

  private MinecraftFixtureTransformationGate evaluateGate(MinecraftHookPatchPlan patchPlan) {
    if (patchPlan == null) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Target-8 requires a Target-7 patch plan.",
          false,
          false,
          false,
          0,
          null,
          false,
          false);
    }
    if (!patchPlan.gatePassed()) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Target-8 requires a passing Target-7 patch plan gate.",
          false,
          patchPlan.patchPlanningSucceeded(),
          patchPlan.patchPlanned(),
          patchPlan.plannedPatchCount(),
          null,
          patchPlan.transformReadyForFixtureOnly(),
          patchPlan.transformReadyForMinecraftRuntime());
    }
    if (!patchPlan.patchPlanningSucceeded() || !patchPlan.patchPlanned()) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Target-8 requires a successful Target-7 patch plan.",
          true,
          patchPlan.patchPlanningSucceeded(),
          patchPlan.patchPlanned(),
          patchPlan.plannedPatchCount(),
          null,
          patchPlan.transformReadyForFixtureOnly(),
          patchPlan.transformReadyForMinecraftRuntime());
    }
    if (patchPlan.plannedPatchCount() != 1 || patchPlan.plannedPatches().size() != 1) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Target-8 requires exactly one planned Target-7 patch.",
          true,
          true,
          true,
          patchPlan.plannedPatchCount(),
          null,
          patchPlan.transformReadyForFixtureOnly(),
          patchPlan.transformReadyForMinecraftRuntime());
    }
    MinecraftPlannedHookPatch patch = patchPlan.plannedPatches().getFirst();
    if (!MinecraftHookPatchPlanner.SUPPORTED_PATCH_ID.equals(patch.id())) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Unsupported Target-7 patch id for Target-8: " + patch.id(),
          true,
          true,
          true,
          1,
          patch.id(),
          patch.transformReadyForFixtureOnly(),
          patch.transformReadyForMinecraftRuntime());
    }
    if (patch.mode() != MinecraftHookPatchMode.DRY_RUN_STATIC_DISPATCH_INVOKESTATIC) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Target-8 requires dry-run-static-dispatch-invokestatic mode.",
          true,
          true,
          true,
          1,
          patch.id(),
          patch.transformReadyForFixtureOnly(),
          patch.transformReadyForMinecraftRuntime());
    }
    if (!"net/minecraft/server/Main".equals(patch.ownerInternalName())
        || !"main".equals(patch.memberName())
        || !"([Ljava/lang/String;)V".equals(patch.descriptor())) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Target-8 requires patch target net/minecraft/server/Main.main([Ljava/lang/String;)V.",
          true,
          true,
          true,
          1,
          patch.id(),
          patch.transformReadyForFixtureOnly(),
          patch.transformReadyForMinecraftRuntime());
    }
    if (patch.insertionOffset() != INSERTION_OFFSET
        || patchPlan.insertionOffset() == null
        || patchPlan.insertionOffset() != INSERTION_OFFSET) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Target-8 requires insertion offset 0.",
          true,
          true,
          true,
          1,
          patch.id(),
          patch.transformReadyForFixtureOnly(),
          patch.transformReadyForMinecraftRuntime());
    }
    if (patch.codeInsertion() == null
        || !"invokestatic".equals(patch.codeInsertion().plannedOpcode())
        || patch.codeInsertion().plannedInstructionLength() != INSERTION_LENGTH) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Target-8 requires one 3-byte invokestatic patch instruction.",
          true,
          true,
          true,
          1,
          patch.id(),
          patch.transformReadyForFixtureOnly(),
          patch.transformReadyForMinecraftRuntime());
    }
    if (!MinecraftHookPatchPlanner.DISPATCHER_OWNER_INTERNAL_NAME.equals(
            patch.codeInsertion().dispatcherOwnerInternalName())
        || !MinecraftHookPatchPlanner.DISPATCHER_METHOD_NAME.equals(
            patch.codeInsertion().dispatcherMethodName())
        || !MinecraftHookPatchPlanner.DISPATCHER_DESCRIPTOR.equals(
            patch.codeInsertion().dispatcherDescriptor())) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Target-8 requires dispatcher com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher.beforeMinecraftServerMain:()V.",
          true,
          true,
          true,
          1,
          patch.id(),
          patch.transformReadyForFixtureOnly(),
          patch.transformReadyForMinecraftRuntime());
    }
    if (!patchPlan.transformReadyForFixtureOnly() || !patch.transformReadyForFixtureOnly()) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Target-8 requires transformReadyForFixtureOnly to be true.",
          true,
          true,
          true,
          1,
          patch.id(),
          patch.transformReadyForFixtureOnly(),
          patch.transformReadyForMinecraftRuntime());
    }
    if (patchPlan.transformReadyForMinecraftRuntime()
        || patch.transformReadyForMinecraftRuntime()) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Target-8 must not report transformReadyForMinecraftRuntime.",
          true,
          true,
          true,
          1,
          patch.id(),
          patch.transformReadyForFixtureOnly(),
          true);
    }
    return new MinecraftFixtureTransformationGate(
        true, null, true, true, true, 1, patch.id(), true, false);
  }

  private MinecraftFixtureTransformationResult failedResult(
      byte[] originalClassBytes,
      MinecraftHookPatchPlan patchPlan,
      MinecraftFixtureTransformationGate gate,
      MinecraftFixtureTransformationStatus status,
      String failureReason,
      SteelHookMethodEntryRewriteResult rewriteResult) {
    MinecraftPlannedHookPatch plannedPatch =
        patchPlan == null || patchPlan.plannedPatches().isEmpty()
            ? null
            : patchPlan.plannedPatches().getFirst();
    return new MinecraftFixtureTransformationResult(
        1,
        MILESTONE_NAME,
        TRANSFORMATION_SCOPE,
        status,
        gate,
        plannedPatch == null ? null : plannedPatch.id(),
        plannedPatch == null ? null : plannedPatch.sourcePlacementId(),
        plannedPatch == null ? null : plannedPatch.sourceContractId(),
        plannedPatch == null
            ? patchPlan == null ? null : patchPlan.targetClass()
            : plannedPatch.ownerInternalName(),
        plannedPatch == null
            ? patchPlan == null ? null : patchPlan.targetMethod()
            : plannedPatch.memberName(),
        plannedPatch == null
            ? patchPlan == null ? null : patchPlan.targetDescriptor()
            : plannedPatch.descriptor(),
        patchPlan == null ? null : patchPlan.insertionOffset(),
        null,
        rewriteResult == null ? null : rewriteResult.originalClassSha256(),
        null,
        null,
        null,
        null,
        null,
        rewriteResult == null ? null : rewriteResult.constantPoolCountBefore(),
        rewriteResult == null ? null : rewriteResult.constantPoolCountAfter(),
        rewriteResult == null ? null : rewriteResult.methodrefIndex(),
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
        failureReason,
        rewriteResult == null
            ? null
            : toFixtureConstantPoolPatch(rewriteResult.constantPoolPatch()),
        rewriteResult == null ? null : toFixtureCodePatchResult(rewriteResult.codePatch()),
        rewriteResult == null ? null : toFixtureTransformedClass(rewriteResult.transformedClass()));
  }
}
