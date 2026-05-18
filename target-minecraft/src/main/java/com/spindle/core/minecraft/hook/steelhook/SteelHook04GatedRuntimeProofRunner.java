package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.minecraft.MinecraftClassLoadingAudit;
import com.spindle.core.minecraft.MinecraftRuntimeClassLoader;
import com.spindle.core.minecraft.MinecraftServerRuntimePlan;
import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationResult;
import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationStatus;
import com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher;
import com.spindle.core.minecraft.hook.transform.SteelHookInvokeCallsiteRewriteResult;
import com.spindle.core.minecraft.hook.transform.SteelHookReturnValueInterceptRewriteResult;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class SteelHook04GatedRuntimeProofRunner {
  public static final String REPORT_FILE_NAME = "minecraft-steelhook-0-4-gated-runtime-proof.json";

  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-35";
  private static final String TARGET = "minecraft";
  private static final String STEELHOOK_VERSION = "0.4";
  private static final String RETURN_VALUE_INTERCEPT_LOADER_ID =
      "minecraft-runtime-steelhook-0-4-return-value-intercept";
  private static final String INVOKE_REDIRECT_LOADER_ID =
      "minecraft-runtime-steelhook-0-4-invoke-redirect";
  private static final String INVOKE_WRAP_LOADER_ID = "minecraft-runtime-steelhook-0-4-invoke-wrap";
  private static final String READY_ACTION =
      "Move next to Target-36 completion verification while preserving Target-35 class-definition-only semantics and pre-definition rejection behavior.";
  private static final String BLOCKED_ACTION =
      "Restore the Target-32 through Target-34 SteelHook 0.4 evidence chain before rerunning Target-35.";
  private static final String FAILED_ACTION =
      "Restore the Target-35 bounded runtime transformer or isolated class-definition sessions before continuing toward Target-36.";

  private final SteelHook04RuntimeFixtureJarWriter fixtureJarWriter;
  private final SteelHook02RuntimeClasspathUrlBuilder runtimeClasspathUrlBuilder;
  private final SteelHook04ReturnValueInterceptFixtureClassFactory returnValueFixtureFactory;
  private final SteelHook04InvokeCallsiteFixtureClassFactory invokeFixtureFactory;

  public SteelHook04GatedRuntimeProofRunner() {
    this(
        new SteelHook04RuntimeFixtureJarWriter(),
        new SteelHook02RuntimeClasspathUrlBuilder(),
        new SteelHook04ReturnValueInterceptFixtureClassFactory(),
        new SteelHook04InvokeCallsiteFixtureClassFactory());
  }

  SteelHook04GatedRuntimeProofRunner(
      SteelHook04RuntimeFixtureJarWriter fixtureJarWriter,
      SteelHook02RuntimeClasspathUrlBuilder runtimeClasspathUrlBuilder,
      SteelHook04ReturnValueInterceptFixtureClassFactory returnValueFixtureFactory,
      SteelHook04InvokeCallsiteFixtureClassFactory invokeFixtureFactory) {
    this.fixtureJarWriter = fixtureJarWriter;
    this.runtimeClasspathUrlBuilder = runtimeClasspathUrlBuilder;
    this.returnValueFixtureFactory = returnValueFixtureFactory;
    this.invokeFixtureFactory = invokeFixtureFactory;
  }

  public SteelHook04GatedRuntimeProofReport run(
      MinecraftServerRuntimePlan baseRuntimePlan,
      SteelHook04PrimitiveBoundaryReport target32Report,
      SteelHook04ReturnValueInterceptOfflineProofReport target33Report,
      SteelHook04InvokeRedirectWrapOfflineProofReport target34Report,
      Path fixtureOutputDirectory) {
    List<SteelHook04GatedRuntimeProofFinding> findings = new ArrayList<>();
    GateState gateState =
        validateUpstream(
            baseRuntimePlan,
            target32Report,
            target33Report,
            target34Report,
            fixtureOutputDirectory,
            findings);
    if (!gateState.ready()) {
      String failureReason = gateState.failureReason();
      return report(
          target32Report,
          target33Report,
          target34Report,
          false,
          SteelHook04GatedRuntimeProofStatus.BLOCKED,
          gateState.nextDirection(),
          BLOCKED_ACTION,
          blockedProof(
              SteelHook04RuntimeTransformSpec.returnValueInterceptPrimitiveReplacement(),
              failureReason),
          blockedProof(SteelHook04RuntimeTransformSpec.invokeRedirect(), failureReason),
          blockedProof(SteelHook04RuntimeTransformSpec.invokeWrap(), failureReason),
          false,
          false,
          List.of(),
          failureReason,
          findings);
    }

    SteelHook04RuntimePrimitiveProof returnValueProof;
    SteelHook04RuntimePrimitiveProof invokeRedirectProof;
    SteelHook04RuntimePrimitiveProof invokeWrapProof;
    boolean unsupportedRejected;
    try {
      returnValueProof =
          runSession(
              baseRuntimePlan,
              fixtureOutputDirectory.resolve(
                  "minecraft-steelhook-0-4-return-value-runtime-fixture.jar"),
              RETURN_VALUE_INTERCEPT_LOADER_ID,
              SteelHook04RuntimeTransformSpec.returnValueInterceptPrimitiveReplacement(),
              returnValueFixtureFactory.createFixtureClassBytes());
      invokeRedirectProof =
          runSession(
              baseRuntimePlan,
              fixtureOutputDirectory.resolve(
                  "minecraft-steelhook-0-4-invoke-redirect-runtime-fixture.jar"),
              INVOKE_REDIRECT_LOADER_ID,
              SteelHook04RuntimeTransformSpec.invokeRedirect(),
              invokeFixtureFactory.createFixtureClassBytes());
      invokeWrapProof =
          runSession(
              baseRuntimePlan,
              fixtureOutputDirectory.resolve(
                  "minecraft-steelhook-0-4-invoke-wrap-runtime-fixture.jar"),
              INVOKE_WRAP_LOADER_ID,
              SteelHook04RuntimeTransformSpec.invokeWrap(),
              invokeFixtureFactory.createFixtureClassBytes());
      unsupportedRejected =
          unsupportedPlanRejectedBeforeClassDefinition(
              returnValueFixtureFactory.createFixtureClassBytes());
    } catch (LoaderException exception) {
      addFinding(
          findings,
          20,
          "Target-35 runtime fixture sessions executed.",
          SteelHook04GatedRuntimeProofFindingStatus.FAIL,
          true,
          "Target-35 could not complete the isolated class-definition sessions.",
          exception.getMessage());
      return report(
          target32Report,
          target33Report,
          target34Report,
          false,
          SteelHook04GatedRuntimeProofStatus.FAILED,
          SteelHook04GatedRuntimeProofNextDirection
              .RESTORE_TARGET_35_GATED_RUNTIME_CLASS_DEFINITION_PROOF,
          FAILED_ACTION,
          failedProof(
              SteelHook04RuntimeTransformSpec.returnValueInterceptPrimitiveReplacement(),
              RETURN_VALUE_INTERCEPT_LOADER_ID,
              0,
              0,
              0,
              exception.getMessage()),
          failedProof(
              SteelHook04RuntimeTransformSpec.invokeRedirect(),
              INVOKE_REDIRECT_LOADER_ID,
              0,
              0,
              0,
              exception.getMessage()),
          failedProof(
              SteelHook04RuntimeTransformSpec.invokeWrap(),
              INVOKE_WRAP_LOADER_ID,
              0,
              0,
              0,
              exception.getMessage()),
          false,
          false,
          List.of(),
          exception.getMessage(),
          findings);
    }

    boolean ready =
        returnValueProof.status() == SteelHook04GatedRuntimeProofStatus.GATED_RUNTIME_PROOF_READY
            && invokeRedirectProof.status()
                == SteelHook04GatedRuntimeProofStatus.GATED_RUNTIME_PROOF_READY
            && invokeWrapProof.status()
                == SteelHook04GatedRuntimeProofStatus.GATED_RUNTIME_PROOF_READY
            && unsupportedRejected;
    addFinding(
        findings,
        21,
        "RETURN_VALUE_INTERCEPT class-definition proof passed.",
        returnValueProof.status() == SteelHook04GatedRuntimeProofStatus.GATED_RUNTIME_PROOF_READY
            ? SteelHook04GatedRuntimeProofFindingStatus.PASS
            : SteelHook04GatedRuntimeProofFindingStatus.FAIL,
        true,
        returnValueProof.status() == SteelHook04GatedRuntimeProofStatus.GATED_RUNTIME_PROOF_READY
            ? "Target-35 defined the RETURN_VALUE_INTERCEPT transformed fixture class without initialization."
            : "RETURN_VALUE_INTERCEPT class-definition proof failed.",
        returnValueProof.failureReason());
    addFinding(
        findings,
        22,
        "INVOKE_REDIRECT class-definition proof passed.",
        invokeRedirectProof.status() == SteelHook04GatedRuntimeProofStatus.GATED_RUNTIME_PROOF_READY
            ? SteelHook04GatedRuntimeProofFindingStatus.PASS
            : SteelHook04GatedRuntimeProofFindingStatus.FAIL,
        true,
        invokeRedirectProof.status() == SteelHook04GatedRuntimeProofStatus.GATED_RUNTIME_PROOF_READY
            ? "Target-35 defined the INVOKE_REDIRECT transformed fixture class without initialization."
            : "INVOKE_REDIRECT class-definition proof failed.",
        invokeRedirectProof.failureReason());
    addFinding(
        findings,
        23,
        "INVOKE_WRAP class-definition proof passed.",
        invokeWrapProof.status() == SteelHook04GatedRuntimeProofStatus.GATED_RUNTIME_PROOF_READY
            ? SteelHook04GatedRuntimeProofFindingStatus.PASS
            : SteelHook04GatedRuntimeProofFindingStatus.FAIL,
        true,
        invokeWrapProof.status() == SteelHook04GatedRuntimeProofStatus.GATED_RUNTIME_PROOF_READY
            ? "Target-35 defined the INVOKE_WRAP transformed fixture class without initialization."
            : "INVOKE_WRAP class-definition proof failed.",
        invokeWrapProof.failureReason());
    addFinding(
        findings,
        24,
        "Unsupported runtime primitive plans were rejected before class definition.",
        unsupportedRejected
            ? SteelHook04GatedRuntimeProofFindingStatus.PASS
            : SteelHook04GatedRuntimeProofFindingStatus.FAIL,
        true,
        unsupportedRejected
            ? "Target-35 proved deterministic pre-definition rejection for unsupported or malformed plans."
            : "Target-35 did not prove the required unsupported-plan rejection.",
        unsupportedRejected
            ? "unsupportedPrimitivePlanClassDefinitionAttempted=false"
            : "Unsupported plan rejection proof failed.");

    List<String> targetClassesDefined = new ArrayList<>();
    if (returnValueProof.definedClassName() != null) {
      targetClassesDefined.add(returnValueProof.definedClassName());
    }
    if (invokeRedirectProof.definedClassName() != null) {
      targetClassesDefined.add(invokeRedirectProof.definedClassName());
    }
    if (invokeWrapProof.definedClassName() != null) {
      targetClassesDefined.add(invokeWrapProof.definedClassName());
    }
    return report(
        target32Report,
        target33Report,
        target34Report,
        ready,
        ready
            ? SteelHook04GatedRuntimeProofStatus.GATED_RUNTIME_PROOF_READY
            : SteelHook04GatedRuntimeProofStatus.FAILED,
        ready
            ? SteelHook04GatedRuntimeProofNextDirection
                .MOVE_TO_TARGET_36_STEELHOOK_0_4_COMPLETION_VERIFICATION
            : SteelHook04GatedRuntimeProofNextDirection
                .RESTORE_TARGET_35_GATED_RUNTIME_CLASS_DEFINITION_PROOF,
        ready ? READY_ACTION : FAILED_ACTION,
        returnValueProof,
        invokeRedirectProof,
        invokeWrapProof,
        unsupportedRejected,
        false,
        targetClassesDefined,
        ready
            ? null
            : "Target-35 requires all three isolated class-definition sessions and unsupported-plan rejection to pass.",
        findings);
  }

  private SteelHook04RuntimePrimitiveProof runSession(
      MinecraftServerRuntimePlan baseRuntimePlan,
      Path runtimeJarPath,
      String loaderId,
      SteelHook04RuntimeTransformSpec spec,
      byte[] fixtureClassBytes)
      throws LoaderException {
    SteelHookDispatcher.resetForBootstrap();
    int dispatcherCountBefore = dispatcherInvocationCount();
    fixtureJarWriter.write(runtimeJarPath, spec.targetClassEntryName(), fixtureClassBytes);
    SteelHook02RuntimeClasspathUrls runtimeClasspathUrls =
        runtimeClasspathUrlBuilder.build(runtimePlanForFixture(baseRuntimePlan, runtimeJarPath));
    MinecraftClassLoadingAudit audit = new MinecraftClassLoadingAudit();
    SteelHook04GatedRuntimeClassTransformer transformer =
        new SteelHook04GatedRuntimeClassTransformer(spec);
    try (MinecraftRuntimeClassLoader runtimeClassLoader =
        new MinecraftRuntimeClassLoader(
            loaderId,
            runtimeClasspathUrls.asArray(),
            getClass().getClassLoader(),
            audit,
            transformer)) {
      Class<?> definedClass = Class.forName(spec.targetBinaryName(), false, runtimeClassLoader);
      MinecraftBootstrapHookTransformationResult transformationResult = transformer.currentResult();
      int dispatcherCountAfter = dispatcherInvocationCount();
      if (transformationResult == null
          || transformationResult.status()
              != MinecraftBootstrapHookTransformationStatus.TRANSFORMED) {
        return failedProof(
            spec,
            loaderId,
            runtimeClasspathUrls.normalizedAbsolutePaths().size(),
            dispatcherCountBefore,
            dispatcherCountAfter,
            transformationResult == null
                ? "Target-35 runtime classloader transformation did not produce a result."
                : transformationResult.failureReason());
      }
      if (definedClass.getClassLoader() != runtimeClassLoader) {
        return failedProof(
            spec,
            loaderId,
            runtimeClasspathUrls.normalizedAbsolutePaths().size(),
            dispatcherCountBefore,
            dispatcherCountAfter,
            "Target-35 expected the transformed class to be defined by the isolated runtime classloader.");
      }
      if (dispatcherCountAfter != dispatcherCountBefore) {
        return failedProof(
            spec,
            loaderId,
            runtimeClasspathUrls.normalizedAbsolutePaths().size(),
            dispatcherCountBefore,
            dispatcherCountAfter,
            "Target-35 must not execute SteelHookDispatcher during class definition.");
      }
      return successfulProof(
          spec,
          loaderId,
          runtimeClasspathUrls.normalizedAbsolutePaths().size(),
          audit.summary(),
          transformer,
          transformationResult,
          definedClass,
          runtimeClassLoader,
          dispatcherCountBefore,
          dispatcherCountAfter);
    } catch (ClassNotFoundException | IOException exception) {
      return failedProof(
          spec,
          loaderId,
          runtimeClasspathUrls.normalizedAbsolutePaths().size(),
          dispatcherCountBefore,
          dispatcherInvocationCount(),
          exception.getMessage());
    }
  }

  private boolean unsupportedPlanRejectedBeforeClassDefinition(byte[] fixtureClassBytes) {
    SteelHook04GatedRuntimeClassTransformer transformer =
        new SteelHook04GatedRuntimeClassTransformer(
            SteelHook04RuntimeTransformSpec.unsupportedOrMalformedPlanForRejectionProof());
    MinecraftBootstrapHookTransformationResult result =
        transformer.transform(
            SteelHook04ReturnValueInterceptFixtureClassFactory.TARGET_BINARY_NAME,
            fixtureClassBytes);
    return result != null
        && result.status() == MinecraftBootstrapHookTransformationStatus.REJECTED
        && result.transformedClassBytes() == null;
  }

  private SteelHook04RuntimePrimitiveProof successfulProof(
      SteelHook04RuntimeTransformSpec spec,
      String loaderId,
      int runtimeClasspathEntryCount,
      MinecraftClassLoadingAudit.Summary auditSummary,
      SteelHook04GatedRuntimeClassTransformer transformer,
      MinecraftBootstrapHookTransformationResult transformationResult,
      Class<?> definedClass,
      MinecraftRuntimeClassLoader runtimeClassLoader,
      int dispatcherCountBefore,
      int dispatcherCountAfter) {
    SteelHookReturnValueInterceptRewriteResult returnValueResult =
        transformer.currentReturnValueInterceptRewriteResult();
    SteelHookInvokeCallsiteRewriteResult invokeResult =
        transformer.currentInvokeCallsiteRewriteResult();
    String matchedOpcode =
        returnValueResult != null
            ? returnValueResult.matchedReturnOpcode()
            : invokeResult.matchedInvokeOpcode();
    Integer matchedCallsiteCount =
        invokeResult == null ? null : invokeResult.matchedCallsiteCount();
    return new SteelHook04RuntimePrimitiveProof(
        spec.primitiveKind(),
        spec.sourceMilestone(),
        spec.sourceReportId(),
        SteelHook04GatedRuntimeProofStatus.GATED_RUNTIME_PROOF_READY,
        spec.targetBinaryName(),
        spec.targetInternalName(),
        spec.targetClassEntryName(),
        spec.targetMethodName(),
        spec.targetDescriptor(),
        spec.transformationModeId(),
        loaderId,
        runtimeClasspathEntryCount,
        true,
        true,
        !auditSummary.definedClassLoadsByLoader().isEmpty(),
        true,
        definedClass.getName(),
        definedClass.getClassLoader() == runtimeClassLoader,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        dispatcherCountAfter != dispatcherCountBefore,
        dispatcherCountBefore,
        dispatcherCountAfter,
        transformationResult.bytecodeModified(),
        transformationResult.transformedClassBytes() != null,
        transformationResult.originalClassSha256(),
        transformationResult.transformedClassSha256(),
        transformationResult.originalCodeSha256(),
        transformationResult.transformedCodeSha256(),
        transformationResult.originalCodeLength(),
        transformationResult.transformedCodeLength(),
        matchedOpcode,
        matchedCallsiteCount,
        invokeResult == null || spec.primitiveKind() != SteelHook04PrimitiveKind.INVOKE_WRAP
            ? null
            : spec.expectedInvokeOwnerInternalName(),
        invokeResult == null || spec.primitiveKind() != SteelHook04PrimitiveKind.INVOKE_WRAP
            ? null
            : spec.expectedInvokeName(),
        invokeResult == null || spec.primitiveKind() != SteelHook04PrimitiveKind.INVOKE_WRAP
            ? null
            : spec.expectedInvokeDescriptor(),
        invokeResult == null || spec.primitiveKind() != SteelHook04PrimitiveKind.INVOKE_WRAP
            ? null
            : spec.replacementInvokeOwnerInternalName(),
        invokeResult == null || spec.primitiveKind() != SteelHook04PrimitiveKind.INVOKE_WRAP
            ? null
            : spec.replacementInvokeName(),
        invokeResult == null || spec.primitiveKind() != SteelHook04PrimitiveKind.INVOKE_WRAP
            ? null
            : spec.replacementInvokeDescriptor(),
        null);
  }

  private SteelHook04RuntimePrimitiveProof blockedProof(
      SteelHook04RuntimeTransformSpec spec, String failureReason) {
    return new SteelHook04RuntimePrimitiveProof(
        spec.primitiveKind(),
        spec.sourceMilestone(),
        spec.sourceReportId(),
        SteelHook04GatedRuntimeProofStatus.BLOCKED,
        spec.targetBinaryName(),
        spec.targetInternalName(),
        spec.targetClassEntryName(),
        spec.targetMethodName(),
        spec.targetDescriptor(),
        spec.transformationModeId(),
        null,
        0,
        false,
        false,
        false,
        false,
        null,
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
        0,
        false,
        false,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        failureReason);
  }

  private SteelHook04RuntimePrimitiveProof failedProof(
      SteelHook04RuntimeTransformSpec spec,
      String loaderId,
      int runtimeClasspathEntryCount,
      int dispatcherCountBefore,
      int dispatcherCountAfter,
      String failureReason) {
    return new SteelHook04RuntimePrimitiveProof(
        spec.primitiveKind(),
        spec.sourceMilestone(),
        spec.sourceReportId(),
        SteelHook04GatedRuntimeProofStatus.FAILED,
        spec.targetBinaryName(),
        spec.targetInternalName(),
        spec.targetClassEntryName(),
        spec.targetMethodName(),
        spec.targetDescriptor(),
        spec.transformationModeId(),
        loaderId,
        runtimeClasspathEntryCount,
        true,
        false,
        false,
        false,
        null,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        dispatcherCountAfter != dispatcherCountBefore,
        dispatcherCountBefore,
        dispatcherCountAfter,
        false,
        false,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        failureReason);
  }

  private GateState validateUpstream(
      MinecraftServerRuntimePlan baseRuntimePlan,
      SteelHook04PrimitiveBoundaryReport target32Report,
      SteelHook04ReturnValueInterceptOfflineProofReport target33Report,
      SteelHook04InvokeRedirectWrapOfflineProofReport target34Report,
      Path fixtureOutputDirectory,
      List<SteelHook04GatedRuntimeProofFinding> findings) {
    if (baseRuntimePlan == null) {
      return failedGate(
          findings,
          1,
          SteelHook04GatedRuntimeProofNextDirection
              .RESTORE_TARGET_34_INVOKE_REDIRECT_WRAP_OFFLINE_PROOF,
          "Target-35 requires a Minecraft runtime plan.");
    }
    if (fixtureOutputDirectory == null) {
      return failedGate(
          findings,
          2,
          SteelHook04GatedRuntimeProofNextDirection
              .RESTORE_TARGET_34_INVOKE_REDIRECT_WRAP_OFFLINE_PROOF,
          "Target-35 requires a fixture output directory.");
    }
    if (target32Report == null) {
      return failedGate(
          findings,
          3,
          SteelHook04GatedRuntimeProofNextDirection.RESTORE_TARGET_32_PRIMITIVE_BOUNDARY,
          "Target-35 requires the Target-32 primitive boundary report.");
    }
    if (!"Target-32".equals(target32Report.milestoneName())
        || !TARGET.equals(target32Report.target())
        || !STEELHOOK_VERSION.equals(target32Report.steelHookVersion())
        || !target32Report.gatePassed()
        || target32Report.boundaryStatus() != SteelHook04PrimitiveBoundaryStatus.BOUNDARY_READY
        || target32Report.approvedPrimitiveCount() != 3
        || !candidateAllowed(
            findCandidate(target32Report, SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT))
        || !candidateAllowed(
            findCandidate(target32Report, SteelHook04PrimitiveKind.INVOKE_REDIRECT))
        || !candidateAllowed(findCandidate(target32Report, SteelHook04PrimitiveKind.INVOKE_WRAP))
        || !sideEffectsSafe(
            target32Report.runtimeClassLoadingPathEnabled(),
            target32Report.classLoadingOccurred(),
            target32Report.serverLaunchOccurred(),
            target32Report.minecraftMainInvoked(),
            target32Report.hookInstallationOccurred(),
            target32Report.runtimeDispatchOccurred(),
            target32Report.publicApiExposed(),
            target32Report.javaAgentUsed(),
            target32Report.mixinUsed(),
            target32Report.javaModExecutionSandboxed())) {
      return failedGate(
          findings,
          4,
          SteelHook04GatedRuntimeProofNextDirection.RESTORE_TARGET_32_PRIMITIVE_BOUNDARY,
          "Target-35 requires a passed Target-32 SteelHook 0.4 primitive boundary handoff.");
    }
    if (target33Report == null) {
      return failedGate(
          findings,
          5,
          SteelHook04GatedRuntimeProofNextDirection
              .RESTORE_TARGET_33_RETURN_VALUE_INTERCEPT_OFFLINE_PROOF,
          "Target-35 requires the Target-33 return-value intercept offline proof report.");
    }
    if (!"Target-33".equals(target33Report.milestoneName())
        || !TARGET.equals(target33Report.target())
        || !STEELHOOK_VERSION.equals(target33Report.steelHookVersion())
        || !target33Report.proofReady()
        || target33Report.proofStatus()
            != SteelHook04ReturnValueInterceptOfflineProofStatus.PROOF_READY
        || target33Report.primitiveKind() != SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT
        || target33Report.successfulProofCaseCount() < 4
        || !target33Report.offlineOnly()
        || !sideEffectsSafe(
            target33Report.runtimeClassLoadingPathEnabled(),
            target33Report.classLoadingOccurred(),
            target33Report.serverLaunchOccurred(),
            target33Report.minecraftMainInvoked(),
            target33Report.hookInstallationOccurred(),
            target33Report.runtimeDispatchOccurred(),
            target33Report.publicApiExposed(),
            target33Report.javaAgentUsed(),
            target33Report.mixinUsed(),
            target33Report.javaModExecutionSandboxed())) {
      return failedGate(
          findings,
          6,
          SteelHook04GatedRuntimeProofNextDirection
              .RESTORE_TARGET_33_RETURN_VALUE_INTERCEPT_OFFLINE_PROOF,
          "Target-35 requires a passed Target-33 RETURN_VALUE_INTERCEPT offline proof handoff.");
    }
    if (target34Report == null) {
      return failedGate(
          findings,
          7,
          SteelHook04GatedRuntimeProofNextDirection
              .RESTORE_TARGET_34_INVOKE_REDIRECT_WRAP_OFFLINE_PROOF,
          "Target-35 requires the Target-34 invoke redirect/wrap offline proof report.");
    }
    if (!"Target-34".equals(target34Report.milestoneName())
        || !TARGET.equals(target34Report.target())
        || !STEELHOOK_VERSION.equals(target34Report.steelHookVersion())
        || !target34Report.proofReady()
        || target34Report.proofStatus()
            != SteelHook04InvokeRedirectWrapOfflineProofStatus.PROOF_READY
        || !target34Report
            .approvedPrimitiveKinds()
            .contains(SteelHook04PrimitiveKind.INVOKE_REDIRECT)
        || !target34Report.approvedPrimitiveKinds().contains(SteelHook04PrimitiveKind.INVOKE_WRAP)
        || target34Report.successfulProofCaseCount() != 2
        || !target34Report.offlineOnly()
        || !sideEffectsSafe(
            target34Report.runtimeClassLoadingPathEnabled(),
            target34Report.classLoadingOccurred(),
            target34Report.serverLaunchOccurred(),
            target34Report.minecraftMainInvoked(),
            target34Report.hookInstallationOccurred(),
            target34Report.runtimeDispatchOccurred(),
            target34Report.publicApiExposed(),
            target34Report.javaAgentUsed(),
            target34Report.mixinUsed(),
            target34Report.javaModExecutionSandboxed())) {
      return failedGate(
          findings,
          8,
          SteelHook04GatedRuntimeProofNextDirection
              .RESTORE_TARGET_34_INVOKE_REDIRECT_WRAP_OFFLINE_PROOF,
          "Target-35 requires a passed Target-34 INVOKE_REDIRECT and INVOKE_WRAP offline proof handoff.");
    }
    addFinding(
        findings,
        9,
        "Target-32 through Target-34 evidence chain passed Target-35 gating.",
        SteelHook04GatedRuntimeProofFindingStatus.PASS,
        true,
        "Target-35 validated the required upstream handoff reports and no-execution booleans.",
        "Target-32, Target-33, and Target-34 were consumed in-memory.");
    return new GateState(
        true,
        SteelHook04GatedRuntimeProofNextDirection
            .MOVE_TO_TARGET_36_STEELHOOK_0_4_COMPLETION_VERIFICATION,
        null);
  }

  private GateState failedGate(
      List<SteelHook04GatedRuntimeProofFinding> findings,
      int sequence,
      SteelHook04GatedRuntimeProofNextDirection nextDirection,
      String failureReason) {
    addFinding(
        findings,
        sequence,
        "Target-35 upstream gate passed.",
        SteelHook04GatedRuntimeProofFindingStatus.FAIL,
        true,
        failureReason,
        failureReason);
    return new GateState(false, nextDirection, failureReason);
  }

  private MinecraftServerRuntimePlan runtimePlanForFixture(
      MinecraftServerRuntimePlan baseRuntimePlan, Path runtimeJarPath) {
    return new MinecraftServerRuntimePlan(
        baseRuntimePlan.schema(),
        baseRuntimePlan.milestoneName(),
        baseRuntimePlan.projectJavaBaseline(),
        baseRuntimePlan.projectTargetMinecraft(),
        baseRuntimePlan.resolvedMinecraftVersion(),
        baseRuntimePlan.selectorUsed(),
        baseRuntimePlan.selectorResolutionReason(),
        baseRuntimePlan.manifestSource(),
        baseRuntimePlan.versionJsonSource(),
        runtimeJarPath.toAbsolutePath().normalize().toString(),
        "steelhook-0-4-runtime-fixture",
        baseRuntimePlan.serverJarSha1(),
        baseRuntimePlan.serverJarSha256(),
        baseRuntimePlan.serverJarSize(),
        baseRuntimePlan.launchMode(),
        baseRuntimePlan.launchModeReason(),
        baseRuntimePlan.mainClass(),
        List.of(),
        List.of(),
        baseRuntimePlan.jvmArgs(),
        baseRuntimePlan.serverArgs(),
        baseRuntimePlan.workingDirectory(),
        baseRuntimePlan.javaExecutable(),
        baseRuntimePlan.commandPreview(),
        baseRuntimePlan.cacheDirectory(),
        baseRuntimePlan.runtimeCacheDirectory(),
        baseRuntimePlan.offline(),
        baseRuntimePlan.strict(),
        baseRuntimePlan.networkRequestCount(),
        baseRuntimePlan.generatedFromCacheOnly(),
        baseRuntimePlan.replayableOffline(),
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        baseRuntimePlan.provenance());
  }

  private SteelHook04PrimitiveCandidate findCandidate(
      SteelHook04PrimitiveBoundaryReport report, SteelHook04PrimitiveKind primitiveKind) {
    return report.candidates().stream()
        .filter(candidate -> candidate.primitiveKind() == primitiveKind)
        .findFirst()
        .orElse(null);
  }

  private boolean candidateAllowed(SteelHook04PrimitiveCandidate candidate) {
    return candidate != null
        && candidate.internalOnly()
        && !candidate.publicApiExposed()
        && !candidate.runtimeReady()
        && !candidate.gatedRuntimeReady()
        && !candidate.implementedInTarget32();
  }

  private boolean sideEffectsSafe(
      boolean runtimeClassLoadingPathEnabled,
      boolean classLoadingOccurred,
      boolean serverLaunchOccurred,
      boolean minecraftMainInvoked,
      boolean hookInstallationOccurred,
      boolean runtimeDispatchOccurred,
      boolean publicApiExposed,
      boolean javaAgentUsed,
      boolean mixinUsed,
      boolean javaModExecutionSandboxed) {
    return !runtimeClassLoadingPathEnabled
        && !classLoadingOccurred
        && !serverLaunchOccurred
        && !minecraftMainInvoked
        && !hookInstallationOccurred
        && !runtimeDispatchOccurred
        && !publicApiExposed
        && !javaAgentUsed
        && !mixinUsed
        && !javaModExecutionSandboxed;
  }

  private int dispatcherInvocationCount() {
    return SteelHookDispatcher.beforeMinecraftServerMainInvocationCount()
        + SteelHookDispatcher.afterMinecraftServerMainInvocationCount();
  }

  private SteelHook04GatedRuntimeProofReport report(
      SteelHook04PrimitiveBoundaryReport target32Report,
      SteelHook04ReturnValueInterceptOfflineProofReport target33Report,
      SteelHook04InvokeRedirectWrapOfflineProofReport target34Report,
      boolean ready,
      SteelHook04GatedRuntimeProofStatus status,
      SteelHook04GatedRuntimeProofNextDirection nextDirection,
      String nextRecommendedAction,
      SteelHook04RuntimePrimitiveProof returnValueProof,
      SteelHook04RuntimePrimitiveProof invokeRedirectProof,
      SteelHook04RuntimePrimitiveProof invokeWrapProof,
      boolean unsupportedRejected,
      boolean unsupportedClassDefinitionAttempted,
      List<String> targetClassesDefined,
      String failureReason,
      List<SteelHook04GatedRuntimeProofFinding> findings) {
    int successCount = 0;
    if (returnValueProof.status() == SteelHook04GatedRuntimeProofStatus.GATED_RUNTIME_PROOF_READY) {
      successCount++;
    }
    if (invokeRedirectProof.status()
        == SteelHook04GatedRuntimeProofStatus.GATED_RUNTIME_PROOF_READY) {
      successCount++;
    }
    if (invokeWrapProof.status() == SteelHook04GatedRuntimeProofStatus.GATED_RUNTIME_PROOF_READY) {
      successCount++;
    }
    return new SteelHook04GatedRuntimeProofReport(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        STEELHOOK_VERSION,
        target32Report == null ? null : target32Report.milestoneName(),
        target32Report == null || target32Report.boundaryStatus() == null
            ? null
            : target32Report.boundaryStatus().name(),
        target32Report != null && target32Report.gatePassed(),
        target32Report == null ? 0 : target32Report.approvedPrimitiveCount(),
        target33Report == null ? null : target33Report.milestoneName(),
        target33Report == null || target33Report.proofStatus() == null
            ? null
            : target33Report.proofStatus().name(),
        target33Report != null && target33Report.proofReady(),
        target33Report == null ? 0 : target33Report.successfulProofCaseCount(),
        target34Report == null ? null : target34Report.milestoneName(),
        target34Report == null || target34Report.proofStatus() == null
            ? null
            : target34Report.proofStatus().name(),
        target34Report != null && target34Report.proofReady(),
        target34Report == null ? 0 : target34Report.successfulProofCaseCount(),
        ready,
        status,
        nextDirection,
        nextRecommendedAction,
        List.of(
            SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT,
            SteelHook04PrimitiveKind.INVOKE_REDIRECT,
            SteelHook04PrimitiveKind.INVOKE_WRAP),
        3,
        successCount,
        returnValueProof,
        invokeRedirectProof,
        invokeWrapProof,
        unsupportedRejected,
        unsupportedClassDefinitionAttempted,
        true,
        returnValueProof.classLoadingOccurred()
            || invokeRedirectProof.classLoadingOccurred()
            || invokeWrapProof.classLoadingOccurred(),
        !targetClassesDefined.isEmpty(),
        targetClassesDefined,
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
        findings);
  }

  private void addFinding(
      List<SteelHook04GatedRuntimeProofFinding> findings,
      int sequence,
      String checkName,
      SteelHook04GatedRuntimeProofFindingStatus status,
      boolean blocking,
      String summary,
      String details) {
    findings.add(
        new SteelHook04GatedRuntimeProofFinding(
            "target-35.gated-runtime.finding.%03d".formatted(sequence),
            checkName,
            status,
            blocking,
            summary,
            details));
  }

  private record GateState(
      boolean ready,
      SteelHook04GatedRuntimeProofNextDirection nextDirection,
      String failureReason) {}
}
