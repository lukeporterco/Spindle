package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.hook.transform.SteelHookMethodEntryClassFileRewriter;
import com.spindle.core.minecraft.hook.transform.SteelHookMethodEntryRewriteRequest;
import com.spindle.core.minecraft.hook.transform.SteelHookMethodEntryRewriteResult;
import com.spindle.core.minecraft.hook.transform.SteelHookMethodEntryRewriteStatus;
import java.util.ArrayList;
import java.util.List;

public final class SteelHook02MethodEntryTransformer {
  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-25";
  private static final String TARGET = "minecraft";
  private static final String STEELHOOK_VERSION = "0.2";
  private static final String SOURCE_PATCH_PLAN_MILESTONE = "Target-7";
  private static final String SOURCE_PRIMITIVE_BOUNDARY_MILESTONE = "Target-23";
  private static final String SOURCE_CONTRACT_GENERALIZATION_MILESTONE = "Target-24";
  private static final String EXPECTED_TARGET_OWNER = "net/minecraft/server/Main";
  private static final String EXPECTED_TARGET_BINARY = "net.minecraft.server.Main";
  private static final String EXPECTED_CLASS_ENTRY = "net/minecraft/server/Main.class";
  private static final String EXPECTED_METHOD = "main";
  private static final String EXPECTED_DESCRIPTOR = "([Ljava/lang/String;)V";
  private static final String EXPECTED_DISPATCHER_OWNER =
      "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher";
  private static final String EXPECTED_DISPATCHER_BINARY =
      "com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher";
  private static final String EXPECTED_DISPATCHER_METHOD = "beforeMinecraftServerMain";
  private static final String EXPECTED_DISPATCHER_DESCRIPTOR = "()V";

  private final SteelHookMethodEntryClassFileRewriter rewriter =
      new SteelHookMethodEntryClassFileRewriter();

  public SteelHook02MethodEntryTransformerResult transform(
      SteelHook02ContractGeneralizationAnalysis contractGeneralizationAnalysis,
      byte[] originalClassBytes) {
    List<SteelHook02MethodEntryTransformerFinding> findings = new ArrayList<>();
    if (contractGeneralizationAnalysis == null) {
      return blocked("Target-24 contract generalization analysis is missing.", findings);
    }

    boolean targetDescriptorPresent = contractGeneralizationAnalysis.targetDescriptor() != null;
    boolean dispatcherDescriptorPresent =
        contractGeneralizationAnalysis.dispatcherDescriptor() != null;
    boolean primitiveContractPresent = contractGeneralizationAnalysis.primitiveContract() != null;
    boolean generalizedPatchPlanPresent =
        contractGeneralizationAnalysis.generalizedPatchPlan() != null;
    boolean targetClassBytesPresent = originalClassBytes != null && originalClassBytes.length > 0;
    SteelHook02MethodEntryTransformerGate gate =
        new SteelHook02MethodEntryTransformerGate(
            false,
            null,
            contractGeneralizationAnalysis.gatePassed(),
            contractGeneralizationAnalysis.contractGeneralizationReady(),
            contractGeneralizationAnalysis.eligibleForTarget25TransformerExtraction(),
            targetDescriptorPresent,
            dispatcherDescriptorPresent,
            primitiveContractPresent,
            generalizedPatchPlanPresent,
            targetClassBytesPresent,
            contractGeneralizationAnalysis.minecraftRuntimeTransformReady(),
            false);

    if (!contractGeneralizationAnalysis.gatePassed()
        || contractGeneralizationAnalysis.status()
            != SteelHook02ContractGeneralizationStatus.CONTRACT_GENERALIZATION_READY
        || contractGeneralizationAnalysis.nextDirection()
            != SteelHook02ContractGeneralizationNextDirection
                .MOVE_TO_TARGET_25_RUNTIME_SAFE_METHOD_ENTRY_TRANSFORMER
        || !contractGeneralizationAnalysis.contractGeneralizationReady()
        || !contractGeneralizationAnalysis.eligibleForTarget25TransformerExtraction()
        || !targetDescriptorPresent
        || !dispatcherDescriptorPresent
        || !primitiveContractPresent
        || !generalizedPatchPlanPresent) {
      return blocked(
          "Target-24 contract generalization did not provide the approved Target-25 handoff.",
          gate,
          findings,
          contractGeneralizationAnalysis,
          originalClassBytes);
    }
    if (contractGeneralizationAnalysis.minecraftRuntimeTransformReady()
        || contractGeneralizationAnalysis.eligibleForTarget26RuntimeTransformation()
        || contractGeneralizationAnalysis.classLoadingOccurred()
        || contractGeneralizationAnalysis.injectionOccurred()
        || contractGeneralizationAnalysis.transformationOccurred()
        || contractGeneralizationAnalysis.patchingOccurred()
        || contractGeneralizationAnalysis.bytecodeModified()
        || contractGeneralizationAnalysis.hookInstallationOccurred()
        || contractGeneralizationAnalysis.runtimeDispatchOccurred()
        || contractGeneralizationAnalysis.publicApiExposed()
        || contractGeneralizationAnalysis.javaModExecutionSandboxed()) {
      return rejected(
          "Target-24 invariants drifted beyond the approved Target-25 offline-only boundary.",
          gate,
          findings,
          contractGeneralizationAnalysis,
          originalClassBytes);
    }
    String descriptorFailure = validateDescriptorShape(contractGeneralizationAnalysis);
    if (descriptorFailure != null) {
      return rejected(
          descriptorFailure, gate, findings, contractGeneralizationAnalysis, originalClassBytes);
    }
    if (!targetClassBytesPresent) {
      return rejected(
          "Target-25 requires target class bytes for offline transformation.",
          new SteelHook02MethodEntryTransformerGate(
              false,
              "Target class bytes are missing.",
              gate.contractGeneralizationGatePassed(),
              gate.contractGeneralizationReady(),
              gate.eligibleForTarget25TransformerExtraction(),
              gate.targetDescriptorPresent(),
              gate.dispatcherDescriptorPresent(),
              gate.primitiveContractPresent(),
              gate.generalizedPatchPlanPresent(),
              false,
              gate.minecraftRuntimeTransformReady(),
              false),
          findings,
          contractGeneralizationAnalysis,
          originalClassBytes);
    }

    SteelHook02TargetDescriptor targetDescriptor =
        contractGeneralizationAnalysis.targetDescriptor();
    SteelHook02DispatcherDescriptor dispatcherDescriptor =
        contractGeneralizationAnalysis.dispatcherDescriptor();
    SteelHookMethodEntryRewriteRequest request =
        new SteelHookMethodEntryRewriteRequest(
            contractGeneralizationAnalysis.generalizedPatchPlan().id(),
            "Target-25 method-entry transformer",
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
            false,
            false,
            false);
    SteelHookMethodEntryRewriteResult rewriteResult = rewriter.rewrite(request, originalClassBytes);
    if (rewriteResult.status() != SteelHookMethodEntryRewriteStatus.TRANSFORMED) {
      return rejected(
          rewriteResult.failureReason(),
          new SteelHook02MethodEntryTransformerGate(
              false,
              rewriteResult.failureReason(),
              true,
              true,
              true,
              true,
              true,
              true,
              true,
              true,
              false,
              false),
          findings,
          contractGeneralizationAnalysis,
          originalClassBytes);
    }

    return new SteelHook02MethodEntryTransformerResult(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        STEELHOOK_VERSION,
        SOURCE_PATCH_PLAN_MILESTONE,
        SOURCE_PRIMITIVE_BOUNDARY_MILESTONE,
        SOURCE_CONTRACT_GENERALIZATION_MILESTONE,
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
        true,
        true,
        true,
        true,
        true,
        true,
        SteelHook02MethodEntryTransformerStatus.TRANSFORMED,
        SteelHook02MethodEntryTransformerNextDirection
            .MOVE_TO_TARGET_26_GATED_REAL_RUNTIME_TRANSFORMATION,
        null,
        rewriteResult.originalClassSha256(),
        rewriteResult.transformedClassSha256(),
        rewriteResult.originalCodeSha256(),
        rewriteResult.transformedCodeSha256(),
        rewriteResult.originalCodeLength(),
        rewriteResult.transformedCodeLength(),
        rewriteResult.constantPoolCountBefore(),
        rewriteResult.constantPoolCountAfter(),
        rewriteResult.methodrefIndex(),
        rewriteResult.insertedInstructionHex(),
        new SteelHook02MethodEntryTransformerGate(
            true, null, true, true, true, true, true, true, true, true, false, false),
        contractGeneralizationAnalysis.targetDescriptor(),
        contractGeneralizationAnalysis.dispatcherDescriptor(),
        contractGeneralizationAnalysis.primitiveContract(),
        contractGeneralizationAnalysis.generalizedPatchPlan(),
        null,
        findings);
  }

  private String validateDescriptorShape(
      SteelHook02ContractGeneralizationAnalysis contractGeneralizationAnalysis) {
    SteelHook02TargetDescriptor targetDescriptor =
        contractGeneralizationAnalysis.targetDescriptor();
    SteelHook02DispatcherDescriptor dispatcherDescriptor =
        contractGeneralizationAnalysis.dispatcherDescriptor();
    SteelHook02PrimitiveContract primitiveContract =
        contractGeneralizationAnalysis.primitiveContract();
    if (!EXPECTED_TARGET_OWNER.equals(targetDescriptor.ownerInternalName())
        || !EXPECTED_TARGET_BINARY.equals(targetDescriptor.binaryName())
        || !EXPECTED_CLASS_ENTRY.equals(targetDescriptor.classEntryName())
        || !EXPECTED_METHOD.equals(targetDescriptor.memberName())
        || !EXPECTED_DESCRIPTOR.equals(targetDescriptor.descriptor())
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

  private SteelHook02MethodEntryTransformerResult blocked(
      String failureReason, List<SteelHook02MethodEntryTransformerFinding> findings) {
    return blocked(failureReason, null, findings, null, null);
  }

  private SteelHook02MethodEntryTransformerResult blocked(
      String failureReason,
      SteelHook02MethodEntryTransformerGate gate,
      List<SteelHook02MethodEntryTransformerFinding> findings,
      SteelHook02ContractGeneralizationAnalysis analysis,
      byte[] originalClassBytes) {
    return baseFailure(
        SteelHook02MethodEntryTransformerStatus.UPSTREAM_GATE_BLOCKED,
        SteelHook02MethodEntryTransformerNextDirection.RESTORE_TARGET_24_CONTRACT_GENERALIZATION,
        failureReason,
        gate,
        findings,
        analysis,
        originalClassBytes);
  }

  private SteelHook02MethodEntryTransformerResult rejected(
      String failureReason,
      SteelHook02MethodEntryTransformerGate gate,
      List<SteelHook02MethodEntryTransformerFinding> findings,
      SteelHook02ContractGeneralizationAnalysis analysis,
      byte[] originalClassBytes) {
    return baseFailure(
        SteelHook02MethodEntryTransformerStatus.REJECTED,
        originalClassBytes == null || originalClassBytes.length == 0
            ? SteelHook02MethodEntryTransformerNextDirection.RESTORE_TARGET_CLASS_BYTES
            : SteelHook02MethodEntryTransformerNextDirection
                .RESTORE_TARGET_24_CONTRACT_GENERALIZATION,
        failureReason,
        gate,
        findings,
        analysis,
        originalClassBytes);
  }

  private SteelHook02MethodEntryTransformerResult baseFailure(
      SteelHook02MethodEntryTransformerStatus status,
      SteelHook02MethodEntryTransformerNextDirection nextDirection,
      String failureReason,
      SteelHook02MethodEntryTransformerGate gate,
      List<SteelHook02MethodEntryTransformerFinding> findings,
      SteelHook02ContractGeneralizationAnalysis analysis,
      byte[] originalClassBytes) {
    return new SteelHook02MethodEntryTransformerResult(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        STEELHOOK_VERSION,
        SOURCE_PATCH_PLAN_MILESTONE,
        SOURCE_PRIMITIVE_BOUNDARY_MILESTONE,
        SOURCE_CONTRACT_GENERALIZATION_MILESTONE,
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
        false,
        status,
        nextDirection,
        failureReason,
        originalClassBytes == null ? null : sha256Hex(originalClassBytes),
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        gate,
        analysis == null ? null : analysis.targetDescriptor(),
        analysis == null ? null : analysis.dispatcherDescriptor(),
        analysis == null ? null : analysis.primitiveContract(),
        analysis == null ? null : analysis.generalizedPatchPlan(),
        null,
        findings);
  }

  private String sha256Hex(byte[] bytes) {
    try {
      java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(bytes);
      StringBuilder builder = new StringBuilder(hash.length * 2);
      for (byte value : hash) {
        builder.append(Character.forDigit((value >>> 4) & 0xF, 16));
        builder.append(Character.forDigit(value & 0xF, 16));
      }
      return builder.toString();
    } catch (java.security.NoSuchAlgorithmException exception) {
      throw new IllegalStateException(
          "SHA-256 is unavailable for Target-25 transformation.", exception);
    }
  }
}
