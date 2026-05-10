package com.spindle.core.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spindle.core.app.LoaderApplication;
import com.spindle.core.cli.LaunchArguments;
import com.spindle.core.diagnostics.JsonDiagnosticSink;
import com.spindle.core.metadata.ModMetadata;
import com.spindle.core.resolve.ResolvedModSet;
import com.spindle.core.security.risk.StaticRiskAnalyzer;
import com.spindle.core.security.risk.StaticRiskRuleId;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.AlphaLifecycle;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.BetaLifecycle;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Security2StaticRiskSignalsTest {
  @TempDir Path tempDirectory;

  private final StaticRiskAnalyzer analyzer = new StaticRiskAnalyzer();

  @Test
  void cleanSchemaTwoModHasZeroRiskSignals() throws Exception {
    StaticRiskAnalyzer.Analysis analysis =
        analyzeJar(
            "cleanmod",
            "clean.jar",
            Map.of(resourceName(AlphaLifecycle.class), readClassBytes(AlphaLifecycle.class)));

    assertEquals(0, analysis.summary().signalCount());
    assertEquals(0, analysis.summary().modCountWithSignals());
    assertTrue(analysis.signals().isEmpty());
    assertTrue(analysis.findings().isEmpty());
  }

  @Test
  void processApiReferenceProducesRiskProcess001() throws Exception {
    StaticRiskAnalyzer.Analysis analysis =
        analyzeJar(
            "processmod",
            "process.jar",
            Map.of(
                "com/example/risk/ProcessRisk.class",
                minimalClassBytes("java/lang/Runtime", "exec", "java/lang/ProcessBuilder")));

    assertRulePresent(analysis, StaticRiskRuleId.RISK_PROCESS_001);
  }

  @Test
  void nativeFileEntryProducesRiskNative001() throws Exception {
    StaticRiskAnalyzer.Analysis analysis =
        analyzeJar("nativemod", "native.jar", Map.of("natives/example.dll", new byte[] {1, 2, 3}));

    assertRulePresent(analysis, StaticRiskRuleId.RISK_NATIVE_001);
  }

  @Test
  void networkApiReferenceProducesRiskNetwork001() throws Exception {
    StaticRiskAnalyzer.Analysis analysis =
        analyzeJar(
            "networkmod",
            "network.jar",
            Map.of("com/example/risk/NetworkRisk.class", minimalClassBytes("java/net/Socket")));

    assertRulePresent(analysis, StaticRiskRuleId.RISK_NETWORK_001);
  }

  @Test
  void reflectionApiReferenceProducesRiskReflection001() throws Exception {
    StaticRiskAnalyzer.Analysis analysis =
        analyzeJar(
            "reflectionmod",
            "reflection.jar",
            Map.of(
                "com/example/risk/ReflectionRisk.class",
                minimalClassBytes(
                    "java/lang/reflect/Method",
                    "java/lang/invoke/MethodHandles",
                    "java/lang/invoke/MethodType")));

    assertRulePresent(analysis, StaticRiskRuleId.RISK_REFLECTION_001);
  }

  @Test
  void unsafeReferenceProducesRiskUnsafe001() throws Exception {
    StaticRiskAnalyzer.Analysis analysis =
        analyzeJar(
            "unsafemod",
            "unsafe.jar",
            Map.of(
                "com/example/risk/UnsafeRisk.class",
                minimalClassBytes("sun/misc/Unsafe", "jdk/internal/misc/Unsafe")));

    assertRulePresent(analysis, StaticRiskRuleId.RISK_UNSAFE_001);
  }

  @Test
  void dynamicClassloadReferenceProducesRiskDynamicClassload001() throws Exception {
    StaticRiskAnalyzer.Analysis analysis =
        analyzeJar(
            "classloadermod",
            "classloader.jar",
            Map.of(
                "com/example/risk/ClassloaderRisk.class",
                minimalClassBytes("java/lang/ClassLoader", "defineClass")));

    assertRulePresent(analysis, StaticRiskRuleId.RISK_DYNAMIC_CLASSLOAD_001);
  }

  @Test
  void scriptApiReferenceProducesRiskScript001() throws Exception {
    StaticRiskAnalyzer.Analysis analysis =
        analyzeJar(
            "scriptmod",
            "script.jar",
            Map.of(
                "com/example/risk/ScriptRisk.class",
                minimalClassBytes("javax/script/ScriptEngineManager")));

    assertRulePresent(analysis, StaticRiskRuleId.RISK_SCRIPT_001);
  }

  @Test
  void serviceProviderEntryProducesRiskService001() throws Exception {
    StaticRiskAnalyzer.Analysis analysis =
        analyzeJar(
            "servicemod",
            "service.jar",
            Map.of(
                "META-INF/services/com.example.Service",
                "com.example.impl.RealService\n".getBytes(StandardCharsets.UTF_8)));

    assertRulePresent(analysis, StaticRiskRuleId.RISK_SERVICE_001);
  }

  @Test
  void nestedJarEntryProducesRiskEmbeddedJar001() throws Exception {
    StaticRiskAnalyzer.Analysis analysis =
        analyzeJar("embeddedmod", "embedded.jar", Map.of("libs/payload.jar", new byte[] {1, 2, 3}));

    assertRulePresent(analysis, StaticRiskRuleId.RISK_EMBEDDED_JAR_001);
  }

  @Test
  void malformedClassEntryProducesRiskClassfile001() throws Exception {
    StaticRiskAnalyzer.Analysis analysis =
        analyzeJar(
            "brokenmod",
            "broken.jar",
            Map.of("com/example/risk/Broken.class", new byte[] {1, 2, 3}));

    assertRulePresent(analysis, StaticRiskRuleId.RISK_CLASSFILE_001);
  }

  @Test
  void riskSignalsDoNotBlockLifecycleExecution() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/risky-but-runnable.jar"),
        "riskybutrunnable",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(
            resourceName(AlphaLifecycle.class),
            readClassBytes(AlphaLifecycle.class),
            "com/example/risk/ProcessRisk.class",
            minimalClassBytes("java/lang/ProcessBuilder")),
        true,
        List.of());

    execute(false);

    JsonObject report = readSecurityReport();
    assertEquals("validated", report.get("state").getAsString());
    assertEquals(0, report.get("fatalCount").getAsInt());
    assertTrue(Files.exists(tempDirectory.resolve("lifecycle.log")));
    assertTrue(reportRiskRuleIds(report).contains(StaticRiskRuleId.RISK_PROCESS_001.id()));
    assertTrue(findingRuleIds(report).contains(StaticRiskRuleId.RISK_PROCESS_001.id()));
  }

  @Test
  void riskSignalOrderingIsDeterministic() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/beta-risk.jar"),
        "betarisk",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(
            resourceName(AlphaLifecycle.class),
            readClassBytes(AlphaLifecycle.class),
            "com/example/risk/BetaNetworkRisk.class",
            minimalClassBytes("java/net/Socket")),
        true,
        List.of());
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/alpha-risk.jar"),
        "alpharisk",
        Map.of("BOOTSTRAP", List.of(BetaLifecycle.class.getName() + "::bootstrap")),
        Map.of(
            resourceName(BetaLifecycle.class),
            readClassBytes(BetaLifecycle.class),
            "com/example/risk/AlphaProcessRisk.class",
            minimalClassBytes("java/lang/ProcessBuilder"),
            "libs/payload.jar",
            new byte[] {1, 2, 3}),
        true,
        List.of());

    execute(true);
    List<String> firstOrder = reportRiskOrder(readSecurityReport());

    execute(true);
    List<String> secondOrder = reportRiskOrder(readSecurityReport());

    assertEquals(firstOrder, secondOrder);
    assertEquals(
        List.of(
            "warning|RISK-EMBEDDED-JAR-001|alpharisk|jar-entry|libs/payload.jar",
            "warning|RISK-NETWORK-001|betarisk|class-entry|com/example/risk/BetaNetworkRisk.class",
            "warning|RISK-PROCESS-001|alpharisk|class-entry|com/example/risk/AlphaProcessRisk.class"),
        firstOrder);
  }

  @Test
  void securityReportContainsNoRawAbsolutePathsInRiskSignals() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/native-risk.jar"),
        "nativerisk",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(
            resourceName(AlphaLifecycle.class),
            readClassBytes(AlphaLifecycle.class),
            "natives/example.dylib",
            new byte[] {4, 5, 6}),
        true,
        List.of());

    execute(true);

    String report = Files.readString(tempDirectory.resolve("spindle.security-report.json"));
    assertFalse(
        report.contains(tempDirectory.toAbsolutePath().normalize().toString().replace('\\', '/')));
    assertTrue(report.contains("natives/example.dylib"));
    assertTrue(report.contains("mods/native-risk.jar"));
  }

  private void assertRulePresent(
      StaticRiskAnalyzer.Analysis analysis, StaticRiskRuleId expectedRuleId) {
    assertTrue(
        analysis.signals().stream().anyMatch(signal -> signal.ruleId() == expectedRuleId),
        () -> "Expected signal " + expectedRuleId.id() + " but saw " + analysis.signals());
    assertTrue(
        analysis.findings().stream()
            .anyMatch(finding -> finding.ruleId().id().equals(expectedRuleId.id())),
        () -> "Expected finding " + expectedRuleId.id() + " but saw " + analysis.findings());
  }

  private StaticRiskAnalyzer.Analysis analyzeJar(
      String modId, String jarFileName, Map<String, byte[]> entries) throws Exception {
    Path jarPath = tempDirectory.resolve("scanner").resolve(jarFileName);
    createJar(jarPath, entries);
    return analyzer.analyze(List.of(resolvedMod(modId, jarPath)));
  }

  private ResolvedModSet.ResolvedMod resolvedMod(String modId, Path jarPath) {
    return new ResolvedModSet.ResolvedMod(
        modId,
        "1.0.0",
        Path.of("mods/" + jarPath.getFileName()),
        jarPath,
        "0".repeat(64),
        Map.of(),
        Map.of(),
        Map.of(),
        2,
        Map.of(),
        List.of(),
        ModMetadata.Storage.disabled(),
        ModMetadata.Services.empty());
  }

  private JsonObject readSecurityReport() throws IOException {
    return JsonParser.parseString(
            Files.readString(
                tempDirectory.resolve("spindle.security-report.json"), StandardCharsets.UTF_8))
        .getAsJsonObject();
  }

  private List<String> reportRiskRuleIds(JsonObject report) {
    List<String> ruleIds = new ArrayList<>();
    for (var element : report.getAsJsonObject("riskSignals").getAsJsonArray("signals")) {
      ruleIds.add(element.getAsJsonObject().get("ruleId").getAsString());
    }
    return ruleIds;
  }

  private List<String> findingRuleIds(JsonObject report) {
    List<String> ruleIds = new ArrayList<>();
    for (var element : report.getAsJsonArray("findings")) {
      ruleIds.add(element.getAsJsonObject().get("ruleId").getAsString());
    }
    return ruleIds;
  }

  private List<String> reportRiskOrder(JsonObject report) {
    List<String> order = new ArrayList<>();
    JsonArray signals = report.getAsJsonObject("riskSignals").getAsJsonArray("signals");
    for (var element : signals) {
      JsonObject signal = element.getAsJsonObject();
      JsonObject location = signal.getAsJsonObject("location");
      order.add(
          signal.get("severity").getAsString()
              + "|"
              + signal.get("ruleId").getAsString()
              + "|"
              + signal.get("modId").getAsString()
              + "|"
              + location.get("kind").getAsString()
              + "|"
              + location.get("value").getAsString());
    }
    return order;
  }

  private byte[] minimalClassBytes(String... utf8Values) throws IOException {
    TreeSet<String> values = new TreeSet<>(List.of(utf8Values));
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try (DataOutputStream data = new DataOutputStream(outputStream)) {
      data.writeInt(0xCAFEBABE);
      data.writeShort(0);
      data.writeShort(69);
      data.writeShort(values.size() + 1);
      for (String value : values) {
        data.writeByte(1);
        data.writeUTF(value);
      }
      data.writeShort(0x0021);
      data.writeShort(0);
      data.writeShort(0);
      data.writeShort(0);
      data.writeShort(0);
      data.writeShort(0);
      data.writeShort(0);
    }
    return outputStream.toByteArray();
  }

  private void createSchemaTwoModJar(
      Path jarPath,
      String modId,
      Map<String, List<String>> lifecycle,
      Map<String, byte[]> entries,
      boolean storageEnabled,
      List<String> permissions)
      throws IOException {
    createJar(
        jarPath,
        withMetadata(entries, schemaTwoMetadata(modId, lifecycle, storageEnabled, permissions)));
  }

  private Map<String, byte[]> withMetadata(Map<String, byte[]> entries, String metadataJson) {
    java.util.LinkedHashMap<String, byte[]> allEntries = new java.util.LinkedHashMap<>();
    allEntries.put("loader.mod.json", metadataJson.getBytes(StandardCharsets.UTF_8));
    entries.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .forEach(entry -> allEntries.put(entry.getKey(), entry.getValue()));
    return allEntries;
  }

  private void createJar(Path jarPath, Map<String, byte[]> entries) throws IOException {
    Files.createDirectories(jarPath.getParent());
    try (OutputStream outputStream = Files.newOutputStream(jarPath);
        JarOutputStream jarOutputStream = new JarOutputStream(outputStream)) {
      for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
        jarOutputStream.putNextEntry(new JarEntry(entry.getKey()));
        jarOutputStream.write(entry.getValue());
        jarOutputStream.closeEntry();
      }
    }
  }

  private String schemaTwoMetadata(
      String modId,
      Map<String, List<String>> lifecycle,
      boolean storageEnabled,
      List<String> permissions) {
    StringBuilder lifecycleJson = new StringBuilder();
    boolean firstPhase = true;
    for (Map.Entry<String, List<String>> entry :
        lifecycle.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
      if (!firstPhase) {
        lifecycleJson.append(",\n");
      }
      lifecycleJson.append("    \"").append(entry.getKey()).append("\": [\n");
      for (int index = 0; index < entry.getValue().size(); index++) {
        lifecycleJson.append("      \"").append(entry.getValue().get(index)).append("\"");
        if (index + 1 < entry.getValue().size()) {
          lifecycleJson.append(",");
        }
        lifecycleJson.append("\n");
      }
      lifecycleJson.append("    ]");
      firstPhase = false;
    }

    return """
        {
          "schema": 2,
          "id": "%s",
          "version": "1.0.0",
          "side": "universal",
          "depends": {
            "loader": ">=0.1.0",
            "java": ">=25",
            "minecraft": ">=26.1.2"
          },
          "breaks": {},
          "lifecycle": {
        %s
          },
          "permissions": %s,
          "storage": {
            "config": %s,
            "data": %s,
            "cache": %s,
            "generated": %s
          }
        }
        """
        .formatted(
            modId,
            lifecycleJson,
            toJsonArray(permissions),
            Boolean.toString(storageEnabled),
            Boolean.toString(storageEnabled),
            Boolean.toString(storageEnabled),
            Boolean.toString(storageEnabled));
  }

  private String toJsonArray(List<String> values) {
    return values.stream()
        .map(value -> "\"" + value + "\"")
        .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
  }

  private String execute(boolean validateOnly) throws Exception {
    JsonDiagnosticSink sink =
        new JsonDiagnosticSink(tempDirectory.resolve("diagnostics/startup-trace.json"));
    try {
      return captureStdout(
          () ->
              new LoaderApplication()
                  .run(
                      tempDirectory,
                      new LaunchArguments(
                          ValidationGameMain.class.getName(),
                          "sample",
                          List.of(),
                          validateOnly,
                          false,
                          false,
                          false,
                          null,
                          null,
                          null,
                          false),
                      sink));
    } finally {
      sink.write();
    }
  }

  private String captureStdout(ThrowingRunnable runnable) throws Exception {
    PrintStream originalOut = System.out;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try (PrintStream replacement = new PrintStream(outputStream, true, StandardCharsets.UTF_8)) {
      System.setOut(replacement);
      runnable.run();
    } finally {
      System.setOut(originalOut);
    }
    return outputStream.toString(StandardCharsets.UTF_8);
  }

  private byte[] readClassBytes(Class<?> type) throws IOException {
    try (var inputStream = type.getClassLoader().getResourceAsStream(resourceName(type))) {
      if (inputStream == null) {
        throw new IOException("Missing class bytes for " + resourceName(type));
      }
      return inputStream.readAllBytes();
    }
  }

  private String resourceName(Class<?> type) {
    return type.getName().replace('.', '/') + ".class";
  }

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws Exception;
  }

  public static final class ValidationGameMain {
    public static void main(String[] args) {}
  }
}
