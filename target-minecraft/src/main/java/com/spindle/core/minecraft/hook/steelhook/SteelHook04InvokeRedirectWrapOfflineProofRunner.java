package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.hook.transform.SteelHookInvokeCallsiteClassFileRewriter;
import com.spindle.core.minecraft.hook.transform.SteelHookInvokeCallsiteRewriteMode;
import com.spindle.core.minecraft.hook.transform.SteelHookInvokeCallsiteRewriteRequest;
import com.spindle.core.minecraft.hook.transform.SteelHookInvokeCallsiteRewriteResult;
import com.spindle.core.minecraft.hook.transform.SteelHookInvokeCallsiteRewriteStatus;
import com.spindle.core.minecraft.hook.transform.SteelHookInvokeOpcode;
import java.util.ArrayList;
import java.util.List;

public final class SteelHook04InvokeRedirectWrapOfflineProofRunner {
  public static final String REPORT_FILE_NAME =
      "minecraft-steelhook-0-4-invoke-redirect-wrap-offline-proof.json";

  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-34";
  private static final String TARGET = "minecraft";
  private static final String STEELHOOK_VERSION = "0.4";
  private static final String READY_ACTION =
      "Move next to Target-35 isolated gated runtime class-definition proof for the approved SteelHook 0.4 primitives without installing hooks or invoking Minecraft.";
  private static final String BLOCKED_ACTION =
      "Restore the Target-32 and Target-33 source reports before continuing with Target-34 offline invoke redirect and wrap proof.";
  private static final String FAILED_ACTION =
      "Restore the bounded Target-34 invoke rewrite fixtures or rewriter validation before continuing toward Target-35.";

  private final SteelHookInvokeCallsiteClassFileRewriter rewriter;
  private final SteelHook04InvokeCallsiteFixtureClassFactory fixtureFactory;

  public SteelHook04InvokeRedirectWrapOfflineProofRunner() {
    this(
        new SteelHookInvokeCallsiteClassFileRewriter(),
        new SteelHook04InvokeCallsiteFixtureClassFactory());
  }

  SteelHook04InvokeRedirectWrapOfflineProofRunner(
      SteelHookInvokeCallsiteClassFileRewriter rewriter,
      SteelHook04InvokeCallsiteFixtureClassFactory fixtureFactory) {
    this.rewriter = rewriter;
    this.fixtureFactory = fixtureFactory;
  }

  public SteelHook04InvokeRedirectWrapOfflineProofReport run(
      SteelHook04PrimitiveBoundaryReport sourceTarget32,
      SteelHook04ReturnValueInterceptOfflineProofReport sourceTarget33) {
    List<SteelHook04InvokeCallsiteFinding> findings = new ArrayList<>();
    SteelHook04PrimitiveCandidate invokeRedirectCandidate =
        findCandidate(sourceTarget32, SteelHook04PrimitiveKind.INVOKE_REDIRECT);
    SteelHook04PrimitiveCandidate invokeWrapCandidate =
        findCandidate(sourceTarget32, SteelHook04PrimitiveKind.INVOKE_WRAP);
    GateState gateState =
        gateState(
            sourceTarget32, sourceTarget33, invokeRedirectCandidate, invokeWrapCandidate, findings);
    if (!gateState.ready()) {
      return report(
          sourceTarget32,
          sourceTarget33,
          invokeRedirectCandidate,
          invokeWrapCandidate,
          false,
          SteelHook04InvokeRedirectWrapOfflineProofStatus.BLOCKED,
          gateState.nextDirection(),
          BLOCKED_ACTION,
          0,
          0,
          List.of(),
          findings);
    }

    byte[] fixtureClassBytes = fixtureFactory.createFixtureClassBytes();
    try {
      List<SteelHook04InvokeCallsiteProofCase> proofCases = new ArrayList<>();
      proofCases.add(
          successfulCase(
              "target-34.invoke.case.001",
              "invoke redirect replacement",
              successful(
                  request(
                      "target-34.invoke.request.001",
                      SteelHook04PrimitiveKind.INVOKE_REDIRECT,
                      SteelHookInvokeCallsiteRewriteMode.REDIRECT,
                      SteelHook04InvokeCallsiteFixtureClassFactory.INVOKE_METHOD_NAME,
                      SteelHook04InvokeCallsiteFixtureClassFactory.ORIGINAL_METHOD_NAME,
                      SteelHook04InvokeCallsiteFixtureClassFactory.REDIRECTED_METHOD_NAME),
                  fixtureClassBytes)));
      proofCases.add(
          successfulCase(
              "target-34.invoke.case.002",
              "invoke wrap replacement",
              successful(
                  request(
                      "target-34.invoke.request.002",
                      SteelHook04PrimitiveKind.INVOKE_WRAP,
                      SteelHookInvokeCallsiteRewriteMode.WRAP,
                      SteelHook04InvokeCallsiteFixtureClassFactory.INVOKE_METHOD_NAME,
                      SteelHook04InvokeCallsiteFixtureClassFactory.ORIGINAL_METHOD_NAME,
                      SteelHook04InvokeCallsiteFixtureClassFactory.WRAPPED_METHOD_NAME),
                  fixtureClassBytes)));

      int rejectionProofCaseCount = rejectionCoverage(fixtureClassBytes, findings);
      addFinding(
          findings,
          30,
          "Target-34 stayed offline-only and fixture-bounded.",
          SteelHook04InvokeCallsiteFindingStatus.PASS,
          true,
          "Target-34 produced deterministic redirect and wrap evidence without runtime side effects.",
          "No runtime class loading, hook installation, dispatcher execution, Minecraft execution, public API exposure, or sandbox claim occurred.");

      return report(
          sourceTarget32,
          sourceTarget33,
          invokeRedirectCandidate,
          invokeWrapCandidate,
          true,
          SteelHook04InvokeRedirectWrapOfflineProofStatus.PROOF_READY,
          SteelHook04InvokeRedirectWrapOfflineProofNextDirection
              .MOVE_TO_TARGET_35_GATED_RUNTIME_CLASS_DEFINITION_PROOF,
          READY_ACTION,
          proofCases.size(),
          rejectionProofCaseCount,
          proofCases,
          findings);
    } catch (IllegalStateException exception) {
      addFinding(
          findings,
          31,
          "Target-34 successful proof cases executed.",
          SteelHook04InvokeCallsiteFindingStatus.FAIL,
          true,
          "Target-34 expected both redirect and wrap proof cases to transform successfully.",
          exception.getMessage());
      return report(
          sourceTarget32,
          sourceTarget33,
          invokeRedirectCandidate,
          invokeWrapCandidate,
          false,
          SteelHook04InvokeRedirectWrapOfflineProofStatus.FAILED,
          SteelHook04InvokeRedirectWrapOfflineProofNextDirection
              .RESTORE_TARGET_34_INVOKE_REWRITE_FIXTURES,
          FAILED_ACTION,
          0,
          0,
          List.of(),
          findings);
    }
  }

  private SteelHookInvokeCallsiteRewriteRequest request(
      String id,
      SteelHook04PrimitiveKind primitiveKind,
      SteelHookInvokeCallsiteRewriteMode rewriteMode,
      String targetMethodName,
      String expectedInvokeName,
      String replacementInvokeName) {
    return new SteelHookInvokeCallsiteRewriteRequest(
        id,
        "Target-34 invoke redirect/wrap offline proof",
        "Target-33",
        SteelHook04ReturnValueInterceptOfflineProofReportWriter.REPORT_FILE_NAME,
        primitiveKind,
        rewriteMode,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_BINARY_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_CLASS_ENTRY_NAME,
        targetMethodName,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        expectedInvokeName,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        SteelHookInvokeOpcode.INVOKESTATIC,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        replacementInvokeName,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        SteelHookInvokeOpcode.INVOKESTATIC,
        false,
        false,
        false);
  }

  private SteelHookInvokeCallsiteRewriteResult successful(
      SteelHookInvokeCallsiteRewriteRequest request, byte[] fixtureClassBytes) {
    SteelHookInvokeCallsiteRewriteResult result = rewriter.rewrite(request, fixtureClassBytes);
    if (result.status() == SteelHookInvokeCallsiteRewriteStatus.REJECTED) {
      throw new IllegalStateException(
          "Expected successful Target-34 proof case: " + result.failureReason());
    }
    return result;
  }

  private int rejectionCoverage(
      byte[] fixtureClassBytes, List<SteelHook04InvokeCallsiteFinding> findings) {
    int count = 0;
    count += rejected(rewriter.rewrite(null, fixtureClassBytes)) ? 1 : 0;
    count +=
        rejected(
                rewriter.rewrite(
                    new SteelHookInvokeCallsiteRewriteRequest(
                        "target-34.reject.001",
                        "Target-34 wrong primitive",
                        "Target-33",
                        SteelHook04ReturnValueInterceptOfflineProofReportWriter.REPORT_FILE_NAME,
                        SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT,
                        SteelHookInvokeCallsiteRewriteMode.REDIRECT,
                        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_BINARY_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_CLASS_ENTRY_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.INVOKE_METHOD_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
                        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.ORIGINAL_METHOD_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
                        SteelHookInvokeOpcode.INVOKESTATIC,
                        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.REDIRECTED_METHOD_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
                        SteelHookInvokeOpcode.INVOKESTATIC,
                        false,
                        false,
                        false),
                    fixtureClassBytes))
            ? 1
            : 0;
    count += rejected(rewriter.rewrite(wrongOwnerRequest(), fixtureClassBytes)) ? 1 : 0;
    count += rejected(rewriter.rewrite(wrongMethodRequest(), fixtureClassBytes)) ? 1 : 0;
    count += rejected(rewriter.rewrite(wrongDescriptorRequest(), fixtureClassBytes)) ? 1 : 0;
    count += rejected(rewriter.rewrite(wrongInvokeOwnerRequest(), fixtureClassBytes)) ? 1 : 0;
    count += rejected(rewriter.rewrite(wrongInvokeNameRequest(), fixtureClassBytes)) ? 1 : 0;
    count += rejected(rewriter.rewrite(wrongInvokeDescriptorRequest(), fixtureClassBytes)) ? 1 : 0;
    count += rejected(rewriter.rewrite(wrongInvokeOpcodeRequest(), fixtureClassBytes)) ? 1 : 0;
    count +=
        rejected(
                rewriter.rewrite(
                    request(
                        "target-34.reject.009",
                        SteelHook04PrimitiveKind.INVOKE_REDIRECT,
                        SteelHookInvokeCallsiteRewriteMode.REDIRECT,
                        SteelHook04InvokeCallsiteFixtureClassFactory.NO_INVOKE_METHOD_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.ORIGINAL_METHOD_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.REDIRECTED_METHOD_NAME),
                    fixtureClassBytes))
            ? 1
            : 0;
    count +=
        rejected(
                rewriter.rewrite(
                    request(
                        "target-34.reject.010",
                        SteelHook04PrimitiveKind.INVOKE_REDIRECT,
                        SteelHookInvokeCallsiteRewriteMode.REDIRECT,
                        SteelHook04InvokeCallsiteFixtureClassFactory.AMBIGUOUS_METHOD_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.ORIGINAL_METHOD_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.REDIRECTED_METHOD_NAME),
                    fixtureClassBytes))
            ? 1
            : 0;
    count +=
        rejected(
                rewriter.rewrite(
                    request(
                        "target-34.reject.011",
                        SteelHook04PrimitiveKind.INVOKE_REDIRECT,
                        SteelHookInvokeCallsiteRewriteMode.REDIRECT,
                        SteelHook04InvokeCallsiteFixtureClassFactory.CONSTRUCTOR_METHOD_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.ORIGINAL_METHOD_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.REDIRECTED_METHOD_NAME),
                    fixtureClassBytes))
            ? 1
            : 0;
    count +=
        rejected(
                rewriter.rewrite(
                    request(
                        "target-34.reject.012",
                        SteelHook04PrimitiveKind.INVOKE_REDIRECT,
                        SteelHookInvokeCallsiteRewriteMode.REDIRECT,
                        SteelHook04InvokeCallsiteFixtureClassFactory.SPECIAL_METHOD_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.ORIGINAL_METHOD_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.REDIRECTED_METHOD_NAME),
                    fixtureClassBytes))
            ? 1
            : 0;
    count +=
        rejected(rewriter.rewrite(replacementDescriptorMismatchRequest(), fixtureClassBytes))
            ? 1
            : 0;
    count +=
        rejected(rewriter.rewrite(replacementOpcodeMismatchRequest(), fixtureClassBytes)) ? 1 : 0;
    count +=
        rejected(
                rewriter.rewrite(
                    request(
                        "target-34.reject.015",
                        SteelHook04PrimitiveKind.INVOKE_REDIRECT,
                        SteelHookInvokeCallsiteRewriteMode.REDIRECT,
                        SteelHook04InvokeCallsiteFixtureClassFactory.BRANCHING_METHOD_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.ORIGINAL_METHOD_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.REDIRECTED_METHOD_NAME),
                    fixtureClassBytes))
            ? 1
            : 0;
    count +=
        rejected(
                rewriter.rewrite(
                    request(
                        "target-34.reject.016",
                        SteelHook04PrimitiveKind.INVOKE_REDIRECT,
                        SteelHookInvokeCallsiteRewriteMode.REDIRECT,
                        SteelHook04InvokeCallsiteFixtureClassFactory.SWITCH_METHOD_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.ORIGINAL_METHOD_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.REDIRECTED_METHOD_NAME),
                    fixtureClassBytes))
            ? 1
            : 0;
    count +=
        rejected(
                rewriter.rewrite(
                    request(
                        "target-34.reject.017",
                        SteelHook04PrimitiveKind.INVOKE_REDIRECT,
                        SteelHookInvokeCallsiteRewriteMode.REDIRECT,
                        SteelHook04InvokeCallsiteFixtureClassFactory.EXCEPTION_TABLE_METHOD_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.ORIGINAL_METHOD_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.REDIRECTED_METHOD_NAME),
                    fixtureClassBytes))
            ? 1
            : 0;
    count +=
        rejected(
                rewriter.rewrite(
                    request(
                        "target-34.reject.018",
                        SteelHook04PrimitiveKind.INVOKE_REDIRECT,
                        SteelHookInvokeCallsiteRewriteMode.REDIRECT,
                        SteelHook04InvokeCallsiteFixtureClassFactory.STACKMAP_METHOD_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.ORIGINAL_METHOD_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.REDIRECTED_METHOD_NAME),
                    fixtureClassBytes))
            ? 1
            : 0;
    count +=
        rejected(
                rewriter.rewrite(
                    request(
                        "target-34.reject.019",
                        SteelHook04PrimitiveKind.INVOKE_REDIRECT,
                        SteelHookInvokeCallsiteRewriteMode.REDIRECT,
                        SteelHook04InvokeCallsiteFixtureClassFactory.SYNCHRONIZED_METHOD_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.ORIGINAL_METHOD_NAME,
                        SteelHook04InvokeCallsiteFixtureClassFactory.REDIRECTED_METHOD_NAME),
                    fixtureClassBytes))
            ? 1
            : 0;
    addFinding(
        findings,
        29,
        "Target-34 exercised the required rejection taxonomy.",
        count >= 19
            ? SteelHook04InvokeCallsiteFindingStatus.PASS
            : SteelHook04InvokeCallsiteFindingStatus.FAIL,
        true,
        "Target-34 rewriter rejection coverage was exercised against bounded malformed and unsupported invoke shapes.",
        "rejectionProofCaseCount=" + count);
    return count;
  }

  private boolean rejected(SteelHookInvokeCallsiteRewriteResult result) {
    return result.status() == SteelHookInvokeCallsiteRewriteStatus.REJECTED;
  }

  private SteelHookInvokeCallsiteRewriteRequest wrongOwnerRequest() {
    return new SteelHookInvokeCallsiteRewriteRequest(
        "target-34.reject.wrong-owner",
        "test",
        "Target-33",
        SteelHook04ReturnValueInterceptOfflineProofReportWriter.REPORT_FILE_NAME,
        SteelHook04PrimitiveKind.INVOKE_REDIRECT,
        SteelHookInvokeCallsiteRewriteMode.REDIRECT,
        "wrong/Owner",
        "wrong.Owner",
        "wrong/Owner.class",
        SteelHook04InvokeCallsiteFixtureClassFactory.INVOKE_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.ORIGINAL_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        SteelHookInvokeOpcode.INVOKESTATIC,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.REDIRECTED_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        SteelHookInvokeOpcode.INVOKESTATIC,
        false,
        false,
        false);
  }

  private SteelHookInvokeCallsiteRewriteRequest wrongMethodRequest() {
    return request(
        "target-34.reject.wrong-method",
        SteelHook04PrimitiveKind.INVOKE_REDIRECT,
        SteelHookInvokeCallsiteRewriteMode.REDIRECT,
        "missingInvokeValue",
        SteelHook04InvokeCallsiteFixtureClassFactory.ORIGINAL_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.REDIRECTED_METHOD_NAME);
  }

  private SteelHookInvokeCallsiteRewriteRequest wrongDescriptorRequest() {
    return new SteelHookInvokeCallsiteRewriteRequest(
        "target-34.reject.wrong-descriptor",
        "test",
        "Target-33",
        SteelHook04ReturnValueInterceptOfflineProofReportWriter.REPORT_FILE_NAME,
        SteelHook04PrimitiveKind.INVOKE_REDIRECT,
        SteelHookInvokeCallsiteRewriteMode.REDIRECT,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_BINARY_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_CLASS_ENTRY_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INVOKE_METHOD_NAME,
        "()Ljava/lang/String;",
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.ORIGINAL_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        SteelHookInvokeOpcode.INVOKESTATIC,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.REDIRECTED_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        SteelHookInvokeOpcode.INVOKESTATIC,
        false,
        false,
        false);
  }

  private SteelHookInvokeCallsiteRewriteRequest wrongInvokeOwnerRequest() {
    return new SteelHookInvokeCallsiteRewriteRequest(
        "target-34.reject.wrong-invoke-owner",
        "test",
        "Target-33",
        SteelHook04ReturnValueInterceptOfflineProofReportWriter.REPORT_FILE_NAME,
        SteelHook04PrimitiveKind.INVOKE_REDIRECT,
        SteelHookInvokeCallsiteRewriteMode.REDIRECT,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_BINARY_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_CLASS_ENTRY_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INVOKE_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        "wrong/InvokeOwner",
        SteelHook04InvokeCallsiteFixtureClassFactory.ORIGINAL_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        SteelHookInvokeOpcode.INVOKESTATIC,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.REDIRECTED_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        SteelHookInvokeOpcode.INVOKESTATIC,
        false,
        false,
        false);
  }

  private SteelHookInvokeCallsiteRewriteRequest wrongInvokeNameRequest() {
    return request(
        "target-34.reject.wrong-invoke-name",
        SteelHook04PrimitiveKind.INVOKE_REDIRECT,
        SteelHookInvokeCallsiteRewriteMode.REDIRECT,
        SteelHook04InvokeCallsiteFixtureClassFactory.INVOKE_METHOD_NAME,
        "missingOriginalValue",
        SteelHook04InvokeCallsiteFixtureClassFactory.REDIRECTED_METHOD_NAME);
  }

  private SteelHookInvokeCallsiteRewriteRequest wrongInvokeDescriptorRequest() {
    return new SteelHookInvokeCallsiteRewriteRequest(
        "target-34.reject.wrong-invoke-descriptor",
        "test",
        "Target-33",
        SteelHook04ReturnValueInterceptOfflineProofReportWriter.REPORT_FILE_NAME,
        SteelHook04PrimitiveKind.INVOKE_REDIRECT,
        SteelHookInvokeCallsiteRewriteMode.REDIRECT,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_BINARY_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_CLASS_ENTRY_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INVOKE_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.ORIGINAL_METHOD_NAME,
        "()Ljava/lang/String;",
        SteelHookInvokeOpcode.INVOKESTATIC,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.REDIRECTED_METHOD_NAME,
        "()Ljava/lang/String;",
        SteelHookInvokeOpcode.INVOKESTATIC,
        false,
        false,
        false);
  }

  private SteelHookInvokeCallsiteRewriteRequest wrongInvokeOpcodeRequest() {
    return new SteelHookInvokeCallsiteRewriteRequest(
        "target-34.reject.wrong-invoke-opcode",
        "test",
        "Target-33",
        SteelHook04ReturnValueInterceptOfflineProofReportWriter.REPORT_FILE_NAME,
        SteelHook04PrimitiveKind.INVOKE_REDIRECT,
        SteelHookInvokeCallsiteRewriteMode.REDIRECT,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_BINARY_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_CLASS_ENTRY_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INVOKE_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.ORIGINAL_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        SteelHookInvokeOpcode.INVOKEVIRTUAL,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.REDIRECTED_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        SteelHookInvokeOpcode.INVOKEVIRTUAL,
        false,
        false,
        false);
  }

  private SteelHookInvokeCallsiteRewriteRequest replacementDescriptorMismatchRequest() {
    return new SteelHookInvokeCallsiteRewriteRequest(
        "target-34.reject.replacement-descriptor-mismatch",
        "test",
        "Target-33",
        SteelHook04ReturnValueInterceptOfflineProofReportWriter.REPORT_FILE_NAME,
        SteelHook04PrimitiveKind.INVOKE_REDIRECT,
        SteelHookInvokeCallsiteRewriteMode.REDIRECT,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_BINARY_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_CLASS_ENTRY_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INVOKE_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.ORIGINAL_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        SteelHookInvokeOpcode.INVOKESTATIC,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.REDIRECTED_METHOD_NAME,
        "()Ljava/lang/String;",
        SteelHookInvokeOpcode.INVOKESTATIC,
        false,
        false,
        false);
  }

  private SteelHookInvokeCallsiteRewriteRequest replacementOpcodeMismatchRequest() {
    return new SteelHookInvokeCallsiteRewriteRequest(
        "target-34.reject.replacement-opcode-mismatch",
        "test",
        "Target-33",
        SteelHook04ReturnValueInterceptOfflineProofReportWriter.REPORT_FILE_NAME,
        SteelHook04PrimitiveKind.INVOKE_REDIRECT,
        SteelHookInvokeCallsiteRewriteMode.REDIRECT,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_BINARY_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_CLASS_ENTRY_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INVOKE_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.ORIGINAL_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        SteelHookInvokeOpcode.INVOKESTATIC,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.REDIRECTED_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        SteelHookInvokeOpcode.INVOKEVIRTUAL,
        false,
        false,
        false);
  }

  private SteelHook04InvokeCallsiteProofCase successfulCase(
      String id, String label, SteelHookInvokeCallsiteRewriteResult result) {
    boolean wrap = result.request().primitiveKind() == SteelHook04PrimitiveKind.INVOKE_WRAP;
    return new SteelHook04InvokeCallsiteProofCase(
        id,
        label,
        result.request().primitiveKind(),
        result.request().rewriteMode(),
        SteelHook04FixtureShape.INVOKE_SINGLE_STATIC_OR_VIRTUAL_CALLSITE,
        result.request().targetOwnerInternalName(),
        result.request().targetMethodName(),
        result.request().targetDescriptor(),
        result.request().expectedInvokeOwnerInternalName(),
        result.request().expectedInvokeName(),
        result.request().expectedInvokeDescriptor(),
        result.request().expectedInvokeOpcode(),
        result.request().replacementInvokeOwnerInternalName(),
        result.request().replacementInvokeName(),
        result.request().replacementInvokeDescriptor(),
        result.request().replacementInvokeOpcode(),
        result.matchedInvokeOpcode(),
        result.matchedCallsiteCount() == null ? 0 : result.matchedCallsiteCount(),
        result.originalClassSha256(),
        result.transformedClassSha256(),
        result.originalCodeSha256(),
        result.transformedCodeSha256(),
        result.originalCodeLength() == null ? 0 : result.originalCodeLength(),
        result.transformedCodeLength() == null ? 0 : result.transformedCodeLength(),
        result.bytecodeModified(),
        result.transformedClassBytesProduced(),
        result.replacementSummary(),
        wrap ? result.request().expectedInvokeOwnerInternalName() : null,
        wrap ? result.request().expectedInvokeName() : null,
        wrap ? result.request().expectedInvokeDescriptor() : null,
        wrap ? result.request().expectedInvokeOpcode() : null,
        wrap ? result.request().replacementInvokeOwnerInternalName() : null,
        wrap ? result.request().replacementInvokeName() : null,
        wrap ? result.request().replacementInvokeDescriptor() : null,
        wrap ? result.request().replacementInvokeOpcode() : null);
  }

  private GateState gateState(
      SteelHook04PrimitiveBoundaryReport sourceTarget32,
      SteelHook04ReturnValueInterceptOfflineProofReport sourceTarget33,
      SteelHook04PrimitiveCandidate invokeRedirectCandidate,
      SteelHook04PrimitiveCandidate invokeWrapCandidate,
      List<SteelHook04InvokeCallsiteFinding> findings) {
    boolean target32Present = sourceTarget32 != null;
    boolean target33Present = sourceTarget33 != null;
    addFinding(
        findings,
        1,
        "Target-32 primitive boundary report exists.",
        target32Present
            ? SteelHook04InvokeCallsiteFindingStatus.PASS
            : SteelHook04InvokeCallsiteFindingStatus.FAIL,
        true,
        target32Present
            ? "Target-32 report is present."
            : "Target-34 requires the Target-32 report.",
        target32Present
            ? "Input report object provided."
            : "No SteelHook04PrimitiveBoundaryReport was provided.");
    addFinding(
        findings,
        2,
        "Target-33 return-value intercept proof report exists.",
        target33Present
            ? SteelHook04InvokeCallsiteFindingStatus.PASS
            : SteelHook04InvokeCallsiteFindingStatus.FAIL,
        true,
        target33Present
            ? "Target-33 report is present."
            : "Target-34 requires the Target-33 report.",
        target33Present
            ? "Input report object provided."
            : "No SteelHook04ReturnValueInterceptOfflineProofReport was provided.");
    if (!target32Present) {
      return new GateState(
          false,
          SteelHook04InvokeRedirectWrapOfflineProofNextDirection
              .RESTORE_TARGET_32_PRIMITIVE_BOUNDARY);
    }
    if (!target33Present) {
      return new GateState(
          false,
          SteelHook04InvokeRedirectWrapOfflineProofNextDirection
              .RESTORE_TARGET_33_RETURN_VALUE_INTERCEPT_OFFLINE_PROOF);
    }

    boolean target32Contract =
        "Target-32".equals(sourceTarget32.milestoneName())
            && TARGET.equals(sourceTarget32.target())
            && STEELHOOK_VERSION.equals(sourceTarget32.steelHookVersion())
            && sourceTarget32.gatePassed()
            && sourceTarget32.boundaryStatus() == SteelHook04PrimitiveBoundaryStatus.BOUNDARY_READY
            && sourceTarget32.approvedPrimitiveCount() == 3;
    boolean target32SideEffectsSafe =
        !sourceTarget32.runtimeClassLoadingPathEnabled()
            && !sourceTarget32.classLoadingOccurred()
            && !sourceTarget32.serverLaunchOccurred()
            && !sourceTarget32.minecraftMainInvoked()
            && !sourceTarget32.hookInstallationOccurred()
            && !sourceTarget32.runtimeDispatchOccurred()
            && !sourceTarget32.publicApiExposed()
            && !sourceTarget32.javaAgentUsed()
            && !sourceTarget32.mixinUsed()
            && !sourceTarget32.javaModExecutionSandboxed();
    boolean invokeRedirectValid = candidateAllowed(invokeRedirectCandidate);
    boolean invokeWrapValid = candidateAllowed(invokeWrapCandidate);
    addFinding(
        findings,
        3,
        "Target-32 source fields match the required Target-34 handoff contract.",
        target32Contract
            ? SteelHook04InvokeCallsiteFindingStatus.PASS
            : SteelHook04InvokeCallsiteFindingStatus.FAIL,
        true,
        "Target-34 validated the Target-32 milestone, target, version, gate, boundary status, and approved primitive count.",
        "milestone="
            + sourceTarget32.milestoneName()
            + ", target="
            + sourceTarget32.target()
            + ", version="
            + sourceTarget32.steelHookVersion()
            + ", gatePassed="
            + sourceTarget32.gatePassed()
            + ", boundaryStatus="
            + sourceTarget32.boundaryStatus()
            + ", approvedPrimitiveCount="
            + sourceTarget32.approvedPrimitiveCount());
    addFinding(
        findings,
        4,
        "INVOKE_REDIRECT candidate remains internal-only and non-runtime-ready.",
        invokeRedirectValid
            ? SteelHook04InvokeCallsiteFindingStatus.PASS
            : SteelHook04InvokeCallsiteFindingStatus.FAIL,
        true,
        invokeRedirectCandidate == null
            ? "INVOKE_REDIRECT candidate is missing."
            : "INVOKE_REDIRECT candidate matches the Target-32 handoff contract.",
        candidateDetails(invokeRedirectCandidate));
    addFinding(
        findings,
        5,
        "INVOKE_WRAP candidate remains internal-only and non-runtime-ready.",
        invokeWrapValid
            ? SteelHook04InvokeCallsiteFindingStatus.PASS
            : SteelHook04InvokeCallsiteFindingStatus.FAIL,
        true,
        invokeWrapCandidate == null
            ? "INVOKE_WRAP candidate is missing."
            : "INVOKE_WRAP candidate matches the Target-32 handoff contract.",
        candidateDetails(invokeWrapCandidate));
    addFinding(
        findings,
        6,
        "Target-32 preserved zero runtime side effects.",
        target32SideEffectsSafe
            ? SteelHook04InvokeCallsiteFindingStatus.PASS
            : SteelHook04InvokeCallsiteFindingStatus.FAIL,
        true,
        target32SideEffectsSafe
            ? "Target-32 remained offline and side-effect free."
            : "Target-32 side-effect booleans must remain false before Target-34 can proceed.",
        sideEffectDetails(
            sourceTarget32.runtimeClassLoadingPathEnabled(),
            sourceTarget32.classLoadingOccurred(),
            sourceTarget32.serverLaunchOccurred(),
            sourceTarget32.minecraftMainInvoked(),
            sourceTarget32.hookInstallationOccurred(),
            sourceTarget32.runtimeDispatchOccurred(),
            sourceTarget32.publicApiExposed(),
            sourceTarget32.javaAgentUsed(),
            sourceTarget32.mixinUsed(),
            sourceTarget32.javaModExecutionSandboxed()));

    boolean target33Contract =
        "Target-33".equals(sourceTarget33.milestoneName())
            && TARGET.equals(sourceTarget33.target())
            && STEELHOOK_VERSION.equals(sourceTarget33.steelHookVersion())
            && sourceTarget33.proofReady()
            && sourceTarget33.proofStatus()
                == SteelHook04ReturnValueInterceptOfflineProofStatus.PROOF_READY
            && sourceTarget33.primitiveKind() == SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT
            && sourceTarget33.successfulProofCaseCount() >= 4
            && sourceTarget33.sourceRuntimeSideEffectsSafe()
            && sourceTarget33.offlineOnly();
    boolean target33SideEffectsSafe =
        !sourceTarget33.runtimeClassLoadingPathEnabled()
            && !sourceTarget33.classLoadingOccurred()
            && !sourceTarget33.serverLaunchOccurred()
            && !sourceTarget33.minecraftMainInvoked()
            && !sourceTarget33.hookInstallationOccurred()
            && !sourceTarget33.runtimeDispatchOccurred()
            && !sourceTarget33.publicApiExposed()
            && !sourceTarget33.javaAgentUsed()
            && !sourceTarget33.mixinUsed()
            && !sourceTarget33.javaModExecutionSandboxed();
    addFinding(
        findings,
        7,
        "Target-33 source fields match the required Target-34 handoff contract.",
        target33Contract
            ? SteelHook04InvokeCallsiteFindingStatus.PASS
            : SteelHook04InvokeCallsiteFindingStatus.FAIL,
        true,
        "Target-34 validated the Target-33 milestone, target, version, proof readiness, primitive kind, proof case count, and offline-only safety fields.",
        "milestone="
            + sourceTarget33.milestoneName()
            + ", target="
            + sourceTarget33.target()
            + ", version="
            + sourceTarget33.steelHookVersion()
            + ", proofReady="
            + sourceTarget33.proofReady()
            + ", proofStatus="
            + sourceTarget33.proofStatus()
            + ", primitiveKind="
            + sourceTarget33.primitiveKind()
            + ", successfulProofCaseCount="
            + sourceTarget33.successfulProofCaseCount()
            + ", offlineOnly="
            + sourceTarget33.offlineOnly());
    addFinding(
        findings,
        8,
        "Target-33 preserved zero runtime side effects.",
        target33SideEffectsSafe
            ? SteelHook04InvokeCallsiteFindingStatus.PASS
            : SteelHook04InvokeCallsiteFindingStatus.FAIL,
        true,
        target33SideEffectsSafe
            ? "Target-33 remained offline and side-effect free."
            : "Target-33 side-effect booleans must remain false before Target-34 can proceed.",
        sideEffectDetails(
            sourceTarget33.runtimeClassLoadingPathEnabled(),
            sourceTarget33.classLoadingOccurred(),
            sourceTarget33.serverLaunchOccurred(),
            sourceTarget33.minecraftMainInvoked(),
            sourceTarget33.hookInstallationOccurred(),
            sourceTarget33.runtimeDispatchOccurred(),
            sourceTarget33.publicApiExposed(),
            sourceTarget33.javaAgentUsed(),
            sourceTarget33.mixinUsed(),
            sourceTarget33.javaModExecutionSandboxed()));

    boolean ready =
        target32Contract
            && invokeRedirectValid
            && invokeWrapValid
            && target32SideEffectsSafe
            && target33Contract
            && target33SideEffectsSafe;
    SteelHook04InvokeRedirectWrapOfflineProofNextDirection nextDirection =
        target32Contract && invokeRedirectValid && invokeWrapValid && target32SideEffectsSafe
            ? SteelHook04InvokeRedirectWrapOfflineProofNextDirection
                .RESTORE_TARGET_33_RETURN_VALUE_INTERCEPT_OFFLINE_PROOF
            : SteelHook04InvokeRedirectWrapOfflineProofNextDirection
                .RESTORE_TARGET_32_PRIMITIVE_BOUNDARY;
    return new GateState(
        ready,
        ready
            ? SteelHook04InvokeRedirectWrapOfflineProofNextDirection
                .MOVE_TO_TARGET_35_GATED_RUNTIME_CLASS_DEFINITION_PROOF
            : nextDirection);
  }

  private boolean candidateAllowed(SteelHook04PrimitiveCandidate candidate) {
    return candidate != null
        && candidate.internalOnly()
        && !candidate.publicApiExposed()
        && !candidate.runtimeReady()
        && !candidate.gatedRuntimeReady()
        && !candidate.implementedInTarget32();
  }

  private String candidateDetails(SteelHook04PrimitiveCandidate candidate) {
    if (candidate == null) {
      return "No candidate was present.";
    }
    return "internalOnly="
        + candidate.internalOnly()
        + ", publicApiExposed="
        + candidate.publicApiExposed()
        + ", runtimeReady="
        + candidate.runtimeReady()
        + ", gatedRuntimeReady="
        + candidate.gatedRuntimeReady()
        + ", implementedInTarget32="
        + candidate.implementedInTarget32();
  }

  private String sideEffectDetails(
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
    return "runtimeClassLoadingPathEnabled="
        + runtimeClassLoadingPathEnabled
        + ", classLoadingOccurred="
        + classLoadingOccurred
        + ", serverLaunchOccurred="
        + serverLaunchOccurred
        + ", minecraftMainInvoked="
        + minecraftMainInvoked
        + ", hookInstallationOccurred="
        + hookInstallationOccurred
        + ", runtimeDispatchOccurred="
        + runtimeDispatchOccurred
        + ", publicApiExposed="
        + publicApiExposed
        + ", javaAgentUsed="
        + javaAgentUsed
        + ", mixinUsed="
        + mixinUsed
        + ", javaModExecutionSandboxed="
        + javaModExecutionSandboxed;
  }

  private SteelHook04PrimitiveCandidate findCandidate(
      SteelHook04PrimitiveBoundaryReport sourceReport, SteelHook04PrimitiveKind primitiveKind) {
    if (sourceReport == null) {
      return null;
    }
    return sourceReport.candidates().stream()
        .filter(candidate -> candidate.primitiveKind() == primitiveKind)
        .findFirst()
        .orElse(null);
  }

  private SteelHook04InvokeRedirectWrapOfflineProofReport report(
      SteelHook04PrimitiveBoundaryReport sourceTarget32,
      SteelHook04ReturnValueInterceptOfflineProofReport sourceTarget33,
      SteelHook04PrimitiveCandidate invokeRedirectCandidate,
      SteelHook04PrimitiveCandidate invokeWrapCandidate,
      boolean proofReady,
      SteelHook04InvokeRedirectWrapOfflineProofStatus status,
      SteelHook04InvokeRedirectWrapOfflineProofNextDirection nextDirection,
      String nextRecommendedAction,
      int successfulProofCaseCount,
      int rejectionProofCaseCount,
      List<SteelHook04InvokeCallsiteProofCase> proofCases,
      List<SteelHook04InvokeCallsiteFinding> findings) {
    return new SteelHook04InvokeRedirectWrapOfflineProofReport(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        STEELHOOK_VERSION,
        sourceTarget32 == null ? null : sourceTarget32.milestoneName(),
        sourceTarget32 == null || sourceTarget32.boundaryStatus() == null
            ? null
            : sourceTarget32.boundaryStatus().name(),
        sourceTarget32 != null && sourceTarget32.gatePassed(),
        sourceTarget32 == null ? 0 : sourceTarget32.approvedPrimitiveCount(),
        invokeRedirectCandidate != null,
        invokeRedirectCandidate != null && invokeRedirectCandidate.internalOnly(),
        invokeRedirectCandidate != null && invokeRedirectCandidate.publicApiExposed(),
        invokeRedirectCandidate != null && invokeRedirectCandidate.runtimeReady(),
        invokeRedirectCandidate != null && invokeRedirectCandidate.gatedRuntimeReady(),
        invokeRedirectCandidate != null && invokeRedirectCandidate.implementedInTarget32(),
        invokeWrapCandidate != null,
        invokeWrapCandidate != null && invokeWrapCandidate.internalOnly(),
        invokeWrapCandidate != null && invokeWrapCandidate.publicApiExposed(),
        invokeWrapCandidate != null && invokeWrapCandidate.runtimeReady(),
        invokeWrapCandidate != null && invokeWrapCandidate.gatedRuntimeReady(),
        invokeWrapCandidate != null && invokeWrapCandidate.implementedInTarget32(),
        sourceTarget32 != null
            && !sourceTarget32.runtimeClassLoadingPathEnabled()
            && !sourceTarget32.classLoadingOccurred()
            && !sourceTarget32.serverLaunchOccurred()
            && !sourceTarget32.minecraftMainInvoked()
            && !sourceTarget32.hookInstallationOccurred()
            && !sourceTarget32.runtimeDispatchOccurred()
            && !sourceTarget32.publicApiExposed()
            && !sourceTarget32.javaAgentUsed()
            && !sourceTarget32.mixinUsed()
            && !sourceTarget32.javaModExecutionSandboxed(),
        sourceTarget33 == null ? null : sourceTarget33.milestoneName(),
        sourceTarget33 == null || sourceTarget33.proofStatus() == null
            ? null
            : sourceTarget33.proofStatus().name(),
        sourceTarget33 != null && sourceTarget33.proofReady(),
        sourceTarget33 == null ? null : sourceTarget33.primitiveKind(),
        sourceTarget33 == null ? 0 : sourceTarget33.successfulProofCaseCount(),
        sourceTarget33 != null
            && !sourceTarget33.runtimeClassLoadingPathEnabled()
            && !sourceTarget33.classLoadingOccurred()
            && !sourceTarget33.serverLaunchOccurred()
            && !sourceTarget33.minecraftMainInvoked()
            && !sourceTarget33.hookInstallationOccurred()
            && !sourceTarget33.runtimeDispatchOccurred()
            && !sourceTarget33.publicApiExposed()
            && !sourceTarget33.javaAgentUsed()
            && !sourceTarget33.mixinUsed()
            && !sourceTarget33.javaModExecutionSandboxed(),
        proofReady,
        status,
        nextDirection,
        nextRecommendedAction,
        List.of(SteelHook04PrimitiveKind.INVOKE_REDIRECT, SteelHook04PrimitiveKind.INVOKE_WRAP),
        SteelHook04FixtureShape.INVOKE_SINGLE_STATIC_OR_VIRTUAL_CALLSITE,
        rejectionProofCaseCount > 0,
        successfulProofCaseCount,
        rejectionProofCaseCount,
        proofCases,
        findings,
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
        false);
  }

  private void addFinding(
      List<SteelHook04InvokeCallsiteFinding> findings,
      int sequence,
      String checkName,
      SteelHook04InvokeCallsiteFindingStatus status,
      boolean blocking,
      String summary,
      String details) {
    findings.add(
        new SteelHook04InvokeCallsiteFinding(
            "target-34.invoke.finding.%03d".formatted(sequence),
            checkName,
            status,
            blocking,
            summary,
            details));
  }

  private record GateState(
      boolean ready, SteelHook04InvokeRedirectWrapOfflineProofNextDirection nextDirection) {}
}
