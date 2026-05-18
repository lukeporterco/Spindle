package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.minecraft.MinecraftClassLoadingAudit;
import com.spindle.core.minecraft.MinecraftRuntimeClassLoader;
import com.spindle.core.minecraft.MinecraftServerRuntimePlan;
import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationResult;
import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationStatus;
import com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher;
import com.spindle.core.minecraft.hook.transform.SteelHookMethodEntryRewriteResult;
import com.spindle.core.minecraft.hook.transform.SteelHookMethodExitRewriteResult;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class SteelHook03GatedRuntimeProofRunner {
  public static final String REPORT_FILE_NAME =
      "minecraft-steelhook-0-3-generalized-transformer-gated-runtime-proof.json";

  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-30";
  private static final String TARGET = "minecraft";
  private static final String STEELHOOK_VERSION = "0.3";
  private static final String ENTRY_RUNTIME_LOADER_ID = "minecraft-runtime-steelhook-0-3-entry";
  private static final String EXIT_RUNTIME_LOADER_ID = "minecraft-runtime-steelhook-0-3-exit";

  private final SteelHook03RuntimeFixtureJarWriter fixtureJarWriter;
  private final SteelHook02RuntimeClasspathUrlBuilder runtimeClasspathUrlBuilder;

  public SteelHook03GatedRuntimeProofRunner() {
    this(new SteelHook03RuntimeFixtureJarWriter(), new SteelHook02RuntimeClasspathUrlBuilder());
  }

  SteelHook03GatedRuntimeProofRunner(
      SteelHook03RuntimeFixtureJarWriter fixtureJarWriter,
      SteelHook02RuntimeClasspathUrlBuilder runtimeClasspathUrlBuilder) {
    this.fixtureJarWriter = fixtureJarWriter;
    this.runtimeClasspathUrlBuilder = runtimeClasspathUrlBuilder;
  }

  public SteelHook03RuntimeProofReport run(
      MinecraftServerRuntimePlan baseRuntimePlan,
      SteelHook03FramedMethodFoundationReport target28Report,
      SteelHook03MethodExitDispatchReport target29Report,
      Path fixtureOutputDirectory) {
    List<SteelHook03RuntimeProofFinding> findings = new ArrayList<>();
    String gateFailure =
        validateUpstream(baseRuntimePlan, target28Report, target29Report, fixtureOutputDirectory);
    if (gateFailure != null) {
      findings.add(new SteelHook03RuntimeProofFinding("target30.upstream-gate", true, gateFailure));
      return failureReport(
          target29Report,
          SteelHook03RuntimeProofStatus.BLOCKED,
          SteelHook03RuntimeProofNextDirection.RESTORE_TARGET_29_METHOD_EXIT_STATIC_DISPATCH,
          gateFailure,
          findings,
          blockedProof(SteelHook03RuntimeTransformSpec.methodEntryStaticDispatch(), gateFailure),
          blockedProof(SteelHook03RuntimeTransformSpec.methodExitStaticDispatch(), gateFailure));
    }

    SteelHook03RuntimePrimitiveProof entryProof;
    SteelHook03RuntimePrimitiveProof exitProof;
    try {
      entryProof =
          runSession(
              baseRuntimePlan,
              fixtureOutputDirectory.resolve("minecraft-steelhook-0-3-entry-runtime-fixture.jar"),
              ENTRY_RUNTIME_LOADER_ID,
              SteelHook03RuntimeTransformSpec.methodEntryStaticDispatch(),
              new SteelHook03FramedMethodFixtureClassFactory()
                  .createRuntimeMainFixtureClassBytes());
      exitProof =
          runSession(
              baseRuntimePlan,
              fixtureOutputDirectory.resolve("minecraft-steelhook-0-3-exit-runtime-fixture.jar"),
              EXIT_RUNTIME_LOADER_ID,
              SteelHook03RuntimeTransformSpec.methodExitStaticDispatch(),
              new SteelHook03MethodExitFixtureClassFactory().createMethodExitFixtureClassBytes());
    } catch (LoaderException exception) {
      findings.add(
          new SteelHook03RuntimeProofFinding(
              "target30.runtime-fixture", true, exception.getMessage()));
      return failureReport(
          target29Report,
          SteelHook03RuntimeProofStatus.FAILED,
          SteelHook03RuntimeProofNextDirection
              .RESTORE_TARGET_30_GENERALIZED_TRANSFORMER_GATED_RUNTIME_PROOF,
          exception.getMessage(),
          findings,
          blockedProof(
              SteelHook03RuntimeTransformSpec.methodEntryStaticDispatch(), exception.getMessage()),
          blockedProof(
              SteelHook03RuntimeTransformSpec.methodExitStaticDispatch(), exception.getMessage()));
    }

    findings.add(
        new SteelHook03RuntimeProofFinding(
            "target30.method-entry-runtime-proof",
            entryProof.status() != SteelHook03RuntimeProofStatus.GATED_RUNTIME_PROOF_READY,
            entryProof.status() == SteelHook03RuntimeProofStatus.GATED_RUNTIME_PROOF_READY
                ? "Target-30 defined the method-entry transformed class in an isolated runtime classloader session without initialization or dispatch."
                : entryProof.failureReason()));
    findings.add(
        new SteelHook03RuntimeProofFinding(
            "target30.method-exit-runtime-proof",
            exitProof.status() != SteelHook03RuntimeProofStatus.GATED_RUNTIME_PROOF_READY,
            exitProof.status() == SteelHook03RuntimeProofStatus.GATED_RUNTIME_PROOF_READY
                ? "Target-30 defined the method-exit transformed class in an isolated runtime classloader session without initialization or dispatch."
                : exitProof.failureReason()));

    boolean ready =
        entryProof.status() == SteelHook03RuntimeProofStatus.GATED_RUNTIME_PROOF_READY
            && exitProof.status() == SteelHook03RuntimeProofStatus.GATED_RUNTIME_PROOF_READY;
    List<String> targetClassesDefined = new ArrayList<>();
    if (entryProof.definedClassName() != null) {
      targetClassesDefined.add(entryProof.definedClassName());
    }
    if (exitProof.definedClassName() != null) {
      targetClassesDefined.add(exitProof.definedClassName());
    }
    return new SteelHook03RuntimeProofReport(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        STEELHOOK_VERSION,
        target29Report.milestoneName(),
        target29Report.status().id(),
        target29Report.methodExitDispatchReady(),
        target29Report.nextDirection().id(),
        ready,
        ready
            ? SteelHook03RuntimeProofStatus.GATED_RUNTIME_PROOF_READY
            : SteelHook03RuntimeProofStatus.FAILED,
        ready
            ? SteelHook03RuntimeProofNextDirection.MOVE_TO_TARGET_31_STEELHOOK_0_3_COMPLETION
            : SteelHook03RuntimeProofNextDirection
                .RESTORE_TARGET_30_GENERALIZED_TRANSFORMER_GATED_RUNTIME_PROOF,
        2,
        (ready ? 2 : 0)
            + (!ready
                    && entryProof.status()
                        == SteelHook03RuntimeProofStatus.GATED_RUNTIME_PROOF_READY
                ? 1
                : 0)
            + (!ready
                    && exitProof.status() == SteelHook03RuntimeProofStatus.GATED_RUNTIME_PROOF_READY
                ? 1
                : 0),
        entryProof,
        exitProof,
        true,
        entryProof.classLoadingOccurred() || exitProof.classLoadingOccurred(),
        targetClassesDefined,
        false,
        false,
        false,
        false,
        entryProof.dispatcherInvocationObserved(),
        exitProof.dispatcherInvocationObserved(),
        false,
        false,
        false,
        false,
        ready ? null : "Target-30 requires both isolated runtime proof sessions to pass.",
        findings);
  }

  private SteelHook03RuntimePrimitiveProof runSession(
      MinecraftServerRuntimePlan baseRuntimePlan,
      Path runtimeJarPath,
      String loaderId,
      SteelHook03RuntimeTransformSpec spec,
      byte[] fixtureClassBytes)
      throws LoaderException {
    SteelHookDispatcher.resetForBootstrap();
    int dispatcherCountBefore = dispatcherInvocationCount(spec);
    fixtureJarWriter.write(runtimeJarPath, fixtureClassBytes);
    SteelHook02RuntimeClasspathUrls runtimeClasspathUrls =
        runtimeClasspathUrlBuilder.build(runtimePlanForFixture(baseRuntimePlan, runtimeJarPath));
    MinecraftClassLoadingAudit audit = new MinecraftClassLoadingAudit();
    SteelHook03GatedRuntimeClassTransformer transformer =
        new SteelHook03GatedRuntimeClassTransformer(spec);
    try (MinecraftRuntimeClassLoader runtimeClassLoader =
        new MinecraftRuntimeClassLoader(
            loaderId,
            runtimeClasspathUrls.asArray(),
            getClass().getClassLoader(),
            audit,
            transformer)) {
      Class<?> definedClass = Class.forName(spec.targetBinaryName(), false, runtimeClassLoader);
      MinecraftBootstrapHookTransformationResult transformationResult = transformer.currentResult();
      int dispatcherCountAfter = dispatcherInvocationCount(spec);
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
                ? "Target-30 runtime classloader transformation did not produce a result."
                : transformationResult.failureReason());
      }
      if (definedClass.getClassLoader() != runtimeClassLoader) {
        return failedProof(
            spec,
            loaderId,
            runtimeClasspathUrls.normalizedAbsolutePaths().size(),
            dispatcherCountBefore,
            dispatcherCountAfter,
            "Target-30 expected the transformed class to be defined by the runtime classloader.");
      }
      if (dispatcherCountAfter != dispatcherCountBefore) {
        return failedProof(
            spec,
            loaderId,
            runtimeClasspathUrls.normalizedAbsolutePaths().size(),
            dispatcherCountBefore,
            dispatcherCountAfter,
            "Target-30 must not execute the runtime dispatcher during class definition.");
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
    } catch (ClassNotFoundException exception) {
      return failedProof(
          spec,
          loaderId,
          runtimeClasspathUrls.normalizedAbsolutePaths().size(),
          dispatcherCountBefore,
          dispatcherInvocationCount(spec),
          exception.getMessage());
    } catch (IOException exception) {
      return failedProof(
          spec,
          loaderId,
          runtimeClasspathUrls.normalizedAbsolutePaths().size(),
          dispatcherCountBefore,
          dispatcherInvocationCount(spec),
          exception.getMessage());
    }
  }

  private SteelHook03RuntimePrimitiveProof successfulProof(
      SteelHook03RuntimeTransformSpec spec,
      String loaderId,
      int runtimeClasspathEntryCount,
      MinecraftClassLoadingAudit.Summary auditSummary,
      SteelHook03GatedRuntimeClassTransformer transformer,
      MinecraftBootstrapHookTransformationResult transformationResult,
      Class<?> definedClass,
      MinecraftRuntimeClassLoader runtimeClassLoader,
      int dispatcherCountBefore,
      int dispatcherCountAfter) {
    SteelHookMethodEntryRewriteResult methodEntryRewriteResult =
        transformer.currentMethodEntryRewriteResult();
    SteelHookMethodExitRewriteResult methodExitRewriteResult =
        transformer.currentMethodExitRewriteResult();
    return new SteelHook03RuntimePrimitiveProof(
        spec.primitiveKind(),
        spec.sourceMilestone(),
        SteelHook03RuntimeProofStatus.GATED_RUNTIME_PROOF_READY,
        spec.targetBinaryName(),
        spec.targetInternalName(),
        spec.targetClassEntryName(),
        spec.targetMethodName(),
        spec.targetDescriptor(),
        spec.dispatcherOwnerInternalName(),
        spec.dispatcherMethodName(),
        spec.dispatcherDescriptor(),
        transformationResult.transformationMode().id(),
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
        dispatcherCountBefore,
        dispatcherCountAfter,
        spec.stackMapTableRewriteSupported(),
        methodEntryRewriteResult != null && methodEntryRewriteResult.stackMapTableRewriteApplied(),
        methodEntryRewriteResult != null
            && methodEntryRewriteResult.methodEntryTransformationOccurred(),
        methodExitRewriteResult != null
            && methodExitRewriteResult.methodExitTransformationOccurred(),
        transformationResult.bytecodeModified(),
        transformationResult.transformedClassBytes() != null,
        transformationResult.originalClassSha256(),
        transformationResult.transformedClassSha256(),
        transformationResult.originalCodeSha256(),
        transformationResult.transformedCodeSha256(),
        transformationResult.originalCodeLength(),
        transformationResult.transformedCodeLength(),
        transformationResult.constantPoolCountBefore(),
        transformationResult.constantPoolCountAfter(),
        null);
  }

  private SteelHook03RuntimePrimitiveProof blockedProof(
      SteelHook03RuntimeTransformSpec spec, String failureReason) {
    return new SteelHook03RuntimePrimitiveProof(
        spec.primitiveKind(),
        spec.sourceMilestone(),
        SteelHook03RuntimeProofStatus.BLOCKED,
        spec.targetBinaryName(),
        spec.targetInternalName(),
        spec.targetClassEntryName(),
        spec.targetMethodName(),
        spec.targetDescriptor(),
        spec.dispatcherOwnerInternalName(),
        spec.dispatcherMethodName(),
        spec.dispatcherDescriptor(),
        spec.transformationMode() == null ? null : spec.transformationMode().id(),
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
        0,
        0,
        spec.stackMapTableRewriteSupported(),
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
        null,
        null,
        null,
        failureReason);
  }

  private SteelHook03RuntimePrimitiveProof failedProof(
      SteelHook03RuntimeTransformSpec spec,
      String loaderId,
      int runtimeClasspathEntryCount,
      int dispatcherCountBefore,
      int dispatcherCountAfter,
      String failureReason) {
    return new SteelHook03RuntimePrimitiveProof(
        spec.primitiveKind(),
        spec.sourceMilestone(),
        SteelHook03RuntimeProofStatus.FAILED,
        spec.targetBinaryName(),
        spec.targetInternalName(),
        spec.targetClassEntryName(),
        spec.targetMethodName(),
        spec.targetDescriptor(),
        spec.dispatcherOwnerInternalName(),
        spec.dispatcherMethodName(),
        spec.dispatcherDescriptor(),
        spec.transformationMode() == null ? null : spec.transformationMode().id(),
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
        dispatcherCountAfter != dispatcherCountBefore,
        dispatcherCountBefore,
        dispatcherCountAfter,
        spec.stackMapTableRewriteSupported(),
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
        null,
        null,
        null,
        failureReason);
  }

  private SteelHook03RuntimeProofReport failureReport(
      SteelHook03MethodExitDispatchReport target29Report,
      SteelHook03RuntimeProofStatus status,
      SteelHook03RuntimeProofNextDirection nextDirection,
      String failureReason,
      List<SteelHook03RuntimeProofFinding> findings,
      SteelHook03RuntimePrimitiveProof entryProof,
      SteelHook03RuntimePrimitiveProof exitProof) {
    return new SteelHook03RuntimeProofReport(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        STEELHOOK_VERSION,
        target29Report == null ? null : target29Report.milestoneName(),
        target29Report == null || target29Report.status() == null
            ? null
            : target29Report.status().id(),
        target29Report != null && target29Report.methodExitDispatchReady(),
        target29Report == null || target29Report.nextDirection() == null
            ? null
            : target29Report.nextDirection().id(),
        false,
        status,
        nextDirection,
        2,
        0,
        entryProof,
        exitProof,
        true,
        false,
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
        failureReason,
        findings);
  }

  private String validateUpstream(
      MinecraftServerRuntimePlan baseRuntimePlan,
      SteelHook03FramedMethodFoundationReport target28Report,
      SteelHook03MethodExitDispatchReport target29Report,
      Path fixtureOutputDirectory) {
    if (baseRuntimePlan == null) {
      return "Target-30 requires a Minecraft runtime plan.";
    }
    if (fixtureOutputDirectory == null) {
      return "Target-30 requires a fixture output directory.";
    }
    if (target29Report == null) {
      return "Target-30 requires the Target-29 method-exit static dispatch report.";
    }
    if (target29Report.status() != SteelHook03MethodExitDispatchStatus.METHOD_EXIT_DISPATCH_READY
        || !target29Report.methodExitDispatchReady()
        || target29Report.nextDirection()
            != SteelHook03MethodExitDispatchNextDirection
                .MOVE_TO_TARGET_30_GENERALIZED_TRANSFORMER_GATED_RUNTIME_PROOF) {
      return "Target-30 requires a passed Target-29 handoff.";
    }
    if (target28Report == null) {
      return "Target-30 requires the Target-28 framed method foundation report.";
    }
    if (target28Report.status() != SteelHook03FramedMethodFoundationStatus.FOUNDATION_READY
        || !target28Report.framedMethodFoundationReady()
        || target28Report.nextDirection()
            != SteelHook03FramedMethodFoundationNextDirection
                .MOVE_TO_TARGET_29_METHOD_EXIT_STATIC_DISPATCH) {
      return "Target-30 requires a passed Target-28 handoff.";
    }
    if (!target28Report.milestoneName().equals(target29Report.sourceTarget28Milestone())
        || !target28Report.status().id().equals(target29Report.sourceTarget28Status())
        || target28Report.framedMethodFoundationReady()
            != target29Report.sourceTarget28FramedMethodFoundationReady()
        || !target28Report
            .nextDirection()
            .id()
            .equals(target29Report.sourceTarget28NextDirection())) {
      return "Target-29 did not preserve the approved Target-28 handoff state for Target-30.";
    }
    return null;
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
        "steelhook-0-3-runtime-fixture",
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

  private int dispatcherInvocationCount(SteelHook03RuntimeTransformSpec spec) {
    if (spec.primitiveKind() == SteelHook03PrimitiveKind.METHOD_ENTRY_STATIC_DISPATCH) {
      return SteelHookDispatcher.beforeMinecraftServerMainInvocationCount();
    }
    return SteelHookDispatcher.afterMinecraftServerMainInvocationCount();
  }
}
