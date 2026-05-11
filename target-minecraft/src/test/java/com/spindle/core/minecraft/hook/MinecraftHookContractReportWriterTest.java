package com.spindle.core.minecraft.hook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinecraftHookContractReportWriterTest {
  @TempDir Path tempDirectory;

  private final MinecraftHookContractReportWriter writer = new MinecraftHookContractReportWriter();

  @Test
  void writesDeterministicJsonAndCreatesParentDirectories() throws Exception {
    MinecraftHookContractReport report =
        new MinecraftHookContractReport(
            2,
            "Target-3",
            "minecraft",
            "26.1.2",
            "server",
            "minecraft-26.1.2-server-known-symbols",
            "Target-3 known-symbol hook contracts for Minecraft 26.1.2 server artifacts.",
            "26.1.2",
            "server",
            true,
            false,
            false,
            false,
            false,
            false,
            1,
            "Target-1",
            2,
            1,
            1,
            1,
            1,
            1,
            1,
            false,
            List.of(
                new MinecraftHookContractResult(
                    "contract-alpha",
                    "alpha",
                    "server",
                    "CLASS",
                    "net/minecraft/server/Alpha",
                    null,
                    null,
                    "REQUIRED",
                    "VALID",
                    true,
                    true,
                    false,
                    List.of(),
                    "net/minecraft/server/Alpha",
                    null),
                new MinecraftHookContractResult(
                    "contract-beta",
                    "beta",
                    "server",
                    "METHOD",
                    "net/minecraft/server/Beta",
                    "tick",
                    "()V",
                    "OPTIONAL",
                    "MISSING_MEMBER",
                    false,
                    false,
                    true,
                    List.of("hook-contract-0002"),
                    null,
                    null)),
            List.of(
                new MinecraftHookContractDiagnostic(
                    "hook-contract-0001",
                    MinecraftHookDiagnosticSeverity.INFO,
                    "NO_CONTRACTS_DECLARED",
                    null,
                    "minecraft.hook_contract.no_contracts_declared",
                    "info",
                    null,
                    null,
                    null),
                new MinecraftHookContractDiagnostic(
                    "hook-contract-0002",
                    MinecraftHookDiagnosticSeverity.ERROR,
                    "MISSING_MEMBER",
                    "contract-beta",
                    "minecraft.hook_contract.missing_member",
                    "missing member",
                    "net/minecraft/server/Beta",
                    "tick",
                    "()V")));

    Path firstOutput = tempDirectory.resolve("reports/nested/minecraft-hook-contracts.json");
    Path secondOutput = tempDirectory.resolve("reports/second/minecraft-hook-contracts.json");

    writer.write(firstOutput, report);
    writer.write(secondOutput, report);

    String firstJson = Files.readString(firstOutput, StandardCharsets.UTF_8);
    String secondJson = Files.readString(secondOutput, StandardCharsets.UTF_8);

    assertTrue(Files.exists(firstOutput));
    assertTrue(firstJson.contains("\"schema\": 2"));
    assertTrue(firstJson.contains("\"milestoneName\": \"Target-3\""));
    assertTrue(firstJson.contains("\"catalogId\": \"minecraft-26.1.2-server-known-symbols\""));
    assertTrue(firstJson.contains("\"catalogSide\": \"server\""));
    assertTrue(firstJson.contains("\"analysisOnly\": true"));
    assertTrue(firstJson.contains("\"classLoadingOccurred\": false"));
    assertTrue(firstJson.contains("\"injectionOccurred\": false"));
    assertTrue(firstJson.contains("\"transformationOccurred\": false"));
    assertTrue(firstJson.contains("\"patchingOccurred\": false"));
    assertTrue(firstJson.contains("\"hookInstallationOccurred\": false"));
    assertTrue(firstJson.contains("\"artifactInterpretationMilestone\": \"Target-1\""));
    assertTrue(
        firstJson.indexOf("\"id\": \"contract-alpha\"")
            < firstJson.indexOf("\"id\": \"contract-beta\""));
    assertTrue(
        firstJson.indexOf("\"id\": \"hook-contract-0001\"")
            < firstJson.indexOf("\"id\": \"hook-contract-0002\""));
    assertEquals(firstJson, secondJson);
  }
}
