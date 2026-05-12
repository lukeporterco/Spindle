package com.spindle.core.minecraft.hook.bootstrap;

import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchPlan;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchPlanner;
import com.spindle.core.minecraft.hook.patch.MinecraftPlannedHookPatch;
import com.spindle.core.minecraft.hook.transform.MinecraftFixtureBytecodeTransformer;
import com.spindle.core.minecraft.hook.transform.MinecraftFixtureTransformationResult;
import com.spindle.core.minecraft.hook.transform.MinecraftFixtureTransformationStatus;

public final class MinecraftBootstrapHookTransformer implements MinecraftRuntimeClassTransformer {
  public static final String MILESTONE_NAME = "Target-9";
  public static final String TARGET_BINARY_NAME = "net.minecraft.server.Main";
  public static final String TARGET_INTERNAL_NAME = "net/minecraft/server/Main";
  public static final String TARGET_METHOD_NAME = "main";
  public static final String TARGET_DESCRIPTOR = "([Ljava/lang/String;)V";
  public static final String TARGET_SCOPE = "bootstrap-fake-server-only";
  public static final String DISPATCHER =
      MinecraftHookPatchPlanner.DISPATCHER_OWNER_INTERNAL_NAME
          + "."
          + MinecraftHookPatchPlanner.DISPATCHER_METHOD_NAME
          + ":"
          + MinecraftHookPatchPlanner.DISPATCHER_DESCRIPTOR;

  private final MinecraftHookPatchPlan patchPlan;
  private final MinecraftFixtureBytecodeTransformer fixtureBytecodeTransformer =
      new MinecraftFixtureBytecodeTransformer();
  private volatile MinecraftBootstrapHookTransformationResult currentResult;

  public MinecraftBootstrapHookTransformer(MinecraftHookPatchPlan patchPlan) {
    this.patchPlan = patchPlan;
  }

  @Override
  public boolean shouldTransform(String binaryName) {
    return TARGET_BINARY_NAME.equals(binaryName);
  }

  @Override
  public MinecraftBootstrapHookTransformationResult transform(
      String binaryName, byte[] originalClassBytes) {
    MinecraftBootstrapHookTransformationGate gate = evaluateGate(patchPlan);
    if (!gate.passed()) {
      currentResult =
          failedResult(
              gate,
              MinecraftBootstrapHookTransformationStatus.PATCH_PLAN_GATE_FAILED,
              gate.failureReason(),
              null,
              null);
      return currentResult;
    }
    if (!shouldTransform(binaryName)) {
      currentResult =
          failedResult(
              gate,
              MinecraftBootstrapHookTransformationStatus.REJECTED,
              "Target-9 supports only net.minecraft.server.Main.",
              null,
              null);
      return currentResult;
    }
    if (originalClassBytes == null || originalClassBytes.length == 0) {
      currentResult =
          failedResult(
              gate,
              MinecraftBootstrapHookTransformationStatus.REJECTED,
              "Target-9 requires runtime class bytes for net.minecraft.server.Main.",
              null,
              null);
      return currentResult;
    }

    MinecraftFixtureTransformationResult fixtureResult =
        fixtureBytecodeTransformer.transformFixtureClass(originalClassBytes, patchPlan);
    currentResult = fromFixtureResult(gate, fixtureResult);
    return currentResult;
  }

  @Override
  public MinecraftBootstrapHookTransformationResult currentResult() {
    return currentResult;
  }

  public MinecraftBootstrapHookTransformationResult withRuntimeObservation(
      int dispatcherInvocationCount, boolean minecraftMainInvoked) {
    MinecraftBootstrapHookTransformationResult result = currentResult;
    if (result == null) {
      return null;
    }
    MinecraftBootstrapHookTransformationResult updated =
        new MinecraftBootstrapHookTransformationResult(
            result.schema(),
            result.milestoneName(),
            result.transformationMode(),
            result.scope(),
            result.status(),
            result.gate(),
            result.sourcePatchId(),
            result.sourcePlacementId(),
            result.sourceContractId(),
            result.targetBinaryName(),
            result.targetInternalName(),
            result.targetMethod(),
            result.targetDescriptor(),
            result.dispatcher(),
            result.originalClassSha256(),
            result.transformedClassSha256(),
            result.originalCodeSha256(),
            result.transformedCodeSha256(),
            result.originalCodeLength(),
            result.transformedCodeLength(),
            result.constantPoolCountBefore(),
            result.constantPoolCountAfter(),
            result.methodrefIndex(),
            result.bootstrapTransformationEnabled(),
            result.runtimeClassLoaderTransformationEnabled(),
            result.fakeServerRuntimeTransformed(),
            result.realMinecraftRuntimeTransformed(),
            result.transformationOccurred(),
            result.patchingOccurred(),
            result.bytecodeModified(),
            result.publicApiExposed(),
            result.javaAgentUsed(),
            result.mixinUsed(),
            result.remappingOccurred(),
            result.accessWidenersUsed(),
            result.javaModExecutionSandboxed(),
            dispatcherInvocationCount,
            dispatcherInvocationCount > 0,
            minecraftMainInvoked,
            result.fixtureTransformationStatus(),
            result.fixtureTransformationFailureReason(),
            result.failureReason(),
            result.transformedClassBytes());
    currentResult = updated;
    return updated;
  }

  private MinecraftBootstrapHookTransformationResult fromFixtureResult(
      MinecraftBootstrapHookTransformationGate gate, MinecraftFixtureTransformationResult result) {
    if (result.status() != MinecraftFixtureTransformationStatus.TRANSFORMED
        || result.transformedClass() == null) {
      return failedResult(
          gate,
          MinecraftBootstrapHookTransformationStatus.REJECTED,
          result.failureReason(),
          result,
          null);
    }
    return new MinecraftBootstrapHookTransformationResult(
        1,
        MILESTONE_NAME,
        MinecraftBootstrapHookTransformationMode.BOOTSTRAP_FAKE_SERVER_METHOD_ENTRY_TRANSFORM,
        TARGET_SCOPE,
        MinecraftBootstrapHookTransformationStatus.TRANSFORMED,
        gate,
        result.sourcePatchId(),
        result.sourcePlacementId(),
        result.sourceContractId(),
        TARGET_BINARY_NAME,
        TARGET_INTERNAL_NAME,
        TARGET_METHOD_NAME,
        TARGET_DESCRIPTOR,
        DISPATCHER,
        result.originalClassSha256(),
        result.transformedClassSha256(),
        result.originalCodeSha256(),
        result.transformedCodeSha256(),
        result.originalCodeLength(),
        result.transformedCodeLength(),
        result.constantPoolCountBefore(),
        result.constantPoolCountAfter(),
        result.methodrefIndex(),
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
        0,
        false,
        false,
        result.status().id(),
        result.failureReason(),
        null,
        result.transformedClass().classBytes());
  }

  private MinecraftBootstrapHookTransformationResult failedResult(
      MinecraftBootstrapHookTransformationGate gate,
      MinecraftBootstrapHookTransformationStatus status,
      String failureReason,
      MinecraftFixtureTransformationResult fixtureResult,
      byte[] transformedClassBytes) {
    MinecraftPlannedHookPatch patch =
        patchPlan == null || patchPlan.plannedPatches().isEmpty()
            ? null
            : patchPlan.plannedPatches().getFirst();
    return new MinecraftBootstrapHookTransformationResult(
        1,
        MILESTONE_NAME,
        MinecraftBootstrapHookTransformationMode.BOOTSTRAP_FAKE_SERVER_METHOD_ENTRY_TRANSFORM,
        TARGET_SCOPE,
        status,
        gate,
        patch == null ? null : patch.id(),
        patch == null ? null : patch.sourcePlacementId(),
        patch == null ? null : patch.sourceContractId(),
        TARGET_BINARY_NAME,
        TARGET_INTERNAL_NAME,
        TARGET_METHOD_NAME,
        TARGET_DESCRIPTOR,
        DISPATCHER,
        fixtureResult == null ? null : fixtureResult.originalClassSha256(),
        fixtureResult == null ? null : fixtureResult.transformedClassSha256(),
        fixtureResult == null ? null : fixtureResult.originalCodeSha256(),
        fixtureResult == null ? null : fixtureResult.transformedCodeSha256(),
        fixtureResult == null ? null : fixtureResult.originalCodeLength(),
        fixtureResult == null ? null : fixtureResult.transformedCodeLength(),
        fixtureResult == null ? null : fixtureResult.constantPoolCountBefore(),
        fixtureResult == null ? null : fixtureResult.constantPoolCountAfter(),
        fixtureResult == null ? null : fixtureResult.methodrefIndex(),
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
        false,
        0,
        false,
        false,
        fixtureResult == null ? null : fixtureResult.status().id(),
        fixtureResult == null ? null : fixtureResult.failureReason(),
        failureReason,
        transformedClassBytes);
  }

  private MinecraftBootstrapHookTransformationGate evaluateGate(MinecraftHookPatchPlan plan) {
    if (plan == null) {
      return new MinecraftBootstrapHookTransformationGate(
          false,
          "Target-9 requires a Target-7 patch plan.",
          null,
          null,
          false,
          false,
          false,
          0,
          null,
          false,
          false);
    }
    if (plan.schema() != 1) {
      return gateFailure(plan, "Target-9 requires Target-7 patch plan schema 1.");
    }
    if (!MinecraftHookPatchPlanner.MILESTONE_NAME.equals(plan.milestoneName())) {
      return gateFailure(plan, "Target-9 requires Target-7 patch plan milestone.");
    }
    if (!plan.gatePassed()) {
      return gateFailure(plan, "Target-9 requires a passing Target-7 patch plan gate.");
    }
    if (!plan.patchPlanningSucceeded() || !plan.patchPlanned()) {
      return gateFailure(plan, "Target-9 requires a successful Target-7 patch plan.");
    }
    if (plan.plannedPatchCount() != 1 || plan.plannedPatches().size() != 1) {
      return gateFailure(plan, "Target-9 requires exactly one planned Target-7 patch.");
    }
    MinecraftPlannedHookPatch patch = plan.plannedPatches().getFirst();
    if (!MinecraftHookPatchPlanner.SUPPORTED_PATCH_ID.equals(patch.id())) {
      return gateFailure(plan, "Unsupported Target-7 patch id for Target-9: " + patch.id());
    }
    if (!TARGET_INTERNAL_NAME.equals(patch.ownerInternalName())
        || !TARGET_METHOD_NAME.equals(patch.memberName())
        || !TARGET_DESCRIPTOR.equals(patch.descriptor())) {
      return gateFailure(
          plan,
          "Target-9 requires patch target net/minecraft/server/Main.main([Ljava/lang/String;)V.");
    }
    if (patch.codeInsertion() == null
        || !MinecraftHookPatchPlanner.DISPATCHER_OWNER_INTERNAL_NAME.equals(
            patch.codeInsertion().dispatcherOwnerInternalName())
        || !MinecraftHookPatchPlanner.DISPATCHER_METHOD_NAME.equals(
            patch.codeInsertion().dispatcherMethodName())
        || !MinecraftHookPatchPlanner.DISPATCHER_DESCRIPTOR.equals(
            patch.codeInsertion().dispatcherDescriptor())) {
      return gateFailure(
          plan,
          "Target-9 requires dispatcher com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher.beforeMinecraftServerMain:()V.");
    }
    if (!plan.transformReadyForFixtureOnly() || !patch.transformReadyForFixtureOnly()) {
      return gateFailure(plan, "Target-9 requires transformReadyForFixtureOnly to be true.");
    }
    if (plan.transformReadyForMinecraftRuntime() || patch.transformReadyForMinecraftRuntime()) {
      return gateFailure(plan, "Target-9 must not report transformReadyForMinecraftRuntime.");
    }
    return new MinecraftBootstrapHookTransformationGate(
        true,
        null,
        plan.schema(),
        plan.milestoneName(),
        plan.gatePassed(),
        plan.patchPlanningSucceeded(),
        plan.patchPlanned(),
        plan.plannedPatchCount(),
        patch.id(),
        true,
        false);
  }

  private MinecraftBootstrapHookTransformationGate gateFailure(
      MinecraftHookPatchPlan plan, String failureReason) {
    String selectedPatchId =
        plan.plannedPatches().isEmpty() ? null : plan.plannedPatches().getFirst().id();
    return new MinecraftBootstrapHookTransformationGate(
        false,
        failureReason,
        plan.schema(),
        plan.milestoneName(),
        plan.gatePassed(),
        plan.patchPlanningSucceeded(),
        plan.patchPlanned(),
        plan.plannedPatchCount(),
        selectedPatchId,
        plan.transformReadyForFixtureOnly(),
        plan.transformReadyForMinecraftRuntime());
  }
}
