package com.mcmodloader.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mcmodloader.core.diagnostics.JsonDiagnosticSink;
import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.minecraft.MinecraftVersionManifest;
import com.mcmodloader.core.minecraft.MinecraftVersionManifestParser;
import com.mcmodloader.core.minecraft.MinecraftVersionSelection;
import com.mcmodloader.core.minecraft.MinecraftVersionSelector;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Milestone6MinecraftRealBaselineTest {
    private static final String REAL_VERSION = "1.21.8";

    @TempDir
    Path tempDirectory;

    @Test
    void versionSelectorResolvesLatestRelease() throws Exception {
        MinecraftVersionManifest manifest = parseManifestFixture("minecraft/real-version-manifest.json");

        MinecraftVersionSelection selection = new MinecraftVersionSelector().select("latest-release", manifest, false);

        assertEquals("latest-release", selection.requested());
        assertEquals("1.21.8", selection.resolved());
        assertEquals("manifest", selection.source());
    }

    @Test
    void versionSelectorResolvesLatestSnapshot() throws Exception {
        MinecraftVersionManifest manifest = parseManifestFixture("minecraft/real-version-manifest.json");

        MinecraftVersionSelection selection = new MinecraftVersionSelector().select("latest-snapshot", manifest, false);

        assertEquals("latest-snapshot", selection.requested());
        assertEquals("26w20a", selection.resolved());
        assertEquals("manifest", selection.source());
    }

    @Test
    void versionSelectorRejectsMissingExactVersion() throws Exception {
        MinecraftVersionManifest manifest = parseManifestFixture("minecraft/real-version-manifest.json");

        LoaderException exception = assertThrows(
            LoaderException.class,
            () -> new MinecraftVersionSelector().select("9.9.9", manifest, false)
        );

        assertTrue(exception.getMessage().contains("9.9.9"));
        assertTrue(exception.getMessage().contains("1.21.8"));
        assertTrue(exception.getMessage().contains("26w20a"));
        assertTrue(exception.getMessage().contains("Manifest version count: 2"));
    }

    @Test
    void baselineReportDistinguishesProjectTargetAndRealVersion() throws Exception {
        Path cacheDir = tempDirectory.resolve("runtime/minecraft-cache");
        prepareCachedBaseline(cacheDir, createFakeServerJar(tempDirectory.resolve("fake-server.jar"), false), true);

        executeAndWrite(
            baselineArgs(
                cacheDir,
                "--minecraft-offline",
                "--minecraft-offline-replay",
                "--minecraft-verify-files",
                "--minecraft-cache-strict"
            )
        );

        String baselineJson = Files.readString(tempDirectory.resolve("minecraft-server-baseline.json"), StandardCharsets.UTF_8);
        assertTrue(baselineJson.contains("\"projectTargetMinecraft\": \"26.1.2\""));
        assertTrue(baselineJson.contains("\"baselineMinecraft\": \"1.21.8\""));
        assertTrue(baselineJson.contains("\"resolved\": \"1.21.8\""));
    }

    @Test
    void realBaselineAcquireWritesReportsWithoutLaunch() throws Exception {
        Path cacheDir = tempDirectory.resolve("runtime/minecraft-cache");
        byte[] serverBytes = "real-server".getBytes(StandardCharsets.UTF_8);
        String versionJsonBody = versionJsonBody("__SERVER_URL__", sha1(serverBytes), serverBytes.length);
        try (VersionAndServerHttpServer server = new VersionAndServerHttpServer(serverBytes, versionJsonBody)) {
            Path manifestJson = tempDirectory.resolve("manifest.json");
            Files.writeString(
                manifestJson,
                manifestBody(server.versionUri().toString(), versionJsonBody.replace("__SERVER_URL__", server.serverUri().toString())),
                StandardCharsets.UTF_8
            );

            String output =
                executeAndWrite(
                    baselineArgs(
                        cacheDir,
                        "--minecraft-baseline-version",
                        "latest-release",
                        "--minecraft-manifest-json",
                        manifestJson.toString(),
                        "--minecraft-fetch-metadata",
                        "--minecraft-download-server",
                        "--minecraft-verify-files"
                    )
                );

            assertTrue(Files.exists(tempDirectory.resolve("minecraft-artifacts.json")));
            assertTrue(Files.exists(tempDirectory.resolve("minecraft-launch-plan.json")));
            assertTrue(Files.exists(tempDirectory.resolve("minecraft-server-baseline.json")));
            assertFalse(Files.exists(tempDirectory.resolve("minecraft-server-launch-result.json")));
            assertFalse(output.contains("Sample mod initialized"));
            assertFalse(output.contains("Game starting"));

            String artifactJson = Files.readString(tempDirectory.resolve("minecraft-artifacts.json"), StandardCharsets.UTF_8);
            assertTrue(artifactJson.contains("\"networkRequestCount\": 2"));
            assertTrue(artifactJson.contains("\"projectTargetMinecraft\": \"26.1.2\""));
        }
    }

    @Test
    void offlineReplayUsesCacheAndZeroNetworkRequests() throws Exception {
        Path cacheDir = tempDirectory.resolve("runtime/minecraft-cache");
        prepareCachedBaseline(cacheDir, createFakeServerJar(tempDirectory.resolve("fake-server.jar"), false), true);

        executeAndWrite(
            baselineArgs(
                cacheDir,
                "--minecraft-offline",
                "--minecraft-offline-replay",
                "--minecraft-verify-files",
                "--minecraft-cache-strict"
            )
        );

        String artifactJson = Files.readString(tempDirectory.resolve("minecraft-artifacts.json"), StandardCharsets.UTF_8);
        String baselineJson = Files.readString(tempDirectory.resolve("minecraft-server-baseline.json"), StandardCharsets.UTF_8);
        assertTrue(artifactJson.contains("\"networkRequestCount\": 0"));
        assertTrue(baselineJson.contains("\"networkCalls\": 0"));
        assertTrue(baselineJson.contains("\"succeeded\": true"));
    }

    @Test
    void offlineReplayFailsClearlyWithMissingServerJar() throws Exception {
        Path cacheDir = tempDirectory.resolve("runtime/minecraft-cache");
        prepareCachedMetadataOnly(cacheDir);

        LoaderException exception =
            assertThrows(
                LoaderException.class,
                () ->
                    executeWithoutWrite(
                        baselineArgs(
                            cacheDir,
                            "--minecraft-offline",
                            "--minecraft-offline-replay",
                            "--minecraft-verify-files",
                            "--minecraft-cache-strict"
                        )
                    )
            );

        assertTrue(exception.getMessage().contains("Missing cached Minecraft server jar"));
        assertTrue(exception.getMessage().contains("minecraftRealServerAcquire"));
    }

    @Test
    void serverArtifactLockRecreatedWhenMissingButJarVerifies() throws Exception {
        Path cacheDir = tempDirectory.resolve("runtime/minecraft-cache");
        prepareCachedBaseline(cacheDir, createFakeServerJar(tempDirectory.resolve("fake-server.jar"), false), false);

        executeAndWrite(
            baselineArgs(
                cacheDir,
                "--minecraft-offline",
                "--minecraft-offline-replay",
                "--minecraft-verify-files",
                "--minecraft-cache-strict"
            )
        );

        String lockJson = Files.readString(cacheDir.resolve("versions/1.21.8/server-artifacts.lock.json"), StandardCharsets.UTF_8);
        assertTrue(lockJson.contains("\"baselineMinecraft\": \"1.21.8\""));
        assertTrue(lockJson.contains("\"id\": \"server-jar\""));
    }

    @Test
    void serverArtifactLockMismatchFailsStrictOfflineReplay() throws Exception {
        Path cacheDir = tempDirectory.resolve("runtime/minecraft-cache");
        Path serverJar = createFakeServerJar(tempDirectory.resolve("fake-server.jar"), false);
        prepareCachedBaseline(cacheDir, serverJar, true);
        Files.writeString(
            cacheDir.resolve("versions/1.21.8/server-artifacts.lock.json"),
            """
            {
              "schema": 1,
              "projectTargetMinecraft": "26.1.2",
              "baselineMinecraft": "1.21.8",
              "createdBy": "MCModLoader",
              "artifacts": [
                {
                  "id": "server-jar",
                  "kind": "server",
                  "path": "minecraft-cache/versions/1.21.8/1.21.8-server.jar",
                  "sourceUrl": "https://example.invalid/server.jar",
                  "sha1": "wrong",
                  "sha256": "wrong",
                  "size": 1,
                  "verifiedAt": "2026-05-08T00:00:00Z"
                }
              ]
            }
            """,
            StandardCharsets.UTF_8
        );

        LoaderException exception =
            assertThrows(
                LoaderException.class,
                () ->
                    executeWithoutWrite(
                        baselineArgs(
                            cacheDir,
                            "--minecraft-offline",
                            "--minecraft-offline-replay",
                            "--minecraft-verify-files",
                            "--minecraft-cache-strict"
                        )
                    )
            );

        assertTrue(exception.getMessage().contains("artifact lock"));
    }

    @Test
    void realSmokeDoesNotPutModJarsOnMinecraftClasspath() throws Exception {
        Path cacheDir = tempDirectory.resolve("runtime/minecraft-cache");
        prepareCachedBaseline(cacheDir, createFakeServerJar(tempDirectory.resolve("fake-server.jar"), false), true);
        createStandardModJar(tempDirectory.resolve("mods/sample-mod.jar"), "samplemod", "com.example.NotLoaded");

        executeAndWrite(
            baselineArgs(
                cacheDir,
                "--minecraft-offline",
                "--minecraft-offline-replay",
                "--minecraft-verify-files",
                "--minecraft-cache-strict"
            )
        );

        String planJson = Files.readString(tempDirectory.resolve("minecraft-launch-plan.json"), StandardCharsets.UTF_8);
        assertTrue(planJson.contains("\"modJarsOnMinecraftClasspath\": false"));
        assertFalse(planJson.contains("sample-mod.jar"));
    }

    @Test
    void realSmokeDoesNotCreateModClassLoaderOrInvokeEntrypoints() throws Exception {
        Path cacheDir = tempDirectory.resolve("runtime/minecraft-cache");
        prepareCachedBaseline(cacheDir, createFakeServerJar(tempDirectory.resolve("fake-server.jar"), false), true);
        createStandardModJar(tempDirectory.resolve("mods/sample-mod.jar"), "samplemod", "com.example.NotLoaded");

        String output =
            executeAndWrite(
                baselineArgs(
                    cacheDir,
                    "--minecraft-offline",
                    "--minecraft-offline-replay",
                    "--minecraft-verify-files",
                    "--minecraft-cache-strict"
                )
            );

        String diagnostics = Files.readString(tempDirectory.resolve("diagnostics/startup-trace.json"), StandardCharsets.UTF_8);
        assertFalse(output.contains("Sample mod initialized"));
        assertFalse(output.contains("Game starting"));
        assertFalse(diagnostics.contains("\"name\": \"classpath.create\""));
        assertFalse(diagnostics.contains("\"name\": \"entrypoint.invoke\""));
        assertFalse(diagnostics.contains("\"name\": \"game.launch\""));
    }

    @Test
    void requireReadyFailsWhenNoReadyLineDetected() throws Exception {
        Path cacheDir = tempDirectory.resolve("runtime/minecraft-cache");
        prepareCachedBaseline(cacheDir, createFakeServerJar(tempDirectory.resolve("fake-server.jar"), false), true);

        LoaderException exception =
            assertThrows(
                LoaderException.class,
                () ->
                    executeWithoutWrite(
                        baselineArgs(
                            cacheDir,
                            "--minecraft-offline",
                            "--minecraft-offline-replay",
                            "--minecraft-verify-files",
                            "--minecraft-cache-strict",
                            "--minecraft-launch",
                            "--minecraft-require-ready",
                            "--minecraft-launch-timeout-seconds",
                            "2",
                            "--minecraft-server-arg",
                            "--skip-ready"
                        )
                    )
            );

        assertTrue(exception.getMessage().contains("--minecraft-require-ready"));
        assertTrue(Files.exists(tempDirectory.resolve("minecraft-server-launch-result.json")));
        assertTrue(Files.readString(tempDirectory.resolve("minecraft-server-launch-result.json")).contains("\"readyDetected\": false"));
    }

    @Test
    void eulaSmokeStopAfterReadyRecordsReadyAndStop() throws Exception {
        Path cacheDir = tempDirectory.resolve("runtime/minecraft-cache");
        prepareCachedBaseline(cacheDir, createFakeServerJar(tempDirectory.resolve("fake-server.jar"), false), true);

        executeAndWrite(
            baselineArgs(
                cacheDir,
                "--minecraft-offline",
                "--minecraft-offline-replay",
                "--minecraft-verify-files",
                "--minecraft-cache-strict",
                "--minecraft-launch",
                "--minecraft-accept-eula-for-test",
                "--minecraft-stop-after-ready",
                "--minecraft-require-ready",
                "--minecraft-ready-timeout-seconds",
                "5",
                "--minecraft-launch-timeout-seconds",
                "10"
            )
        );

        String resultJson = Files.readString(tempDirectory.resolve("minecraft-server-launch-result.json"), StandardCharsets.UTF_8);
        assertTrue(resultJson.contains("\"readyDetected\": true"));
        assertTrue(resultJson.contains("\"stopRequested\": true"));
        assertEquals(
            "eula=true",
            Files.readString(tempDirectory.resolve("minecraft-server-baseline/1.21.8/eula.txt"), StandardCharsets.UTF_8).trim()
        );
    }

    private MinecraftVersionManifest parseManifestFixture(String resourcePath) throws Exception {
        return new MinecraftVersionManifestParser().parse(readFixture(resourcePath), resourcePath);
    }

    private void prepareCachedMetadataOnly(Path cacheDir) throws Exception {
        Files.createDirectories(cacheDir.resolve("metadata/versions"));
        Files.createDirectories(cacheDir.resolve("versions/1.21.8"));
        Files.writeString(cacheDir.resolve("metadata/version-manifest.json"), cachedManifest("https://example.invalid/1.21.8.json", "ignored"), StandardCharsets.UTF_8);
        Files.writeString(cacheDir.resolve("metadata/versions/1.21.8.json"), cachedVersionJson("https://example.invalid/server.jar", "abc123", 4L), StandardCharsets.UTF_8);
    }

    private void prepareCachedBaseline(Path cacheDir, Path serverJar, boolean createLock) throws Exception {
        Files.createDirectories(cacheDir.resolve("metadata/versions"));
        Path cachedServerJar = cacheDir.resolve("versions/1.21.8/1.21.8-server.jar");
        Files.createDirectories(cachedServerJar.getParent());
        Files.copy(serverJar, cachedServerJar);
        byte[] serverBytes = Files.readAllBytes(cachedServerJar);
        String serverSha1 = sha1(serverBytes);
        String serverSha256 = sha256(serverBytes);

        Files.writeString(
            cacheDir.resolve("metadata/version-manifest.json"),
            cachedManifest("https://example.invalid/1.21.8.json", sha1(cachedVersionJson("https://example.invalid/server.jar", serverSha1, serverBytes.length).getBytes(StandardCharsets.UTF_8))),
            StandardCharsets.UTF_8
        );
        Files.writeString(
            cacheDir.resolve("metadata/versions/1.21.8.json"),
            cachedVersionJson("https://example.invalid/server.jar", serverSha1, serverBytes.length),
            StandardCharsets.UTF_8
        );

        if (createLock) {
            Files.writeString(
                cacheDir.resolve("versions/1.21.8/server-artifacts.lock.json"),
                """
                {
                  "schema": 1,
                  "projectTargetMinecraft": "26.1.2",
                  "baselineMinecraft": "1.21.8",
                  "createdBy": "MCModLoader",
                  "artifacts": [
                    {
                      "id": "server-jar",
                      "kind": "server",
                      "path": "minecraft-cache/versions/1.21.8/1.21.8-server.jar",
                      "sourceUrl": "https://example.invalid/server.jar",
                      "sha1": "%s",
                      "sha256": "%s",
                      "size": %d,
                      "verifiedAt": "2026-05-08T00:00:00Z"
                    }
                  ]
                }
                """.formatted(serverSha1, serverSha256, serverBytes.length),
                StandardCharsets.UTF_8
            );
        }
    }

    private String[] baselineArgs(Path cacheDir, String... extraArgs) {
        List<String> args = new ArrayList<>();
        args.add("--game-main");
        args.add("unused.for.minecraft.RealBaseline");
        args.add("--game-provider");
        args.add("minecraft");
        args.add("--minecraft-side");
        args.add("server");
        args.add("--minecraft-dry-run");
        args.add("--minecraft-baseline-server");
        args.add("--minecraft-baseline-version");
        args.add(REAL_VERSION);
        args.add("--minecraft-real-smoke");
        args.add("--minecraft-cache-dir");
        args.add(cacheDir.toString());
        for (String extraArg : extraArgs) {
            args.add(extraArg);
        }
        return args.toArray(String[]::new);
    }

    private String cachedManifest(String versionUrl, String versionSha1) {
        return """
            {
              "latest": {
                "release": "1.21.8",
                "snapshot": "26w20a"
              },
              "versions": [
                {
                  "id": "1.21.8",
                  "type": "release",
                  "url": "%s",
                  "sha1": "%s"
                },
                {
                  "id": "26w20a",
                  "type": "snapshot",
                  "url": "https://example.invalid/26w20a.json",
                  "sha1": "snapshot"
                }
              ]
            }
            """.formatted(versionUrl, versionSha1);
    }

    private String cachedVersionJson(String serverUrl, String serverSha1, long size) {
        return """
            {
              "id": "1.21.8",
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
            """.formatted(serverUrl, serverSha1, size);
    }

    private String manifestBody(String versionUrl, String versionJsonBody) throws Exception {
        return """
            {
              "latest": {
                "release": "1.21.8",
                "snapshot": "26w20a"
              },
              "versions": [
                {
                  "id": "1.21.8",
                  "type": "release",
                  "url": "%s",
                  "sha1": "%s"
                }
              ]
            }
            """.formatted(versionUrl, sha1(versionJsonBody.getBytes(StandardCharsets.UTF_8)));
    }

    private String versionJsonBody(String serverUrlTemplate, String serverSha1, long size) {
        return """
            {
              "id": "1.21.8",
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
            """.formatted(serverUrlTemplate, serverSha1, size);
    }

    private Path createFakeServerJar(Path jarPath, boolean writeStderr) throws IOException {
        Files.createDirectories(jarPath.getParent());
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, FakeBaselineServerMain.class.getName());
        try (OutputStream outputStream = Files.newOutputStream(jarPath); JarOutputStream jarOutputStream = new JarOutputStream(outputStream, manifest)) {
            jarOutputStream.putNextEntry(new JarEntry(FakeBaselineServerMain.class.getName().replace('.', '/') + ".class"));
            jarOutputStream.write(readClassBytes(FakeBaselineServerMain.class));
            jarOutputStream.closeEntry();
        }
        return jarPath;
    }

    private byte[] readClassBytes(Class<?> type) throws IOException {
        String resourceName = type.getName().replace('.', '/') + ".class";
        try (var inputStream = type.getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IOException("Missing class bytes for " + resourceName);
            }
            return inputStream.readAllBytes();
        }
    }

    private String executeAndWrite(String[] args) throws Exception {
        JsonDiagnosticSink sink = new JsonDiagnosticSink(tempDirectory.resolve("diagnostics/startup-trace.json"));
        String output = captureStdout(() -> LoaderMain.execute(tempDirectory, args, sink));
        sink.write();
        return output;
    }

    private void executeWithoutWrite(String[] args) throws Exception {
        LoaderMain.execute(tempDirectory, args, new JsonDiagnosticSink(tempDirectory.resolve("diagnostics/startup-trace.json")));
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

    private void createStandardModJar(Path jarPath, String modId, String entrypointClassName) throws IOException {
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
            """.formatted(modId, entrypointClassName, toJsonObject(Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.2"))),
            Map.of(entrypointClassName.replace('.', '/') + ".class", new byte[] {1})
        );
    }

    private void createModJar(Path jarPath, String metadataJson, Map<String, byte[]> entries) throws IOException {
        Files.createDirectories(jarPath.getParent());
        try (OutputStream outputStream = Files.newOutputStream(jarPath); JarOutputStream jarOutputStream = new JarOutputStream(outputStream)) {
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
        for (Map.Entry<String, String> entry : values.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            if (!first) {
                builder.append(", ");
            }
            builder.append("\"").append(entry.getKey()).append("\": \"").append(entry.getValue()).append("\"");
            first = false;
        }
        builder.append("}");
        return builder.toString();
    }

    private String readFixture(String resourcePath) throws IOException {
        try (var inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Missing fixture " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
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

    private static final class VersionAndServerHttpServer implements AutoCloseable {
        private final HttpServer server;
        private final byte[] serverBytes;
        private final byte[] versionJsonBytes;

        VersionAndServerHttpServer(byte[] serverBytes, String versionJsonBodyTemplate) throws IOException {
            this.server = HttpServer.create(new InetSocketAddress(0), 0);
            this.serverBytes = serverBytes;
            this.versionJsonBytes =
                versionJsonBodyTemplate.replace("__SERVER_URL__", serverUri().toString()).getBytes(StandardCharsets.UTF_8);
            server.createContext(
                "/version",
                exchange -> {
                    exchange.sendResponseHeaders(200, this.versionJsonBytes.length);
                    try (OutputStream outputStream = exchange.getResponseBody()) {
                        outputStream.write(this.versionJsonBytes);
                    }
                }
            );
            server.createContext(
                "/server",
                exchange -> {
                    exchange.sendResponseHeaders(200, this.serverBytes.length);
                    try (OutputStream outputStream = exchange.getResponseBody()) {
                        outputStream.write(this.serverBytes);
                    }
                }
            );
            server.start();
        }

        java.net.URI versionUri() {
            return java.net.URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/version");
        }

        java.net.URI serverUri() {
            return java.net.URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/server");
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

    public static final class FakeBaselineServerMain {
        private FakeBaselineServerMain() {
        }

        public static void main(String[] args) throws Exception {
            boolean skipReady = false;
            int sleepSeconds = 0;

            for (int index = 0; index < args.length; index++) {
                String argument = args[index];
                if ("--skip-ready".equals(argument)) {
                    skipReady = true;
                    continue;
                }
                if ("--sleep-seconds".equals(argument) && index + 1 < args.length) {
                    sleepSeconds = Integer.parseInt(args[++index]);
                }
            }

            System.out.println("Starting fake baseline Minecraft server");
            if (!skipReady) {
                System.out.println("Done (0.1s)! For help, type \"help\"");
            }
            if (sleepSeconds > 0) {
                Thread.sleep(sleepSeconds * 1_000L);
            }

            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(System.in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if ("stop".equals(line.trim())) {
                        System.out.println("Stopping fake baseline Minecraft server");
                        return;
                    }
                }
            }
        }
    }
}
