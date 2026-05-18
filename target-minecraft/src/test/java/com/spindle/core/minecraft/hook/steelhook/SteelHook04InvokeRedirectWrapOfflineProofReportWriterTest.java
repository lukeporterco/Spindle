package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SteelHook04InvokeRedirectWrapOfflineProofReportWriterTest {
  @TempDir Path tempDirectory;

  private final SteelHook04InvokeRedirectWrapOfflineProofReportWriter writer =
      new SteelHook04InvokeRedirectWrapOfflineProofReportWriter();

  @Test
  void writesSchema1Target34MinecraftAndSteelHookVersion04() throws Exception {
    SteelHook04InvokeRedirectWrapOfflineProofReport report = sampleReport();
    Path output =
        tempDirectory.resolve(
            SteelHook04InvokeRedirectWrapOfflineProofReportWriter.REPORT_FILE_NAME);

    writer.write(output, report);

    assertEquals(
        SteelHook04InvokeRedirectWrapOfflineProofReportWriter.REPORT_FILE_NAME,
        output.getFileName().toString());
    String json = Files.readString(output, StandardCharsets.UTF_8);
    assertTrue(json.contains("\"schema\": 1"));
    assertTrue(json.contains("\"milestoneName\": \"Target-34\""));
    assertTrue(json.contains("\"target\": \"minecraft\""));
    assertTrue(json.contains("\"steelHookVersion\": \"0.4\""));
  }

  @Test
  void writesRequiredProofFieldsAndFalseSideEffects() {
    String json = writer.toJson(sampleReport()).toString();

    assertTrue(json.contains("INVOKE_REDIRECT"));
    assertTrue(json.contains("INVOKE_WRAP"));
    assertTrue(json.contains("invoke redirect replacement"));
    assertTrue(json.contains("invoke wrap replacement"));
    assertTrue(json.contains("expectedInvokeOwnerInternalName"));
    assertTrue(json.contains("expectedInvokeName"));
    assertTrue(json.contains("expectedInvokeDescriptor"));
    assertTrue(json.contains("expectedInvokeOpcode"));
    assertTrue(json.contains("wrappedDelegateOwnerInternalName"));
    assertTrue(json.contains("wrapperOwnerInternalName"));
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

  private SteelHook04InvokeRedirectWrapOfflineProofReport sampleReport() {
    return new SteelHook04InvokeRedirectWrapOfflineProofRunner()
        .run(
            SteelHook04TestFixtures.passedTarget32Report(),
            SteelHook04TestFixtures.passedTarget33Report());
  }
}
