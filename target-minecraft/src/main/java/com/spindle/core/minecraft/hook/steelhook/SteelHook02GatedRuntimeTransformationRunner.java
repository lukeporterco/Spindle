package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.minecraft.MinecraftClassLoadingAudit;
import com.spindle.core.minecraft.MinecraftRuntimeClassLoader;
import com.spindle.core.minecraft.MinecraftServerRuntimePlan;
import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationResult;
import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationStatus;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class SteelHook02GatedRuntimeTransformationRunner {
  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-26";
  private static final String TARGET = "minecraft";
  private static final String STEELHOOK_VERSION = "0.2";
  private static final String SOURCE_PATCH_PLAN_MILESTONE = "Target-7";
  private static final String SOURCE_PRIMITIVE_BOUNDARY_MILESTONE = "Target-23";
  private static final String SOURCE_CONTRACT_GENERALIZATION_MILESTONE = "Target-24";
  private static final String SOURCE_METHOD_ENTRY_TRANSFORMER_MILESTONE = "Target-25";
  private static final String LOADER_ID = "minecraft-runtime-steelhook-0-2";

  public SteelHook02GatedRuntimeTransformationResult run(
      MinecraftServerRuntimePlan runtimePlan,
      SteelHook02ContractGeneralizationAnalysis contractGeneralizationAnalysis,
      SteelHook02MethodEntryTransformerResult methodEntryTransformerResult) {
    MinecraftClassLoadingAudit audit = new MinecraftClassLoadingAudit();
    List<SteelHook02GatedRuntimeTransformationFinding> findings = new ArrayList<>();
    SteelHook02RuntimeClasspathUrls runtimeClasspathUrls = null;
    SteelHook02GatedRuntimeTransformationGate initialGate =
        buildGate(methodEntryTransformerResult, contractGeneralizationAnalysis, false, null, null);

    try {
      runtimeClasspathUrls = new SteelHook02RuntimeClasspathUrlBuilder().build(runtimePlan);
      SteelHook02GatedRuntimeTransformationGate gate =
          buildGate(methodEntryTransformerResult, contractGeneralizationAnalysis, true, null, null);
      if (!gate.passed()) {
        findings.add(
            new SteelHook02GatedRuntimeTransformationFinding(
                "target26.upstream-gate",
                SteelHook02GatedRuntimeTransformationFindingStatus.FAIL,
                gate.failureReason()));
        return failureResult(
            gate,
            audit.summary(),
            false,
            SteelHook02GatedRuntimeTransformationStatus.UPSTREAM_GATE_BLOCKED,
            SteelHook02GatedRuntimeTransformationNextDirection
                .RESTORE_TARGET_25_METHOD_ENTRY_TRANSFORMER,
            gate.failureReason(),
            findings,
            null);
      }
      SteelHook02GatedRuntimeClassTransformer transformer =
          new SteelHook02GatedRuntimeClassTransformer(
              contractGeneralizationAnalysis, methodEntryTransformerResult);
      try (MinecraftRuntimeClassLoader runtimeClassLoader =
          new MinecraftRuntimeClassLoader(
              LOADER_ID,
              runtimeClasspathUrls.asArray(),
              getClass().getClassLoader(),
              audit,
              transformer)) {
        Class<?> definedClass =
            Class.forName(
                SteelHook02GatedRuntimeClassTransformer.TARGET_BINARY_NAME,
                false,
                runtimeClassLoader);
        MinecraftBootstrapHookTransformationResult transformationResult =
            transformer.currentResult();
        if (transformationResult == null
            || transformationResult.status()
                != MinecraftBootstrapHookTransformationStatus.TRANSFORMED) {
          String failureReason =
              transformationResult == null
                  ? "Target-26 runtime classloader transformation did not produce a result."
                  : transformationResult.failureReason();
          findings.add(
              new SteelHook02GatedRuntimeTransformationFinding(
                  "target26.runtime-transform",
                  SteelHook02GatedRuntimeTransformationFindingStatus.FAIL,
                  failureReason));
          return failureResult(
              buildGate(
                  methodEntryTransformerResult,
                  contractGeneralizationAnalysis,
                  true,
                  false,
                  failureReason),
              audit.summary(),
              true,
              transformationResult != null
                      && transformationResult.status()
                          == MinecraftBootstrapHookTransformationStatus.PATCH_PLAN_GATE_FAILED
                  ? SteelHook02GatedRuntimeTransformationStatus.UPSTREAM_GATE_BLOCKED
                  : SteelHook02GatedRuntimeTransformationStatus.REJECTED,
              transformationResult != null
                      && transformationResult.status()
                          == MinecraftBootstrapHookTransformationStatus.PATCH_PLAN_GATE_FAILED
                  ? SteelHook02GatedRuntimeTransformationNextDirection
                      .RESTORE_TARGET_25_METHOD_ENTRY_TRANSFORMER
                  : SteelHook02GatedRuntimeTransformationNextDirection.RESTORE_RUNTIME_CLASSPATH,
              failureReason,
              findings,
              transformationResult);
        }
        findings.add(
            new SteelHook02GatedRuntimeTransformationFinding(
                "target26.runtime-classloader-defined",
                SteelHook02GatedRuntimeTransformationFindingStatus.PASS,
                "Target-26 defined the approved transformed class through the runtime classloader."));
        return successResult(
            buildGate(
                methodEntryTransformerResult, contractGeneralizationAnalysis, true, true, null),
            audit.summary(),
            findings,
            transformationResult,
            definedClass,
            runtimeClassLoader);
      }
    } catch (ClassNotFoundException exception) {
      findings.add(
          new SteelHook02GatedRuntimeTransformationFinding(
              "target26.class-definition",
              SteelHook02GatedRuntimeTransformationFindingStatus.FAIL,
              exception.getMessage()));
      return failureResult(
          buildGate(
              methodEntryTransformerResult,
              contractGeneralizationAnalysis,
              runtimeClasspathUrls != null,
              false,
              exception.getMessage()),
          audit.summary(),
          true,
          SteelHook02GatedRuntimeTransformationStatus.REJECTED,
          SteelHook02GatedRuntimeTransformationNextDirection.RESTORE_RUNTIME_CLASSPATH,
          exception.getMessage(),
          findings,
          null);
    } catch (LoaderException exception) {
      findings.add(
          new SteelHook02GatedRuntimeTransformationFinding(
              "target26.runtime-classpath",
              SteelHook02GatedRuntimeTransformationFindingStatus.FAIL,
              exception.getMessage()));
      return failureResult(
          buildGate(
              methodEntryTransformerResult,
              contractGeneralizationAnalysis,
              false,
              false,
              exception.getMessage()),
          audit.summary(),
          false,
          SteelHook02GatedRuntimeTransformationStatus.REJECTED,
          SteelHook02GatedRuntimeTransformationNextDirection.RESTORE_RUNTIME_CLASSPATH,
          exception.getMessage(),
          findings,
          null);
    } catch (IOException exception) {
      findings.add(
          new SteelHook02GatedRuntimeTransformationFinding(
              "target26.runtime-classloader-close",
              SteelHook02GatedRuntimeTransformationFindingStatus.FAIL,
              exception.getMessage()));
      return failureResult(
          buildGate(
              methodEntryTransformerResult,
              contractGeneralizationAnalysis,
              runtimeClasspathUrls != null,
              false,
              exception.getMessage()),
          audit.summary(),
          true,
          SteelHook02GatedRuntimeTransformationStatus.REJECTED,
          SteelHook02GatedRuntimeTransformationNextDirection.RESTORE_RUNTIME_CLASSPATH,
          exception.getMessage(),
          findings,
          null);
    }
  }

  private SteelHook02GatedRuntimeTransformationResult successResult(
      SteelHook02GatedRuntimeTransformationGate gate,
      MinecraftClassLoadingAudit.Summary auditSummary,
      List<SteelHook02GatedRuntimeTransformationFinding> findings,
      MinecraftBootstrapHookTransformationResult transformationResult,
      Class<?> definedClass,
      MinecraftRuntimeClassLoader runtimeClassLoader) {
    return new SteelHook02GatedRuntimeTransformationResult(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        STEELHOOK_VERSION,
        SOURCE_PATCH_PLAN_MILESTONE,
        SOURCE_PRIMITIVE_BOUNDARY_MILESTONE,
        SOURCE_CONTRACT_GENERALIZATION_MILESTONE,
        SOURCE_METHOD_ENTRY_TRANSFORMER_MILESTONE,
        true,
        true,
        true,
        !auditSummary.definedClassLoadsByLoader().isEmpty(),
        definedClass != null,
        transformationResult.transformationMode(),
        SteelHook02GatedRuntimeClassTransformer.TARGET_BINARY_NAME,
        SteelHook02GatedRuntimeClassTransformer.TARGET_CLASS_ENTRY_NAME,
        definedClass == null ? null : definedClass.getName(),
        definedClass != null && definedClass.getClassLoader() == runtimeClassLoader,
        true,
        transformationResult.transformationOccurred(),
        transformationResult.bytecodeModified(),
        transformationResult.transformedClassBytes() != null,
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
        true,
        true,
        gate.passed(),
        SteelHook02GatedRuntimeTransformationStatus.TRANSFORMED_AND_DEFINED,
        SteelHook02GatedRuntimeTransformationNextDirection
            .MOVE_TO_TARGET_27_STEELHOOK_0_2_COMPLETION,
        null,
        transformationResult.originalClassSha256(),
        transformationResult.transformedClassSha256(),
        transformationResult.originalCodeSha256(),
        transformationResult.transformedCodeSha256(),
        transformationResult.originalCodeLength(),
        transformationResult.transformedCodeLength(),
        transformationResult.constantPoolCountBefore(),
        transformationResult.constantPoolCountAfter(),
        transformationResult.methodrefIndex(),
        methodrefInstructionHex(transformationResult),
        auditSummary,
        gate,
        findings);
  }

  private SteelHook02GatedRuntimeTransformationResult failureResult(
      SteelHook02GatedRuntimeTransformationGate gate,
      MinecraftClassLoadingAudit.Summary auditSummary,
      boolean runtimeClassLoadingAttempted,
      SteelHook02GatedRuntimeTransformationStatus status,
      SteelHook02GatedRuntimeTransformationNextDirection nextDirection,
      String failureReason,
      List<SteelHook02GatedRuntimeTransformationFinding> findings,
      MinecraftBootstrapHookTransformationResult transformationResult) {
    return new SteelHook02GatedRuntimeTransformationResult(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        STEELHOOK_VERSION,
        SOURCE_PATCH_PLAN_MILESTONE,
        SOURCE_PRIMITIVE_BOUNDARY_MILESTONE,
        SOURCE_CONTRACT_GENERALIZATION_MILESTONE,
        SOURCE_METHOD_ENTRY_TRANSFORMER_MILESTONE,
        true,
        runtimeClassLoadingAttempted,
        false,
        !auditSummary.definedClassLoadsByLoader().isEmpty(),
        false,
        transformationResult == null ? null : transformationResult.transformationMode(),
        SteelHook02GatedRuntimeClassTransformer.TARGET_BINARY_NAME,
        SteelHook02GatedRuntimeClassTransformer.TARGET_CLASS_ENTRY_NAME,
        null,
        false,
        false,
        transformationResult != null && transformationResult.transformationOccurred(),
        transformationResult != null && transformationResult.bytecodeModified(),
        transformationResult != null && transformationResult.transformedClassBytes() != null,
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
        gate.passed(),
        status,
        nextDirection,
        failureReason,
        transformationResult == null ? null : transformationResult.originalClassSha256(),
        transformationResult == null ? null : transformationResult.transformedClassSha256(),
        transformationResult == null ? null : transformationResult.originalCodeSha256(),
        transformationResult == null ? null : transformationResult.transformedCodeSha256(),
        transformationResult == null ? null : transformationResult.originalCodeLength(),
        transformationResult == null ? null : transformationResult.transformedCodeLength(),
        transformationResult == null ? null : transformationResult.constantPoolCountBefore(),
        transformationResult == null ? null : transformationResult.constantPoolCountAfter(),
        transformationResult == null ? null : transformationResult.methodrefIndex(),
        methodrefInstructionHex(transformationResult),
        auditSummary,
        gate,
        findings);
  }

  private SteelHook02GatedRuntimeTransformationGate buildGate(
      SteelHook02MethodEntryTransformerResult methodEntryTransformerResult,
      SteelHook02ContractGeneralizationAnalysis contractGeneralizationAnalysis,
      boolean runtimeClasspathUrlsPresent,
      Boolean runtimeClassLoadingSucceeded,
      String failureReason) {
    boolean target25Present = methodEntryTransformerResult != null;
    boolean passed =
        target25Present
            && methodEntryTransformerResult.gatePassed()
            && methodEntryTransformerResult.status()
                == SteelHook02MethodEntryTransformerStatus.TRANSFORMED
            && methodEntryTransformerResult.nextDirection()
                == SteelHook02MethodEntryTransformerNextDirection
                    .MOVE_TO_TARGET_26_GATED_REAL_RUNTIME_TRANSFORMATION
            && methodEntryTransformerResult.eligibleForTarget26GatedRuntimeTransformation()
            && !methodEntryTransformerResult.runtimeClassLoadingPathEnabled()
            && !methodEntryTransformerResult.classLoadingOccurred()
            && !methodEntryTransformerResult.hookInstallationOccurred()
            && !methodEntryTransformerResult.runtimeDispatchOccurred()
            && !methodEntryTransformerResult.realMinecraftRuntimeTransformed()
            && !methodEntryTransformerResult.minecraftRuntimeTransformReady()
            && !methodEntryTransformerResult.publicApiExposed()
            && !methodEntryTransformerResult.javaModExecutionSandboxed()
            && contractGeneralizationAnalysis != null
            && contractGeneralizationAnalysis.targetDescriptor() != null
            && contractGeneralizationAnalysis.dispatcherDescriptor() != null
            && contractGeneralizationAnalysis.primitiveContract() != null
            && contractGeneralizationAnalysis.generalizedPatchPlan() != null
            && methodEntryTransformerResult.targetClassBytes() != null
            && runtimeClasspathUrlsPresent;
    String resolvedFailureReason = failureReason;
    if (resolvedFailureReason == null && !passed) {
      resolvedFailureReason =
          "Target-26 requires the approved Target-25 offline-only handoff and runtime classpath.";
    }
    return new SteelHook02GatedRuntimeTransformationGate(
        passed && (runtimeClassLoadingSucceeded == null || runtimeClassLoadingSucceeded),
        resolvedFailureReason,
        target25Present && methodEntryTransformerResult.gatePassed(),
        target25Present
            && methodEntryTransformerResult.status()
                == SteelHook02MethodEntryTransformerStatus.TRANSFORMED,
        target25Present
            && methodEntryTransformerResult.eligibleForTarget26GatedRuntimeTransformation(),
        target25Present && !methodEntryTransformerResult.runtimeClassLoadingPathEnabled(),
        true,
        contractGeneralizationAnalysis != null
            && contractGeneralizationAnalysis.targetDescriptor() != null,
        contractGeneralizationAnalysis != null
            && contractGeneralizationAnalysis.dispatcherDescriptor() != null,
        contractGeneralizationAnalysis != null
            && contractGeneralizationAnalysis.primitiveContract() != null,
        contractGeneralizationAnalysis != null
            && contractGeneralizationAnalysis.generalizedPatchPlan() != null,
        target25Present && methodEntryTransformerResult.targetClassBytes() != null,
        runtimeClasspathUrlsPresent,
        target25Present && methodEntryTransformerResult.minecraftRuntimeTransformReady(),
        false);
  }

  private String methodrefInstructionHex(MinecraftBootstrapHookTransformationResult result) {
    if (result == null || result.methodrefIndex() == null) {
      return null;
    }
    int methodrefIndex = result.methodrefIndex();
    return String.format("b8 %02x %02x", (methodrefIndex >>> 8) & 0xff, methodrefIndex & 0xff);
  }
}
