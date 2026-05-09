package com.spindle.core.runtime;

import java.util.List;

public final class RuntimeProtectedPackagePolicy {
  private static final List<String> PROTECTED_PACKAGES =
      List.of(
          "java",
          "javax",
          "sun",
          "jdk",
          "com.sun",
          "com.spindle.core",
          "com.spindle.api.internal",
          "net.minecraft",
          "org.spongepowered.asm");

  public List<String> protectedPackages() {
    return PROTECTED_PACKAGES;
  }

  public boolean isProtectedDefinitionPackage(String packageName) {
    for (String protectedPackage : PROTECTED_PACKAGES) {
      if (packageName.equals(protectedPackage)
          || packageName.startsWith(protectedPackage + ".")) {
        return true;
      }
    }
    return false;
  }
}
