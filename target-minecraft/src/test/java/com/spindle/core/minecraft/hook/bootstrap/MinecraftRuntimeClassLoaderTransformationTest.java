package com.spindle.core.minecraft.hook.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.spindle.core.minecraft.MinecraftClassLoadingAudit;
import com.spindle.core.minecraft.MinecraftRuntimeClassLoader;
import com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinecraftRuntimeClassLoaderTransformationTest {
  @TempDir Path tempDirectory;

  @Test
  void runtimeClassLoaderTransformsExactTargetAndInvokesDispatcher() throws Exception {
    Path runtimeJar = createRuntimeJar(tempDirectory.resolve("fake-server.jar"));
    MinecraftClassLoadingAudit audit = new MinecraftClassLoadingAudit();
    SteelHookDispatcher.resetForBootstrap();
    MinecraftBootstrapHookTransformer transformer =
        new MinecraftBootstrapHookTransformer(
            MinecraftBootstrapHookTransformerTest.validPatchPlan());

    try (MinecraftRuntimeClassLoader classLoader =
        new MinecraftRuntimeClassLoader(
            "minecraft-runtime",
            new URL[] {runtimeJar.toUri().toURL()},
            getClass().getClassLoader(),
            audit,
            transformer)) {
      Class<?> mainClass = Class.forName("net.minecraft.server.Main", true, classLoader);
      Method mainMethod = mainClass.getMethod("main", String[].class);
      mainMethod.invoke(null, (Object) new String[0]);

      assertEquals(1, SteelHookDispatcher.beforeMinecraftServerMainInvocationCount());
      assertEquals(
          MinecraftBootstrapHookTransformationStatus.TRANSFORMED,
          transformer.currentResult().status());
      assertEquals(
          1, audit.summary().definedClassLoadsByLoader().getOrDefault("minecraft-runtime", 0));
    }
  }

  @Test
  void runtimeClassLoaderKeepsParentFirstBehaviorForNonTargetClasses() throws Exception {
    Path runtimeJar = createRuntimeJar(tempDirectory.resolve("fake-server.jar"));
    MinecraftClassLoadingAudit audit = new MinecraftClassLoadingAudit();
    SteelHookDispatcher.resetForBootstrap();
    MinecraftBootstrapHookTransformer transformer =
        new MinecraftBootstrapHookTransformer(
            MinecraftBootstrapHookTransformerTest.validPatchPlan());

    try (MinecraftRuntimeClassLoader classLoader =
        new MinecraftRuntimeClassLoader(
            "minecraft-runtime",
            new URL[] {runtimeJar.toUri().toURL()},
            getClass().getClassLoader(),
            audit,
            transformer)) {
      Class<?> loaded = Class.forName(getClass().getName(), true, classLoader);

      assertSame(getClass(), loaded);
      assertFalse(audit.summary().definedClassLoadsByLoader().containsKey("minecraft-runtime"));
      assertEquals(0, SteelHookDispatcher.beforeMinecraftServerMainInvocationCount());
    }
  }

  @Test
  void dispatcherClassResolvesFromParentClassLoader() throws Exception {
    Path runtimeJar = createRuntimeJar(tempDirectory.resolve("fake-server.jar"));
    MinecraftClassLoadingAudit audit = new MinecraftClassLoadingAudit();
    MinecraftBootstrapHookTransformer transformer =
        new MinecraftBootstrapHookTransformer(
            MinecraftBootstrapHookTransformerTest.validPatchPlan());

    try (MinecraftRuntimeClassLoader classLoader =
        new MinecraftRuntimeClassLoader(
            "minecraft-runtime",
            new URL[] {runtimeJar.toUri().toURL()},
            getClass().getClassLoader(),
            audit,
            transformer)) {
      Class<?> dispatcherClass =
          Class.forName(
              "com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher", true, classLoader);

      assertSame(SteelHookDispatcher.class, dispatcherClass);
      assertSame(getClass().getClassLoader(), dispatcherClass.getClassLoader());
    }
  }

  private Path createRuntimeJar(Path jarPath) throws IOException {
    Files.createDirectories(jarPath.getParent());
    try (OutputStream outputStream = Files.newOutputStream(jarPath);
        JarOutputStream jar = new JarOutputStream(outputStream)) {
      for (Map.Entry<String, byte[]> entry :
          Map.of(
                  "net/minecraft/server/Main.class",
                  MinecraftBootstrapHookTransformerTest.fixtureClassBytes(
                      "net/minecraft/server/Main", "([Ljava/lang/String;)V", true, false))
              .entrySet()) {
        jar.putNextEntry(new JarEntry(entry.getKey()));
        jar.write(entry.getValue());
        jar.closeEntry();
      }
    }
    return jarPath;
  }
}
