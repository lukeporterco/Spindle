package com.mcmodloader.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mcmodloader.core.diagnostics.JsonDiagnosticSink;
import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.minecraft.MinecraftReproducibilityCheck;
import com.mcmodloader.core.minecraft.MinecraftReproducibilityChecker;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MegaMilestone7MinecraftRuntimeOwnershipTest {
    @TempDir
    Path tempDirectory;

    @Test
    void defaultCachePathDoesNotNestRuntimeRuntimeWhenWorkingDirectoryIsRuntime() throws Exception {
        Path runtime = tempDirectory.resolve("runtime");
        Files.createDirectories(runtime);

        execute(
            runtime,
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
            }
        );

        String artifacts = Files.readString(runtime.resolve("minecraft-artifacts.json"), StandardCharsets.UTF_8);
        assertTrue(artifacts.contains("\"cacheDirectory\": \"minecraft-cache\""));
        assertFalse(Files.exists(runtime.resolve("runtime/minecraft-cache")));
    }

    @Test
    void simpleJarRuntimePlanIsDeterministicAndAnalysisOnly() throws Exception {
        Path minecraftDir = createFixtureMinecraftDirectory(tempDirectory.resolve("minecraft"), createFakeServerJar(tempDirectory.resolve("fake-server.jar")), true);

        execute(
            tempDirectory,
            serverArgs(
                minecraftDir,
                "--minecraft-runtime-plan",
                "--minecraft-explain-runtime"
            )
        );
        String first = Files.readString(tempDirectory.resolve("minecraft-server-runtime-plan.json"), StandardCharsets.UTF_8);

        execute(
            tempDirectory,
            serverArgs(
                minecraftDir,
                "--minecraft-runtime-plan",
                "--minecraft-explain-runtime"
            )
        );
        String second = Files.readString(tempDirectory.resolve("minecraft-server-runtime-plan.json"), StandardCharsets.UTF_8);

        assertEquals(first, second);
        assertTrue(first.contains("\"milestoneName\": \"Mega-Milestone 7\""));
        assertTrue(first.contains("\"launchMode\": \"simple-jar\""));
        assertTrue(first.contains("\"modJarsOnMinecraftRuntimeClasspath\": false"));
        assertTrue(first.contains("\"minecraftModClassesLoaded\": false"));
        assertTrue(first.contains("\"mixinOccurred\": false"));
    }

    @Test
    void fakeBundledServerRuntimePlanExtractsRuntimeFiles() throws Exception {
        Path minecraftDir = createBundledFixtureMinecraftDirectory(tempDirectory.resolve("minecraft-bundled"));

        execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-runtime-plan"));

        String plan = Files.readString(tempDirectory.resolve("minecraft-server-runtime-plan.json"), StandardCharsets.UTF_8);
        assertTrue(plan.contains("\"launchMode\": \"bundled-server\""));
        assertTrue(plan.contains("server-runtime/"));
        assertTrue(Files.exists(tempDirectory.resolve("minecraft-cache/versions/26.1.2/server-runtime/versions/fixture-server.jar")));
        assertTrue(Files.exists(tempDirectory.resolve("minecraft-cache/versions/26.1.2/server-runtime/libraries/fixture-lib.jar")));
    }

    @Test
    void runtimeBoundaryOutputIsByteStableAcrossRepeatedRuns() throws Exception {
        Path minecraftDir = createFixtureMinecraftDirectory(tempDirectory.resolve("minecraft"), createFakeServerJar(tempDirectory.resolve("fake-server.jar")), true);

        execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-runtime-plan", "--minecraft-boundary-report"));
        String first = Files.readString(tempDirectory.resolve("minecraft-runtime-boundary.json"), StandardCharsets.UTF_8);

        execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-runtime-plan", "--minecraft-boundary-report"));
        String second = Files.readString(tempDirectory.resolve("minecraft-runtime-boundary.json"), StandardCharsets.UTF_8);

        assertEquals(first, second);
    }

    @Test
    void integrationPlanOutputIsByteStableAcrossRepeatedRuns() throws Exception {
        Path minecraftDir = createFixtureMinecraftDirectory(tempDirectory.resolve("minecraft"), createFakeServerJar(tempDirectory.resolve("fake-server.jar")), true);
        createModJar(
            tempDirectory.resolve("mods/server-mod.jar"),
            metadataJson("servermod", ThrowingEntrypoint.class.getName(), Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.2"), "server"),
            Map.of(ThrowingEntrypoint.class.getName().replace('.', '/') + ".class", readClassBytes(ThrowingEntrypoint.class))
        );

        execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-plan-mods"));
        String first = Files.readString(tempDirectory.resolve("minecraft-mod-integration-plan.json"), StandardCharsets.UTF_8);

        execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-plan-mods"));
        String second = Files.readString(tempDirectory.resolve("minecraft-mod-integration-plan.json"), StandardCharsets.UTF_8);

        assertEquals(first, second);
    }

    @Test
    void integrationPlanDoesNotLoadMinecraftTargetedEntrypoints() throws Exception {
        Path minecraftDir = createFixtureMinecraftDirectory(tempDirectory.resolve("minecraft"), createFakeServerJar(tempDirectory.resolve("fake-server.jar")), true);
        createModJar(
            tempDirectory.resolve("mods/server-mod.jar"),
            metadataJson("servermod", ThrowingEntrypoint.class.getName(), Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.2"), "server"),
            Map.of(ThrowingEntrypoint.class.getName().replace('.', '/') + ".class", readClassBytes(ThrowingEntrypoint.class))
        );

        String output =
            execute(
                tempDirectory,
                serverArgs(
                    minecraftDir,
                    "--minecraft-runtime-plan",
                    "--minecraft-boundary-report",
                    "--minecraft-integration-plan",
                    "--minecraft-explain-mods"
                )
            );

        String plan = Files.readString(tempDirectory.resolve("minecraft-mod-integration-plan.json"), StandardCharsets.UTF_8);
        String diagnostics = Files.readString(tempDirectory.resolve("diagnostics/startup-trace.json"), StandardCharsets.UTF_8);
        assertTrue(plan.contains("\"modId\": \"servermod\""));
        assertTrue(plan.contains("\"analysisOnly\": true"));
        assertTrue(plan.contains("\"modClassesLoaded\": false"));
        assertTrue(plan.contains("\"entrypointsInvoked\": false"));
        assertFalse(output.contains("should not run"));
        assertFalse(diagnostics.contains("\"name\": \"classpath.create\""));
        assertFalse(diagnostics.contains("\"name\": \"entrypoint.invoke\""));
    }

    @Test
    void minecraftPlanModsAloneWritesIntegrationPlanAndDoesNotLaunch() throws Exception {
        Path minecraftDir = createFixtureMinecraftDirectory(tempDirectory.resolve("minecraft"), createFakeServerJar(tempDirectory.resolve("fake-server.jar")), true);
        createModJar(
            tempDirectory.resolve("mods/server-mod.jar"),
            metadataJson("servermod", ThrowingEntrypoint.class.getName(), Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.2"), "server"),
            Map.of(ThrowingEntrypoint.class.getName().replace('.', '/') + ".class", readClassBytes(ThrowingEntrypoint.class))
        );

        String output = execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-plan-mods"));

        assertTrue(Files.exists(tempDirectory.resolve("minecraft-server-runtime-plan.json")));
        assertTrue(Files.exists(tempDirectory.resolve("minecraft-runtime-boundary.json")));
        assertTrue(Files.exists(tempDirectory.resolve("minecraft-mod-integration-plan.json")));
        assertFalse(Files.exists(tempDirectory.resolve("minecraft-server-launch-result.json")));
        assertFalse(output.contains("should not run"));
    }

    @Test
    void preflightWritesReportsAndDoesNotLaunchMinecraft() throws Exception {
        Path minecraftDir = createFixtureMinecraftDirectory(tempDirectory.resolve("minecraft"), createFakeServerJar(tempDirectory.resolve("fake-server.jar")), true);

        execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-preflight"));

        String preflight = Files.readString(tempDirectory.resolve("minecraft-preflight-result.json"), StandardCharsets.UTF_8);
        assertTrue(preflight.contains("\"milestoneName\": \"Mega-Milestone 7\""));
        assertTrue(preflight.contains("\"launchAttempted\": false"));
        assertTrue(preflight.contains("\"injectionOccurred\": false"));
        assertTrue(Files.exists(tempDirectory.resolve("minecraft-runtime-boundary.json")));
        assertTrue(Files.exists(tempDirectory.resolve("minecraft-mod-integration-plan.json")));
        assertFalse(Files.exists(tempDirectory.resolve("minecraft-server-launch-result.json")));
    }

    @Test
    void rejectedMinecraftTargetedModMakesPreflightFail() throws Exception {
        Path minecraftDir = createFixtureMinecraftDirectory(tempDirectory.resolve("minecraft"), createFakeServerJar(tempDirectory.resolve("fake-server.jar")), true);
        createModJar(
            tempDirectory.resolve("mods/client-only.jar"),
            metadataJson("clientonly", ThrowingEntrypoint.class.getName(), Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.2"), "client"),
            Map.of(ThrowingEntrypoint.class.getName().replace('.', '/') + ".class", readClassBytes(ThrowingEntrypoint.class))
        );

        assertThrows(LoaderException.class, () -> execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-preflight")));

        String preflight = Files.readString(tempDirectory.resolve("minecraft-preflight-result.json"), StandardCharsets.UTF_8);
        assertTrue(preflight.contains("\"succeeded\": false"));
        assertTrue(preflight.contains("\"rejectedModCount\": 1"));
        assertTrue(preflight.contains("clientonly"));
    }

    @Test
    void strictSideMismatchFailsPreflight() throws Exception {
        Path minecraftDir = createFixtureMinecraftDirectory(tempDirectory.resolve("minecraft"), createFakeServerJar(tempDirectory.resolve("fake-server.jar")), true);
        createModJar(
            tempDirectory.resolve("mods/client-only.jar"),
            metadataJson("clientonly", ThrowingEntrypoint.class.getName(), Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.2"), "client"),
            Map.of(ThrowingEntrypoint.class.getName().replace('.', '/') + ".class", readClassBytes(ThrowingEntrypoint.class))
        );

        assertThrows(LoaderException.class, () -> execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-preflight", "--minecraft-strict-side")));

        String preflight = Files.readString(tempDirectory.resolve("minecraft-preflight-result.json"), StandardCharsets.UTF_8);
        assertTrue(preflight.contains("\"type\": \"mod-side-mismatch\""));
        assertTrue(preflight.contains("\"severity\": \"FATAL\""));
    }

    @Test
    void strictClassVersionMismatchFailsPreflight() throws Exception {
        Path minecraftDir = createFixtureMinecraftDirectory(tempDirectory.resolve("minecraft"), createFakeServerJar(tempDirectory.resolve("fake-server.jar")), true);
        createModJar(
            tempDirectory.resolve("mods/too-new.jar"),
            metadataJson("toonew", "com.example.TooNew", Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.2"), "server"),
            Map.of("com/example/TooNew.class", classFileHeader(70))
        );

        assertThrows(LoaderException.class, () -> execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-preflight", "--minecraft-strict-class-versions")));

        String preflight = Files.readString(tempDirectory.resolve("minecraft-preflight-result.json"), StandardCharsets.UTF_8);
        assertTrue(preflight.contains("\"type\": \"mod-class-version-too-new\""));
        assertTrue(preflight.contains("\"fatalCount\": 1"));
    }

    @Test
    void strictBundledRuntimeVerificationPassesForValidFixture() throws Exception {
        Path minecraftDir = createBundledFixtureMinecraftDirectory(tempDirectory.resolve("minecraft-bundled-strict"));

        execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-runtime-plan", "--minecraft-cache-strict"));

        String plan = Files.readString(tempDirectory.resolve("minecraft-server-runtime-plan.json"), StandardCharsets.UTF_8);
        assertTrue(plan.contains("\"verificationStatus\": \"verified\""));
    }

    @Test
    void strictBundledRuntimeVerificationFailsForHashMismatch() throws Exception {
        Path minecraftDir = createBundledFixtureMinecraftDirectory(tempDirectory.resolve("minecraft-bundled-bad-hash"), false, false);

        assertThrows(LoaderException.class, () -> execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-runtime-plan", "--minecraft-cache-strict")));
    }

    @Test
    void bundledRuntimeTraversalCannotEscapeRuntimeCache() throws Exception {
        Path minecraftDir = createBundledFixtureMinecraftDirectory(tempDirectory.resolve("minecraft-bundled-traversal"), true, true);

        assertThrows(LoaderException.class, () -> execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-runtime-plan", "--minecraft-cache-strict")));
        assertFalse(Files.exists(tempDirectory.resolve("minecraft-cache/versions/26.1.2/escape.jar")));
    }

    @Test
    void modJarPathTraversalIsRejectedInIntegrationPlan() throws Exception {
        Path minecraftDir = createFixtureMinecraftDirectory(tempDirectory.resolve("minecraft"), createFakeServerJar(tempDirectory.resolve("fake-server.jar")), true);
        createModJar(
            tempDirectory.resolve("mods/path-traversal.jar"),
            metadataJson("pathtraversal", ThrowingEntrypoint.class.getName(), Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.2"), "server"),
            Map.of(
                ThrowingEntrypoint.class.getName().replace('.', '/') + ".class",
                readClassBytes(ThrowingEntrypoint.class),
                "../escape.txt",
                "escape".getBytes(StandardCharsets.UTF_8)
            )
        );

        execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-plan-mods"));

        String plan = Files.readString(tempDirectory.resolve("minecraft-mod-integration-plan.json"), StandardCharsets.UTF_8);
        assertTrue(plan.contains("\"type\": \"mod-suspicious-path\""));
        assertTrue(plan.contains("pathtraversal"));
    }

    @Test
    void launchCommandClasspathExcludesPlannedModJars() throws Exception {
        Path minecraftDir = createFixtureMinecraftDirectory(tempDirectory.resolve("minecraft"), createFakeServerJar(tempDirectory.resolve("fake-server.jar")), true);
        createModJar(
            tempDirectory.resolve("mods/server-mod.jar"),
            metadataJson("servermod", ThrowingEntrypoint.class.getName(), Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.2"), "server"),
            Map.of(ThrowingEntrypoint.class.getName().replace('.', '/') + ".class", readClassBytes(ThrowingEntrypoint.class))
        );

        execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-plan-mods"));

        String runtimePlan = Files.readString(tempDirectory.resolve("minecraft-server-runtime-plan.json"), StandardCharsets.UTF_8);
        String integrationPlan = Files.readString(tempDirectory.resolve("minecraft-mod-integration-plan.json"), StandardCharsets.UTF_8);
        assertFalse(runtimePlan.contains("server-mod.jar"));
        assertTrue(integrationPlan.contains("future-mod-classloader:servermod"));
        assertTrue(runtimePlan.contains("\"modJarsOnMinecraftRuntimeClasspath\": false"));
    }

    @Test
    void reproducibilityCheckerReportsIntentionalReportChanges() throws Exception {
        Path first = tempDirectory.resolve("first.json");
        Path second = tempDirectory.resolve("second.json");
        Files.writeString(first, "{\n  \"value\": 1\n}\n", StandardCharsets.UTF_8);
        Files.writeString(second, "{\n  \"value\": 2\n}\n", StandardCharsets.UTF_8);

        MinecraftReproducibilityCheck check =
            new MinecraftReproducibilityChecker().check(
                "Mega-Milestone 7",
                java.util.List.of(new MinecraftReproducibilityChecker.ReportPair("runtime-plan", first, second)),
                false
            );

        assertFalse(check.byteForByteEqual());
        assertTrue(check.failures().stream().anyMatch(failure -> failure.contains("bytes differ")));
    }

    private String[] serverArgs(Path minecraftDir, String... extraArgs) {
        java.util.List<String> args = new java.util.ArrayList<>();
        args.add("--game-main");
        args.add("unused.for.minecraft.MegaMilestone7");
        args.add("--game-provider");
        args.add("minecraft");
        args.add("--minecraft-version");
        args.add("26.1.2");
        args.add("--minecraft-dir");
        args.add(minecraftDir.toString());
        args.add("--minecraft-side");
        args.add("server");
        args.add("--minecraft-dry-run");
        args.add("--minecraft-verify-files");
        for (String extraArg : extraArgs) {
            args.add(extraArg);
        }
        return args.toArray(String[]::new);
    }

    private Path createFixtureMinecraftDirectory(Path minecraftDir, Path serverJar, boolean includeServerJar) throws IOException {
        Path versionDirectory = minecraftDir.resolve("versions/26.1.2");
        Files.createDirectories(versionDirectory);
        Files.writeString(versionDirectory.resolve("26.1.2.json"), versionJson(), StandardCharsets.UTF_8);
        if (includeServerJar) {
            Files.copy(serverJar, versionDirectory.resolve("26.1.2-server.jar"));
        }
        return minecraftDir;
    }

    private Path createBundledFixtureMinecraftDirectory(Path minecraftDir) throws IOException {
        return createBundledFixtureMinecraftDirectory(minecraftDir, true, false);
    }

    private Path createBundledFixtureMinecraftDirectory(Path minecraftDir, boolean validHashes, boolean traversalPath) throws IOException {
        Path versionDirectory = minecraftDir.resolve("versions/26.1.2");
        Files.createDirectories(versionDirectory);
        Files.writeString(versionDirectory.resolve("26.1.2.json"), versionJson(), StandardCharsets.UTF_8);
        Path outerJar = versionDirectory.resolve("26.1.2-server.jar");
        byte[] nestedJar = Files.readAllBytes(createFakeServerJar(tempDirectory.resolve("nested-server.jar")));
        String versionPath = traversalPath ? "../escape.jar" : "fixture-server.jar";
        String libraryPath = "fixture-lib.jar";
        String versionSha = validHashes ? sha1(nestedJar) : "0000000000000000000000000000000000000000";
        String librarySha = validHashes ? sha1(nestedJar) : "0000000000000000000000000000000000000000";
        try (OutputStream outputStream = Files.newOutputStream(outerJar); JarOutputStream jar = new JarOutputStream(outputStream)) {
            put(jar, "META-INF/main-class", FakeServerMain.class.getName().getBytes(StandardCharsets.UTF_8));
            put(jar, "META-INF/versions.list", (versionSha + "\tfixture\t" + versionPath + "\n").getBytes(StandardCharsets.UTF_8));
            put(jar, "META-INF/libraries.list", (librarySha + "\tfixture-lib\t" + libraryPath + "\n").getBytes(StandardCharsets.UTF_8));
            put(jar, "META-INF/versions/" + versionPath, nestedJar);
            put(jar, "META-INF/libraries/fixture-lib.jar", nestedJar);
        }
        return minecraftDir;
    }

    private String versionJson() {
        return """
            {
              "id": "26.1.2",
              "type": "release",
              "downloads": {
                "server": {
                  "url": "https://example.invalid/server.jar",
                  "sha1": "server-sha1",
                  "size": 111
                }
              },
              "libraries": [],
              "arguments": {
                "game": [],
                "jvm": []
              }
            }
            """;
    }

    private Path createFakeServerJar(Path jarPath) throws IOException {
        Files.createDirectories(jarPath.getParent());
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, FakeServerMain.class.getName());
        try (OutputStream outputStream = Files.newOutputStream(jarPath); JarOutputStream jar = new JarOutputStream(outputStream, manifest)) {
            put(jar, FakeServerMain.class.getName().replace('.', '/') + ".class", readClassBytes(FakeServerMain.class));
        }
        return jarPath;
    }

    private void createModJar(Path jarPath, String metadataJson, Map<String, byte[]> entries) throws IOException {
        Files.createDirectories(jarPath.getParent());
        try (OutputStream outputStream = Files.newOutputStream(jarPath); JarOutputStream jar = new JarOutputStream(outputStream)) {
            put(jar, "loader.mod.json", metadataJson.getBytes(StandardCharsets.UTF_8));
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                put(jar, entry.getKey(), entry.getValue());
            }
        }
    }

    private String metadataJson(String modId, String entrypointClassName, Map<String, String> depends, String side) {
        return """
            {
              "schema": 1,
              "id": "%s",
              "version": "1.0.0",
              "side": "%s",
              "entrypoints": {
                "main": [
                  "%s"
                ]
              },
              "depends": %s,
              "breaks": {}
            }
            """.formatted(modId, side, entrypointClassName, toJsonObject(depends));
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

    private void put(JarOutputStream jar, String name, byte[] bytes) throws IOException {
        jar.putNextEntry(new JarEntry(name));
        jar.write(bytes);
        jar.closeEntry();
    }

    private byte[] classFileHeader(int majorVersion) {
        return new byte[] {
            (byte) 0xCA,
            (byte) 0xFE,
            (byte) 0xBA,
            (byte) 0xBE,
            0,
            0,
            (byte) ((majorVersion >> 8) & 0xFF),
            (byte) (majorVersion & 0xFF)
        };
    }

    private String sha1(byte[] bytes) throws IOException {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IOException("SHA-1 unavailable", exception);
        }
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

    private String execute(Path workingDirectory, String[] args) throws Exception {
        JsonDiagnosticSink sink = new JsonDiagnosticSink(workingDirectory.resolve("diagnostics/startup-trace.json"));
        try {
            return captureStdout(() -> LoaderMain.execute(workingDirectory, args, sink));
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

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    public static final class FakeServerMain {
        private FakeServerMain() {
        }

        public static void main(String[] args) {
            System.out.println("Done (0.1s)! For help, type \"help\"");
        }
    }

    public static final class ThrowingEntrypoint implements com.mcmodloader.api.ModInitializer {
        @Override
        public void onInitialize() {
            throw new IllegalStateException("should not run");
        }
    }
}
