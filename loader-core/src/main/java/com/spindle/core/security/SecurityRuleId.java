package com.spindle.core.security;

public enum SecurityRuleId {
  SEC_PACKAGE_001("SEC-PACKAGE-001"),
  SEC_PACKAGE_002("SEC-PACKAGE-002"),
  SEC_CLASS_001("SEC-CLASS-001"),
  SEC_LIFECYCLE_001("SEC-LIFECYCLE-001"),
  SEC_LIFECYCLE_002("SEC-LIFECYCLE-002"),
  SEC_PATH_001("SEC-PATH-001"),
  SEC_PATH_002("SEC-PATH-002"),
  SEC_CACHE_001("SEC-CACHE-001"),
  SEC_PERM_001("SEC-PERM-001"),
  SEC_ARTIFACT_001("SEC-ARTIFACT-001"),
  SEC_TRUST_001("SEC-TRUST-001"),
  SEC_TRUST_002("SEC-TRUST-002"),
  SEC_TRUST_003("SEC-TRUST-003"),
  SEC_TRUST_004("SEC-TRUST-004"),
  SEC_TRUST_005("SEC-TRUST-005"),
  SEC_TRUST_006("SEC-TRUST-006"),
  SEC_METADATA_001("SEC-METADATA-001"),
  SEC_RUNTIME_001("SEC-RUNTIME-001");

  private final String id;

  SecurityRuleId(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }

  @Override
  public String toString() {
    return id;
  }
}
