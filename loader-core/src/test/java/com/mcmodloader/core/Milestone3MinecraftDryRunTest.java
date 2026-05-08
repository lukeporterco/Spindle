package com.mcmodloader.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mcmodloader.core.diagnostics.JsonDiagnosticSink;
import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.mache.MacheReferenceReport;
import com.mcmodloader.core.mache.MacheReferenceScanner;
import com.mcmodloader.core.minecraft.MinecraftArgumentResolver;
import com.mcmodloader.core.minecraft.MinecraftFileVerifier;
import com.mcmodloader.core.minecraft.MinecraftInstallLocator;
import com.mcmodloader.core.minecraft.MinecraftLaunchPlan;
import com.mcmodloader.core.minecraft.MinecraftLaunchPlanBuilder;
import com.mcmodloader.core.minecraft.MinecraftLaunchPlanWriter;
import com.mcmodloader.core.minecraft.MinecraftLibrarySelector;
import com.mcmodloader.core.minecraft.MinecraftMetadataResolver;
import com.mcmodloader.core.minecraft.MinecraftProviderConfig;
import com.mcmodloader.core.minecraft.MinecraftSide;
import com.mcmodloader.core.minecraft.MinecraftVersionManifest;
import com.mcmodloader.core.minecraft.MinecraftVersionManifestParser;
import com.mcmodloader.core.minecraft.MinecraftVersionMetadata;
import com.mcmodloader.core.minecraft.MinecraftVersionMetadataParser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Milestone3MinecraftDryRunTest {
    @TempDir
    Path tempDirectory;

    @Test
    void minecraftProviderRequiresDryRun() throws Exception {
        Path minecraftDir = createFixtureMinecraftDirectory(tempDirectory.resolve("minecraft"));

        LoaderException exception =
            assertThrows(
                LoaderException.class,
                () -> executeWithoutWrite(
                    new String[] {
                        "--game-main",
                        "unused.for.minecraft.DryRun",
                        "--game-provider",
                        "minecraft",
                        "--minecraft-version",
                        "26.1.2",
                        "--minecraft-dir",
                        minecraftDir.toString()
                    }
                )
            );

        assertTrue(exception.getMessage().contains("--minecraft-dry-run"));
    }

    @Test
    void minecraftProviderRejectsMissingVersion() {
        LoaderException exception =
            assertThrows(
                LoaderException.class,
                () -> LoaderMain.execute(
                    tempDirectory,
                    new String[] {"--game-main", "unused.for.minecraft.DryRun", "--game-provider", "minecraft", "--minecraft-dry-run"},
                    new JsonDiagnosticSink(tempDirectory.resolve("diagnostics/startup-trace.json"))
                )
            );

        assertTrue(exception.getMessage().contains("--minecraft-version"));
    }

    @Test
    void minecraftVersionManifestParsesFixture() throws Exception {
        MinecraftVersionManifest manifest =
            new MinecraftVersionManifestParser().parse(readFixture("minecraft/version-manifest.json"), "minecraft/version-manifest.json");

        assertEquals("26.1.2", manifest.latestRelease());
        assertEquals("26w19a", manifest.latestSnapshot());
        assertEquals(2, manifest.versions().size());
        assertEquals("26.1.2", manifest.versions().getFirst().id());
    }

    @Test
    void minecraftMetadataResolverUsesExplicitVersionJsonFirst() throws Exception {
        Path explicitVersionJson = tempDirectory.resolve("explicit.json");
        Files.writeString(explicitVersionJson, readFixture("minecraft/version-26.1.2.json"), StandardCharsets.UTF_8);
        Path minecraftDir = createFixtureMinecraftDirectory(tempDirectory.resolve("minecraft"));
        Files.writeString(
            minecraftDir.resolve("versions/26.1.2/26.1.2.json"),
            readFixture("minecraft/version-26.1.2.json").replace("\"26.1.2\"", "\"LOCAL\""),
            StandardCharsets.UTF_8
        );

        MinecraftMetadataResolver.ResolvedVersionJson resolved =
            new MinecraftMetadataResolver()
                .resolve(
                    tempDirectory,
                    new MinecraftProviderConfig(
                        "26.1.2",
                        minecraftDir,
                        explicitVersionJson,
                        null,
                        MinecraftSide.CLIENT,
                        true,
                        false,
                        false,
                        tempDirectory.resolve("minecraft-launch-plan.json")
                    )
                );

        assertEquals(explicitVersionJson.toAbsolutePath().normalize(), resolved.versionJsonPath());
        assertEquals("explicit", resolved.metadataSource());
    }

    @Test
    void minecraftVersionMetadataParsesFixture() throws Exception {
        MinecraftVersionMetadata metadata =
            new MinecraftVersionMetadataParser().parse(readFixture("minecraft/version-26.1.2.json"), "fixture", MinecraftSide.CLIENT);

        assertEquals("26.1.2", metadata.id());
        assertEquals("net.minecraft.client.main.Main", metadata.mainClass());
        assertEquals("legacy", metadata.assetIndex().id());
        assertEquals("client-sha1", metadata.clientDownload().sha1());
        assertEquals("server-sha1", metadata.serverDownload().sha1());
        assertEquals(3, metadata.libraries().size());
        assertTrue(metadata.libraries().get(1).rules().stream().anyMatch(rule -> "windows".equals(rule.osName())));
        assertTrue(metadata.libraries().get(2).classifiers().containsKey("natives-windows"));
        assertEquals("natives-windows", metadata.libraries().get(2).natives().get("windows"));
        assertFalse(metadata.arguments().game().isEmpty());
        assertFalse(metadata.arguments().jvm().isEmpty());
    }

    @Test
    void minecraftInstallLocatorFindsDefaultPaths() {
        MinecraftInstallLocator locator = new MinecraftInstallLocator();
        Path minecraftDir = tempDirectory.resolve("fixture-minecraft");

        assertEquals(minecraftDir.resolve("versions/26.1.2/26.1.2.json").toAbsolutePath().normalize(), locator.versionJsonPath(minecraftDir, "26.1.2"));
        assertEquals(minecraftDir.resolve("versions/26.1.2/26.1.2.jar").toAbsolutePath().normalize(), locator.clientJarPath(minecraftDir, "26.1.2"));
        assertEquals(
            minecraftDir.resolve("versions/26.1.2/26.1.2-server.jar").toAbsolutePath().normalize(),
            locator.primaryServerJarPath(minecraftDir, "26.1.2")
        );
        assertEquals(minecraftDir.resolve("libraries").toAbsolutePath().normalize(), locator.librariesRoot(minecraftDir));
        assertEquals(minecraftDir.resolve("assets").toAbsolutePath().normalize(), locator.assetsRoot(minecraftDir));
        assertEquals(
            minecraftDir.resolve("assets/indexes/legacy.json").toAbsolutePath().normalize(),
            locator.assetIndexPath(minecraftDir, "legacy")
        );
    }

    @Test
    void minecraftLibrarySelectorAppliesOsRules() throws Exception {
        MinecraftVersionMetadata metadata =
            new MinecraftVersionMetadataParser().parse(readFixture("minecraft/version-26.1.2.json"), "fixture", MinecraftSide.CLIENT);
        MinecraftLibrarySelector selector = new MinecraftLibrarySelector();
        Path librariesRoot = tempDirectory.resolve("minecraft/libraries");

        MinecraftLibrarySelector.Selection windowsSelection =
            selector.select(metadata, librariesRoot, new MinecraftLibrarySelector.OperatingSystem("windows", "amd64", "10.0"));
        MinecraftLibrarySelector.Selection linuxSelection =
            selector.select(metadata, librariesRoot, new MinecraftLibrarySelector.OperatingSystem("linux", "amd64", "6.0"));

        assertEquals(
            List.of(
                librariesRoot.resolve("com/example/alpha/1.0/alpha-1.0.jar").toAbsolutePath().normalize(),
                librariesRoot.resolve("com/example/beta/2.0/beta-2.0.jar").toAbsolutePath().normalize(),
                librariesRoot.resolve("com/example/gamma/3.0/gamma-3.0.jar").toAbsolutePath().normalize()
            ),
            windowsSelection.libraries().stream().map(MinecraftLibrarySelector.SelectedLibrary::path).toList()
        );
        assertEquals(1, windowsSelection.nativeLibraries().size());
        assertEquals(2, linuxSelection.libraries().size());
        assertFalse(
            linuxSelection.libraries().stream().anyMatch(library -> library.path().toString().replace('\\', '/').contains("beta-2.0.jar"))
        );
    }

    @Test
    void minecraftArgumentResolverSubstitutesSafePlaceholders() throws Exception {
        MinecraftVersionMetadata metadata =
            new MinecraftVersionMetadataParser().parse(readFixture("minecraft/version-26.1.2.json"), "fixture", MinecraftSide.CLIENT);
        MinecraftArgumentResolver.ResolvedArguments arguments =
            new MinecraftArgumentResolver()
                .resolve(
                    clientConfig(tempDirectory.resolve("minecraft")),
                    metadata,
                    tempDirectory.resolve("minecraft"),
                    tempDirectory.resolve("minecraft/assets"),
                    tempDirectory.resolve("runtime/natives"),
                    List.of(tempDirectory.resolve("libs/a.jar"), tempDirectory.resolve("versions/26.1.2/26.1.2.jar"))
                );

        assertTrue(arguments.gameArguments().contains("offline_player"));
        assertTrue(arguments.gameArguments().contains("REDACTED"));
        assertFalse(arguments.gameArguments().contains("${auth_access_token}"));
        assertTrue(arguments.jvmArguments().stream().anyMatch(argument -> argument.contains(tempDirectory.resolve("runtime/natives").toString())));
    }

    @Test
    void minecraftClientLaunchPlanIsDeterministic() throws Exception {
        MinecraftLaunchPlan first = buildClientPlan(tempDirectory.resolve("one"));
        MinecraftLaunchPlan second = buildClientPlan(tempDirectory.resolve("two"));

        assertEquals(first.libraries(), second.libraries());
        assertEquals(first.classpath(), second.classpath());
        assertEquals("minecraft", first.provider());
        assertEquals("26.1.2", first.minecraftVersion());
        assertEquals("client", first.side());
        assertEquals("net.minecraft.client.main.Main", first.mainClass());
        assertTrue(first.classpath().getLast().endsWith("versions/26.1.2/26.1.2.jar"));
    }

    @Test
    void minecraftServerLaunchPlanIsDeterministic() throws Exception {
        Path workingDirectory = tempDirectory.resolve("server-plan");
        MinecraftLaunchPlan plan = buildPlan(workingDirectory, MinecraftSide.SERVER);
        Path outputPath = workingDirectory.resolve("minecraft-launch-plan.json");
        MinecraftLaunchPlanWriter writer = new MinecraftLaunchPlanWriter();
        writer.write(outputPath, plan);
        String written = Files.readString(outputPath, StandardCharsets.UTF_8);

        assertEquals("server", plan.side());
        assertTrue(plan.serverJar().endsWith("versions/26.1.2/26.1.2-server.jar"));
        assertTrue(plan.commandPreview().contains("-jar"));
        assertTrue(plan.commandPreview().stream().anyMatch(argument -> argument.endsWith("versions/26.1.2/26.1.2-server.jar")));
        assertTrue(written.contains("\"provider\": \"minecraft\""));
    }

    @Test
    void minecraftVerifyFilesReportsMissingFiles() throws Exception {
        Path minecraftDir = createFixtureMinecraftDirectory(tempDirectory.resolve("minecraft"));
        MinecraftVersionMetadata metadata =
            new MinecraftVersionMetadataParser().parse(readFixture("minecraft/version-26.1.2.json"), "fixture", MinecraftSide.CLIENT);
        MinecraftLibrarySelector.Selection selection =
            new MinecraftLibrarySelector()
                .select(
                    metadata,
                    minecraftDir.resolve("libraries"),
                    new MinecraftLibrarySelector.OperatingSystem("windows", "amd64", "10.0")
                );

        LoaderException exception =
            assertThrows(
                LoaderException.class,
                () -> new MinecraftFileVerifier()
                    .verify(
                        new MinecraftProviderConfig(
                            "26.1.2",
                            minecraftDir,
                            minecraftDir.resolve("versions/26.1.2/26.1.2.json"),
                            null,
                            MinecraftSide.CLIENT,
                            true,
                            true,
                            false,
                            tempDirectory.resolve("minecraft-launch-plan.json")
                        ),
                        new MinecraftMetadataResolver.ResolvedVersionJson(
                            "26.1.2",
                            minecraftDir.resolve("versions/26.1.2/26.1.2.json"),
                            readFixture("minecraft/version-26.1.2.json"),
                            "local"
                        ),
                        metadata,
                        selection,
                        new MinecraftInstallLocator()
                    )
            );

        assertTrue(exception.getMessage().contains("26.1.2.jar"));
        assertTrue(exception.getMessage().contains("alpha-1.0.jar"));
        assertTrue(exception.getMessage().contains("legacy.json"));
    }

    @Test
    void minecraftDryRunDoesNotCreateModClassLoaderOrInvokeEntrypoints() throws Exception {
        Path minecraftDir = createFixtureMinecraftDirectory(tempDirectory.resolve("minecraft"));
        createStandardModJar(tempDirectory.resolve("mods/sample-mod.jar"), "samplemod", "com.example.NotLoaded");

        String output =
            executeAndWrite(
                new String[] {
                    "--game-main",
                    "unused.for.minecraft.DryRun",
                    "--game-provider",
                    "minecraft",
                    "--minecraft-version",
                    "26.1.2",
                    "--minecraft-dir",
                    minecraftDir.toString(),
                    "--minecraft-side",
                    "client",
                    "--minecraft-dry-run"
                }
            );

        String diagnostics = Files.readString(tempDirectory.resolve("diagnostics/startup-trace.json"), StandardCharsets.UTF_8);
        assertTrue(output.contains("[loader] minecraft dry run complete"));
        assertFalse(output.contains("Sample mod initialized"));
        assertFalse(output.contains("Game starting"));
        assertFalse(diagnostics.contains("\"name\": \"classpath.create\""));
        assertFalse(diagnostics.contains("\"name\": \"entrypoint.invoke\""));
        assertFalse(diagnostics.contains("\"name\": \"game.launch\""));
        assertTrue(diagnostics.contains("\"name\": \"minecraft.dry_run.complete\""));
    }

    @Test
    void macheReferenceScannerReportsVersionDirectory() throws Exception {
        Path macheDir = tempDirectory.resolve("mache");
        Files.createDirectories(macheDir.resolve("versions/26.1.2"));
        Files.writeString(macheDir.resolve("settings.gradle.kts"), "rootProject.name = \"mache\"", StandardCharsets.UTF_8);
        Files.writeString(macheDir.resolve("gradle.properties"), "group=io.papermc", StandardCharsets.UTF_8);
        Files.writeString(macheDir.resolve("README.md"), "# Mache", StandardCharsets.UTF_8);
        Files.writeString(macheDir.resolve("LICENSE"), "MIT", StandardCharsets.UTF_8);

        MacheReferenceReport report = new MacheReferenceScanner().scan(macheDir, "26.1.2");

        assertTrue(report.detectedVersionDirectories().contains("26.1.2"));
        assertTrue(report.hasRequestedVersionDirectory());
        assertEquals("release/26.1.2", report.branchHint());
        assertTrue(report.files().settingsGradle());
    }

    @Test
    void macheReferenceScannerWarnsAboutReferenceOnlyLicense() throws Exception {
        Path macheDir = tempDirectory.resolve("mache");
        Files.createDirectories(macheDir.resolve("versions/26.1.2"));
        Files.writeString(macheDir.resolve("LICENSE"), "LGPL reference text", StandardCharsets.UTF_8);

        MacheReferenceReport report = new MacheReferenceScanner().scan(macheDir, "26.1.2");

        assertTrue(report.warnings().contains("Mache is reference-only. Do not copy code into this MIT project."));
    }

    private MinecraftLaunchPlan buildClientPlan(Path workingDirectory) throws Exception {
        return buildPlan(workingDirectory, MinecraftSide.CLIENT);
    }

    private MinecraftLaunchPlan buildPlan(Path workingDirectory, MinecraftSide side) throws Exception {
        Path minecraftDir = createFixtureMinecraftDirectory(workingDirectory.resolve("minecraft"));
        MinecraftVersionMetadata metadata =
            new MinecraftVersionMetadataParser().parse(readFixture("minecraft/version-26.1.2.json"), "fixture", side == MinecraftSide.CLIENT ? MinecraftSide.CLIENT : MinecraftSide.SERVER);
        MinecraftProviderConfig config =
            new MinecraftProviderConfig(
                "26.1.2",
                minecraftDir,
                minecraftDir.resolve("versions/26.1.2/26.1.2.json"),
                null,
                side,
                true,
                false,
                false,
                workingDirectory.resolve("minecraft-launch-plan.json")
            );
        MinecraftLibrarySelector.Selection selection =
            new MinecraftLibrarySelector()
                .select(
                    metadata,
                    minecraftDir.resolve("libraries"),
                    new MinecraftLibrarySelector.OperatingSystem("windows", "amd64", "10.0")
                );
        MinecraftInstallLocator installLocator = new MinecraftInstallLocator();
        List<Path> classpath = new java.util.ArrayList<>(selection.libraries().stream().map(MinecraftLibrarySelector.SelectedLibrary::path).toList());
        if (side == MinecraftSide.CLIENT) {
            classpath.add(installLocator.clientJarPath(minecraftDir, metadata.id()));
        } else {
            classpath.add(installLocator.primaryServerJarPath(minecraftDir, metadata.id()));
        }
        MinecraftArgumentResolver.ResolvedArguments arguments =
            new MinecraftArgumentResolver()
                .resolve(
                    config,
                    metadata,
                    minecraftDir,
                    minecraftDir.resolve("assets"),
                    workingDirectory.resolve("natives/26.1.2").resolve(side.id()),
                    classpath
                );

        MinecraftLaunchPlan plan =
            new MinecraftLaunchPlanBuilder()
                .build(
                    workingDirectory,
                    config,
                    new MinecraftMetadataResolver.ResolvedVersionJson(
                        "26.1.2",
                        minecraftDir.resolve("versions/26.1.2/26.1.2.json"),
                        readFixture("minecraft/version-26.1.2.json"),
                        "explicit"
                    ),
                    metadata,
                    selection,
                    arguments,
                    installLocator
                );

        assertNotNull(plan.assetIndex());
        return plan;
    }

    private MinecraftProviderConfig clientConfig(Path minecraftDir) {
        return new MinecraftProviderConfig(
            "26.1.2",
            minecraftDir,
            minecraftDir.resolve("versions/26.1.2/26.1.2.json"),
            null,
            MinecraftSide.CLIENT,
            true,
            false,
            false,
            tempDirectory.resolve("minecraft-launch-plan.json")
        );
    }

    private Path createFixtureMinecraftDirectory(Path minecraftDir) throws IOException {
        Path versionJson = minecraftDir.resolve("versions/26.1.2/26.1.2.json");
        Files.createDirectories(versionJson.getParent());
        Files.writeString(versionJson, readFixture("minecraft/version-26.1.2.json"), StandardCharsets.UTF_8);
        return minecraftDir;
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

    private String readFixture(String resourcePath) throws IOException {
        try (var inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Missing fixture " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
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
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        builder.append("  \"schema\": 1,\n");
        builder.append("  \"id\": \"").append(modId).append("\",\n");
        builder.append("  \"version\": \"1.0.0\",\n");
        builder.append("  \"side\": \"universal\",\n");
        builder.append("  \"entrypoints\": {\n");
        builder.append("    \"main\": [\n");
        builder.append("      \"").append(entrypointClassName).append("\"\n");
        builder.append("    ]\n");
        builder.append("  },\n");
        builder.append("  \"depends\": ").append(toJsonObject(depends)).append(",\n");
        builder.append("  \"breaks\": {}\n");
        builder.append("}\n");
        return builder.toString();
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
}
