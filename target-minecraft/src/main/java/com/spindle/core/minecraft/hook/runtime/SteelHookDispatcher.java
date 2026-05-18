package com.spindle.core.minecraft.hook.runtime;

import java.util.concurrent.atomic.AtomicInteger;

public final class SteelHookDispatcher {
  private static final AtomicInteger BEFORE_MAIN_INVOCATION_COUNT = new AtomicInteger();
  private static final AtomicInteger AFTER_MAIN_INVOCATION_COUNT = new AtomicInteger();

  private SteelHookDispatcher() {}

  public static void beforeMinecraftServerMain() {
    BEFORE_MAIN_INVOCATION_COUNT.incrementAndGet();
  }

  public static void afterMinecraftServerMain() {
    AFTER_MAIN_INVOCATION_COUNT.incrementAndGet();
  }

  public static int beforeMinecraftServerMainInvocationCount() {
    return BEFORE_MAIN_INVOCATION_COUNT.get();
  }

  public static int afterMinecraftServerMainInvocationCount() {
    return AFTER_MAIN_INVOCATION_COUNT.get();
  }

  public static void resetForBootstrap() {
    BEFORE_MAIN_INVOCATION_COUNT.set(0);
    AFTER_MAIN_INVOCATION_COUNT.set(0);
  }
}
