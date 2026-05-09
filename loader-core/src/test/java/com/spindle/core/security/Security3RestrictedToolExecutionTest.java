package com.spindle.core.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spindle.core.LoaderMain;
import com.spindle.core.app.LoaderApplication;
import com.spindle.core.cli.LaunchArguments;
import com.spindle.core.diagnostics.JsonDiagnosticSink;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.execution.StandardGameLaunchExecutor;
import com.spindle.core.game.GameProvider;
import com.spindle.core.game.GameProviderResolver;
import com.spindle.core.launch.LaunchContext;
import com.spindle.core.metadata.ModMetadata;
import com.spindle.core.pipeline.ModpackPlanningPipeline;
import com.spindle.core.pipeline.ModpackPlanningResult;
import com.spindle.core.resolve.ResolvedModSet;
import com.spindle.core.runtime.CompiledRuntimeOrchestrator;
import com.spindle.core.runtime.CompiledRuntimeResult;
import com.spindle.core.runtime.RuntimePolicyFingerprint;
import com.spindle.core.security.risk.StaticRiskRuleId;
import com.spindle.core.security.tool.RestrictedToolOutputReader;
import com.spindle.core.security.tool.RestrictedToolProcessLauncher;
import com.spindle.core.security.tool.RestrictedToolRequest;
import com.spindle.core.security.tool.RestrictedToolResult;
import com.spindle.core.security.tool.SecurityRiskScanWorkerMain;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.AlphaLifecycle;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Security3RestrictedToolExecutionTest {
  @TempDir Path tempDirectory;

  @Test
  void workerMainScansCleanJarAndWritesDeterministicOutput() throws Exception {
    Path jarPath =
        createSchemaTwoModJar(
            tempDirectory.resolve("mods/clean.jar"),
            "cleanmod",
            Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
            Map.of(resourceName(AlphaLifecycle.class), readClassBytes(AlphaLifecycle.class)),
            true,
            List.of());
    Path outputPath = tempDirectory.resolve("worker-output/output.json");

    SecurityRiskScanWorkerMain workerMain = new SecurityRiskScanWorkerMain();
    assertEquals(0, workerMain.run(workerArgs(outputPath, "cleanmod", "mods/clean.jar", jarPath)));
    String firstOutput = Files.readString(outputPath, StandardCharsets.UTF_8);

    Files.delete(outputPath);
    assertEquals(0, workerMain.run(workerArgs(outputPath, "cleanmod", "mods/clean.jar", jarPath)));
    String secondOutput = Files.readString(outputPath, StandardCharsets.UTF_8);

    JsonObject report = JsonParser.parseString(firstOutput).getAsJsonObject();
    assertEquals(0, report.getAsJsonObject("summary").get("signalCount").getAsInt());
    assertEquals(0, report.getAsJsonArray("signals").size());
    assertEquals(firstOutput, secondOutput);
    assertFalse(Files.exists(tempDirectory.resolve("lifecycle.log")));
  }

  @Test
  void parentLauncherRunsWorkerAndMergesRiskSignalsIntoSecurityReport() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/process-risk.jar"),
        "processrisk",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(
            resourceName(AlphaLifecycle.class),
            readClassBytes(AlphaLifecycle.class),
            "com/example/risk/ProcessRisk.class",
            minimalClassBytes("java/lang/ProcessBuilder")),
        true,
        List.of());

    execute(true);

    JsonObject report = readSecurityReport();
    JsonObject toolIsolation = report.getAsJsonObject("toolIsolation");
    assertEquals("restricted-child-jvm", toolIsolation.get("mode").getAsString());
    assertEquals("static-risk-scan", toolIsolation.get("worker").getAsString());
    assertEquals("passed", toolIsolation.get("status").getAsString());
    assertEquals(
        ".spindle/security-tools/static-risk-scan/output.json",
        toolIsolation.get("outputPath").getAsString());
    assertEquals(
        "in-process-unrestricted-java", report.get("runtimeExecutionIsolationMode").getAsString());
    assertFalse(report.get("runtimeSandboxed").getAsBoolean());
    assertTrue(reportRiskRuleIds(report).contains(StaticRiskRuleId.RISK_PROCESS_001.id()));
    assertTrue(
        Files.isRegularFile(
            tempDirectory.resolve(".spindle/security-tools/static-risk-scan/output.json")));
  }

  @Test
  void modJarsAreNotPlacedOnWorkerClasspath() throws Exception {
    Path jarPath =
        createSchemaTwoModJar(
            tempDirectory.resolve("mods/classpath-check.jar"),
            "classpathcheck",
            Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
            Map.of(resourceName(AlphaLifecycle.class), readClassBytes(AlphaLifecycle.class)),
            true,
            List.of());
    RestrictedToolRequest request =
        RestrictedToolRequest.staticRiskScan(
            tempDirectory, List.of(resolvedMod("classpathcheck", jarPath)));

    RestrictedToolProcessLauncher launcher = new RestrictedToolProcessLauncher();
    @SuppressWarnings("unchecked")
    List<String> command = (List<String>) invokeHidden(launcher, "buildCommand", request);
    int classpathIndex = command.indexOf("-cp");
    String classpath = command.get(classpathIndex + 1);

    assertFalse(classpath.contains(jarPath.toString()));
    assertTrue(command.stream().anyMatch(part -> part.contains(jarPath.toString())));
  }

  @Test
  void riskScannerDoesNotExecuteLifecycleHandlers() throws Exception {
    Path jarPath =
        createSchemaTwoModJar(
            tempDirectory.resolve("mods/non-executed.jar"),
            "nonexecuted",
            Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
            Map.of(resourceName(AlphaLifecycle.class), readClassBytes(AlphaLifecycle.class)),
            true,
            List.of());

    RestrictedToolResult result =
        new RestrictedToolProcessLauncher()
            .runStaticRiskScan(tempDirectory, List.of(resolvedMod("nonexecuted", jarPath)));

    assertEquals(RestrictedToolResult.STATUS_PASSED, result.status());
    assertFalse(Files.exists(tempDirectory.resolve("lifecycle.log")));
    assertFalse(Files.exists(tempDirectory.resolve("generated/nonexecuted/alpha.marker")));
  }

  @Test
  void invalidWorkerOutputProducesSecTool001Fatal() throws Exception {
    Path jarPath =
        createSchemaTwoModJar(
            tempDirectory.resolve("mods/invalid-output.jar"),
            "invalidoutput",
            Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
            Map.of(resourceName(AlphaLifecycle.class), readClassBytes(AlphaLifecycle.class)),
            true,
            List.of());

    RestrictedToolResult result =
        customLauncher(InvalidOutputWorkerMain.class)
            .runStaticRiskScan(tempDirectory, List.of(resolvedMod("invalidoutput", jarPath)));

    assertEquals(RestrictedToolResult.STATUS_FAILED, result.status());
    assertTrue(
        result.findings().stream()
            .anyMatch(finding -> finding.ruleId() == SecurityRuleId.SEC_TOOL_001));
    assertTrue(result.findings().getFirst().message().contains("validation failed"));
  }

  @Test
  void nonzeroWorkerExitProducesSecTool001Fatal() throws Exception {
    Path jarPath =
        createSchemaTwoModJar(
            tempDirectory.resolve("mods/nonzero-worker.jar"),
            "nonzeroworker",
            Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
            Map.of(resourceName(AlphaLifecycle.class), readClassBytes(AlphaLifecycle.class)),
            true,
            List.of());

    RestrictedToolResult result =
        customLauncher(ExitFailureWorkerMain.class)
            .runStaticRiskScan(tempDirectory, List.of(resolvedMod("nonzeroworker", jarPath)));

    assertEquals(RestrictedToolResult.STATUS_FAILED, result.status());
    assertTrue(result.findings().getFirst().message().contains("status 7"));
    assertEquals(".spindle/security-tools/static-risk-scan/output.json", result.outputPath());
  }

  @Test
  void toolFailureBlocksLifecycleExecution() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/blocked-by-tool.jar"),
        "blockedbytool",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(resourceName(AlphaLifecycle.class), readClassBytes(AlphaLifecycle.class)),
        true,
        List.of());

    LoaderException exception =
        assertThrows(
            LoaderException.class,
            () ->
                executeWithSecurityValidator(
                    new SecurityValidator(customLauncher(ExitFailureWorkerMain.class)), false));

    assertTrue(exception.getMessage().contains(SecurityRuleId.SEC_TOOL_001.id()));
    assertFalse(Files.exists(tempDirectory.resolve("lifecycle.log")));
    JsonObject report = readSecurityReport();
    assertEquals("blocked", report.get("state").getAsString());
    assertEquals("failed", report.getAsJsonObject("toolIsolation").get("status").getAsString());
  }

  @Test
  void successfulWorkerDoesNotChangeExistingFindingsExceptToolIsolation() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/risky.jar"),
        "risky",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(
            resourceName(AlphaLifecycle.class),
            readClassBytes(AlphaLifecycle.class),
            "com/example/risk/ProcessRisk.class",
            minimalClassBytes("java/lang/ProcessBuilder")),
        true,
        List.of());

    execute(true);

    JsonObject report = readSecurityReport();
    assertEquals(3, report.get("warningCount").getAsInt());
    assertFalse(findingRuleIds(report).contains(SecurityRuleId.SEC_TOOL_001.id()));
    assertTrue(findingRuleIds(report).contains(SecurityRuleId.SEC_TRUST_001.id()));
    assertTrue(findingRuleIds(report).contains(SecurityRuleId.SEC_TRUST_006.id()));
    assertTrue(findingRuleIds(report).contains(StaticRiskRuleId.RISK_PROCESS_001.id()));
  }

  @Test
  void workerOutputPathIsNormalizedRelativeAndReportContainsNoRawTempPaths() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/native-risk.jar"),
        "nativerisk",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(
            resourceName(AlphaLifecycle.class),
            readClassBytes(AlphaLifecycle.class),
            "natives/example.dylib",
            new byte[] {1, 2, 3}),
        true,
        List.of());

    execute(true);

    String reportText = Files.readString(tempDirectory.resolve("spindle.security-report.json"));
    JsonObject report = JsonParser.parseString(reportText).getAsJsonObject();
    assertEquals(
        ".spindle/security-tools/static-risk-scan/output.json",
        report.getAsJsonObject("toolIsolation").get("outputPath").getAsString());
    assertFalse(
        reportText.contains(
            tempDirectory.toAbsolutePath().normalize().toString().replace('\\', '/')));
  }

  private Object invokeHidden(Object target, String methodName, Object argument) throws Exception {
    Method method = target.getClass().getDeclaredMethod(methodName, argument.getClass());
    method.setAccessible(true);
    return method.invoke(target, argument);
  }

  private RestrictedToolProcessLauncher customLauncher(Class<?> workerMainClass) throws Exception {
    List<Path> classpathEntries =
        List.of(
            classpathEntryFor(SecurityRiskScanWorkerMain.class),
            classpathEntryFor(Gson.class),
            classpathEntryFor(workerMainClass));
    return new RestrictedToolProcessLauncher(
        new com.spindle.core.process.JavaExecutableResolver(),
        new RestrictedToolOutputReader(),
        new com.spindle.core.security.risk.StaticRiskAnalyzer(),
        workerMainClass.getName(),
        classpathEntries,
        30L);
  }

  private Path classpathEntryFor(Class<?> type) throws URISyntaxException {
    return Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI())
        .toAbsolutePath()
        .normalize();
  }

  private String[] workerArgs(Path outputPath, String modId, String relativePath, Path jarPath) {
    return new String[] {
      "--working-directory",
      tempDirectory.toString(),
      "--output",
      outputPath.toString(),
      "--mod",
      modId + "|" + relativePath + "|" + "0".repeat(64) + "|" + jarPath
    };
  }

  private JsonObject readSecurityReport() throws IOException {
    return JsonParser.parseString(
            Files.readString(
                tempDirectory.resolve("spindle.security-report.json"), StandardCharsets.UTF_8))
        .getAsJsonObject();
  }

  private List<String> reportRiskRuleIds(JsonObject report) {
    List<String> ruleIds = new ArrayList<>();
    JsonArray signals = report.getAsJsonObject("riskSignals").getAsJsonArray("signals");
    for (var element : signals) {
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
        ModMetadata.Storage.disabled());
  }

  private Path createSchemaTwoModJar(
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
    return jarPath;
  }

  private Map<String, byte[]> withMetadata(Map<String, byte[]> entries, String metadataJson) {
    LinkedHashMap<String, byte[]> allEntries = new LinkedHashMap<>();
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

  private String executeWithSecurityValidator(
      SecurityValidator securityValidator, boolean validateOnly) throws Exception {
    JsonDiagnosticSink sink =
        new JsonDiagnosticSink(tempDirectory.resolve("diagnostics/startup-trace.json"));
    try {
      return captureStdout(
          () -> {
            LaunchContext context = createLaunchContext(validateOnly);
            GameProvider gameProvider = new GameProviderResolver().resolve(context);
            ModpackPlanningResult planningResult =
                new ModpackPlanningPipeline().plan(context, gameProvider, sink);
            CompiledRuntimeResult compiledRuntimeResult =
                new CompiledRuntimeOrchestrator()
                    .compile(context, planningResult, "universal", sink);
            SecurityValidationContext validationContext =
                new SecurityValidationContext(
                    context,
                    planningResult,
                    compiledRuntimeResult.profile(),
                    new RuntimePolicyFingerprint().compute(context));
            SecurityValidationResult validationResult =
                securityValidator.validate(validationContext);
            new SecurityValidationReportWriter()
                .write(
                    tempDirectory.resolve("spindle.security-report.json"),
                    securityValidator.toReport(validationContext, validationResult));
            new StandardGameLaunchExecutor()
                .execute(
                    context,
                    gameProvider,
                    planningResult,
                    compiledRuntimeResult.profile(),
                    validationResult,
                    sink);
          });
    } finally {
      sink.write();
    }
  }

  private LaunchContext createLaunchContext(boolean validateOnly) {
    return new LaunchContext(
        tempDirectory,
        tempDirectory.resolve("mods"),
        ValidationGameMain.class.getName(),
        "sample",
        List.of(),
        validateOnly,
        false,
        false,
        false,
        LoaderMain.LOADER_VERSION,
        Runtime.version().feature(),
        LoaderMain.TARGET_MINECRAFT_VERSION);
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

  public static final class InvalidOutputWorkerMain {
    public static void main(String[] args) throws Exception {
      Path outputPath = findOutputPath(args);
      Files.createDirectories(outputPath.getParent());
      Files.writeString(outputPath, "{not-json", StandardCharsets.UTF_8);
    }
  }

  public static final class ExitFailureWorkerMain {
    public static void main(String[] args) {
      System.err.println("synthetic worker failure");
      System.exit(7);
    }
  }

  private static Path findOutputPath(String[] args) {
    for (int index = 0; index < args.length - 1; index++) {
      if ("--output".equals(args[index])) {
        return Path.of(args[index + 1]);
      }
    }
    throw new IllegalArgumentException("Missing --output");
  }
}
