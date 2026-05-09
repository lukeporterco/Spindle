package com.spindle.core.security;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.launch.LaunchContext;
import com.spindle.core.pipeline.ModpackPlanningResult;
import com.spindle.core.resolve.ResolvedModSet;
import com.spindle.core.runtime.CompiledModpackProfile;
import com.spindle.core.runtime.CompiledModpackProfileFingerprint;
import com.spindle.core.runtime.ProtectedPackageViolation;
import com.spindle.core.runtime.RuntimeProtectedPackagePolicy;
import com.spindle.core.security.tool.RestrictedToolProcessLauncher;
import com.spindle.core.security.tool.RestrictedToolResult;
import com.spindle.core.security.trust.ArtifactTrustEvaluation;
import com.spindle.core.security.trust.ArtifactTrustEvaluator;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SecurityValidator {
  private final SecurityPolicy policy = SecurityPolicy.standard();
  private final RuntimeProtectedPackagePolicy protectedPackagePolicy =
      new RuntimeProtectedPackagePolicy();
  private final CompiledModpackProfileFingerprint profileFingerprintCalculator =
      new CompiledModpackProfileFingerprint();
  private final ArtifactTrustEvaluator artifactTrustEvaluator = new ArtifactTrustEvaluator();
  private final RestrictedToolProcessLauncher restrictedToolProcessLauncher;

  public SecurityValidator() {
    this(new RestrictedToolProcessLauncher());
  }

  SecurityValidator(RestrictedToolProcessLauncher restrictedToolProcessLauncher) {
    this.restrictedToolProcessLauncher = restrictedToolProcessLauncher;
  }

  public SecurityValidationResult validate(SecurityValidationContext context)
      throws LoaderException {
    List<SecurityFinding> findings = new ArrayList<>();
    Map<String, ResolvedModSet.ResolvedMod> resolvedModsById =
        resolvedModsById(context.planningResult());
    SecurityPolicyFingerprint securityPolicyFingerprint =
        SecurityPolicyFingerprint.compute(policy, protectedPackagePolicy);
    ArtifactTrustEvaluation artifactTrustEvaluation =
        artifactTrustEvaluator.evaluate(
            context.planningResult().resolvedMods().mods(),
            context.planningResult().lockfileAction());
    RestrictedToolResult restrictedToolResult =
        restrictedToolProcessLauncher.runStaticRiskScan(
            context.launchContext().workingDirectory(),
            context.planningResult().resolvedMods().mods());

    validateLoaderOwnedPackages(context.planningResult(), resolvedModsById, findings);
    validateProtectedPackages(context.planningResult(), findings);
    validateShadowedClasses(context.planningResult(), findings);
    validateContextPaths(context.launchContext(), context.compiledProfile(), findings);
    validateCacheStatus(context.compiledProfile(), findings);
    validateRequestedPermissions(context.compiledProfile(), findings);
    validateRuntimeIdentity(context, findings);
    findings.addAll(artifactTrustEvaluation.findings());
    findings.addAll(restrictedToolResult.findings());

    return new SecurityValidationResult(
        securityPolicyFingerprint,
        restrictedToolResult,
        policy.validatedSurfaces(),
        artifactTrustEvaluation.entries(),
        artifactTrustEvaluation.summary(),
        restrictedToolResult.analysis().summary(),
        restrictedToolResult.analysis().signals(),
        findings);
  }

  public SecurityValidationReport toReport(
      SecurityValidationContext context, SecurityValidationResult validationResult) {
    CompiledModpackProfile profile = context.compiledProfile();
    return new SecurityValidationReport(
        SecurityValidationReport.SCHEMA_VERSION,
        SecurityValidationReport.REPORT_KIND,
        validationResult.state(),
        profile.loader(),
        profile.game(),
        profile.fingerprint(),
        profile.inputFingerprint(),
        context.currentRuntimePolicyFingerprint(),
        validationResult.securityPolicyFingerprint().value(),
        SecurityPolicy.EXECUTION_ISOLATION_MODE,
        SecurityPolicy.EXECUTION_ISOLATION_MODE,
        false,
        false,
        SecurityPolicy.SANDBOX_CLAIM,
        new SecurityValidationReport.ToolIsolation(
            validationResult.restrictedToolResult().mode().id(),
            validationResult.restrictedToolResult().worker(),
            validationResult.restrictedToolResult().status(),
            validationResult.restrictedToolResult().outputPath()),
        validationResult.fatalCount(),
        validationResult.warningCount(),
        validationResult.validatedSurfaces(),
        validationResult.artifactTrustEntries(),
        validationResult.artifactTrustSummary(),
        validationResult.staticRiskSummary(),
        validationResult.staticRiskSignals(),
        validationResult.findings());
  }

  private void validateLoaderOwnedPackages(
      ModpackPlanningResult planningResult,
      Map<String, ResolvedModSet.ResolvedMod> resolvedModsById,
      List<SecurityFinding> findings) {
    planningResult
        .packageOwnershipIndex()
        .packageOwners()
        .forEach(
            (packageName, modIds) -> {
              if (!policy.isLoaderOwnedPackage(packageName)) {
                return;
              }
              for (String modId : modIds) {
                ResolvedModSet.ResolvedMod mod = resolvedModsById.get(modId);
                if (mod == null || mod.metadataSchema() < 2) {
                  continue;
                }
                findings.add(
                    new SecurityFinding(
                        SecurityRuleId.SEC_PACKAGE_001,
                        SecuritySeverity.FATAL,
                        modId,
                        SecurityLocation.of("package", packageName),
                        "defines loader-owned package `"
                            + packageName
                            + "`, which is reserved for Spindle loader code.",
                        "Move mod classes into a mod-owned package that does not start with `com.spindle.api` or `com.spindle.core`"));
              }
            });
  }

  private void validateProtectedPackages(
      ModpackPlanningResult planningResult, List<SecurityFinding> findings) {
    for (ProtectedPackageViolation violation : planningResult.protectedPackageViolations()) {
      if (policy.isLoaderOwnedPackage(violation.packageName())) {
        continue;
      }
      findings.add(
          new SecurityFinding(
              SecurityRuleId.SEC_PACKAGE_002,
              SecuritySeverity.FATAL,
              violation.modId(),
              SecurityLocation.of("package", violation.packageName()),
              "defines protected platform or compatibility package `"
                  + violation.packageName()
                  + "`, which Spindle does not allow mods to own.",
              "Move classes into a mod-owned package and keep `"
                  + violation.packageName()
                  + "` reserved for the platform or compatibility layer"));
    }
  }

  private void validateShadowedClasses(
      ModpackPlanningResult planningResult, List<SecurityFinding> findings) {
    Map<String, String> classOwners = planningResult.classOwnershipIndex().classOwners();
    for (String className : policy.knownShadowedClasses()) {
      String modId = classOwners.get(className);
      if (modId == null) {
        continue;
      }
      findings.add(
          new SecurityFinding(
              SecurityRuleId.SEC_CLASS_001,
              SecuritySeverity.FATAL,
              modId,
              SecurityLocation.of("class", className),
              "shadows known Spindle API/core class `"
                  + className
                  + "`, which would blur the loader trust boundary.",
              "Rename or move the mod class into a mod-owned namespace and reference the real Spindle API class instead"));
    }
  }

  private void validateContextPaths(
      LaunchContext launchContext,
      CompiledModpackProfile compiledProfile,
      List<SecurityFinding> findings) {
    Path workingDirectory = launchContext.workingDirectory().toAbsolutePath().normalize();
    for (CompiledModpackProfile.ModContextPlan plan : compiledProfile.contexts().mods()) {
      validateContextPathField(
          workingDirectory, plan.modId(), "configDirectory", plan.configDirectory(), findings);
      validateContextPathField(
          workingDirectory, plan.modId(), "dataDirectory", plan.dataDirectory(), findings);
      validateContextPathField(
          workingDirectory, plan.modId(), "cacheDirectory", plan.cacheDirectory(), findings);
      validateContextPathField(
          workingDirectory,
          plan.modId(),
          "generatedDirectory",
          plan.generatedDirectory(),
          findings);
    }
  }

  private void validateContextPathField(
      Path workingDirectory,
      String modId,
      String fieldName,
      String pathValue,
      List<SecurityFinding> findings) {
    String normalizedValue = normalizePathDisplay(pathValue);
    Path logicalPath = parseLogicalPath(normalizedValue);
    if (normalizedValue == null || normalizedValue.isBlank()) {
      findings.add(
          new SecurityFinding(
              SecurityRuleId.SEC_PATH_002,
              SecuritySeverity.FATAL,
              modId,
              SecurityLocation.of("field", "contexts.mods[" + modId + "]." + fieldName),
              "declares empty owned storage path for `" + fieldName + "`.",
              "Use a logical relative directory such as `config/"
                  + modId
                  + "` inside the working directory"));
      return;
    }
    if (logicalPath == null
        || logicalPath.isAbsolute()
        || hasBacktracking(logicalPath)
        || normalizedValue.startsWith("/")) {
      findings.add(
          new SecurityFinding(
              SecurityRuleId.SEC_PATH_002,
              SecuritySeverity.FATAL,
              modId,
              SecurityLocation.of("field", "contexts.mods[" + modId + "]." + fieldName),
              "declares non-logical owned storage path value `"
                  + redactAbsolutePath(normalizedValue, logicalPath)
                  + "` for `"
                  + fieldName
                  + "`.",
              "Use a logical relative path without absolute roots or `..` segments, for example `config/"
                  + modId
                  + "`"));
      return;
    }

    Path resolved = workingDirectory.resolve(logicalPath).normalize();
    if (!resolved.startsWith(workingDirectory)) {
      findings.add(
          new SecurityFinding(
              SecurityRuleId.SEC_PATH_001,
              SecuritySeverity.FATAL,
              modId,
              SecurityLocation.of("field", "contexts.mods[" + modId + "]." + fieldName),
              "declares owned storage path `"
                  + normalizedValue
                  + "` for `"
                  + fieldName
                  + "`, but the planned path escapes the working directory boundary.",
              "Keep owned storage paths under the working directory, for example `config/"
                  + modId
                  + "` or `mod-data/"
                  + modId
                  + "`"));
    }
  }

  private void validateCacheStatus(
      CompiledModpackProfile compiledProfile, List<SecurityFinding> findings) {
    if (!"miss".equals(compiledProfile.cache().status())) {
      return;
    }
    if ("missing profile".equals(compiledProfile.cache().reason())) {
      return;
    }
    findings.add(
        new SecurityFinding(
            SecurityRuleId.SEC_CACHE_001,
            SecuritySeverity.WARNING,
            null,
            SecurityLocation.of("cache", "compiledProfile.cache"),
            "cached compiled profile failed validation and Spindle rebuilt it with miss reason `"
                + compiledProfile.cache().reason()
                + "`.",
            "Inspect the cached profile cause if this repeats; Spindle should only execute rebuilt data that passes current validation"));
  }

  private void validateRequestedPermissions(
      CompiledModpackProfile compiledProfile, List<SecurityFinding> findings) {
    for (CompiledModpackProfile.ModPermissions modPermissions :
        compiledProfile.permissions().mods()) {
      for (String permission : modPermissions.requested()) {
        findings.add(
            new SecurityFinding(
                SecurityRuleId.SEC_PERM_001,
                SecuritySeverity.WARNING,
                modPermissions.modId(),
                SecurityLocation.of("permission", permission),
                "requests permission `"
                    + permission
                    + "`, which Spindle records for developer visibility but does not yet grant or enforce.",
                "Treat requested permissions as documentation only for now and design the mod to run without assuming sandbox or permission enforcement"));
      }
    }
  }

  private void validateRuntimeIdentity(
      SecurityValidationContext context, List<SecurityFinding> findings) throws LoaderException {
    CompiledModpackProfile compiledProfile = context.compiledProfile();
    String recomputedProfileFingerprint = profileFingerprintCalculator.compute(compiledProfile);
    if (!context
            .currentRuntimePolicyFingerprint()
            .equals(compiledProfile.runtimePolicyFingerprint())
        || !recomputedProfileFingerprint.equals(compiledProfile.fingerprint())) {
      findings.add(
          new SecurityFinding(
              SecurityRuleId.SEC_RUNTIME_001,
              SecuritySeverity.FATAL,
              null,
              SecurityLocation.of("profile", "spindle.profile.json"),
              "compiled runtime identity does not match the current runtime or compiled profile policy fingerprint.",
              "Rebuild the compiled profile and ensure Spindle executes only the profile generated for the current policy"));
    }
  }

  private Map<String, ResolvedModSet.ResolvedMod> resolvedModsById(
      ModpackPlanningResult planningResult) {
    Map<String, ResolvedModSet.ResolvedMod> resolvedModsById = new HashMap<>();
    for (ResolvedModSet.ResolvedMod mod : planningResult.resolvedMods().mods()) {
      resolvedModsById.put(mod.id(), mod);
    }
    return resolvedModsById;
  }

  private Path parseLogicalPath(String value) {
    if (value == null) {
      return null;
    }
    try {
      return Path.of(value);
    } catch (InvalidPathException exception) {
      return null;
    }
  }

  private boolean hasBacktracking(Path path) {
    for (Path segment : path.normalize()) {
      if ("..".equals(segment.toString()) || ".".equals(segment.toString())) {
        return true;
      }
    }
    for (Path segment : path) {
      if ("..".equals(segment.toString())) {
        return true;
      }
    }
    return false;
  }

  private String normalizePathDisplay(String value) {
    if (value == null) {
      return null;
    }
    return value.trim().replace('\\', '/');
  }

  private String redactAbsolutePath(String normalizedValue, Path logicalPath) {
    if (normalizedValue == null) {
      return "";
    }
    if (logicalPath != null && logicalPath.isAbsolute()) {
      return "[absolute path]";
    }
    if (normalizedValue.matches("^[A-Za-z]:/.*")) {
      return "[absolute path]";
    }
    return normalizedValue;
  }
}
