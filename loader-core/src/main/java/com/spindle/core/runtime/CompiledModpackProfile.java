package com.spindle.core.runtime;

import java.util.List;

public record CompiledModpackProfile(
    int schemaVersion,
    String profileKind,
    String fingerprint,
    String inputFingerprint,
    String runtimePolicyFingerprint,
    Cache cache,
    Loader loader,
    Game game,
    Metadata metadata,
    List<Mod> mods,
    List<String> resolvedOrder,
    List<ClasspathEntry> classpath,
    Ownership ownership,
    Lockfile lockfile,
    Permissions permissions,
    Lifecycle lifecycle,
    Contexts contexts,
    PackagePolicy packagePolicy,
    Quality quality) {
  public static final int SCHEMA_VERSION = 2;
  public static final String PROFILE_KIND = "compiled-modpack";
  public static final String LOADER_ID = "spindle";

  public CompiledModpackProfile {
    cache = cache == null ? new Cache("miss", "profile not found") : cache;
    mods = List.copyOf(mods);
    resolvedOrder = List.copyOf(resolvedOrder);
    classpath = List.copyOf(classpath);
  }

  public CompiledModpackProfile withFingerprint(String nextFingerprint) {
    return new CompiledModpackProfile(
        schemaVersion,
        profileKind,
        nextFingerprint,
        inputFingerprint,
        runtimePolicyFingerprint,
        cache,
        loader,
        game,
        metadata,
        mods,
        resolvedOrder,
        classpath,
        ownership,
        lockfile,
        permissions,
        lifecycle,
        contexts,
        packagePolicy,
        quality);
  }

  public CompiledModpackProfile withCache(Cache nextCache) {
    return new CompiledModpackProfile(
        schemaVersion,
        profileKind,
        fingerprint,
        inputFingerprint,
        runtimePolicyFingerprint,
        nextCache,
        loader,
        game,
        metadata,
        mods,
        resolvedOrder,
        classpath,
        ownership,
        lockfile,
        permissions,
        lifecycle,
        contexts,
        packagePolicy,
        quality);
  }

  public CompiledModpackProfile withLockfile(Lockfile nextLockfile) {
    return new CompiledModpackProfile(
        schemaVersion,
        profileKind,
        fingerprint,
        inputFingerprint,
        runtimePolicyFingerprint,
        cache,
        loader,
        game,
        metadata,
        mods,
        resolvedOrder,
        classpath,
        ownership,
        nextLockfile,
        permissions,
        lifecycle,
        contexts,
        packagePolicy,
        quality);
  }

  public record Cache(String status, String reason) {}

  public record Loader(String id, String version) {}

  public record Game(String id, String version, String side) {}

  public record Metadata(List<Integer> schemaVersions) {
    public Metadata {
      schemaVersions = List.copyOf(schemaVersions);
    }
  }

  public record Mod(String id, String version, String path, String hash) {}

  public record ClasspathEntry(String path, String owner) {}

  public record Ownership(Count classes, Count packages, Resources resources) {}

  public record Count(int count) {}

  public record Resources(int duplicates) {}

  public record Lockfile(String mode, String action, String path, String fingerprint) {}

  public record Permissions(List<ModPermissions> mods) {
    public Permissions {
      mods = List.copyOf(mods);
    }
  }

  public record ModPermissions(String modId, List<String> requested) {
    public ModPermissions {
      requested = List.copyOf(requested);
    }
  }

  public record Lifecycle(List<String> phaseOrder, List<LifecycleHandler> handlers) {
    public Lifecycle {
      phaseOrder = List.copyOf(phaseOrder);
      handlers = List.copyOf(handlers);
    }
  }

  public record LifecycleHandler(
      String phase,
      String modId,
      String ownerModId,
      String kind,
      String className,
      String methodName,
      String interfaceName,
      String jarPath,
      String jarHash) {}

  public record Contexts(List<ModContextPlan> mods) {
    public Contexts {
      mods = List.copyOf(mods);
    }
  }

  public record ModContextPlan(
      String modId,
      Storage storage,
      String configDirectory,
      String dataDirectory,
      String cacheDirectory,
      String generatedDirectory) {}

  public record Storage(boolean config, boolean data, boolean cache, boolean generated) {}

  public record PackagePolicy(
      List<String> protectedPackages,
      List<SplitPackage> splitPackages,
      List<String> duplicateClasses,
      List<PackageOwner> packageOwners,
      List<ProtectedPackageViolation> fatalViolations) {
    public PackagePolicy {
      protectedPackages = List.copyOf(protectedPackages);
      splitPackages = List.copyOf(splitPackages);
      duplicateClasses = List.copyOf(duplicateClasses);
      packageOwners = List.copyOf(packageOwners);
      fatalViolations = List.copyOf(fatalViolations);
    }
  }

  public record SplitPackage(String packageName, List<String> modIds) {
    public SplitPackage {
      modIds = List.copyOf(modIds);
    }
  }

  public record PackageOwner(String packageName, List<String> modIds) {
    public PackageOwner {
      modIds = List.copyOf(modIds);
    }
  }

  public record Quality(int score, int fatalCount, int warningCount) {}
}
