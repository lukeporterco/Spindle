package com.spindle.core.minecraft;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class MinecraftVersionPlanningHardeningTest {
  @Test
  void minecraftVersionPlanningDoesNotThrowOnHugeNumericSegment() throws Exception {
    assertEquals(
        Integer.MAX_VALUE,
        invokeParsePart(new MinecraftModIntegrationPlanner(), "999999999999999999alpha"));
    assertEquals(
        Integer.MAX_VALUE,
        invokeParsePart(new MinecraftModExecutionPlanner(), "999999999999999999beta"));
  }

  private int invokeParsePart(Object target, String value) throws Exception {
    Method method = target.getClass().getDeclaredMethod("parsePart", String[].class, int.class);
    method.setAccessible(true);
    return (int) method.invoke(target, new String[] {value}, 0);
  }
}
