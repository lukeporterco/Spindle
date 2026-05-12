package com.spindle.core.minecraft.hook.transform;

public final class SteelHookFixtureClassLoader extends ClassLoader {
  private static final String TARGET_CLASS_NAME = "net.minecraft.server.Main";

  private final byte[] transformedMainClassBytes;

  public SteelHookFixtureClassLoader(ClassLoader parent, byte[] transformedMainClassBytes) {
    super(parent);
    this.transformedMainClassBytes = transformedMainClassBytes.clone();
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    synchronized (getClassLoadingLock(name)) {
      Class<?> loaded = findLoadedClass(name);
      if (loaded == null && TARGET_CLASS_NAME.equals(name)) {
        loaded =
            defineClass(
                TARGET_CLASS_NAME, transformedMainClassBytes, 0, transformedMainClassBytes.length);
      }
      if (loaded == null) {
        loaded = super.loadClass(name, false);
      }
      if (resolve) {
        resolveClass(loaded);
      }
      return loaded;
    }
  }
}
