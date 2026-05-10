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
import com.spindle.core.security.trust.ArtifactSignatureVerifier;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.AlphaLifecycle;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.BetaLifecycle;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Security1ArtifactTrustTest {
  @TempDir Path tempDirectory;

  private final ArtifactSignatureVerifier artifactSignatureVerifier =
      new ArtifactSignatureVerifier();

  @Test
  void unsignedLocalSchemaTwoModProducesTrustWarningsAndRuns() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/local-dev.jar"),
        "localdev",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(resourceName(AlphaLifecycle.class), readClassBytes(AlphaLifecycle.class)),
        true);

    execute(false);

    JsonObject report = readSecurityReport();
    assertEquals("validated", report.get("state").getAsString());
    assertEquals(0, report.get("fatalCount").getAsInt());
    assertTrue(ruleIds(report).contains(SecurityRuleId.SEC_TRUST_001.id()));
    assertTrue(ruleIds(report).contains(SecurityRuleId.SEC_TRUST_006.id()));
    assertEquals("local-unsigned", firstTrustEntry(report).get("trustState").getAsString());
    assertTrue(Files.exists(tempDirectory.resolve("lifecycle.log")));
  }

  @Test
  void existingLockfileHashIdentityAppearsInArtifactTrust() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/locked.jar"),
        "lockedmod",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(resourceName(AlphaLifecycle.class), readClassBytes(AlphaLifecycle.class)),
        true);

    execute(true);
    execute(true);

    JsonObject report = readSecurityReport();
    JsonObject entry = firstTrustEntry(report);
    assertEquals("locked-hash", entry.get("trustState").getAsString());
    assertEquals("hash-locked", entry.get("trustTier").getAsString());
    assertEquals(
        1,
        report
            .getAsJsonObject("artifactTrust")
            .getAsJsonObject("summary")
            .get("lockedHashCount")
            .getAsInt());
    assertTrue(ruleIds(report).contains(SecurityRuleId.SEC_TRUST_005.id()));
  }

  @Test
  void validEd25519SidecarProducesSignedArtifactWithoutFatalFinding() throws Exception {
    Path jarPath = tempDirectory.resolve("mods/signed.jar");
    createSchemaTwoModJar(
        jarPath,
        "signedmod",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(resourceName(AlphaLifecycle.class), readClassBytes(AlphaLifecycle.class)),
        true);
    KeyPair keyPair = generateKeyPair();
    writeValidSidecar(jarPath, "signedmod", "1.0.0", "example.dev", keyPair);

    execute(false);

    JsonObject report = readSecurityReport();
    assertEquals("validated", report.get("state").getAsString());
    assertEquals(0, report.get("fatalCount").getAsInt());
    assertEquals(0, report.get("warningCount").getAsInt());
    JsonObject entry = firstTrustEntry(report);
    assertEquals("signed-artifact", entry.get("trustState").getAsString());
    assertEquals("publisher-signed", entry.get("trustTier").getAsString());
    assertEquals("example.dev", entry.get("signerId").getAsString());
    assertEquals("present", entry.get("provenanceState").getAsString());
  }

  @Test
  void malformedSidecarProducesSecTrust002Fatal() throws Exception {
    Path jarPath = tempDirectory.resolve("mods/malformed.jar");
    createSchemaTwoModJar(
        jarPath,
        "malformedmod",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(resourceName(AlphaLifecycle.class), readClassBytes(AlphaLifecycle.class)),
        true);
    Files.writeString(
        sidecarPath(jarPath),
        """
        {
          "schemaVersion": 1,
          "signatureKind": "spindle-ed25519",
          "algorithm": "Ed25519",
          "signerId": "example.dev",
          "publicKey": "%%%not-base64%%%",
          "signature": "%%%not-base64%%%",
          "artifactSha256": "abcd",
          "signedFields": {
            "modId": "malformedmod"
          }
        }
        """,
        StandardCharsets.UTF_8);

    execute(true);

    JsonObject report = readSecurityReport();
    assertEquals("blocked", report.get("state").getAsString());
    assertTrue(ruleIds(report).contains(SecurityRuleId.SEC_TRUST_002.id()));
    assertEquals(
        "signature-sidecar-invalid", firstTrustEntry(report).get("trustState").getAsString());
  }

  @Test
  void sidecarArtifactSha256MismatchProducesSecTrust003Fatal() throws Exception {
    Path jarPath = tempDirectory.resolve("mods/hash-mismatch.jar");
    createSchemaTwoModJar(
        jarPath,
        "hashmismatch",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(resourceName(AlphaLifecycle.class), readClassBytes(AlphaLifecycle.class)),
        true);
    KeyPair keyPair = generateKeyPair();
    String wrongSha256 = "0".repeat(64);
    writeSidecar(
        jarPath,
        sidecarJson(
            "example.dev",
            Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()),
            signPayload(
                payload(wrongSha256, "hashmismatch", "1.0.0", "example.dev"), keyPair.getPrivate()),
            wrongSha256,
            "hashmismatch",
            "1.0.0"));

    execute(true);

    JsonObject report = readSecurityReport();
    assertEquals("blocked", report.get("state").getAsString());
    assertTrue(ruleIds(report).contains(SecurityRuleId.SEC_TRUST_003.id()));
    assertEquals(
        "signature-artifact-hash-mismatch",
        firstTrustEntry(report).get("trustState").getAsString());
  }

  @Test
  void invalidSignatureProducesSecTrust004Fatal() throws Exception {
    Path jarPath = tempDirectory.resolve("mods/invalid-signature.jar");
    createSchemaTwoModJar(
        jarPath,
        "invalidsig",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(resourceName(AlphaLifecycle.class), readClassBytes(AlphaLifecycle.class)),
        true);
    KeyPair keyPair = generateKeyPair();
    String sha256 = sha256(jarPath);
    String signature =
        signPayload(payload(sha256, "invalidsig", "1.0.0", "example.dev"), keyPair.getPrivate());
    byte[] mutatedSignature = Base64.getDecoder().decode(signature);
    mutatedSignature[mutatedSignature.length - 1] ^= 0x01;
    writeSidecar(
        jarPath,
        sidecarJson(
            "example.dev",
            Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()),
            Base64.getEncoder().encodeToString(mutatedSignature),
            sha256,
            "invalidsig",
            "1.0.0"));

    execute(true);

    JsonObject report = readSecurityReport();
    assertEquals("blocked", report.get("state").getAsString());
    assertTrue(ruleIds(report).contains(SecurityRuleId.SEC_TRUST_004.id()));
    assertEquals("signature-invalid", firstTrustEntry(report).get("trustState").getAsString());
  }

  @Test
  void invalidSignatureBlocksLifecycleExecution() throws Exception {
    Path jarPath = tempDirectory.resolve("mods/blocked-by-signature.jar");
    createSchemaTwoModJar(
        jarPath,
        "blockedtrust",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(resourceName(AlphaLifecycle.class), readClassBytes(AlphaLifecycle.class)),
        true);
    KeyPair keyPair = generateKeyPair();
    String sha256 = sha256(jarPath);
    writeSidecar(
        jarPath,
        sidecarJson(
            "example.dev",
            Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()),
            signPayload(payload(sha256, "othermod", "1.0.0", "example.dev"), keyPair.getPrivate()),
            sha256,
            "othermod",
            "1.0.0"));

    LoaderException exception = assertThrows(LoaderException.class, () -> execute(false));

    assertTrue(exception.getMessage().contains(SecurityRuleId.SEC_TRUST_004.id()));
    assertFalse(Files.exists(tempDirectory.resolve("lifecycle.log")));
  }

  @Test
  void artifactTrustSectionUsesRelativePathsOnly() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/relative-only.jar"),
        "relativeonly",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(resourceName(AlphaLifecycle.class), readClassBytes(AlphaLifecycle.class)),
        true);

    execute(true);

    JsonObject entry = firstTrustEntry(readSecurityReport());
    assertEquals("mods/relative-only.jar", entry.get("path").getAsString());
    assertFalse(
        entry.get("path").getAsString().contains(tempDirectory.toString().replace('\\', '/')));
  }

  @Test
  void trustReportOrderingIsDeterministic() throws Exception {
    Path betaJar = tempDirectory.resolve("mods/beta.jar");
    createSchemaTwoModJar(
        betaJar,
        "beta",
        Map.of("BOOTSTRAP", List.of(BetaLifecycle.class.getName() + "::bootstrap")),
        Map.of(resourceName(BetaLifecycle.class), readClassBytes(BetaLifecycle.class)),
        true);
    writeValidSidecar(betaJar, "beta", "1.0.0", "beta.dev", generateKeyPair());

    Path alphaJar = tempDirectory.resolve("mods/alpha.jar");
    createSchemaTwoModJar(
        alphaJar,
        "alpha",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(resourceName(AlphaLifecycle.class), readClassBytes(AlphaLifecycle.class)),
        true);
    writeValidSidecar(alphaJar, "alpha", "1.0.0", "alpha.dev", generateKeyPair());

    execute(true);
    List<String> firstOrder = artifactTrustOrder(readSecurityReport());

    execute(true);
    List<String> secondOrder = artifactTrustOrder(readSecurityReport());

    assertEquals(firstOrder, secondOrder);
    assertEquals(
        List.of("alpha|mods/alpha.jar|signed-artifact", "beta|mods/beta.jar|signed-artifact"),
        firstOrder);
  }

  @Test
  void schemaOneCompatibilityRemainsIntactWithArtifactTrustWarnings() throws Exception {
    createSchemaOneModJar(
        tempDirectory.resolve("mods/schema-one.jar"),
        "legacytrust",
        Map.of(resourceName(LegacyEntrypoint.class), readClassBytes(LegacyEntrypoint.class)));

    execute(true);

    JsonObject report = readSecurityReport();
    assertEquals("validated", report.get("state").getAsString());
    assertEquals(0, report.get("fatalCount").getAsInt());
    assertEquals("local-unsigned", firstTrustEntry(report).get("trustState").getAsString());
    assertTrue(ruleIds(report).contains(SecurityRuleId.SEC_TRUST_001.id()));
  }

  private JsonObject readSecurityReport() throws IOException {
    return JsonParser.parseString(
            Files.readString(
                tempDirectory.resolve("spindle.security-report.json"), StandardCharsets.UTF_8))
        .getAsJsonObject();
  }

  private JsonObject firstTrustEntry(JsonObject report) {
    return report
        .getAsJsonObject("artifactTrust")
        .getAsJsonArray("entries")
        .get(0)
        .getAsJsonObject();
  }

  private List<String> ruleIds(JsonObject report) {
    List<String> ruleIds = new ArrayList<>();
    for (var element : report.getAsJsonArray("findings")) {
      ruleIds.add(element.getAsJsonObject().get("ruleId").getAsString());
    }
    return ruleIds;
  }

  private List<String> artifactTrustOrder(JsonObject report) {
    List<String> order = new ArrayList<>();
    JsonArray entries = report.getAsJsonObject("artifactTrust").getAsJsonArray("entries");
    for (var element : entries) {
      JsonObject entry = element.getAsJsonObject();
      order.add(
          entry.get("modId").getAsString()
              + "|"
              + entry.get("path").getAsString()
              + "|"
              + entry.get("trustState").getAsString());
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
      boolean storageEnabled)
      throws IOException {
    createModJar(jarPath, schemaTwoMetadata(modId, lifecycle, storageEnabled), entries);
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
      String modId, Map<String, List<String>> lifecycle, boolean storageEnabled) {
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
          "permissions": [],
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
            Boolean.toString(storageEnabled),
            Boolean.toString(storageEnabled),
            Boolean.toString(storageEnabled),
            Boolean.toString(storageEnabled));
  }

  private void writeValidSidecar(
      Path jarPath, String modId, String version, String signerId, KeyPair keyPair)
      throws Exception {
    String sha256 = sha256(jarPath);
    writeSidecar(
        jarPath,
        sidecarJson(
            signerId,
            Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()),
            signPayload(payload(sha256, modId, version, signerId), keyPair.getPrivate()),
            sha256,
            modId,
            version));
  }

  private void writeSidecar(Path jarPath, String json) throws IOException {
    Files.writeString(sidecarPath(jarPath), json, StandardCharsets.UTF_8);
  }

  private Path sidecarPath(Path jarPath) {
    return jarPath.resolveSibling(jarPath.getFileName().toString() + ".spindle-signature.json");
  }

  private String sidecarJson(
      String signerId,
      String publicKey,
      String signature,
      String artifactSha256,
      String modId,
      String version) {
    return """
        {
          "schemaVersion": 1,
          "signatureKind": "spindle-ed25519",
          "algorithm": "Ed25519",
          "signerId": "%s",
          "publicKey": "%s",
          "signature": "%s",
          "artifactSha256": "%s",
          "signedFields": {
            "modId": "%s",
            "version": "%s"
          }
        }
        """
        .formatted(signerId, publicKey, signature, artifactSha256, modId, version);
  }

  private KeyPair generateKeyPair() throws Exception {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("Ed25519");
    return keyPairGenerator.generateKeyPair();
  }

  private String signPayload(String payload, PrivateKey privateKey) throws Exception {
    Signature signature = Signature.getInstance("Ed25519");
    signature.initSign(privateKey);
    signature.update(payload.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(signature.sign());
  }

  private String payload(String artifactSha256, String modId, String version, String signerId) {
    return artifactSignatureVerifier.payload(artifactSha256, modId, version, signerId);
  }

  private String sha256(Path path) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    digest.update(Files.readAllBytes(path));
    return HexFormat.of().formatHex(digest.digest());
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

  public static final class LegacyEntrypoint implements com.spindle.api.ModInitializer {
    @Override
    public void onInitialize() {}
  }
}
