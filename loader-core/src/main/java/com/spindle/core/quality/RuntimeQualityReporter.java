package com.spindle.core.quality;

import com.spindle.core.pipeline.ModpackPlanningResult;
import com.spindle.core.resolve.ResolvedModSet;
import java.util.ArrayList;
import java.util.List;

public final class RuntimeQualityReporter {
  public RuntimeQualityReport create(ModpackPlanningResult planningResult) {
    List<RuntimeQualityReport.Finding> fatalFindings = new ArrayList<>();
    List<RuntimeQualityReport.Finding> warningFindings = new ArrayList<>();

    List<RuntimeQualityReport.DuplicateResource> duplicateResources =
        planningResult.resourceConflictIndex().conflicts().stream()
            .map(
                conflict ->
                    new RuntimeQualityReport.DuplicateResource(
                        conflict.resourcePath(), conflict.modIds()))
            .toList();
    for (RuntimeQualityReport.DuplicateResource duplicateResource : duplicateResources) {
      warningFindings.add(
          new RuntimeQualityReport.Finding(
              "resource.duplicate",
              "warning",
              null,
              "Duplicate resource `"
                  + duplicateResource.resourcePath()
                  + "` is provided by mods "
                  + String.join(", ", duplicateResource.modIds())
                  + "."));
    }

    List<RuntimeQualityReport.SplitPackageRisk> splitPackages =
        planningResult.packageOwnershipIndex().splitPackages().stream()
            .map(
                splitPackage ->
                    new RuntimeQualityReport.SplitPackageRisk(
                        splitPackage.packageName(), splitPackage.modIds()))
            .toList();
    for (RuntimeQualityReport.SplitPackageRisk splitPackage : splitPackages) {
      warningFindings.add(
          new RuntimeQualityReport.Finding(
              "package.split",
              "warning",
              null,
              "Split package `"
                  + splitPackage.packageName()
                  + "` is owned by mods "
                  + String.join(", ", splitPackage.modIds())
                  + "."));
    }

    List<RuntimeQualityReport.ProtectedPackageFinding> protectedPackageViolations =
        planningResult.protectedPackageViolations().stream()
            .map(
                violation ->
                    new RuntimeQualityReport.ProtectedPackageFinding(
                        violation.modId(), violation.packageName(), violation.reason()))
            .toList();
    for (RuntimeQualityReport.ProtectedPackageFinding violation : protectedPackageViolations) {
      fatalFindings.add(
          new RuntimeQualityReport.Finding(
              "package.protected",
              "fatal",
              violation.modId(),
              "Mod `"
                  + violation.modId()
                  + "` defines protected package `"
                  + violation.packageName()
                  + "`. "
                  + violation.reason()));
    }

    List<RuntimeQualityReport.MetadataFinding> metadataFindings =
        metadataFindings(planningResult.resolvedMods());
    for (RuntimeQualityReport.MetadataFinding metadataFinding : metadataFindings) {
      warningFindings.add(
          new RuntimeQualityReport.Finding(
              "metadata." + metadataFinding.field(),
              "warning",
              metadataFinding.modId(),
              metadataFinding.message()));
    }

    int score = Math.max(0, 100 - (fatalFindings.size() * 25) - (warningFindings.size() * 5));
    return new RuntimeQualityReport(
        score,
        fatalFindings,
        warningFindings,
        duplicateResources,
        splitPackages,
        protectedPackageViolations,
        List.of(),
        metadataFindings);
  }

  private List<RuntimeQualityReport.MetadataFinding> metadataFindings(ResolvedModSet resolvedMods) {
    List<RuntimeQualityReport.MetadataFinding> findings = new ArrayList<>();
    for (ResolvedModSet.ResolvedMod mod : resolvedMods.mods()) {
      if (!mod.permissions().isEmpty()) {
        findings.add(
            new RuntimeQualityReport.MetadataFinding(
                mod.id(),
                "permissions",
                "Runtime-1 records permissions but does not implement a permission runtime; requested values: "
                    + String.join(", ", mod.permissions())));
      }
    }
    return List.copyOf(findings);
  }
}
