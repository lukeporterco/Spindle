package com.spindle.core.runtime;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.lifecycle.LifecycleHandlerDeclaration;
import com.spindle.core.lifecycle.LifecyclePlan;
import com.spindle.core.launch.LaunchContext;
import com.spindle.core.pipeline.ModpackPlanningResult;
import com.spindle.core.quality.RuntimeQualityReport;
import com.spindle.core.report.DisplayPaths;
import com.spindle.core.resolve.ResolvedModSet;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public final class CompiledModpackProfileBuilder {
  private final RuntimeProtectedPackagePolicy protectedPackagePolicy =
      new RuntimeProtectedPackagePolicy();
  private final CompiledModpackProfileFingerprint fingerprint = new CompiledModpackProfileFingerprint();
  private final RuntimePolicyFingerprint runtimePolicyFingerprint = new RuntimePolicyFingerprint();

  public CompiledModpackProfile build(
      LaunchContext context,
      ModpackPlanningResult planningResult,
      String gameSide,
      String inputFingerprint,
      CompiledModpackProfile.Cache cache,
      LifecyclePlan lifecyclePlan,
      RuntimeQualityReport qualityReport)
      throws LoaderException {
    List<CompiledModpackProfile.Mod> mods =
        new ArrayList<>(planningResult.resolvedMods().mods().size());
    List<String> resolvedOrder = new ArrayList<>(planningResult.resolvedMods().mods().size());
    Map<Path, CompiledModpackProfile.ClasspathEntry> classpathEntriesByJar = new LinkedHashMap<>();
    List<CompiledModpackProfile.ModContextPlan> contextPlans =
        new ArrayList<>(planningResult.resolvedMods().mods().size());

    for (ResolvedModSet.ResolvedMod mod : planningResult.resolvedMods().mods()) {
      mods.add(
          new CompiledModpackProfile.Mod(
              mod.id(), mod.version(), mod.normalizedRelativePath(), mod.sha256()));
      resolvedOrder.add(mod.id());
      classpathEntriesByJar.put(
          mod.jarPath().toAbsolutePath().normalize(),
          new CompiledModpackProfile.ClasspathEntry(mod.normalizedRelativePath(), mod.id()));
      contextPlans.add(
          new CompiledModpackProfile.ModContextPlan(
              mod.id(),
              new CompiledModpackProfile.Storage(
                  mod.storage().config(),
                  mod.storage().data(),
                  mod.storage().cache(),
                  mod.storage().generated()),
              "config/" + mod.id(),
              "mod-data/" + mod.id(),
              "cache/mods/" + mod.id(),
              "generated/" + mod.id()));
    }

    List<CompiledModpackProfile.ClasspathEntry> classpath = new ArrayList<>();
    for (Path path : planningResult.classpathPlan().modJars()) {
      CompiledModpackProfile.ClasspathEntry entry =
          classpathEntriesByJar.get(path.toAbsolutePath().normalize());
      if (entry != null) {
        classpath.add(entry);
      }
    }

    CompiledModpackProfile.Lockfile lockfile =
        new CompiledModpackProfile.Lockfile(
            "verify-or-write",
            planningResult.lockfileAction(),
            DisplayPaths.displayPath(context, planningResult.lockfilePath()),
            CompiledModpackProfileFingerprint.fromFile(planningResult.lockfilePath()));

    CompiledModpackProfile profileWithoutFingerprint =
        new CompiledModpackProfile(
            CompiledModpackProfile.SCHEMA_VERSION,
            CompiledModpackProfile.PROFILE_KIND,
            "",
            inputFingerprint,
            runtimePolicyFingerprint.compute(context),
            cache,
            new CompiledModpackProfile.Loader(
                CompiledModpackProfile.LOADER_ID, context.loaderVersion()),
            new CompiledModpackProfile.Game(
                planningResult.frozenModGraph().gameProviderId(),
                planningResult.frozenModGraph().gameProviderVersion(),
                gameSide),
            new CompiledModpackProfile.Metadata(metadataSchemaVersions(planningResult)),
            mods,
            resolvedOrder,
            classpath,
            new CompiledModpackProfile.Ownership(
                new CompiledModpackProfile.Count(
                    planningResult.classOwnershipIndex().totalClasses()),
                new CompiledModpackProfile.Count(
                    planningResult.packageOwnershipIndex().packageOwners().size()),
                new CompiledModpackProfile.Resources(
                    planningResult.resourceConflictIndex().conflicts().size())),
            lockfile,
            new CompiledModpackProfile.Permissions(requestedPermissions(planningResult)),
            new CompiledModpackProfile.Lifecycle(
                lifecyclePlan.phaseOrder(), lifecycleHandlers(lifecyclePlan)),
            new CompiledModpackProfile.Contexts(contextPlans),
            new CompiledModpackProfile.PackagePolicy(
                protectedPackagePolicy.protectedPackages(),
                splitPackages(planningResult),
                List.of(),
                packageOwners(planningResult),
                planningResult.protectedPackageViolations()),
            new CompiledModpackProfile.Quality(
                qualityReport.score(),
                qualityReport.fatalCount(),
                qualityReport.warningCount()));
    return profileWithoutFingerprint.withFingerprint(fingerprint.compute(profileWithoutFingerprint));
  }

  private List<Integer> metadataSchemaVersions(ModpackPlanningResult planningResult) {
    return planningResult.resolvedMods().mods().stream()
        .map(ResolvedModSet.ResolvedMod::metadataSchema)
        .collect(java.util.stream.Collectors.toCollection(TreeSet::new))
        .stream()
        .toList();
  }

  private List<CompiledModpackProfile.LifecycleHandler> lifecycleHandlers(LifecyclePlan lifecyclePlan) {
    return lifecyclePlan.handlers().stream()
        .map(
            handler ->
                new CompiledModpackProfile.LifecycleHandler(
                    handler.phase(),
                    handler.modId(),
                    handler.ownerModId(),
                    handler.kind(),
                    handler.className(),
                    handler.methodName(),
                    handler.interfaceName(),
                    handler.jarPath(),
                    handler.jarHash()))
        .toList();
  }

  private List<CompiledModpackProfile.ModPermissions> requestedPermissions(
      ModpackPlanningResult planningResult) {
    return planningResult.resolvedMods().mods().stream()
        .map(mod -> new CompiledModpackProfile.ModPermissions(mod.id(), mod.permissions()))
        .toList();
  }

  private List<CompiledModpackProfile.SplitPackage> splitPackages(
      ModpackPlanningResult planningResult) {
    return planningResult.packageOwnershipIndex().splitPackages().stream()
        .map(
            splitPackage ->
                new CompiledModpackProfile.SplitPackage(
                    splitPackage.packageName(), splitPackage.modIds()))
        .toList();
  }

  private List<CompiledModpackProfile.PackageOwner> packageOwners(
      ModpackPlanningResult planningResult) {
    return planningResult.packageOwnershipIndex().packageOwners().entrySet().stream()
        .map(
            entry ->
                new CompiledModpackProfile.PackageOwner(entry.getKey(), entry.getValue()))
        .toList();
  }
}
