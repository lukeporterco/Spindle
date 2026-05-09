package com.mcmodloader.core.runtime;

import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.launch.LaunchContext;
import com.mcmodloader.core.pipeline.ModpackPlanningResult;
import com.mcmodloader.core.report.DisplayPaths;
import com.mcmodloader.core.resolve.ResolvedModSet;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CompiledModpackProfileBuilder {
  public CompiledModpackProfile build(
      LaunchContext context, ModpackPlanningResult planningResult, String gameSide)
      throws LoaderException {
    List<CompiledModpackProfile.Mod> mods = new ArrayList<>(planningResult.resolvedMods().mods().size());
    List<String> resolvedOrder = new ArrayList<>(planningResult.resolvedMods().mods().size());
    Map<Path, CompiledModpackProfile.ClasspathEntry> classpathEntriesByJar = new LinkedHashMap<>();

    for (ResolvedModSet.ResolvedMod mod : planningResult.resolvedMods().mods()) {
      mods.add(
          new CompiledModpackProfile.Mod(
              mod.id(), mod.version(), mod.normalizedRelativePath(), mod.sha256()));
      resolvedOrder.add(mod.id());
      classpathEntriesByJar.put(
          mod.jarPath().toAbsolutePath().normalize(),
          new CompiledModpackProfile.ClasspathEntry(mod.normalizedRelativePath(), mod.id()));
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
            DisplayPaths.displayPath(context, planningResult.lockfilePath()),
            CompiledModpackProfileFingerprint.fromFile(planningResult.lockfilePath()));
    CompiledModpackProfile profileWithoutFingerprint =
        new CompiledModpackProfile(
            CompiledModpackProfile.SCHEMA_VERSION,
            CompiledModpackProfile.PROFILE_KIND,
            "",
            new CompiledModpackProfile.Loader(
                CompiledModpackProfile.LOADER_ID, context.loaderVersion()),
            new CompiledModpackProfile.Game(
                planningResult.frozenModGraph().gameProviderId(),
                planningResult.frozenModGraph().gameProviderVersion(),
                gameSide),
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
            lockfile);
    String fingerprint = new CompiledModpackProfileFingerprint().compute(profileWithoutFingerprint);
    return new CompiledModpackProfile(
        profileWithoutFingerprint.schemaVersion(),
        profileWithoutFingerprint.profileKind(),
        fingerprint,
        profileWithoutFingerprint.loader(),
        profileWithoutFingerprint.game(),
        profileWithoutFingerprint.mods(),
        profileWithoutFingerprint.resolvedOrder(),
        profileWithoutFingerprint.classpath(),
        profileWithoutFingerprint.ownership(),
        profileWithoutFingerprint.lockfile());
  }
}
