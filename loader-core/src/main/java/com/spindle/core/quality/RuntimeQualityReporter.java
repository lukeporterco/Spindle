package com.spindle.core.quality;

import com.spindle.core.pipeline.ModpackPlanningResult;
import com.spindle.core.resolve.ResolvedModSet;
import com.spindle.core.runtime.capability.RuntimeCapabilityGrant;
import com.spindle.core.runtime.capability.RuntimeCapabilityModPlan;
import com.spindle.core.runtime.capability.RuntimeCapabilityPlanner;
import com.spindle.core.runtime.service.RuntimeServiceBinding;
import com.spindle.core.runtime.service.RuntimeServiceContract;
import com.spindle.core.runtime.service.RuntimeServiceModPlan;
import com.spindle.core.runtime.service.RuntimeServiceProviderPlan;
import java.util.ArrayList;
import java.util.List;

public final class RuntimeQualityReporter {
  private final RuntimeCapabilityPlanner runtimeCapabilityPlanner = new RuntimeCapabilityPlanner();

  public RuntimeQualityReport create(
      ModpackPlanningResult planningResult, RuntimeServiceContract serviceContract) {
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

    addServiceFindings(serviceContract, fatalFindings, warningFindings);

    int score = Math.max(0, 100 - (fatalFindings.size() * 25) - (warningFindings.size() * 5));
    return new RuntimeQualityReport(
        score,
        RuntimeQualityReport.SCORE_KIND_EARLY_DETERMINISTIC_SIGNAL,
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
    for (RuntimeCapabilityModPlan modPlan : runtimeCapabilityPlanner.plan(resolvedMods).mods()) {
      for (String requestedCapability : modPlan.requested()) {
        RuntimeCapabilityGrant grant = grantFor(modPlan, requestedCapability);
        if (grant == null || "granted".equals(grant.state())) {
          continue;
        }
        findings.add(
            new RuntimeQualityReport.MetadataFinding(
                modPlan.modId(), "capabilities", qualityMessage(requestedCapability, grant)));
      }
    }
    return List.copyOf(findings);
  }

  private void addServiceFindings(
      RuntimeServiceContract serviceContract,
      List<RuntimeQualityReport.Finding> fatalFindings,
      List<RuntimeQualityReport.Finding> warningFindings) {
    for (RuntimeServiceModPlan modPlan : serviceContract.mods()) {
      for (RuntimeServiceProviderPlan provider : modPlan.provides()) {
        switch (provider.state()) {
          case "conflict" ->
              fatalFindings.add(
                  new RuntimeQualityReport.Finding(
                      "service.provider_conflict",
                      "fatal",
                      modPlan.modId(),
                      "Mod `"
                          + modPlan.modId()
                          + "` declares conflicting provider for service `"
                          + provider.id()
                          + "`. "
                          + provider.reason()));
          case "implementation-missing" ->
              fatalFindings.add(
                  new RuntimeQualityReport.Finding(
                      "service.implementation_missing",
                      "fatal",
                      modPlan.modId(),
                      "Mod `"
                          + modPlan.modId()
                          + "` declares provider implementation `"
                          + provider.implementation()
                          + "` for service `"
                          + provider.id()
                          + "`, but it is missing. "
                          + provider.reason()));
          case "implementation-not-owned" ->
              fatalFindings.add(
                  new RuntimeQualityReport.Finding(
                      "service.implementation_not_owned",
                      "fatal",
                      modPlan.modId(),
                      "Mod `"
                          + modPlan.modId()
                          + "` declares provider implementation `"
                          + provider.implementation()
                          + "` for service `"
                          + provider.id()
                          + "`, but it is not owned by the declaring mod. "
                          + provider.reason()));
          default -> {}
        }
      }
    }

    for (RuntimeServiceBinding binding : serviceContract.bindings()) {
      switch (binding.state()) {
        case "required-unbound" ->
            fatalFindings.add(
                new RuntimeQualityReport.Finding(
                    "service.required_unbound",
                    "fatal",
                    binding.consumerModId(),
                    "Mod `"
                        + binding.consumerModId()
                        + "` declares required service `"
                        + binding.id()
                        + "`, but it was not bound."));
        case "provider-conflict" ->
            fatalFindings.add(
                new RuntimeQualityReport.Finding(
                    "service.provider_conflict",
                    "fatal",
                    binding.consumerModId(),
                    "Mod `"
                        + binding.consumerModId()
                        + "` cannot bind service `"
                        + binding.id()
                        + "` because multiple providers were declared."));
        case "type-mismatch" ->
            fatalFindings.add(
                new RuntimeQualityReport.Finding(
                    "service.type_mismatch",
                    "fatal",
                    binding.consumerModId(),
                    "Mod `"
                        + binding.consumerModId()
                        + "` declares service `"
                        + binding.id()
                        + "` with type `"
                        + binding.type()
                        + "`, but the provider type does not match."));
        case "optional-unbound" ->
            warningFindings.add(
                new RuntimeQualityReport.Finding(
                    "service.optional_unbound",
                    "warning",
                    binding.consumerModId(),
                    "Mod `"
                        + binding.consumerModId()
                        + "` declares optional service `"
                        + binding.id()
                        + "`, but it was not bound."));
        default -> {}
      }
    }
  }

  private RuntimeCapabilityGrant grantFor(RuntimeCapabilityModPlan modPlan, String capability) {
    return modPlan.grants().stream()
        .filter(grant -> capability.equals(grant.capability()))
        .findFirst()
        .orElse(null);
  }

  private String qualityMessage(String requestedCapability, RuntimeCapabilityGrant grant) {
    return switch (grant.state()) {
      case "denied" -> deniedCapabilityMessage(requestedCapability);
      case "unavailable" ->
          "Runtime-3 marks requested capability `"
              + requestedCapability
              + "` as unavailable; the corresponding Spindle API surface is not implemented yet.";
      case "unknown" ->
          "Runtime-3 does not recognize requested capability `" + requestedCapability + "`.";
      case "visibility-only" ->
          "Runtime-3 records requested capability `"
              + requestedCapability
              + "` as visibility-only; runtime Java behavior remains in-process unrestricted Java.";
      default ->
          throw new IllegalArgumentException("Unsupported capability state " + grant.state());
    };
  }

  private String deniedCapabilityMessage(String requestedCapability) {
    return switch (requestedCapability) {
      case "service.provide" ->
          "Runtime-3 denied requested capability `service.provide` because `services.provides`"
              + " does not declare any service providers in loader.mod.json.";
      case "service.consume" ->
          "Runtime-3 denied requested capability `service.consume` because `services.consumes`"
              + " does not declare any service consumers in loader.mod.json.";
      default ->
          "Runtime-3 denied requested capability `"
              + requestedCapability
              + "` because the matching storage flag is not enabled in loader.mod.json.";
    };
  }
}
