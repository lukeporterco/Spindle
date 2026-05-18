package com.spindle.core.minecraft.hook.verify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.spindle.core.minecraft.hook.steelhook.SteelHook03FramedMethodFoundationReportWriter;
import com.spindle.core.minecraft.hook.steelhook.SteelHook03FramedMethodFoundationRunner;
import com.spindle.core.minecraft.hook.steelhook.SteelHook03GatedRuntimeProofRunner;
import com.spindle.core.minecraft.hook.steelhook.SteelHook03MethodExitDispatchReportWriter;
import com.spindle.core.minecraft.hook.steelhook.SteelHook03MethodExitDispatchRunner;
import com.spindle.core.minecraft.hook.steelhook.SteelHook03RuntimeProofReportWriter;
import com.spindle.core.minecraft.hook.steelhook.SteelHook03TestFixtures;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SteelHook03CompletionVerifierTest {
  @TempDir Path tempDirectory;

  private static final Gson GSON =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  private final SteelHook03CompletionVerifier verifier = new SteelHook03CompletionVerifier();

  @Test
  void validSteelHook03ChainPassesTargetThirtyOne() throws Exception {
    writeValidChain(tempDirectory);

    SteelHook03CompletionReport report =
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory));

    assertEquals(SteelHook03CompletionStatus.PASSED, report.status());
    assertTrue(report.completionReady());
    assertEquals(SteelHook03CompletionHandoffStatus.STEELHOOK_0_3_COMPLETE, report.handoffStatus());
    assertEquals("Target-31", report.milestoneName());
    assertEquals("0.3", report.steelHookVersion());
    assertEquals(2, report.runtimeClassLoaderProofCount());
    assertEquals(2, report.runtimeClassLoaderSuccessCount());
    assertTrue(report.entryPrimitiveVerified());
    assertTrue(report.exitPrimitiveVerified());
    assertNullOrAbsent(report.failureReason());
    assertTrue(
        report.stageVerifications().stream()
            .allMatch(SteelHook03CompletionStageVerification::passed));
    assertTrue(
        report.safetyInvariants().stream().allMatch(SteelHook03CompletionSafetyInvariant::passed));
    assertTrue(
        report.forbiddenReportChecks().stream().noneMatch(SteelHook03CompletionFinding::fatal));
  }

  @Test
  void missingSteelHook02ReportFails() throws Exception {
    writeValidChain(tempDirectory);
    Files.delete(tempDirectory.resolve("minecraft-steelhook-0-2-report.json"));

    SteelHook03CompletionReport report =
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory));

    assertFailedStage(report, "target-27-steelhook-0-2-completion");
  }

  @Test
  void failedSteelHook02ReportFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        tempDirectory,
        "minecraft-steelhook-0-2-report.json",
        root -> root.addProperty("status", "failed"));

    SteelHook03CompletionReport report =
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory));

    assertFailedStage(report, "target-27-steelhook-0-2-completion");
  }

  @Test
  void missingTarget28ReportFails() throws Exception {
    writeValidChain(tempDirectory);
    Files.delete(tempDirectory.resolve("minecraft-steelhook-0-3-framed-method-foundation.json"));

    SteelHook03CompletionReport report =
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory));

    assertFailedStage(report, "target-28-framed-method-foundation");
  }

  @Test
  void missingTarget29ReportFails() throws Exception {
    writeValidChain(tempDirectory);
    Files.delete(tempDirectory.resolve("minecraft-steelhook-0-3-method-exit-static-dispatch.json"));

    SteelHook03CompletionReport report =
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory));

    assertFailedStage(report, "target-29-method-exit-static-dispatch");
  }

  @Test
  void missingTarget30ReportFails() throws Exception {
    writeValidChain(tempDirectory);
    Files.delete(
        tempDirectory.resolve(
            "minecraft-steelhook-0-3-generalized-transformer-gated-runtime-proof.json"));

    SteelHook03CompletionReport report =
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory));

    assertFailedStage(report, "target-30-gated-runtime-proof");
  }

  @Test
  void target28StatusMismatchFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        tempDirectory,
        "minecraft-steelhook-0-3-framed-method-foundation.json",
        root -> root.addProperty("status", "failed"));

    assertFailedStage(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-28-framed-method-foundation");
  }

  @Test
  void target28FrameDeltaMismatchFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        tempDirectory,
        "minecraft-steelhook-0-3-framed-method-foundation.json",
        root -> root.addProperty("firstFrameOffsetDeltaAfter", 99));

    assertFailedStage(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-28-framed-method-foundation");
  }

  @Test
  void target28RuntimeClassLoadingEnabledFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        tempDirectory,
        "minecraft-steelhook-0-3-framed-method-foundation.json",
        root -> root.addProperty("runtimeClassLoadingPathEnabled", true));

    assertFailedStage(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-28-framed-method-foundation");
  }

  @Test
  void target29PrimitiveKindMismatchFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        tempDirectory,
        "minecraft-steelhook-0-3-method-exit-static-dispatch.json",
        root -> root.addProperty("primitiveKind", "METHOD_ENTRY_STATIC_DISPATCH"));

    assertFailedStage(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-29-method-exit-static-dispatch");
  }

  @Test
  void target29SourceTarget28MismatchFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        tempDirectory,
        "minecraft-steelhook-0-3-method-exit-static-dispatch.json",
        root -> root.addProperty("sourceTarget28Milestone", "Target-99"));

    assertFailedStage(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-29-method-exit-static-dispatch");
  }

  @Test
  void target29StackMapTablePresentFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        tempDirectory,
        "minecraft-steelhook-0-3-method-exit-static-dispatch.json",
        root -> root.addProperty("stackMapTablePresent", true));

    assertFailedStage(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-29-method-exit-static-dispatch");
  }

  @Test
  void target29BranchRewriteRequiredFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        tempDirectory,
        "minecraft-steelhook-0-3-method-exit-static-dispatch.json",
        root -> root.addProperty("branchRewriteRequired", true));

    assertFailedStage(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-29-method-exit-static-dispatch");
  }

  @Test
  void target29SwitchRewriteRequiredFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        tempDirectory,
        "minecraft-steelhook-0-3-method-exit-static-dispatch.json",
        root -> root.addProperty("switchRewriteRequired", true));

    assertFailedStage(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-29-method-exit-static-dispatch");
  }

  @Test
  void target30StatusMismatchFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        tempDirectory,
        "minecraft-steelhook-0-3-generalized-transformer-gated-runtime-proof.json",
        root -> root.addProperty("status", "failed"));

    assertFailedStage(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-30-gated-runtime-proof");
  }

  @Test
  void target30RuntimeClassLoaderProofCountNotTwoFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        tempDirectory,
        "minecraft-steelhook-0-3-generalized-transformer-gated-runtime-proof.json",
        root -> root.addProperty("runtimeClassLoaderProofCount", 1));

    assertFailedStage(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-30-gated-runtime-proof");
  }

  @Test
  void target30RuntimeClassLoaderSuccessCountNotTwoFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        tempDirectory,
        "minecraft-steelhook-0-3-generalized-transformer-gated-runtime-proof.json",
        root -> root.addProperty("runtimeClassLoaderSuccessCount", 1));

    assertFailedStage(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-30-gated-runtime-proof");
  }

  @Test
  void target30EntryPrimitiveMissingFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        tempDirectory,
        "minecraft-steelhook-0-3-generalized-transformer-gated-runtime-proof.json",
        root -> root.remove("entryPrimitiveProof"));

    assertFailedStage(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-30-gated-runtime-proof");
  }

  @Test
  void target30ExitPrimitiveMissingFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        tempDirectory,
        "minecraft-steelhook-0-3-generalized-transformer-gated-runtime-proof.json",
        root -> root.remove("exitPrimitiveProof"));

    assertFailedStage(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-30-gated-runtime-proof");
  }

  @Test
  void target30EqualRuntimeLoaderIdsFail() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        tempDirectory,
        "minecraft-steelhook-0-3-generalized-transformer-gated-runtime-proof.json",
        root ->
            root.getAsJsonObject("exitPrimitiveProof")
                .addProperty(
                    "runtimeLoaderId",
                    root.getAsJsonObject("entryPrimitiveProof")
                        .get("runtimeLoaderId")
                        .getAsString()));

    SteelHook03CompletionReport report =
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory));

    assertFalse(
        report.safetyInvariants().stream()
            .filter(invariant -> "target-30.separate-runtime-loader-ids".equals(invariant.id()))
            .findFirst()
            .orElseThrow()
            .passed());
  }

  @Test
  void target30EntryPrimitiveKindMismatchFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        tempDirectory,
        "minecraft-steelhook-0-3-generalized-transformer-gated-runtime-proof.json",
        root ->
            root.getAsJsonObject("entryPrimitiveProof")
                .addProperty("primitiveKind", "METHOD_EXIT_STATIC_DISPATCH"));

    assertFailedStage(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-30-entry-primitive-proof");
  }

  @Test
  void target30ExitPrimitiveKindMismatchFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        tempDirectory,
        "minecraft-steelhook-0-3-generalized-transformer-gated-runtime-proof.json",
        root ->
            root.getAsJsonObject("exitPrimitiveProof")
                .addProperty("primitiveKind", "METHOD_ENTRY_STATIC_DISPATCH"));

    assertFailedStage(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-30-exit-primitive-proof");
  }

  @Test
  void target30ClassInitializedTrueFails() throws Exception {
    writeValidChain(tempDirectory);
    mutatePrimitive("entryPrimitiveProof", root -> root.addProperty("classInitialized", true));

    assertFailedStage(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-30-entry-primitive-proof");
  }

  @Test
  void target30MinecraftMainInvokedTrueFails() throws Exception {
    writeValidChain(tempDirectory);
    mutatePrimitive("entryPrimitiveProof", root -> root.addProperty("minecraftMainInvoked", true));

    assertFailedStage(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-30-entry-primitive-proof");
  }

  @Test
  void target30HookInstallationOccurredTrueFails() throws Exception {
    writeValidChain(tempDirectory);
    mutatePrimitive(
        "entryPrimitiveProof", root -> root.addProperty("hookInstallationOccurred", true));

    assertFailedStage(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-30-entry-primitive-proof");
  }

  @Test
  void target30RuntimeDispatchOccurredTrueFails() throws Exception {
    writeValidChain(tempDirectory);
    mutatePrimitive(
        "entryPrimitiveProof", root -> root.addProperty("runtimeDispatchOccurred", true));

    assertFailedStage(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-30-entry-primitive-proof");
  }

  @Test
  void target30DispatcherInvocationObservedTrueFails() throws Exception {
    writeValidChain(tempDirectory);
    mutatePrimitive(
        "entryPrimitiveProof", root -> root.addProperty("dispatcherInvocationObserved", true));

    assertFailedStage(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-30-entry-primitive-proof");
  }

  @Test
  void target30DispatcherCountChangedFails() throws Exception {
    writeValidChain(tempDirectory);
    mutatePrimitive(
        "entryPrimitiveProof", root -> root.addProperty("dispatcherInvocationCountAfter", 1));

    assertFailedStage(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-30-entry-primitive-proof");
  }

  @Test
  void target30JavaModExecutionSandboxedTrueFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        tempDirectory,
        "minecraft-steelhook-0-3-generalized-transformer-gated-runtime-proof.json",
        root -> root.addProperty("javaModExecutionSandboxed", true));

    assertFailedStage(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-30-gated-runtime-proof");
  }

  @Test
  void forbiddenHookInstallationReportFails() throws Exception {
    assertForbiddenFileFails("minecraft-hook-installation-result.json");
  }

  @Test
  void forbiddenServerBootstrapReportFails() throws Exception {
    assertForbiddenFileFails("minecraft-server-bootstrap-result.json");
  }

  @Test
  void forbiddenFixtureTransformationReportFails() throws Exception {
    assertForbiddenFileFails("minecraft-fixture-transformation-result.json");
  }

  @Test
  void forbiddenHookBootstrapTransformationReportFails() throws Exception {
    assertForbiddenFileFails("minecraft-hook-bootstrap-transformation-result.json");
  }

  @Test
  void rawTransformedClassBytesKeyInSourceReportFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        tempDirectory,
        "minecraft-steelhook-0-3-generalized-transformer-gated-runtime-proof.json",
        root -> {
          JsonArray bytes = new JsonArray();
          bytes.add(1);
          root.getAsJsonObject("entryPrimitiveProof").add("transformedClassBytes", bytes);
        });

    assertFailedInvariant(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-30.no-raw-byte-payload-keys");
  }

  @Test
  void rawClassBytesKeyInSourceReportFails() throws Exception {
    writeValidChain(tempDirectory);
    mutate(
        tempDirectory,
        "minecraft-steelhook-0-3-framed-method-foundation.json",
        root -> {
          JsonArray bytes = new JsonArray();
          bytes.add(1);
          root.add("classBytes", bytes);
        });

    assertFailedInvariant(
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory)),
        "target-28.no-raw-byte-payload-keys");
  }

  private void assertForbiddenFileFails(String fileName) throws Exception {
    writeValidChain(tempDirectory);
    Files.writeString(tempDirectory.resolve(fileName), "{ }\n", StandardCharsets.UTF_8);

    SteelHook03CompletionReport report =
        verifier.verify(SteelHook03CompletionInput.fromWorkingDirectory(tempDirectory));

    assertEquals(SteelHook03CompletionStatus.FAILED, report.status());
    assertTrue(
        report.forbiddenReportChecks().stream()
            .filter(check -> fileName.equals(check.id()))
            .findFirst()
            .orElseThrow()
            .fatal());
  }

  private void mutatePrimitive(String primitiveName, Consumer<JsonObject> mutator)
      throws Exception {
    mutate(
        tempDirectory,
        "minecraft-steelhook-0-3-generalized-transformer-gated-runtime-proof.json",
        root -> mutator.accept(root.getAsJsonObject(primitiveName)));
  }

  private void assertFailedStage(SteelHook03CompletionReport report, String stageId) {
    assertEquals(SteelHook03CompletionStatus.FAILED, report.status());
    assertFalse(report.completionReady());
    assertEquals(
        SteelHook03CompletionHandoffStatus.STEELHOOK_0_3_INCOMPLETE, report.handoffStatus());
    assertNotNull(report.failureReason());
    assertFalse(
        report.stageVerifications().stream()
            .filter(stage -> stageId.equals(stage.stageId()))
            .findFirst()
            .orElseThrow()
            .passed());
  }

  private void assertFailedInvariant(SteelHook03CompletionReport report, String invariantId) {
    assertEquals(SteelHook03CompletionStatus.FAILED, report.status());
    assertFalse(
        report.safetyInvariants().stream()
            .filter(invariant -> invariantId.equals(invariant.id()))
            .findFirst()
            .orElseThrow()
            .passed());
  }

  private void writeValidChain(Path directory) throws Exception {
    Files.createDirectories(directory);
    write(directory.resolve("minecraft-steelhook-0-2-report.json"), validSteelHook02Report());
    new SteelHook03FramedMethodFoundationReportWriter()
        .write(
            directory.resolve(SteelHook03FramedMethodFoundationRunner.REPORT_FILE_NAME),
            SteelHook03TestFixtures.passedTarget28Report());
    new SteelHook03MethodExitDispatchReportWriter()
        .write(
            directory.resolve(SteelHook03MethodExitDispatchRunner.REPORT_FILE_NAME),
            SteelHook03TestFixtures.passedTarget29Report());
    new SteelHook03RuntimeProofReportWriter()
        .write(
            directory.resolve(SteelHook03GatedRuntimeProofRunner.REPORT_FILE_NAME),
            validTarget30Report());
  }

  private JsonObject validSteelHook02Report() {
    JsonObject root = new JsonObject();
    root.addProperty("schema", 1);
    root.addProperty("milestoneName", "Target-27");
    root.addProperty("status", "passed");
    root.addProperty("completionReady", true);
    root.addProperty("handoffStatus", "steelhook-0-2-complete");
    return root;
  }

  private com.spindle.core.minecraft.hook.steelhook.SteelHook03RuntimeProofReport
      validTarget30Report() throws Exception {
    Path runtimeDir = Files.createDirectories(tempDirectory.resolve("runtime-proof"));
    return new SteelHook03GatedRuntimeProofRunner()
        .run(
            SteelHook03TestFixtures.runtimePlan(runtimeDir.resolve("unused.jar")),
            SteelHook03TestFixtures.passedTarget28Report(),
            SteelHook03TestFixtures.passedTarget29Report(),
            runtimeDir);
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

  private void assertNullOrAbsent(String value) {
    assertTrue(value == null || value.isBlank());
  }
}
