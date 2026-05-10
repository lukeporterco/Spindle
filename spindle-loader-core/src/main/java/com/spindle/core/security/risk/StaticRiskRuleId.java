package com.spindle.core.security.risk;

import com.spindle.core.security.RuleId;

public enum StaticRiskRuleId implements RuleId {
  RISK_PROCESS_001("RISK-PROCESS-001"),
  RISK_NATIVE_001("RISK-NATIVE-001"),
  RISK_NETWORK_001("RISK-NETWORK-001"),
  RISK_REFLECTION_001("RISK-REFLECTION-001"),
  RISK_UNSAFE_001("RISK-UNSAFE-001"),
  RISK_DYNAMIC_CLASSLOAD_001("RISK-DYNAMIC-CLASSLOAD-001"),
  RISK_SCRIPT_001("RISK-SCRIPT-001"),
  RISK_SERVICE_001("RISK-SERVICE-001"),
  RISK_EMBEDDED_JAR_001("RISK-EMBEDDED-JAR-001"),
  RISK_CLASSFILE_001("RISK-CLASSFILE-001");

  private final String id;

  StaticRiskRuleId(String id) {
    this.id = id;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String toString() {
    return id;
  }

  public static StaticRiskRuleId fromId(String id) {
    for (StaticRiskRuleId value : values()) {
      if (value.id.equals(id)) {
        return value;
      }
    }
    throw new IllegalArgumentException("Unknown static risk rule id: " + id);
  }
}
