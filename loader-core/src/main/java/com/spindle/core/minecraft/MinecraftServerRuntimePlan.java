package com.spindle.core.minecraft;

import java.util.List;

public record MinecraftServerRuntimePlan(
    int schema,
    String milestoneName,
    String projectJavaBaseline,
    String projectTargetMinecraft,
    String resolvedMinecraftVersion,
    String selectorUsed,
    String selectorResolutionReason,
    String manifestSource,
    String versionJsonSource,
    String serverJarPath,
    String serverJarSource,
    String serverJarSha1,
    String serverJarSha256,
    Long serverJarSize,
    String launchMode,
    String launchModeReason,
    String mainClass,
    List<MinecraftServerRuntimeClasspath.Entry> classpathEntries,
    List<MinecraftRuntimeFile> bundledRuntimeFiles,
    List<String> jvmArgs,
    List<String> serverArgs,
    String workingDirectory,
    String javaExecutable,
    List<String> commandPreview,
    String cacheDirectory,
    String runtimeCacheDirectory,
    boolean offline,
    boolean strict,
    int networkRequestCount,
    boolean generatedFromCacheOnly,
    boolean replayableOffline,
    boolean modJarsOnMinecraftRuntimeClasspath,
    boolean injectionOccurred,
    boolean minecraftModClassesLoaded,
    boolean minecraftModClassLoaderAttachedToMinecraft,
    boolean minecraftEntrypointsInvoked,
    boolean transformationsOccurred,
    boolean remappingOccurred,
    boolean mixinOccurred,
    boolean patchingOccurred,
    MinecraftRuntimeProvenance provenance) {
  public MinecraftServerRuntimePlan {
    classpathEntries = List.copyOf(classpathEntries);
    bundledRuntimeFiles = List.copyOf(bundledRuntimeFiles);
    jvmArgs = List.copyOf(jvmArgs);
    serverArgs = List.copyOf(serverArgs);
    commandPreview = List.copyOf(commandPreview);
  }
}
