package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SteelHook04ReturnValueInterceptOfflineProofReportWriterTest {
  @TempDir Path tempDirectory;

  private final SteelHook04ReturnValueInterceptOfflineProofReportWriter writer =
      new SteelHook04ReturnValueInterceptOfflineProofReportWriter();

  @Test
  void writesSchemaTarget33MinecraftAndSteelHookVersion04() throws Exception {
    SteelHook04ReturnValueInterceptOfflineProofReport report = sampleReport();
    Path output =
        tempDirectory.resolve(
            SteelHook04ReturnValueInterceptOfflineProofReportWriter.REPORT_FILE_NAME);

    writer.write(output, report);

    assertEquals(
        SteelHook04ReturnValueInterceptOfflineProofReportWriter.REPORT_FILE_NAME,
        output.getFileName().toString());
    String json = Files.readString(output, StandardCharsets.UTF_8);
    assertTrue(json.contains("\"schema\": 1"));
    assertTrue(json.contains("\"milestoneName\": \"Target-33\""));
    assertTrue(json.contains("\"target\": \"minecraft\""));
    assertTrue(json.contains("\"steelHookVersion\": \"0.4\""));
  }

  @Test
  void writesRequiredProofFieldsAndFalseSideEffects() {
    String json = writer.toJson(sampleReport()).toString();

    assertTrue(json.contains("RETURN_VALUE_INTERCEPT"));
    assertTrue(json.contains("primitive observe-only"));
    assertTrue(json.contains("reference replacement"));
    assertTrue(json.contains("\"bytecodeModified\":false"));
    assertTrue(json.contains("\"transformedClassBytesProduced\":false"));
    assertTrue(json.contains("\"bytecodeModified\":true"));
    assertTrue(json.contains("\"transformedClassBytesProduced\":true"));
    assertTrue(json.contains("originalClassSha256"));
    assertTrue(json.contains("transformedClassSha256"));
    assertTrue(json.contains("originalCodeLength"));
    assertTrue(json.contains("transformedCodeLength"));
    assertTrue(json.contains("\"runtimeClassLoadingPathEnabled\":false"));
    assertTrue(json.contains("\"hookInstallationOccurred\":false"));
    assertTrue(json.contains("\"runtimeDispatchOccurred\":false"));
    assertTrue(json.contains("\"javaModExecutionSandboxed\":false"));
  }

  @Test
  void doesNotIncludeRawBytePayloadKeys() {
    String json = writer.toJson(sampleReport()).toString();

    assertFalse(json.contains("classBytes"));
    assertFalse(json.contains("rawClassBytes"));
    assertFalse(json.contains("originalClassBytes"));
    assertFalse(json.contains("transformedClassBytes\""));
    assertFalse(json.contains("methodCodeBytes"));
    assertFalse(json.contains("rawMethodCode"));
    assertFalse(json.contains("bytecodeBytes"));
    assertFalse(json.contains("stackMapTableBytes"));
    assertFalse(json.contains("rawStackMapTableBytes"));
    assertFalse(json.contains("codeBytes"));
    assertFalse(json.contains("\"bytes\""));
    assertFalse(json.contains("payload"));
  }

  private SteelHook04ReturnValueInterceptOfflineProofReport sampleReport() {
    return new SteelHook04ReturnValueInterceptOfflineProofRunner()
        .run(SteelHook04TestFixtures.passedTarget32Report());
  }
}
