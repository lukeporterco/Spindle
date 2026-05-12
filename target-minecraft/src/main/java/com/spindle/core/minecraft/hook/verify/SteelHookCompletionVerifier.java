package com.spindle.core.minecraft.hook.verify;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class SteelHookCompletionVerifier {
  private static final String TARGET_INTERNAL_NAME = "net/minecraft/server/Main";
  private static final String TARGET_BINARY_NAME = "net.minecraft.server.Main";
  private static final String TARGET_METHOD_NAME = "main";
  private static final String TARGET_DESCRIPTOR = "([Ljava/lang/String;)V";
  private static final String TARGET_CONTRACT_ID = "minecraft.26_1_2.server.main.entrypoint";
  private static final String SUPPORTED_STAGE_8_STATUS = "transformed";

  public SteelHookCompletionReport verify(SteelHookCompletionInput input) {
    LoadedReport contracts =
        loadRequired(input.hookContractsReportPath(), "minecraft-hook-contracts.json");
    LoadedReport placement =
        loadRequired(input.hookPlacementPlanPath(), "minecraft-hook-placement-plan.json");
    LoadedReport bytecode =
        loadRequired(
            input.hookBytecodeAnalysisReportPath(), "minecraft-hook-bytecode-analysis.json");
    LoadedReport patchPlan =
        loadRequired(input.hookPatchPlanPath(), "minecraft-hook-patch-plan.json");
    LoadedReport bootstrapTransform =
        loadRequired(
            input.bootstrapTransformationResultPath(),
            "minecraft-hook-bootstrap-transformation-result.json");
    LoadedReport bootstrapResult =
        loadRequired(input.serverBootstrapResultPath(), "minecraft-server-bootstrap-result.json");
    LoadedReport executionResult =
        loadRequired(input.modExecutionResultPath(), "minecraft-mod-execution-result.json");

    SteelHookStageVerification target3 =
        verifyTarget3KnownContracts(contracts.object(), contracts.failureReason());
    SteelHookStageVerification target5 =
        verifyTarget5Placement(placement.object(), placement.failureReason());
    SteelHookStageVerification target6 =
        verifyTarget6BytecodeAnalysis(bytecode.object(), bytecode.failureReason());
    SteelHookStageVerification target7 =
        verifyTarget7PatchPlan(patchPlan.object(), patchPlan.failureReason());
    SteelHookStageVerification target8 =
        verifyTarget8FixturePrimitive(
            bootstrapTransform.object(), bootstrapTransform.failureReason());
    SteelHookStageVerification target9 =
        verifyTarget9BootstrapTransform(
            bootstrapTransform.object(),
            bootstrapTransform.failureReason(),
            bootstrapResult.object(),
            bootstrapResult.failureReason(),
            executionResult.object(),
            executionResult.failureReason());

    List<SteelHookSafetyInvariant> safetyInvariants =
        buildSafetyInvariants(
            patchPlan.object(),
            patchPlan.failureReason(),
            bootstrapTransform.object(),
            bootstrapTransform.failureReason(),
            input.hookInstallationResultPath());
    long safetyFailureCount =
        safetyInvariants.stream().filter(invariant -> !invariant.passed()).count();
    SteelHookStageVerification target10 =
        new SteelHookStageVerification(
            "target-10-completion",
            "Target-10",
            "All SteelHook 0.1 safety invariants passed.",
            safetyFailureCount == 0,
            safetyFailureCount == 0
                ? null
                : "Safety invariant failures: "
                    + safetyInvariants.stream()
                        .filter(invariant -> !invariant.passed())
                        .map(SteelHookSafetyInvariant::id)
                        .toList());

    List<SteelHookStageVerification> stages =
        List.of(target3, target5, target6, target7, target8, target9, target10);
    long stageFailureCount = stages.stream().filter(stage -> !stage.passed()).count();
    boolean reportChainVerified =
        contracts.object() != null
            && placement.object() != null
            && bytecode.object() != null
            && patchPlan.object() != null
            && bootstrapTransform.object() != null
            && bootstrapResult.object() != null
            && executionResult.object() != null;

    return new SteelHookCompletionReport(
        1,
        "Target-10",
        "0.1",
        stageFailureCount == 0 && safetyFailureCount == 0
            ? SteelHookCompletionStatus.PASSED
            : SteelHookCompletionStatus.FAILED,
        reportChainVerified,
        (int) stageFailureCount,
        (int) safetyFailureCount,
        stages,
        safetyInvariants,
        capabilityBoundaries());
  }

  private SteelHookStageVerification verifyTarget3KnownContracts(
      JsonObject report, String loadFailureReason) {
    List<String> failures = baseFailures(loadFailureReason, report, "Target-3", 1);
    if (loadFailureReason == null && report != null) {
      failures.clear();
      requireSchema(report, 2, failures, "Target-3 schema");
      requireString(report, "milestoneName", "Target-3", failures, "Target-3 milestoneName");
    }
    if (report != null) {
      requireBoolean(report, "validationPassed", true, failures, "Target-3 validationPassed");
      requireInt(report, "errorCount", 0, failures, "Target-3 errorCount");
      JsonObject matchedContract = findMatchingContract(report.getAsJsonArray("contracts"));
      if (matchedContract == null) {
        failures.add(
            "Target-3 requires contract for net/minecraft/server/Main.main([Ljava/lang/String;)V.");
      } else {
        requireBoolean(matchedContract, "valid", true, failures, "Target-3 contract valid");
        if (stringValue(matchedContract, "memberName") != null) {
          requireString(
              matchedContract,
              "memberName",
              TARGET_METHOD_NAME,
              failures,
              "Target-3 contract memberName");
        }
        if (stringValue(matchedContract, "descriptor") != null) {
          requireString(
              matchedContract,
              "descriptor",
              TARGET_DESCRIPTOR,
              failures,
              "Target-3 contract descriptor");
        }
      }
    }
    return stage(
        "target-3-known-contracts",
        "Target-3",
        "Known-symbol hook contract validation passed.",
        failures);
  }

  private SteelHookStageVerification verifyTarget5Placement(
      JsonObject report, String loadFailureReason) {
    List<String> failures = baseFailures(loadFailureReason, report, "Target-5", 1);
    if (report != null) {
      requireBoolean(report, "gatePassed", true, failures, "Target-5 gatePassed");
      requireBoolean(report, "placementPlanned", true, failures, "Target-5 placementPlanned");
      requireInt(report, "plannedPlacementCount", 1, failures, "Target-5 plannedPlacementCount");
      JsonObject placement = firstArrayObject(report, "plannedPlacements");
      if (placement == null) {
        failures.add("Target-5 requires exactly one planned placement.");
      } else {
        requireString(
            placement,
            "ownerInternalName",
            TARGET_INTERNAL_NAME,
            failures,
            "Target-5 ownerInternalName");
        requireString(placement, "memberName", TARGET_METHOD_NAME, failures, "Target-5 memberName");
        requireString(placement, "descriptor", TARGET_DESCRIPTOR, failures, "Target-5 descriptor");
      }
    }
    return stage("target-5-placement", "Target-5", "Method-entry placement planned.", failures);
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
        "Bytecode analysis succeeded and boundary validations passed.",
        failures);
  }

  private SteelHookStageVerification verifyTarget7PatchPlan(
      JsonObject report, String loadFailureReason) {
    List<String> failures = baseFailures(loadFailureReason, report, "Target-7", 1);
    if (report != null) {
      requireBoolean(report, "gatePassed", true, failures, "Target-7 gatePassed");
      requireBoolean(
          report, "patchPlanningSucceeded", true, failures, "Target-7 patchPlanningSucceeded");
      requireBoolean(report, "patchPlanned", true, failures, "Target-7 patchPlanned");
      requireInt(report, "plannedPatchCount", 1, failures, "Target-7 plannedPatchCount");
      requireString(report, "targetClass", TARGET_INTERNAL_NAME, failures, "Target-7 targetClass");
      requireString(report, "targetMethod", TARGET_METHOD_NAME, failures, "Target-7 targetMethod");
      requireString(
          report, "targetDescriptor", TARGET_DESCRIPTOR, failures, "Target-7 targetDescriptor");
      requireBoolean(
          report,
          "transformReadyForMinecraftRuntime",
          false,
          failures,
          "Target-7 transformReadyForMinecraftRuntime");
    }
    return stage("target-7-patch-plan", "Target-7", "Dry-run patch plan succeeded.", failures);
  }

  private SteelHookStageVerification verifyTarget8FixturePrimitive(
      JsonObject report, String loadFailureReason) {
    List<String> failures = new ArrayList<>();
    if (loadFailureReason != null) {
      failures.add(loadFailureReason);
    }
    if (report != null) {
      requireString(
          report,
          "fixtureTransformationStatus",
          SUPPORTED_STAGE_8_STATUS,
          failures,
          "Target-8 fixtureTransformationStatus");
      requireString(
          report,
          "transformationMode",
          "bootstrap-fake-server-method-entry-transform",
          failures,
          "Target-9 transformationMode");
    }
    return stage(
        "target-8-fixture-transform-primitive",
        "Target-8",
        "Fixture transformer is test-only and embedded through the Target-9 transformer path.",
        failures);
  }

  private SteelHookStageVerification verifyTarget9BootstrapTransform(
      JsonObject transformReport,
      String transformLoadFailureReason,
      JsonObject bootstrapReport,
      String bootstrapLoadFailureReason,
      JsonObject executionReport,
      String executionLoadFailureReason) {
    List<String> failures =
        baseFailures(transformLoadFailureReason, transformReport, "Target-9", 1);
    if (bootstrapLoadFailureReason != null) {
      failures.add(bootstrapLoadFailureReason);
    }
    if (executionLoadFailureReason != null) {
      failures.add(executionLoadFailureReason);
    }
    if (transformReport != null) {
      requireString(
          transformReport, "status", "transformed", failures, "Target-9 transformation status");
      requireBooleanPath(transformReport, "gate", "passed", true, failures, "Target-9 gate.passed");
      requireString(
          transformReport,
          "targetBinaryName",
          TARGET_BINARY_NAME,
          failures,
          "Target-9 targetBinaryName");
      requireString(
          transformReport,
          "targetInternalName",
          TARGET_INTERNAL_NAME,
          failures,
          "Target-9 targetInternalName");
      requireString(
          transformReport, "targetMethod", TARGET_METHOD_NAME, failures, "Target-9 targetMethod");
      requireString(
          transformReport,
          "targetDescriptor",
          TARGET_DESCRIPTOR,
          failures,
          "Target-9 targetDescriptor");
      requireInt(
          transformReport,
          "dispatcherInvocationCount",
          1,
          failures,
          "Target-9 dispatcherInvocationCount");
      requireBoolean(
          transformReport,
          "dispatcherInvocationObserved",
          true,
          failures,
          "Target-9 dispatcherInvocationObserved");
      requireBoolean(
          transformReport,
          "fakeServerRuntimeTransformed",
          true,
          failures,
          "Target-9 fakeServerRuntimeTransformed");
      requireBoolean(
          transformReport, "minecraftMainInvoked", true, failures, "Target-9 minecraftMainInvoked");
    }
    if (bootstrapReport != null) {
      requireSchema(bootstrapReport, 1, failures, "bootstrap result schema");
      requireString(
          bootstrapReport,
          "milestoneName",
          "Milestone 8",
          failures,
          "bootstrap result milestoneName");
      requireInt(bootstrapReport, "exitCode", 0, failures, "bootstrap result exitCode");
      requireBoolean(
          bootstrapReport,
          "minecraftMainInvoked",
          true,
          failures,
          "bootstrap result minecraftMainInvoked");
      requireBoolean(
          bootstrapReport,
          "bytecodeTransformationUsed",
          true,
          failures,
          "bootstrap result bytecodeTransformationUsed");
    }
    if (executionReport != null) {
      requireSchema(executionReport, 1, failures, "mod execution result schema");
      requireString(
          executionReport,
          "milestoneName",
          "Milestone 8",
          failures,
          "mod execution result milestoneName");
      requireInt(
          executionReport, "processOutcome", 0, failures, "mod execution result processOutcome");
      requireBoolean(
          executionReport,
          "minecraftMainInvoked",
          true,
          failures,
          "mod execution result minecraftMainInvoked");
    }
    return stage(
        "target-9-bootstrap-transform",
        "Target-9",
        "Fake-server bootstrap transformation succeeded and dispatcher was invoked exactly once.",
        failures);
  }

  private List<SteelHookSafetyInvariant> buildSafetyInvariants(
      JsonObject patchPlan,
      String patchPlanLoadFailureReason,
      JsonObject transformReport,
      String transformLoadFailureReason,
      Path hookInstallationResultPath) {
    List<SteelHookSafetyInvariant> invariants = new ArrayList<>();
    invariants.add(
        booleanInvariant(
            "realMinecraftRuntimeTransformed",
            false,
            transformReport,
            "realMinecraftRuntimeTransformed",
            transformLoadFailureReason));
    invariants.add(
        booleanInvariant(
            "publicApiExposed",
            false,
            transformReport,
            "publicApiExposed",
            transformLoadFailureReason));
    invariants.add(
        booleanInvariant(
            "javaAgentUsed", false, transformReport, "javaAgentUsed", transformLoadFailureReason));
    invariants.add(
        booleanInvariant(
            "mixinUsed", false, transformReport, "mixinUsed", transformLoadFailureReason));
    invariants.add(
        booleanInvariant(
            "remappingOccurred",
            false,
            transformReport,
            "remappingOccurred",
            transformLoadFailureReason));
    invariants.add(
        booleanInvariant(
            "accessWidenersUsed",
            false,
            transformReport,
            "accessWidenersUsed",
            transformLoadFailureReason));
    invariants.add(
        booleanInvariant(
            "javaModExecutionSandboxed",
            false,
            transformReport,
            "javaModExecutionSandboxed",
            transformLoadFailureReason));
    invariants.add(
        booleanInvariant(
            "bootstrapTransformationEnabled",
            true,
            transformReport,
            "bootstrapTransformationEnabled",
            transformLoadFailureReason));
    invariants.add(
        booleanInvariant(
            "runtimeClassLoaderTransformationEnabled",
            true,
            transformReport,
            "runtimeClassLoaderTransformationEnabled",
            transformLoadFailureReason));
    invariants.add(
        booleanInvariant(
            "fakeServerRuntimeTransformed",
            true,
            transformReport,
            "fakeServerRuntimeTransformed",
            transformLoadFailureReason));
    invariants.add(
        booleanInvariant(
            "bytecodeModified",
            true,
            transformReport,
            "bytecodeModified",
            transformLoadFailureReason));
    invariants.add(
        booleanInvariant(
            "dispatcherInvocationObserved",
            true,
            transformReport,
            "dispatcherInvocationObserved",
            transformLoadFailureReason));
    invariants.add(
        intInvariant(
            "dispatcherInvocationCount",
            1,
            transformReport,
            "dispatcherInvocationCount",
            transformLoadFailureReason));
    invariants.add(
        booleanInvariant(
            "minecraftMainInvoked",
            true,
            transformReport,
            "minecraftMainInvoked",
            transformLoadFailureReason));
    invariants.add(
        booleanInvariant(
            "transformReadyForMinecraftRuntime",
            false,
            patchPlan,
            "transformReadyForMinecraftRuntime",
            patchPlanLoadFailureReason));
    boolean installationResultPresent =
        hookInstallationResultPath != null && Files.isRegularFile(hookInstallationResultPath);
    invariants.add(
        new SteelHookSafetyInvariant(
            "noTarget4HookInstallationResultWritten",
            "false",
            Boolean.toString(installationResultPresent),
            !installationResultPresent,
            installationResultPresent
                ? "Target-10 completion check must not write minecraft-hook-installation-result.json."
                : null));
    return invariants;
  }

  private List<SteelHookCapabilityBoundary> capabilityBoundaries() {
    return List.of(
        new SteelHookCapabilityBoundary(
            "steelhook-0.1", "supported", "Narrow fake-server method-entry transform proof only."),
        new SteelHookCapabilityBoundary(
            "real-minecraft-runtime-transformation",
            "not-supported",
            "Real Minecraft runtime artifacts are not transformed."),
        new SteelHookCapabilityBoundary(
            "public-hook-api",
            "not-supported",
            "SteelHook 0.1 remains an internal implementation detail."),
        new SteelHookCapabilityBoundary(
            "java-mod-sandboxing",
            "not-supported",
            "Passing SteelHook verification does not sandbox Java mod execution."));
  }

  private SteelHookStageVerification stage(
      String stageId, String milestoneName, String summary, List<String> failures) {
    return new SteelHookStageVerification(
        stageId,
        milestoneName,
        summary,
        failures.isEmpty(),
        failures.isEmpty() ? null : String.join("; ", failures));
  }

  private List<String> baseFailures(
      String loadFailureReason, JsonObject report, String expectedMilestone, int expectedSchema) {
    List<String> failures = new ArrayList<>();
    if (loadFailureReason != null) {
      failures.add(loadFailureReason);
      return failures;
    }
    requireSchema(report, expectedSchema, failures, expectedMilestone + " schema");
    requireString(
        report, "milestoneName", expectedMilestone, failures, expectedMilestone + " milestoneName");
    return failures;
  }

  private LoadedReport loadRequired(Path path, String fileName) {
    if (path == null || !Files.isRegularFile(path)) {
      return new LoadedReport(null, "Missing required report " + fileName + ".");
    }
    try {
      JsonElement element = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8));
      if (!element.isJsonObject()) {
        return new LoadedReport(null, "Malformed report " + fileName + ": expected JSON object.");
      }
      return new LoadedReport(element.getAsJsonObject(), null);
    } catch (IOException | RuntimeException exception) {
      return new LoadedReport(
          null, "Malformed report " + fileName + ": " + exception.getClass().getSimpleName() + ".");
    }
  }

  private void requireSchema(JsonObject object, int expected, List<String> failures, String label) {
    if (object == null) {
      return;
    }
    Integer actual = intValue(object, "schema");
    if (actual == null || actual != expected) {
      failures.add(
          label + " expected " + expected + " but was " + valueString(object, "schema") + ".");
    }
  }

  private void requireString(
      JsonObject object, String fieldName, String expected, List<String> failures, String label) {
    String actual = stringValue(object, fieldName);
    if (!expected.equals(actual)) {
      failures.add(label + " expected " + expected + " but was " + printable(actual) + ".");
    }
  }

  private void requireBoolean(
      JsonObject object, String fieldName, boolean expected, List<String> failures, String label) {
    Boolean actual = booleanValue(object, fieldName);
    if (actual == null || actual != expected) {
      failures.add(
          label + " expected " + expected + " but was " + valueString(object, fieldName) + ".");
    }
  }

  private void requireBooleanPath(
      JsonObject object,
      String objectField,
      String fieldName,
      boolean expected,
      List<String> failures,
      String label) {
    JsonObject child = childObject(object, objectField);
    if (child == null) {
      failures.add(label + " expected " + expected + " but was missing.");
      return;
    }
    requireBoolean(child, fieldName, expected, failures, label);
  }

  private void requireInt(
      JsonObject object, String fieldName, int expected, List<String> failures, String label) {
    Integer actual = intValue(object, fieldName);
    if (actual == null || actual != expected) {
      failures.add(
          label + " expected " + expected + " but was " + valueString(object, fieldName) + ".");
    }
  }

  private SteelHookSafetyInvariant booleanInvariant(
      String id, boolean expected, JsonObject object, String fieldName, String loadFailureReason) {
    if (loadFailureReason != null) {
      return new SteelHookSafetyInvariant(
          id, Boolean.toString(expected), "missing", false, loadFailureReason);
    }
    Boolean actual = booleanValue(object, fieldName);
    return new SteelHookSafetyInvariant(
        id,
        Boolean.toString(expected),
        actual == null ? "missing" : actual.toString(),
        actual != null && actual == expected,
        actual != null && actual == expected
            ? null
            : id + " expected " + expected + " but was " + (actual == null ? "missing" : actual));
  }

  private SteelHookSafetyInvariant intInvariant(
      String id, int expected, JsonObject object, String fieldName, String loadFailureReason) {
    if (loadFailureReason != null) {
      return new SteelHookSafetyInvariant(
          id, Integer.toString(expected), "missing", false, loadFailureReason);
    }
    Integer actual = intValue(object, fieldName);
    return new SteelHookSafetyInvariant(
        id,
        Integer.toString(expected),
        actual == null ? "missing" : actual.toString(),
        actual != null && actual == expected,
        actual != null && actual == expected
            ? null
            : id + " expected " + expected + " but was " + (actual == null ? "missing" : actual));
  }

  private JsonObject firstArrayObject(JsonObject object, String fieldName) {
    if (object == null || !object.has(fieldName) || !object.get(fieldName).isJsonArray()) {
      return null;
    }
    if (object.getAsJsonArray(fieldName).size() != 1) {
      return null;
    }
    JsonElement element = object.getAsJsonArray(fieldName).get(0);
    return element.isJsonObject() ? element.getAsJsonObject() : null;
  }

  private JsonObject findMatchingObject(
      com.google.gson.JsonArray array, String fieldName, String expectedValue) {
    if (array == null) {
      return null;
    }
    for (JsonElement element : array) {
      if (!element.isJsonObject()) {
        continue;
      }
      JsonObject object = element.getAsJsonObject();
      if (expectedValue.equals(stringValue(object, fieldName))) {
        return object;
      }
    }
    return null;
  }

  private JsonObject findMatchingContract(com.google.gson.JsonArray array) {
    if (array == null) {
      return null;
    }
    for (JsonElement element : array) {
      if (!element.isJsonObject()) {
        continue;
      }
      JsonObject object = element.getAsJsonObject();
      if (TARGET_CONTRACT_ID.equals(stringValue(object, "id"))
          || TARGET_INTERNAL_NAME.equals(stringValue(object, "ownerInternalName"))) {
        return object;
      }
    }
    return null;
  }

  private JsonObject childObject(JsonObject object, String fieldName) {
    if (object == null || !object.has(fieldName) || !object.get(fieldName).isJsonObject()) {
      return null;
    }
    return object.getAsJsonObject(fieldName);
  }

  private String stringValue(JsonObject object, String fieldName) {
    if (object == null || !object.has(fieldName) || object.get(fieldName).isJsonNull()) {
      return null;
    }
    try {
      return object.get(fieldName).getAsString();
    } catch (RuntimeException exception) {
      return null;
    }
  }

  private Boolean booleanValue(JsonObject object, String fieldName) {
    if (object == null || !object.has(fieldName) || object.get(fieldName).isJsonNull()) {
      return null;
    }
    try {
      return object.get(fieldName).getAsBoolean();
    } catch (RuntimeException exception) {
      return null;
    }
  }

  private Integer intValue(JsonObject object, String fieldName) {
    if (object == null || !object.has(fieldName) || object.get(fieldName).isJsonNull()) {
      return null;
    }
    try {
      return object.get(fieldName).getAsInt();
    } catch (RuntimeException exception) {
      return null;
    }
  }

  private String valueString(JsonObject object, String fieldName) {
    if (object == null || !object.has(fieldName)) {
      return "missing";
    }
    try {
      JsonElement element = object.get(fieldName);
      return element == null || element.isJsonNull() ? "null" : element.toString();
    } catch (RuntimeException exception) {
      return "invalid";
    }
  }

  private String printable(String value) {
    return value == null ? "missing" : value;
  }

  private record LoadedReport(JsonObject object, String failureReason) {}
}
