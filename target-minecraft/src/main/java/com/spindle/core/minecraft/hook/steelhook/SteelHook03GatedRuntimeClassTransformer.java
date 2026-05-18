package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationGate;
import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationResult;
import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationStatus;
import com.spindle.core.minecraft.hook.bootstrap.MinecraftRuntimeClassTransformer;
import com.spindle.core.minecraft.hook.transform.SteelHookMethodEntryClassFileRewriter;
import com.spindle.core.minecraft.hook.transform.SteelHookMethodEntryRewriteRequest;
import com.spindle.core.minecraft.hook.transform.SteelHookMethodEntryRewriteResult;
import com.spindle.core.minecraft.hook.transform.SteelHookMethodEntryRewriteStatus;
import com.spindle.core.minecraft.hook.transform.SteelHookMethodExitClassFileRewriter;
import com.spindle.core.minecraft.hook.transform.SteelHookMethodExitRewriteRequest;
import com.spindle.core.minecraft.hook.transform.SteelHookMethodExitRewriteResult;
import com.spindle.core.minecraft.hook.transform.SteelHookMethodExitRewriteStatus;

public final class SteelHook03GatedRuntimeClassTransformer
    implements MinecraftRuntimeClassTransformer {
  static final String MILESTONE_NAME = "Target-30";
  private static final String TARGET_SCOPE = "steelhook-0-3-gated-runtime-classloader-only";

  private final SteelHook03RuntimeTransformSpec spec;
  private final SteelHookMethodEntryClassFileRewriter methodEntryRewriter =
      new SteelHookMethodEntryClassFileRewriter();
  private final SteelHookMethodExitClassFileRewriter methodExitRewriter =
      new SteelHookMethodExitClassFileRewriter();

  private volatile MinecraftBootstrapHookTransformationResult currentResult;
  private volatile SteelHookMethodEntryRewriteResult currentMethodEntryRewriteResult;
  private volatile SteelHookMethodExitRewriteResult currentMethodExitRewriteResult;

  public SteelHook03GatedRuntimeClassTransformer(SteelHook03RuntimeTransformSpec spec) {
    this.spec = spec;
  }

  @Override
  public boolean shouldTransform(String binaryName) {
    return spec != null && spec.targetBinaryName().equals(binaryName);
  }

  @Override
  public MinecraftBootstrapHookTransformationResult transform(
      String binaryName, byte[] originalClassBytes) {
    currentMethodEntryRewriteResult = null;
    currentMethodExitRewriteResult = null;
    if (spec == null) {
      currentResult =
          failureResult(
              null,
              MinecraftBootstrapHookTransformationStatus.REJECTED,
              "Target-30 requires a runtime transform spec.",
              null,
              null);
      return currentResult;
    }
    if (spec.primitiveKind() == null) {
      currentResult =
          failureResult(
              spec,
              MinecraftBootstrapHookTransformationStatus.REJECTED,
              "Target-30 requires an approved SteelHook 0.3 primitive kind.",
              null,
              null);
      return currentResult;
    }
    if (!spec.targetBinaryName().equals(binaryName)) {
      currentResult =
          failureResult(
              spec,
              MinecraftBootstrapHookTransformationStatus.REJECTED,
              "Target-30 supports only " + spec.targetBinaryName() + ".",
              null,
              null);
      return currentResult;
    }
    if (originalClassBytes == null || originalClassBytes.length == 0) {
      currentResult =
          failureResult(
              spec,
              MinecraftBootstrapHookTransformationStatus.REJECTED,
              "Target-30 requires runtime class bytes for " + spec.targetBinaryName() + ".",
              null,
              null);
      return currentResult;
    }

    return switch (spec.primitiveKind()) {
      case METHOD_ENTRY_STATIC_DISPATCH -> transformMethodEntry(binaryName, originalClassBytes);
      case METHOD_EXIT_STATIC_DISPATCH -> transformMethodExit(binaryName, originalClassBytes);
    };
  }

  @Override
  public MinecraftBootstrapHookTransformationResult currentResult() {
    return currentResult;
  }

  SteelHookMethodEntryRewriteResult currentMethodEntryRewriteResult() {
    return currentMethodEntryRewriteResult;
  }

  SteelHookMethodExitRewriteResult currentMethodExitRewriteResult() {
    return currentMethodExitRewriteResult;
  }

  private MinecraftBootstrapHookTransformationResult transformMethodEntry(
      String binaryName, byte[] originalClassBytes) {
    SteelHookMethodEntryRewriteRequest request =
        new SteelHookMethodEntryRewriteRequest(
            "target-30.runtime.method-entry.001",
            "Target-30 gated runtime proof method entry",
            "target-30.runtime.method-entry.001",
            "target-30.runtime.method-entry.placement.001",
            "target-30.runtime.method-entry.contract.001",
            spec.targetInternalName(),
            binaryName,
            spec.targetClassEntryName(),
            spec.targetMethodName(),
            spec.targetDescriptor(),
            spec.insertionOffset() == null ? 0 : spec.insertionOffset(),
            spec.dispatcherOwnerInternalName(),
            spec.dispatcherBinaryName(),
            spec.dispatcherMethodName(),
            spec.dispatcherDescriptor(),
            spec.opcodeMnemonic(),
            spec.opcodeHex(),
            spec.instructionLength(),
            spec.stackMapTableRewriteSupported(),
            spec.runtimeClassLoadingPathEnabled(),
            spec.publicApiExposed(),
            spec.javaModExecutionSandboxed());
    currentMethodEntryRewriteResult = methodEntryRewriter.rewrite(request, originalClassBytes);
    if (currentMethodEntryRewriteResult.status() != SteelHookMethodEntryRewriteStatus.TRANSFORMED
        || currentMethodEntryRewriteResult.transformedClass() == null
        || currentMethodEntryRewriteResult.transformedClass().classBytes() == null) {
      currentResult =
          failureResult(
              spec,
              MinecraftBootstrapHookTransformationStatus.REJECTED,
              currentMethodEntryRewriteResult.failureReason(),
              currentMethodEntryRewriteResult,
              null);
      return currentResult;
    }
    currentResult =
        successResult(
            spec,
            currentMethodEntryRewriteResult.originalClassSha256(),
            currentMethodEntryRewriteResult.transformedClassSha256(),
            currentMethodEntryRewriteResult.originalCodeSha256(),
            currentMethodEntryRewriteResult.transformedCodeSha256(),
            currentMethodEntryRewriteResult.originalCodeLength(),
            currentMethodEntryRewriteResult.transformedCodeLength(),
            currentMethodEntryRewriteResult.constantPoolCountBefore(),
            currentMethodEntryRewriteResult.constantPoolCountAfter(),
            currentMethodEntryRewriteResult.methodrefIndex(),
            currentMethodEntryRewriteResult.methodEntryTransformationOccurred(),
            false,
            currentMethodEntryRewriteResult.bytecodeModified(),
            currentMethodEntryRewriteResult.transformedClassBytesProduced(),
            currentMethodEntryRewriteResult.transformedClass().classBytes(),
            null,
            currentMethodEntryRewriteResult.failureReason());
    return currentResult;
  }

  private MinecraftBootstrapHookTransformationResult transformMethodExit(
      String binaryName, byte[] originalClassBytes) {
    SteelHookMethodExitRewriteRequest request =
        new SteelHookMethodExitRewriteRequest(
            "target-30.runtime.method-exit.001",
            "Target-30 gated runtime proof method exit",
            spec.sourceMilestone(),
            SteelHook03MethodExitDispatchRunner.REPORT_FILE_NAME,
            spec.targetInternalName(),
            binaryName,
            spec.targetClassEntryName(),
            spec.targetMethodName(),
            spec.targetDescriptor(),
            spec.dispatcherOwnerInternalName(),
            spec.dispatcherBinaryName(),
            spec.dispatcherMethodName(),
            spec.dispatcherDescriptor(),
            spec.opcodeMnemonic(),
            spec.opcodeHex(),
            spec.instructionLength(),
            spec.stackMapTableRewriteSupported(),
            spec.runtimeClassLoadingPathEnabled(),
            spec.publicApiExposed(),
            spec.javaModExecutionSandboxed());
    currentMethodExitRewriteResult = methodExitRewriter.rewrite(request, originalClassBytes);
    if (currentMethodExitRewriteResult.status() != SteelHookMethodExitRewriteStatus.TRANSFORMED
        || currentMethodExitRewriteResult.transformedClass() == null
        || currentMethodExitRewriteResult.transformedClass().classBytes() == null) {
      currentResult =
          failureResult(
              spec,
              MinecraftBootstrapHookTransformationStatus.REJECTED,
              currentMethodExitRewriteResult.failureReason(),
              null,
              currentMethodExitRewriteResult);
      return currentResult;
    }
    currentResult =
        successResult(
            spec,
            currentMethodExitRewriteResult.originalClassSha256(),
            currentMethodExitRewriteResult.transformedClassSha256(),
            currentMethodExitRewriteResult.originalCodeSha256(),
            currentMethodExitRewriteResult.transformedCodeSha256(),
            currentMethodExitRewriteResult.originalCodeLength(),
            currentMethodExitRewriteResult.transformedCodeLength(),
            currentMethodExitRewriteResult.constantPoolCountBefore(),
            currentMethodExitRewriteResult.constantPoolCountAfter(),
            currentMethodExitRewriteResult.methodrefIndex(),
            false,
            currentMethodExitRewriteResult.methodExitTransformationOccurred(),
            currentMethodExitRewriteResult.bytecodeModified(),
            currentMethodExitRewriteResult.transformedClassBytesProduced(),
            currentMethodExitRewriteResult.transformedClass().classBytes(),
            null,
            currentMethodExitRewriteResult.failureReason());
    return currentResult;
  }

  private MinecraftBootstrapHookTransformationResult successResult(
      SteelHook03RuntimeTransformSpec spec,
      String originalClassSha256,
      String transformedClassSha256,
      String originalCodeSha256,
      String transformedCodeSha256,
      Integer originalCodeLength,
      Integer transformedCodeLength,
      Integer constantPoolCountBefore,
      Integer constantPoolCountAfter,
      Integer methodrefIndex,
      boolean methodEntryTransformationOccurred,
      boolean methodExitTransformationOccurred,
      boolean bytecodeModified,
      boolean transformedClassBytesProduced,
      byte[] transformedClassBytes,
      String fixtureFailureReason,
      String failureReason) {
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
        spec.dispatcherOwnerInternalName()
            + "."
            + spec.dispatcherMethodName()
            + ":"
            + spec.dispatcherDescriptor(),
        originalClassSha256,
        transformedClassSha256,
        originalCodeSha256,
        transformedCodeSha256,
        originalCodeLength,
        transformedCodeLength,
        constantPoolCountBefore,
        constantPoolCountAfter,
        methodrefIndex,
        false,
        true,
        false,
        true,
        true,
        true,
        bytecodeModified,
        false,
        false,
        false,
        false,
        false,
        false,
        0,
        false,
        false,
        transformedClassBytesProduced ? "TRANSFORMED" : "REJECTED",
        fixtureFailureReason,
        failureReason,
        transformedClassBytes);
  }

  private MinecraftBootstrapHookTransformationResult failureResult(
      SteelHook03RuntimeTransformSpec spec,
      MinecraftBootstrapHookTransformationStatus status,
      String failureReason,
      SteelHookMethodEntryRewriteResult methodEntryRewriteResult,
      SteelHookMethodExitRewriteResult methodExitRewriteResult) {
    return new MinecraftBootstrapHookTransformationResult(
        1,
        MILESTONE_NAME,
        spec == null ? null : spec.transformationMode(),
        TARGET_SCOPE,
        status,
        gate(spec, false, failureReason),
        spec == null ? null : spec.sourceMilestone(),
        spec == null ? null : spec.targetClassEntryName(),
        spec == null ? null : spec.targetMethodName(),
        spec == null ? null : spec.targetBinaryName(),
        spec == null ? null : spec.targetInternalName(),
        spec == null ? null : spec.targetMethodName(),
        spec == null ? null : spec.targetDescriptor(),
        spec == null
            ? null
            : spec.dispatcherOwnerInternalName()
                + "."
                + spec.dispatcherMethodName()
                + ":"
                + spec.dispatcherDescriptor(),
        methodEntryRewriteResult != null
            ? methodEntryRewriteResult.originalClassSha256()
            : methodExitRewriteResult == null
                ? null
                : methodExitRewriteResult.originalClassSha256(),
        methodEntryRewriteResult != null
            ? methodEntryRewriteResult.transformedClassSha256()
            : methodExitRewriteResult == null
                ? null
                : methodExitRewriteResult.transformedClassSha256(),
        methodEntryRewriteResult != null
            ? methodEntryRewriteResult.originalCodeSha256()
            : methodExitRewriteResult == null ? null : methodExitRewriteResult.originalCodeSha256(),
        methodEntryRewriteResult != null
            ? methodEntryRewriteResult.transformedCodeSha256()
            : methodExitRewriteResult == null
                ? null
                : methodExitRewriteResult.transformedCodeSha256(),
        methodEntryRewriteResult != null
            ? methodEntryRewriteResult.originalCodeLength()
            : methodExitRewriteResult == null ? null : methodExitRewriteResult.originalCodeLength(),
        methodEntryRewriteResult != null
            ? methodEntryRewriteResult.transformedCodeLength()
            : methodExitRewriteResult == null
                ? null
                : methodExitRewriteResult.transformedCodeLength(),
        methodEntryRewriteResult != null
            ? methodEntryRewriteResult.constantPoolCountBefore()
            : methodExitRewriteResult == null
                ? null
                : methodExitRewriteResult.constantPoolCountBefore(),
        methodEntryRewriteResult != null
            ? methodEntryRewriteResult.constantPoolCountAfter()
            : methodExitRewriteResult == null
                ? null
                : methodExitRewriteResult.constantPoolCountAfter(),
        methodEntryRewriteResult != null
            ? methodEntryRewriteResult.methodrefIndex()
            : methodExitRewriteResult == null ? null : methodExitRewriteResult.methodrefIndex(),
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
        methodEntryRewriteResult != null
            ? methodEntryRewriteResult.status().name()
            : methodExitRewriteResult == null ? null : methodExitRewriteResult.status().name(),
        methodEntryRewriteResult != null
            ? methodEntryRewriteResult.failureReason()
            : methodExitRewriteResult == null ? null : methodExitRewriteResult.failureReason(),
        failureReason,
        null);
  }

  private MinecraftBootstrapHookTransformationGate gate(
      SteelHook03RuntimeTransformSpec spec, boolean passed, String failureReason) {
    return new MinecraftBootstrapHookTransformationGate(
        passed,
        failureReason,
        1,
        MILESTONE_NAME,
        passed,
        passed,
        passed,
        passed ? 1 : 0,
        spec == null ? null : spec.sourceMilestone(),
        false,
        spec != null && spec.runtimeClassLoadingPathEnabled());
  }
}
