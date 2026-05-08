package com.mcmodloader.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mcmodloader.api.ModInitializer;
import com.mcmodloader.core.classpath.RuntimeClasspathPlan;
import com.mcmodloader.core.classpath.RuntimeClasspathPlanner;
import com.mcmodloader.core.diagnostics.JsonDiagnosticSink;
import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.game.GameProvider;
import com.mcmodloader.core.game.GameProviderResolver;
import com.mcmodloader.core.game.SampleGameProvider;
import com.mcmodloader.core.launch.LaunchContext;
import com.mcmodloader.core.ownership.ClassOwnershipIndex;
import com.mcmodloader.core.resolve.ResolvedModSet;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Milestone1LaunchArchitectureTest {
    private static final String SAMPLE_METADATA = """
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
        """;

    @TempDir
    Path tempDirectory;

    @Test
    void gameProviderResolverDefaultsToSample() throws LoaderException {
        LoaderMain.LaunchArguments arguments = LoaderMain.parseArguments(new String[] {"--game-main", "com.example.Game"});

        assertEquals("sample", arguments.gameProviderId());

        GameProvider provider =
            new GameProviderResolver().resolve(
                new LaunchContext(
                    tempDirectory,
                    tempDirectory.resolve("mods"),
                    arguments.gameMainClass(),
                    arguments.gameProviderId(),
                    arguments.launchArguments(),
                    false,
                    false,
                    false,
                    false,
                    LoaderMain.LOADER_VERSION,
                    25,
                    LoaderMain.TARGET_MINECRAFT_VERSION
                )
            );

        assertInstanceOf(SampleGameProvider.class, provider);
        assertEquals("sample", provider.id());
    }

    @Test
    void gameProviderResolverRejectsUnknownProvider() {
        LoaderException exception =
            assertThrows(
                LoaderException.class,
                () -> new GameProviderResolver().resolve(
                    new LaunchContext(
                        tempDirectory,
                        tempDirectory.resolve("mods"),
                        "com.example.Game",
                        "unknown",
                        List.of(),
                        false,
                        false,
                        false,
                        false,
                        LoaderMain.LOADER_VERSION,
                        25,
                        LoaderMain.TARGET_MINECRAFT_VERSION
                    )
                )
            );

        assertEquals("Unknown game provider: unknown", exception.getMessage());
    }

    @Test
    void sampleGameProviderValidatesGameMain() {
        SampleGameProvider provider = new SampleGameProvider(LoaderMain.TARGET_MINECRAFT_VERSION);

        assertThrows(
            LoaderException.class,
            () -> provider.validate(
                new LaunchContext(
                    tempDirectory,
                    tempDirectory.resolve("mods"),
                    " ",
                    "sample",
                    List.of(),
                    false,
                    false,
                    false,
                    false,
                    LoaderMain.LOADER_VERSION,
                    25,
                    LoaderMain.TARGET_MINECRAFT_VERSION
                )
            )
        );
        assertThrows(
            LoaderException.class,
            () -> provider.validate(
                new LaunchContext(
                    tempDirectory,
                    tempDirectory.resolve("mods"),
                    null,
                    "sample",
                    List.of(),
                    false,
                    false,
                    false,
                    false,
                    LoaderMain.LOADER_VERSION,
                    25,
                    LoaderMain.TARGET_MINECRAFT_VERSION
                )
            )
        );
    }

    @Test
    void runtimeClasspathPlanContainsOnlyResolvedModJars() throws IOException, LoaderException {
        Path resolvedJar = createJarWithEntries(tempDirectory.resolve("resolved.jar"), Map.of("placeholder.txt", new byte[] {1}));
        Path unresolvedJar = createJarWithEntries(tempDirectory.resolve("unresolved.jar"), Map.of("placeholder.txt", new byte[] {2}));
        ResolvedModSet resolvedModSet =
            new ResolvedModSet(
                List.of(new ResolvedModSet.ResolvedMod("samplemod", "1.0.0", Path.of("mods/resolved.jar"), resolvedJar, "aaa", Map.of(), Map.of(), Map.of()))
            );

        RuntimeClasspathPlan plan =
            new RuntimeClasspathPlanner().plan(
                new LaunchContext(
                    tempDirectory,
                    tempDirectory.resolve("mods"),
                    "com.example.Game",
                    "sample",
                    List.of(),
                    false,
                    false,
                    false,
                    false,
                    LoaderMain.LOADER_VERSION,
                    25,
                    LoaderMain.TARGET_MINECRAFT_VERSION
                ),
                resolvedModSet
            );

        assertEquals(List.of(resolvedJar.toAbsolutePath().normalize()), plan.modJars());
        assertFalse(plan.modJars().contains(unresolvedJar.toAbsolutePath().normalize()));
    }

    @Test
    void classOwnershipIndexMapsClassToModId() throws IOException, LoaderException {
        Path jarPath =
            createJarWithEntries(
                tempDirectory.resolve("owner.jar"),
                Map.of("com/example/Foo.class", new byte[] {1, 2, 3}, "module-info.class", new byte[] {4})
            );
        ResolvedModSet resolvedModSet =
            new ResolvedModSet(
                List.of(new ResolvedModSet.ResolvedMod("samplemod", "1.0.0", Path.of("mods/owner.jar"), jarPath, "aaa", Map.of(), Map.of(), Map.of()))
            );

        ClassOwnershipIndex index = ClassOwnershipIndex.build(resolvedModSet);

        assertEquals("samplemod", index.ownerOfClass("com.example.Foo").orElseThrow());
        assertFalse(index.ownerOfClass("module-info").isPresent());
    }

    @Test
    void classOwnershipIndexRejectsDuplicateClass() throws IOException {
        Path leftJar = createJarWithEntries(tempDirectory.resolve("left.jar"), Map.of("com/example/Foo.class", new byte[] {1}));
        Path rightJar = createJarWithEntries(tempDirectory.resolve("right.jar"), Map.of("com/example/Foo.class", new byte[] {2}));
        ResolvedModSet resolvedModSet =
            new ResolvedModSet(
                List.of(
                    new ResolvedModSet.ResolvedMod("leftmod", "1.0.0", Path.of("mods/left.jar"), leftJar, "aaa", Map.of(), Map.of(), Map.of()),
                    new ResolvedModSet.ResolvedMod("rightmod", "1.0.0", Path.of("mods/right.jar"), rightJar, "bbb", Map.of(), Map.of(), Map.of())
                )
            );

        LoaderException exception = assertThrows(LoaderException.class, () -> ClassOwnershipIndex.build(resolvedModSet));

        assertTrue(exception.getMessage().contains("com.example.Foo"));
        assertTrue(exception.getMessage().contains("leftmod"));
        assertTrue(exception.getMessage().contains("rightmod"));
    }

    @Test
    void diagnosticsContainLaunchPhases() throws Exception {
        Path modsDirectory = Files.createDirectories(tempDirectory.resolve("mods"));
        Files.createDirectories(tempDirectory.resolve("diagnostics"));
        Path modJar =
            createModJar(
                modsDirectory.resolve("sample-mod.jar"),
                "samplemod",
                TestEntrypoint.class.getName(),
                TestEntrypoint.class.getName().replace('.', '/') + ".class"
            );
        JsonDiagnosticSink sink = new JsonDiagnosticSink(tempDirectory.resolve("diagnostics/startup-trace.json"));

        TestEntrypoint.INVOKED.set(false);
        FakeGameMain.INVOKED.set(false);

        LoaderMain.execute(tempDirectory, new String[] {"--game-main", FakeGameMain.class.getName()}, sink);
        sink.write();

        String diagnostics = Files.readString(tempDirectory.resolve("diagnostics/startup-trace.json"));

        assertTrue(TestEntrypoint.INVOKED.get());
        assertTrue(FakeGameMain.INVOKED.get());
        assertTrue(Files.exists(tempDirectory.resolve("loader.lock.json")));
        assertTrue(Files.exists(modJar));
        assertTrue(diagnostics.contains("\"name\": \"argument.parse\""));
        assertTrue(diagnostics.contains("\"name\": \"game_provider.resolve\""));
        assertTrue(diagnostics.contains("\"name\": \"mod.discovery\""));
        assertTrue(diagnostics.contains("\"name\": \"metadata.parse\""));
        assertTrue(diagnostics.contains("\"name\": \"dependency.resolution\""));
        assertTrue(diagnostics.contains("\"name\": \"lockfile.verify_or_write\""));
        assertTrue(diagnostics.contains("\"name\": \"classpath.plan\""));
        assertTrue(diagnostics.contains("\"name\": \"frozen_mod_graph.create\""));
        assertTrue(diagnostics.contains("\"name\": \"classpath.create\""));
        assertTrue(diagnostics.contains("\"name\": \"ownership.index\""));
        assertTrue(diagnostics.contains("\"name\": \"package.index\""));
        assertTrue(diagnostics.contains("\"name\": \"resource.index\""));
        assertTrue(diagnostics.contains("\"name\": \"modpack_state.write\""));
        assertTrue(diagnostics.contains("\"name\": \"dependency_graph.write\""));
        assertTrue(diagnostics.contains("\"name\": \"startup_profile.write\""));
        assertTrue(diagnostics.contains("\"name\": \"entrypoint.invoke\""));
        assertTrue(diagnostics.contains("\"name\": \"game.launch\""));
        assertTrue(diagnostics.contains("\"phase\": \"ARGUMENT_PARSE\""));
        assertTrue(diagnostics.contains("\"phase\": \"GAME_PROVIDER_RESOLVE\""));
        assertTrue(diagnostics.contains("\"phase\": \"MOD_DISCOVERY\""));
        assertTrue(diagnostics.contains("\"phase\": \"METADATA_PARSE\""));
        assertTrue(diagnostics.contains("\"phase\": \"DEPENDENCY_RESOLUTION\""));
        assertTrue(diagnostics.contains("\"phase\": \"LOCKFILE\""));
        assertTrue(diagnostics.contains("\"phase\": \"CLASSPATH_PLAN\""));
        assertTrue(diagnostics.contains("\"phase\": \"CLASSLOADER_CREATE\""));
        assertTrue(diagnostics.contains("\"phase\": \"ENTRYPOINT_INVOKE\""));
        assertTrue(diagnostics.contains("\"phase\": \"GAME_LAUNCH\""));
    }

    private Path createModJar(Path jarPath, String modId, String entrypointClassName, String entrypointClassFile) throws IOException {
        return createJarWithEntries(
            jarPath,
            Map.of(
                "loader.mod.json",
                SAMPLE_METADATA.formatted(modId, entrypointClassName).getBytes(),
                entrypointClassFile,
                new byte[] {1, 2, 3}
            )
        );
    }

    private Path createJarWithEntries(Path jarPath, Map<String, byte[]> entries) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(jarPath); JarOutputStream jarOutputStream = new JarOutputStream(outputStream)) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                jarOutputStream.putNextEntry(new JarEntry(entry.getKey()));
                jarOutputStream.write(entry.getValue());
                jarOutputStream.closeEntry();
            }
        }
        return jarPath;
    }

    public static final class TestEntrypoint implements ModInitializer {
        private static final AtomicBoolean INVOKED = new AtomicBoolean();

        @Override
        public void onInitialize() {
            INVOKED.set(true);
        }
    }

    public static final class FakeGameMain {
        private static final AtomicBoolean INVOKED = new AtomicBoolean();

        public static void main(String[] args) {
            INVOKED.set(true);
        }
    }
}
