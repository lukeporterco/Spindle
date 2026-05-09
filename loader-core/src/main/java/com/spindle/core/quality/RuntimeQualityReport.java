package com.spindle.core.quality;

import java.util.List;

public record RuntimeQualityReport(
    int score,
    String scoreKind,
    List<Finding> fatalFindings,
    List<Finding> warningFindings,
    List<DuplicateResource> duplicateResources,
    List<SplitPackageRisk> splitPackages,
    List<ProtectedPackageFinding> protectedPackageViolations,
    List<LifecycleDeclarationProblem> lifecycleDeclarationProblems,
    List<MetadataFinding> metadataFindings) {
  public RuntimeQualityReport {
    fatalFindings = List.copyOf(fatalFindings);
    warningFindings = List.copyOf(warningFindings);
    duplicateResources = List.copyOf(duplicateResources);
    splitPackages = List.copyOf(splitPackages);
    protectedPackageViolations = List.copyOf(protectedPackageViolations);
    lifecycleDeclarationProblems = List.copyOf(lifecycleDeclarationProblems);
    metadataFindings = List.copyOf(metadataFindings);
  }

  public int fatalCount() {
    return fatalFindings.size();
  }

  public int warningCount() {
    return warningFindings.size();
  }

  public record Finding(String code, String severity, String modId, String message) {}

  public record DuplicateResource(String resourcePath, List<String> modIds) {
    public DuplicateResource {
      modIds = List.copyOf(modIds);
    }
  }

  public record SplitPackageRisk(String packageName, List<String> modIds) {
    public SplitPackageRisk {
      modIds = List.copyOf(modIds);
    }
  }

  public record ProtectedPackageFinding(String modId, String packageName, String reason) {}

  public record LifecycleDeclarationProblem(
      String modId, String phase, String declaration, String reason) {}

  public record MetadataFinding(String modId, String field, String message) {}

  public static final String SCORE_KIND_EARLY_DETERMINISTIC_SIGNAL = "early-deterministic-signal";
}
