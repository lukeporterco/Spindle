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
import java.util.List;

public final class SteelHook02CompletionVerifier {
  private static final String TARGET_INTERNAL_NAME = "net/minecraft/server/Main";
  private static final String TARGET_BINARY_NAME = "net.minecraft.server.Main";
  private static final String TARGET_CLASS_ENTRY_NAME = "net/minecraft/server/Main.class";
  private static final String TARGET_METHOD_NAME = "main";
  private static final String TARGET_DESCRIPTOR = "([Ljava/lang/String;)V";
  private static final String DISPATCHER_BINARY_NAME =
      "com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher";
  private static final String DISPATCHER_METHOD_NAME = "beforeMinecraftServerMain";
  private static final String DISPATCHER_DESCRIPTOR = "()V";
  private static final String PRIMITIVE_KIND = "METHOD_ENTRY_STATIC_DISPATCH";

  public SteelHook02CompletionReport verify(SteelHook02CompletionInput input) {
    LoadedReport contracts =
        loadRequired(input.hookContractsReportPath(), "minecraft-hook-contracts.json");
    LoadedReport placement =
        loadRequired(input.hookPlacementPlanPath(), "minecraft-hook-placement-plan.json");
    LoadedReport bytecode =
        loadRequired(
            input.hookBytecodeAnalysisReportPath(), "minecraft-hook-bytecode-analysis.json");
    LoadedReport patchPlan =
        loadRequired(input.hookPatchPlanPath(), "minecraft-hook-patch-plan.json");
    LoadedReport target23 =
        loadRequired(
            input.primitiveBoundaryReportPath(), "minecraft-steelhook-0-2-primitive-boundary.json");
    LoadedReport target24 =
        loadRequired(
            input.contractGeneralizationReportPath(),
            "minecraft-steelhook-0-2-contract-generalization.json");
    LoadedReport target25 =
        loadRequired(
            input.methodEntryTransformerReportPath(),
            "minecraft-steelhook-0-2-method-entry-transformer-result.json");
    LoadedReport target26 =
        loadRequired(
            input.gatedRuntimeTransformationReportPath(),
            "minecraft-steelhook-0-2-gated-runtime-transformation-result.json");

    SteelHookStageVerification stage3 =
        verifyTarget3KnownContracts(contracts.object(), contracts.failureReason());
    SteelHookStageVerification stage5 =
        verifyTarget5Placement(placement.object(), placement.failureReason());
    SteelHookStageVerification stage6 =
        verifyTarget6BytecodeAnalysis(bytecode.object(), bytecode.failureReason());
    SteelHookStageVerification stage7 =
        verifyTarget7PatchPlan(patchPlan.object(), patchPlan.failureReason());
    SteelHookStageVerification stage23 =
        verifyTarget23PrimitiveBoundary(target23.object(), target23.failureReason());
    SteelHookStageVerification stage24 =
        verifyTarget24ContractGeneralization(target24.object(), target24.failureReason());
    SteelHookStageVerification stage25 =
        verifyTarget25MethodEntryTransformer(target25.object(), target25.failureReason());
    SteelHookStageVerification stage26 =
        verifyTarget26GatedRuntimeTransformation(
            target25.object(), target26.object(), target26.failureReason());

    List<SteelHookStageVerification> stagesBeforeCompletion =
        List.of(stage3, stage5, stage6, stage7, stage23, stage24, stage25, stage26);
    List<SteelHookSafetyInvariant> invariants =
        buildSafetyInvariants(
            input,
            patchPlan.object(),
            target23.object(),
            target24.object(),
            target25.object(),
            target26.object());
    long safetyFailureCount = invariants.stream().filter(invariant -> !invariant.passed()).count();
    SteelHookStageVerification stage27 =
        verifyTarget27Completion(
            stagesBeforeCompletion,
            invariants,
            target26.object(),
            input.hookInstallationResultPath());

    List<SteelHookStageVerification> stages = new ArrayList<>(stagesBeforeCompletion);
    stages.add(stage27);
    long stageFailureCount = stages.stream().filter(stage -> !stage.passed()).count();
    boolean reportChainVerified =
        contracts.object() != null
            && placement.object() != null
            && bytecode.object() != null
            && patchPlan.object() != null
            && target23.object() != null
            && target24.object() != null
            && target25.object() != null
            && target26.object() != null;

    boolean passed = stageFailureCount == 0 && safetyFailureCount == 0;
    SteelHook02CompletionNextDirection nextDirection =
        passed
            ? SteelHook02CompletionNextDirection.MOVE_TO_STEELHOOK_0_3_STACKMAP_AND_EXIT_PRIMITIVES
            : failureDirection(stage7, stage23, stage24, stage25, stage26);
    String failureSummary =
        passed
            ? null
            : "SteelHook 0.2 completion blocked: failed stages="
                + failedStageIds(stages)
                + ", failed invariants="
                + failedInvariantIds(invariants);

    return new SteelHook02CompletionReport(
        1,
        "Target-27",
        "minecraft",
        "0.2",
        passed ? SteelHook02CompletionStatus.PASSED : SteelHook02CompletionStatus.FAILED,
        passed
            ? SteelHook02CompletionHandoffStatus.STEELHOOK_0_2_COMPLETE
            : SteelHook02CompletionHandoffStatus.STEELHOOK_0_2_BLOCKED,
        nextDirection,
        reportChainVerified,
        passed,
        (int) stageFailureCount,
        (int) safetyFailureCount,
        capabilityBoundaries().size(),
        TARGET_BINARY_NAME,
        TARGET_CLASS_ENTRY_NAME,
        PRIMITIVE_KIND,
        bool(target26.object(), "runtimeClassLoadingPathEnabled"),
        bool(target26.object(), "targetClassDefined"),
        !bool(target26.object(), "minecraftMainInvoked"),
        !bool(target26.object(), "minecraftServerLaunched"),
        !bool(target26.object(), "hookInstallationOccurred")
            && !Files.exists(input.hookInstallationResultPath()),
        !bool(target26.object(), "runtimeDispatchOccurred")
            && !bool(target26.object(), "dispatcherInvocationObserved"),
        !bool(target26.object(), "publicApiExposed"),
        !bool(target26.object(), "javaModExecutionSandboxed"),
        unsupportedCapabilitiesRemainBlockedVerified(),
        stages,
        invariants,
        capabilityBoundaries(),
        failureSummary);
  }

  private SteelHookStageVerification verifyTarget3KnownContracts(
      JsonObject report, String loadFailureReason) {
    List<String> failures = baseFailures(loadFailureReason, report, "Target-3", 2);
    if (report != null) {
      requireBoolean(report, "validationPassed", true, failures, "Target-3 validationPassed");
      requireInt(report, "errorCount", 0, failures, "Target-3 errorCount");
      requireOptionalBoolean(
          report, "publicApiExposed", false, failures, "Target-3 publicApiExposed");
      requireOptionalBoolean(
          report,
          "javaModExecutionSandboxed",
          false,
          failures,
          "Target-3 javaModExecutionSandboxed");
      JsonObject matchedContract = findMatchingContract(array(report, "contracts"));
      if (matchedContract == null) {
        failures.add(
            "Target-3 requires contract for net/minecraft/server/Main.main([Ljava/lang/String;)V.");
      } else {
        requireBoolean(matchedContract, "valid", true, failures, "Target-3 contract valid");
      }
    }
    return stage(
        "target-3-known-contracts",
        "Target-3",
        "Known contract for net.minecraft.server.Main.main remained valid.",
        failures);
  }

  private SteelHookStageVerification verifyTarget5Placement(
      JsonObject report, String loadFailureReason) {
    List<String> failures = baseFailures(loadFailureReason, report, "Target-5", 1);
    if (report != null) {
      requireBoolean(report, "gatePassed", true, failures, "Target-5 gatePassed");
      requireBoolean(report, "placementPlanned", true, failures, "Target-5 placementPlanned");
      requireInt(report, "plannedPlacementCount", 1, failures, "Target-5 plannedPlacementCount");
      JsonObject plannedPlacement = firstArrayObject(report, "plannedPlacements");
      if (plannedPlacement == null) {
        failures.add("Target-5 requires exactly one planned placement.");
      } else {
        requireString(
            plannedPlacement,
            "ownerInternalName",
            TARGET_INTERNAL_NAME,
            failures,
            "Target-5 ownerInternalName");
        requireString(
            plannedPlacement, "memberName", TARGET_METHOD_NAME, failures, "Target-5 memberName");
        requireString(
            plannedPlacement, "descriptor", TARGET_DESCRIPTOR, failures, "Target-5 descriptor");
        requireOneOfStrings(
            plannedPlacement,
            stringValue(plannedPlacement, "placementKind") == null ? "kind" : "placementKind",
            List.of("METHOD_ENTRY", "method-entry"),
            failures,
            "Target-5 placementKind");
      }
    }
    return stage(
        "target-5-method-entry-placement",
        "Target-5",
        "Method-entry placement remained fixed to net.minecraft.server.Main.main.",
        failures);
  }

  private SteelHookStageVerification verifyTarget6BytecodeAnalysis(
      JsonObject report, String loadFailureReason) {
    List<String> failures = baseFailures(loadFailureReason, report, "Target-6", 1);
    if (report != null) {
      requireBoolean(report, "gatePassed", true, failures, "Target-6 gatePassed");
      requireBoolean(
          report,
          "bytecodeAnalysisSucceeded",
          true,
          failures,
          "Target-6 bytecodeAnalysisSucceeded");
      requireBoolean(
          report,
          "instructionBoundaryValidationPassed",
          true,
          failures,
          "Target-6 instructionBoundaryValidationPassed");
      requireBoolean(
          report,
          "branchTargetValidationPassed",
          true,
          failures,
          "Target-6 branchTargetValidationPassed");
      requireBoolean(
          report,
          "switchTargetValidationPassed",
          true,
          failures,
          "Target-6 switchTargetValidationPassed");
      requireBoolean(
          report,
          "exceptionTableValidationPassed",
          true,
          failures,
          "Target-6 exceptionTableValidationPassed");
      requireString(
          report,
          "ownerInternalName",
          TARGET_INTERNAL_NAME,
          failures,
          "Target-6 ownerInternalName");
      requireString(report, "memberName", TARGET_METHOD_NAME, failures, "Target-6 memberName");
      requireString(report, "descriptor", TARGET_DESCRIPTOR, failures, "Target-6 descriptor");
    }
    return stage(
        "target-6-bytecode-analysis",
        "Target-6",
        "Bytecode analysis and boundary validation remained green for the approved target.",
        failures);
  }

  private SteelHookStageVerification verifyTarget7PatchPlan(
      JsonObject report, String loadFailureReason) {
    List<String> failures = baseFailures(loadFailureReason, report, "Target-7", 1);
    if (report != null) {
      JsonObject plannedPatch = firstArrayObject(report, "plannedPatches");
      requireBoolean(report, "gatePassed", true, failures, "Target-7 gatePassed");
      requireBoolean(
          report, "patchPlanningSucceeded", true, failures, "Target-7 patchPlanningSucceeded");
      requireBoolean(report, "patchPlanned", true, failures, "Target-7 patchPlanned");
      requireInt(report, "plannedPatchCount", 1, failures, "Target-7 plannedPatchCount");
      requireString(report, "targetClass", TARGET_INTERNAL_NAME, failures, "Target-7 targetClass");
      requireString(report, "targetMethod", TARGET_METHOD_NAME, failures, "Target-7 targetMethod");
      requireString(
          report, "targetDescriptor", TARGET_DESCRIPTOR, failures, "Target-7 targetDescriptor");
      requireOneOfStrings(
          stringValue(report, "patchKind") == null ? plannedPatch : report,
          stringValue(report, "patchKind") == null ? "kind" : "patchKind",
          List.of("METHOD_ENTRY_STATIC_DISPATCH", "method-entry-static-dispatch"),
          failures,
          "Target-7 patchKind");
      requireOneOfStrings(
          stringValue(report, "patchMode") == null ? plannedPatch : report,
          stringValue(report, "patchMode") == null ? "mode" : "patchMode",
          List.of("DRY_RUN_STATIC_DISPATCH_INVOKESTATIC", "dry-run-static-dispatch-invokestatic"),
          failures,
          "Target-7 patchMode");
      requireOneOfStrings(
          report.has("patchEligibility") ? report : plannedPatch,
          "patchEligibility",
          List.of("FIXTURE_ONLY_FUTURE_TRANSFORM", "fixture-only-future-transform"),
          failures,
          "Target-7 patchEligibility");
      requireInt(report, "insertionOffset", 0, failures, "Target-7 insertionOffset");
      requireBoolean(
          report,
          "transformReadyForFixtureOnly",
          true,
          failures,
          "Target-7 transformReadyForFixtureOnly");
      requireBoolean(
          report,
          "transformReadyForMinecraftRuntime",
          false,
          failures,
          "Target-7 transformReadyForMinecraftRuntime");
      requireBoolean(report, "injectionOccurred", false, failures, "Target-7 injectionOccurred");
      requireBoolean(
          report, "transformationOccurred", false, failures, "Target-7 transformationOccurred");
      requireBoolean(report, "patchingOccurred", false, failures, "Target-7 patchingOccurred");
      requireBoolean(report, "bytecodeModified", false, failures, "Target-7 bytecodeModified");
      requireBoolean(report, "publicApiExposed", false, failures, "Target-7 publicApiExposed");
      requireBoolean(
          report,
          "javaModExecutionSandboxed",
          false,
          failures,
          "Target-7 javaModExecutionSandboxed");
      requireBoolean(report, "mixinUsed", false, failures, "Target-7 mixinUsed");
      requireBoolean(report, "javaAgentUsed", false, failures, "Target-7 javaAgentUsed");
    }
    return stage(
        "target-7-patch-plan",
        "Target-7",
        "Patch plan remained bounded to one dry-run method-entry static dispatch primitive.",
        failures);
  }

  private SteelHookStageVerification verifyTarget23PrimitiveBoundary(
      JsonObject report, String loadFailureReason) {
    List<String> failures = baseFailures(loadFailureReason, report, "Target-23", 1);
    if (report != null) {
      requireString(report, "target", "minecraft", failures, "Target-23 target");
      requireString(report, "steelHookVersion", "0.2", failures, "Target-23 steelHookVersion");
      requireBoolean(report, "analysisOnly", true, failures, "Target-23 analysisOnly");
      requireBoolean(report, "gatePassed", true, failures, "Target-23 gatePassed");
      requireOneOfStrings(
          report,
          "boundaryStatus",
          List.of("primitive-boundary-selected", "PRIMITIVE_BOUNDARY_SELECTED"),
          failures,
          "Target-23 boundaryStatus");
      requireOneOfStrings(
          report,
          "nextDirection",
          List.of(
              "move-to-target-24-contract-and-patch-plan-generalization",
              "MOVE_TO_TARGET_24_CONTRACT_AND_PATCH_PLAN_GENERALIZATION"),
          failures,
          "Target-23 nextDirection");
      requireInt(report, "approvedCandidateCount", 1, failures, "Target-23 approvedCandidateCount");
      requireInt(
          report, "supportedPrimitiveCount", 1, failures, "Target-23 supportedPrimitiveCount");
      requireBoolean(
          report, "classLoadingOccurred", false, failures, "Target-23 classLoadingOccurred");
      requireBoolean(report, "injectionOccurred", false, failures, "Target-23 injectionOccurred");
      requireBoolean(
          report, "transformationOccurred", false, failures, "Target-23 transformationOccurred");
      requireBoolean(report, "patchingOccurred", false, failures, "Target-23 patchingOccurred");
      requireOptionalBoolean(
          report, "bytecodeModified", false, failures, "Target-23 bytecodeModified");
      requireBoolean(
          report,
          "hookInstallationOccurred",
          false,
          failures,
          "Target-23 hookInstallationOccurred");
      requireBoolean(
          report, "runtimeDispatchOccurred", false, failures, "Target-23 runtimeDispatchOccurred");
      requireBoolean(report, "publicApiExposed", false, failures, "Target-23 publicApiExposed");
      requireBoolean(
          report,
          "javaModExecutionSandboxed",
          false,
          failures,
          "Target-23 javaModExecutionSandboxed");
      JsonObject candidate = firstArrayObject(report, "candidates");
      if (candidate == null) {
        failures.add("Target-23 requires one approved candidate.");
      } else {
        requireString(
            candidate,
            "primitiveKind",
            PRIMITIVE_KIND,
            failures,
            "Target-23 candidate primitiveKind");
        requireBoolean(
            candidate,
            "eligibleForTarget24ContractGeneralization",
            true,
            failures,
            "Target-23 candidate eligibleForTarget24ContractGeneralization");
        requireBoolean(
            candidate,
            "eligibleForTarget25TransformerExtraction",
            true,
            failures,
            "Target-23 candidate eligibleForTarget25TransformerExtraction");
        requireBoolean(
            candidate,
            "eligibleForTarget26RuntimeTransformation",
            false,
            failures,
            "Target-23 candidate eligibleForTarget26RuntimeTransformation");
      }
    }
    return stage(
        "target-23-primitive-boundary",
        "Target-23",
        "Primitive boundary remained analysis-only and limited to one approved candidate.",
        failures);
  }

  private SteelHookStageVerification verifyTarget24ContractGeneralization(
      JsonObject report, String loadFailureReason) {
    List<String> failures = baseFailures(loadFailureReason, report, "Target-24", 1);
    if (report != null) {
      requireBoolean(report, "analysisOnly", true, failures, "Target-24 analysisOnly");
      requireBoolean(report, "gatePassed", true, failures, "Target-24 gatePassed");
      requireOneOfStrings(
          report,
          "status",
          List.of("contract-generalization-ready", "CONTRACT_GENERALIZATION_READY"),
          failures,
          "Target-24 status");
      requireOneOfStrings(
          report,
          "nextDirection",
          List.of(
              "move-to-target-25-runtime-safe-method-entry-transformer",
              "MOVE_TO_TARGET_25_RUNTIME_SAFE_METHOD_ENTRY_TRANSFORMER"),
          failures,
          "Target-24 nextDirection");
      requireBoolean(
          report,
          "contractGeneralizationOccurred",
          true,
          failures,
          "Target-24 contractGeneralizationOccurred");
      requireBoolean(
          report,
          "contractGeneralizationReady",
          true,
          failures,
          "Target-24 contractGeneralizationReady");
      requireBoolean(
          report,
          "minecraftRuntimeTransformReady",
          false,
          failures,
          "Target-24 minecraftRuntimeTransformReady");
      requireBoolean(
          report,
          "eligibleForTarget25TransformerExtraction",
          true,
          failures,
          "Target-24 eligibleForTarget25TransformerExtraction");
      requireBoolean(
          report,
          "eligibleForTarget26RuntimeTransformation",
          false,
          failures,
          "Target-24 eligibleForTarget26RuntimeTransformation");
      requireBoolean(
          report, "classLoadingOccurred", false, failures, "Target-24 classLoadingOccurred");
      requireBoolean(report, "injectionOccurred", false, failures, "Target-24 injectionOccurred");
      requireBoolean(
          report, "transformationOccurred", false, failures, "Target-24 transformationOccurred");
      requireBoolean(report, "patchingOccurred", false, failures, "Target-24 patchingOccurred");
      requireBoolean(report, "bytecodeModified", false, failures, "Target-24 bytecodeModified");
      requireBoolean(
          report,
          "hookInstallationOccurred",
          false,
          failures,
          "Target-24 hookInstallationOccurred");
      requireBoolean(
          report, "runtimeDispatchOccurred", false, failures, "Target-24 runtimeDispatchOccurred");
      requireBoolean(report, "publicApiExposed", false, failures, "Target-24 publicApiExposed");
      requireBoolean(
          report,
          "javaModExecutionSandboxed",
          false,
          failures,
          "Target-24 javaModExecutionSandboxed");
      JsonObject targetDescriptor = object(report, "targetDescriptor");
      JsonObject dispatcherDescriptor = object(report, "dispatcherDescriptor");
      JsonObject primitiveContract = object(report, "primitiveContract");
      JsonObject generalizedPatchPlan = object(report, "generalizedPatchPlan");
      if (targetDescriptor == null) {
        failures.add("Target-24 targetDescriptor must be present.");
      } else {
        requireString(
            targetDescriptor,
            "ownerInternalName",
            TARGET_INTERNAL_NAME,
            failures,
            "Target-24 targetDescriptor ownerInternalName");
        requireString(
            targetDescriptor,
            "memberName",
            TARGET_METHOD_NAME,
            failures,
            "Target-24 targetDescriptor memberName");
        requireString(
            targetDescriptor,
            "descriptor",
            TARGET_DESCRIPTOR,
            failures,
            "Target-24 targetDescriptor descriptor");
      }
      if (dispatcherDescriptor == null) {
        failures.add("Target-24 dispatcherDescriptor must be present.");
      } else {
        requireString(
            dispatcherDescriptor,
            "binaryName",
            DISPATCHER_BINARY_NAME,
            failures,
            "Target-24 dispatcherDescriptor binaryName");
        requireString(
            dispatcherDescriptor,
            "methodName",
            DISPATCHER_METHOD_NAME,
            failures,
            "Target-24 dispatcherDescriptor methodName");
        requireString(
            dispatcherDescriptor,
            "descriptor",
            DISPATCHER_DESCRIPTOR,
            failures,
            "Target-24 dispatcherDescriptor descriptor");
      }
      if (primitiveContract == null) {
        failures.add("Target-24 primitiveContract must be present.");
      } else {
        requireString(
            primitiveContract,
            "primitiveKind",
            PRIMITIVE_KIND,
            failures,
            "Target-24 primitiveContract primitiveKind");
      }
      if (generalizedPatchPlan == null) {
        failures.add("Target-24 generalizedPatchPlan must be present.");
      } else {
        requireOneOfStrings(
            generalizedPatchPlan,
            "patchMode",
            List.of("steelhook-0-2-contract-generalized-static-dispatch-invokestatic"),
            failures,
            "Target-24 generalizedPatchPlan patchMode");
        requireOneOfStrings(
            generalizedPatchPlan,
            "patchEligibility",
            List.of("steelhook-0-2-contract-ready-runtime-candidate"),
            failures,
            "Target-24 generalizedPatchPlan patchEligibility");
      }
    }
    return stage(
        "target-24-contract-generalization",
        "Target-24",
        "Contract generalization remained analysis-only and descriptor-complete.",
        failures);
  }

  private SteelHookStageVerification verifyTarget25MethodEntryTransformer(
      JsonObject report, String loadFailureReason) {
    List<String> failures = baseFailures(loadFailureReason, report, "Target-25", 1);
    if (report != null) {
      requireBoolean(report, "gatePassed", true, failures, "Target-25 gatePassed");
      requireOneOfStrings(report, "status", List.of("TRANSFORMED"), failures, "Target-25 status");
      requireOneOfStrings(
          report,
          "nextDirection",
          List.of("MOVE_TO_TARGET_26_GATED_REAL_RUNTIME_TRANSFORMATION"),
          failures,
          "Target-25 nextDirection");
      requireBoolean(
          report, "localTransformationOnly", true, failures, "Target-25 localTransformationOnly");
      requireBoolean(
          report,
          "runtimeClassLoadingPathEnabled",
          false,
          failures,
          "Target-25 runtimeClassLoadingPathEnabled");
      requireBoolean(
          report, "classLoadingOccurred", false, failures, "Target-25 classLoadingOccurred");
      requireBoolean(
          report,
          "hookInstallationOccurred",
          false,
          failures,
          "Target-25 hookInstallationOccurred");
      requireBoolean(
          report, "runtimeDispatchOccurred", false, failures, "Target-25 runtimeDispatchOccurred");
      requireBoolean(
          report,
          "realMinecraftRuntimeTransformed",
          false,
          failures,
          "Target-25 realMinecraftRuntimeTransformed");
      requireBoolean(report, "publicApiExposed", false, failures, "Target-25 publicApiExposed");
      requireBoolean(
          report,
          "javaModExecutionSandboxed",
          false,
          failures,
          "Target-25 javaModExecutionSandboxed");
      requireBoolean(
          report,
          "minecraftRuntimeTransformReady",
          false,
          failures,
          "Target-25 minecraftRuntimeTransformReady");
      requireBoolean(
          report,
          "target25TransformerExtractionOccurred",
          true,
          failures,
          "Target-25 target25TransformerExtractionOccurred");
      requireBoolean(
          report,
          "methodEntryTransformationOccurred",
          true,
          failures,
          "Target-25 methodEntryTransformationOccurred");
      requireBoolean(report, "bytecodeModified", true, failures, "Target-25 bytecodeModified");
      requireBoolean(
          report,
          "transformedClassBytesProduced",
          true,
          failures,
          "Target-25 transformedClassBytesProduced");
      requireBoolean(
          report,
          "eligibleForTarget26GatedRuntimeTransformation",
          true,
          failures,
          "Target-25 eligibleForTarget26GatedRuntimeTransformation");
      requireNonBlank(report, "originalClassSha256", failures, "Target-25 originalClassSha256");
      requireNonBlank(
          report, "transformedClassSha256", failures, "Target-25 transformedClassSha256");
      requireNonBlank(report, "originalCodeSha256", failures, "Target-25 originalCodeSha256");
      requireNonBlank(report, "transformedCodeSha256", failures, "Target-25 transformedCodeSha256");
      requireIntegerPresent(report, "originalCodeLength", failures, "Target-25 originalCodeLength");
      Integer originalCodeLength = integerValue(report, "originalCodeLength");
      Integer transformedCodeLength = integerValue(report, "transformedCodeLength");
      if (originalCodeLength == null || transformedCodeLength == null) {
        failures.add("Target-25 code lengths must be present.");
      } else if (transformedCodeLength != originalCodeLength + 3) {
        failures.add("Target-25 transformedCodeLength must equal originalCodeLength + 3.");
      }
      Integer constantPoolCountBefore = integerValue(report, "constantPoolCountBefore");
      Integer constantPoolCountAfter = integerValue(report, "constantPoolCountAfter");
      if (constantPoolCountBefore == null || constantPoolCountAfter == null) {
        failures.add("Target-25 constant pool counts must be present.");
      } else if (constantPoolCountAfter <= constantPoolCountBefore) {
        failures.add(
            "Target-25 constantPoolCountAfter must be greater than constantPoolCountBefore.");
      }
      requireIntegerPresent(report, "methodrefIndex", failures, "Target-25 methodrefIndex");
      String insertedInstructionHex = stringValue(report, "insertedInstructionHex");
      if (insertedInstructionHex == null || !insertedInstructionHex.startsWith("b8")) {
        failures.add("Target-25 insertedInstructionHex must start with b8.");
      }
      if (containsRawTransformedClassBytes(report)) {
        failures.add("Target-25 report must not serialize raw transformed class bytes.");
      }
    }
    return stage(
        "target-25-method-entry-transformer",
        "Target-25",
        "Offline method-entry transformation remained bounded and byte-array-free.",
        failures);
  }

  private SteelHookStageVerification verifyTarget26GatedRuntimeTransformation(
      JsonObject target25Report, JsonObject report, String loadFailureReason) {
    List<String> failures = baseFailures(loadFailureReason, report, "Target-26", 1);
    if (report != null) {
      requireBoolean(report, "gatePassed", true, failures, "Target-26 gatePassed");
      requireOneOfStrings(
          report, "status", List.of("TRANSFORMED_AND_DEFINED"), failures, "Target-26 status");
      requireOneOfStrings(
          report,
          "nextDirection",
          List.of("MOVE_TO_TARGET_27_STEELHOOK_0_2_COMPLETION"),
          failures,
          "Target-26 nextDirection");
      requireBoolean(
          report,
          "runtimeClassLoadingPathEnabled",
          true,
          failures,
          "Target-26 runtimeClassLoadingPathEnabled");
      requireBoolean(
          report,
          "runtimeClassLoadingAttempted",
          true,
          failures,
          "Target-26 runtimeClassLoadingAttempted");
      requireBoolean(
          report,
          "runtimeClassLoadingSucceeded",
          true,
          failures,
          "Target-26 runtimeClassLoadingSucceeded");
      requireBoolean(
          report, "classLoadingOccurred", true, failures, "Target-26 classLoadingOccurred");
      requireBoolean(report, "targetClassDefined", true, failures, "Target-26 targetClassDefined");
      requireString(
          report, "targetBinaryName", TARGET_BINARY_NAME, failures, "Target-26 targetBinaryName");
      requireString(
          report,
          "targetClassEntryName",
          TARGET_CLASS_ENTRY_NAME,
          failures,
          "Target-26 targetClassEntryName");
      requireString(
          report, "definedClassName", TARGET_BINARY_NAME, failures, "Target-26 definedClassName");
      requireBoolean(
          report,
          "definedBySteelHookRuntimeClassLoader",
          true,
          failures,
          "Target-26 definedBySteelHookRuntimeClassLoader");
      requireBoolean(
          report,
          "realMinecraftRuntimeTransformed",
          true,
          failures,
          "Target-26 realMinecraftRuntimeTransformed");
      requireBoolean(
          report,
          "methodEntryTransformationOccurred",
          true,
          failures,
          "Target-26 methodEntryTransformationOccurred");
      requireBoolean(report, "bytecodeModified", true, failures, "Target-26 bytecodeModified");
      requireBoolean(
          report,
          "transformedClassBytesProduced",
          true,
          failures,
          "Target-26 transformedClassBytesProduced");
      requireBoolean(
          report, "minecraftMainInvoked", false, failures, "Target-26 minecraftMainInvoked");
      requireBoolean(
          report, "minecraftServerLaunched", false, failures, "Target-26 minecraftServerLaunched");
      requireBoolean(
          report,
          "hookInstallationOccurred",
          false,
          failures,
          "Target-26 hookInstallationOccurred");
      requireBoolean(
          report, "runtimeDispatchOccurred", false, failures, "Target-26 runtimeDispatchOccurred");
      requireBoolean(
          report,
          "dispatcherInvocationObserved",
          false,
          failures,
          "Target-26 dispatcherInvocationObserved");
      requireBoolean(report, "publicApiExposed", false, failures, "Target-26 publicApiExposed");
      requireBoolean(report, "javaAgentUsed", false, failures, "Target-26 javaAgentUsed");
      requireBoolean(report, "mixinUsed", false, failures, "Target-26 mixinUsed");
      requireBoolean(report, "remappingOccurred", false, failures, "Target-26 remappingOccurred");
      requireBoolean(report, "accessWidenersUsed", false, failures, "Target-26 accessWidenersUsed");
      requireBoolean(
          report,
          "javaModExecutionSandboxed",
          false,
          failures,
          "Target-26 javaModExecutionSandboxed");
      requireBoolean(
          report,
          "minecraftRuntimeTransformReady",
          true,
          failures,
          "Target-26 minecraftRuntimeTransformReady");
      requireBoolean(
          report,
          "eligibleForTarget27CompletionVerification",
          true,
          failures,
          "Target-26 eligibleForTarget27CompletionVerification");
      if (target25Report != null) {
        requireSameString(
            target25Report,
            report,
            "originalClassSha256",
            failures,
            "Target-25/26 originalClassSha256");
        requireSameString(
            target25Report,
            report,
            "transformedClassSha256",
            failures,
            "Target-25/26 transformedClassSha256");
        requireSameString(
            target25Report,
            report,
            "originalCodeSha256",
            failures,
            "Target-25/26 originalCodeSha256");
        requireSameString(
            target25Report,
            report,
            "transformedCodeSha256",
            failures,
            "Target-25/26 transformedCodeSha256");
        requireSameString(
            target25Report,
            report,
            "insertedInstructionHex",
            failures,
            "Target-25/26 insertedInstructionHex");
      }
      if (containsRawTransformedClassBytes(report)) {
        failures.add("Target-26 report must not serialize raw transformed class bytes.");
      }
    }
    return stage(
        "target-26-gated-runtime-transformation",
        "Target-26",
        "Gated runtime classloader definition remained the only approved runtime proof path.",
        failures);
  }

  private SteelHookStageVerification verifyTarget27Completion(
      List<SteelHookStageVerification> stagesBeforeCompletion,
      List<SteelHookSafetyInvariant> invariants,
      JsonObject target26Report,
      Path hookInstallationResultPath) {
    List<String> failures = new ArrayList<>();
    if (stagesBeforeCompletion.stream().anyMatch(stage -> !stage.passed())) {
      failures.add("Prior Target-3 through Target-26 verification stages must all pass.");
    }
    if (invariants.stream().anyMatch(invariant -> !invariant.passed())) {
      failures.add("All SteelHook 0.2 safety invariants must pass.");
    }
    if (target26Report != null) {
      requireOneOfStrings(
          target26Report,
          "status",
          List.of("TRANSFORMED_AND_DEFINED"),
          failures,
          "Target-27 Target-26 status");
      requireBoolean(
          target26Report,
          "eligibleForTarget27CompletionVerification",
          true,
          failures,
          "Target-27 Target-26 eligibleForTarget27CompletionVerification");
    }
    if (Files.exists(hookInstallationResultPath)) {
      failures.add(
          "Target-27 forbids minecraft-hook-installation-result.json during completion verification.");
    }
    return stage(
        "target-27-completion",
        "Target-27",
        "SteelHook 0.2 completion chain, invariants, and handoff remained coherent.",
        failures);
  }

  private List<SteelHookSafetyInvariant> buildSafetyInvariants(
      SteelHook02CompletionInput input,
      JsonObject patchPlan,
      JsonObject target23,
      JsonObject target24,
      JsonObject target25,
      JsonObject target26) {
    List<SteelHookSafetyInvariant> invariants = new ArrayList<>();
    invariants.add(
        stringInvariant(
            "steelhook-0-2.single-approved-primitive",
            PRIMITIVE_KIND,
            stringValue(object(target24, "primitiveContract"), "primitiveKind")));
    invariants.add(
        stringInvariant(
            "steelhook-0-2.target-shape-fixed",
            TARGET_BINARY_NAME,
            stringValue(target26, "targetBinaryName")));
    invariants.add(
        booleanInvariant("steelhook-0-2.target23-analysis-only", true, target23, "analysisOnly"));
    invariants.add(
        booleanInvariant("steelhook-0-2.target24-analysis-only", true, target24, "analysisOnly"));
    invariants.add(
        booleanInvariant(
            "steelhook-0-2.target25-offline-only", true, target25, "localTransformationOnly"));
    invariants.add(
        booleanInvariant(
            "steelhook-0-2.target26-runtime-classloader-only",
            true,
            target26,
            "runtimeClassLoadingPathEnabled"));
    invariants.add(
        booleanInvariant(
            "steelhook-0-2.runtime-classloading-succeeded",
            true,
            target26,
            "runtimeClassLoadingSucceeded"));
    invariants.add(
        negatedBooleanInvariant(
            "steelhook-0-2.minecraft-main-not-invoked", target26, "minecraftMainInvoked"));
    invariants.add(
        negatedBooleanInvariant(
            "steelhook-0-2.minecraft-server-not-launched", target26, "minecraftServerLaunched"));
    invariants.add(
        noFileAndFalseInvariant(
            "steelhook-0-2.hook-installation-not-occurred",
            target26,
            "hookInstallationOccurred",
            input.hookInstallationResultPath()));
    invariants.add(
        negatedBooleanInvariant(
            "steelhook-0-2.runtime-dispatch-not-observed", target26, "runtimeDispatchOccurred"));
    invariants.add(
        negatedBooleanInvariant(
            "steelhook-0-2.dispatcher-invocation-not-required",
            target26,
            "dispatcherInvocationObserved"));
    invariants.add(
        negatedBooleanInvariant(
            "steelhook-0-2.public-api-not-exposed", target26, "publicApiExposed"));
    invariants.add(
        negatedBooleanInvariant(
            "steelhook-0-2.java-mod-execution-not-sandboxed",
            target26,
            "javaModExecutionSandboxed"));
    invariants.add(
        negatedBooleanInvariant("steelhook-0-2.no-java-agent", target26, "javaAgentUsed"));
    invariants.add(negatedBooleanInvariant("steelhook-0-2.no-mixin", target26, "mixinUsed"));
    invariants.add(
        negatedBooleanInvariant("steelhook-0-2.no-remapping", target26, "remappingOccurred"));
    invariants.add(
        negatedBooleanInvariant(
            "steelhook-0-2.no-access-wideners", target26, "accessWidenersUsed"));
    invariants.add(
        negatedBooleanInvariant(
            "steelhook-0-2.stackmaptable-rewriting-not-claimed",
            target26,
            "stackMapTableRewritingClaimed"));
    invariants.add(
        new SteelHookSafetyInvariant(
            "steelhook-0-2.no-transformed-byte-arrays-serialized",
            "false",
            Boolean.toString(
                containsRawTransformedClassBytes(target25)
                    || containsRawTransformedClassBytes(target26)),
            !containsRawTransformedClassBytes(target25)
                && !containsRawTransformedClassBytes(target26),
            containsRawTransformedClassBytes(target25) || containsRawTransformedClassBytes(target26)
                ? "Target-25 or Target-26 serialized raw transformed class bytes."
                : null));
    invariants.add(
        new SteelHookSafetyInvariant(
            "steelhook-0-2.old-bootstrap-report-not-required",
            "absent",
            filePresenceValue(input.hookBootstrapTransformationResultPath())
                + "/"
                + filePresenceValue(input.fixtureTransformationResultPath())
                + "/"
                + filePresenceValue(input.serverBootstrapResultPath()),
            !Files.exists(input.hookBootstrapTransformationResultPath())
                && !Files.exists(input.fixtureTransformationResultPath())
                && !Files.exists(input.serverBootstrapResultPath()),
            Files.exists(input.hookBootstrapTransformationResultPath())
                    || Files.exists(input.fixtureTransformationResultPath())
                    || Files.exists(input.serverBootstrapResultPath())
                ? "Old bootstrap side-effect reports were produced during Target-27 verification."
                : null));
    invariants.add(
        new SteelHookSafetyInvariant(
            "steelhook-0-2.installation-report-not-produced",
            "absent",
            filePresenceValue(input.hookInstallationResultPath()),
            !Files.exists(input.hookInstallationResultPath()),
            Files.exists(input.hookInstallationResultPath())
                ? "minecraft-hook-installation-result.json must not be present for Target-27."
                : null));
    return invariants;
  }

  private List<SteelHookCapabilityBoundary> capabilityBoundaries() {
    return List.of(
        new SteelHookCapabilityBoundary(
            "method-entry-static-dispatch",
            "supported-in-one-approved-runtime-classloader-path",
            "SteelHook 0.2 supports one approved method-entry static-dispatch runtime classloader definition path."),
        new SteelHookCapabilityBoundary(
            "real-runtime-class-definition",
            "supported-for-one-approved-target",
            "SteelHook 0.2 supports transformed definition of net.minecraft.server.Main only."),
        new SteelHookCapabilityBoundary(
            "minecraft-main-invocation",
            "intentionally-blocked",
            "Minecraft main invocation is still intentionally blocked."),
        new SteelHookCapabilityBoundary(
            "server-launch",
            "intentionally-blocked",
            "Minecraft server launch is still intentionally blocked."),
        new SteelHookCapabilityBoundary(
            "dispatcher-observation",
            "not-required-in-0-2",
            "Dispatcher observation is not required for SteelHook 0.2 completion."),
        new SteelHookCapabilityBoundary(
            "hook-installation",
            "not-supported",
            "Hook installation is not supported in SteelHook 0.2 completion."),
        new SteelHookCapabilityBoundary(
            "stackmaptable-rewriting",
            "not-supported",
            "StackMapTable rewriting is not supported."),
        new SteelHookCapabilityBoundary(
            "method-exit-hooks", "not-supported", "Method-exit hooks are not supported."),
        new SteelHookCapabilityBoundary(
            "cancellable-hooks", "not-supported", "Cancellable hooks are not supported."),
        new SteelHookCapabilityBoundary(
            "callsite-redirects", "not-supported", "Callsite redirects are not supported."),
        new SteelHookCapabilityBoundary(
            "return-value-interception",
            "not-supported",
            "Return-value interception is not supported."),
        new SteelHookCapabilityBoundary(
            "field-hooks", "not-supported", "Field hooks are not supported."),
        new SteelHookCapabilityBoundary(
            "constructor-hooks", "not-supported", "Constructor hooks are not supported."),
        new SteelHookCapabilityBoundary(
            "multi-hook-composition", "not-supported", "Multi-hook composition is not supported."),
        new SteelHookCapabilityBoundary(
            "public-steelhook-api",
            "not-exposed",
            "SteelHook 0.2 does not expose a public SteelHook API."),
        new SteelHookCapabilityBoundary(
            "java-mod-execution-sandboxing",
            "not-claimed",
            "SteelHook 0.2 does not claim Java mod execution sandboxing."),
        new SteelHookCapabilityBoundary(
            "registry-implementation",
            "not-implemented",
            "Registry implementation is not part of SteelHook 0.2."),
        new SteelHookCapabilityBoundary(
            "command-implementation",
            "not-implemented",
            "Command implementation is not part of SteelHook 0.2."),
        new SteelHookCapabilityBoundary(
            "resource-implementation",
            "not-implemented",
            "Resource implementation is not part of SteelHook 0.2."));
  }

  private boolean unsupportedCapabilitiesRemainBlockedVerified() {
    return capabilityBoundaries().stream()
        .skip(2)
        .allMatch(
            boundary ->
                !"supported".equals(boundary.status())
                    && !"supported-for-one-approved-target".equals(boundary.status())
                    && !"supported-in-one-approved-runtime-classloader-path"
                        .equals(boundary.status()));
  }

  private SteelHook02CompletionNextDirection failureDirection(
      SteelHookStageVerification stage7,
      SteelHookStageVerification stage23,
      SteelHookStageVerification stage24,
      SteelHookStageVerification stage25,
      SteelHookStageVerification stage26) {
    if (!stage26.passed()) {
      return SteelHook02CompletionNextDirection.RESTORE_TARGET_26_GATED_RUNTIME_TRANSFORMATION;
    }
    if (!stage7.passed() || !stage23.passed() || !stage24.passed() || !stage25.passed()) {
      return SteelHook02CompletionNextDirection.RESTORE_UPSTREAM_STEELHOOK_0_2_CHAIN;
    }
    return SteelHook02CompletionNextDirection.RESTORE_UPSTREAM_STEELHOOK_0_2_CHAIN;
  }

  private String failedStageIds(List<SteelHookStageVerification> stages) {
    return stages.stream()
        .filter(stage -> !stage.passed())
        .map(SteelHookStageVerification::stageId)
        .reduce((left, right) -> left + "," + right)
        .orElse("none");
  }

  private String failedInvariantIds(List<SteelHookSafetyInvariant> invariants) {
    return invariants.stream()
        .filter(invariant -> !invariant.passed())
        .map(SteelHookSafetyInvariant::id)
        .reduce((left, right) -> left + "," + right)
        .orElse("none");
  }

  private SteelHookStageVerification stage(
      String stageId, String milestoneName, String summary, List<String> failures) {
    return new SteelHookStageVerification(
        stageId,
        milestoneName,
        summary,
        failures.isEmpty(),
        failures.isEmpty() ? null : String.join(" ", failures));
  }

  private List<String> baseFailures(
      String loadFailureReason, JsonObject report, String milestoneName, int schema) {
    List<String> failures = new ArrayList<>();
    if (loadFailureReason != null) {
      failures.add(loadFailureReason);
      return failures;
    }
    if (report == null) {
      failures.add("Missing required " + milestoneName + " report.");
      return failures;
    }
    requireSchema(report, schema, failures, milestoneName + " schema");
    requireString(
        report, "milestoneName", milestoneName, failures, milestoneName + " milestoneName");
    return failures;
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

  private JsonObject findMatchingContract(JsonArray contracts) {
    if (contracts == null) {
      return null;
    }
    for (JsonElement element : contracts) {
      if (!element.isJsonObject()) {
        continue;
      }
      JsonObject object = element.getAsJsonObject();
      if (TARGET_INTERNAL_NAME.equals(stringValue(object, "ownerInternalName"))
          && TARGET_METHOD_NAME.equals(stringValue(object, "memberName"))
          && TARGET_DESCRIPTOR.equals(stringValue(object, "descriptor"))) {
        return object;
      }
    }
    return null;
  }

  private JsonArray array(JsonObject object, String field) {
    if (object == null || !object.has(field) || !object.get(field).isJsonArray()) {
      return null;
    }
    return object.getAsJsonArray(field);
  }

  private JsonObject firstArrayObject(JsonObject object, String field) {
    JsonArray array = array(object, field);
    if (array == null || array.isEmpty() || !array.get(0).isJsonObject()) {
      return null;
    }
    return array.get(0).getAsJsonObject();
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
    } catch (ClassCastException | IllegalStateException exception) {
      return null;
    }
  }

  private Integer integerValue(JsonObject object, String field) {
    if (object == null || !object.has(field) || object.get(field).isJsonNull()) {
      return null;
    }
    try {
      return object.get(field).getAsInt();
    } catch (ClassCastException | IllegalStateException exception) {
      return null;
    }
  }

  private boolean bool(JsonObject object, String field) {
    if (object == null || !object.has(field) || object.get(field).isJsonNull()) {
      return false;
    }
    try {
      return object.get(field).getAsBoolean();
    } catch (ClassCastException | IllegalStateException exception) {
      return false;
    }
  }

  private void requireSchema(JsonObject object, int expected, List<String> failures, String label) {
    Integer actual = integerValue(object, "schema");
    if (actual == null || actual != expected) {
      failures.add(label + " expected " + expected + " but was " + actual + ".");
    }
  }

  private void requireString(
      JsonObject object, String field, String expected, List<String> failures, String label) {
    String actual = stringValue(object, field);
    if (!expected.equals(actual)) {
      failures.add(label + " expected " + expected + " but was " + actual + ".");
    }
  }

  private void requireSameString(
      JsonObject left, JsonObject right, String field, List<String> failures, String label) {
    String leftValue = stringValue(left, field);
    String rightValue = stringValue(right, field);
    if (leftValue == null || rightValue == null || !leftValue.equals(rightValue)) {
      failures.add(label + " must match between Target-25 and Target-26.");
    }
  }

  private void requireOneOfStrings(
      JsonObject object,
      String field,
      List<String> expectedValues,
      List<String> failures,
      String label) {
    String actual = stringValue(object, field);
    if (actual == null || expectedValues.stream().noneMatch(actual::equals)) {
      failures.add(label + " expected one of " + expectedValues + " but was " + actual + ".");
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

  private void requireOptionalBoolean(
      JsonObject object, String field, boolean expected, List<String> failures, String label) {
    if (object != null && object.has(field) && !object.get(field).isJsonNull()) {
      requireBoolean(object, field, expected, failures, label);
    }
  }

  private void requireInt(
      JsonObject object, String field, int expected, List<String> failures, String label) {
    Integer actual = integerValue(object, field);
    if (actual == null || actual != expected) {
      failures.add(label + " expected " + expected + " but was " + actual + ".");
    }
  }

  private void requireIntegerPresent(
      JsonObject object, String field, List<String> failures, String label) {
    if (integerValue(object, field) == null) {
      failures.add(label + " must be present.");
    }
  }

  private void requireNonBlank(
      JsonObject object, String field, List<String> failures, String label) {
    String actual = stringValue(object, field);
    if (actual == null || actual.isBlank()) {
      failures.add(label + " must be nonblank.");
    }
  }

  private SteelHookSafetyInvariant booleanInvariant(
      String id, boolean expected, JsonObject object, String field) {
    boolean actual = bool(object, field);
    return new SteelHookSafetyInvariant(
        id,
        Boolean.toString(expected),
        Boolean.toString(actual),
        actual == expected,
        actual == expected ? null : id + " expected " + expected + " but was " + actual + ".");
  }

  private SteelHookSafetyInvariant negatedBooleanInvariant(
      String id, JsonObject object, String field) {
    boolean actual = bool(object, field);
    return new SteelHookSafetyInvariant(
        id,
        "false",
        Boolean.toString(actual),
        !actual,
        !actual ? null : id + " expected false but was true.");
  }

  private SteelHookSafetyInvariant stringInvariant(String id, String expected, String actual) {
    boolean passed = expected.equals(actual);
    return new SteelHookSafetyInvariant(
        id,
        expected,
        actual,
        passed,
        passed ? null : id + " expected " + expected + " but was " + actual + ".");
  }

  private SteelHookSafetyInvariant noFileAndFalseInvariant(
      String id, JsonObject object, String field, Path path) {
    boolean actualValue = bool(object, field);
    boolean filePresent = Files.exists(path);
    boolean passed = !actualValue && !filePresent;
    return new SteelHookSafetyInvariant(
        id,
        "false/absent",
        Boolean.toString(actualValue) + "/" + filePresenceValue(path),
        passed,
        passed
            ? null
            : id
                + " expected false/absent but observed "
                + actualValue
                + "/"
                + filePresenceValue(path)
                + ".");
  }

  private String filePresenceValue(Path path) {
    return Files.exists(path) ? "present" : "absent";
  }

  private boolean containsRawTransformedClassBytes(JsonObject object) {
    if (object == null) {
      return false;
    }
    for (String key : object.keySet()) {
      if ("transformedClassBytes".equals(key)
          || "rawTransformedClassBytes".equals(key)
          || "classBytes".equals(key)
          || "transformedBytes".equals(key)) {
        return true;
      }
      JsonElement child = object.get(key);
      if (child != null) {
        if (child.isJsonObject() && containsRawTransformedClassBytes(child.getAsJsonObject())) {
          return true;
        }
        if (child.isJsonArray()) {
          for (JsonElement element : child.getAsJsonArray()) {
            if (element.isJsonObject()
                && containsRawTransformedClassBytes(element.getAsJsonObject())) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  private record LoadedReport(JsonObject object, String failureReason) {}
}
