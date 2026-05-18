package com.spindle.core.minecraft.hook.verify;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SteelHook03CompletionReportWriterTest {
  @TempDir Path tempDir;

  @Test
  void writerEmitsExpectedFieldsAndCreatesParentDirectories() throws Exception {
    SteelHook03CompletionReport report =
        new SteelHook03CompletionReport(
            1,
            "Target-31",
            "minecraft",
            "0.3",
            true,
            SteelHook03CompletionStatus.PASSED,
            SteelHook03CompletionHandoffStatus.STEELHOOK_0_3_COMPLETE,
            "Target-27",
            "passed",
            true,
            "steelhook-0-2-complete",
            "Target-28",
            "foundation-ready",
            true,
            "Target-29",
            "method-exit-dispatch-ready",
            true,
            "Target-30",
            "gated-runtime-proof-ready",
            true,
            List.of("capability-a"),
            List.of("capability-b"),
            List.of(
                new SteelHook03CompletionStageVerification(
                    "stage", "Target-31", "summary", true, null)),
            List.of(new SteelHook03CompletionSafetyInvariant("invariant", "x", "x", true, null)),
            List.of(new SteelHook03CompletionFinding("forbidden", false, "absent")),
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
            List.of(new SteelHook03CompletionFinding("finding", false, "ok")));

    Path outputPath = tempDir.resolve("reports/nested/minecraft-steelhook-0-3-report.json");
    new SteelHook03CompletionReportWriter().write(outputPath, report);

    String json = Files.readString(outputPath, StandardCharsets.UTF_8);
    assertTrue(json.contains("\"schema\": 1"));
    assertTrue(json.contains("\"milestoneName\": \"Target-31\""));
    assertTrue(json.contains("\"target\": \"minecraft\""));
    assertTrue(json.contains("\"steelHookVersion\": \"0.3\""));
    assertTrue(json.contains("\"status\": \"passed\""));
    assertTrue(json.contains("\"handoffStatus\": \"steelhook-0-3-complete\""));
    assertTrue(json.contains("\"completedCapabilities\""));
    assertTrue(json.contains("\"unsupportedCapabilities\""));
    assertTrue(json.contains("\"stageVerifications\""));
    assertTrue(json.contains("\"safetyInvariants\""));
    assertTrue(json.contains("\"forbiddenReportChecks\""));
    assertTrue(json.contains("\"findings\""));
    assertFalse(json.contains("\"transformedClassBytes\":"));
    assertFalse(json.contains("\"classBytes\":"));
  }
}
