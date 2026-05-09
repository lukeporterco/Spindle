package com.spindle.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.artifact.ArtifactKind;
import com.spindle.core.artifact.ArtifactStatus;
import com.spindle.core.artifact.MinecraftArtifactCache;
import com.spindle.core.artifact.MinecraftArtifactCacheReport;
import com.spindle.core.artifact.MinecraftArtifactCacheWriter;
import com.spindle.core.artifact.MinecraftArtifactDownloader;
import com.spindle.core.artifact.MinecraftArtifactRecord;
import com.spindle.core.artifact.MinecraftArtifactResolver;
import com.spindle.core.artifact.MinecraftArtifactVerifier;
import com.spindle.core.diagnostics.JsonDiagnosticSink;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.minecraft.MinecraftProviderConfig;
import com.spindle.core.minecraft.MinecraftSide;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Milestone5MinecraftArtifactCacheTest {
  @TempDir Path tempDirectory;

  @Test
  void artifactCachePathsAreDeterministic() {
    MinecraftArtifactCache cache =
        new MinecraftArtifactCache(tempDirectory, tempDirectory.resolve("runtime/minecraft-cache"));

    assertEquals(
        tempDirectory
            .resolve("runtime/minecraft-cache/metadata/version-manifest.json")
            .toAbsolutePath()
            .normalize(),
        cache.manifestPath());
    assertEquals(
        tempDirectory
            .resolve("runtime/minecraft-cache/metadata/versions/26.1.2.json")
            .toAbsolutePath()
            .normalize(),
        cache.versionJsonPath("26.1.2"));
    assertEquals(
        tempDirectory
            .resolve("runtime/minecraft-cache/versions/26.1.2/26.1.2-server.jar")
            .toAbsolutePath()
            .normalize(),
        cache.serverJarPath("26.1.2"));
    assertEquals(
        tempDirectory.resolve("runtime/minecraft-cache/tmp").toAbsolutePath().normalize(),
        cache.tmpDirectory());
    assertEquals(
        tempDirectory
            .resolve("runtime/minecraft-cache/versions/26.1.2/server-artifacts.lock.json")
            .toAbsolutePath()
            .normalize(),
        cache.artifactLockPath("26.1.2"));
    assertEquals(
        tempDirectory.resolve("minecraft-artifacts.json").toAbsolutePath().normalize(),
        cache.artifactReportPath());
  }

  @Test
  void artifactVerifierAcceptsMatchingSha1SizeAndComputesSha256() throws Exception {
    Path artifact = tempDirectory.resolve("artifact.bin");
    byte[] bytes = "hello world".getBytes(StandardCharsets.UTF_8);
    Files.write(artifact, bytes);

    MinecraftArtifactVerifier.VerificationResult result =
        new MinecraftArtifactVerifier().verify(artifact, sha1(bytes), (long) bytes.length);

    assertEquals(sha1(bytes), result.sha1());
    assertEquals(sha256(bytes), result.sha256());
    assertEquals(bytes.length, result.size());
    assertTrue(result.verified());
  }

  @Test
  void artifactVerifierRejectsBadSha1() throws Exception {
    Path artifact = tempDirectory.resolve("artifact.bin");
    Files.writeString(artifact, "hello", StandardCharsets.UTF_8);

    LoaderException exception =
        assertThrows(
            LoaderException.class,
            () -> new MinecraftArtifactVerifier().verify(artifact, "deadbeef", 5L));

    assertTrue(exception.getMessage().contains("SHA-1 mismatch"));
  }

  @Test
  void artifactVerifierRejectsBadSize() throws Exception {
    Path artifact = tempDirectory.resolve("artifact.bin");
    Files.writeString(artifact, "hello", StandardCharsets.UTF_8);

    LoaderException exception =
        assertThrows(
            LoaderException.class,
            () ->
                new MinecraftArtifactVerifier()
                    .verify(artifact, sha1("hello".getBytes(StandardCharsets.UTF_8)), 4L));

    assertTrue(exception.getMessage().contains("size mismatch"));
  }

  @Test
  void downloaderWritesTempThenMovesOnSuccess() throws Exception {
    byte[] bytes = "download me".getBytes(StandardCharsets.UTF_8);
    try (TestHttpServer server = new TestHttpServer(bytes, 200)) {
      Path target = tempDirectory.resolve("cache/versions/26.1.2/26.1.2-server.jar");
      Path tmpDirectory = tempDirectory.resolve("cache/tmp");

      new MinecraftArtifactDownloader(
              HttpClient.newHttpClient(),
              new MinecraftArtifactVerifier(),
              java.time.Duration.ofSeconds(5))
          .download(server.uri(), target, tmpDirectory, sha1(bytes), (long) bytes.length);

      assertTrue(Files.isRegularFile(target));
      assertEquals("download me", Files.readString(target, StandardCharsets.UTF_8));
      assertTrue(Files.exists(tmpDirectory));
      assertEquals(0L, Files.list(tmpDirectory).count());
    }
  }

  @Test
  void downloaderCleansTempOnVerificationFailure() throws Exception {
    byte[] bytes = "download me".getBytes(StandardCharsets.UTF_8);
    try (TestHttpServer server = new TestHttpServer(bytes, 200)) {
      Path target = tempDirectory.resolve("cache/versions/26.1.2/26.1.2-server.jar");
      Path tmpDirectory = tempDirectory.resolve("cache/tmp");

      assertThrows(
          LoaderException.class,
          () ->
              new MinecraftArtifactDownloader(
                      HttpClient.newHttpClient(),
                      new MinecraftArtifactVerifier(),
                      java.time.Duration.ofSeconds(5))
                  .download(server.uri(), target, tmpDirectory, "deadbeef", (long) bytes.length));

      assertFalse(Files.exists(target));
      assertTrue(Files.exists(tmpDirectory));
      assertEquals(0L, Files.list(tmpDirectory).count());
    }
  }

  @Test
  void downloaderFollowsRedirectsByDefault() throws Exception {
    byte[] bytes = "redirected".getBytes(StandardCharsets.UTF_8);
    HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext(
        "/redirect",
        exchange -> {
          exchange.getResponseHeaders().add("Location", "/artifact");
          exchange.sendResponseHeaders(302, -1);
          exchange.close();
        });
    server.createContext(
        "/artifact",
        exchange -> {
          exchange.sendResponseHeaders(200, bytes.length);
          try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
          }
        });
    server.start();
    try {
      Path target = tempDirectory.resolve("cache/redirected.bin");
      new MinecraftArtifactDownloader()
          .download(
              java.net.URI.create(
                  "http://127.0.0.1:" + server.getAddress().getPort() + "/redirect"),
              target,
              tempDirectory.resolve("cache/tmp"),
              sha1(bytes),
              (long) bytes.length);
      assertEquals("redirected", Files.readString(target, StandardCharsets.UTF_8));
    } finally {
      server.stop(0);
    }
  }

  @Test
  void offlineModeRejectsMissingMetadata() {
    LoaderException exception =
        assertThrows(
            LoaderException.class,
            () ->
                LoaderMain.execute(
                    tempDirectory,
                    new String[] {
                      "--game-main",
                      "unused.for.minecraft.Offline",
                      "--game-provider",
                      "minecraft",
                      "--minecraft-version",
                      "26.1.2",
                      "--minecraft-dir",
                      tempDirectory.resolve("missing-minecraft").toString(),
                      "--minecraft-side",
                      "server",
                      "--minecraft-dry-run",
                      "--minecraft-offline"
                    },
                    new JsonDiagnosticSink(
                        tempDirectory.resolve("diagnostics/startup-trace.json"))));

    assertTrue(exception.getMessage().contains("offline mode"));
  }

  @Test
  void offlineModeRejectsMissingServerJarForLaunch() throws Exception {
    Path versionJson =
        writeVersionJson(
            tempDirectory.resolve("explicit-version.json"),
            "https://example.invalid/server.jar",
            "abc123",
            9L);

    LoaderException exception =
        assertThrows(
            LoaderException.class,
            () ->
                LoaderMain.execute(
                    tempDirectory,
                    new String[] {
                      "--game-main",
                      "unused.for.minecraft.OfflineLaunch",
                      "--game-provider",
                      "minecraft",
                      "--minecraft-version",
                      "26.1.2",
                      "--minecraft-side",
                      "server",
                      "--minecraft-dry-run",
                      "--minecraft-verify-files",
                      "--minecraft-launch",
                      "--minecraft-offline",
                      "--minecraft-version-json",
                      versionJson.toString()
                    },
                    new JsonDiagnosticSink(
                        tempDirectory.resolve("diagnostics/startup-trace.json"))));

    assertTrue(exception.getMessage().contains("cached or local server jar"));
  }

  @Test
  void artifactReportIsDeterministic() throws Exception {
    MinecraftArtifactCache cache =
        new MinecraftArtifactCache(tempDirectory, tempDirectory.resolve("runtime/minecraft-cache"));
    MinecraftArtifactCacheReport report =
        new MinecraftArtifactCacheReport(
            1,
            "26.1.2",
            "minecraft-cache",
            false,
            false,
            false,
            false,
            List.of(
                new MinecraftArtifactRecord(
                    "server-jar",
                    ArtifactKind.SERVER,
                    cache.serverJarPath("26.1.2"),
                    "https://example.invalid/server.jar",
                    "a",
                    "b",
                    3L,
                    true,
                    true,
                    true,
                    ArtifactStatus.VERIFIED),
                new MinecraftArtifactRecord(
                    "version-manifest",
                    ArtifactKind.METADATA,
                    cache.manifestPath(),
                    null,
                    null,
                    "c",
                    2L,
                    true,
                    false,
                    false,
                    ArtifactStatus.PRESENT)),
            List.of("warning"));

    Path output = tempDirectory.resolve("runtime/minecraft-artifacts.json");
    new MinecraftArtifactCacheWriter().writeReport(output, cache, report);
    String json = Files.readString(output, StandardCharsets.UTF_8);

    assertTrue(json.indexOf("\"version-manifest\"") < json.indexOf("\"server-jar\""));
    assertTrue(json.contains("\"cacheDirectory\": \"runtime/minecraft-cache\""));
    assertTrue(json.contains("\"warnings\": ["));
  }

  @Test
  void serverArtifactLockIsWrittenAfterVerification() throws Exception {
    Path cacheDir = tempDirectory.resolve("runtime/minecraft-cache");
    MinecraftArtifactCache cache = new MinecraftArtifactCache(tempDirectory, cacheDir);
    byte[] serverBytes = "server-bytes".getBytes(StandardCharsets.UTF_8);
    Path serverJar = cache.serverJarPath("26.1.2");
    Files.createDirectories(serverJar.getParent());
    Files.write(serverJar, serverBytes);
    Path versionJson =
        writeVersionJson(
            tempDirectory.resolve("explicit-version.json"),
            "https://example.invalid/server.jar",
            sha1(serverBytes),
            (long) serverBytes.length);

    MinecraftArtifactResolver resolver = new MinecraftArtifactResolver(cache);
    resolver.resolve(
        tempDirectory,
        serverConfig(versionJson, cacheDir, true, false, false, false),
        new JsonDiagnosticSink(tempDirectory.resolve("diagnostics/startup-trace.json")));

    String lockJson = Files.readString(cache.artifactLockPath("26.1.2"), StandardCharsets.UTF_8);
    assertTrue(lockJson.contains("\"id\": \"server-jar\""));
    assertTrue(lockJson.contains("\"sha1\": \"" + sha1(serverBytes) + "\""));
  }

  @Test
  void serverArtifactLockDetectsMismatch() throws Exception {
    Path cacheDir = tempDirectory.resolve("runtime/minecraft-cache");
    MinecraftArtifactCache cache = new MinecraftArtifactCache(tempDirectory, cacheDir);
    byte[] serverBytes = "server-bytes".getBytes(StandardCharsets.UTF_8);
    Path serverJar = cache.serverJarPath("26.1.2");
    Files.createDirectories(serverJar.getParent());
    Files.write(serverJar, serverBytes);
    Path versionJson =
        writeVersionJson(
            tempDirectory.resolve("explicit-version.json"),
            "https://example.invalid/server.jar",
            sha1(serverBytes),
            (long) serverBytes.length);
    Files.createDirectories(cache.artifactLockPath("26.1.2").getParent());
    Files.writeString(
        cache.artifactLockPath("26.1.2"),
        """
            {
              "schema": 1,
              "minecraftVersion": "26.1.2",
              "artifacts": [
                {
                  "id": "server-jar",
                  "path": "runtime/minecraft-cache/versions/26.1.2/26.1.2-server.jar",
                  "sha1": "wrong",
                  "sha256": "wrong",
                  "size": 1
                }
              ]
            }
            """,
        StandardCharsets.UTF_8);

    MinecraftArtifactResolver resolver = new MinecraftArtifactResolver(cache);
    LoaderException exception =
        assertThrows(
            LoaderException.class,
            () ->
                resolver.resolve(
                    tempDirectory,
                    serverConfig(versionJson, cacheDir, true, true, true, false),
                    new JsonDiagnosticSink(
                        tempDirectory.resolve("diagnostics/startup-trace.json"))));

    assertTrue(exception.getMessage().contains("artifact lock"));
  }

  @Test
  void launchPlanUsesCachedServerJarWhenDownloaded() throws Exception {
    Path cacheDir = tempDirectory.resolve("runtime/minecraft-cache");
    MinecraftArtifactCache cache = new MinecraftArtifactCache(tempDirectory, cacheDir);
    byte[] serverBytes = "server-bytes".getBytes(StandardCharsets.UTF_8);
    Files.createDirectories(cache.serverJarPath("26.1.2").getParent());
    Files.write(cache.serverJarPath("26.1.2"), serverBytes);
    Path versionJson =
        writeVersionJson(
            tempDirectory.resolve("explicit-version.json"),
            "https://example.invalid/server.jar",
            sha1(serverBytes),
            (long) serverBytes.length);

    executeAndWrite(
        new String[] {
          "--game-main",
          "unused.for.minecraft.CachedLaunchPlan",
          "--game-provider",
          "minecraft",
          "--minecraft-version",
          "26.1.2",
          "--minecraft-side",
          "server",
          "--minecraft-dry-run",
          "--minecraft-download-server",
          "--minecraft-version-json",
          versionJson.toString(),
          "--minecraft-cache-dir",
          cacheDir.toString()
        });

    String planJson =
        Files.readString(
            tempDirectory.resolve("minecraft-launch-plan.json"), StandardCharsets.UTF_8);
    assertTrue(planJson.contains("\"serverJarSource\": \"cache\""));
    assertTrue(planJson.contains("runtime/minecraft-cache/versions/26.1.2/26.1.2-server.jar"));
  }

  @Test
  void cacheRepairRedownloadsInvalidCachedServerJar() throws Exception {
    Path cacheDir = tempDirectory.resolve("runtime/minecraft-cache");
    MinecraftArtifactCache cache = new MinecraftArtifactCache(tempDirectory, cacheDir);
    byte[] goodServerBytes = "good-server-bytes".getBytes(StandardCharsets.UTF_8);
    Files.createDirectories(cache.serverJarPath("26.1.2").getParent());
    Files.writeString(cache.serverJarPath("26.1.2"), "corrupt", StandardCharsets.UTF_8);

    try (TestHttpServer server = new TestHttpServer(goodServerBytes, 200)) {
      Path versionJson =
          writeVersionJson(
              tempDirectory.resolve("explicit-version.json"),
              server.uri().toString(),
              sha1(goodServerBytes),
              (long) goodServerBytes.length);

      MinecraftArtifactResolver.Resolution resolution =
          new MinecraftArtifactResolver(cache)
              .resolve(
                  tempDirectory,
                  serverConfig(versionJson, cacheDir, false, false, false, false, true),
                  new JsonDiagnosticSink(tempDirectory.resolve("diagnostics/startup-trace.json")));

      assertEquals("downloaded", resolution.serverJarSource());
      assertArrayEquals(goodServerBytes, Files.readAllBytes(cache.serverJarPath("26.1.2")));
    }
  }

  @Test
  void cacheRepairRedownloadsInvalidCachedVersionJson() throws Exception {
    Path cacheDir = tempDirectory.resolve("runtime/minecraft-cache");
    MinecraftArtifactCache cache = new MinecraftArtifactCache(tempDirectory, cacheDir);
    Files.createDirectories(cache.versionJsonPath("26.1.2").getParent());
    Files.writeString(cache.versionJsonPath("26.1.2"), "{ invalid json", StandardCharsets.UTF_8);

    byte[] serverBytes = "server-bytes".getBytes(StandardCharsets.UTF_8);
    HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
    byte[][] versionJsonBytes = new byte[1][];
    server.createContext(
        "/version",
        exchange -> {
          exchange.sendResponseHeaders(200, versionJsonBytes[0].length);
          try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(versionJsonBytes[0]);
          }
        });
    server.createContext(
        "/server",
        exchange -> {
          exchange.sendResponseHeaders(200, serverBytes.length);
          try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(serverBytes);
          }
        });
    server.start();
    try {
      String serverJarUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/server";
      versionJsonBytes[0] =
          versionJsonBody(serverJarUrl, serverBytes).getBytes(StandardCharsets.UTF_8);
      Path manifestJson = tempDirectory.resolve("manifest.json");
      Files.writeString(
          manifestJson,
          """
                {
                  "latest": {
                    "release": "26.1.2",
                    "snapshot": "26w19a"
                  },
                  "versions": [
                    {
                      "id": "26.1.2",
                      "type": "release",
                      "url": "%s"
                    }
                  ]
                }
                """
              .formatted("http://127.0.0.1:" + server.getAddress().getPort() + "/version"),
          StandardCharsets.UTF_8);

      MinecraftProviderConfig config =
          new MinecraftProviderConfig(
              "26.1.2",
              null,
              null,
              manifestJson,
              MinecraftSide.SERVER,
              true,
              true,
              false,
              false,
              cacheDir,
              false,
              false,
              true,
              false,
              false,
              tempDirectory.resolve("minecraft-launch-plan.json"),
              false,
              null,
              false,
              List.of(),
              List.of(),
              30,
              false,
              20);

      MinecraftArtifactResolver.Resolution resolution =
          new MinecraftArtifactResolver(cache)
              .resolve(
                  tempDirectory,
                  config,
                  new JsonDiagnosticSink(tempDirectory.resolve("diagnostics/startup-trace.json")));

      assertEquals("cache", resolution.resolvedVersionJson().metadataSource());
      assertTrue(
          Files.readString(cache.versionJsonPath("26.1.2"), StandardCharsets.UTF_8)
              .contains("\"downloads\""));
    } finally {
      server.stop(0);
    }
  }

  @Test
  void cacheInspectDoesNotLaunchOrInvokeEntrypoints() throws Exception {
    createStandardModJar(
        tempDirectory.resolve("mods/sample-mod.jar"), "samplemod", "com.example.NotLoaded");

    String output =
        executeAndWrite(
            new String[] {
              "--game-main",
              "unused.for.minecraft.CacheInspect",
              "--game-provider",
              "minecraft",
              "--minecraft-version",
              "26.1.2",
              "--minecraft-side",
              "server",
              "--minecraft-dry-run",
              "--minecraft-cache-inspect"
            });

    String diagnostics =
        Files.readString(
            tempDirectory.resolve("diagnostics/startup-trace.json"), StandardCharsets.UTF_8);
    assertTrue(output.contains("[spindle] minecraft cache inspection complete"));
    assertTrue(Files.exists(tempDirectory.resolve("minecraft-artifacts.json")));
    assertFalse(Files.exists(tempDirectory.resolve("minecraft-server-launch-result.json")));
    assertFalse(diagnostics.contains("\"name\": \"classpath.create\""));
    assertFalse(diagnostics.contains("\"name\": \"entrypoint.invoke\""));
  }

  private MinecraftProviderConfig serverConfig(
      Path versionJson,
      Path cacheDir,
      boolean downloadServer,
      boolean offline,
      boolean strict,
      boolean forceRedownload) {
    return serverConfig(
        versionJson, cacheDir, downloadServer, offline, strict, forceRedownload, false);
  }

  private MinecraftProviderConfig serverConfig(
      Path versionJson,
      Path cacheDir,
      boolean downloadServer,
      boolean offline,
      boolean strict,
      boolean forceRedownload,
      boolean cacheRepair) {
    return new MinecraftProviderConfig(
        "26.1.2",
        null,
        versionJson,
        null,
        MinecraftSide.SERVER,
        true,
        true,
        false,
        downloadServer,
        cacheDir,
        offline,
        false,
        cacheRepair,
        strict,
        forceRedownload,
        tempDirectory.resolve("minecraft-launch-plan.json"),
        false,
        null,
        false,
        List.of(),
        List.of(),
        30,
        false,
        20);
  }

  private String versionJsonBody(String serverUrl, byte[] serverBytes) throws Exception {
    return """
            {
              "id": "26.1.2",
              "type": "release",
              "downloads": {
                "server": {
                  "url": "%s",
                  "sha1": "%s",
                  "size": %d
                }
              },
              "libraries": [],
              "arguments": {
                "game": [],
                "jvm": []
              }
            }
            """
        .formatted(serverUrl, sha1(serverBytes), serverBytes.length);
  }

  private Path writeVersionJson(Path path, String serverUrl, String sha1, long size)
      throws IOException {
    Files.createDirectories(path.getParent());
    Files.writeString(
        path,
        """
            {
              "id": "26.1.2",
              "type": "release",
              "downloads": {
                "server": {
                  "url": "%s",
                  "sha1": "%s",
                  "size": %d
                }
              },
              "libraries": [],
              "arguments": {
                "game": [],
                "jvm": []
              }
            }
            """
            .formatted(serverUrl, sha1, size),
        StandardCharsets.UTF_8);
    return path;
  }

  private String executeAndWrite(String[] args) throws Exception {
    JsonDiagnosticSink sink =
        new JsonDiagnosticSink(tempDirectory.resolve("diagnostics/startup-trace.json"));
    String output = captureStdout(() -> LoaderMain.execute(tempDirectory, args, sink));
    sink.write();
    return output;
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

  private void createStandardModJar(Path jarPath, String modId, String entrypointClassName)
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
              "depends": %s,
              "breaks": {}
            }
            """
            .formatted(
                modId,
                entrypointClassName,
                toJsonObject(Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.2"))),
        Map.of(entrypointClassName.replace('.', '/') + ".class", new byte[] {1}));
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

  private String toJsonObject(Map<String, String> values) {
    StringBuilder builder = new StringBuilder("{");
    boolean first = true;
    for (Map.Entry<String, String> entry :
        values.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
      if (!first) {
        builder.append(", ");
      }
      builder
          .append("\"")
          .append(entry.getKey())
          .append("\": \"")
          .append(entry.getValue())
          .append("\"");
      first = false;
    }
    builder.append("}");
    return builder.toString();
  }

  private String sha1(byte[] bytes) throws Exception {
    return digest("SHA-1", bytes);
  }

  private String sha256(byte[] bytes) throws Exception {
    return digest("SHA-256", bytes);
  }

  private String digest(String algorithm, byte[] bytes) throws Exception {
    MessageDigest digest = MessageDigest.getInstance(algorithm);
    return HexFormat.of().formatHex(digest.digest(bytes));
  }

  private record TestHttpServer(HttpServer server, byte[] body, int status)
      implements AutoCloseable {
    TestHttpServer(byte[] body, int status) throws IOException {
      this(HttpServer.create(new InetSocketAddress(0), 0), body, status);
      server.createContext(
          "/artifact",
          exchange -> {
            exchange.sendResponseHeaders(status, body.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
              outputStream.write(body);
            }
          });
      server.start();
    }

    java.net.URI uri() {
      return java.net.URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/artifact");
    }

    @Override
    public void close() {
      server.stop(0);
    }
  }

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws Exception;
  }
}
