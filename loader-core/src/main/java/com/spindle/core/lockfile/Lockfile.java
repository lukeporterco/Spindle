package com.spindle.core.lockfile;

import com.google.gson.annotations.SerializedName;
import com.spindle.core.launch.LaunchContext;
import com.spindle.core.resolve.ResolvedModSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record Lockfile(
    int schema,
    String loader,
    @SerializedName("java") int javaMajorVersion,
    @SerializedName("minecraft") String minecraftVersion,
    List<LockedMod> mods) {
  public static Lockfile from(LaunchContext context, ResolvedModSet resolvedModSet) {
    List<LockedMod> lockedMods = new ArrayList<>(resolvedModSet.mods().size());
    for (ResolvedModSet.ResolvedMod mod : resolvedModSet.mods()) {
      lockedMods.add(
          new LockedMod(mod.id(), mod.version(), mod.normalizedRelativePath(), mod.sha256()));
    }
    lockedMods.sort(Comparator.comparing(LockedMod::id));
    return new Lockfile(
        1,
        context.loaderVersion(),
        context.javaMajorVersion(),
        context.targetMinecraftVersion(),
        lockedMods);
  }

  public Lockfile {
    mods = List.copyOf(mods);
  }

  public record LockedMod(String id, String version, String path, String sha256) {}
}
