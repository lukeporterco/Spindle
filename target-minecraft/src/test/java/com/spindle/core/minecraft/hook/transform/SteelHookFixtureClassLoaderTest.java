package com.spindle.core.minecraft.hook.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class SteelHookFixtureClassLoaderTest {
  private final MinecraftFixtureBytecodeTransformer transformer =
      new MinecraftFixtureBytecodeTransformer();

  @Test
  void transformedFixtureClassLoadsAndInvokesDispatcherOnce() throws Exception {
    MinecraftFixtureTransformationResult result =
        transformer.transformFixtureClass(
            MinecraftFixtureBytecodeTransformerTest.fixtureClassBytes(
                "net/minecraft/server/Main", "([Ljava/lang/String;)V", true, false, false),
            MinecraftFixtureBytecodeTransformerTest.validPatchPlan());

    SteelHookDispatcher.reset();
    SteelHookFixtureClassLoader classLoader =
        new SteelHookFixtureClassLoader(
            SteelHookFixtureClassLoaderTest.class.getClassLoader(),
            result.transformedClass().classBytes());

    Class<?> mainClass = classLoader.loadClass("net.minecraft.server.Main");
    Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
    mainMethod.invoke(null, (Object) new String[0]);

    assertNotSame(
        SteelHookFixtureClassLoaderTest.class.getClassLoader(), mainClass.getClassLoader());
    assertEquals(1, SteelHookDispatcher.invocationCount());
  }
}
