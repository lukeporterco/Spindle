package com.spindle.core.quality;

import java.util.Comparator;
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
    fatalFindings = fatalFindings.stream().sorted(Finding.ORDER).toList();
    warningFindings = warningFindings.stream().sorted(Finding.ORDER).toList();
    duplicateResources =
        duplicateResources.stream().sorted(Comparator.comparing(DuplicateResource::resourcePath)).toList();
    splitPackages = splitPackages.stream().sorted(Comparator.comparing(SplitPackageRisk::packageName)).toList();
    protectedPackageViolations =
        protectedPackageViolations.stream()
            .sorted(
                Comparator.comparing(ProtectedPackageFinding::modId)
                    .thenComparing(ProtectedPackageFinding::packageName)
                    .thenComparing(ProtectedPackageFinding::reason))
            .toList();
    lifecycleDeclarationProblems =
        lifecycleDeclarationProblems.stream()
            .sorted(
                Comparator.comparing(LifecycleDeclarationProblem::modId)
                    .thenComparing(LifecycleDeclarationProblem::phase)
                    .thenComparing(LifecycleDeclarationProblem::declaration)
                    .thenComparing(LifecycleDeclarationProblem::reason))
            .toList();
    metadataFindings =
        metadataFindings.stream()
            .sorted(
                Comparator.comparing(MetadataFinding::modId)
                    .thenComparing(MetadataFinding::field)
                    .thenComparing(MetadataFinding::message))
            .toList();
  }

  public int fatalCount() {
    return fatalFindings.size();
  }

  public int warningCount() {
    return warningFindings.size();
  }

  public record Finding(String code, String severity, String modId, String message) {
    private static final Comparator<Finding> ORDER =
        Comparator.comparing(Finding::code)
            .thenComparing(finding -> finding.modId() == null ? "" : finding.modId())
            .thenComparing(Finding::message);
  }

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
