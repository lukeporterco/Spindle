package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.hook.verify.SteelHook03CompletionHandoffStatus;
import com.spindle.core.minecraft.hook.verify.SteelHook03CompletionReport;
import com.spindle.core.minecraft.hook.verify.SteelHook03CompletionStatus;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SteelHook04PrimitiveBoundaryReportWriterTest {
  @TempDir Path tempDirectory;

  private final SteelHook04PrimitiveBoundaryReportWriter writer =
      new SteelHook04PrimitiveBoundaryReportWriter();

  @Test
  void writesSchemaAndReportFileWithExpectedFields() throws Exception {
    SteelHook04PrimitiveBoundaryReport report = sampleReport();
    Path output = tempDirectory.resolve(SteelHook04PrimitiveBoundaryReportWriter.REPORT_FILE_NAME);

    writer.write(output, report);

    assertEquals(
        SteelHook04PrimitiveBoundaryReportWriter.REPORT_FILE_NAME, output.getFileName().toString());
    String json = Files.readString(output, StandardCharsets.UTF_8);
    assertTrue(json.contains("\"schema\": 1"));
    assertTrue(json.contains("\"milestoneName\": \"Target-32\""));
    assertTrue(json.contains("\"target\": \"minecraft\""));
    assertTrue(json.contains("\"steelHookVersion\": \"0.4\""));
  }

  @Test
  void writesAllPrimitiveNamesFixtureShapesAndFalseSideEffects() {
    String json = writer.toJson(sampleReport()).toString();

    assertTrue(json.contains("RETURN_VALUE_INTERCEPT"));
    assertTrue(json.contains("INVOKE_REDIRECT"));
    assertTrue(json.contains("INVOKE_WRAP"));
    assertTrue(json.contains("RETURN_SINGLE_PRIMITIVE_VALUE"));
    assertTrue(json.contains("RETURN_SINGLE_REFERENCE_VALUE"));
    assertTrue(json.contains("INVOKE_SINGLE_STATIC_OR_VIRTUAL_CALLSITE"));
    assertTrue(json.contains("CONSTRUCTOR_INVOCATION"));
    assertTrue(json.contains("FRAME_RECOMPUTATION_REQUIRED"));
    assertTrue(json.contains("\"analysisOnly\":true"));
    assertTrue(json.contains("\"bytecodeModified\":false"));
    assertTrue(json.contains("\"transformedClassBytesProduced\":false"));
    assertTrue(json.contains("\"runtimeClassLoadingPathEnabled\":false"));
    assertTrue(json.contains("\"classLoadingOccurred\":false"));
    assertTrue(json.contains("\"serverLaunchOccurred\":false"));
    assertTrue(json.contains("\"minecraftMainInvoked\":false"));
    assertTrue(json.contains("\"hookInstallationOccurred\":false"));
    assertTrue(json.contains("\"runtimeDispatchOccurred\":false"));
    assertTrue(json.contains("\"publicApiExposed\":false"));
    assertTrue(json.contains("\"javaAgentUsed\":false"));
    assertTrue(json.contains("\"mixinUsed\":false"));
    assertTrue(json.contains("\"javaModExecutionSandboxed\":false"));
  }

  @Test
  void doesNotIncludeRawBytePayloadKeys() {
    String json = writer.toJson(sampleReport()).toString();

    assertFalse(json.contains("classBytes"));
    assertFalse(json.contains("rawClassBytes"));
    assertFalse(json.contains("originalClassBytes"));
    assertFalse(json.contains("transformedClassBytes\""));
    assertFalse(json.contains("stackMapTableBytes"));
    assertFalse(json.contains("rawStackMapTableBytes"));
    assertFalse(json.contains("bytecodeBytes"));
  }

  private SteelHook04PrimitiveBoundaryReport sampleReport() {
    SteelHook03CompletionReport sourceReport =
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
            "passed",
            true,
            "Target-29",
            "passed",
            true,
            "Target-30",
            "passed",
            true,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            2,
            2,
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
            List.of());
    return new SteelHook04PrimitiveBoundaryAnalyzer().analyze(sourceReport);
  }
}
