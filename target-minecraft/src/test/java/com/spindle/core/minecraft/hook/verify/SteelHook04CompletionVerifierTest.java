package com.spindle.core.minecraft.hook.verify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.spindle.core.minecraft.hook.steelhook.SteelHook02TestFixtures;
import com.spindle.core.minecraft.hook.steelhook.SteelHook04GatedRuntimeProofReport;
import com.spindle.core.minecraft.hook.steelhook.SteelHook04GatedRuntimeProofReportWriter;
import com.spindle.core.minecraft.hook.steelhook.SteelHook04GatedRuntimeProofRunner;
import com.spindle.core.minecraft.hook.steelhook.SteelHook04InvokeRedirectWrapOfflineProofReportWriter;
import com.spindle.core.minecraft.hook.steelhook.SteelHook04PrimitiveBoundaryReportWriter;
import com.spindle.core.minecraft.hook.steelhook.SteelHook04ReturnValueInterceptOfflineProofReportWriter;
import com.spindle.core.minecraft.hook.steelhook.SteelHook04TestFixtures;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SteelHook04CompletionVerifierTest {
  @TempDir Path tempDirectory;

  private static final Gson GSON =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  private final SteelHook04CompletionVerifier verifier = new SteelHook04CompletionVerifier();

  @Test
  void validTarget32Through35ReportsProduceCompletionReadyTrue() throws Exception {
    writeValidChain(tempDirectory);

    SteelHook04CompletionReport report =
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory));

    assertTrue(report.completionReady(), report.stageVerifications().toString());
    assertEquals(SteelHook04CompletionStatus.PASSED, report.status());
    assertEquals(SteelHook04CompletionHandoffStatus.STEELHOOK_0_4_COMPLETE, report.handoffStatus());
    assertEquals("Target-36", report.milestoneName());
    assertEquals("0.4", report.steelHookVersion());
    assertTrue(report.returnValueInterceptVerified());
    assertTrue(report.invokeRedirectVerified());
    assertTrue(report.invokeWrapVerified());
    assertTrue(report.rawBytePayloadsAbsent());
    assertTrue(report.unsupportedPrimitiveLeakageAbsent());
    assertTrue(
        report.stageVerifications().stream()
            .allMatch(SteelHook04CompletionStageVerification::passed));
    assertTrue(
        report.safetyInvariants().stream().allMatch(SteelHook04CompletionSafetyInvariant::passed));
  }

  @Test
  void missingTarget32ReportFails() throws Exception {
    writeValidChain(tempDirectory);
    Files.delete(tempDirectory.resolve("minecraft-steelhook-0-4-primitive-boundary.json"));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-32-primitive-boundary");
  }

  @Test
  void missingTarget33ReportFails() throws Exception {
    writeValidChain(tempDirectory);
    Files.delete(
        tempDirectory.resolve("minecraft-steelhook-0-4-return-value-intercept-offline-proof.json"));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-33-return-value-intercept-offline-proof");
  }

  @Test
  void missingTarget34ReportFails() throws Exception {
    writeValidChain(tempDirectory);
    Files.delete(
        tempDirectory.resolve("minecraft-steelhook-0-4-invoke-redirect-wrap-offline-proof.json"));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-34-invoke-redirect-wrap-offline-proof");
  }

  @Test
  void missingTarget35ReportFails() throws Exception {
    writeValidChain(tempDirectory);
    Files.delete(tempDirectory.resolve("minecraft-steelhook-0-4-gated-runtime-proof.json"));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-35-gated-runtime-proof");
  }

  @Test
  void target32SchemaMismatchFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-steelhook-0-4-primitive-boundary.json", root -> root.addProperty("schema", 2));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-32-primitive-boundary");
  }

  @Test
  void target33SchemaMismatchFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-steelhook-0-4-return-value-intercept-offline-proof.json",
        root -> root.addProperty("schema", 2));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-33-return-value-intercept-offline-proof");
  }

  @Test
  void target34SchemaMismatchFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-steelhook-0-4-invoke-redirect-wrap-offline-proof.json",
        root -> root.addProperty("schema", 2));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-34-invoke-redirect-wrap-offline-proof");
  }

  @Test
  void target35SchemaMismatchFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-steelhook-0-4-gated-runtime-proof.json", root -> root.addProperty("schema", 2));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-35-gated-runtime-proof");
  }

  @Test
  void target32UnsupportedPrimitiveCandidateFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-steelhook-0-4-primitive-boundary.json",
        root ->
            root.getAsJsonArray("candidates")
                .get(0)
                .getAsJsonObject()
                .addProperty("primitiveKind", "OTHER"));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-32-primitive-boundary");
  }

  @Test
  void target33MissingReturnValueInterceptEvidenceFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-steelhook-0-4-return-value-intercept-offline-proof.json",
        root -> root.addProperty("primitiveKind", "INVOKE_REDIRECT"));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-33-return-value-intercept-offline-proof");
  }

  @Test
  void target34MissingInvokeRedirectEvidenceFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-steelhook-0-4-invoke-redirect-wrap-offline-proof.json",
        root -> root.getAsJsonArray("approvedPrimitiveKinds").remove(0));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-34-invoke-redirect-wrap-offline-proof");
  }

  @Test
  void target34MissingInvokeWrapEvidenceFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-steelhook-0-4-invoke-redirect-wrap-offline-proof.json",
        root -> root.getAsJsonArray("approvedPrimitiveKinds").remove(1));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-34-invoke-redirect-wrap-offline-proof");
  }

  @Test
  void target35MissingReturnValueInterceptRuntimeProofFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-steelhook-0-4-gated-runtime-proof.json",
        root -> root.remove("returnValueInterceptProof"));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-35-gated-runtime-proof");
  }

  @Test
  void target35MissingInvokeRedirectRuntimeProofFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-steelhook-0-4-gated-runtime-proof.json",
        root -> root.remove("invokeRedirectProof"));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-35-gated-runtime-proof");
  }

  @Test
  void target35MissingInvokeWrapRuntimeProofFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-steelhook-0-4-gated-runtime-proof.json", root -> root.remove("invokeWrapProof"));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-35-gated-runtime-proof");
  }

  @Test
  void target35RuntimeClassLoaderSuccessCountLessThanThreeFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-steelhook-0-4-gated-runtime-proof.json",
        root -> root.addProperty("runtimeClassLoaderSuccessCount", 2));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-35-gated-runtime-proof");
  }

  @Test
  void target35ClassInitializedTrueFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-steelhook-0-4-gated-runtime-proof.json",
        root -> root.addProperty("classInitialized", true));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-35-gated-runtime-proof");
  }

  @Test
  void target35TargetMethodInvokedTrueFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-steelhook-0-4-gated-runtime-proof.json",
        root -> root.addProperty("targetMethodInvoked", true));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-35-gated-runtime-proof");
  }

  @Test
  void target35WrapperExecutedTrueFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-steelhook-0-4-gated-runtime-proof.json",
        root -> root.addProperty("wrapperExecuted", true));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-35-gated-runtime-proof");
  }

  @Test
  void target35RuntimeDispatchOccurredTrueFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-steelhook-0-4-gated-runtime-proof.json",
        root -> root.addProperty("runtimeDispatchOccurred", true));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-35-gated-runtime-proof");
  }

  @Test
  void target35HookInstallationOccurredTrueFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-steelhook-0-4-gated-runtime-proof.json",
        root -> root.addProperty("hookInstallationOccurred", true));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-35-gated-runtime-proof");
  }

  @Test
  void target35MinecraftMainInvokedTrueFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-steelhook-0-4-gated-runtime-proof.json",
        root -> root.addProperty("minecraftMainInvoked", true));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-35-gated-runtime-proof");
  }

  @Test
  void target35ServerLaunchOccurredTrueFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-steelhook-0-4-gated-runtime-proof.json",
        root -> root.addProperty("serverLaunchOccurred", true));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-35-gated-runtime-proof");
  }

  @Test
  void publicApiExposedTrueInAnySourceReportFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-steelhook-0-4-return-value-intercept-offline-proof.json",
        root -> root.addProperty("publicApiExposed", true));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-33-return-value-intercept-offline-proof");
  }

  @Test
  void javaModExecutionSandboxedTrueInAnySourceReportFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-steelhook-0-4-invoke-redirect-wrap-offline-proof.json",
        root -> root.addProperty("javaModExecutionSandboxed", true));
    assertFailedStage(
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-34-invoke-redirect-wrap-offline-proof");
  }

  @Test
  void rawBytePayloadKeyInAnySourceReportFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        "minecraft-steelhook-0-4-gated-runtime-proof.json",
        root -> {
          JsonArray bytes = new JsonArray();
          bytes.add(1);
          root.getAsJsonObject("returnValueInterceptProof").add("transformedClassBytes", bytes);
        });

    SteelHook04CompletionReport report =
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory));
    assertEquals(SteelHook04CompletionStatus.FAILED, report.status());
    assertFalse(
        report.safetyInvariants().stream()
            .filter(invariant -> "target-35.no-raw-byte-payload-keys".equals(invariant.id()))
            .findFirst()
            .orElseThrow()
            .passed());
  }

  @Test
  void staleHookInstallationReportFails() throws Exception {
    assertForbiddenFileFails("minecraft-hook-installation-result.json");
  }

  @Test
  void staleServerBootstrapReportFails() throws Exception {
    assertForbiddenFileFails("minecraft-server-bootstrap-result.json");
  }

  @Test
  void staleFixtureTransformationReportFails() throws Exception {
    assertForbiddenFileFails("minecraft-fixture-transformation-result.json");
  }

  @Test
  void staleHookBootstrapTransformationReportFails() throws Exception {
    assertForbiddenFileFails("minecraft-hook-bootstrap-transformation-result.json");
  }

  private void assertForbiddenFileFails(String fileName) throws Exception {
    writeValidChain(tempDirectory);
    Files.writeString(tempDirectory.resolve(fileName), "{ }\n", StandardCharsets.UTF_8);

    SteelHook04CompletionReport report =
        verifier.verify(SteelHook04CompletionInput.fromWorkingDirectory(tempDirectory));

    assertEquals(SteelHook04CompletionStatus.FAILED, report.status());
    assertTrue(
        report.forbiddenReportChecks().stream()
            .filter(check -> fileName.equals(check.id()))
            .findFirst()
            .orElseThrow()
            .fatal());
  }

  private void writeValidChain(Path directory) throws Exception {
    Files.createDirectories(directory);
    new SteelHook04PrimitiveBoundaryReportWriter()
        .write(
            directory.resolve("minecraft-steelhook-0-4-primitive-boundary.json"),
            SteelHook04TestFixtures.passedTarget32Report());
    new SteelHook04ReturnValueInterceptOfflineProofReportWriter()
        .write(
            directory.resolve("minecraft-steelhook-0-4-return-value-intercept-offline-proof.json"),
            SteelHook04TestFixtures.passedTarget33Report());
    new SteelHook04InvokeRedirectWrapOfflineProofReportWriter()
        .write(
            directory.resolve("minecraft-steelhook-0-4-invoke-redirect-wrap-offline-proof.json"),
            SteelHook04TestFixtures.passedTarget34Report());
    new SteelHook04GatedRuntimeProofReportWriter()
        .write(
            directory.resolve("minecraft-steelhook-0-4-gated-runtime-proof.json"),
            validTarget35Report());
  }

  private SteelHook04GatedRuntimeProofReport validTarget35Report() throws Exception {
    Path runtimeJar =
        SteelHook02TestFixtures.createRuntimeJar(
            tempDirectory.resolve("hook-server.jar"),
            SteelHook02TestFixtures.readResourceBytes("net/minecraft/server/Main.class"));
    return new SteelHook04GatedRuntimeProofRunner()
        .run(
            SteelHook02TestFixtures.runtimePlan(runtimeJar),
            SteelHook04TestFixtures.passedTarget32Report(),
            SteelHook04TestFixtures.passedTarget33Report(),
            SteelHook04TestFixtures.passedTarget34Report(),
            tempDirectory.resolve("fixtures"));
  }

  private void mutate(String fileName, Consumer<JsonObject> mutator) throws Exception {
    Path path = tempDirectory.resolve(fileName);
    JsonObject root =
        com.google.gson.JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8))
            .getAsJsonObject();
    mutator.accept(root);
    write(path, root);
  }

  private void write(Path path, JsonObject object) throws IOException {
    Files.writeString(path, GSON.toJson(object), StandardCharsets.UTF_8);
  }

  private void assertFailedStage(SteelHook04CompletionReport report, String stageId) {
    assertEquals(SteelHook04CompletionStatus.FAILED, report.status());
    assertFalse(report.completionReady());
    assertEquals(
        SteelHook04CompletionHandoffStatus.STEELHOOK_0_4_INCOMPLETE, report.handoffStatus());
    assertNotNull(report.failureReason());
    assertFalse(
        report.stageVerifications().stream()
            .filter(stage -> stageId.equals(stage.stageId()))
            .findFirst()
            .orElseThrow()
            .passed());
  }
}
