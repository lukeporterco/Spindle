package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.hook.transform.SteelHookMethodExitClassFileRewriter;
import com.spindle.core.minecraft.hook.transform.SteelHookMethodExitRewriteRequest;
import com.spindle.core.minecraft.hook.transform.SteelHookMethodExitRewriteResult;
import com.spindle.core.minecraft.hook.transform.SteelHookMethodExitRewriteStatus;
import java.util.ArrayList;
import java.util.List;

public final class SteelHook03MethodExitDispatchRunner {
  public static final String REPORT_FILE_NAME =
      "minecraft-steelhook-0-3-method-exit-static-dispatch.json";
  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-29";
  private static final String TARGET = "minecraft";
  private static final String STEELHOOK_VERSION = "0.3";
  private static final String DISPATCHER_OWNER_INTERNAL_NAME =
      "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher";
  private static final String DISPATCHER_BINARY_NAME =
      "com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher";
  private static final String DISPATCHER_METHOD_NAME = "afterMinecraftServerMain";
  private static final String DISPATCHER_DESCRIPTOR = "()V";
  private static final int INSERTED_INSTRUCTION_LENGTH = 3;

  private final SteelHookMethodExitClassFileRewriter rewriter;

  public SteelHook03MethodExitDispatchRunner() {
    this(new SteelHookMethodExitClassFileRewriter());
  }

  SteelHook03MethodExitDispatchRunner(SteelHookMethodExitClassFileRewriter rewriter) {
    this.rewriter = rewriter;
  }

  public SteelHook03MethodExitDispatchReport run(
      SteelHook03FramedMethodFoundationReport target28Report, byte[] fixtureClassBytes) {
    List<SteelHook03MethodExitDispatchFinding> findings = new ArrayList<>();
    if (target28Report == null) {
      findings.add(
          new SteelHook03MethodExitDispatchFinding(
              "target29.source-report-missing", true, "Target-28 report is missing."));
      return failureReport(
          null,
          false,
          SteelHook03MethodExitDispatchStatus.BLOCKED,
          SteelHook03MethodExitDispatchNextDirection.RESTORE_TARGET_28_FRAMED_METHOD_FOUNDATION,
          "Target-29 requires the Target-28 framed method foundation report.",
          findings,
          null);
    }
    if (target28Report.status() != SteelHook03FramedMethodFoundationStatus.FOUNDATION_READY
        || !target28Report.framedMethodFoundationReady()
        || target28Report.nextDirection()
            != SteelHook03FramedMethodFoundationNextDirection
                .MOVE_TO_TARGET_29_METHOD_EXIT_STATIC_DISPATCH) {
      findings.add(
          new SteelHook03MethodExitDispatchFinding(
              "target29.source-report-blocked",
              true,
              "Target-28 must hand off foundation-ready with next direction move-to-target-29-method-exit-static-dispatch."));
      return failureReport(
          target28Report,
          false,
          SteelHook03MethodExitDispatchStatus.BLOCKED,
          SteelHook03MethodExitDispatchNextDirection.RESTORE_TARGET_28_FRAMED_METHOD_FOUNDATION,
          "Target-29 requires a passed Target-28 framed method foundation handoff.",
          findings,
          null);
    }
    if (fixtureClassBytes == null || fixtureClassBytes.length == 0) {
      findings.add(
          new SteelHook03MethodExitDispatchFinding(
              "target29.fixture-missing", true, "Method-exit fixture class bytes are missing."));
      return failureReport(
          target28Report,
          false,
          SteelHook03MethodExitDispatchStatus.FAILED,
          SteelHook03MethodExitDispatchNextDirection.RESTORE_TARGET_29_METHOD_EXIT_STATIC_DISPATCH,
          "Target-29 requires method-exit fixture class bytes.",
          findings,
          null);
    }

    SteelHookMethodExitRewriteRequest request =
        new SteelHookMethodExitRewriteRequest(
            "target-29.method-exit-static-dispatch.001",
            "Target-29 method-exit static dispatch",
            "Target-28",
            "minecraft-steelhook-0-3-framed-method-foundation.json",
            SteelHook03MethodExitFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
            SteelHook03MethodExitFixtureClassFactory.TARGET_BINARY_NAME,
            SteelHook03MethodExitFixtureClassFactory.TARGET_CLASS_ENTRY_NAME,
            SteelHook03MethodExitFixtureClassFactory.TARGET_METHOD_NAME,
            SteelHook03MethodExitFixtureClassFactory.TARGET_DESCRIPTOR,
            DISPATCHER_OWNER_INTERNAL_NAME,
            DISPATCHER_BINARY_NAME,
            DISPATCHER_METHOD_NAME,
            DISPATCHER_DESCRIPTOR,
            "invokestatic",
            "b8",
            INSERTED_INSTRUCTION_LENGTH,
            false,
            false,
            false,
            false);
    SteelHookMethodExitRewriteResult rewriteResult = rewriter.rewrite(request, fixtureClassBytes);
    if (rewriteResult.status() != SteelHookMethodExitRewriteStatus.TRANSFORMED) {
      findings.add(
          new SteelHook03MethodExitDispatchFinding(
              "target29.rewrite-failed",
              true,
              rewriteResult.failureReason() == null
                  ? "Method-exit rewrite failed."
                  : rewriteResult.failureReason()));
      return failureReport(
          target28Report,
          false,
          SteelHook03MethodExitDispatchStatus.FAILED,
          SteelHook03MethodExitDispatchNextDirection.RESTORE_TARGET_29_METHOD_EXIT_STATIC_DISPATCH,
          rewriteResult.failureReason(),
          findings,
          rewriteResult);
    }

    findings.add(
        new SteelHook03MethodExitDispatchFinding(
            "target29.method-exit-dispatch-proved",
            false,
            "Target-29 inserted invokestatic SteelHookDispatcher.afterMinecraftServerMain:()V immediately before the supported normal return opcode in bounded offline bytecode."));
    return new SteelHook03MethodExitDispatchReport(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        STEELHOOK_VERSION,
        SteelHook03PrimitiveKind.METHOD_EXIT_STATIC_DISPATCH,
        target28Report.milestoneName(),
        target28Report.status().id(),
        target28Report.framedMethodFoundationReady(),
        target28Report.nextDirection().id(),
        true,
        SteelHook03MethodExitDispatchStatus.METHOD_EXIT_DISPATCH_READY,
        SteelHook03MethodExitDispatchNextDirection
            .MOVE_TO_TARGET_30_GENERALIZED_TRANSFORMER_GATED_RUNTIME_PROOF,
        request.targetOwnerInternalName(),
        request.targetMethodName(),
        request.targetDescriptor(),
        request.dispatcherOwnerInternalName(),
        request.dispatcherMethodName(),
        request.dispatcherDescriptor(),
        request.opcodeMnemonic(),
        request.opcodeHex(),
        request.instructionLength(),
        rewriteResult.codePatch().supportedReturnOpcodes(),
        rewriteResult.normalReturnOpcodeCount(),
        rewriteResult.insertionCount(),
        rewriteResult.codePatch().insertionOffsetsOriginal(),
        rewriteResult.codePatch().insertionOffsetsTransformed(),
        rewriteResult.originalCodeLength(),
        rewriteResult.transformedCodeLength(),
        rewriteResult.constantPoolCountBefore(),
        rewriteResult.constantPoolCountAfter(),
        rewriteResult.methodExitTransformationOccurred(),
        rewriteResult.bytecodeModified(),
        rewriteResult.transformedClassBytesProduced(),
        rewriteResult.stackMapTablePresent(),
        rewriteResult.stackMapTableRewriteSupported(),
        rewriteResult.stackMapTableRewriteApplied(),
        rewriteResult.exceptionTablePresent(),
        rewriteResult.branchRewriteRequired(),
        rewriteResult.switchRewriteRequired(),
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
        findings);
  }

  public byte[] defaultMethodExitFixtureClassBytes() {
    return new SteelHook03MethodExitFixtureClassFactory().createMethodExitFixtureClassBytes();
  }

  private SteelHook03MethodExitDispatchReport failureReport(
      SteelHook03FramedMethodFoundationReport target28Report,
      boolean methodExitDispatchReady,
      SteelHook03MethodExitDispatchStatus status,
      SteelHook03MethodExitDispatchNextDirection nextDirection,
      String failureReason,
      List<SteelHook03MethodExitDispatchFinding> findings,
      SteelHookMethodExitRewriteResult rewriteResult) {
    return new SteelHook03MethodExitDispatchReport(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        STEELHOOK_VERSION,
        SteelHook03PrimitiveKind.METHOD_EXIT_STATIC_DISPATCH,
        target28Report == null ? null : target28Report.milestoneName(),
        target28Report == null || target28Report.status() == null
            ? null
            : target28Report.status().id(),
        target28Report != null && target28Report.framedMethodFoundationReady(),
        target28Report == null || target28Report.nextDirection() == null
            ? null
            : target28Report.nextDirection().id(),
        methodExitDispatchReady,
        status,
        nextDirection,
        SteelHook03MethodExitFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook03MethodExitFixtureClassFactory.TARGET_METHOD_NAME,
        SteelHook03MethodExitFixtureClassFactory.TARGET_DESCRIPTOR,
        DISPATCHER_OWNER_INTERNAL_NAME,
        DISPATCHER_METHOD_NAME,
        DISPATCHER_DESCRIPTOR,
        "invokestatic",
        "b8",
        INSERTED_INSTRUCTION_LENGTH,
        supportedReturnOpcodes(),
        rewriteResult == null ? null : rewriteResult.normalReturnOpcodeCount(),
        rewriteResult == null ? null : rewriteResult.insertionCount(),
        rewriteResult == null || rewriteResult.codePatch() == null
            ? List.of()
            : rewriteResult.codePatch().insertionOffsetsOriginal(),
        rewriteResult == null || rewriteResult.codePatch() == null
            ? List.of()
            : rewriteResult.codePatch().insertionOffsetsTransformed(),
        rewriteResult == null ? null : rewriteResult.originalCodeLength(),
        rewriteResult == null ? null : rewriteResult.transformedCodeLength(),
        rewriteResult == null ? null : rewriteResult.constantPoolCountBefore(),
        rewriteResult == null ? null : rewriteResult.constantPoolCountAfter(),
        rewriteResult != null && rewriteResult.methodExitTransformationOccurred(),
        rewriteResult != null && rewriteResult.bytecodeModified(),
        rewriteResult != null && rewriteResult.transformedClassBytesProduced(),
        rewriteResult != null && rewriteResult.stackMapTablePresent(),
        false,
        false,
        rewriteResult != null && rewriteResult.exceptionTablePresent(),
        rewriteResult != null && rewriteResult.branchRewriteRequired(),
        rewriteResult != null && rewriteResult.switchRewriteRequired(),
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

  private List<String> supportedReturnOpcodes() {
    return List.of("ireturn", "lreturn", "freturn", "dreturn", "areturn", "return");
  }
}
