package com.spindle.core.security;

import java.util.List;

public record SecurityPolicy(
    int securityPolicyVersion,
    int lifecycleSignaturePolicyVersion,
    int pathBoundaryPolicyVersion,
    int permissionPolicyVersion,
    int cacheProfileValidationPolicyVersion,
    int artifactLockfileVerificationPolicyVersion,
    int artifactTrustPolicyVersion,
    List<String> loaderOwnedPackages,
    List<String> knownShadowedClasses,
    List<String> validatedSurfaces) {
  public static final String EXECUTION_ISOLATION_MODE = "in-process-unrestricted-java";
  public static final String SANDBOX_CLAIM = "not-sandboxed";

  public SecurityPolicy {
    loaderOwnedPackages = List.copyOf(loaderOwnedPackages);
    knownShadowedClasses = List.copyOf(knownShadowedClasses);
    validatedSurfaces = List.copyOf(validatedSurfaces);
  }

  public static SecurityPolicy standard() {
    return new SecurityPolicy(
        8,
        1,
        1,
        5,
        1,
        1,
        1,
        List.of("com.spindle.api", "com.spindle.api.internal", "com.spindle.core"),
        List.of(
            "com.spindle.api.LoaderApi",
            "com.spindle.api.ModContext",
            "com.spindle.api.ModInitializer",
            "com.spindle.api.config.ModConfig",
            "com.spindle.api.exception.CapabilityDeniedException",
            "com.spindle.api.exception.ConfigAccessException",
            "com.spindle.api.exception.ServiceAccessException",
            "com.spindle.api.exception.SpindleApiException",
            "com.spindle.api.lifecycle.LifecyclePhase",
            "com.spindle.api.service.ServiceRegistry"),
        List.of(
            "artifact-lockfile-identity",
            "artifact-trust",
            "capability-grant-contract",
            "cache-profile-validation",
            "class-ownership",
            "config-schema-capability-grants",
            "loader-api-0-public-boundary",
            "loader-api-boundary-inventory",
            "lifecycle-declarations",
            "lifecycle-signatures",
            "mod-context-paths",
            "package-ownership",
            "permission-declarations",
            "resource-capabilities-explicitly-unavailable",
            "runtime-contract-closure",
            "runtime-policy-fingerprint",
            "service-registry-capability-grants",
            "static-risk-signals"));
  }

  public boolean isLoaderOwnedPackage(String packageName) {
    for (String loaderOwnedPackage : loaderOwnedPackages) {
      if (packageName.equals(loaderOwnedPackage)
          || packageName.startsWith(loaderOwnedPackage + ".")) {
        return true;
      }
    }
    return false;
  }
}
