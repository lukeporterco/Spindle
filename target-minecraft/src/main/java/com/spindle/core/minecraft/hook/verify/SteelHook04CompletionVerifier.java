package com.spindle.core.minecraft.hook.verify;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SteelHook04CompletionVerifier {
  private static final Set<String> RAW_BYTE_KEYS =
      Set.of(
          "classBytes",
          "rawClassBytes",
          "originalClassBytes",
          "transformedClassBytes",
          "methodCodeBytes",
          "rawMethodCode",
          "bytecodeBytes",
          "stackMapTableBytes",
          "rawStackMapTableBytes",
          "codeBytes",
          "bytes",
          "payload");
  private static final List<String> COMPLETED_PRIMITIVE_KINDS =
      List.of("RETURN_VALUE_INTERCEPT", "INVOKE_REDIRECT", "INVOKE_WRAP");
  private static final List<String> COMPLETED_CAPABILITIES =
      List.of(
          "bounded return-value intercept offline observation and replacement evidence",
          "bounded invoke redirect offline callsite rewrite evidence",
          "bounded invoke wrap offline callsite rewrite evidence",
          "isolated gated runtime class-definition proof for all three approved 0.4 primitives",
          "unsupported primitive plan rejection before class definition");
  private static final List<String> UNSUPPORTED_CAPABILITIES =
      List.of(
          "public SteelHook API",
          "Minecraft Modding API exposure",
          "arbitrary bytecode rewriting",
          "arbitrary invoke rewriting",
          "arbitrary return rewriting",
          "constructor invocation support",
          "invokespecial support",
          "invokedynamic support",
          "StackMapTable recomputation",
          "branch rewriting",
          "switch rewriting",
          "exception-table rewriting",
          "hook installation",
          "Minecraft server launch",
          "transformed method execution",
          "wrapper execution",
          "dispatcher execution",
          "Java agent behavior",
          "Mixin behavior",
          "Java mod execution sandboxing");
  private static final List<String> TARGET_33_EXPECTED_CASE_LABELS =
      List.of(
          "primitive observe-only",
          "primitive replacement",
          "reference observe-only",
          "reference replacement");

  public SteelHook04CompletionReport verify(SteelHook04CompletionInput input) {
    LoadedReport target32 =
        loadRequired(input.target32ReportPath(), "minecraft-steelhook-0-4-primitive-boundary.json");
    LoadedReport target33 =
        loadRequired(
            input.target33ReportPath(),
            "minecraft-steelhook-0-4-return-value-intercept-offline-proof.json");
    LoadedReport target34 =
        loadRequired(
            input.target34ReportPath(),
            "minecraft-steelhook-0-4-invoke-redirect-wrap-offline-proof.json");
    LoadedReport target35 =
        loadRequired(
            input.target35ReportPath(), "minecraft-steelhook-0-4-gated-runtime-proof.json");

    List<SteelHook04CompletionFinding> findings = new ArrayList<>();
    List<SteelHook04CompletionStageVerification> stages = new ArrayList<>();
    List<SteelHook04CompletionSafetyInvariant> invariants = new ArrayList<>();
    List<SteelHook04CompletionFinding> forbiddenReportChecks = new ArrayList<>();

    stages.add(verifyTarget32(target32.object(), target32.failureReason()));
    stages.add(verifyTarget33(target33.object(), target33.failureReason()));
    stages.add(verifyTarget34(target34.object(), target34.failureReason()));
    stages.add(verifyTarget35(target35.object(), target35.failureReason()));
    stages.add(
        verifyReturnValueInterceptEvidenceChain(target35.object(), target35.failureReason()));
    stages.add(verifyInvokeRedirectEvidenceChain(target35.object(), target35.failureReason()));
    stages.add(verifyInvokeWrapEvidenceChain(target35.object(), target35.failureReason()));
    addStageFailures(findings, stages);

    invariants.add(rawByteKeyInvariant(target32.object(), "target-32", "Target-32"));
    invariants.add(rawByteKeyInvariant(target33.object(), "target-33", "Target-33"));
    invariants.add(rawByteKeyInvariant(target34.object(), "target-34", "Target-34"));
    invariants.add(rawByteKeyInvariant(target35.object(), "target-35", "Target-35"));
    invariants.add(
        unsupportedPrimitiveLeakageInvariant(
            target32.object(), target34.object(), target35.object()));
    invariants.add(noExecutionBeyondClassDefinitionInvariant(target35.object()));
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

    SteelHook04CompletionReport draft =
        buildReport(
            target32.object(),
            target33.object(),
            target34.object(),
            target35.object(),
            stages,
            invariants,
            forbiddenReportChecks,
            findings);
    String outputRawByteKey =
        firstRawByteKey(new SteelHook04CompletionReportWriter().toJson(draft));
    if (outputRawByteKey != null) {
      findings.add(
          new SteelHook04CompletionFinding(
              "target-36.raw-byte-output",
              true,
              "Target-36 output contains forbidden key " + outputRawByteKey + "."));
      draft =
          buildReport(
              target32.object(),
              target33.object(),
              target34.object(),
              target35.object(),
              stages,
              invariants,
              forbiddenReportChecks,
              findings);
    }
    return draft;
  }

  private SteelHook04CompletionReport buildReport(
      JsonObject target32,
      JsonObject target33,
      JsonObject target34,
      JsonObject target35,
      List<SteelHook04CompletionStageVerification> stages,
      List<SteelHook04CompletionSafetyInvariant> invariants,
      List<SteelHook04CompletionFinding> forbiddenReportChecks,
      List<SteelHook04CompletionFinding> findings) {
    boolean passed =
        stages.stream().allMatch(SteelHook04CompletionStageVerification::passed)
            && invariants.stream().allMatch(SteelHook04CompletionSafetyInvariant::passed)
            && forbiddenReportChecks.stream().noneMatch(SteelHook04CompletionFinding::fatal)
            && findings.stream().noneMatch(SteelHook04CompletionFinding::fatal);
    SteelHook04CompletionNextDirection nextDirection = nextDirection(stages, invariants);
    String failureReason =
        passed
            ? null
            : "SteelHook 0.4 completion blocked: failed stages="
                + failedStageIds(stages)
                + ", failed invariants="
                + failedInvariantIds(invariants)
                + ", forbidden reports="
                + failedForbiddenIds(forbiddenReportChecks);
    return new SteelHook04CompletionReport(
        1,
        "Target-36",
        "minecraft",
        "0.4",
        passed,
        passed ? SteelHook04CompletionStatus.PASSED : SteelHook04CompletionStatus.FAILED,
        passed
            ? SteelHook04CompletionHandoffStatus.STEELHOOK_0_4_COMPLETE
            : SteelHook04CompletionHandoffStatus.STEELHOOK_0_4_INCOMPLETE,
        nextDirection,
        nextRecommendedAction(nextDirection),
        stringValue(target32, "milestoneName"),
        stringValue(target32, "boundaryStatus"),
        bool(target32, "gatePassed"),
        integerValue(target32, "approvedPrimitiveCount") == null
            ? 0
            : integerValue(target32, "approvedPrimitiveCount"),
        stringValue(target33, "milestoneName"),
        stringValue(target33, "proofStatus"),
        bool(target33, "proofReady"),
        stringValue(target33, "primitiveKind"),
        integerValue(target33, "successfulProofCaseCount") == null
            ? 0
            : integerValue(target33, "successfulProofCaseCount"),
        stringValue(target34, "milestoneName"),
        stringValue(target34, "proofStatus"),
        bool(target34, "proofReady"),
        integerValue(target34, "successfulProofCaseCount") == null
            ? 0
            : integerValue(target34, "successfulProofCaseCount"),
        stringValue(target35, "milestoneName"),
        stringValue(target35, "status"),
        bool(target35, "gatedRuntimeProofReady"),
        integerValue(target35, "runtimeClassLoaderProofCount") == null
            ? 0
            : integerValue(target35, "runtimeClassLoaderProofCount"),
        integerValue(target35, "runtimeClassLoaderSuccessCount") == null
            ? 0
            : integerValue(target35, "runtimeClassLoaderSuccessCount"),
        COMPLETED_PRIMITIVE_KINDS,
        COMPLETED_CAPABILITIES,
        UNSUPPORTED_CAPABILITIES,
        stages,
        invariants,
        forbiddenReportChecks,
        stagePassed(stages, "target-35-return-value-intercept-evidence-chain"),
        stagePassed(stages, "target-35-invoke-redirect-evidence-chain"),
        stagePassed(stages, "target-35-invoke-wrap-evidence-chain"),
        stagePassed(stages, "target-33-return-value-intercept-offline-proof")
            && stagePassed(stages, "target-34-invoke-redirect-wrap-offline-proof"),
        stagePassed(stages, "target-35-gated-runtime-proof"),
        bool(target35, "unsupportedPrimitivePlanRejectedBeforeClassDefinition")
            && !bool(target35, "unsupportedPrimitivePlanClassDefinitionAttempted"),
        invariants.stream()
            .filter(invariant -> invariant.id().endsWith(".no-raw-byte-payload-keys"))
            .allMatch(SteelHook04CompletionSafetyInvariant::passed),
        invariants.stream()
            .filter(
                invariant ->
                    "target-36.unsupported-primitive-leakage-absent".equals(invariant.id()))
            .findFirst()
            .map(SteelHook04CompletionSafetyInvariant::passed)
            .orElse(false),
        bool(target35, "runtimeClassLoadingPathEnabled"),
        bool(target35, "classLoadingOccurred"),
        bool(target35, "targetClassDefinitionOccurred"),
        bool(target35, "classInitialized"),
        bool(target35, "targetMethodInvoked"),
        bool(target35, "wrapperExecuted"),
        bool(target35, "serverLaunchOccurred"),
        bool(target35, "minecraftMainInvoked"),
        bool(target35, "hookInstallationOccurred"),
        bool(target35, "runtimeDispatchOccurred"),
        bool(target35, "publicApiExposed"),
        bool(target35, "javaAgentUsed"),
        bool(target35, "mixinUsed"),
        bool(target35, "javaModExecutionSandboxed"),
        failureReason,
        findings);
  }

  private SteelHook04CompletionStageVerification verifyTarget32(
      JsonObject report, String loadFailureReason) {
    List<String> failures = baseFailures(loadFailureReason, report, "Target-32");
    if (report != null) {
      requireInt(report, "schema", 1, failures, "Target-32 schema");
      requireString(report, "target", "minecraft", failures, "Target-32 target");
      requireString(report, "steelHookVersion", "0.4", failures, "Target-32 steelHookVersion");
      requireBoolean(report, "gatePassed", true, failures, "Target-32 gatePassed");
      requireStatusString(
          report, "boundaryStatus", "boundary-ready", failures, "Target-32 boundaryStatus");
      requireInt(report, "approvedPrimitiveCount", 3, failures, "Target-32 approvedPrimitiveCount");
      requirePrimitiveSet(report, "candidates", COMPLETED_PRIMITIVE_KINDS, failures, "Target-32");
      requireAllCandidateBooleans(report, failures);
      requireBoolean(report, "analysisOnly", true, failures, "Target-32 analysisOnly");
      requireBoolean(report, "bytecodeModified", false, failures, "Target-32 bytecodeModified");
      requireBoolean(
          report,
          "transformedClassBytesProduced",
          false,
          failures,
          "Target-32 transformedClassBytesProduced");
      requireBoolean(
          report,
          "runtimeClassLoadingPathEnabled",
          false,
          failures,
          "Target-32 runtimeClassLoadingPathEnabled");
      requireBoolean(
          report, "classLoadingOccurred", false, failures, "Target-32 classLoadingOccurred");
      requireBoolean(
          report, "serverLaunchOccurred", false, failures, "Target-32 serverLaunchOccurred");
      requireBoolean(
          report, "minecraftMainInvoked", false, failures, "Target-32 minecraftMainInvoked");
      requireBoolean(
          report,
          "hookInstallationOccurred",
          false,
          failures,
          "Target-32 hookInstallationOccurred");
      requireBoolean(
          report, "runtimeDispatchOccurred", false, failures, "Target-32 runtimeDispatchOccurred");
      requireBoolean(report, "publicApiExposed", false, failures, "Target-32 publicApiExposed");
      requireBoolean(report, "javaAgentUsed", false, failures, "Target-32 javaAgentUsed");
      requireBoolean(report, "mixinUsed", false, failures, "Target-32 mixinUsed");
      requireBoolean(
          report,
          "javaModExecutionSandboxed",
          false,
          failures,
          "Target-32 javaModExecutionSandboxed");
    }
    return stage(
        "target-32-primitive-boundary",
        "Target-32",
        "Target-32 primitive boundary remained coherent.",
        failures);
  }

  private void requireAllCandidateBooleans(JsonObject report, List<String> failures) {
    JsonArray candidates = array(report, "candidates");
    if (candidates == null || candidates.size() != 3) {
      return;
    }
    for (JsonElement candidateElement : candidates) {
      JsonObject candidate =
          candidateElement.isJsonObject() ? candidateElement.getAsJsonObject() : null;
      if (candidate == null) {
        failures.add("Target-32 candidates must contain only objects.");
        continue;
      }
      String primitiveKind = stringValue(candidate, "primitiveKind");
      requireBoolean(candidate, "internalOnly", true, failures, primitiveKind + " internalOnly");
      requireBoolean(
          candidate, "publicApiExposed", false, failures, primitiveKind + " publicApiExposed");
      requireBoolean(candidate, "runtimeReady", false, failures, primitiveKind + " runtimeReady");
      requireBoolean(
          candidate, "gatedRuntimeReady", false, failures, primitiveKind + " gatedRuntimeReady");
      requireBoolean(
          candidate,
          "implementedInTarget32",
          false,
          failures,
          primitiveKind + " implementedInTarget32");
    }
  }

  private SteelHook04CompletionStageVerification verifyTarget33(
      JsonObject report, String loadFailureReason) {
    List<String> failures = baseFailures(loadFailureReason, report, "Target-33");
    if (report != null) {
      requireInt(report, "schema", 1, failures, "Target-33 schema");
      requireString(report, "target", "minecraft", failures, "Target-33 target");
      requireString(report, "steelHookVersion", "0.4", failures, "Target-33 steelHookVersion");
      requireString(
          report,
          "sourceTarget32Milestone",
          "Target-32",
          failures,
          "Target-33 sourceTarget32Milestone");
      requireStatusString(
          report,
          "sourceTarget32BoundaryStatus",
          "boundary-ready",
          failures,
          "Target-33 sourceTarget32BoundaryStatus");
      requireBoolean(
          report, "sourceTarget32GatePassed", true, failures, "Target-33 sourceTarget32GatePassed");
      requireBoolean(report, "proofReady", true, failures, "Target-33 proofReady");
      requireStatusString(report, "proofStatus", "proof-ready", failures, "Target-33 proofStatus");
      requireString(
          report, "primitiveKind", "RETURN_VALUE_INTERCEPT", failures, "Target-33 primitiveKind");
      Integer successfulProofCaseCount = integerValue(report, "successfulProofCaseCount");
      if (successfulProofCaseCount == null || successfulProofCaseCount < 4) {
        failures.add("Target-33 successfulProofCaseCount must be at least 4.");
      }
      requireTarget33Cases(report, failures);
      requireBoolean(
          report,
          "unsupportedFixtureShapesRejected",
          true,
          failures,
          "Target-33 unsupportedFixtureShapesRejected");
      Integer rejectionProofCaseCount = integerValue(report, "rejectionProofCaseCount");
      if (rejectionProofCaseCount == null || rejectionProofCaseCount <= 0) {
        failures.add("Target-33 rejectionProofCaseCount must be greater than 0.");
      }
      requireBoolean(report, "offlineOnly", true, failures, "Target-33 offlineOnly");
      requireBoolean(
          report,
          "runtimeClassLoadingPathEnabled",
          false,
          failures,
          "Target-33 runtimeClassLoadingPathEnabled");
      requireBoolean(
          report, "classLoadingOccurred", false, failures, "Target-33 classLoadingOccurred");
      requireBoolean(
          report, "serverLaunchOccurred", false, failures, "Target-33 serverLaunchOccurred");
      requireBoolean(
          report, "minecraftMainInvoked", false, failures, "Target-33 minecraftMainInvoked");
      requireBoolean(
          report,
          "hookInstallationOccurred",
          false,
          failures,
          "Target-33 hookInstallationOccurred");
      requireBoolean(
          report, "runtimeDispatchOccurred", false, failures, "Target-33 runtimeDispatchOccurred");
      requireBoolean(report, "publicApiExposed", false, failures, "Target-33 publicApiExposed");
      requireBoolean(report, "javaAgentUsed", false, failures, "Target-33 javaAgentUsed");
      requireBoolean(report, "mixinUsed", false, failures, "Target-33 mixinUsed");
      requireBoolean(
          report,
          "javaModExecutionSandboxed",
          false,
          failures,
          "Target-33 javaModExecutionSandboxed");
    }
    return stage(
        "target-33-return-value-intercept-offline-proof",
        "Target-33",
        "Target-33 return-value intercept offline proof remained coherent.",
        failures);
  }

  private void requireTarget33Cases(JsonObject report, List<String> failures) {
    JsonArray proofCases = array(report, "proofCases");
    Set<String> labels = new HashSet<>();
    if (proofCases != null) {
      for (JsonElement proofCase : proofCases) {
        if (proofCase.isJsonObject()) {
          JsonObject proofCaseObject = proofCase.getAsJsonObject();
          labels.add(stringValue(proofCaseObject, "id"));
          labels.add(stringValue(proofCaseObject, "label"));
        }
      }
    }
    for (String expected : TARGET_33_EXPECTED_CASE_LABELS) {
      if (labels.stream()
          .filter(value -> value != null)
          .noneMatch(value -> value.contains(expected))) {
        failures.add("Target-33 proof cases must include " + expected + ".");
      }
    }
  }

  private SteelHook04CompletionStageVerification verifyTarget34(
      JsonObject report, String loadFailureReason) {
    List<String> failures = baseFailures(loadFailureReason, report, "Target-34");
    if (report != null) {
      requireInt(report, "schema", 1, failures, "Target-34 schema");
      requireString(report, "target", "minecraft", failures, "Target-34 target");
      requireString(report, "steelHookVersion", "0.4", failures, "Target-34 steelHookVersion");
      requireString(
          report,
          "sourceTarget32Milestone",
          "Target-32",
          failures,
          "Target-34 sourceTarget32Milestone");
      requireStatusString(
          report,
          "sourceTarget32BoundaryStatus",
          "boundary-ready",
          failures,
          "Target-34 sourceTarget32BoundaryStatus");
      requireBoolean(
          report, "sourceTarget32GatePassed", true, failures, "Target-34 sourceTarget32GatePassed");
      requireString(
          report,
          "sourceTarget33Milestone",
          "Target-33",
          failures,
          "Target-34 sourceTarget33Milestone");
      requireStatusString(
          report,
          "sourceTarget33ProofStatus",
          "proof-ready",
          failures,
          "Target-34 sourceTarget33ProofStatus");
      requireBoolean(
          report, "sourceTarget33ProofReady", true, failures, "Target-34 sourceTarget33ProofReady");
      requireBoolean(report, "proofReady", true, failures, "Target-34 proofReady");
      requireStatusString(report, "proofStatus", "proof-ready", failures, "Target-34 proofStatus");
      requirePrimitiveArray(
          report,
          "approvedPrimitiveKinds",
          List.of("INVOKE_REDIRECT", "INVOKE_WRAP"),
          failures,
          "Target-34 approvedPrimitiveKinds");
      requireInt(
          report, "successfulProofCaseCount", 2, failures, "Target-34 successfulProofCaseCount");
      requireTarget34Cases(report, failures);
      requireBoolean(
          report,
          "unsupportedFixtureShapesRejected",
          true,
          failures,
          "Target-34 unsupportedFixtureShapesRejected");
      Integer rejectionProofCaseCount = integerValue(report, "rejectionProofCaseCount");
      if (rejectionProofCaseCount == null || rejectionProofCaseCount <= 0) {
        failures.add("Target-34 rejectionProofCaseCount must be greater than 0.");
      }
      requireBoolean(report, "offlineOnly", true, failures, "Target-34 offlineOnly");
      requireBoolean(
          report,
          "runtimeClassLoadingPathEnabled",
          false,
          failures,
          "Target-34 runtimeClassLoadingPathEnabled");
      requireBoolean(
          report, "classLoadingOccurred", false, failures, "Target-34 classLoadingOccurred");
      requireBoolean(
          report, "serverLaunchOccurred", false, failures, "Target-34 serverLaunchOccurred");
      requireBoolean(
          report, "minecraftMainInvoked", false, failures, "Target-34 minecraftMainInvoked");
      requireBoolean(
          report,
          "hookInstallationOccurred",
          false,
          failures,
          "Target-34 hookInstallationOccurred");
      requireBoolean(
          report, "runtimeDispatchOccurred", false, failures, "Target-34 runtimeDispatchOccurred");
      requireBoolean(report, "publicApiExposed", false, failures, "Target-34 publicApiExposed");
      requireBoolean(report, "javaAgentUsed", false, failures, "Target-34 javaAgentUsed");
      requireBoolean(report, "mixinUsed", false, failures, "Target-34 mixinUsed");
      requireBoolean(
          report,
          "javaModExecutionSandboxed",
          false,
          failures,
          "Target-34 javaModExecutionSandboxed");
    }
    return stage(
        "target-34-invoke-redirect-wrap-offline-proof",
        "Target-34",
        "Target-34 invoke redirect and invoke wrap offline proof remained coherent.",
        failures);
  }

  private void requireTarget34Cases(JsonObject report, List<String> failures) {
    JsonArray proofCases = array(report, "proofCases");
    boolean redirectFound = false;
    boolean wrapFound = false;
    boolean wrappedDelegateMetadataPresent = false;
    if (proofCases != null) {
      for (JsonElement proofCase : proofCases) {
        if (!proofCase.isJsonObject()) {
          continue;
        }
        JsonObject proofCaseObject = proofCase.getAsJsonObject();
        String primitiveKind = stringValue(proofCaseObject, "primitiveKind");
        if ("INVOKE_REDIRECT".equals(primitiveKind)) {
          redirectFound = true;
        }
        if ("INVOKE_WRAP".equals(primitiveKind)) {
          wrapFound = true;
          wrappedDelegateMetadataPresent =
              nonBlank(proofCaseObject, "wrappedDelegateOwnerInternalName")
                  && nonBlank(proofCaseObject, "wrappedDelegateName")
                  && nonBlank(proofCaseObject, "wrappedDelegateDescriptor");
        }
      }
    }
    if (!redirectFound) {
      failures.add("Target-34 proof cases must include INVOKE_REDIRECT.");
    }
    if (!wrapFound) {
      failures.add("Target-34 proof cases must include INVOKE_WRAP.");
    }
    if (!wrappedDelegateMetadataPresent) {
      failures.add("Target-34 INVOKE_WRAP proof must include wrapped delegate metadata.");
    }
  }

  private SteelHook04CompletionStageVerification verifyTarget35(
      JsonObject report, String loadFailureReason) {
    List<String> failures = baseFailures(loadFailureReason, report, "Target-35");
    if (report != null) {
      requireInt(report, "schema", 1, failures, "Target-35 schema");
      requireString(report, "target", "minecraft", failures, "Target-35 target");
      requireString(report, "steelHookVersion", "0.4", failures, "Target-35 steelHookVersion");
      requireString(
          report,
          "sourceTarget32Milestone",
          "Target-32",
          failures,
          "Target-35 sourceTarget32Milestone");
      requireString(
          report,
          "sourceTarget33Milestone",
          "Target-33",
          failures,
          "Target-35 sourceTarget33Milestone");
      requireString(
          report,
          "sourceTarget34Milestone",
          "Target-34",
          failures,
          "Target-35 sourceTarget34Milestone");
      requireBoolean(
          report, "gatedRuntimeProofReady", true, failures, "Target-35 gatedRuntimeProofReady");
      requireString(report, "status", "gated-runtime-proof-ready", failures, "Target-35 status");
      requirePrimitiveArray(
          report,
          "approvedPrimitiveKinds",
          COMPLETED_PRIMITIVE_KINDS,
          failures,
          "Target-35 approvedPrimitiveKinds");
      requireInt(
          report,
          "runtimeClassLoaderProofCount",
          3,
          failures,
          "Target-35 runtimeClassLoaderProofCount");
      requireInt(
          report,
          "runtimeClassLoaderSuccessCount",
          3,
          failures,
          "Target-35 runtimeClassLoaderSuccessCount");
      requireObjectPresent(
          report, "returnValueInterceptProof", failures, "Target-35 returnValueInterceptProof");
      requireObjectPresent(
          report, "invokeRedirectProof", failures, "Target-35 invokeRedirectProof");
      requireObjectPresent(report, "invokeWrapProof", failures, "Target-35 invokeWrapProof");
      requireBoolean(
          report,
          "unsupportedPrimitivePlanRejectedBeforeClassDefinition",
          true,
          failures,
          "Target-35 unsupportedPrimitivePlanRejectedBeforeClassDefinition");
      requireBoolean(
          report,
          "unsupportedPrimitivePlanClassDefinitionAttempted",
          false,
          failures,
          "Target-35 unsupportedPrimitivePlanClassDefinitionAttempted");
      requireBoolean(
          report,
          "runtimeClassLoadingPathEnabled",
          true,
          failures,
          "Target-35 runtimeClassLoadingPathEnabled");
      requireBoolean(
          report, "classLoadingOccurred", true, failures, "Target-35 classLoadingOccurred");
      requireBoolean(
          report,
          "targetClassDefinitionOccurred",
          true,
          failures,
          "Target-35 targetClassDefinitionOccurred");
      JsonArray targetClassesDefined = array(report, "targetClassesDefined");
      if (targetClassesDefined == null || targetClassesDefined.size() != 3) {
        failures.add("Target-35 targetClassesDefined must contain 3 entries.");
      }
      requireBoolean(report, "classInitialized", false, failures, "Target-35 classInitialized");
      requireBoolean(
          report, "targetMethodInvoked", false, failures, "Target-35 targetMethodInvoked");
      requireBoolean(report, "wrapperExecuted", false, failures, "Target-35 wrapperExecuted");
      requireBoolean(
          report, "serverLaunchOccurred", false, failures, "Target-35 serverLaunchOccurred");
      requireBoolean(
          report, "minecraftMainInvoked", false, failures, "Target-35 minecraftMainInvoked");
      requireBoolean(
          report,
          "hookInstallationOccurred",
          false,
          failures,
          "Target-35 hookInstallationOccurred");
      requireBoolean(
          report, "runtimeDispatchOccurred", false, failures, "Target-35 runtimeDispatchOccurred");
      requireBoolean(report, "publicApiExposed", false, failures, "Target-35 publicApiExposed");
      requireBoolean(report, "javaAgentUsed", false, failures, "Target-35 javaAgentUsed");
      requireBoolean(report, "mixinUsed", false, failures, "Target-35 mixinUsed");
      requireBoolean(
          report,
          "javaModExecutionSandboxed",
          false,
          failures,
          "Target-35 javaModExecutionSandboxed");
    }
    return stage(
        "target-35-gated-runtime-proof",
        "Target-35",
        "Target-35 gated runtime proof remained coherent.",
        failures);
  }

  private SteelHook04CompletionStageVerification verifyReturnValueInterceptEvidenceChain(
      JsonObject report, String loadFailureReason) {
    return verifyTarget35PrimitiveProof(
        report == null ? null : object(report, "returnValueInterceptProof"),
        loadFailureReason,
        "target-35-return-value-intercept-evidence-chain",
        "Target-35-return-value-intercept",
        "RETURN_VALUE_INTERCEPT",
        false);
  }

  private SteelHook04CompletionStageVerification verifyInvokeRedirectEvidenceChain(
      JsonObject report, String loadFailureReason) {
    return verifyTarget35PrimitiveProof(
        report == null ? null : object(report, "invokeRedirectProof"),
        loadFailureReason,
        "target-35-invoke-redirect-evidence-chain",
        "Target-35-invoke-redirect",
        "INVOKE_REDIRECT",
        false);
  }

  private SteelHook04CompletionStageVerification verifyInvokeWrapEvidenceChain(
      JsonObject report, String loadFailureReason) {
    return verifyTarget35PrimitiveProof(
        report == null ? null : object(report, "invokeWrapProof"),
        loadFailureReason,
        "target-35-invoke-wrap-evidence-chain",
        "Target-35-invoke-wrap",
        "INVOKE_WRAP",
        true);
  }

  private SteelHook04CompletionStageVerification verifyTarget35PrimitiveProof(
      JsonObject proof,
      String loadFailureReason,
      String stageId,
      String milestoneName,
      String primitiveKind,
      boolean verifyWrappedDelegateMetadata) {
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
        proof, "primitiveKind", primitiveKind, failures, milestoneName + " primitiveKind");
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
    requireBoolean(
        proof,
        "definedBySteelHookRuntimeClassLoader",
        true,
        failures,
        milestoneName + " definedBySteelHookRuntimeClassLoader");
    requireBoolean(proof, "classInitialized", false, failures, milestoneName + " classInitialized");
    requireBoolean(
        proof, "targetMethodInvoked", false, failures, milestoneName + " targetMethodInvoked");
    requireBoolean(proof, "wrapperExecuted", false, failures, milestoneName + " wrapperExecuted");
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
    Integer before = integerValue(proof, "dispatcherInvocationCountBefore");
    Integer after = integerValue(proof, "dispatcherInvocationCountAfter");
    if (before == null || after == null || !before.equals(after)) {
      failures.add(
          milestoneName
              + " dispatcherInvocationCountBefore must equal dispatcherInvocationCountAfter.");
    }
    requireBoolean(proof, "bytecodeModified", true, failures, milestoneName + " bytecodeModified");
    requireBoolean(
        proof,
        "transformedClassBytesProduced",
        true,
        failures,
        milestoneName + " transformedClassBytesProduced");
    if (verifyWrappedDelegateMetadata) {
      if (!(nonBlank(proof, "wrappedDelegateOwnerInternalName")
          && nonBlank(proof, "wrappedDelegateName")
          && nonBlank(proof, "wrappedDelegateDescriptor"))) {
        failures.add(milestoneName + " must include wrapped delegate metadata.");
      }
    }
    return stage(stageId, milestoneName, "Primitive proof remained coherent.", failures);
  }

  private SteelHook04CompletionSafetyInvariant rawByteKeyInvariant(
      JsonObject report, String reportIdPrefix, String reportName) {
    String offendingKey = firstRawByteKey(report);
    return new SteelHook04CompletionSafetyInvariant(
        reportIdPrefix + ".no-raw-byte-payload-keys",
        "absent",
        offendingKey == null ? "absent" : offendingKey,
        offendingKey == null,
        offendingKey == null
            ? null
            : reportName + " contains forbidden raw byte payload key " + offendingKey + ".");
  }

  private SteelHook04CompletionSafetyInvariant unsupportedPrimitiveLeakageInvariant(
      JsonObject target32, JsonObject target34, JsonObject target35) {
    Set<String> target32Kinds = candidatePrimitiveKinds(target32);
    Set<String> target34Kinds = primitiveArrayValues(target34, "approvedPrimitiveKinds");
    Set<String> target35Kinds = primitiveArrayValues(target35, "approvedPrimitiveKinds");
    boolean passed =
        target32Kinds.equals(Set.copyOf(COMPLETED_PRIMITIVE_KINDS))
            && target34Kinds.equals(Set.of("INVOKE_REDIRECT", "INVOKE_WRAP"))
            && target35Kinds.equals(Set.copyOf(COMPLETED_PRIMITIVE_KINDS));
    return new SteelHook04CompletionSafetyInvariant(
        "target-36.unsupported-primitive-leakage-absent",
        "RETURN_VALUE_INTERCEPT,INVOKE_REDIRECT,INVOKE_WRAP only",
        "target32="
            + sortedJoin(target32Kinds)
            + ";target34="
            + sortedJoin(target34Kinds)
            + ";target35="
            + sortedJoin(target35Kinds),
        passed,
        passed ? null : "SteelHook 0.4 reports leaked unsupported primitive evidence.");
  }

  private SteelHook04CompletionSafetyInvariant noExecutionBeyondClassDefinitionInvariant(
      JsonObject target35) {
    boolean passed =
        !bool(target35, "classInitialized")
            && !bool(target35, "targetMethodInvoked")
            && !bool(target35, "wrapperExecuted")
            && !bool(target35, "serverLaunchOccurred")
            && !bool(target35, "minecraftMainInvoked")
            && !bool(target35, "hookInstallationOccurred")
            && !bool(target35, "runtimeDispatchOccurred")
            && !bool(target35, "publicApiExposed")
            && !bool(target35, "javaAgentUsed")
            && !bool(target35, "mixinUsed")
            && !bool(target35, "javaModExecutionSandboxed");
    return new SteelHook04CompletionSafetyInvariant(
        "target-35.no-execution-beyond-class-definition",
        "all forbidden execution booleans false",
        "classInitialized="
            + bool(target35, "classInitialized")
            + ",targetMethodInvoked="
            + bool(target35, "targetMethodInvoked")
            + ",wrapperExecuted="
            + bool(target35, "wrapperExecuted")
            + ",serverLaunchOccurred="
            + bool(target35, "serverLaunchOccurred")
            + ",minecraftMainInvoked="
            + bool(target35, "minecraftMainInvoked")
            + ",hookInstallationOccurred="
            + bool(target35, "hookInstallationOccurred")
            + ",runtimeDispatchOccurred="
            + bool(target35, "runtimeDispatchOccurred")
            + ",publicApiExposed="
            + bool(target35, "publicApiExposed")
            + ",javaAgentUsed="
            + bool(target35, "javaAgentUsed")
            + ",mixinUsed="
            + bool(target35, "mixinUsed")
            + ",javaModExecutionSandboxed="
            + bool(target35, "javaModExecutionSandboxed"),
        passed,
        passed ? null : "Target-35 implies execution beyond class definition.");
  }

  private SteelHook04CompletionFinding forbiddenReportCheck(String id, Path path) {
    boolean present = path != null && Files.exists(path);
    return new SteelHook04CompletionFinding(
        id, present, present ? id + " must not be present for Target-36." : id + " absent.");
  }

  private void addStageFailures(
      List<SteelHook04CompletionFinding> findings,
      List<SteelHook04CompletionStageVerification> stages) {
    for (SteelHook04CompletionStageVerification stage : stages) {
      if (!stage.passed()) {
        findings.add(
            new SteelHook04CompletionFinding(
                stage.stageId(),
                true,
                stage.failureReason() == null ? "failed" : stage.failureReason()));
      }
    }
  }

  private void addInvariantFailures(
      List<SteelHook04CompletionFinding> findings,
      List<SteelHook04CompletionSafetyInvariant> invariants) {
    for (SteelHook04CompletionSafetyInvariant invariant : invariants) {
      if (!invariant.passed()) {
        findings.add(
            new SteelHook04CompletionFinding(
                invariant.id(),
                true,
                invariant.failureReason() == null ? "failed" : invariant.failureReason()));
      }
    }
  }

  private void addForbiddenFailures(
      List<SteelHook04CompletionFinding> findings, List<SteelHook04CompletionFinding> checks) {
    for (SteelHook04CompletionFinding check : checks) {
      if (check.fatal()) {
        findings.add(check);
      }
    }
  }

  private boolean stagePassed(List<SteelHook04CompletionStageVerification> stages, String stageId) {
    return stages.stream()
        .filter(stage -> stageId.equals(stage.stageId()))
        .findFirst()
        .map(SteelHook04CompletionStageVerification::passed)
        .orElse(false);
  }

  private SteelHook04CompletionNextDirection nextDirection(
      List<SteelHook04CompletionStageVerification> stages,
      List<SteelHook04CompletionSafetyInvariant> invariants) {
    if (stages.stream().allMatch(SteelHook04CompletionStageVerification::passed)
        && invariants.stream().allMatch(SteelHook04CompletionSafetyInvariant::passed)) {
      return SteelHook04CompletionNextDirection.STEELHOOK_0_4_COMPLETE;
    }
    if (!stagePassed(stages, "target-32-primitive-boundary")) {
      return SteelHook04CompletionNextDirection.RESTORE_TARGET_32_PRIMITIVE_BOUNDARY;
    }
    if (!stagePassed(stages, "target-33-return-value-intercept-offline-proof")) {
      return SteelHook04CompletionNextDirection
          .RESTORE_TARGET_33_RETURN_VALUE_INTERCEPT_OFFLINE_PROOF;
    }
    if (!stagePassed(stages, "target-34-invoke-redirect-wrap-offline-proof")) {
      return SteelHook04CompletionNextDirection
          .RESTORE_TARGET_34_INVOKE_REDIRECT_WRAP_OFFLINE_PROOF;
    }
    return SteelHook04CompletionNextDirection.RESTORE_TARGET_35_GATED_RUNTIME_PROOF;
  }

  private String nextRecommendedAction(SteelHook04CompletionNextDirection nextDirection) {
    return switch (nextDirection) {
      case STEELHOOK_0_4_COMPLETE -> "SteelHook 0.4 Arc complete.";
      case RESTORE_TARGET_32_PRIMITIVE_BOUNDARY ->
          "Restore the Target-32 primitive boundary report and rerun Target-36.";
      case RESTORE_TARGET_33_RETURN_VALUE_INTERCEPT_OFFLINE_PROOF ->
          "Restore the Target-33 return-value intercept offline proof and rerun Target-36.";
      case RESTORE_TARGET_34_INVOKE_REDIRECT_WRAP_OFFLINE_PROOF ->
          "Restore the Target-34 invoke redirect and wrap offline proof and rerun Target-36.";
      case RESTORE_TARGET_35_GATED_RUNTIME_PROOF ->
          "Restore the Target-35 gated runtime proof and rerun Target-36.";
    };
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

  private SteelHook04CompletionStageVerification stage(
      String stageId, String milestoneName, String summary, List<String> failures) {
    return new SteelHook04CompletionStageVerification(
        stageId,
        milestoneName,
        summary,
        failures.isEmpty(),
        failures.isEmpty() ? null : String.join(" ", failures));
  }

  private String failedStageIds(List<SteelHook04CompletionStageVerification> stages) {
    return stages.stream()
        .filter(stage -> !stage.passed())
        .map(SteelHook04CompletionStageVerification::stageId)
        .reduce((left, right) -> left + "," + right)
        .orElse("none");
  }

  private String failedInvariantIds(List<SteelHook04CompletionSafetyInvariant> invariants) {
    return invariants.stream()
        .filter(invariant -> !invariant.passed())
        .map(SteelHook04CompletionSafetyInvariant::id)
        .reduce((left, right) -> left + "," + right)
        .orElse("none");
  }

  private String failedForbiddenIds(List<SteelHook04CompletionFinding> findings) {
    return findings.stream()
        .filter(SteelHook04CompletionFinding::fatal)
        .map(SteelHook04CompletionFinding::id)
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

  private JsonArray array(JsonObject object, String field) {
    if (object == null || !object.has(field) || !object.get(field).isJsonArray()) {
      return null;
    }
    return object.getAsJsonArray(field);
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

  private void requireStatusString(
      JsonObject object, String field, String expected, List<String> failures, String label) {
    String actual = stringValue(object, field);
    if (!stringMatches(actual, expected)) {
      failures.add(label + " expected " + expected + " but was " + actual + ".");
    }
  }

  private void requireBoolean(
      JsonObject object, String field, boolean expected, List<String> failures, String label) {
    if (object == null || !object.has(field) || object.get(field).isJsonNull()) {
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

  private void requireObjectPresent(
      JsonObject object, String field, List<String> failures, String label) {
    if (this.object(object, field) == null) {
      failures.add(label + " must be present.");
    }
  }

  private void requirePrimitiveSet(
      JsonObject object, String field, List<String> expected, List<String> failures, String label) {
    Set<String> actual = candidatePrimitiveKinds(object);
    if (!actual.equals(Set.copyOf(expected))) {
      failures.add(label + " candidates must contain exactly " + String.join(", ", expected) + ".");
    }
  }

  private void requirePrimitiveArray(
      JsonObject object, String field, List<String> expected, List<String> failures, String label) {
    Set<String> actual = primitiveArrayValues(object, field);
    if (!actual.equals(Set.copyOf(expected))) {
      failures.add(label + " must contain exactly " + String.join(", ", expected) + ".");
    }
  }

  private Set<String> candidatePrimitiveKinds(JsonObject object) {
    Set<String> values = new HashSet<>();
    JsonArray array = array(object, "candidates");
    if (array == null) {
      return values;
    }
    for (JsonElement element : array) {
      if (element.isJsonObject()) {
        String primitiveKind = stringValue(element.getAsJsonObject(), "primitiveKind");
        if (primitiveKind != null) {
          values.add(primitiveKind);
        }
      }
    }
    return values;
  }

  private Set<String> primitiveArrayValues(JsonObject object, String field) {
    Set<String> values = new HashSet<>();
    JsonArray array = array(object, field);
    if (array == null) {
      return values;
    }
    for (JsonElement element : array) {
      try {
        values.add(element.getAsString());
      } catch (RuntimeException ignored) {
        // leave validation to caller
      }
    }
    return values;
  }

  private String sortedJoin(Set<String> values) {
    return values.stream()
        .sorted(Comparator.naturalOrder())
        .reduce((left, right) -> left + "," + right)
        .orElse("none");
  }

  private boolean nonBlank(JsonObject object, String field) {
    String value = stringValue(object, field);
    return value != null && !value.isBlank();
  }

  private boolean stringMatches(String actual, String expected) {
    if (actual == null || expected == null) {
      return false;
    }
    return normalizeValue(actual).equals(normalizeValue(expected));
  }

  private String normalizeValue(String value) {
    return value.trim().replace('_', '-').toLowerCase();
  }

  private record LoadedReport(JsonObject object, String failureReason) {}
}
