package com.spindle.core.security;

import com.spindle.api.lifecycle.LifecyclePhase;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.runtime.CompiledModpackProfile;
import com.spindle.core.runtime.RuntimeProtectedPackagePolicy;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public record SecurityPolicyFingerprint(String value) {
  public static SecurityPolicyFingerprint compute(
      SecurityPolicy policy, RuntimeProtectedPackagePolicy protectedPackagePolicy)
      throws LoaderException {
    MessageDigest digest = createDigest();
    update(digest, "securityPolicyVersion", Integer.toString(policy.securityPolicyVersion()));
    update(
        digest,
        "runtimeProtectedPackagePolicyVersion",
        Integer.toString(RuntimeProtectedPackagePolicy.POLICY_VERSION));
    for (String protectedPackage : protectedPackagePolicy.protectedPackages()) {
      update(digest, "protectedPackage", protectedPackage);
    }
    for (String loaderOwnedPackage : policy.loaderOwnedPackages()) {
      update(digest, "loaderOwnedPackage", loaderOwnedPackage);
    }
    for (String shadowedClass : policy.knownShadowedClasses()) {
      update(digest, "knownShadowedClass", shadowedClass);
    }
    update(
        digest,
        "lifecycleSignaturePolicyVersion",
        Integer.toString(policy.lifecycleSignaturePolicyVersion()));
    update(
        digest, "pathBoundaryPolicyVersion", Integer.toString(policy.pathBoundaryPolicyVersion()));
    update(digest, "permissionPolicyVersion", Integer.toString(policy.permissionPolicyVersion()));
    update(
        digest,
        "cacheProfileValidationPolicyVersion",
        Integer.toString(policy.cacheProfileValidationPolicyVersion()));
    update(
        digest,
        "artifactLockfileVerificationPolicyVersion",
        Integer.toString(policy.artifactLockfileVerificationPolicyVersion()));
    update(
        digest,
        "artifactTrustPolicyVersion",
        Integer.toString(policy.artifactTrustPolicyVersion()));
    update(
        digest,
        "compiledProfileSchemaVersion",
        Integer.toString(CompiledModpackProfile.SCHEMA_VERSION));
    for (LifecyclePhase phase : LifecyclePhase.values()) {
      update(digest, "lifecyclePhase", phase.name());
    }
    return new SecurityPolicyFingerprint(HexFormat.of().formatHex(digest.digest()));
  }

  private static MessageDigest createDigest() throws LoaderException {
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException exception) {
      throw new LoaderException("SHA-256 algorithm unavailable", exception);
    }
  }

  private static void update(MessageDigest digest, String key, String value) {
    digest.update(key.getBytes(StandardCharsets.UTF_8));
    digest.update((byte) '=');
    digest.update((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
    digest.update((byte) '\n');
  }
}
