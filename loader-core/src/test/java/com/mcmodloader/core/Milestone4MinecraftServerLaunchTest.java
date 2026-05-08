package com.mcmodloader.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mcmodloader.core.diagnostics.JsonDiagnosticSink;
import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.process.JavaExecutableResolver;
import com.mcmodloader.core.process.MinecraftProcessConfig;
import com.mcmodloader.core.process.MinecraftProcessResult;
import com.mcmodloader.core.process.MinecraftProcessResultWriter;
import com.mcmodloader.core.process.MinecraftServerProcessLauncher;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Milestone4MinecraftServerLaunchTest {
    @TempDir
    Path tempDirectory;

    @Test
    void minecraftLaunchRequiresServerSide() throws Exception {
        Path minecraftDir = createFixtureMinecraftDirectory(tempDirectory.resolve("minecraft"), createFakeServerJar(tempDirectory.resolve("fake-server.jar")), true);

        LoaderException exception =
            assertThrows(
                LoaderException.class,
                () -> executeWithoutWrite(
                    new String[] {
                        "--game-main",
                        "unused.for.minecraft.ClientLaunch",
                        "--game-provider",
                        "minecraft",
                        "--minecraft-version",
                        "26.1.2",
                        "--minecraft-dir",
                        minecraftDir.toString(),
                        "--minecraft-side",
                        "client",
                        "--minecraft-dry-run",
                        "--minecraft-verify-files",
                        "--minecraft-launch"
                    }
                )
            );

        assertTrue(exception.getMessage().contains("--minecraft-side server"));
    }

    @Test
    void minecraftLaunchRequiresVerifyFiles() throws Exception {
        Path minecraftDir = createFixtureMinecraftDirectory(tempDirectory.resolve("minecraft"), createFakeServerJar(tempDirectory.resolve("fake-server.jar")), true);

        LoaderException exception =
            assertThrows(
                LoaderException.class,
                () -> executeWithoutWrite(
                    new String[] {
                        "--game-main",
                        "unused.for.minecraft.ServerLaunch",
                        "--game-provider",
                        "minecraft",
                        "--minecraft-version",
                        "26.1.2",
                        "--minecraft-dir",
                        minecraftDir.toString(),
                        "--minecraft-side",
                        "server",
                        "--minecraft-dry-run",
                        "--minecraft-launch"
                    }
                )
            );

        assertTrue(exception.getMessage().contains("--minecraft-verify-files"));
    }

    @Test
    void minecraftServerDirDefaultsUnderRuntime() throws Exception {
        Path minecraftDir = createFixtureMinecraftDirectory(tempDirectory.resolve("minecraft"), createFakeServerJar(tempDirectory.resolve("fake-server.jar")), true);

        executeAndWrite(
            new String[] {
                "--game-main",
                "unused.for.minecraft.ServerLaunch",
                "--game-provider",
                "minecraft",
                "--minecraft-version",
                "26.1.2",
                "--minecraft-dir",
                minecraftDir.toString(),
                "--minecraft-side",
                "server",
                "--minecraft-dry-run",
                "--minecraft-verify-files",
                "--minecraft-launch",
                "--minecraft-stop-after-ready",
                "--minecraft-accept-eula-for-test"
            }
        );

        Path expected = tempDirectory.resolve("runtime/minecraft-server/26.1.2");
        assertTrue(Files.isDirectory(expected));
        String result = Files.readString(tempDirectory.resolve("runtime/minecraft-server-launch-result.json"));
        assertTrue(result.contains("\"serverDirectory\": \"runtime/minecraft-server/26.1.2\""));
    }

    @Test
    void minecraftAcceptEulaWritesOnlyWhenFlagPresent() throws Exception {
        Path serverJar = createFakeServerJar(tempDirectory.resolve("fake-server.jar"));
        Path withoutEulaDir = createFixtureMinecraftDirectory(tempDirectory.resolve("minecraft-no-eula"), serverJar, true);

        executeAndWrite(
            new String[] {
                "--game-main",
                "unused.for.minecraft.ServerLaunch",
                "--game-provider",
                "minecraft",
                "--minecraft-version",
                "26.1.2",
                "--minecraft-dir",
                withoutEulaDir.toString(),
                "--minecraft-side",
                "server",
                "--minecraft-dry-run",
                "--minecraft-verify-files",
                "--minecraft-launch",
                "--minecraft-stop-after-ready",
                "--minecraft-server-dir",
                tempDirectory.resolve("server-no-eula").toString()
            }
        );
        assertFalse(Files.exists(tempDirectory.resolve("server-no-eula/eula.txt")));

        Path withEulaDir = createFixtureMinecraftDirectory(tempDirectory.resolve("minecraft-with-eula"), serverJar, true);
        executeAndWrite(
            new String[] {
                "--game-main",
                "unused.for.minecraft.ServerLaunch",
                "--game-provider",
                "minecraft",
                "--minecraft-version",
                "26.1.2",
                "--minecraft-dir",
                withEulaDir.toString(),
                "--minecraft-side",
                "server",
                "--minecraft-dry-run",
                "--minecraft-verify-files",
                "--minecraft-launch",
                "--minecraft-stop-after-ready",
                "--minecraft-accept-eula-for-test",
                "--minecraft-server-dir",
                tempDirectory.resolve("server-with-eula").toString()
            }
        );
        assertEquals("eula=true", Files.readString(tempDirectory.resolve("server-with-eula/eula.txt")).trim());
    }

    @Test
    void javaExecutableResolverUsesCurrentJvm() throws Exception {
        Path javaExecutable = new JavaExecutableResolver().resolve();

        assertNotNull(javaExecutable);
        assertTrue(Files.isRegularFile(javaExecutable) || javaExecutable.toString().endsWith("java"));
    }

    @Test
    void serverProcessLauncherCapturesOutput() throws Exception {
        Path serverJar = createFakeServerJar(tempDirectory.resolve("fake-server.jar"));
        MinecraftProcessResult result = launchProcess(serverJar, List.of("--write-stderr"), false, 10, 5);

        assertTrue(result.stdoutTail().contains("Starting fake Minecraft server"));
        assertTrue(result.stderrTail().contains("Fake server stderr line"));
    }

    @Test
    void serverProcessLauncherHandlesTimeout() throws Exception {
        Path serverJar = createFakeServerJar(tempDirectory.resolve("fake-server.jar"));
        MinecraftProcessResult result = launchProcess(serverJar, List.of("--skip-ready", "--sleep-seconds", "30"), false, 1, 1);

        assertTrue(result.started());
        assertTrue(result.timedOut());
        assertFalse(result.readyDetected());
    }

    @Test
    void serverProcessLauncherStopAfterReady() throws Exception {
        Path serverJar = createFakeServerJar(tempDirectory.resolve("fake-server.jar"));
        MinecraftProcessResult result = launchProcess(serverJar, List.of(), true, 10, 5);

        assertTrue(result.readyDetected());
        assertTrue(result.stopRequested());
        assertEquals(0, result.exitCode());
        assertTrue(result.stdoutTail().contains("Stopping fake Minecraft server"));
    }

    @Test
    void serverLaunchResultIsWritten() throws Exception {
        Path minecraftDir = createFixtureMinecraftDirectory(tempDirectory.resolve("minecraft"), createFakeServerJar(tempDirectory.resolve("fake-server.jar")), true);

        executeAndWrite(
            new String[] {
                "--game-main",
                "unused.for.minecraft.ServerLaunch",
                "--game-provider",
                "minecraft",
                "--minecraft-version",
                "26.1.2",
                "--minecraft-dir",
                minecraftDir.toString(),
                "--minecraft-side",
                "server",
                "--minecraft-dry-run",
                "--minecraft-verify-files",
                "--minecraft-launch",
                "--minecraft-stop-after-ready",
                "--minecraft-accept-eula-for-test",
                "--minecraft-server-jvm-arg",
                "-Xmx128m"
            }
        );

        String resultJson = Files.readString(tempDirectory.resolve("runtime/minecraft-server-launch-result.json"));
        assertTrue(resultJson.contains("\"commandPreview\""));
        assertTrue(resultJson.contains("\"started\": true"));
        assertTrue(resultJson.contains("\"readyDetected\": true"));
        assertTrue(resultJson.contains("\"stopRequested\": true"));
        assertTrue(resultJson.contains("\"durationMs\""));
        assertTrue(resultJson.contains("\"stdoutTail\""));
        assertTrue(resultJson.contains("\"stderrTail\""));
        assertTrue(resultJson.contains("-Xmx128m"));
    }

    @Test
    void minecraftServerLaunchDoesNotCreateModClassLoaderOrInvokeEntrypoints() throws Exception {
        Path minecraftDir = createFixtureMinecraftDirectory(tempDirectory.resolve("minecraft"), createFakeServerJar(tempDirectory.resolve("fake-server.jar")), true);
        createStandardModJar(tempDirectory.resolve("mods/sample-mod.jar"), "samplemod", "com.example.NotLoaded");

        String output =
            executeAndWrite(
                new String[] {
                    "--game-main",
                    "unused.for.minecraft.ServerLaunch",
                    "--game-provider",
                    "minecraft",
                    "--minecraft-version",
                    "26.1.2",
                    "--minecraft-dir",
                    minecraftDir.toString(),
                    "--minecraft-side",
                    "server",
                    "--minecraft-dry-run",
                    "--minecraft-verify-files",
                    "--minecraft-launch",
                    "--minecraft-stop-after-ready",
                    "--minecraft-accept-eula-for-test"
                }
            );

        String diagnostics = Files.readString(tempDirectory.resolve("diagnostics/startup-trace.json"), StandardCharsets.UTF_8);
        assertTrue(output.contains("[loader] minecraft server launch complete"));
        assertFalse(output.contains("Sample mod initialized"));
        assertFalse(output.contains("Game starting"));
        assertFalse(diagnostics.contains("\"name\": \"classpath.create\""));
        assertFalse(diagnostics.contains("\"name\": \"entrypoint.invoke\""));
        assertFalse(diagnostics.contains("\"name\": \"game.launch\""));
        assertTrue(diagnostics.contains("\"name\": \"minecraft.server_launch.complete\""));
    }

    @Test
    void minecraftServerLaunchRequiresExistingServerJar() throws Exception {
        Path minecraftDir = createFixtureMinecraftDirectory(tempDirectory.resolve("minecraft"), tempDirectory.resolve("missing-server.jar"), false);

        LoaderException exception =
            assertThrows(
                LoaderException.class,
                () -> executeWithoutWrite(
                    new String[] {
                        "--game-main",
                        "unused.for.minecraft.ServerLaunch",
                        "--game-provider",
                        "minecraft",
                        "--minecraft-version",
                        "26.1.2",
                        "--minecraft-dir",
                        minecraftDir.toString(),
                        "--minecraft-side",
                        "server",
                        "--minecraft-dry-run",
                        "--minecraft-verify-files",
                        "--minecraft-launch"
                    }
                )
            );

        assertTrue(exception.getMessage().contains("26.1.2-server.jar"));
        assertFalse(Files.exists(tempDirectory.resolve("runtime/minecraft-server-launch-result.json")));
    }

    private MinecraftProcessResult launchProcess(Path serverJar, List<String> serverArgs, boolean stopAfterReady, int timeoutSeconds, int readyTimeoutSeconds)
        throws Exception {
        Path serverDirectory = tempDirectory.resolve("process-server");
        return new MinecraftServerProcessLauncher()
            .launch(
                "26.1.2",
                new MinecraftProcessConfig(
                    serverDirectory,
                    serverJar,
                    new JavaExecutableResolver().resolve(),
                    List.of(),
                    serverArgs,
                    timeoutSeconds,
                    stopAfterReady,
                    readyTimeoutSeconds,
                    false
                ),
                path -> tempDirectory.relativize(path.toAbsolutePath().normalize()).toString().replace('\\', '/')
            );
    }

    private Path createFixtureMinecraftDirectory(Path minecraftDir, Path serverJar, boolean includeServerJar) throws IOException {
        Path versionDirectory = minecraftDir.resolve("versions/26.1.2");
        Files.createDirectories(versionDirectory);
        Files.writeString(
            versionDirectory.resolve("26.1.2.json"),
            """
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
            """,
            StandardCharsets.UTF_8
        );
        if (includeServerJar) {
            Files.copy(serverJar, versionDirectory.resolve("26.1.2-server.jar"));
        }
        return minecraftDir;
    }

    private Path createFakeServerJar(Path jarPath) throws IOException {
        Files.createDirectories(jarPath.getParent());
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, FakeServerMain.class.getName());
        try (
            OutputStream outputStream = Files.newOutputStream(jarPath);
            JarOutputStream jarOutputStream = new JarOutputStream(outputStream, manifest)
        ) {
            jarOutputStream.putNextEntry(new JarEntry(FakeServerMain.class.getName().replace('.', '/') + ".class"));
            jarOutputStream.write(readClassBytes(FakeServerMain.class));
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
            metadataJson(modId, entrypointClassName, Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.2")),
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

    private String metadataJson(String modId, String entrypointClassName, Map<String, String> depends) {
        return """
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
            """.formatted(modId, entrypointClassName, toJsonObject(depends));
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

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    public static final class FakeServerMain {
        private FakeServerMain() {
        }

        public static void main(String[] args) throws Exception {
            boolean skipReady = false;
            boolean writeStderr = false;
            int sleepSeconds = 0;

            for (int index = 0; index < args.length; index++) {
                String argument = args[index];
                if ("--skip-ready".equals(argument)) {
                    skipReady = true;
                    continue;
                }
                if ("--write-stderr".equals(argument)) {
                    writeStderr = true;
                    continue;
                }
                if ("--sleep-seconds".equals(argument) && index + 1 < args.length) {
                    sleepSeconds = Integer.parseInt(args[++index]);
                }
            }

            System.out.println("Starting fake Minecraft server");
            if (writeStderr) {
                System.err.println("Fake server stderr line");
            }
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
                        System.out.println("Stopping fake Minecraft server");
                        return;
                    }
                }
            }
        }
    }
}
