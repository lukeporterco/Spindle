package com.spindle.core.runtime;

import com.spindle.core.runtime.capability.RuntimeCapabilityPlan;
import com.spindle.core.runtime.closure.RuntimeClosureContract;
import com.spindle.core.runtime.config.RuntimeConfigContract;
import com.spindle.core.runtime.service.RuntimeServiceContract;
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
    RuntimeCapabilityPlan permissions,
    RuntimeConfigContract config,
    RuntimeServiceContract services,
    RuntimeClosureContract runtimeClosure,
    Lifecycle lifecycle,
    Contexts contexts,
    PackagePolicy packagePolicy,
    Quality quality) {
  public static final int SCHEMA_VERSION = 6;
  public static final String PROFILE_KIND = "compiled-modpack";
  public static final String LOADER_ID = "spindle";

  public CompiledModpackProfile(
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
      RuntimeCapabilityPlan permissions,
      RuntimeServiceContract services,
      RuntimeClosureContract runtimeClosure,
      Lifecycle lifecycle,
      Contexts contexts,
      PackagePolicy packagePolicy,
      Quality quality) {
    this(
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
        lockfile,
        permissions,
        RuntimeConfigContract.empty(),
        services,
        runtimeClosure,
        lifecycle,
        contexts,
        packagePolicy,
        quality);
  }

  public CompiledModpackProfile(
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
      RuntimeCapabilityPlan permissions,
      RuntimeServiceContract services,
      Lifecycle lifecycle,
      Contexts contexts,
      PackagePolicy packagePolicy,
      Quality quality) {
    this(
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
        lockfile,
        permissions,
        RuntimeConfigContract.empty(),
        services,
        RuntimeClosureContract.empty(),
        lifecycle,
        contexts,
        packagePolicy,
        quality);
  }

  public CompiledModpackProfile(
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
      RuntimeCapabilityPlan permissions,
      RuntimeConfigContract config,
      RuntimeServiceContract services,
      Lifecycle lifecycle,
      Contexts contexts,
      PackagePolicy packagePolicy,
      Quality quality) {
    this(
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
        lockfile,
        permissions,
        config,
        services,
        RuntimeClosureContract.empty(),
        lifecycle,
        contexts,
        packagePolicy,
        quality);
  }

  public CompiledModpackProfile {
    cache = cache == null ? new Cache("miss", "profile not found") : cache;
    mods = List.copyOf(mods);
    resolvedOrder = List.copyOf(resolvedOrder);
    classpath = List.copyOf(classpath);
    config = config == null ? RuntimeConfigContract.empty() : config;
    services = services == null ? RuntimeServiceContract.empty() : services;
    runtimeClosure = runtimeClosure == null ? RuntimeClosureContract.empty() : runtimeClosure;
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
        config,
        services,
        runtimeClosure,
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
        config,
        services,
        runtimeClosure,
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
        config,
        services,
        runtimeClosure,
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
