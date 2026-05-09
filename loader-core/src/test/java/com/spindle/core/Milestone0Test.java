package com.spindle.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.classpath.ModClassLoader;
import com.spindle.core.classpath.RuntimeClasspathPlan;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.discovery.ModCandidate;
import com.spindle.core.entrypoint.EntrypointInvoker;
import com.spindle.core.game.SampleGameProvider;
import com.spindle.core.graph.FrozenModGraph;
import com.spindle.core.graph.FrozenModGraphBuilder;
import com.spindle.core.launch.LaunchContext;
import com.spindle.core.lockfile.LockfileVerifier;
import com.spindle.core.lockfile.LockfileWriter;
import com.spindle.core.metadata.ModMetadata;
import com.spindle.core.metadata.ModMetadataParser;
import com.spindle.core.ownership.ClassOwnershipIndex;
import com.spindle.core.ownership.PackageOwnershipIndex;
import com.spindle.core.resolve.DependencyResolver;
import com.spindle.core.resolve.ResolvedModSet;
import com.spindle.core.resource.ResourceConflictIndex;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Milestone0Test {
  private static final String SAMPLE_METADATA =
      """
        {
          "schema": 1,
          "id": "samplemod",
          "version": "1.0.0",
          "side": "universal",
          "entrypoints": {
            "main": [
              "com.spindle.samplemod.SampleMod"
            ]
          },
          "depends": {
            "loader": ">=0.1.0",
            "java": ">=25",
            "minecraft": ">=26.1.2"
          }
        }
        """;

  private final ModMetadataParser metadataParser = new ModMetadataParser();
  private final DependencyResolver dependencyResolver = new DependencyResolver();

  @TempDir Path tempDirectory;

  @Test
  void validMetadataParses() throws LoaderException {
    ModMetadata metadata = metadataParser.parse(SAMPLE_METADATA, "sample");

    assertEquals("samplemod", metadata.id());
    assertEquals("1.0.0", metadata.version());
    assertEquals("universal", metadata.side());
    assertEquals(">=25", metadata.depends().get("java"));
    assertEquals(">=26.1.2", metadata.depends().get("minecraft"));
  }

  @Test
  void schemaTwoMetadataParsesLifecycleAndStorage() throws LoaderException {
    ModMetadata metadata =
        metadataParser.parse(
            """
            {
              "schema": 2,
              "id": "samplemod",
              "version": "1.0.0",
              "side": "server",
              "depends": {
                "loader": ">=0.1.0",
                "java": ">=25",
                "minecraft": ">=26.1.2"
              },
              "breaks": {},
              "lifecycle": {
                "BOOTSTRAP": [
                  "com.example.SampleLifecycle::bootstrap"
                ],
                "CONFIGURE": [
                  "com.example.SampleLifecycle::configure"
                ]
              },
              "permissions": [],
              "storage": {
                "config": true,
                "data": true,
                "cache": false,
                "generated": true
              }
            }
            """,
            "schema-two");

    assertEquals(2, metadata.schema());
    assertEquals(
        List.of("com.example.SampleLifecycle::bootstrap"), metadata.lifecycle().get("BOOTSTRAP"));
    assertEquals(
        List.of("com.example.SampleLifecycle::configure"), metadata.lifecycle().get("CONFIGURE"));
    assertTrue(metadata.entrypoints().isEmpty());
    assertTrue(metadata.storage().config());
    assertTrue(metadata.storage().data());
    assertFalse(metadata.storage().cache());
    assertTrue(metadata.storage().generated());
  }

  @Test
  void invalidLifecycleDeclarationFailsDuringParse() {
    LoaderException exception =
        assertThrows(
            LoaderException.class,
            () ->
                metadataParser.parse(
                    """
                    {
                      "schema": 2,
                      "id": "samplemod",
                      "version": "1.0.0",
                      "side": "server",
                      "depends": {
                        "loader": ">=0.1.0",
                        "java": ">=25",
                        "minecraft": ">=26.1.2"
                      },
                      "lifecycle": {
                        "BOOTSTRAP": [
                          "com.example.SampleLifecycle.bootstrap"
                        ]
                      }
                    }
                    """,
                    "invalid-lifecycle"));

    assertTrue(exception.getMessage().contains("samplemod"));
    assertTrue(exception.getMessage().contains("BOOTSTRAP"));
    assertTrue(exception.getMessage().contains("ClassName::methodName"));
  }

  @Test
  void schemaOneLifecycleFieldFailsWithUpgradeGuidance() {
    LoaderException exception =
        assertThrows(
            LoaderException.class,
            () ->
                metadataParser.parse(
                    """
                    {
                      "schema": 1,
                      "id": "samplemod",
                      "version": "1.0.0",
                      "side": "server",
                      "entrypoints": {
                        "main": [
                          "com.example.SampleEntrypoint"
                        ]
                      },
                      "depends": {
                        "loader": ">=0.1.0",
                        "java": ">=25",
                        "minecraft": ">=26.1.2"
                      },
                      "lifecycle": {
                        "BOOTSTRAP": [
                          "com.example.SampleLifecycle::bootstrap"
                        ]
                      }
                    }
                    """,
                    "schema-one-lifecycle"));

    assertTrue(exception.getMessage().contains("samplemod"));
    assertTrue(exception.getMessage().contains("schema `1`"));
    assertTrue(exception.getMessage().contains("upgrade to schema `2`"));
  }

  @Test
  void invalidModIdFails() {
    String invalidMetadata = SAMPLE_METADATA.replace("\"samplemod\"", "\"BadId\"");

    assertThrows(LoaderException.class, () -> metadataParser.parse(invalidMetadata, "invalid"));
  }

  @Test
  void schemaMustBeExactInteger() {
    assertThrows(
        LoaderException.class,
        () ->
            metadataParser.parse(
                SAMPLE_METADATA.replace("\"schema\": 1", "\"schema\": 1.5"), "decimal-schema"));
    assertThrows(
        LoaderException.class,
        () ->
            metadataParser.parse(
                SAMPLE_METADATA.replace("\"schema\": 1", "\"schema\": \"1\""), "string-schema"));
    assertThrows(
        LoaderException.class,
        () ->
            metadataParser.parse(
                SAMPLE_METADATA.replace("\"schema\": 1", "\"schema\": null"), "null-schema"));
    assertThrows(
        LoaderException.class,
        () ->
            metadataParser.parse(
                SAMPLE_METADATA.replace("\"schema\": 1,\n", ""), "missing-schema"));
  }

  @Test
  void missingDependencyFails() throws LoaderException {
    ModMetadata metadata =
        metadata(
            "samplemod",
            "1.0.0",
            Map.of(
                "loader",
                ">=0.1.0",
                "java",
                ">=25",
                "minecraft",
                ">=26.1.2",
                "missinglib",
                ">=1.0.0"));

    LoaderException exception =
        assertThrows(
            LoaderException.class,
            () ->
                dependencyResolver.resolve(
                    context(), List.of(candidate("mods/sample-mod.jar", "aaa", metadata))));

    assertTrue(exception.getMessage().contains("missinglib"));
  }

  @Test
  void duplicateModIdFails() throws LoaderException {
    ModMetadata left = metadata("samplemod", "1.0.0", Map.of("loader", ">=0.1.0"));
    ModMetadata right = metadata("samplemod", "1.1.0", Map.of("loader", ">=0.1.0"));

    assertThrows(
        LoaderException.class,
        () ->
            dependencyResolver.resolve(
                context(),
                List.of(
                    candidate("mods/left.jar", "aaa", left),
                    candidate("mods/right.jar", "bbb", right))));
  }

  @Test
  void javaVersionRequirementFails() throws LoaderException {
    ModMetadata metadata =
        metadata(
            "samplemod",
            "1.0.0",
            Map.of("loader", ">=0.1.0", "java", ">=26", "minecraft", ">=26.1.2"));

    LoaderException exception =
        assertThrows(
            LoaderException.class,
            () ->
                dependencyResolver.resolve(
                    context(), List.of(candidate("mods/sample-mod.jar", "aaa", metadata))));

    assertTrue(exception.getMessage().contains("java"));
  }

  @Test
  void minecraftVersionRequirementFails() throws LoaderException {
    ModMetadata metadata =
        metadata(
            "samplemod",
            "1.0.0",
            Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.3"));

    LoaderException exception =
        assertThrows(
            LoaderException.class,
            () ->
                dependencyResolver.resolve(
                    context(), List.of(candidate("mods/sample-mod.jar", "aaa", metadata))));

    assertTrue(exception.getMessage().contains("minecraft"));
  }

  @Test
  void lockfileDetectsHashMismatch() throws LoaderException {
    ResolvedModSet resolvedModSet =
        new ResolvedModSet(
            List.of(
                resolvedMod(
                    "samplemod",
                    "1.0.0",
                    Path.of("mods/sample-mod.jar"),
                    tempDirectory.resolve("sample-mod.jar"),
                    "aaa",
                    List.of("com.example.Sample"))));
    Path lockfilePath = tempDirectory.resolve("spindle.lock.json");

    new LockfileWriter().write(lockfilePath, context(), resolvedModSet);

    ResolvedModSet changedResolvedModSet =
        new ResolvedModSet(
            List.of(
                resolvedMod(
                    "samplemod",
                    "1.0.0",
                    Path.of("mods/sample-mod.jar"),
                    tempDirectory.resolve("sample-mod.jar"),
                    "bbb",
                    List.of("com.example.Sample"))));

    LoaderException exception =
        assertThrows(
            LoaderException.class,
            () -> new LockfileVerifier().verify(lockfilePath, context(), changedResolvedModSet));

    assertTrue(exception.getMessage().contains("sha256"));
  }

  @Test
  void lockfileNullModsFailsWithLoaderException() throws IOException {
    assertMalformedLockfileFails(
        """
            {
              "schema": 1,
              "loader": "0.1.0",
              "java": 25,
              "minecraft": "26.1.2",
              "mods": null
            }
            """);
  }

  @Test
  void lockfileNullLoaderFailsWithLoaderException() throws IOException {
    assertMalformedLockfileFails(
        """
            {
              "schema": 1,
              "loader": null,
              "java": 25,
              "minecraft": "26.1.2",
              "mods": []
            }
            """);
  }

  @Test
  void lockfileNullMinecraftFailsWithLoaderException() throws IOException {
    assertMalformedLockfileFails(
        """
            {
              "schema": 1,
              "loader": "0.1.0",
              "java": 25,
              "minecraft": null,
              "mods": []
            }
            """);
  }

  @Test
  void lockfileNullModFieldsFailWithLoaderException() throws IOException {
    assertMalformedLockfileFails(
        """
            {
              "schema": 1,
              "loader": "0.1.0",
              "java": 25,
              "minecraft": "26.1.2",
              "mods": [
                {
                  "id": null,
                  "version": "1.0.0",
                  "path": "mods/sample-mod.jar",
                  "sha256": "aaa"
                }
              ]
            }
            """);

    assertMalformedLockfileFails(
        """
            {
              "schema": 1,
              "loader": "0.1.0",
              "java": 25,
              "minecraft": "26.1.2",
              "mods": [
                {
                  "id": "samplemod",
                  "version": null,
                  "path": "mods/sample-mod.jar",
                  "sha256": "aaa"
                }
              ]
            }
            """);

    assertMalformedLockfileFails(
        """
            {
              "schema": 1,
              "loader": "0.1.0",
              "java": 25,
              "minecraft": "26.1.2",
              "mods": [
                {
                  "id": "samplemod",
                  "version": "1.0.0",
                  "path": null,
                  "sha256": "aaa"
                }
              ]
            }
            """);

    assertMalformedLockfileFails(
        """
            {
              "schema": 1,
              "loader": "0.1.0",
              "java": 25,
              "minecraft": "26.1.2",
              "mods": [
                {
                  "id": "samplemod",
                  "version": "1.0.0",
                  "path": "mods/sample-mod.jar",
                  "sha256": null
                }
              ]
            }
            """);
  }

  @Test
  void entrypointMustImplementModInitializer() throws IOException, LoaderException {
    Path jarPath = createEmptyJar(tempDirectory.resolve("sample-mod.jar"));
    ResolvedModSet resolvedModSet =
        new ResolvedModSet(
            List.of(
                new ResolvedModSet.ResolvedMod(
                    "samplemod",
                    "1.0.0",
                    Path.of("mods/sample-mod.jar"),
                    jarPath,
                    "aaa",
                    Map.of("main", List.of(PlainEntrypoint.class.getName())),
                    Map.of(),
                    Map.of())));

    RuntimeClasspathPlan classpathPlan =
        new RuntimeClasspathPlan(List.of(jarPath), List.of(), List.of());
    FrozenModGraph frozenModGraph =
        new FrozenModGraphBuilder()
            .build(
                context(),
                new SampleGameProvider(LoaderMain.TARGET_MINECRAFT_VERSION),
                resolvedModSet,
                classpathPlan,
                ClassOwnershipIndex.build(resolvedModSet),
                PackageOwnershipIndex.build(resolvedModSet),
                ResourceConflictIndex.build(resolvedModSet));

    try (ModClassLoader classLoader =
        ModClassLoader.create(classpathPlan, getClass().getClassLoader())) {
      LoaderException exception =
          assertThrows(
              LoaderException.class,
              () ->
                  new EntrypointInvoker()
                      .invoke(
                          frozenModGraph, classLoader, ClassOwnershipIndex.build(resolvedModSet)));

      assertTrue(exception.getMessage().contains("ModInitializer"));
    }
  }

  private LaunchContext context() {
    return new LaunchContext(
        tempDirectory,
        tempDirectory.resolve("mods"),
        "com.example.Game",
        "sample",
        List.of(),
        false,
        false,
        false,
        false,
        "0.1.0",
        25,
        "26.1.2");
  }

  private ModMetadata metadata(String id, String version, Map<String, String> depends) {
    return new ModMetadata(
        1,
        id,
        version,
        "universal",
        Map.of("main", List.of("com.example.Entrypoint")),
        depends,
        Map.of());
  }

  private ModCandidate candidate(String relativePath, String sha256, ModMetadata metadata) {
    Path relative = Path.of(relativePath);
    return new ModCandidate(
        tempDirectory.resolve(relative.getFileName()), relative, sha256, metadata);
  }

  private Path createEmptyJar(Path jarPath) throws IOException {
    try (OutputStream outputStream = Files.newOutputStream(jarPath);
        JarOutputStream jarOutputStream = new JarOutputStream(outputStream)) {
      jarOutputStream.putNextEntry(new JarEntry("placeholder.txt"));
      jarOutputStream.write(new byte[] {0});
      jarOutputStream.closeEntry();
    }
    return jarPath;
  }

  private void assertMalformedLockfileFails(String lockfileJson) throws IOException {
    Path lockfilePath = tempDirectory.resolve("spindle.lock.json");
    Files.writeString(lockfilePath, lockfileJson);

    ResolvedModSet resolvedModSet =
        new ResolvedModSet(
            List.of(
                new ResolvedModSet.ResolvedMod(
                    "samplemod",
                    "1.0.0",
                    Path.of("mods/sample-mod.jar"),
                    tempDirectory.resolve("sample-mod.jar"),
                    "aaa",
                    Map.of("main", List.of("com.example.Sample")),
                    Map.of(),
                    Map.of())));

    assertThrows(
        LoaderException.class,
        () -> new LockfileVerifier().verify(lockfilePath, context(), resolvedModSet));
  }

  public static final class PlainEntrypoint {
    public PlainEntrypoint() {}
  }

  private ResolvedModSet.ResolvedMod resolvedMod(
      String id,
      String version,
      Path relativePath,
      Path jarPath,
      String sha256,
      List<String> entrypoints) {
    return new ResolvedModSet.ResolvedMod(
        id,
        version,
        relativePath,
        jarPath,
        sha256,
        Map.of("main", entrypoints),
        Map.of(),
        Map.of());
  }
}
