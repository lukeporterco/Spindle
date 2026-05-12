package com.spindle.core.minecraft.hook.verify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SteelHookCompletionVerifierTest {
  @TempDir Path tempDirectory;

  private static final Gson GSON =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  private final SteelHookCompletionVerifier verifier = new SteelHookCompletionVerifier();

  @Test
  void validTargetNineChainProducesPassedCompletionReport() throws Exception {
    writeValidChain(tempDirectory);

    SteelHookCompletionReport report =
        verifier.verify(SteelHookCompletionInput.fromWorkingDirectory(tempDirectory));

    assertEquals(SteelHookCompletionStatus.PASSED, report.status());
    assertTrue(report.reportChainVerified());
    assertEquals(
        List.of(
            "target-3-known-contracts",
            "target-5-placement",
            "target-6-bytecode-analysis",
            "target-7-patch-plan",
            "target-8-fixture-transform-primitive",
            "target-9-bootstrap-transform",
            "target-10-completion"),
        report.stageVerifications().stream().map(SteelHookStageVerification::stageId).toList());
    assertTrue(report.stageVerifications().stream().allMatch(SteelHookStageVerification::passed));
    assertTrue(report.safetyInvariants().stream().allMatch(SteelHookSafetyInvariant::passed));
  }

  @Test
  void missingContractReportFailsTargetThree() throws Exception {
    writeValidChain(tempDirectory);
    Files.delete(tempDirectory.resolve("minecraft-hook-contracts.json"));

    assertFailedStage(
        verifier.verify(SteelHookCompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-3-known-contracts");
  }

  @Test
  void failedPlacementGateFailsTargetFive() throws Exception {
    writeValidChain(tempDirectory);
    mutate("minecraft-hook-placement-plan.json", root -> root.addProperty("gatePassed", false));

    assertFailedStage(
        verifier.verify(SteelHookCompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-5-placement");
  }

  @Test
  void failedBytecodeAnalysisFailsTargetSix() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-hook-bytecode-analysis.json",
        root -> root.addProperty("bytecodeAnalysisSucceeded", false));

    assertFailedStage(
        verifier.verify(SteelHookCompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-6-bytecode-analysis");
  }

  @Test
  void failedPatchPlanFailsTargetSeven() throws Exception {
    writeValidChain(tempDirectory);
    mutate("minecraft-hook-patch-plan.json", root -> root.addProperty("patchPlanned", false));

    assertFailedStage(
        verifier.verify(SteelHookCompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-7-patch-plan");
  }

  @Test
  void transformReadyForMinecraftRuntimeTrueFailsCompletion() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-hook-patch-plan.json",
        root -> root.addProperty("transformReadyForMinecraftRuntime", true));

    SteelHookCompletionReport report =
        verifier.verify(SteelHookCompletionInput.fromWorkingDirectory(tempDirectory));

    assertFailedStage(report, "target-7-patch-plan");
    assertFailedInvariant(report, "transformReadyForMinecraftRuntime");
  }

  @Test
  void missingBootstrapTransformationResultFailsTargetNine() throws Exception {
    writeValidChain(tempDirectory);
    Files.delete(tempDirectory.resolve("minecraft-hook-bootstrap-transformation-result.json"));

    assertFailedStage(
        verifier.verify(SteelHookCompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-9-bootstrap-transform");
  }

  @Test
  void dispatcherInvocationCountZeroFailsCompletion() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-hook-bootstrap-transformation-result.json",
        root -> root.addProperty("dispatcherInvocationCount", 0));

    SteelHookCompletionReport report =
        verifier.verify(SteelHookCompletionInput.fromWorkingDirectory(tempDirectory));

    assertFailedStage(report, "target-9-bootstrap-transform");
    assertFailedInvariant(report, "dispatcherInvocationCount");
  }

  @Test
  void dispatcherInvocationCountTwoFailsCompletion() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-hook-bootstrap-transformation-result.json",
        root -> root.addProperty("dispatcherInvocationCount", 2));

    SteelHookCompletionReport report =
        verifier.verify(SteelHookCompletionInput.fromWorkingDirectory(tempDirectory));

    assertFailedStage(report, "target-9-bootstrap-transform");
    assertFailedInvariant(report, "dispatcherInvocationCount");
  }

  @Test
  void realMinecraftRuntimeTransformedTrueFailsCompletion() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-hook-bootstrap-transformation-result.json",
        root -> root.addProperty("realMinecraftRuntimeTransformed", true));

    assertFailedInvariant(
        verifier.verify(SteelHookCompletionInput.fromWorkingDirectory(tempDirectory)),
        "realMinecraftRuntimeTransformed");
  }

  @Test
  void publicApiExposedTrueFailsCompletion() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-hook-bootstrap-transformation-result.json",
        root -> root.addProperty("publicApiExposed", true));

    assertFailedInvariant(
        verifier.verify(SteelHookCompletionInput.fromWorkingDirectory(tempDirectory)),
        "publicApiExposed");
  }

  @Test
  void disallowedTransformationFlagsFailCompletion() throws Exception {
    for (String field :
        List.of("javaAgentUsed", "mixinUsed", "remappingOccurred", "accessWidenersUsed")) {
      Path caseDirectory = Files.createDirectory(tempDirectory.resolve(field));
      writeValidChain(caseDirectory);
      mutate(
          caseDirectory,
          "minecraft-hook-bootstrap-transformation-result.json",
          root -> root.addProperty(field, true));

      assertFailedInvariant(
          verifier.verify(SteelHookCompletionInput.fromWorkingDirectory(caseDirectory)), field);
    }
  }

  @Test
  void javaModExecutionSandboxedTrueFailsCompletion() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-hook-bootstrap-transformation-result.json",
        root -> root.addProperty("javaModExecutionSandboxed", true));

    assertFailedInvariant(
        verifier.verify(SteelHookCompletionInput.fromWorkingDirectory(tempDirectory)),
        "javaModExecutionSandboxed");
  }

  @Test
  void targetFourInstallationResultPresenceFailsCompletion() throws Exception {
    writeValidChain(tempDirectory);
    Files.writeString(
        tempDirectory.resolve("minecraft-hook-installation-result.json"),
        "{\n  \"schema\": 1,\n  \"milestoneName\": \"Target-4\"\n}\n",
        StandardCharsets.UTF_8);

    assertFailedInvariant(
        verifier.verify(SteelHookCompletionInput.fromWorkingDirectory(tempDirectory)),
        "noTarget4HookInstallationResultWritten");
  }

  private void assertFailedStage(SteelHookCompletionReport report, String stageId) {
    assertEquals(SteelHookCompletionStatus.FAILED, report.status());
    assertFalse(
        report.stageVerifications().stream()
            .filter(stage -> stageId.equals(stage.stageId()))
            .findFirst()
            .orElseThrow()
            .passed());
  }

  private void assertFailedInvariant(SteelHookCompletionReport report, String invariantId) {
    assertEquals(SteelHookCompletionStatus.FAILED, report.status());
    assertFalse(
        report.safetyInvariants().stream()
            .filter(invariant -> invariantId.equals(invariant.id()))
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
        directory.resolve("minecraft-hook-bootstrap-transformation-result.json"),
        validBootstrapTransformationResult());
    write(directory.resolve("minecraft-server-bootstrap-result.json"), validBootstrapResult());
    write(directory.resolve("minecraft-mod-execution-result.json"), validExecutionResult());
  }

  private void mutate(String fileName, java.util.function.Consumer<JsonObject> mutator)
      throws Exception {
    mutate(tempDirectory, fileName, mutator);
  }

  private void mutate(
      Path directory, String fileName, java.util.function.Consumer<JsonObject> mutator)
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
    JsonArray contracts = new JsonArray();
    JsonObject contract = new JsonObject();
    contract.addProperty("id", "minecraft.26_1_2.server.main.entrypoint");
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
    placement.addProperty("id", "target-5.minecraft.server.main.method-entry-placement");
    placement.addProperty("ownerInternalName", "net/minecraft/server/Main");
    placement.addProperty("memberName", "main");
    placement.addProperty("descriptor", "([Ljava/lang/String;)V");
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
    root.addProperty("targetClass", "net/minecraft/server/Main");
    root.addProperty("targetMethod", "main");
    root.addProperty("targetDescriptor", "([Ljava/lang/String;)V");
    root.addProperty("transformReadyForMinecraftRuntime", false);
    return root;
  }

  private JsonObject validBootstrapTransformationResult() {
    JsonObject root = new JsonObject();
    root.addProperty("schema", 1);
    root.addProperty("milestoneName", "Target-9");
    root.addProperty("status", "transformed");
    JsonObject gate = new JsonObject();
    gate.addProperty("passed", true);
    root.add("gate", gate);
    root.addProperty("transformationMode", "bootstrap-fake-server-method-entry-transform");
    root.addProperty("targetBinaryName", "net.minecraft.server.Main");
    root.addProperty("targetInternalName", "net/minecraft/server/Main");
    root.addProperty("targetMethod", "main");
    root.addProperty("targetDescriptor", "([Ljava/lang/String;)V");
    root.addProperty("bootstrapTransformationEnabled", true);
    root.addProperty("runtimeClassLoaderTransformationEnabled", true);
    root.addProperty("fakeServerRuntimeTransformed", true);
    root.addProperty("realMinecraftRuntimeTransformed", false);
    root.addProperty("bytecodeModified", true);
    root.addProperty("publicApiExposed", false);
    root.addProperty("javaAgentUsed", false);
    root.addProperty("mixinUsed", false);
    root.addProperty("remappingOccurred", false);
    root.addProperty("accessWidenersUsed", false);
    root.addProperty("javaModExecutionSandboxed", false);
    root.addProperty("dispatcherInvocationCount", 1);
    root.addProperty("dispatcherInvocationObserved", true);
    root.addProperty("minecraftMainInvoked", true);
    root.addProperty("fixtureTransformationStatus", "transformed");
    return root;
  }

  private JsonObject validBootstrapResult() {
    JsonObject root = new JsonObject();
    root.addProperty("schema", 1);
    root.addProperty("milestoneName", "Milestone 8");
    root.addProperty("minecraftMainInvoked", true);
    root.addProperty("exitCode", 0);
    root.addProperty("bytecodeTransformationUsed", true);
    return root;
  }

  private JsonObject validExecutionResult() {
    JsonObject root = new JsonObject();
    root.addProperty("schema", 1);
    root.addProperty("milestoneName", "Milestone 8");
    root.addProperty("minecraftMainInvoked", true);
    root.addProperty("processOutcome", 0);
    return root;
  }
}
