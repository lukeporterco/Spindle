package com.spindle.core.runtime;

import com.spindle.api.lifecycle.LifecyclePhase;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.launch.LaunchContext;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class RuntimePolicyFingerprint {
  private final RuntimeProtectedPackagePolicy protectedPackagePolicy =
      new RuntimeProtectedPackagePolicy();

  public String compute(LaunchContext context) throws LoaderException {
    MessageDigest digest = CompiledModpackProfileFingerprint.createDigest();
    update(digest, "profileSchemaVersion", Integer.toString(CompiledModpackProfile.SCHEMA_VERSION));
    update(digest, "packagePolicyVersion", Integer.toString(protectedPackagePolicy.policyVersion()));
    update(digest, "strictResources", Boolean.toString(context.strictResources()));
    update(digest, "strictPackages", Boolean.toString(context.strictPackages()));
    for (LifecyclePhase phase : LifecyclePhase.values()) {
      update(digest, "lifecyclePhase", phase.name());
    }
    for (String protectedPackage : protectedPackagePolicy.protectedPackages()) {
      update(digest, "protectedPackage", protectedPackage);
    }
    return HexFormat.of().formatHex(digest.digest());
  }

  private static void update(MessageDigest digest, String key, String value) {
    digest.update(key.getBytes(StandardCharsets.UTF_8));
    digest.update((byte) '=');
    digest.update((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
    digest.update((byte) '\n');
  }
}
