package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationGate;
import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationResult;
import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationStatus;
import com.spindle.core.minecraft.hook.bootstrap.MinecraftRuntimeClassTransformer;
import com.spindle.core.minecraft.hook.transform.SteelHookInvokeCallsiteClassFileRewriter;
import com.spindle.core.minecraft.hook.transform.SteelHookInvokeCallsiteRewriteRequest;
import com.spindle.core.minecraft.hook.transform.SteelHookInvokeCallsiteRewriteResult;
import com.spindle.core.minecraft.hook.transform.SteelHookInvokeCallsiteRewriteStatus;
import com.spindle.core.minecraft.hook.transform.SteelHookReturnValueInterceptClassFileRewriter;
import com.spindle.core.minecraft.hook.transform.SteelHookReturnValueInterceptRewriteRequest;
import com.spindle.core.minecraft.hook.transform.SteelHookReturnValueInterceptRewriteResult;
import com.spindle.core.minecraft.hook.transform.SteelHookReturnValueInterceptRewriteStatus;

public final class SteelHook04GatedRuntimeClassTransformer
    implements MinecraftRuntimeClassTransformer {
  static final String MILESTONE_NAME = "Target-35";
  private static final String TARGET_SCOPE = "steelhook-0-4-gated-runtime-class-definition-only";

  private final SteelHook04RuntimeTransformSpec spec;
  private final SteelHookReturnValueInterceptClassFileRewriter returnValueInterceptRewriter =
      new SteelHookReturnValueInterceptClassFileRewriter();
  private final SteelHookInvokeCallsiteClassFileRewriter invokeCallsiteRewriter =
      new SteelHookInvokeCallsiteClassFileRewriter();

  private volatile MinecraftBootstrapHookTransformationResult currentResult;
  private volatile SteelHookReturnValueInterceptRewriteResult
      currentReturnValueInterceptRewriteResult;
  private volatile SteelHookInvokeCallsiteRewriteResult currentInvokeCallsiteRewriteResult;

  public SteelHook04GatedRuntimeClassTransformer(SteelHook04RuntimeTransformSpec spec) {
    this.spec = spec;
  }

  @Override
  public boolean shouldTransform(String binaryName) {
    return spec != null && spec.targetBinaryName().equals(binaryName);
  }

  @Override
  public MinecraftBootstrapHookTransformationResult transform(
      String binaryName, byte[] originalClassBytes) {
    currentReturnValueInterceptRewriteResult = null;
    currentInvokeCallsiteRewriteResult = null;
    if (spec == null) {
      currentResult =
          failureResult(null, "Target-35 requires a runtime transform spec.", null, null);
      return currentResult;
    }
    if (spec.primitiveKind() == null) {
      currentResult =
          failureResult(
              spec,
              "Target-35 requires an approved SteelHook 0.4 primitive runtime plan.",
              null,
              null);
      return currentResult;
    }
    if (spec.primitiveKind() != SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT
        && spec.primitiveKind() != SteelHook04PrimitiveKind.INVOKE_REDIRECT
        && spec.primitiveKind() != SteelHook04PrimitiveKind.INVOKE_WRAP) {
      currentResult =
          failureResult(
              spec,
              "Target-35 rejects unsupported SteelHook 0.4 primitive runtime plans before class definition.",
              null,
              null);
      return currentResult;
    }
    if (!spec.targetBinaryName().equals(binaryName)) {
      currentResult =
          failureResult(
              spec, "Target-35 supports only " + spec.targetBinaryName() + ".", null, null);
      return currentResult;
    }
    if (originalClassBytes == null || originalClassBytes.length == 0) {
      currentResult =
          failureResult(
              spec,
              "Target-35 requires runtime class bytes for " + spec.targetBinaryName() + ".",
              null,
              null);
      return currentResult;
    }
    if (!spec.runtimeClassLoadingPathEnabled()) {
      currentResult =
          failureResult(
              spec,
              "Target-35 requires runtimeClassLoadingPathEnabled true before class definition proof.",
              null,
              null);
      return currentResult;
    }
    if (spec.publicApiExposed()) {
      currentResult =
          failureResult(
              spec,
              "Target-35 rejects runtime proof plans that expose public APIs before class definition.",
              null,
              null);
      return currentResult;
    }
    if (spec.javaModExecutionSandboxed()) {
      currentResult =
          failureResult(
              spec,
              "Target-35 rejects runtime proof plans that claim Java mod execution sandboxing.",
              null,
              null);
      return currentResult;
    }

    return switch (spec.primitiveKind()) {
      case RETURN_VALUE_INTERCEPT -> transformReturnValueIntercept(originalClassBytes);
      case INVOKE_REDIRECT, INVOKE_WRAP -> transformInvokeCallsite(originalClassBytes);
    };
  }

  @Override
  public MinecraftBootstrapHookTransformationResult currentResult() {
    return currentResult;
  }

  SteelHookReturnValueInterceptRewriteResult currentReturnValueInterceptRewriteResult() {
    return currentReturnValueInterceptRewriteResult;
  }

  SteelHookInvokeCallsiteRewriteResult currentInvokeCallsiteRewriteResult() {
    return currentInvokeCallsiteRewriteResult;
  }

  private MinecraftBootstrapHookTransformationResult transformReturnValueIntercept(
      byte[] originalClassBytes) {
    SteelHookReturnValueInterceptRewriteRequest request =
        new SteelHookReturnValueInterceptRewriteRequest(
            spec.id(),
            "Target-35 gated runtime class-definition proof",
            spec.sourceMilestone(),
            spec.sourceReportId(),
            spec.primitiveKind(),
            spec.returnValueInterceptMode(),
            spec.targetOwnerInternalName(),
            spec.targetBinaryName(),
            spec.targetClassEntryName(),
            spec.targetMethodName(),
            spec.targetDescriptor(),
            spec.returnValueInterceptKind(),
            spec.replacementPrimitiveValue(),
            spec.replacementReferenceValue(),
            spec.runtimeClassLoadingPathEnabled(),
            spec.publicApiExposed(),
            spec.javaModExecutionSandboxed());
    currentReturnValueInterceptRewriteResult =
        returnValueInterceptRewriter.rewrite(request, originalClassBytes);
    if (currentReturnValueInterceptRewriteResult.status()
            != SteelHookReturnValueInterceptRewriteStatus.TRANSFORMED
        || currentReturnValueInterceptRewriteResult.transformedClassBytes() == null) {
      currentResult =
          failureResult(
              spec,
              currentReturnValueInterceptRewriteResult.failureReason(),
              currentReturnValueInterceptRewriteResult,
              null);
      return currentResult;
    }
    currentResult = successResult(spec, currentReturnValueInterceptRewriteResult, null);
    return currentResult;
  }

  private MinecraftBootstrapHookTransformationResult transformInvokeCallsite(
      byte[] originalClassBytes) {
    SteelHookInvokeCallsiteRewriteRequest request =
        new SteelHookInvokeCallsiteRewriteRequest(
            spec.id(),
            "Target-35 gated runtime class-definition proof",
            spec.sourceMilestone(),
            spec.sourceReportId(),
            spec.primitiveKind(),
            spec.invokeRewriteMode(),
            spec.targetOwnerInternalName(),
            spec.targetBinaryName(),
            spec.targetClassEntryName(),
            spec.targetMethodName(),
            spec.targetDescriptor(),
            spec.expectedInvokeOwnerInternalName(),
            spec.expectedInvokeName(),
            spec.expectedInvokeDescriptor(),
            spec.expectedInvokeOpcode(),
            spec.replacementInvokeOwnerInternalName(),
            spec.replacementInvokeName(),
            spec.replacementInvokeDescriptor(),
            spec.replacementInvokeOpcode(),
            spec.runtimeClassLoadingPathEnabled(),
            spec.publicApiExposed(),
            spec.javaModExecutionSandboxed());
    currentInvokeCallsiteRewriteResult =
        invokeCallsiteRewriter.rewrite(request, originalClassBytes);
    if (currentInvokeCallsiteRewriteResult.status()
            != SteelHookInvokeCallsiteRewriteStatus.TRANSFORMED
        || currentInvokeCallsiteRewriteResult.transformedClassBytes() == null) {
      currentResult =
          failureResult(
              spec,
              currentInvokeCallsiteRewriteResult.failureReason(),
              null,
              currentInvokeCallsiteRewriteResult);
      return currentResult;
    }
    currentResult = successResult(spec, null, currentInvokeCallsiteRewriteResult);
    return currentResult;
  }

  private MinecraftBootstrapHookTransformationResult successResult(
      SteelHook04RuntimeTransformSpec spec,
      SteelHookReturnValueInterceptRewriteResult returnValueResult,
      SteelHookInvokeCallsiteRewriteResult invokeResult) {
    return new MinecraftBootstrapHookTransformationResult(
        1,
        MILESTONE_NAME,
        spec.transformationMode(),
        TARGET_SCOPE,
        MinecraftBootstrapHookTransformationStatus.TRANSFORMED,
        gate(spec, true, null),
        spec.sourceMilestone(),
        spec.targetClassEntryName(),
        spec.targetMethodName(),
        spec.targetBinaryName(),
        spec.targetInternalName(),
        spec.targetMethodName(),
        spec.targetDescriptor(),
        spec.primitiveKind().id(),
        returnValueResult != null
            ? returnValueResult.originalClassSha256()
            : invokeResult.originalClassSha256(),
        returnValueResult != null
            ? returnValueResult.transformedClassSha256()
            : invokeResult.transformedClassSha256(),
        returnValueResult != null
            ? returnValueResult.originalCodeSha256()
            : invokeResult.originalCodeSha256(),
        returnValueResult != null
            ? returnValueResult.transformedCodeSha256()
            : invokeResult.transformedCodeSha256(),
        returnValueResult != null
            ? returnValueResult.originalCodeLength()
            : invokeResult.originalCodeLength(),
        returnValueResult != null
            ? returnValueResult.transformedCodeLength()
            : invokeResult.transformedCodeLength(),
        null,
        null,
        null,
        false,
        true,
        false,
        true,
        true,
        true,
        returnValueResult != null
            ? returnValueResult.bytecodeModified()
            : invokeResult.bytecodeModified(),
        false,
        false,
        false,
        false,
        false,
        false,
        0,
        false,
        false,
        "TRANSFORMED",
        null,
        null,
        returnValueResult != null
            ? returnValueResult.transformedClassBytes()
            : invokeResult.transformedClassBytes());
  }

  private MinecraftBootstrapHookTransformationResult failureResult(
      SteelHook04RuntimeTransformSpec spec,
      String failureReason,
      SteelHookReturnValueInterceptRewriteResult returnValueResult,
      SteelHookInvokeCallsiteRewriteResult invokeResult) {
    return new MinecraftBootstrapHookTransformationResult(
        1,
        MILESTONE_NAME,
        spec == null ? null : spec.transformationMode(),
        TARGET_SCOPE,
        MinecraftBootstrapHookTransformationStatus.REJECTED,
        gate(spec, false, failureReason),
        spec == null ? null : spec.sourceMilestone(),
        spec == null ? null : spec.targetClassEntryName(),
        spec == null ? null : spec.targetMethodName(),
        spec == null ? null : spec.targetBinaryName(),
        spec == null ? null : spec.targetInternalName(),
        spec == null ? null : spec.targetMethodName(),
        spec == null ? null : spec.targetDescriptor(),
        spec == null || spec.primitiveKind() == null ? null : spec.primitiveKind().id(),
        returnValueResult != null
            ? returnValueResult.originalClassSha256()
            : invokeResult == null ? null : invokeResult.originalClassSha256(),
        returnValueResult != null
            ? returnValueResult.transformedClassSha256()
            : invokeResult == null ? null : invokeResult.transformedClassSha256(),
        returnValueResult != null
            ? returnValueResult.originalCodeSha256()
            : invokeResult == null ? null : invokeResult.originalCodeSha256(),
        returnValueResult != null
            ? returnValueResult.transformedCodeSha256()
            : invokeResult == null ? null : invokeResult.transformedCodeSha256(),
        returnValueResult != null
            ? returnValueResult.originalCodeLength()
            : invokeResult == null ? null : invokeResult.originalCodeLength(),
        returnValueResult != null
            ? returnValueResult.transformedCodeLength()
            : invokeResult == null ? null : invokeResult.transformedCodeLength(),
        null,
        null,
        null,
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
        returnValueResult != null
            ? returnValueResult.status().name()
            : invokeResult == null ? null : invokeResult.status().name(),
        returnValueResult != null
            ? returnValueResult.failureReason()
            : invokeResult == null ? null : invokeResult.failureReason(),
        failureReason,
        null);
  }

  private MinecraftBootstrapHookTransformationGate gate(
      SteelHook04RuntimeTransformSpec spec, boolean passed, String failureReason) {
    return new MinecraftBootstrapHookTransformationGate(
        passed,
        failureReason,
        1,
        MILESTONE_NAME,
        passed,
        passed,
        passed,
        passed ? 1 : 0,
        spec == null ? null : spec.id(),
        false,
        spec != null && spec.runtimeClassLoadingPathEnabled());
  }
}
