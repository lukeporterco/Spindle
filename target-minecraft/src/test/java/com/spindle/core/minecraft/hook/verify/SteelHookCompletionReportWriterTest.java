package com.spindle.core.minecraft.hook.verify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SteelHookCompletionReportWriterTest {
  @TempDir Path tempDirectory;

  private final SteelHookCompletionReportWriter writer = new SteelHookCompletionReportWriter();

  @Test
  void writerOutputIsDeterministicAndContainsNoByteArrays() throws Exception {
    SteelHookCompletionReport report =
        new SteelHookCompletionReport(
            1,
            "Target-10",
            "0.1",
            SteelHookCompletionStatus.PASSED,
            true,
            0,
            0,
            List.of(
                new SteelHookStageVerification(
                    "target-3-known-contracts",
                    "Target-3",
                    "Known-symbol hook contract validation passed.",
                    true,
                    null)),
            List.of(
                new SteelHookSafetyInvariant("dispatcherInvocationCount", "1", "1", true, null)),
            List.of(
                new SteelHookCapabilityBoundary(
                    "steelhook-0.1",
                    "supported",
                    "Narrow fake-server method-entry transform proof only.")));

    Path first = tempDirectory.resolve("one/minecraft-steelhook-0.1-report.json");
    Path second = tempDirectory.resolve("two/minecraft-steelhook-0.1-report.json");

    writer.write(first, report);
    writer.write(second, report);

    String firstJson = Files.readString(first, StandardCharsets.UTF_8);
    String secondJson = Files.readString(second, StandardCharsets.UTF_8);

    assertEquals(firstJson, secondJson);
    assertTrue(firstJson.contains("\"milestoneName\": \"Target-10\""));
    assertTrue(firstJson.contains("\"steelHookVersion\": \"0.1\""));
    assertTrue(firstJson.contains("\"status\": \"passed\""));
    assertTrue(firstJson.contains("\"failureReason\": null"));
    assertFalse(firstJson.contains("transformedClassBytes"));
  }
}
