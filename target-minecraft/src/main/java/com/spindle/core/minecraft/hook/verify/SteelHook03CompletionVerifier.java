package com.spindle.core.minecraft.hook.verify;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SteelHook03CompletionVerifier {
  private static final Set<String> RAW_BYTE_KEYS =
      Set.of(
          "classBytes",
          "rawClassBytes",
          "originalClassBytes",
          "transformedClassBytes",
          "stackMapTableBytes",
          "rawStackMapTableBytes",
          "bytecodeBytes");
  private static final List<String> COMPLETED_CAPABILITIES =
      List.of(
          "METHOD_ENTRY_STATIC_DISPATCH with bounded StackMapTable first-frame shifting for method-entry insertion at offset 0",
          "METHOD_EXIT_STATIC_DISPATCH before supported normal return opcodes in bounded unframed methods",
          "Isolated gated runtime class definition proof for each approved primitive");
  private static final List<String> UNSUPPORTED_CAPABILITIES =
      List.of(
          "full StackMapTable recomputation",
          "method-entry and method-exit composition in one class",
          "cancellable hooks",
          "return-value interception or replacement",
          "exceptional-exit hooks",
          "branch-offset rewriting",
          "switch-offset rewriting",
          "exception-table rewriting",
          "constructor hooks",
          "field hooks",
          "callsite redirects",
          "hook priorities",
          "conflict resolution",
          "runtime hook installation",
          "Minecraft server launch",
          "public SteelHook API",
          "Java mod execution sandboxing");

  public SteelHook03CompletionReport verify(SteelHook03CompletionInput input) {
    LoadedReport steelHook02 =
        loadRequired(input.steelHook02ReportPath(), "minecraft-steelhook-0-2-report.json");
    LoadedReport target28 =
        loadRequired(
            input.target28ReportPath(), "minecraft-steelhook-0-3-framed-method-foundation.json");
    LoadedReport target29 =
        loadRequired(
            input.target29ReportPath(), "minecraft-steelhook-0-3-method-exit-static-dispatch.json");
    LoadedReport target30 =
        loadRequired(
            input.target30ReportPath(),
            "minecraft-steelhook-0-3-generalized-transformer-gated-runtime-proof.json");

    List<SteelHook03CompletionFinding> findings = new ArrayList<>();
    List<SteelHook03CompletionStageVerification> stages = new ArrayList<>();
    List<SteelHook03CompletionSafetyInvariant> invariants = new ArrayList<>();
    List<SteelHook03CompletionFinding> forbiddenReportChecks = new ArrayList<>();

    SteelHook03CompletionStageVerification stage27 =
        verifySteelHook02Completion(steelHook02.object(), steelHook02.failureReason());
    SteelHook03CompletionStageVerification stage28 =
        verifyTarget28(target28.object(), target28.failureReason());
    SteelHook03CompletionStageVerification stage29 =
        verifyTarget29(target29.object(), target29.failureReason());
    SteelHook03CompletionStageVerification stage30 =
        verifyTarget30(target30.object(), target30.failureReason());
    SteelHook03CompletionStageVerification entryStage =
        verifyEntryPrimitive(target30.object(), target30.failureReason());
    SteelHook03CompletionStageVerification exitStage =
        verifyExitPrimitive(target30.object(), target30.failureReason());

    stages.add(stage27);
    stages.add(stage28);
    stages.add(stage29);
    stages.add(stage30);
    stages.add(entryStage);
    stages.add(exitStage);

    addStageFailures(findings, stages);

    invariants.addAll(rawByteKeyInvariants(target28.object(), "Target-28"));
    invariants.addAll(rawByteKeyInvariants(target29.object(), "Target-29"));
    invariants.addAll(rawByteKeyInvariants(target30.object(), "Target-30"));
    invariants.add(loaderIdInvariant(target30.object()));
    addInvariantFailures(findings, invariants);

    forbiddenReportChecks.add(
        forbiddenReportCheck(
            "minecraft-hook-installation-result.json", input.hookInstallationResultPath()));
    forbiddenReportChecks.add(
        forbiddenReportCheck(
            "minecraft-server-bootstrap-result.json", input.serverBootstrapResultPath()));
    forbiddenReportChecks.add(
        forbiddenReportCheck(
            "minecraft-fixture-transformation-result.json",
            input.fixtureTransformationResultPath()));
    forbiddenReportChecks.add(
        forbiddenReportCheck(
            "minecraft-hook-bootstrap-transformation-result.json",
            input.hookBootstrapTransformationResultPath()));
    addForbiddenFailures(findings, forbiddenReportChecks);

    SteelHook03CompletionReport draft =
        buildReport(
            input,
            steelHook02.object(),
            target28.object(),
            target29.object(),
            target30.object(),
            stages,
            invariants,
            forbiddenReportChecks,
            findings);
    String outputRawKey = firstRawByteKey(new SteelHook03CompletionReportWriter().toJson(draft));
    if (outputRawKey != null) {
      findings.add(
          new SteelHook03CompletionFinding(
              "target-31.raw-byte-output",
              true,
              "Target-31 output contains forbidden key " + outputRawKey + "."));
      draft =
          buildReport(
              input,
              steelHook02.object(),
              target28.object(),
              target29.object(),
              target30.object(),
              stages,
              invariants,
              forbiddenReportChecks,
              findings);
    }
    return draft;
  }

  private SteelHook03CompletionReport buildReport(
      SteelHook03CompletionInput input,
      JsonObject steelHook02,
      JsonObject target28,
      JsonObject target29,
      JsonObject target30,
      List<SteelHook03CompletionStageVerification> stages,
      List<SteelHook03CompletionSafetyInvariant> invariants,
      List<SteelHook03CompletionFinding> forbiddenReportChecks,
      List<SteelHook03CompletionFinding> findings) {
    boolean passed =
        stages.stream().allMatch(SteelHook03CompletionStageVerification::passed)
            && invariants.stream().allMatch(SteelHook03CompletionSafetyInvariant::passed)
            && forbiddenReportChecks.stream().noneMatch(SteelHook03CompletionFinding::fatal)
            && findings.stream().noneMatch(SteelHook03CompletionFinding::fatal);
    String failureReason =
        passed
            ? null
            : "SteelHook 0.3 completion blocked: failed stages="
                + failedStageIds(stages)
                + ", failed invariants="
                + failedInvariantIds(invariants)
                + ", forbidden reports="
                + failedForbiddenIds(forbiddenReportChecks);
    SteelHook03CompletionReport report =
        new SteelHook03CompletionReport(
            1,
            "Target-31",
            "minecraft",
            "0.3",
            passed,
            passed ? SteelHook03CompletionStatus.PASSED : SteelHook03CompletionStatus.FAILED,
            passed
                ? SteelHook03CompletionHandoffStatus.STEELHOOK_0_3_COMPLETE
                : SteelHook03CompletionHandoffStatus.STEELHOOK_0_3_INCOMPLETE,
            stringValue(steelHook02, "milestoneName"),
            stringValue(steelHook02, "status"),
            bool(steelHook02, "completionReady"),
            stringValue(steelHook02, "handoffStatus"),
            stringValue(target28, "milestoneName"),
            stringValue(target28, "status"),
            bool(target28, "framedMethodFoundationReady"),
            stringValue(target29, "milestoneName"),
            stringValue(target29, "status"),
            bool(target29, "methodExitDispatchReady"),
            stringValue(target30, "milestoneName"),
            stringValue(target30, "status"),
            bool(target30, "gatedRuntimeProofReady"),
            COMPLETED_CAPABILITIES,
            UNSUPPORTED_CAPABILITIES,
            stages,
            invariants,
            forbiddenReportChecks,
            integerValue(target30, "runtimeClassLoaderProofCount") == null
                ? 0
                : integerValue(target30, "runtimeClassLoaderProofCount"),
            integerValue(target30, "runtimeClassLoaderSuccessCount") == null
                ? 0
                : integerValue(target30, "runtimeClassLoaderSuccessCount"),
            stagePassed(stages, "target-30-entry-primitive-proof"),
            stagePassed(stages, "target-30-exit-primitive-proof"),
            bool(target30, "runtimeClassLoadingPathEnabled"),
            bool(target30, "classLoadingOccurred"),
            bool(target30, "serverLaunchOccurred"),
            bool(target30, "minecraftMainInvoked"),
            bool(target30, "hookInstallationOccurred"),
            bool(target30, "runtimeDispatchOccurred"),
            bool(target30, "beforeDispatcherInvocationObserved"),
            bool(target30, "afterDispatcherInvocationObserved"),
            bool(target30, "publicApiExposed"),
            bool(target30, "javaAgentUsed"),
            bool(target30, "mixinUsed"),
            bool(target30, "javaModExecutionSandboxed"),
            failureReason,
            findings);
    if (!passed) {
      return report;
    }
    return new SteelHook03CompletionReport(
        report.schema(),
        report.milestoneName(),
        report.target(),
        report.steelHookVersion(),
        true,
        SteelHook03CompletionStatus.PASSED,
        SteelHook03CompletionHandoffStatus.STEELHOOK_0_3_COMPLETE,
        report.sourceSteelHook02Milestone(),
        report.sourceSteelHook02Status(),
        report.sourceSteelHook02CompletionReady(),
        report.sourceSteelHook02HandoffStatus(),
        report.sourceTarget28Milestone(),
        report.sourceTarget28Status(),
        report.sourceTarget28FramedMethodFoundationReady(),
        report.sourceTarget29Milestone(),
        report.sourceTarget29Status(),
        report.sourceTarget29MethodExitDispatchReady(),
        report.sourceTarget30Milestone(),
        report.sourceTarget30Status(),
        report.sourceTarget30GatedRuntimeProofReady(),
        report.completedCapabilities(),
        report.unsupportedCapabilities(),
        report.stageVerifications(),
        report.safetyInvariants(),
        report.forbiddenReportChecks(),
        2,
        2,
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
        false,
        false,
        false,
        false,
        null,
        report.findings());
  }

  private SteelHook03CompletionStageVerification verifySteelHook02Completion(
      JsonObject report, String loadFailureReason) {
    List<String> failures = baseFailures(loadFailureReason, report, "Target-27");
    if (report != null) {
      requireString(report, "status", "passed", failures, "Target-27 status");
      requireBoolean(report, "completionReady", true, failures, "Target-27 completionReady");
      requireString(
          report, "handoffStatus", "steelhook-0-2-complete", failures, "Target-27 handoffStatus");
    }
    return stage(
        "target-27-steelhook-0-2-completion",
        "Target-27",
        "SteelHook 0.2 completion handoff remained valid for SteelHook 0.3.",
        failures);
  }

  private SteelHook03CompletionStageVerification verifyTarget28(
      JsonObject report, String loadFailureReason) {
    List<String> failures = baseFailures(loadFailureReason, report, "Target-28");
    if (report != null) {
      requireInt(report, "schema", 1, failures, "Target-28 schema");
      requireString(report, "target", "minecraft", failures, "Target-28 target");
      requireString(report, "steelHookVersion", "0.3", failures, "Target-28 steelHookVersion");
      requireString(report, "status", "foundation-ready", failures, "Target-28 status");
      requireBoolean(
          report,
          "framedMethodFoundationReady",
          true,
          failures,
          "Target-28 framedMethodFoundationReady");
      requireString(
          report,
          "nextDirection",
          "move-to-target-29-method-exit-static-dispatch",
          failures,
          "Target-28 nextDirection");
      requireBoolean(
          report,
          "sourceSteelHook02CompletionReady",
          true,
          failures,
          "Target-28 sourceSteelHook02CompletionReady");
      requireString(
          report,
          "sourceSteelHook02HandoffStatus",
          "steelhook-0-2-complete",
          failures,
          "Target-28 sourceSteelHook02HandoffStatus");
      requireBoolean(
          report,
          "stackMapTableRewriteSupported",
          true,
          failures,
          "Target-28 stackMapTableRewriteSupported");
      requireBoolean(
          report,
          "stackMapTableRewriteApplied",
          true,
          failures,
          "Target-28 stackMapTableRewriteApplied");
      requireBoolean(
          report,
          "stackMapTableFrameShiftApplied",
          true,
          failures,
          "Target-28 stackMapTableFrameShiftApplied");
      requireInt(report, "insertionOffset", 0, failures, "Target-28 insertionOffset");
      requireInt(
          report, "insertedInstructionLength", 3, failures, "Target-28 insertedInstructionLength");
      Integer before = integerValue(report, "firstFrameOffsetDeltaBefore");
      Integer after = integerValue(report, "firstFrameOffsetDeltaAfter");
      Integer inserted = integerValue(report, "insertedInstructionLength");
      if (before == null || after == null || inserted == null || after != before + inserted) {
        failures.add(
            "Target-28 firstFrameOffsetDeltaAfter must equal firstFrameOffsetDeltaBefore + insertedInstructionLength.");
      }
      requireBoolean(
          report,
          "methodEntryTransformationOccurred",
          true,
          failures,
          "Target-28 methodEntryTransformationOccurred");
      requireBoolean(report, "bytecodeModified", true, failures, "Target-28 bytecodeModified");
      requireBoolean(
          report,
          "transformedClassBytesProduced",
          true,
          failures,
          "Target-28 transformedClassBytesProduced");
      requireBoolean(
          report,
          "runtimeClassLoadingPathEnabled",
          false,
          failures,
          "Target-28 runtimeClassLoadingPathEnabled");
      requireBoolean(
          report, "classLoadingOccurred", false, failures, "Target-28 classLoadingOccurred");
      requireBoolean(
          report, "serverLaunchOccurred", false, failures, "Target-28 serverLaunchOccurred");
      requireBoolean(
          report,
          "hookInstallationOccurred",
          false,
          failures,
          "Target-28 hookInstallationOccurred");
      requireBoolean(
          report, "runtimeDispatchOccurred", false, failures, "Target-28 runtimeDispatchOccurred");
      requireBoolean(report, "publicApiExposed", false, failures, "Target-28 publicApiExposed");
      requireBoolean(report, "javaAgentUsed", false, failures, "Target-28 javaAgentUsed");
      requireBoolean(report, "mixinUsed", false, failures, "Target-28 mixinUsed");
      requireBoolean(
          report,
          "javaModExecutionSandboxed",
          false,
          failures,
          "Target-28 javaModExecutionSandboxed");
      requireNull(report, "failureReason", failures, "Target-28 failureReason");
    }
    return stage(
        "target-28-framed-method-foundation",
        "Target-28",
        "Target-28 framed method foundation remained coherent.",
        failures);
  }

  private SteelHook03CompletionStageVerification verifyTarget29(
      JsonObject report, String loadFailureReason) {
    List<String> failures = baseFailures(loadFailureReason, report, "Target-29");
    if (report != null) {
      requireInt(report, "schema", 1, failures, "Target-29 schema");
      requireString(report, "target", "minecraft", failures, "Target-29 target");
      requireString(report, "steelHookVersion", "0.3", failures, "Target-29 steelHookVersion");
      requireString(
          report,
          "primitiveKind",
          "METHOD_EXIT_STATIC_DISPATCH",
          failures,
          "Target-29 primitiveKind");
      requireString(
          report,
          "sourceTarget28Milestone",
          "Target-28",
          failures,
          "Target-29 sourceTarget28Milestone");
      requireString(
          report,
          "sourceTarget28Status",
          "foundation-ready",
          failures,
          "Target-29 sourceTarget28Status");
      requireBoolean(
          report,
          "sourceTarget28FramedMethodFoundationReady",
          true,
          failures,
          "Target-29 sourceTarget28FramedMethodFoundationReady");
      requireString(
          report,
          "sourceTarget28NextDirection",
          "move-to-target-29-method-exit-static-dispatch",
          failures,
          "Target-29 sourceTarget28NextDirection");
      requireString(report, "status", "method-exit-dispatch-ready", failures, "Target-29 status");
      requireBoolean(
          report, "methodExitDispatchReady", true, failures, "Target-29 methodExitDispatchReady");
      requireString(
          report,
          "nextDirection",
          "move-to-target-30-generalized-transformer-gated-runtime-proof",
          failures,
          "Target-29 nextDirection");
      requireString(
          report,
          "targetOwnerInternalName",
          "net/minecraft/server/Main",
          failures,
          "Target-29 targetOwnerInternalName");
      requireString(report, "targetMethodName", "main", failures, "Target-29 targetMethodName");
      requireString(
          report,
          "targetDescriptor",
          "([Ljava/lang/String;)V",
          failures,
          "Target-29 targetDescriptor");
      requireString(
          report,
          "dispatcherOwnerInternalName",
          "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher",
          failures,
          "Target-29 dispatcherOwnerInternalName");
      requireString(
          report,
          "dispatcherMethodName",
          "afterMinecraftServerMain",
          failures,
          "Target-29 dispatcherMethodName");
      requireString(
          report, "dispatcherDescriptor", "()V", failures, "Target-29 dispatcherDescriptor");
      requireString(report, "opcodeMnemonic", "invokestatic", failures, "Target-29 opcodeMnemonic");
      requireString(report, "opcodeHex", "b8", failures, "Target-29 opcodeHex");
      requireInt(
          report, "insertedInstructionLength", 3, failures, "Target-29 insertedInstructionLength");
      Integer returnCount = integerValue(report, "normalReturnOpcodeCount");
      Integer insertionCount = integerValue(report, "insertionCount");
      if (returnCount == null || returnCount < 1) {
        failures.add("Target-29 normalReturnOpcodeCount must be at least 1.");
      }
      if (returnCount == null || insertionCount == null || !returnCount.equals(insertionCount)) {
        failures.add("Target-29 insertionCount must equal normalReturnOpcodeCount.");
      }
      requireBoolean(
          report,
          "methodExitTransformationOccurred",
          true,
          failures,
          "Target-29 methodExitTransformationOccurred");
      requireBoolean(report, "bytecodeModified", true, failures, "Target-29 bytecodeModified");
      requireBoolean(
          report,
          "transformedClassBytesProduced",
          true,
          failures,
          "Target-29 transformedClassBytesProduced");
      requireBoolean(
          report, "stackMapTablePresent", false, failures, "Target-29 stackMapTablePresent");
      requireBoolean(
          report,
          "stackMapTableRewriteSupported",
          false,
          failures,
          "Target-29 stackMapTableRewriteSupported");
      requireBoolean(
          report,
          "stackMapTableRewriteApplied",
          false,
          failures,
          "Target-29 stackMapTableRewriteApplied");
      requireBoolean(
          report, "exceptionTablePresent", false, failures, "Target-29 exceptionTablePresent");
      requireBoolean(
          report, "branchRewriteRequired", false, failures, "Target-29 branchRewriteRequired");
      requireBoolean(
          report, "switchRewriteRequired", false, failures, "Target-29 switchRewriteRequired");
      requireBoolean(
          report,
          "runtimeClassLoadingPathEnabled",
          false,
          failures,
          "Target-29 runtimeClassLoadingPathEnabled");
      requireBoolean(
          report, "classLoadingOccurred", false, failures, "Target-29 classLoadingOccurred");
      requireBoolean(
          report, "serverLaunchOccurred", false, failures, "Target-29 serverLaunchOccurred");
      requireBoolean(
          report,
          "hookInstallationOccurred",
          false,
          failures,
          "Target-29 hookInstallationOccurred");
      requireBoolean(
          report, "runtimeDispatchOccurred", false, failures, "Target-29 runtimeDispatchOccurred");
      requireBoolean(report, "publicApiExposed", false, failures, "Target-29 publicApiExposed");
      requireBoolean(report, "javaAgentUsed", false, failures, "Target-29 javaAgentUsed");
      requireBoolean(report, "mixinUsed", false, failures, "Target-29 mixinUsed");
      requireBoolean(
          report,
          "javaModExecutionSandboxed",
          false,
          failures,
          "Target-29 javaModExecutionSandboxed");
      requireNull(report, "failureReason", failures, "Target-29 failureReason");
    }
    return stage(
        "target-29-method-exit-static-dispatch",
        "Target-29",
        "Target-29 method-exit static dispatch remained coherent.",
        failures);
  }

  private SteelHook03CompletionStageVerification verifyTarget30(
      JsonObject report, String loadFailureReason) {
    List<String> failures = baseFailures(loadFailureReason, report, "Target-30");
    if (report != null) {
      requireInt(report, "schema", 1, failures, "Target-30 schema");
      requireString(report, "target", "minecraft", failures, "Target-30 target");
      requireString(report, "steelHookVersion", "0.3", failures, "Target-30 steelHookVersion");
      requireString(
          report,
          "sourceTarget29Milestone",
          "Target-29",
          failures,
          "Target-30 sourceTarget29Milestone");
      requireString(
          report,
          "sourceTarget29Status",
          "method-exit-dispatch-ready",
          failures,
          "Target-30 sourceTarget29Status");
      requireBoolean(
          report,
          "sourceTarget29MethodExitDispatchReady",
          true,
          failures,
          "Target-30 sourceTarget29MethodExitDispatchReady");
      requireString(
          report,
          "sourceTarget29NextDirection",
          "move-to-target-30-generalized-transformer-gated-runtime-proof",
          failures,
          "Target-30 sourceTarget29NextDirection");
      requireString(report, "status", "gated-runtime-proof-ready", failures, "Target-30 status");
      requireBoolean(
          report, "gatedRuntimeProofReady", true, failures, "Target-30 gatedRuntimeProofReady");
      requireString(
          report,
          "nextDirection",
          "move-to-target-31-steelhook-0-3-completion",
          failures,
          "Target-30 nextDirection");
      requireInt(
          report,
          "runtimeClassLoaderProofCount",
          2,
          failures,
          "Target-30 runtimeClassLoaderProofCount");
      requireInt(
          report,
          "runtimeClassLoaderSuccessCount",
          2,
          failures,
          "Target-30 runtimeClassLoaderSuccessCount");
      requireObjectPresent(
          report, "entryPrimitiveProof", failures, "Target-30 entryPrimitiveProof");
      requireObjectPresent(report, "exitPrimitiveProof", failures, "Target-30 exitPrimitiveProof");
      requireBoolean(
          report,
          "runtimeClassLoadingPathEnabled",
          true,
          failures,
          "Target-30 runtimeClassLoadingPathEnabled");
      requireBoolean(
          report, "classLoadingOccurred", true, failures, "Target-30 classLoadingOccurred");
      requireBoolean(
          report, "serverLaunchOccurred", false, failures, "Target-30 serverLaunchOccurred");
      requireBoolean(
          report, "minecraftMainInvoked", false, failures, "Target-30 minecraftMainInvoked");
      requireBoolean(
          report,
          "hookInstallationOccurred",
          false,
          failures,
          "Target-30 hookInstallationOccurred");
      requireBoolean(
          report, "runtimeDispatchOccurred", false, failures, "Target-30 runtimeDispatchOccurred");
      requireBoolean(
          report,
          "beforeDispatcherInvocationObserved",
          false,
          failures,
          "Target-30 beforeDispatcherInvocationObserved");
      requireBoolean(
          report,
          "afterDispatcherInvocationObserved",
          false,
          failures,
          "Target-30 afterDispatcherInvocationObserved");
      requireBoolean(report, "publicApiExposed", false, failures, "Target-30 publicApiExposed");
      requireBoolean(report, "javaAgentUsed", false, failures, "Target-30 javaAgentUsed");
      requireBoolean(report, "mixinUsed", false, failures, "Target-30 mixinUsed");
      requireBoolean(
          report,
          "javaModExecutionSandboxed",
          false,
          failures,
          "Target-30 javaModExecutionSandboxed");
      requireNull(report, "failureReason", failures, "Target-30 failureReason");
    }
    return stage(
        "target-30-gated-runtime-proof",
        "Target-30",
        "Target-30 isolated gated runtime proof remained coherent.",
        failures);
  }

  private SteelHook03CompletionStageVerification verifyEntryPrimitive(
      JsonObject report, String loadFailureReason) {
    return verifyPrimitive(
        report == null ? null : object(report, "entryPrimitiveProof"),
        loadFailureReason,
        "target-30-entry-primitive-proof",
        "Target-30-entry",
        "METHOD_ENTRY_STATIC_DISPATCH",
        "Target-28",
        "beforeMinecraftServerMain",
        true,
        false);
  }

  private SteelHook03CompletionStageVerification verifyExitPrimitive(
      JsonObject report, String loadFailureReason) {
    return verifyPrimitive(
        report == null ? null : object(report, "exitPrimitiveProof"),
        loadFailureReason,
        "target-30-exit-primitive-proof",
        "Target-30-exit",
        "METHOD_EXIT_STATIC_DISPATCH",
        "Target-29",
        "afterMinecraftServerMain",
        false,
        true);
  }

  private SteelHook03CompletionStageVerification verifyPrimitive(
      JsonObject proof,
      String loadFailureReason,
      String stageId,
      String milestoneName,
      String expectedPrimitiveKind,
      String expectedSourceMilestone,
      String expectedDispatcherMethod,
      boolean stackMapRewriteSupported,
      boolean methodExitTransformationOccurred) {
    List<String> failures = new ArrayList<>();
    if (loadFailureReason != null) {
      failures.add(loadFailureReason);
      return stage(stageId, milestoneName, "Primitive proof could not be loaded.", failures);
    }
    if (proof == null) {
      failures.add(milestoneName + " primitive proof is missing.");
      return stage(stageId, milestoneName, "Primitive proof must be present.", failures);
    }
    requireString(
        proof, "primitiveKind", expectedPrimitiveKind, failures, milestoneName + " primitiveKind");
    requireString(
        proof,
        "sourceMilestone",
        expectedSourceMilestone,
        failures,
        milestoneName + " sourceMilestone");
    requireString(
        proof, "status", "gated-runtime-proof-ready", failures, milestoneName + " status");
    requireString(
        proof,
        "targetBinaryName",
        "net.minecraft.server.Main",
        failures,
        milestoneName + " targetBinaryName");
    requireString(
        proof,
        "targetInternalName",
        "net/minecraft/server/Main",
        failures,
        milestoneName + " targetInternalName");
    requireString(
        proof,
        "targetClassEntryName",
        "net/minecraft/server/Main.class",
        failures,
        milestoneName + " targetClassEntryName");
    requireString(proof, "targetMethodName", "main", failures, milestoneName + " targetMethodName");
    requireString(
        proof,
        "targetDescriptor",
        "([Ljava/lang/String;)V",
        failures,
        milestoneName + " targetDescriptor");
    requireString(
        proof,
        "dispatcherOwnerInternalName",
        "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher",
        failures,
        milestoneName + " dispatcherOwnerInternalName");
    requireString(
        proof,
        "dispatcherMethodName",
        expectedDispatcherMethod,
        failures,
        milestoneName + " dispatcherMethodName");
    requireString(
        proof, "dispatcherDescriptor", "()V", failures, milestoneName + " dispatcherDescriptor");
    requireBoolean(
        proof,
        "runtimeClassLoadingAttempted",
        true,
        failures,
        milestoneName + " runtimeClassLoadingAttempted");
    requireBoolean(
        proof,
        "runtimeClassLoadingSucceeded",
        true,
        failures,
        milestoneName + " runtimeClassLoadingSucceeded");
    requireBoolean(
        proof, "classLoadingOccurred", true, failures, milestoneName + " classLoadingOccurred");
    requireBoolean(
        proof, "targetClassDefined", true, failures, milestoneName + " targetClassDefined");
    requireString(
        proof,
        "definedClassName",
        "net.minecraft.server.Main",
        failures,
        milestoneName + " definedClassName");
    requireBoolean(
        proof,
        "definedBySteelHookRuntimeClassLoader",
        true,
        failures,
        milestoneName + " definedBySteelHookRuntimeClassLoader");
    requireBoolean(proof, "classInitialized", false, failures, milestoneName + " classInitialized");
    requireBoolean(
        proof, "minecraftMainInvoked", false, failures, milestoneName + " minecraftMainInvoked");
    requireBoolean(
        proof, "serverLaunchOccurred", false, failures, milestoneName + " serverLaunchOccurred");
    requireBoolean(
        proof,
        "hookInstallationOccurred",
        false,
        failures,
        milestoneName + " hookInstallationOccurred");
    requireBoolean(
        proof,
        "runtimeDispatchOccurred",
        false,
        failures,
        milestoneName + " runtimeDispatchOccurred");
    requireBoolean(
        proof,
        "dispatcherInvocationObserved",
        false,
        failures,
        milestoneName + " dispatcherInvocationObserved");
    Integer before = integerValue(proof, "dispatcherInvocationCountBefore");
    Integer after = integerValue(proof, "dispatcherInvocationCountAfter");
    if (before == null || after == null || !before.equals(after)) {
      failures.add(
          milestoneName
              + " dispatcherInvocationCountBefore must equal dispatcherInvocationCountAfter.");
    }
    if (after == null || after != 0) {
      failures.add(milestoneName + " dispatcherInvocationCountAfter must be 0.");
    }
    requireBoolean(
        proof,
        "stackMapTableRewriteSupported",
        stackMapRewriteSupported,
        failures,
        milestoneName + " stackMapTableRewriteSupported");
    requireBoolean(
        proof,
        "stackMapTableRewriteApplied",
        stackMapRewriteSupported,
        failures,
        milestoneName + " stackMapTableRewriteApplied");
    requireBoolean(
        proof,
        "methodEntryTransformationOccurred",
        !methodExitTransformationOccurred,
        failures,
        milestoneName + " methodEntryTransformationOccurred");
    requireBoolean(
        proof,
        "methodExitTransformationOccurred",
        methodExitTransformationOccurred,
        failures,
        milestoneName + " methodExitTransformationOccurred");
    requireBoolean(proof, "bytecodeModified", true, failures, milestoneName + " bytecodeModified");
    requireBoolean(
        proof,
        "transformedClassBytesProduced",
        true,
        failures,
        milestoneName + " transformedClassBytesProduced");
    requireNonBlank(proof, "originalClassSha256", failures, milestoneName + " originalClassSha256");
    requireNonBlank(
        proof, "transformedClassSha256", failures, milestoneName + " transformedClassSha256");
    requireNonBlank(proof, "originalCodeSha256", failures, milestoneName + " originalCodeSha256");
    requireNonBlank(
        proof, "transformedCodeSha256", failures, milestoneName + " transformedCodeSha256");
    Integer originalCodeLength = integerValue(proof, "originalCodeLength");
    Integer transformedCodeLength = integerValue(proof, "transformedCodeLength");
    Integer constantPoolBefore = integerValue(proof, "constantPoolCountBefore");
    Integer constantPoolAfter = integerValue(proof, "constantPoolCountAfter");
    if (originalCodeLength == null || originalCodeLength <= 0) {
      failures.add(milestoneName + " originalCodeLength must be greater than 0.");
    }
    if (originalCodeLength == null
        || transformedCodeLength == null
        || transformedCodeLength <= originalCodeLength) {
      failures.add(
          milestoneName + " transformedCodeLength must be greater than originalCodeLength.");
    }
    if (constantPoolBefore == null
        || constantPoolAfter == null
        || constantPoolAfter < constantPoolBefore) {
      failures.add(milestoneName + " constantPoolCountAfter must be >= constantPoolCountBefore.");
    }
    requireNull(proof, "failureReason", failures, milestoneName + " failureReason");
    return stage(stageId, milestoneName, "Primitive proof remained coherent.", failures);
  }

  private List<SteelHook03CompletionSafetyInvariant> rawByteKeyInvariants(
      JsonObject report, String reportName) {
    List<SteelHook03CompletionSafetyInvariant> invariants = new ArrayList<>();
    String offendingKey = firstRawByteKey(report);
    invariants.add(
        new SteelHook03CompletionSafetyInvariant(
            reportName.toLowerCase().replace(' ', '-') + ".no-raw-byte-payload-keys",
            "absent",
            offendingKey == null ? "absent" : offendingKey,
            offendingKey == null,
            offendingKey == null
                ? null
                : reportName + " contains forbidden raw byte payload key " + offendingKey + "."));
    return invariants;
  }

  private SteelHook03CompletionSafetyInvariant loaderIdInvariant(JsonObject report) {
    JsonObject entry = object(report, "entryPrimitiveProof");
    JsonObject exit = object(report, "exitPrimitiveProof");
    String entryId = stringValue(entry, "runtimeLoaderId");
    String exitId = stringValue(exit, "runtimeLoaderId");
    boolean passed = entryId != null && exitId != null && !entryId.equals(exitId);
    return new SteelHook03CompletionSafetyInvariant(
        "target-30.separate-runtime-loader-ids",
        "different",
        entryId + "/" + exitId,
        passed,
        passed
            ? null
            : "Target-30 entryPrimitiveProof.runtimeLoaderId must differ from exitPrimitiveProof.runtimeLoaderId.");
  }

  private SteelHook03CompletionFinding forbiddenReportCheck(String id, Path path) {
    boolean present = path != null && Files.exists(path);
    return new SteelHook03CompletionFinding(
        id, present, present ? id + " must not be present for Target-31." : id + " absent.");
  }

  private void addStageFailures(
      List<SteelHook03CompletionFinding> findings,
      List<SteelHook03CompletionStageVerification> stages) {
    for (SteelHook03CompletionStageVerification stage : stages) {
      if (!stage.passed()) {
        findings.add(
            new SteelHook03CompletionFinding(
                stage.stageId(),
                true,
                stage.failureReason() == null ? "failed" : stage.failureReason()));
      }
    }
  }

  private void addInvariantFailures(
      List<SteelHook03CompletionFinding> findings,
      List<SteelHook03CompletionSafetyInvariant> invariants) {
    for (SteelHook03CompletionSafetyInvariant invariant : invariants) {
      if (!invariant.passed()) {
        findings.add(
            new SteelHook03CompletionFinding(
                invariant.id(),
                true,
                invariant.failureReason() == null ? "failed" : invariant.failureReason()));
      }
    }
  }

  private void addForbiddenFailures(
      List<SteelHook03CompletionFinding> findings, List<SteelHook03CompletionFinding> checks) {
    for (SteelHook03CompletionFinding check : checks) {
      if (check.fatal()) {
        findings.add(check);
      }
    }
  }

  private boolean stagePassed(List<SteelHook03CompletionStageVerification> stages, String stageId) {
    return stages.stream()
        .filter(stage -> stageId.equals(stage.stageId()))
        .findFirst()
        .map(SteelHook03CompletionStageVerification::passed)
        .orElse(false);
  }

  private List<String> baseFailures(JsonObject report, String expectedMilestone) {
    List<String> failures = new ArrayList<>();
    requireString(
        report, "milestoneName", expectedMilestone, failures, expectedMilestone + " milestoneName");
    return failures;
  }

  private List<String> baseFailures(
      String loadFailureReason, JsonObject report, String expectedMilestone) {
    List<String> failures = new ArrayList<>();
    if (loadFailureReason != null) {
      failures.add(loadFailureReason);
      return failures;
    }
    if (report == null) {
      failures.add("Required report " + expectedMilestone + " is missing.");
      return failures;
    }
    return baseFailures(report, expectedMilestone);
  }

  private LoadedReport loadRequired(Path path, String fileName) {
    if (!Files.isRegularFile(path)) {
      return new LoadedReport(null, "Required report " + fileName + " is missing.");
    }
    try {
      JsonElement element = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8));
      if (!element.isJsonObject()) {
        return new LoadedReport(null, "Required report " + fileName + " must be a JSON object.");
      }
      return new LoadedReport(element.getAsJsonObject(), null);
    } catch (IOException | JsonParseException exception) {
      return new LoadedReport(null, "Required report " + fileName + " could not be parsed.");
    }
  }

  private SteelHook03CompletionStageVerification stage(
      String stageId, String milestoneName, String summary, List<String> failures) {
    return new SteelHook03CompletionStageVerification(
        stageId,
        milestoneName,
        summary,
        failures.isEmpty(),
        failures.isEmpty() ? null : String.join(" ", failures));
  }

  private String failedStageIds(List<SteelHook03CompletionStageVerification> stages) {
    return stages.stream()
        .filter(stage -> !stage.passed())
        .map(SteelHook03CompletionStageVerification::stageId)
        .reduce((left, right) -> left + "," + right)
        .orElse("none");
  }

  private String failedInvariantIds(List<SteelHook03CompletionSafetyInvariant> invariants) {
    return invariants.stream()
        .filter(invariant -> !invariant.passed())
        .map(SteelHook03CompletionSafetyInvariant::id)
        .reduce((left, right) -> left + "," + right)
        .orElse("none");
  }

  private String failedForbiddenIds(List<SteelHook03CompletionFinding> findings) {
    return findings.stream()
        .filter(SteelHook03CompletionFinding::fatal)
        .map(SteelHook03CompletionFinding::id)
        .reduce((left, right) -> left + "," + right)
        .orElse("none");
  }

  private String firstRawByteKey(JsonObject object) {
    if (object == null) {
      return null;
    }
    for (String key : object.keySet()) {
      if (RAW_BYTE_KEYS.contains(key)) {
        return key;
      }
      JsonElement element = object.get(key);
      if (element != null && element.isJsonObject()) {
        String nested = firstRawByteKey(element.getAsJsonObject());
        if (nested != null) {
          return nested;
        }
      }
      if (element != null && element.isJsonArray()) {
        for (JsonElement child : element.getAsJsonArray()) {
          if (child.isJsonObject()) {
            String nested = firstRawByteKey(child.getAsJsonObject());
            if (nested != null) {
              return nested;
            }
          }
        }
      }
    }
    return null;
  }

  private JsonObject object(JsonObject object, String field) {
    if (object == null || !object.has(field) || !object.get(field).isJsonObject()) {
      return null;
    }
    return object.getAsJsonObject(field);
  }

  private String stringValue(JsonObject object, String field) {
    if (object == null || !object.has(field) || object.get(field).isJsonNull()) {
      return null;
    }
    try {
      return object.get(field).getAsString();
    } catch (RuntimeException exception) {
      return null;
    }
  }

  private Integer integerValue(JsonObject object, String field) {
    if (object == null || !object.has(field) || object.get(field).isJsonNull()) {
      return null;
    }
    try {
      return object.get(field).getAsInt();
    } catch (RuntimeException exception) {
      return null;
    }
  }

  private boolean bool(JsonObject object, String field) {
    if (object == null || !object.has(field) || object.get(field).isJsonNull()) {
      return false;
    }
    try {
      return object.get(field).getAsBoolean();
    } catch (RuntimeException exception) {
      return false;
    }
  }

  private void requireString(
      JsonObject object, String field, String expected, List<String> failures, String label) {
    String actual = stringValue(object, field);
    if (!expected.equals(actual)) {
      failures.add(label + " expected " + expected + " but was " + actual + ".");
    }
  }

  private void requireBoolean(
      JsonObject object, String field, boolean expected, List<String> failures, String label) {
    if (!object.has(field) || object.get(field).isJsonNull()) {
      failures.add(label + " expected " + expected + " but was missing.");
      return;
    }
    boolean actual = bool(object, field);
    if (actual != expected) {
      failures.add(label + " expected " + expected + " but was " + actual + ".");
    }
  }

  private void requireInt(
      JsonObject object, String field, int expected, List<String> failures, String label) {
    Integer actual = integerValue(object, field);
    if (actual == null || actual != expected) {
      failures.add(label + " expected " + expected + " but was " + actual + ".");
    }
  }

  private void requireNull(JsonObject object, String field, List<String> failures, String label) {
    if (!object.has(field)) {
      return;
    }
    if (!object.get(field).isJsonNull()) {
      failures.add(label + " expected null but was " + object.get(field) + ".");
    }
  }

  private void requireObjectPresent(
      JsonObject object, String field, List<String> failures, String label) {
    if (this.object(object, field) == null) {
      failures.add(label + " must be present.");
    }
  }

  private void requireNonBlank(
      JsonObject object, String field, List<String> failures, String label) {
    String actual = stringValue(object, field);
    if (actual == null || actual.isBlank()) {
      failures.add(label + " must be present.");
    }
  }

  private record LoadedReport(JsonObject object, String failureReason) {}
}
