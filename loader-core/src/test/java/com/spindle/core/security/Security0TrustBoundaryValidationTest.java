package com.spindle.core.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spindle.core.app.LoaderApplication;
import com.spindle.core.cli.LaunchArguments;
import com.spindle.core.diagnostics.JsonDiagnosticSink;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.runtime.CompiledModpackProfile;
import com.spindle.core.runtime.CompiledModpackProfileFingerprint;
import com.spindle.core.runtime.CompiledModpackProfileReader;
import com.spindle.core.runtime.CompiledModpackProfileWriter;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.AlphaLifecycle;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.InvalidLifecycleHandler;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.SecurityReportAwareLifecycle;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Security0TrustBoundaryValidationTest {
  @TempDir Path tempDirectory;

  private final CompiledModpackProfileReader compiledProfileReader =
      new CompiledModpackProfileReader();
  private final CompiledModpackProfileWriter compiledProfileWriter =
      new CompiledModpackProfileWriter();
  private final CompiledModpackProfileFingerprint compiledProfileFingerprint =
      new CompiledModpackProfileFingerprint();

  @Test
  void cleanSchemaTwoModWritesValidatedSecurityReportDuringValidateOnly() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/clean.jar"),
        "cleanmod",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(resourceName(AlphaLifecycle.class), readClassBytes(AlphaLifecycle.class)),
        true,
        List.of());

    execute(true);

    JsonObject report = readSecurityReport();
    assertEquals("validated", report.get("state").getAsString());
    assertEquals(0, report.get("fatalCount").getAsInt());
    assertEquals(2, report.get("warningCount").getAsInt());
    assertEquals(
        "in-process-unrestricted-java", report.get("executionIsolationMode").getAsString());
    assertFalse(report.get("sandboxed").getAsBoolean());
    assertEquals("not-sandboxed", report.get("sandboxClaim").getAsString());
    assertTrue(report.get("securityPolicyFingerprint").getAsString().matches("[0-9a-f]{64}"));
  }

  @Test
  void securityReportExistsBeforeLifecycleExecution() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/report-aware.jar"),
        "reportaware",
        Map.of("BOOTSTRAP", List.of(SecurityReportAwareLifecycle.class.getName() + "::bootstrap")),
        Map.of(
            resourceName(SecurityReportAwareLifecycle.class),
            readClassBytes(SecurityReportAwareLifecycle.class)),
        true,
        List.of());

    execute(false);

    List<String> log =
        Files.readAllLines(tempDirectory.resolve("lifecycle.log"), StandardCharsets.UTF_8);
    assertEquals(List.of("report-exists=true"), log);
    assertTrue(Files.exists(tempDirectory.resolve("spindle.security-report.json")));
  }

  @Test
  void loaderOwnedPackageProducesSecPackage001() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/loader-owned.jar"),
        "loaderowned",
        Map.of("BOOTSTRAP", List.of("com.spindle.core.Hijack::bootstrap")),
        Map.of("com/spindle/core/Hijack.class", new byte[] {1, 2, 3}),
        true,
        List.of());

    execute(true);

    JsonObject report = readSecurityReport();
    assertEquals("blocked", report.get("state").getAsString());
    assertTrue(ruleIds(report).contains(SecurityRuleId.SEC_PACKAGE_001.id()));
  }

  @Test
  void protectedPlatformPackageProducesSecPackage002() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/platform-owned.jar"),
        "platformowned",
        Map.of("BOOTSTRAP", List.of("net.minecraft.BadPatch::bootstrap")),
        Map.of("net/minecraft/BadPatch.class", new byte[] {1, 2, 3}),
        true,
        List.of());

    execute(true);

    assertTrue(ruleIds(readSecurityReport()).contains(SecurityRuleId.SEC_PACKAGE_002.id()));
  }

  @Test
  void shadowedSpindleApiClassProducesSecClass001() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/shadowed-api.jar"),
        "shadowapi",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(
            resourceName(AlphaLifecycle.class),
            readClassBytes(AlphaLifecycle.class),
            "com/spindle/api/ModContext.class",
            new byte[] {1, 2, 3}),
        true,
        List.of());

    execute(true);

    assertTrue(ruleIds(readSecurityReport()).contains(SecurityRuleId.SEC_CLASS_001.id()));
  }

  @Test
  void invalidLifecycleMethodSignatureIncludesSecLifecycle002() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/invalid-signature.jar"),
        "invalidsignature",
        Map.of("BOOTSTRAP", List.of(InvalidLifecycleHandler.class.getName() + "::bootstrap")),
        Map.of(
            resourceName(InvalidLifecycleHandler.class),
            readClassBytes(InvalidLifecycleHandler.class)),
        true,
        List.of());

    LoaderException exception = assertThrows(LoaderException.class, () -> execute(false));

    assertTrue(exception.getMessage().contains(SecurityRuleId.SEC_LIFECYCLE_002.id()));
    assertTrue(
        exception
            .getMessage()
            .contains("public static void bootstrap(com.spindle.api.ModContext)"));
  }

  @Test
  void requestedPermissionsProduceWarningWithoutBlockingExecution() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/permitted.jar"),
        "permittedmod",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(resourceName(AlphaLifecycle.class), readClassBytes(AlphaLifecycle.class)),
        true,
        List.of("filesystem.write", "network.outbound"));

    execute(false);

    JsonObject report = readSecurityReport();
    assertEquals("validated", report.get("state").getAsString());
    assertEquals(0, report.get("fatalCount").getAsInt());
    assertEquals(4, report.get("warningCount").getAsInt());
    assertTrue(ruleIds(report).contains(SecurityRuleId.SEC_PERM_001.id()));
    assertTrue(Files.exists(tempDirectory.resolve("lifecycle.log")));
  }

  @Test
  void cacheValidationFailureThatRebuildsProducesSecCache001() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/cache-warning.jar"),
        "cachewarning",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(resourceName(AlphaLifecycle.class), readClassBytes(AlphaLifecycle.class)),
        true,
        List.of());

    execute(true);

    JsonObject cachedProfile = readCachedProfileJson();
    cachedProfile.getAsJsonObject("loader").addProperty("version", "0.0.0");
    Files.writeString(cachedProfilePath(), cachedProfile.toString(), StandardCharsets.UTF_8);

    execute(true);

    JsonObject report = readSecurityReport();
    assertEquals("validated", report.get("state").getAsString());
    assertTrue(ruleIds(report).contains(SecurityRuleId.SEC_CACHE_001.id()));
  }

  @Test
  void pathTraversalInCompiledContextPlanIsRejected() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/path-traversal.jar"),
        "pathmod",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(resourceName(AlphaLifecycle.class), readClassBytes(AlphaLifecycle.class)),
        true,
        List.of());

    execute(true);
    rewriteCachedProfile(profile -> withFirstContextConfigDirectory(profile, "../escape"));

    execute(true);

    JsonObject report = readSecurityReport();
    assertEquals("blocked", report.get("state").getAsString());
    assertTrue(ruleIds(report).contains(SecurityRuleId.SEC_PATH_002.id()));
  }

  @Test
  void fatalSecurityFindingsBlockLifecycleExecution() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/blocked.jar"),
        "blockedmod",
        Map.of("BOOTSTRAP", List.of("com.spindle.core.Blocked::bootstrap")),
        Map.of("com/spindle/core/Blocked.class", new byte[] {1, 2, 3}),
        true,
        List.of());

    LoaderException exception = assertThrows(LoaderException.class, () -> execute(false));

    assertTrue(exception.getMessage().contains(SecurityRuleId.SEC_PACKAGE_001.id()));
    assertFalse(Files.exists(tempDirectory.resolve("lifecycle.log")));
    assertEquals("blocked", readSecurityReport().get("state").getAsString());
  }

  @Test
  void securityReportOrderingIsDeterministic() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/beta-protected.jar"),
        "betamod",
        Map.of("BOOTSTRAP", List.of("net.minecraft.BadPatch::bootstrap")),
        Map.of("net/minecraft/BadPatch.class", new byte[] {1}),
        true,
        List.of("filesystem.write"));
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/alpha-shadow.jar"),
        "alphamod",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(
            resourceName(AlphaLifecycle.class),
            readClassBytes(AlphaLifecycle.class),
            "com/spindle/api/ModContext.class",
            new byte[] {2}),
        true,
        List.of());

    execute(true);
    execute(true);
    List<String> firstOrder = findingOrder(readSecurityReport());

    execute(true);
    List<String> secondOrder = findingOrder(readSecurityReport());

    assertEquals(firstOrder, secondOrder);
    assertEquals(
        List.of(
            "fatal|SEC-CLASS-001|alphamod|class|com.spindle.api.ModContext",
            "fatal|SEC-PACKAGE-001|alphamod|package|com.spindle.api",
            "fatal|SEC-PACKAGE-002|betamod|package|net.minecraft",
            "warning|SEC-PERM-001|betamod|permission|filesystem.write",
            "warning|SEC-TRUST-005|alphamod|artifact|mods/alpha-shadow.jar",
            "warning|SEC-TRUST-005|betamod|artifact|mods/beta-protected.jar",
            "warning|SEC-TRUST-006|alphamod|artifact|mods/alpha-shadow.jar",
            "warning|SEC-TRUST-006|betamod|artifact|mods/beta-protected.jar"),
        firstOrder);
  }

  @Test
  void securityReportDoesNotIncludeRawAbsolutePath() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/absolute-path.jar"),
        "absolutepath",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(resourceName(AlphaLifecycle.class), readClassBytes(AlphaLifecycle.class)),
        true,
        List.of());

    execute(true);
    rewriteCachedProfile(
        profile ->
            withFirstContextConfigDirectory(profile, tempDirectory.resolve("outside").toString()));

    execute(true);

    String report = Files.readString(tempDirectory.resolve("spindle.security-report.json"));
    assertTrue(report.contains("[absolute path]"));
    assertFalse(
        report.contains(tempDirectory.toAbsolutePath().normalize().toString().replace('\\', '/')));
  }

  @Test
  void schemaOneCompatibilityRemainsValidated() throws Exception {
    createSchemaOneModJar(
        tempDirectory.resolve("mods/schema-one.jar"),
        "legacymod",
        Map.of(resourceName(LegacyEntrypoint.class), readClassBytes(LegacyEntrypoint.class)));

    execute(true);

    JsonObject report = readSecurityReport();
    assertEquals("validated", report.get("state").getAsString());
    assertEquals(0, report.get("fatalCount").getAsInt());
  }

  private JsonObject readSecurityReport() throws IOException {
    return JsonParser.parseString(
            Files.readString(
                tempDirectory.resolve("spindle.security-report.json"), StandardCharsets.UTF_8))
        .getAsJsonObject();
  }

  private JsonObject readCachedProfileJson() throws IOException {
    return JsonParser.parseString(Files.readString(cachedProfilePath(), StandardCharsets.UTF_8))
        .getAsJsonObject();
  }

  private Path cachedProfilePath() throws IOException {
    JsonObject profile =
        JsonParser.parseString(
                Files.readString(
                    tempDirectory.resolve("spindle.profile.json"), StandardCharsets.UTF_8))
            .getAsJsonObject();
    return tempDirectory
        .resolve(".spindle")
        .resolve("profile-cache")
        .resolve(profile.get("inputFingerprint").getAsString())
        .resolve("spindle.profile.json");
  }

  private void rewriteCachedProfile(ProfileMutator mutator) throws Exception {
    Path cachePath = cachedProfilePath();
    CompiledModpackProfile profile = compiledProfileReader.read(cachePath);
    compiledProfileWriter.write(cachePath, mutator.mutate(profile));
  }

  private String recomputeFingerprint(CompiledModpackProfile profile) throws LoaderException {
    return compiledProfileFingerprint.compute(profile);
  }

  private CompiledModpackProfile withFirstContextConfigDirectory(
      CompiledModpackProfile profile, String configDirectory) throws LoaderException {
    CompiledModpackProfile.ModContextPlan firstContext = profile.contexts().mods().getFirst();
    CompiledModpackProfile updatedProfile =
        new CompiledModpackProfile(
            profile.schemaVersion(),
            profile.profileKind(),
            profile.fingerprint(),
            profile.inputFingerprint(),
            profile.runtimePolicyFingerprint(),
            profile.cache(),
            profile.loader(),
            profile.game(),
            profile.metadata(),
            profile.mods(),
            profile.resolvedOrder(),
            profile.classpath(),
            profile.ownership(),
            profile.lockfile(),
            profile.permissions(),
            profile.lifecycle(),
            new CompiledModpackProfile.Contexts(
                List.of(
                    new CompiledModpackProfile.ModContextPlan(
                        firstContext.modId(),
                        firstContext.storage(),
                        configDirectory,
                        firstContext.dataDirectory(),
                        firstContext.cacheDirectory(),
                        firstContext.generatedDirectory()))),
            profile.packagePolicy(),
            profile.quality());
    return updatedProfile.withFingerprint(recomputeFingerprint(updatedProfile));
  }

  private List<String> ruleIds(JsonObject report) {
    List<String> ruleIds = new ArrayList<>();
    for (var element : report.getAsJsonArray("findings")) {
      ruleIds.add(element.getAsJsonObject().get("ruleId").getAsString());
    }
    return ruleIds;
  }

  private List<String> findingOrder(JsonObject report) {
    List<String> order = new ArrayList<>();
    JsonArray findings = report.getAsJsonArray("findings");
    for (var element : findings) {
      JsonObject finding = element.getAsJsonObject();
      JsonObject location = finding.getAsJsonObject("location");
      order.add(
          finding.get("severity").getAsString()
              + "|"
              + finding.get("ruleId").getAsString()
              + "|"
              + finding.get("modId").getAsString()
              + "|"
              + location.get("kind").getAsString()
              + "|"
              + location.get("value").getAsString());
    }
    return order;
  }

  private void createSchemaOneModJar(Path jarPath, String modId, Map<String, byte[]> entries)
      throws IOException {
    createModJar(
        jarPath,
        """
        {
          "schema": 1,
          "id": "%s",
          "version": "1.0.0",
          "side": "universal",
          "entrypoints": {
            "main": [
              "%s"
            ]
          },
          "depends": {
            "loader": ">=0.1.0",
            "java": ">=25",
            "minecraft": ">=26.1.2"
          }
        }
        """
            .formatted(modId, LegacyEntrypoint.class.getName()),
        entries);
  }

  private void createSchemaTwoModJar(
      Path jarPath,
      String modId,
      Map<String, List<String>> lifecycle,
      Map<String, byte[]> entries,
      boolean storageEnabled,
      List<String> permissions)
      throws IOException {
    createModJar(
        jarPath, schemaTwoMetadata(modId, lifecycle, storageEnabled, permissions), entries);
  }

  private void createModJar(Path jarPath, String metadataJson, Map<String, byte[]> entries)
      throws IOException {
    Files.createDirectories(jarPath.getParent());
    try (OutputStream outputStream = Files.newOutputStream(jarPath);
        JarOutputStream jarOutputStream = new JarOutputStream(outputStream)) {
      jarOutputStream.putNextEntry(new JarEntry("loader.mod.json"));
      jarOutputStream.write(metadataJson.getBytes(StandardCharsets.UTF_8));
      jarOutputStream.closeEntry();
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

    return
"""
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

  @FunctionalInterface
  private interface ProfileMutator {
    CompiledModpackProfile mutate(CompiledModpackProfile profile) throws Exception;
  }

  public static final class ValidationGameMain {
    public static void main(String[] args) {}
  }

  public static final class LegacyEntrypoint implements com.spindle.api.ModInitializer {
    @Override
    public void onInitialize() {}
  }
}
