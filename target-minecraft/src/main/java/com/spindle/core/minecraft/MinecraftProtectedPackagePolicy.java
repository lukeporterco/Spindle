package com.spindle.core.minecraft;

import java.util.List;

public final class MinecraftProtectedPackagePolicy {
  private static final List<String> PROTECTED_DEFINITION_PREFIXES =
      List.of(
          "java.", "javax.", "sun.", "jdk.", "com.sun.", "com.spindle.core.", "com.spindle.api.");
  private static final List<String> DENIED_LOAD_PREFIXES = List.of("com.spindle.core.");
  private static final List<String> ALLOWED_API_PREFIXES =
      List.of("com.spindle.api.", "com.spindle.api.minecraft.");

  public List<String> protectedDefinitionPrefixes() {
    return PROTECTED_DEFINITION_PREFIXES;
  }

  public List<String> deniedLoadPrefixes() {
    return DENIED_LOAD_PREFIXES;
  }

  public List<String> allowedApiPrefixes() {
    return ALLOWED_API_PREFIXES;
  }

  public boolean isProtectedDefinitionPackage(String packageName) {
    return matches(packageName + ".", PROTECTED_DEFINITION_PREFIXES);
  }

  public boolean isDeniedLoadClass(String className) {
    return matches(className, DENIED_LOAD_PREFIXES);
  }

  public boolean isAllowedApiClass(String className) {
    return matches(className, ALLOWED_API_PREFIXES);
  }

  private boolean matches(String value, List<String> prefixes) {
    for (String prefix : prefixes) {
      if (value.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }
}
