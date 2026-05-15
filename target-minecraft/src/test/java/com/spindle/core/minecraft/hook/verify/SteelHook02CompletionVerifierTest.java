package com.spindle.core.minecraft.hook.verify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SteelHook02CompletionVerifierTest {
  @TempDir Path tempDirectory;

  private static final Gson GSON =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  private final SteelHook02CompletionVerifier verifier = new SteelHook02CompletionVerifier();

  @Test
  void validReportChainPassesTargetTwentySeven() throws Exception {
    writeValidChain(tempDirectory);

    SteelHook02CompletionReport report =
        verifier.verify(SteelHook02CompletionInput.fromWorkingDirectory(tempDirectory));

    assertEquals(SteelHook02CompletionStatus.PASSED, report.status());
    assertEquals("Target-27", report.milestoneName());
    assertEquals("0.2", report.steelHookVersion());
    assertEquals(SteelHook02CompletionHandoffStatus.STEELHOOK_0_2_COMPLETE, report.handoffStatus());
    assertEquals(
        SteelHook02CompletionNextDirection.MOVE_TO_STEELHOOK_0_3_STACKMAP_AND_EXIT_PRIMITIVES,
        report.nextDirection());
    assertTrue(report.completionReady());
    assertTrue(report.reportChainVerified());
    assertEquals(0, report.stageFailureCount());
    assertEquals(0, report.safetyInvariantFailureCount());
    assertEquals("net.minecraft.server.Main", report.targetBinaryName());
    assertEquals("METHOD_ENTRY_STATIC_DISPATCH", report.primitiveKind());
    assertTrue(report.stageVerifications().stream().allMatch(SteelHookStageVerification::passed));
    assertTrue(report.safetyInvariants().stream().allMatch(SteelHookSafetyInvariant::passed));
    assertEquals(
        List.of(
            "target-3-known-contracts",
            "target-5-method-entry-placement",
            "target-6-bytecode-analysis",
            "target-7-patch-plan",
            "target-23-primitive-boundary",
            "target-24-contract-generalization",
            "target-25-method-entry-transformer",
            "target-26-gated-runtime-transformation",
            "target-27-completion"),
        report.stageVerifications().stream().map(SteelHookStageVerification::stageId).toList());
  }

  @Test
  void missingTargetTwentyThreeReportFailsWithUpstreamRestoreDirection() throws Exception {
    writeValidChain(tempDirectory);
    Files.delete(tempDirectory.resolve("minecraft-steelhook-0-2-primitive-boundary.json"));

    SteelHook02CompletionReport report =
        verifier.verify(SteelHook02CompletionInput.fromWorkingDirectory(tempDirectory));

    assertFailedStage(report, "target-23-primitive-boundary");
    assertEquals(
        SteelHook02CompletionNextDirection.RESTORE_UPSTREAM_STEELHOOK_0_2_CHAIN,
        report.nextDirection());
  }

  @Test
  void missingTargetTwentySixReportFailsWithTargetTwentySixRestoreDirection() throws Exception {
    writeValidChain(tempDirectory);
    Files.delete(
        tempDirectory.resolve("minecraft-steelhook-0-2-gated-runtime-transformation-result.json"));

    SteelHook02CompletionReport report =
        verifier.verify(SteelHook02CompletionInput.fromWorkingDirectory(tempDirectory));

    assertFailedStage(report, "target-26-gated-runtime-transformation");
    assertEquals(
        SteelHook02CompletionNextDirection.RESTORE_TARGET_26_GATED_RUNTIME_TRANSFORMATION,
        report.nextDirection());
  }

  @Test
  void malformedJsonFailsDeterministically() throws Exception {
    writeValidChain(tempDirectory);
    Files.writeString(
        tempDirectory.resolve("minecraft-steelhook-0-2-contract-generalization.json"),
        "{ not-json }\n",
        StandardCharsets.UTF_8);

    SteelHook02CompletionReport report =
        verifier.verify(SteelHook02CompletionInput.fromWorkingDirectory(tempDirectory));

    assertFailedStage(report, "target-24-contract-generalization");
    assertNotNull(report.failureSummary());
  }

  @Test
  void targetTwentySixGuardFlagsFailInvariants() throws Exception {
    for (String field :
        List.of(
            "runtimeClassLoadingAttempted",
            "runtimeClassLoadingSucceeded",
            "targetClassDefined",
            "minecraftMainInvoked",
            "minecraftServerLaunched",
            "hookInstallationOccurred",
            "runtimeDispatchOccurred",
            "dispatcherInvocationObserved",
            "publicApiExposed",
            "javaModExecutionSandboxed",
            "javaAgentUsed",
            "mixinUsed",
            "remappingOccurred",
            "accessWidenersUsed")) {
      Path caseDirectory = Files.createDirectory(tempDirectory.resolve(field));
      writeValidChain(caseDirectory);
      mutate(
          caseDirectory,
          "minecraft-steelhook-0-2-gated-runtime-transformation-result.json",
          root ->
              root.addProperty(
                  field,
                  switch (field) {
                    case "runtimeClassLoadingAttempted",
                        "runtimeClassLoadingSucceeded",
                        "targetClassDefined" ->
                        false;
                    default -> true;
                  }));

      SteelHook02CompletionReport report =
          verifier.verify(SteelHook02CompletionInput.fromWorkingDirectory(caseDirectory));

      assertEquals(SteelHook02CompletionStatus.FAILED, report.status(), field);
      assertTrue(report.stageFailureCount() > 0 || report.safetyInvariantFailureCount() > 0, field);
    }
  }

  @Test
  void mismatchedHashesAndInstructionHexFailTargetTwentySix() throws Exception {
    for (String field :
        List.of(
            "originalClassSha256",
            "transformedClassSha256",
            "originalCodeSha256",
            "transformedCodeSha256",
            "insertedInstructionHex")) {
      Path caseDirectory = Files.createDirectory(tempDirectory.resolve("mismatch-" + field));
      writeValidChain(caseDirectory);
      mutate(
          caseDirectory,
          "minecraft-steelhook-0-2-gated-runtime-transformation-result.json",
          root -> root.addProperty(field, "drifted-" + field));

      SteelHook02CompletionReport report =
          verifier.verify(SteelHook02CompletionInput.fromWorkingDirectory(caseDirectory));

      assertFailedStage(report, "target-26-gated-runtime-transformation");
    }
  }

  @Test
  void rawTransformedClassByteFieldsFail() throws Exception {
    for (String fileName :
        List.of(
            "minecraft-steelhook-0-2-method-entry-transformer-result.json",
            "minecraft-steelhook-0-2-gated-runtime-transformation-result.json")) {
      Path caseDirectory = Files.createDirectory(tempDirectory.resolve(fileName.replace('.', '_')));
      writeValidChain(caseDirectory);
      mutate(
          caseDirectory,
          fileName,
          root -> {
            JsonArray bytes = new JsonArray();
            bytes.add(1);
            bytes.add(2);
            root.add("transformedClassBytes", bytes);
          });

      SteelHook02CompletionReport report =
          verifier.verify(SteelHook02CompletionInput.fromWorkingDirectory(caseDirectory));

      assertFalse(
          report.safetyInvariants().stream()
              .filter(
                  invariant ->
                      "steelhook-0-2.no-transformed-byte-arrays-serialized".equals(invariant.id()))
              .findFirst()
              .orElseThrow()
              .passed());
    }
  }

  @Test
  void existingHookInstallationReportFailsTargetTwentySeven() throws Exception {
    writeValidChain(tempDirectory);
    Files.writeString(
        tempDirectory.resolve("minecraft-hook-installation-result.json"),
        "{\n  \"schema\": 1,\n  \"milestoneName\": \"Target-4\"\n}\n",
        StandardCharsets.UTF_8);

    SteelHook02CompletionReport report =
        verifier.verify(SteelHook02CompletionInput.fromWorkingDirectory(tempDirectory));

    assertFailedStage(report, "target-27-completion");
    assertFalse(report.completionReady());
    assertNotNull(report.failureSummary());
  }

  private void assertFailedStage(SteelHook02CompletionReport report, String stageId) {
    assertEquals(SteelHook02CompletionStatus.FAILED, report.status());
    assertFalse(
        report.stageVerifications().stream()
            .filter(stage -> stageId.equals(stage.stageId()))
            .findFirst()
            .orElseThrow()
            .passed());
  }

  private void writeValidChain(Path directory) throws Exception {
    Files.createDirectories(directory);
    write(directory.resolve("minecraft-hook-contracts.json"), validContractsReport());
    write(directory.resolve("minecraft-hook-placement-plan.json"), validPlacementPlan());
    write(
        directory.resolve("minecraft-hook-bytecode-analysis.json"), validBytecodeAnalysisReport());
    write(directory.resolve("minecraft-hook-patch-plan.json"), validPatchPlan());
    write(
        directory.resolve("minecraft-steelhook-0-2-primitive-boundary.json"),
        validTarget23Report());
    write(
        directory.resolve("minecraft-steelhook-0-2-contract-generalization.json"),
        validTarget24Report());
    write(
        directory.resolve("minecraft-steelhook-0-2-method-entry-transformer-result.json"),
        validTarget25Report());
    write(
        directory.resolve("minecraft-steelhook-0-2-gated-runtime-transformation-result.json"),
        validTarget26Report());
  }

  private void mutate(Path directory, String fileName, Consumer<JsonObject> mutator)
      throws Exception {
    Path path = directory.resolve(fileName);
    JsonObject root =
        com.google.gson.JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8))
            .getAsJsonObject();
    mutator.accept(root);
    write(path, root);
  }

  private void write(Path path, JsonObject object) throws IOException {
    Files.writeString(path, GSON.toJson(object), StandardCharsets.UTF_8);
  }

  private JsonObject validContractsReport() {
    JsonObject root = new JsonObject();
    root.addProperty("schema", 2);
    root.addProperty("milestoneName", "Target-3");
    root.addProperty("validationPassed", true);
    root.addProperty("errorCount", 0);
    root.addProperty("publicApiExposed", false);
    root.addProperty("javaModExecutionSandboxed", false);
    JsonArray contracts = new JsonArray();
    JsonObject contract = new JsonObject();
    contract.addProperty("ownerInternalName", "net/minecraft/server/Main");
    contract.addProperty("memberName", "main");
    contract.addProperty("descriptor", "([Ljava/lang/String;)V");
    contract.addProperty("valid", true);
    contracts.add(contract);
    root.add("contracts", contracts);
    return root;
  }

  private JsonObject validPlacementPlan() {
    JsonObject root = new JsonObject();
    root.addProperty("schema", 1);
    root.addProperty("milestoneName", "Target-5");
    root.addProperty("gatePassed", true);
    root.addProperty("placementPlanned", true);
    root.addProperty("plannedPlacementCount", 1);
    JsonArray placements = new JsonArray();
    JsonObject placement = new JsonObject();
    placement.addProperty("ownerInternalName", "net/minecraft/server/Main");
    placement.addProperty("memberName", "main");
    placement.addProperty("descriptor", "([Ljava/lang/String;)V");
    placement.addProperty("placementKind", "METHOD_ENTRY");
    placements.add(placement);
    root.add("plannedPlacements", placements);
    return root;
  }

  private JsonObject validBytecodeAnalysisReport() {
    JsonObject root = new JsonObject();
    root.addProperty("schema", 1);
    root.addProperty("milestoneName", "Target-6");
    root.addProperty("gatePassed", true);
    root.addProperty("bytecodeAnalysisSucceeded", true);
    root.addProperty("instructionBoundaryValidationPassed", true);
    root.addProperty("branchTargetValidationPassed", true);
    root.addProperty("switchTargetValidationPassed", true);
    root.addProperty("exceptionTableValidationPassed", true);
    root.addProperty("ownerInternalName", "net/minecraft/server/Main");
    root.addProperty("memberName", "main");
    root.addProperty("descriptor", "([Ljava/lang/String;)V");
    return root;
  }

  private JsonObject validPatchPlan() {
    JsonObject root = new JsonObject();
    root.addProperty("schema", 1);
    root.addProperty("milestoneName", "Target-7");
    root.addProperty("gatePassed", true);
    root.addProperty("patchPlanningSucceeded", true);
    root.addProperty("patchPlanned", true);
    root.addProperty("plannedPatchCount", 1);
    root.addProperty("patchKind", "METHOD_ENTRY_STATIC_DISPATCH");
    root.addProperty("patchMode", "DRY_RUN_STATIC_DISPATCH_INVOKESTATIC");
    root.addProperty("patchEligibility", "FIXTURE_ONLY_FUTURE_TRANSFORM");
    root.addProperty("targetClass", "net/minecraft/server/Main");
    root.addProperty("targetMethod", "main");
    root.addProperty("targetDescriptor", "([Ljava/lang/String;)V");
    root.addProperty("insertionOffset", 0);
    root.addProperty("transformReadyForFixtureOnly", true);
    root.addProperty("transformReadyForMinecraftRuntime", false);
    root.addProperty("injectionOccurred", false);
    root.addProperty("transformationOccurred", false);
    root.addProperty("patchingOccurred", false);
    root.addProperty("bytecodeModified", false);
    root.addProperty("publicApiExposed", false);
    root.addProperty("javaModExecutionSandboxed", false);
    root.addProperty("mixinUsed", false);
    root.addProperty("javaAgentUsed", false);
    return root;
  }

  private JsonObject validTarget23Report() {
    JsonObject root = new JsonObject();
    root.addProperty("schema", 1);
    root.addProperty("milestoneName", "Target-23");
    root.addProperty("target", "minecraft");
    root.addProperty("steelHookVersion", "0.2");
    root.addProperty("analysisOnly", true);
    root.addProperty("classLoadingOccurred", false);
    root.addProperty("injectionOccurred", false);
    root.addProperty("transformationOccurred", false);
    root.addProperty("patchingOccurred", false);
    root.addProperty("bytecodeModified", false);
    root.addProperty("hookInstallationOccurred", false);
    root.addProperty("runtimeDispatchOccurred", false);
    root.addProperty("publicApiExposed", false);
    root.addProperty("javaModExecutionSandboxed", false);
    root.addProperty("supportedPrimitiveCount", 1);
    root.addProperty("approvedCandidateCount", 1);
    root.addProperty("gatePassed", true);
    root.addProperty("boundaryStatus", "PRIMITIVE_BOUNDARY_SELECTED");
    root.addProperty("nextDirection", "MOVE_TO_TARGET_24_CONTRACT_AND_PATCH_PLAN_GENERALIZATION");
    JsonArray candidates = new JsonArray();
    JsonObject candidate = new JsonObject();
    candidate.addProperty("primitiveKind", "METHOD_ENTRY_STATIC_DISPATCH");
    candidate.addProperty("eligibleForTarget24ContractGeneralization", true);
    candidate.addProperty("eligibleForTarget25TransformerExtraction", true);
    candidate.addProperty("eligibleForTarget26RuntimeTransformation", false);
    candidates.add(candidate);
    root.add("candidates", candidates);
    return root;
  }

  private JsonObject validTarget24Report() {
    JsonObject root = new JsonObject();
    root.addProperty("schema", 1);
    root.addProperty("milestoneName", "Target-24");
    root.addProperty("analysisOnly", true);
    root.addProperty("classLoadingOccurred", false);
    root.addProperty("injectionOccurred", false);
    root.addProperty("transformationOccurred", false);
    root.addProperty("patchingOccurred", false);
    root.addProperty("bytecodeModified", false);
    root.addProperty("hookInstallationOccurred", false);
    root.addProperty("runtimeDispatchOccurred", false);
    root.addProperty("publicApiExposed", false);
    root.addProperty("javaModExecutionSandboxed", false);
    root.addProperty("contractGeneralizationOccurred", true);
    root.addProperty("contractGeneralizationReady", true);
    root.addProperty("minecraftRuntimeTransformReady", false);
    root.addProperty("eligibleForTarget25TransformerExtraction", true);
    root.addProperty("eligibleForTarget26RuntimeTransformation", false);
    root.addProperty("gatePassed", true);
    root.addProperty("status", "CONTRACT_GENERALIZATION_READY");
    root.addProperty("nextDirection", "MOVE_TO_TARGET_25_RUNTIME_SAFE_METHOD_ENTRY_TRANSFORMER");

    JsonObject targetDescriptor = new JsonObject();
    targetDescriptor.addProperty("ownerInternalName", "net/minecraft/server/Main");
    targetDescriptor.addProperty("memberName", "main");
    targetDescriptor.addProperty("descriptor", "([Ljava/lang/String;)V");
    root.add("targetDescriptor", targetDescriptor);

    JsonObject dispatcherDescriptor = new JsonObject();
    dispatcherDescriptor.addProperty(
        "binaryName", "com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher");
    dispatcherDescriptor.addProperty("methodName", "beforeMinecraftServerMain");
    dispatcherDescriptor.addProperty("descriptor", "()V");
    root.add("dispatcherDescriptor", dispatcherDescriptor);

    JsonObject primitiveContract = new JsonObject();
    primitiveContract.addProperty("primitiveKind", "METHOD_ENTRY_STATIC_DISPATCH");
    root.add("primitiveContract", primitiveContract);

    JsonObject generalizedPatchPlan = new JsonObject();
    generalizedPatchPlan.addProperty(
        "patchMode", "steelhook-0-2-contract-generalized-static-dispatch-invokestatic");
    generalizedPatchPlan.addProperty(
        "patchEligibility", "steelhook-0-2-contract-ready-runtime-candidate");
    root.add("generalizedPatchPlan", generalizedPatchPlan);
    return root;
  }

  private JsonObject validTarget25Report() {
    JsonObject root = new JsonObject();
    root.addProperty("schema", 1);
    root.addProperty("milestoneName", "Target-25");
    root.addProperty("gatePassed", true);
    root.addProperty("status", "TRANSFORMED");
    root.addProperty("nextDirection", "MOVE_TO_TARGET_26_GATED_REAL_RUNTIME_TRANSFORMATION");
    root.addProperty("localTransformationOnly", true);
    root.addProperty("runtimeClassLoadingPathEnabled", false);
    root.addProperty("classLoadingOccurred", false);
    root.addProperty("hookInstallationOccurred", false);
    root.addProperty("runtimeDispatchOccurred", false);
    root.addProperty("realMinecraftRuntimeTransformed", false);
    root.addProperty("publicApiExposed", false);
    root.addProperty("javaModExecutionSandboxed", false);
    root.addProperty("minecraftRuntimeTransformReady", false);
    root.addProperty("target25TransformerExtractionOccurred", true);
    root.addProperty("methodEntryTransformationOccurred", true);
    root.addProperty("bytecodeModified", true);
    root.addProperty("transformedClassBytesProduced", true);
    root.addProperty("eligibleForTarget26GatedRuntimeTransformation", true);
    root.addProperty("originalClassSha256", "original-class");
    root.addProperty("transformedClassSha256", "transformed-class");
    root.addProperty("originalCodeSha256", "original-code");
    root.addProperty("transformedCodeSha256", "transformed-code");
    root.addProperty("originalCodeLength", 4);
    root.addProperty("transformedCodeLength", 7);
    root.addProperty("constantPoolCountBefore", 10);
    root.addProperty("constantPoolCountAfter", 11);
    root.addProperty("methodrefIndex", 44);
    root.addProperty("insertedInstructionHex", "b8002c");
    return root;
  }

  private JsonObject validTarget26Report() {
    JsonObject root = new JsonObject();
    root.addProperty("schema", 1);
    root.addProperty("milestoneName", "Target-26");
    root.addProperty("gatePassed", true);
    root.addProperty("status", "TRANSFORMED_AND_DEFINED");
    root.addProperty("nextDirection", "MOVE_TO_TARGET_27_STEELHOOK_0_2_COMPLETION");
    root.addProperty("runtimeClassLoadingPathEnabled", true);
    root.addProperty("runtimeClassLoadingAttempted", true);
    root.addProperty("runtimeClassLoadingSucceeded", true);
    root.addProperty("classLoadingOccurred", true);
    root.addProperty("targetClassDefined", true);
    root.addProperty("targetBinaryName", "net.minecraft.server.Main");
    root.addProperty("targetClassEntryName", "net/minecraft/server/Main.class");
    root.addProperty("definedClassName", "net.minecraft.server.Main");
    root.addProperty("definedBySteelHookRuntimeClassLoader", true);
    root.addProperty("realMinecraftRuntimeTransformed", true);
    root.addProperty("methodEntryTransformationOccurred", true);
    root.addProperty("bytecodeModified", true);
    root.addProperty("transformedClassBytesProduced", true);
    root.addProperty("minecraftMainInvoked", false);
    root.addProperty("minecraftServerLaunched", false);
    root.addProperty("hookInstallationOccurred", false);
    root.addProperty("runtimeDispatchOccurred", false);
    root.addProperty("dispatcherInvocationObserved", false);
    root.addProperty("publicApiExposed", false);
    root.addProperty("javaAgentUsed", false);
    root.addProperty("mixinUsed", false);
    root.addProperty("remappingOccurred", false);
    root.addProperty("accessWidenersUsed", false);
    root.addProperty("javaModExecutionSandboxed", false);
    root.addProperty("minecraftRuntimeTransformReady", true);
    root.addProperty("eligibleForTarget27CompletionVerification", true);
    root.addProperty("originalClassSha256", "original-class");
    root.addProperty("transformedClassSha256", "transformed-class");
    root.addProperty("originalCodeSha256", "original-code");
    root.addProperty("transformedCodeSha256", "transformed-code");
    root.addProperty("insertedInstructionHex", "b8002c");
    return root;
  }
}
