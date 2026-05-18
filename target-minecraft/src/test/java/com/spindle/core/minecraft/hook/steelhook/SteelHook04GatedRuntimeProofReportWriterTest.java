package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftServerRuntimePlan;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SteelHook04GatedRuntimeProofReportWriterTest {
  @TempDir Path tempDirectory;

  private final SteelHook04GatedRuntimeProofReportWriter writer =
      new SteelHook04GatedRuntimeProofReportWriter();

  @Test
  void writesSchema1Target35MinecraftAndSteelHookVersion04() throws Exception {
    SteelHook04GatedRuntimeProofReport report = sampleReport();
    Path output = tempDirectory.resolve(SteelHook04GatedRuntimeProofReportWriter.REPORT_FILE_NAME);

    writer.write(output, report);

    assertEquals(
        SteelHook04GatedRuntimeProofReportWriter.REPORT_FILE_NAME, output.getFileName().toString());
    String json = Files.readString(output, StandardCharsets.UTF_8);
    assertTrue(json.contains("\"schema\": 1"));
    assertTrue(json.contains("\"milestoneName\": \"Target-35\""));
    assertTrue(json.contains("\"target\": \"minecraft\""));
    assertTrue(json.contains("\"steelHookVersion\": \"0.4\""));
  }

  @Test
  void writesRequiredProofFieldsAndFalseSideEffects() throws Exception {
    String json = writer.toJson(sampleReport()).toString();

    assertTrue(json.contains("minecraft-steelhook-0-4-gated-runtime-proof"));
    assertTrue(json.contains("RETURN_VALUE_INTERCEPT"));
    assertTrue(json.contains("INVOKE_REDIRECT"));
    assertTrue(json.contains("INVOKE_WRAP"));
    assertTrue(json.contains("\"runtimeClassLoadingPathEnabled\":true"));
    assertTrue(json.contains("\"classLoadingOccurred\":true"));
    assertTrue(json.contains("\"targetClassDefinitionOccurred\":true"));
    assertTrue(json.contains("\"classInitialized\":false"));
    assertTrue(json.contains("\"targetMethodInvoked\":false"));
    assertTrue(json.contains("\"wrapperExecuted\":false"));
    assertTrue(json.contains("\"unsupportedPrimitivePlanRejectedBeforeClassDefinition\":true"));
    assertTrue(json.contains("\"unsupportedPrimitivePlanClassDefinitionAttempted\":false"));
    assertTrue(json.contains("\"serverLaunchOccurred\":false"));
    assertTrue(json.contains("\"minecraftMainInvoked\":false"));
    assertTrue(json.contains("\"hookInstallationOccurred\":false"));
    assertTrue(json.contains("\"runtimeDispatchOccurred\":false"));
    assertTrue(json.contains("\"publicApiExposed\":false"));
    assertTrue(json.contains("\"javaAgentUsed\":false"));
    assertTrue(json.contains("\"mixinUsed\":false"));
    assertTrue(json.contains("\"javaModExecutionSandboxed\":false"));
    assertTrue(json.contains("originalClassSha256"));
    assertTrue(json.contains("transformedClassSha256"));
    assertTrue(json.contains("originalCodeLength"));
    assertTrue(json.contains("transformedCodeLength"));
  }

  @Test
  void doesNotIncludeRawBytePayloadKeys() throws Exception {
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

  private SteelHook04GatedRuntimeProofReport sampleReport() throws Exception {
    Path serverJar =
        SteelHook02TestFixtures.createRuntimeJar(
            tempDirectory.resolve("hook-server.jar"),
            SteelHook02TestFixtures.readResourceBytes("net/minecraft/server/Main.class"));
    MinecraftServerRuntimePlan runtimePlan = SteelHook02TestFixtures.runtimePlan(serverJar);
    return new SteelHook04GatedRuntimeProofRunner()
        .run(
            runtimePlan,
            SteelHook04TestFixtures.passedTarget32Report(),
            SteelHook04TestFixtures.passedTarget33Report(),
            SteelHook04TestFixtures.passedTarget34Report(),
            tempDirectory.resolve("fixtures"));
  }
}
