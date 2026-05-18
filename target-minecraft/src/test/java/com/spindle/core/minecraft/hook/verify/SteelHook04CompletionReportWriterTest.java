package com.spindle.core.minecraft.hook.verify;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.spindle.core.diagnostics.LoaderException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SteelHook04CompletionReportWriterTest {
  @TempDir Path tempDir;

  @Test
  void writesExpectedFields() throws Exception {
    SteelHook04CompletionReport report =
        sampleReport(
            List.of("RETURN_VALUE_INTERCEPT", "INVOKE_REDIRECT", "INVOKE_WRAP"),
            List.of(new SteelHook04CompletionFinding("finding", false, "ok")));
    Path output = tempDir.resolve("reports/minecraft-steelhook-0-4-report.json");

    new SteelHook04CompletionReportWriter().write(output, report);

    String json = Files.readString(output, StandardCharsets.UTF_8);
    assertTrue(json.contains("\"schema\": 1"));
    assertTrue(json.contains("\"milestoneName\": \"Target-36\""));
    assertTrue(json.contains("\"target\": \"minecraft\""));
    assertTrue(json.contains("\"steelHookVersion\": \"0.4\""));
    assertTrue(json.contains("\"completionReady\": true"));
    assertTrue(json.contains("\"status\": \"passed\""));
    assertTrue(json.contains("\"handoffStatus\": \"steelhook-0-4-complete\""));
    assertTrue(json.contains("\"completedPrimitiveKinds\""));
    assertTrue(json.contains("RETURN_VALUE_INTERCEPT"));
    assertTrue(json.contains("INVOKE_REDIRECT"));
    assertTrue(json.contains("INVOKE_WRAP"));
    assertTrue(json.contains("\"completedCapabilities\""));
    assertTrue(json.contains("\"unsupportedCapabilities\""));
    assertTrue(json.contains("\"stageVerifications\""));
    assertTrue(json.contains("\"safetyInvariants\""));
    assertTrue(json.contains("\"forbiddenReportChecks\""));
    assertTrue(json.contains("\"runtimeClassLoadingPathEnabled\": true"));
    assertTrue(json.contains("\"classLoadingOccurred\": true"));
    assertTrue(json.contains("\"targetClassDefinitionOccurred\": true"));
    assertTrue(json.contains("\"classInitialized\": false"));
    assertTrue(json.contains("\"targetMethodInvoked\": false"));
    assertTrue(json.contains("\"wrapperExecuted\": false"));
    assertTrue(json.contains("\"serverLaunchOccurred\": false"));
    assertTrue(json.contains("\"minecraftMainInvoked\": false"));
    assertTrue(json.contains("\"hookInstallationOccurred\": false"));
    assertTrue(json.contains("\"runtimeDispatchOccurred\": false"));
    assertTrue(json.contains("\"publicApiExposed\": false"));
    assertTrue(json.contains("\"javaAgentUsed\": false"));
    assertTrue(json.contains("\"mixinUsed\": false"));
    assertTrue(json.contains("\"javaModExecutionSandboxed\": false"));
  }

  @Test
  void throwsWhenOutputContainsRawBytePayloadKey() {
    SteelHook04CompletionReportWriter writer = new SteelHook04CompletionReportWriter();
    SteelHook04CompletionReport report =
        sampleReport(
            List.of("RETURN_VALUE_INTERCEPT", "INVOKE_REDIRECT", "INVOKE_WRAP"),
            List.of(new SteelHook04CompletionFinding("finding", false, "ok")));
    com.google.gson.JsonObject root = writer.toJson(report);
    root.add("transformedClassBytes", new JsonArray());

    assertThrows(LoaderException.class, () -> writer.validateNoRawByteKeys(root));
  }

  private SteelHook04CompletionReport sampleReport(
      List<String> completedPrimitiveKinds, List<SteelHook04CompletionFinding> findings) {
    return new SteelHook04CompletionReport(
        1,
        "Target-36",
        "minecraft",
        "0.4",
        true,
        SteelHook04CompletionStatus.PASSED,
        SteelHook04CompletionHandoffStatus.STEELHOOK_0_4_COMPLETE,
        SteelHook04CompletionNextDirection.STEELHOOK_0_4_COMPLETE,
        "SteelHook 0.4 Arc complete.",
        "Target-32",
        "boundary-ready",
        true,
        3,
        "Target-33",
        "proof-ready",
        true,
        "RETURN_VALUE_INTERCEPT",
        4,
        "Target-34",
        "proof-ready",
        true,
        2,
        "Target-35",
        "gated-runtime-proof-ready",
        true,
        3,
        3,
        completedPrimitiveKinds,
        List.of("bounded return-value intercept offline observation and replacement evidence"),
        List.of("public SteelHook API"),
        List.of(
            new SteelHook04CompletionStageVerification(
                "stage", "Target-36", "summary", true, null)),
        List.of(
            new SteelHook04CompletionSafetyInvariant("invariant", "absent", "absent", true, null)),
        List.of(
            new SteelHook04CompletionFinding(
                "minecraft-hook-installation-result.json", false, "absent")),
        true,
        true,
        true,
        true,
        true,
        true,
        true,
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
        false,
        null,
        findings);
  }
}
