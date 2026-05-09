package com.spindle.core.runtime.capability;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class RuntimeCapabilityCatalog {
  public static final int CATALOG_VERSION = 1;
  public static final String SCOPE = "spindle-api-only";
  public static final String RUNTIME_EXECUTION_ISOLATION_MODE = "in-process-unrestricted-java";
  public static final boolean SANDBOXED = false;

  public static final String STORAGE_CONFIG = "storage.config";
  public static final String STORAGE_DATA = "storage.data";
  public static final String STORAGE_CACHE = "storage.cache";
  public static final String STORAGE_GENERATED = "storage.generated";

  private static final Set<String> GRANTABLE_STORAGE =
      Set.of(STORAGE_CONFIG, STORAGE_DATA, STORAGE_CACHE, STORAGE_GENERATED);
  private static final Set<String> UNAVAILABLE =
      Set.of(
          "config.read",
          "config.write",
          "service.provide",
          "service.consume",
          "resource.declare",
          "resource.overlay");
  private static final Set<String> VISIBILITY_ONLY =
      Set.of(
          "filesystem.read",
          "filesystem.write",
          "network.connect",
          "network.outbound",
          "process.spawn",
          "native.load",
          "reflection.deep",
          "unsafe.access");

  private RuntimeCapabilityCatalog() {}

  public static boolean isGrantableStorage(String capability) {
    return GRANTABLE_STORAGE.contains(capability);
  }

  public static boolean isUnavailable(String capability) {
    return UNAVAILABLE.contains(capability);
  }

  public static boolean isVisibilityOnly(String capability) {
    return VISIBILITY_ONLY.contains(capability);
  }

  public static List<String> storageCapabilities() {
    return List.of(STORAGE_CACHE, STORAGE_CONFIG, STORAGE_DATA, STORAGE_GENERATED);
  }

  public static String storageSource(String capability) {
    return "metadata." + capability;
  }

  public static String storageFlag(String capability) {
    return capability;
  }

  public static String storageMethodName(String capability) {
    return switch (capability) {
      case STORAGE_CONFIG -> "configDirectory()";
      case STORAGE_DATA -> "dataDirectory()";
      case STORAGE_CACHE -> "cacheDirectory()";
      case STORAGE_GENERATED -> "generatedDirectory()";
      default -> throw new IllegalArgumentException("Unsupported storage capability " + capability);
    };
  }

  public static String storageControls(String capability) {
    return "Spindle ModContext " + storageMethodName(capability) + " access only.";
  }

  public static Comparator<String> sourceComparator() {
    return Comparator.comparingInt(RuntimeCapabilityCatalog::sourceRank).thenComparing(value -> value);
  }

  private static int sourceRank(String source) {
    if (source != null && source.startsWith("metadata.storage.")) {
      return 0;
    }
    if ("metadata.permissions".equals(source)) {
      return 1;
    }
    return 2;
  }
}
