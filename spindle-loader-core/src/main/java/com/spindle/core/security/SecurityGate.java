package com.spindle.core.security;

import com.spindle.core.diagnostics.LoaderException;

public final class SecurityGate {
  public void ensureLifecycleExecutionAllowed(SecurityValidationResult validationResult)
      throws LoaderException {
    if (!validationResult.hasFatalFindings()) {
      return;
    }
    SecurityFinding firstFinding =
        validationResult.findings().stream()
            .filter(SecurityFinding::isFatal)
            .findFirst()
            .orElseThrow();
    StringBuilder message =
        new StringBuilder(
            "Security validation blocked standard runtime lifecycle execution with "
                + validationResult.fatalCount()
                + " fatal finding(s). ");
    message.append('[').append(firstFinding.ruleId().id()).append("] ");
    if (firstFinding.modId() != null) {
      message.append("Mod `").append(firstFinding.modId()).append("` ");
    }
    message.append(firstFinding.message());
    if (firstFinding.location() != null
        && firstFinding.location().kind() != null
        && firstFinding.location().value() != null) {
      message
          .append(" Location: ")
          .append(firstFinding.location().kind())
          .append(" `")
          .append(firstFinding.location().value())
          .append("`.");
    }
    message.append(" Fix: ").append(firstFinding.fix()).append(".");
    message.append(" See spindle.security-report.json for the full validation report.");
    throw new LoaderException(message.toString());
  }
}
