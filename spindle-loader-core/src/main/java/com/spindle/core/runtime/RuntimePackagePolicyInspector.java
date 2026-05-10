package com.spindle.core.runtime;

import com.spindle.core.resolve.ResolvedModSet;
import com.spindle.core.ownership.PackageOwnershipIndex;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RuntimePackagePolicyInspector {
  private final RuntimeProtectedPackagePolicy protectedPackagePolicy =
      new RuntimeProtectedPackagePolicy();

  public List<ProtectedPackageViolation> findProtectedPackageViolations(
      ResolvedModSet resolvedMods, PackageOwnershipIndex packageOwnershipIndex) {
    Map<String, ResolvedModSet.ResolvedMod> modById =
        resolvedMods.mods().stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    ResolvedModSet.ResolvedMod::id, mod -> mod, (left, right) -> left));
    List<ProtectedPackageViolation> violations = new ArrayList<>();
    for (Map.Entry<String, List<String>> entry : packageOwnershipIndex.packageOwners().entrySet()) {
      String packageName = entry.getKey();
      if (!protectedPackagePolicy.isProtectedDefinitionPackage(packageName)) {
        continue;
      }
      for (String modId : entry.getValue()) {
        ResolvedModSet.ResolvedMod mod = modById.get(modId);
        if (mod == null || mod.metadataSchema() < 2) {
          continue;
        }
        violations.add(
            new ProtectedPackageViolation(
                modId,
                packageName,
                "Mod `"
                    + modId
                    + "` may not define protected package `"
                    + packageName
                    + "`. Move classes out of the protected package."));
      }
    }
    return List.copyOf(violations);
  }
}
