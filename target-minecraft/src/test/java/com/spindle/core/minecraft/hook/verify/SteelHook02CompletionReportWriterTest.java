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

class SteelHook02CompletionReportWriterTest {
  @TempDir Path tempDirectory;

  private final SteelHook02CompletionReportWriter writer = new SteelHook02CompletionReportWriter();

  @Test
  void writerOutputIsDeterministicAndUsesEnumIds() throws Exception {
    SteelHook02CompletionReport report =
        new SteelHook02CompletionReport(
            1,
            "Target-27",
            "minecraft",
            "0.2",
            SteelHook02CompletionStatus.PASSED,
            SteelHook02CompletionHandoffStatus.STEELHOOK_0_2_COMPLETE,
            SteelHook02CompletionNextDirection.MOVE_TO_STEELHOOK_0_3_STACKMAP_AND_EXIT_PRIMITIVES,
            true,
            true,
            0,
            0,
            2,
            "net.minecraft.server.Main",
            "net/minecraft/server/Main.class",
            "METHOD_ENTRY_STATIC_DISPATCH",
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            List.of(
                new SteelHookStageVerification(
                    "target-27-completion",
                    "Target-27",
                    "SteelHook 0.2 completion passed.",
                    true,
                    null)),
            List.of(
                new SteelHookSafetyInvariant(
                    "steelhook-0-2.no-transformed-byte-arrays-serialized",
                    "false",
                    "false",
                    true,
                    null)),
            List.of(
                new SteelHookCapabilityBoundary(
                    "method-entry-static-dispatch",
                    "supported-in-one-approved-runtime-classloader-path",
                    "One supported path.")),
            null);

    Path first = tempDirectory.resolve("one/minecraft-steelhook-0-2-report.json");
    Path second = tempDirectory.resolve("two/minecraft-steelhook-0-2-report.json");

    writer.write(first, report);
    writer.write(second, report);

    String firstJson = Files.readString(first, StandardCharsets.UTF_8);
    String secondJson = Files.readString(second, StandardCharsets.UTF_8);

    assertEquals(firstJson, secondJson);
    assertTrue(firstJson.contains("\"schema\": 1"));
    assertTrue(firstJson.contains("\"milestoneName\": \"Target-27\""));
    assertTrue(firstJson.contains("\"target\": \"minecraft\""));
    assertTrue(firstJson.contains("\"steelHookVersion\": \"0.2\""));
    assertTrue(firstJson.contains("\"status\": \"passed\""));
    assertTrue(firstJson.contains("\"handoffStatus\": \"steelhook-0-2-complete\""));
    assertTrue(
        firstJson.contains(
            "\"nextDirection\": \"move-to-steelhook-0-3-stackmap-and-exit-primitives\""));
    assertTrue(firstJson.contains("\"failureSummary\": null"));
    assertFalse(firstJson.contains("STEELHOOK_0_2_COMPLETE"));
    assertFalse(firstJson.contains("MOVE_TO_STEELHOOK_0_3_STACKMAP_AND_EXIT_PRIMITIVES"));
    assertFalse(firstJson.contains("transformedClassBytes"));
  }
}
