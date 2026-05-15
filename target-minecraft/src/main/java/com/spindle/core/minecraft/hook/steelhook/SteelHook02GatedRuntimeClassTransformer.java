package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationGate;
import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationMode;
import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationResult;
import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationStatus;
import com.spindle.core.minecraft.hook.bootstrap.MinecraftRuntimeClassTransformer;
import com.spindle.core.minecraft.hook.transform.SteelHookMethodEntryClassFileRewriter;
import com.spindle.core.minecraft.hook.transform.SteelHookMethodEntryRewriteRequest;
import com.spindle.core.minecraft.hook.transform.SteelHookMethodEntryRewriteResult;
import com.spindle.core.minecraft.hook.transform.SteelHookMethodEntryRewriteStatus;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class SteelHook02GatedRuntimeClassTransformer
    implements MinecraftRuntimeClassTransformer {
  public static final String MILESTONE_NAME = "Target-26";
  public static final String TARGET_BINARY_NAME = "net.minecraft.server.Main";
  public static final String TARGET_INTERNAL_NAME = "net/minecraft/server/Main";
  public static final String TARGET_CLASS_ENTRY_NAME = "net/minecraft/server/Main.class";
  public static final String TARGET_METHOD_NAME = "main";
  public static final String TARGET_DESCRIPTOR = "([Ljava/lang/String;)V";
  public static final String TARGET_SCOPE = "steelhook-0-2-gated-runtime-classloader-only";
  private static final String EXPECTED_DISPATCHER_OWNER =
      "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher";
  private static final String EXPECTED_DISPATCHER_BINARY =
      "com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher";
  private static final String EXPECTED_DISPATCHER_METHOD = "beforeMinecraftServerMain";
  private static final String EXPECTED_DISPATCHER_DESCRIPTOR = "()V";

  private final SteelHook02ContractGeneralizationAnalysis contractGeneralizationAnalysis;
  private final SteelHook02MethodEntryTransformerResult methodEntryTransformerResult;
  private final SteelHookMethodEntryClassFileRewriter rewriter =
      new SteelHookMethodEntryClassFileRewriter();
  private volatile MinecraftBootstrapHookTransformationResult currentResult;

  public SteelHook02GatedRuntimeClassTransformer(
      SteelHook02ContractGeneralizationAnalysis contractGeneralizationAnalysis,
      SteelHook02MethodEntryTransformerResult methodEntryTransformerResult) {
    this.contractGeneralizationAnalysis = contractGeneralizationAnalysis;
    this.methodEntryTransformerResult = methodEntryTransformerResult;
  }

  @Override
  public boolean shouldTransform(String binaryName) {
    return TARGET_BINARY_NAME.equals(binaryName);
  }

  @Override
  public MinecraftBootstrapHookTransformationResult transform(
      String binaryName, byte[] originalClassBytes) {
    String gateFailure = validateTarget25Handoff();
    MinecraftBootstrapHookTransformationGate gate = gate(gateFailure == null, gateFailure);
    if (gateFailure != null) {
      currentResult =
          failureResult(
              MinecraftBootstrapHookTransformationStatus.PATCH_PLAN_GATE_FAILED,
              gateFailure,
              gate,
              null);
      return currentResult;
    }
    String descriptorFailure = validateDescriptorShape();
    if (descriptorFailure != null) {
      currentResult =
          failureResult(
              MinecraftBootstrapHookTransformationStatus.REJECTED,
              descriptorFailure,
              gate(false, descriptorFailure),
              null);
      return currentResult;
    }
    if (!shouldTransform(binaryName)) {
      currentResult =
          failureResult(
              MinecraftBootstrapHookTransformationStatus.REJECTED,
              "Target-26 supports only net.minecraft.server.Main.",
              gate(false, "Target-26 supports only net.minecraft.server.Main."),
              null);
      return currentResult;
    }
    if (originalClassBytes == null || originalClassBytes.length == 0) {
      currentResult =
          failureResult(
              MinecraftBootstrapHookTransformationStatus.REJECTED,
              "Target-26 requires runtime class bytes for net.minecraft.server.Main.",
              gate(false, "Target-26 requires runtime class bytes for net.minecraft.server.Main."),
              null);
      return currentResult;
    }
    String originalClassSha256 = sha256Hex(originalClassBytes);
    if (!originalClassSha256.equals(methodEntryTransformerResult.originalClassSha256())) {
      currentResult =
          failureResult(
              MinecraftBootstrapHookTransformationStatus.REJECTED,
              "Target-26 original class hash drifted from the approved Target-25 handoff.",
              gate(
                  false,
                  "Target-26 original class hash drifted from the approved Target-25 handoff."),
              null);
      return currentResult;
    }

    SteelHook02TargetDescriptor targetDescriptor =
        contractGeneralizationAnalysis.targetDescriptor();
    SteelHook02DispatcherDescriptor dispatcherDescriptor =
        contractGeneralizationAnalysis.dispatcherDescriptor();
    SteelHookMethodEntryRewriteRequest request =
        new SteelHookMethodEntryRewriteRequest(
            contractGeneralizationAnalysis.generalizedPatchPlan().id(),
            "Target-26 gated runtime classloader transformation",
            contractGeneralizationAnalysis.generalizedPatchPlan().sourcePatchId(),
            targetDescriptor.sourcePlacementId(),
            targetDescriptor.sourceContractId(),
            targetDescriptor.ownerInternalName(),
            targetDescriptor.binaryName(),
            targetDescriptor.classEntryName(),
            targetDescriptor.memberName(),
            targetDescriptor.descriptor(),
            targetDescriptor.insertionOffset(),
            dispatcherDescriptor.ownerInternalName(),
            dispatcherDescriptor.binaryName(),
            dispatcherDescriptor.methodName(),
            dispatcherDescriptor.descriptor(),
            dispatcherDescriptor.opcodeMnemonic(),
            dispatcherDescriptor.opcodeHex(),
            dispatcherDescriptor.instructionLength(),
            false,
            true,
            false,
            false);
    SteelHookMethodEntryRewriteResult rewriteResult = rewriter.rewrite(request, originalClassBytes);
    if (rewriteResult.status() != SteelHookMethodEntryRewriteStatus.TRANSFORMED
        || rewriteResult.transformedClass() == null
        || rewriteResult.transformedClass().classBytes() == null) {
      currentResult =
          failureResult(
              MinecraftBootstrapHookTransformationStatus.REJECTED,
              rewriteResult.failureReason(),
              gate(false, rewriteResult.failureReason()),
              rewriteResult);
      return currentResult;
    }
    if (!methodEntryTransformerResult
        .transformedClassSha256()
        .equals(rewriteResult.transformedClassSha256())) {
      currentResult =
          failureResult(
              MinecraftBootstrapHookTransformationStatus.REJECTED,
              "Target-26 transformed class hash drifted from the approved Target-25 handoff.",
              gate(
                  false,
                  "Target-26 transformed class hash drifted from the approved Target-25 handoff."),
              rewriteResult);
      return currentResult;
    }
    currentResult =
        new MinecraftBootstrapHookTransformationResult(
            1,
            MILESTONE_NAME,
            MinecraftBootstrapHookTransformationMode
                .STEELHOOK_0_2_GATED_RUNTIME_METHOD_ENTRY_TRANSFORM,
            TARGET_SCOPE,
            MinecraftBootstrapHookTransformationStatus.TRANSFORMED,
            gate,
            request.sourcePatchId(),
            request.sourcePlacementId(),
            request.sourceContractId(),
            TARGET_BINARY_NAME,
            TARGET_INTERNAL_NAME,
            TARGET_METHOD_NAME,
            TARGET_DESCRIPTOR,
            dispatcherDescriptor.ownerInternalName()
                + "."
                + dispatcherDescriptor.methodName()
                + ":"
                + dispatcherDescriptor.descriptor(),
            rewriteResult.originalClassSha256(),
            rewriteResult.transformedClassSha256(),
            rewriteResult.originalCodeSha256(),
            rewriteResult.transformedCodeSha256(),
            rewriteResult.originalCodeLength(),
            rewriteResult.transformedCodeLength(),
            rewriteResult.constantPoolCountBefore(),
            rewriteResult.constantPoolCountAfter(),
            rewriteResult.methodrefIndex(),
            false,
            true,
            false,
            true,
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
            rewriteResult.status().name(),
            rewriteResult.failureReason(),
            null,
            rewriteResult.transformedClass().classBytes());
    return currentResult;
  }

  @Override
  public MinecraftBootstrapHookTransformationResult currentResult() {
    return currentResult;
  }

  private String validateTarget25Handoff() {
    if (methodEntryTransformerResult == null) {
      return "Target-26 requires a Target-25 method-entry transformer result.";
    }
    if (contractGeneralizationAnalysis == null) {
      return "Target-26 requires a Target-24 contract generalization analysis.";
    }
    if (!methodEntryTransformerResult.gatePassed()
        || methodEntryTransformerResult.status()
            != SteelHook02MethodEntryTransformerStatus.TRANSFORMED
        || methodEntryTransformerResult.nextDirection()
            != SteelHook02MethodEntryTransformerNextDirection
                .MOVE_TO_TARGET_26_GATED_REAL_RUNTIME_TRANSFORMATION
        || !methodEntryTransformerResult.eligibleForTarget26GatedRuntimeTransformation()
        || !methodEntryTransformerResult.transformedClassBytesProduced()
        || !methodEntryTransformerResult.bytecodeModified()
        || !methodEntryTransformerResult.methodEntryTransformationOccurred()
        || methodEntryTransformerResult.runtimeClassLoadingPathEnabled()
        || methodEntryTransformerResult.classLoadingOccurred()
        || methodEntryTransformerResult.hookInstallationOccurred()
        || methodEntryTransformerResult.runtimeDispatchOccurred()
        || methodEntryTransformerResult.realMinecraftRuntimeTransformed()
        || methodEntryTransformerResult.minecraftRuntimeTransformReady()
        || methodEntryTransformerResult.publicApiExposed()
        || methodEntryTransformerResult.javaModExecutionSandboxed()) {
      return "Target-25 did not preserve the approved offline-only handoff for Target-26.";
    }
    return null;
  }

  private String validateDescriptorShape() {
    SteelHook02TargetDescriptor targetDescriptor =
        contractGeneralizationAnalysis.targetDescriptor();
    SteelHook02DispatcherDescriptor dispatcherDescriptor =
        contractGeneralizationAnalysis.dispatcherDescriptor();
    SteelHook02PrimitiveContract primitiveContract =
        contractGeneralizationAnalysis.primitiveContract();
    if (targetDescriptor == null
        || dispatcherDescriptor == null
        || primitiveContract == null
        || contractGeneralizationAnalysis.generalizedPatchPlan() == null) {
      return "Target-26 requires the approved Target-24 descriptor set.";
    }
    if (!TARGET_INTERNAL_NAME.equals(targetDescriptor.ownerInternalName())
        || !TARGET_BINARY_NAME.equals(targetDescriptor.binaryName())
        || !TARGET_CLASS_ENTRY_NAME.equals(targetDescriptor.classEntryName())
        || !TARGET_METHOD_NAME.equals(targetDescriptor.memberName())
        || !TARGET_DESCRIPTOR.equals(targetDescriptor.descriptor())
        || targetDescriptor.insertionOffset() != 0
        || !EXPECTED_DISPATCHER_OWNER.equals(dispatcherDescriptor.ownerInternalName())
        || !EXPECTED_DISPATCHER_BINARY.equals(dispatcherDescriptor.binaryName())
        || !EXPECTED_DISPATCHER_METHOD.equals(dispatcherDescriptor.methodName())
        || !EXPECTED_DISPATCHER_DESCRIPTOR.equals(dispatcherDescriptor.descriptor())
        || !"invokestatic".equals(dispatcherDescriptor.opcodeMnemonic())
        || !"b8".equals(dispatcherDescriptor.opcodeHex())
        || dispatcherDescriptor.instructionLength() != 3
        || !dispatcherDescriptor.requiresVoidNoArgs()
        || dispatcherDescriptor.publicApiExposed()
        || primitiveContract.publicApiExposed()) {
      return "Target-24 descriptors drifted from the single approved method-entry static-dispatch shape.";
    }
    return null;
  }

  private MinecraftBootstrapHookTransformationGate gate(boolean passed, String failureReason) {
    return new MinecraftBootstrapHookTransformationGate(
        passed,
        failureReason,
        methodEntryTransformerResult == null ? null : methodEntryTransformerResult.schema(),
        methodEntryTransformerResult == null ? null : methodEntryTransformerResult.milestoneName(),
        methodEntryTransformerResult != null && methodEntryTransformerResult.gatePassed(),
        methodEntryTransformerResult != null
            && methodEntryTransformerResult.status()
                == SteelHook02MethodEntryTransformerStatus.TRANSFORMED,
        methodEntryTransformerResult != null
            && methodEntryTransformerResult.transformedClassBytesProduced(),
        methodEntryTransformerResult != null
                && methodEntryTransformerResult.transformedClassBytesProduced()
            ? 1
            : 0,
        methodEntryTransformerResult == null
                || methodEntryTransformerResult.generalizedPatchPlan() == null
            ? null
            : methodEntryTransformerResult.generalizedPatchPlan().sourcePatchId(),
        false,
        true);
  }

  private MinecraftBootstrapHookTransformationResult failureResult(
      MinecraftBootstrapHookTransformationStatus status,
      String failureReason,
      MinecraftBootstrapHookTransformationGate gate,
      SteelHookMethodEntryRewriteResult rewriteResult) {
    return new MinecraftBootstrapHookTransformationResult(
        1,
        MILESTONE_NAME,
        MinecraftBootstrapHookTransformationMode.STEELHOOK_0_2_GATED_RUNTIME_METHOD_ENTRY_TRANSFORM,
        TARGET_SCOPE,
        status,
        gate,
        methodEntryTransformerResult == null
                || methodEntryTransformerResult.generalizedPatchPlan() == null
            ? null
            : methodEntryTransformerResult.generalizedPatchPlan().sourcePatchId(),
        methodEntryTransformerResult == null
                || methodEntryTransformerResult.targetDescriptor() == null
            ? null
            : methodEntryTransformerResult.targetDescriptor().sourcePlacementId(),
        methodEntryTransformerResult == null
                || methodEntryTransformerResult.targetDescriptor() == null
            ? null
            : methodEntryTransformerResult.targetDescriptor().sourceContractId(),
        TARGET_BINARY_NAME,
        TARGET_INTERNAL_NAME,
        TARGET_METHOD_NAME,
        TARGET_DESCRIPTOR,
        EXPECTED_DISPATCHER_OWNER
            + "."
            + EXPECTED_DISPATCHER_METHOD
            + ":"
            + EXPECTED_DISPATCHER_DESCRIPTOR,
        rewriteResult == null ? null : rewriteResult.originalClassSha256(),
        rewriteResult == null ? null : rewriteResult.transformedClassSha256(),
        rewriteResult == null ? null : rewriteResult.originalCodeSha256(),
        rewriteResult == null ? null : rewriteResult.transformedCodeSha256(),
        rewriteResult == null ? null : rewriteResult.originalCodeLength(),
        rewriteResult == null ? null : rewriteResult.transformedCodeLength(),
        rewriteResult == null ? null : rewriteResult.constantPoolCountBefore(),
        rewriteResult == null ? null : rewriteResult.constantPoolCountAfter(),
        rewriteResult == null ? null : rewriteResult.methodrefIndex(),
        false,
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
        rewriteResult == null ? null : rewriteResult.status().name(),
        rewriteResult == null ? null : rewriteResult.failureReason(),
        failureReason,
        null);
  }

  private String sha256Hex(byte[] bytes) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(bytes);
      StringBuilder builder = new StringBuilder(hash.length * 2);
      for (byte value : hash) {
        builder.append(Character.forDigit((value >>> 4) & 0xF, 16));
        builder.append(Character.forDigit(value & 0xF, 16));
      }
      return builder.toString();
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException(
          "SHA-256 is unavailable for Target-26 transformation.", exception);
    }
  }
}
