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
        4,
        1,
        1,
        2,
        1,
        1,
        1,
        List.of("com.spindle.api", "com.spindle.api.internal", "com.spindle.core"),
        List.of(
            "com.spindle.api.ModContext",
            "com.spindle.api.ModInitializer",
            "com.spindle.api.lifecycle.LifecyclePhase"),
        List.of(
            "artifact-lockfile-identity",
            "artifact-trust",
            "capability-grant-contract",
            "cache-profile-validation",
            "class-ownership",
            "lifecycle-declarations",
            "lifecycle-signatures",
            "mod-context-paths",
            "package-ownership",
            "permission-declarations",
            "runtime-policy-fingerprint",
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
