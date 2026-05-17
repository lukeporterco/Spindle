package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.hook.transform.SteelHookMethodEntryClassFileRewriter;
import com.spindle.core.minecraft.hook.transform.SteelHookMethodEntryRewriteRequest;
import com.spindle.core.minecraft.hook.transform.SteelHookMethodEntryRewriteResult;
import com.spindle.core.minecraft.hook.transform.SteelHookMethodEntryRewriteStatus;
import com.spindle.core.minecraft.hook.verify.SteelHook02CompletionHandoffStatus;
import com.spindle.core.minecraft.hook.verify.SteelHook02CompletionReport;
import com.spindle.core.minecraft.hook.verify.SteelHook02CompletionStatus;
import java.util.ArrayList;
import java.util.List;

public final class SteelHook03FramedMethodFoundationRunner {
  public static final String REPORT_FILE_NAME =
      "minecraft-steelhook-0-3-framed-method-foundation.json";
  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-28";
  private static final String TARGET = "minecraft";
  private static final String STEELHOOK_VERSION = "0.3";
  private static final int INSERTION_OFFSET = 0;
  private static final int INSERTED_INSTRUCTION_LENGTH = 3;

  private final SteelHookMethodEntryClassFileRewriter rewriter;

  public SteelHook03FramedMethodFoundationRunner() {
    this(new SteelHookMethodEntryClassFileRewriter());
  }

  SteelHook03FramedMethodFoundationRunner(SteelHookMethodEntryClassFileRewriter rewriter) {
    this.rewriter = rewriter;
  }

  public SteelHook03FramedMethodFoundationReport run(
      SteelHook02CompletionReport completionReport, byte[] framedFixtureClassBytes) {
    List<SteelHook03FramedMethodFoundationFinding> findings = new ArrayList<>();
    if (completionReport == null) {
      findings.add(
          new SteelHook03FramedMethodFoundationFinding(
              "target28.source-report-missing", true, "Target-27 completion report is missing."));
      return failureReport(
          null,
          false,
          SteelHook03FramedMethodFoundationStatus.BLOCKED,
          SteelHook03FramedMethodFoundationNextDirection.RESTORE_TARGET_27_STEELHOOK_0_2_COMPLETION,
          "Target-28 requires the Target-27 completion report.",
          findings,
          null);
    }
    if (completionReport.status() != SteelHook02CompletionStatus.PASSED
        || !completionReport.completionReady()
        || completionReport.handoffStatus()
            != SteelHook02CompletionHandoffStatus.STEELHOOK_0_2_COMPLETE) {
      findings.add(
          new SteelHook03FramedMethodFoundationFinding(
              "target28.source-report-blocked",
              true,
              "Target-27 completion must pass and hand off steelhook-0-2-complete."));
      return failureReport(
          completionReport,
          false,
          SteelHook03FramedMethodFoundationStatus.BLOCKED,
          SteelHook03FramedMethodFoundationNextDirection.RESTORE_TARGET_27_STEELHOOK_0_2_COMPLETION,
          "Target-28 requires a passed Target-27 completion handoff.",
          findings,
          null);
    }
    if (framedFixtureClassBytes == null || framedFixtureClassBytes.length == 0) {
      findings.add(
          new SteelHook03FramedMethodFoundationFinding(
              "target28.fixture-missing", true, "Framed fixture class bytes are missing."));
      return failureReport(
          completionReport,
          false,
          SteelHook03FramedMethodFoundationStatus.FAILED,
          SteelHook03FramedMethodFoundationNextDirection.RESTORE_TARGET_28_FRAMED_METHOD_FOUNDATION,
          "Target-28 requires framed fixture class bytes.",
          findings,
          null);
    }

    SteelHookMethodEntryRewriteRequest request =
        new SteelHookMethodEntryRewriteRequest(
            "target-28.framed-method-foundation.001",
            "Target-28 framed method foundation",
            "target-28.framed-method-foundation.001",
            "target-28.framed-method-foundation.placement.001",
            "target-28.framed-method-foundation.contract.001",
            SteelHook03FramedMethodFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
            SteelHook03FramedMethodFixtureClassFactory.TARGET_BINARY_NAME,
            SteelHook03FramedMethodFixtureClassFactory.TARGET_CLASS_ENTRY_NAME,
            SteelHook03FramedMethodFixtureClassFactory.TARGET_METHOD_NAME,
            SteelHook03FramedMethodFixtureClassFactory.TARGET_DESCRIPTOR,
            INSERTION_OFFSET,
            "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher",
            "com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher",
            "beforeMinecraftServerMain",
            "()V",
            "invokestatic",
            "b8",
            INSERTED_INSTRUCTION_LENGTH,
            true,
            false,
            false,
            false);
    SteelHookMethodEntryRewriteResult rewriteResult =
        rewriter.rewrite(request, framedFixtureClassBytes);
    if (rewriteResult.status() != SteelHookMethodEntryRewriteStatus.TRANSFORMED) {
      findings.add(
          new SteelHook03FramedMethodFoundationFinding(
              "target28.rewrite-failed",
              true,
              rewriteResult.failureReason() == null
                  ? "Framed method rewrite failed."
                  : rewriteResult.failureReason()));
      return failureReport(
          completionReport,
          false,
          SteelHook03FramedMethodFoundationStatus.FAILED,
          SteelHook03FramedMethodFoundationNextDirection.RESTORE_TARGET_28_FRAMED_METHOD_FOUNDATION,
          rewriteResult.failureReason(),
          findings,
          rewriteResult);
    }

    boolean frameShiftApplied =
        rewriteResult.firstFrameOffsetDeltaBefore() != null
            && rewriteResult.firstFrameOffsetDeltaAfter() != null
            && rewriteResult.firstFrameOffsetDeltaAfter()
                == rewriteResult.firstFrameOffsetDeltaBefore() + INSERTED_INSTRUCTION_LENGTH;
    if (!rewriteResult.stackMapTableRewriteApplied() || !frameShiftApplied) {
      findings.add(
          new SteelHook03FramedMethodFoundationFinding(
              "target28.frame-shift-missing",
              true,
              "Target-28 expected first-frame StackMapTable shifting by three bytes."));
      return failureReport(
          completionReport,
          false,
          SteelHook03FramedMethodFoundationStatus.FAILED,
          SteelHook03FramedMethodFoundationNextDirection.RESTORE_TARGET_28_FRAMED_METHOD_FOUNDATION,
          "Target-28 did not prove the expected first-frame StackMapTable shift.",
          findings,
          rewriteResult);
    }

    findings.add(
        new SteelHook03FramedMethodFoundationFinding(
            "target28.frame-shift-proved",
            false,
            "Target-28 shifted the first explicit StackMapTable frame by three bytes while preserving the bounded offline-only foundation."));
    return new SteelHook03FramedMethodFoundationReport(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        STEELHOOK_VERSION,
        completionReport.milestoneName(),
        completionReport.completionReady(),
        completionReport.handoffStatus().id(),
        true,
        SteelHook03FramedMethodFoundationStatus.FOUNDATION_READY,
        SteelHook03FramedMethodFoundationNextDirection
            .MOVE_TO_TARGET_29_METHOD_EXIT_STATIC_DISPATCH,
        true,
        rewriteResult.stackMapTableRewriteApplied(),
        frameShiftApplied,
        rewriteResult.stackMapTableEntryCountBefore(),
        rewriteResult.stackMapTableEntryCountAfter(),
        rewriteResult.firstFrameOffsetDeltaBefore(),
        rewriteResult.firstFrameOffsetDeltaAfter(),
        INSERTION_OFFSET,
        INSERTED_INSTRUCTION_LENGTH,
        rewriteResult.methodEntryTransformationOccurred(),
        rewriteResult.bytecodeModified(),
        rewriteResult.transformedClassBytesProduced(),
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

  public byte[] defaultFramedFixtureClassBytes() {
    return new SteelHook03FramedMethodFixtureClassFactory().createFramedMethodFixtureClassBytes();
  }

  private SteelHook03FramedMethodFoundationReport failureReport(
      SteelHook02CompletionReport completionReport,
      boolean framedMethodFoundationReady,
      SteelHook03FramedMethodFoundationStatus status,
      SteelHook03FramedMethodFoundationNextDirection nextDirection,
      String failureReason,
      List<SteelHook03FramedMethodFoundationFinding> findings,
      SteelHookMethodEntryRewriteResult rewriteResult) {
    return new SteelHook03FramedMethodFoundationReport(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        STEELHOOK_VERSION,
        completionReport == null ? null : completionReport.milestoneName(),
        completionReport != null && completionReport.completionReady(),
        completionReport == null || completionReport.handoffStatus() == null
            ? null
            : completionReport.handoffStatus().id(),
        framedMethodFoundationReady,
        status,
        nextDirection,
        true,
        rewriteResult != null && rewriteResult.stackMapTableRewriteApplied(),
        rewriteResult != null
            && rewriteResult.firstFrameOffsetDeltaBefore() != null
            && rewriteResult.firstFrameOffsetDeltaAfter() != null
            && rewriteResult.firstFrameOffsetDeltaAfter()
                == rewriteResult.firstFrameOffsetDeltaBefore() + INSERTED_INSTRUCTION_LENGTH,
        rewriteResult == null ? null : rewriteResult.stackMapTableEntryCountBefore(),
        rewriteResult == null ? null : rewriteResult.stackMapTableEntryCountAfter(),
        rewriteResult == null ? null : rewriteResult.firstFrameOffsetDeltaBefore(),
        rewriteResult == null ? null : rewriteResult.firstFrameOffsetDeltaAfter(),
        INSERTION_OFFSET,
        INSERTED_INSTRUCTION_LENGTH,
        rewriteResult != null && rewriteResult.methodEntryTransformationOccurred(),
        rewriteResult != null && rewriteResult.bytecodeModified(),
        rewriteResult != null && rewriteResult.transformedClassBytesProduced(),
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
}
