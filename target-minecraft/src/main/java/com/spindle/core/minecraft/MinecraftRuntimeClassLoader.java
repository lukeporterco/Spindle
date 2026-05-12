package com.spindle.core.minecraft;

import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationResult;
import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationStatus;
import com.spindle.core.minecraft.hook.bootstrap.MinecraftRuntimeClassTransformer;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;

public final class MinecraftRuntimeClassLoader extends URLClassLoader {
  private final String loaderId;
  private final MinecraftClassLoadingAudit audit;
  private final MinecraftRuntimeClassTransformer transformer;

  public MinecraftRuntimeClassLoader(
      String loaderId, URL[] urls, ClassLoader parent, MinecraftClassLoadingAudit audit) {
    this(loaderId, urls, parent, audit, null);
  }

  public MinecraftRuntimeClassLoader(
      String loaderId,
      URL[] urls,
      ClassLoader parent,
      MinecraftClassLoadingAudit audit,
      MinecraftRuntimeClassTransformer transformer) {
    super(urls, parent);
    this.loaderId = loaderId;
    this.audit = audit;
    this.transformer = transformer;
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    synchronized (getClassLoadingLock(name)) {
      audit.recordAttempt(loaderId);
      if (transformer != null && transformer.shouldTransform(name)) {
        Class<?> transformedClass = loadTransformedClass(name, resolve);
        if (transformedClass != null) {
          return transformedClass;
        }
      }
      Class<?> loadedClass = super.loadClass(name, resolve);
      if (loadedClass.getClassLoader() == this) {
        audit.recordDefined(loaderId);
      }
      return loadedClass;
    }
  }

  private Class<?> loadTransformedClass(String name, boolean resolve)
      throws ClassNotFoundException {
    Class<?> loadedClass = findLoadedClass(name);
    if (loadedClass == null) {
      byte[] originalClassBytes = readOwnClassBytes(name);
      MinecraftBootstrapHookTransformationResult result =
          transformer.transform(name, originalClassBytes);
      if (result.status() != MinecraftBootstrapHookTransformationStatus.TRANSFORMED
          || result.transformedClassBytes() == null) {
        throw new ClassNotFoundException(
            result.failureReason() == null
                ? "Minecraft bootstrap class transformation failed for " + name
                : result.failureReason());
      }
      try {
        byte[] transformedClassBytes = result.transformedClassBytes();
        loadedClass = defineClass(name, transformedClassBytes, 0, transformedClassBytes.length);
        audit.recordDefined(loaderId);
      } catch (LinkageError error) {
        throw new ClassNotFoundException(
            "Failed to define transformed Minecraft bootstrap class " + name, error);
      }
    }
    if (resolve) {
      resolveClass(loadedClass);
    }
    return loadedClass;
  }

  private byte[] readOwnClassBytes(String binaryName) throws ClassNotFoundException {
    String resourceName = binaryName.replace('.', '/') + ".class";
    URL resource = findResource(resourceName);
    if (resource == null) {
      return null;
    }
    try {
      URLConnection connection = resource.openConnection();
      connection.setUseCaches(false);
      try (InputStream inputStream = connection.getInputStream()) {
        return inputStream.readAllBytes();
      }
    } catch (IOException exception) {
      throw new ClassNotFoundException(
          "Failed to read runtime class bytes for " + binaryName, exception);
    }
  }
}
