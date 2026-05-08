package com.mcmodloader.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mcmodloader.core.diagnostics.JsonDiagnosticSink;
import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.discovery.ModCandidate;
import com.mcmodloader.core.launch.LaunchContext;
import com.mcmodloader.core.metadata.ModMetadata;
import com.mcmodloader.core.metadata.ModMetadataParser;
import com.mcmodloader.core.ownership.PackageOwnershipIndex;
import com.mcmodloader.core.resource.ResourceConflictIndex;
import com.mcmodloader.core.resolve.DependencyResolver;
import com.mcmodloader.core.resolve.ResolvedModSet;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Milestone2FrozenGraphTest {
    private final ModMetadataParser metadataParser = new ModMetadataParser();
    private final DependencyResolver dependencyResolver = new DependencyResolver();

    @TempDir
    Path tempDirectory;

    @Test
    void frozenModGraphIsCreatedAfterLockfileBoundary() throws Exception {
        createStandardModJar(tempDirectory.resolve("mods/sample-mod.jar"), "samplemod", GraphLoadSentinel.class.getName());

        GraphLoadSentinel.INITIALIZED.set(false);

        executeAndWrite(new String[] {"--game-main", ValidationGameMain.class.getName(), "--validate-only"});

        String diagnostics = Files.readString(tempDirectory.resolve("diagnostics/startup-trace.json"));
        assertTrue(Files.exists(tempDirectory.resolve("loader.lock.json")));
        assertTrue(diagnostics.contains("\"name\": \"frozen_mod_graph.create\""));
        assertFalse(GraphLoadSentinel.INITIALIZED.get());
    }

    @Test
    void modpackStateIsWrittenDeterministically() throws Exception {
        createStandardModJar(tempDirectory.resolve("mods/sample-mod.jar"), "samplemod", GraphLoadSentinel.class.getName());

        executeAndWrite(new String[] {"--game-main", ValidationGameMain.class.getName(), "--validate-only"});
        String first = Files.readString(tempDirectory.resolve("modpack-state.json"));

        executeAndWrite(new String[] {"--game-main", ValidationGameMain.class.getName(), "--validate-only"});
        String second = Files.readString(tempDirectory.resolve("modpack-state.json"));

        assertEquals(first, second);
        assertTrue(first.contains("\"loader\": \"0.1.0\""));
        assertTrue(first.contains("\"java\": 25"));
        assertTrue(first.contains("\"minecraft\": \"26.1.2\""));
        assertTrue(first.contains("\"gameProvider\""));
        assertTrue(first.contains("\"sha256\""));
        assertTrue(first.contains("\"entrypoints\""));
        assertTrue(first.contains("\"depends\""));
        assertTrue(first.contains("\"breaks\": {}"));
        assertTrue(first.contains("\"classCount\": 1"));
        assertTrue(first.contains("\"packageCount\": 1"));
        assertTrue(first.contains("\"resourceCount\": 0"));
        assertTrue(first.contains("\"resourceConflicts\": []"));
        assertTrue(first.contains("\"classOwnership\""));
        assertTrue(first.contains("\"packageOwnership\""));
        assertTrue(first.contains("\"classpath\""));
    }

    @Test
    void dependencyGraphIsWrittenDeterministically() throws Exception {
        createStandardModJar(tempDirectory.resolve("mods/sample-mod.jar"), "samplemod", GraphLoadSentinel.class.getName());

        executeAndWrite(new String[] {"--game-main", ValidationGameMain.class.getName(), "--validate-only"});
        String first = Files.readString(tempDirectory.resolve("dependency-graph.json"));

        executeAndWrite(new String[] {"--game-main", ValidationGameMain.class.getName(), "--validate-only"});
        String second = Files.readString(tempDirectory.resolve("dependency-graph.json"));

        assertEquals(first, second);
        assertTrue(first.contains("\"id\": \"loader\""));
        assertTrue(first.contains("\"id\": \"java\""));
        assertTrue(first.contains("\"id\": \"minecraft\""));
        assertTrue(first.contains("\"id\": \"samplemod\""));
        assertTrue(first.contains("\"kind\": \"depends\""));
        assertTrue(first.contains("\"satisfiedBy\": \"0.1.0\""));
        assertTrue(first.contains("\"incompatibilities\": []"));
    }

    @Test
    void validateOnlyDoesNotInvokeEntrypointsOrLaunchGame() throws Exception {
        createStandardModJar(tempDirectory.resolve("mods/sample-mod.jar"), "samplemod", ValidationEntrypoint.class.getName());

        ValidationEntrypoint.INVOKED.set(false);
        ValidationEntrypoint.INITIALIZED.set(false);
        ValidationGameMain.INVOKED.set(false);

        String output = executeAndWrite(new String[] {"--game-main", ValidationGameMain.class.getName(), "--validate-only"});
        String diagnostics = Files.readString(tempDirectory.resolve("diagnostics/startup-trace.json"));

        assertTrue(output.contains("[loader] validation complete"));
        assertFalse(output.contains("Game starting"));
        assertFalse(ValidationEntrypoint.INITIALIZED.get());
        assertFalse(ValidationEntrypoint.INVOKED.get());
        assertFalse(ValidationGameMain.INVOKED.get());
        assertFalse(diagnostics.contains("\"name\": \"classpath.create\""));
        assertFalse(diagnostics.contains("\"name\": \"entrypoint.invoke\""));
        assertFalse(diagnostics.contains("\"name\": \"game.launch\""));
        assertTrue(diagnostics.contains("\"name\": \"validation.complete\""));
    }

    @Test
    void explainModePrintsSummaryWithoutLaunching() throws Exception {
        createStandardModJar(tempDirectory.resolve("mods/sample-mod.jar"), "samplemod", ValidationEntrypoint.class.getName());

        ValidationEntrypoint.INVOKED.set(false);
        ValidationGameMain.INVOKED.set(false);

        String output = executeAndWrite(new String[] {"--game-main", ValidationGameMain.class.getName(), "--validate-only", "--explain"});

        assertTrue(output.contains("[loader] explain: provider sample 26.1.2"));
        assertTrue(output.contains("[loader] explain: resolved 1 mod"));
        assertTrue(output.contains("[loader] explain: duplicate resources 0"));
        assertTrue(output.contains("[loader] explain: split packages 0"));
        assertFalse(ValidationEntrypoint.INVOKED.get());
        assertFalse(ValidationGameMain.INVOKED.get());
    }

    @Test
    void missingDependencyErrorIncludesContext() throws LoaderException {
        ModMetadata metadata = metadata("samplemod", "1.0.0", Map.of("loader", ">=0.1.0", "missinglib", ">=1.0.0"), Map.of());

        LoaderException exception =
            assertThrows(LoaderException.class, () -> dependencyResolver.resolve(context(), List.of(candidate("mods/a.jar", "aaa", metadata))));

        assertTrue(exception.getMessage().contains("samplemod"));
        assertTrue(exception.getMessage().contains("missinglib"));
        assertTrue(exception.getMessage().contains(">=1.0.0"));
        assertTrue(exception.getMessage().contains("samplemod"));
    }

    @Test
    void duplicateModIdErrorIncludesPaths() throws LoaderException {
        ModMetadata left = metadata("samplemod", "1.0.0", Map.of("loader", ">=0.1.0"), Map.of());
        ModMetadata right = metadata("samplemod", "1.1.0", Map.of("loader", ">=0.1.0"), Map.of());

        LoaderException exception =
            assertThrows(
                LoaderException.class,
                () -> dependencyResolver.resolve(context(), List.of(candidate("mods/left.jar", "aaa", left), candidate("mods/right.jar", "bbb", right)))
            );

        assertTrue(exception.getMessage().contains("samplemod"));
        assertTrue(exception.getMessage().contains("mods/left.jar"));
        assertTrue(exception.getMessage().contains("mods/right.jar"));
    }

    @Test
    void builtinDependencyVersionFailureIncludesContext() throws LoaderException {
        ModMetadata metadata = metadata("samplemod", "1.0.0", Map.of("java", ">=26"), Map.of());

        LoaderException exception =
            assertThrows(LoaderException.class, () -> dependencyResolver.resolve(context(), List.of(candidate("mods/a.jar", "aaa", metadata))));

        assertTrue(exception.getMessage().contains("samplemod"));
        assertTrue(exception.getMessage().contains("java"));
        assertTrue(exception.getMessage().contains(">=26"));
        assertTrue(exception.getMessage().contains("25"));
    }

    @Test
    void modDependencyVersionFailureIncludesContext() throws LoaderException {
        ModMetadata requester = metadata("samplemod", "1.0.0", Map.of("helpermod", ">=2.0.0"), Map.of());
        ModMetadata helper = metadata("helpermod", "1.5.0", Map.of(), Map.of());

        LoaderException exception =
            assertThrows(
                LoaderException.class,
                () -> dependencyResolver.resolve(context(), List.of(candidate("mods/a.jar", "aaa", requester), candidate("mods/b.jar", "bbb", helper)))
            );

        assertTrue(exception.getMessage().contains("samplemod"));
        assertTrue(exception.getMessage().contains("helpermod"));
        assertTrue(exception.getMessage().contains(">=2.0.0"));
        assertTrue(exception.getMessage().contains("1.5.0"));
    }

    @Test
    void breaksMetadataParses() throws LoaderException {
        ModMetadata metadata =
            metadataParser.parse(
                """
                {
                  "schema": 1,
                  "id": "samplemod",
                  "version": "1.0.0",
                  "side": "universal",
                  "entrypoints": {
                    "main": ["com.example.Entrypoint"]
                  },
                  "depends": {},
                  "breaks": {
                    "othermod": ">=1.0.0"
                  }
                }
                """,
                "breaks"
            );
        ModMetadata noBreaks =
            metadataParser.parse(
                """
                {
                  "schema": 1,
                  "id": "samplemod",
                  "version": "1.0.0",
                  "side": "universal",
                  "entrypoints": {
                    "main": ["com.example.Entrypoint"]
                  },
                  "depends": {}
                }
                """,
                "breaks-empty"
            );

        assertEquals(">=1.0.0", metadata.breaks().get("othermod"));
        assertTrue(noBreaks.breaks().isEmpty());
    }

    @Test
    void breaksConflictFailsResolution() throws LoaderException {
        ModMetadata breaker = metadata("samplemod", "1.0.0", Map.of(), Map.of("othermod", ">=1.0.0"));
        ModMetadata other = metadata("othermod", "1.0.0", Map.of(), Map.of());

        LoaderException exception =
            assertThrows(
                LoaderException.class,
                () -> dependencyResolver.resolve(context(), List.of(candidate("mods/a.jar", "aaa", breaker), candidate("mods/b.jar", "bbb", other)))
            );

        assertTrue(exception.getMessage().contains("samplemod"));
        assertTrue(exception.getMessage().contains("othermod"));
        assertTrue(exception.getMessage().contains("1.0.0"));
        assertTrue(exception.getMessage().contains(">=1.0.0"));
    }

    @Test
    void unsupportedVersionRequirementIncludesContext() throws LoaderException {
        ModMetadata metadata = metadata("samplemod", "1.0.0", Map.of("helpermod", "[1.0,2.0)"), Map.of());

        LoaderException exception =
            assertThrows(LoaderException.class, () -> dependencyResolver.resolve(context(), List.of(candidate("mods/a.jar", "aaa", metadata))));

        assertTrue(exception.getMessage().contains("helpermod"));
        assertTrue(exception.getMessage().contains("[1.0,2.0)"));
        assertTrue(exception.getMessage().contains("samplemod"));
    }

    @Test
    void duplicateResourceProducesDiagnosticWarning() throws Exception {
        createStandardModJar(tempDirectory.resolve("mods/left.jar"), "leftmod", "com.example.Left");
        createModJar(
            tempDirectory.resolve("mods/right.jar"),
            metadataJson("rightmod", "com.example.Right", Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.2"), Map.of()),
            Map.of("com/example/Right.class", new byte[] {1}, "assets/shared.txt", "right".getBytes(StandardCharsets.UTF_8))
        );
        appendJarEntry(tempDirectory.resolve("mods/left.jar"), "assets/shared.txt", "left".getBytes(StandardCharsets.UTF_8));

        executeAndWrite(new String[] {"--game-main", ValidationGameMain.class.getName(), "--validate-only"});

        String diagnostics = Files.readString(tempDirectory.resolve("diagnostics/startup-trace.json"));
        String state = Files.readString(tempDirectory.resolve("modpack-state.json"));
        assertTrue(diagnostics.contains("\"name\": \"resource.duplicate\""));
        assertTrue(diagnostics.contains("assets/shared.txt"));
        assertTrue(state.contains("\"resourceConflicts\""));
        assertTrue(state.contains("assets/shared.txt"));
    }

    @Test
    void strictResourcesFailsOnDuplicateResource() throws Exception {
        createStandardModJar(tempDirectory.resolve("mods/left.jar"), "leftmod", "com.example.Left");
        createModJar(
            tempDirectory.resolve("mods/right.jar"),
            metadataJson("rightmod", "com.example.Right", Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.2"), Map.of()),
            Map.of("com/example/Right.class", new byte[] {1}, "assets/shared.txt", "right".getBytes(StandardCharsets.UTF_8))
        );
        appendJarEntry(tempDirectory.resolve("mods/left.jar"), "assets/shared.txt", "left".getBytes(StandardCharsets.UTF_8));

        LoaderException exception =
            assertThrows(
                LoaderException.class,
                () -> executeWithoutWrite(new String[] {"--game-main", ValidationGameMain.class.getName(), "--validate-only", "--strict-resources"})
            );

        assertTrue(exception.getMessage().contains("assets/shared.txt"));
        assertTrue(exception.getMessage().contains("leftmod"));
        assertTrue(exception.getMessage().contains("rightmod"));
    }

    @Test
    void splitPackageProducesDiagnosticWarning() throws Exception {
        createModJar(
            tempDirectory.resolve("mods/left.jar"),
            metadataJson("leftmod", "com.example.split.Left", Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.2"), Map.of()),
            Map.of("com/example/shared/Left.class", new byte[] {1})
        );
        createModJar(
            tempDirectory.resolve("mods/right.jar"),
            metadataJson("rightmod", "com.example.split.Right", Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.2"), Map.of()),
            Map.of("com/example/shared/Right.class", new byte[] {1})
        );

        executeAndWrite(new String[] {"--game-main", ValidationGameMain.class.getName(), "--validate-only"});

        String diagnostics = Files.readString(tempDirectory.resolve("diagnostics/startup-trace.json"));
        String state = Files.readString(tempDirectory.resolve("modpack-state.json"));
        assertTrue(diagnostics.contains("\"name\": \"package.split\""));
        assertTrue(diagnostics.contains("com.example.shared"));
        assertTrue(state.contains("\"splitPackages\""));
        assertTrue(state.contains("com.example.shared"));
    }

    @Test
    void strictPackagesFailsOnSplitPackage() throws Exception {
        createModJar(
            tempDirectory.resolve("mods/left.jar"),
            metadataJson("leftmod", "com.example.shared.Left", Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.2"), Map.of()),
            Map.of("com/example/shared/Left.class", new byte[] {1})
        );
        createModJar(
            tempDirectory.resolve("mods/right.jar"),
            metadataJson("rightmod", "com.example.shared.Right", Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.2"), Map.of()),
            Map.of("com/example/shared/Right.class", new byte[] {1})
        );

        LoaderException exception =
            assertThrows(
                LoaderException.class,
                () -> executeWithoutWrite(new String[] {"--game-main", ValidationGameMain.class.getName(), "--validate-only", "--strict-packages"})
            );

        assertTrue(exception.getMessage().contains("com.example.shared"));
        assertTrue(exception.getMessage().contains("leftmod"));
        assertTrue(exception.getMessage().contains("rightmod"));
    }

    @Test
    void startupProfileWritesSlowestEvents() throws Exception {
        createStandardModJar(tempDirectory.resolve("mods/sample-mod.jar"), "samplemod", GraphLoadSentinel.class.getName());

        executeAndWrite(new String[] {"--game-main", ValidationGameMain.class.getName(), "--validate-only"});

        String startupProfile = Files.readString(tempDirectory.resolve("diagnostics/startup-profile.json"));
        assertTrue(startupProfile.contains("\"totalDurationMs\""));
        assertTrue(startupProfile.contains("\"events\""));
        assertTrue(startupProfile.contains("\"slowestEvents\""));
    }

    @Test
    void packageOwnershipIndexTracksSplitPackagesDeterministically() throws Exception {
        ResolvedModSet resolvedModSet =
            new ResolvedModSet(
                List.of(
                    new ResolvedModSet.ResolvedMod(
                        "leftmod",
                        "1.0.0",
                        Path.of("mods/left.jar"),
                        createJarWithEntries(tempDirectory.resolve("left.jar"), Map.of("com/example/shared/Left.class", new byte[] {1})),
                        "aaa",
                        Map.of(),
                        Map.of(),
                        Map.of()
                    ),
                    new ResolvedModSet.ResolvedMod(
                        "rightmod",
                        "1.0.0",
                        Path.of("mods/right.jar"),
                        createJarWithEntries(tempDirectory.resolve("right.jar"), Map.of("com/example/shared/Right.class", new byte[] {1})),
                        "bbb",
                        Map.of(),
                        Map.of(),
                        Map.of()
                    )
                )
            );

        PackageOwnershipIndex index = PackageOwnershipIndex.build(resolvedModSet);
        assertEquals(List.of("leftmod", "rightmod"), index.packageOwners().get("com.example.shared"));
    }

    @Test
    void resourceConflictIndexTracksDuplicatesDeterministically() throws Exception {
        ResolvedModSet resolvedModSet =
            new ResolvedModSet(
                List.of(
                    new ResolvedModSet.ResolvedMod(
                        "leftmod",
                        "1.0.0",
                        Path.of("mods/left.jar"),
                        createJarWithEntries(tempDirectory.resolve("left.jar"), Map.of("assets/shared.txt", new byte[] {1})),
                        "aaa",
                        Map.of(),
                        Map.of(),
                        Map.of()
                    ),
                    new ResolvedModSet.ResolvedMod(
                        "rightmod",
                        "1.0.0",
                        Path.of("mods/right.jar"),
                        createJarWithEntries(tempDirectory.resolve("right.jar"), Map.of("assets/shared.txt", new byte[] {2})),
                        "bbb",
                        Map.of(),
                        Map.of(),
                        Map.of()
                    )
                )
            );

        ResourceConflictIndex index = ResourceConflictIndex.build(resolvedModSet);
        assertEquals("assets/shared.txt", index.conflicts().getFirst().resourcePath());
        assertEquals(List.of("leftmod", "rightmod"), index.conflicts().getFirst().modIds());
    }

    private LaunchContext context() {
        return new LaunchContext(
            tempDirectory,
            tempDirectory.resolve("mods"),
            ValidationGameMain.class.getName(),
            "sample",
            List.of(),
            false,
            false,
            false,
            false,
            LoaderMain.LOADER_VERSION,
            25,
            LoaderMain.TARGET_MINECRAFT_VERSION
        );
    }

    private ModMetadata metadata(String id, String version, Map<String, String> depends, Map<String, String> breaks) {
        return new ModMetadata(1, id, version, "universal", Map.of("main", List.of("com.example.Entrypoint")), depends, breaks);
    }

    private ModCandidate candidate(String relativePath, String sha256, ModMetadata metadata) {
        Path relative = Path.of(relativePath);
        return new ModCandidate(tempDirectory.resolve(relative.getFileName()), relative, sha256, metadata);
    }

    private String executeAndWrite(String[] args) throws Exception {
        JsonDiagnosticSink sink = new JsonDiagnosticSink(tempDirectory.resolve("diagnostics/startup-trace.json"));
        String output = captureStdout(() -> LoaderMain.execute(tempDirectory, args, sink));
        sink.write();
        return output;
    }

    private void executeWithoutWrite(String[] args) throws Exception {
        JsonDiagnosticSink sink = new JsonDiagnosticSink(tempDirectory.resolve("diagnostics/startup-trace.json"));
        LoaderMain.execute(tempDirectory, args, sink);
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
            metadataJson(modId, entrypointClassName, Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.2"), Map.of()),
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

    private Path createJarWithEntries(Path jarPath, Map<String, byte[]> entries) throws IOException {
        Files.createDirectories(jarPath.getParent());
        try (OutputStream outputStream = Files.newOutputStream(jarPath); JarOutputStream jarOutputStream = new JarOutputStream(outputStream)) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                jarOutputStream.putNextEntry(new JarEntry(entry.getKey()));
                jarOutputStream.write(entry.getValue());
                jarOutputStream.closeEntry();
            }
        }
        return jarPath;
    }

    private void appendJarEntry(Path jarPath, String entryName, byte[] bytes) throws IOException {
        Path rewritten = tempDirectory.resolve("rewritten-" + jarPath.getFileName());
        try (java.util.jar.JarFile jarFile = new java.util.jar.JarFile(jarPath.toFile());
            OutputStream outputStream = Files.newOutputStream(rewritten);
            JarOutputStream jarOutputStream = new JarOutputStream(outputStream)) {
            var entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                jarOutputStream.putNextEntry(new JarEntry(entry.getName()));
                jarFile.getInputStream(entry).transferTo(jarOutputStream);
                jarOutputStream.closeEntry();
            }
            jarOutputStream.putNextEntry(new JarEntry(entryName));
            jarOutputStream.write(bytes);
            jarOutputStream.closeEntry();
        }
        Files.move(rewritten, jarPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    private String metadataJson(
        String modId,
        String entrypointClassName,
        Map<String, String> depends,
        Map<String, String> breaks
    ) {
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
        builder.append("  \"breaks\": ").append(toJsonObject(breaks)).append("\n");
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

    public static final class GraphLoadSentinel {
        private static final AtomicBoolean INITIALIZED = new AtomicBoolean();

        static {
            INITIALIZED.set(true);
        }
    }

    public static final class ValidationEntrypoint implements com.mcmodloader.api.ModInitializer {
        private static final AtomicBoolean INITIALIZED = new AtomicBoolean();
        private static final AtomicBoolean INVOKED = new AtomicBoolean();

        static {
            INITIALIZED.set(true);
        }

        @Override
        public void onInitialize() {
            INVOKED.set(true);
        }
    }

    public static final class ValidationGameMain {
        private static final AtomicBoolean INVOKED = new AtomicBoolean();

        public static void main(String[] args) {
            INVOKED.set(true);
        }
    }
}
