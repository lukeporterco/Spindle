package com.spindle.core.lifecycle;

import java.util.List;

public record LifecycleExecutionReport(
    String state,
    String profileFingerprint,
    String inputFingerprint,
    String runtimePolicyFingerprint,
    String cacheStatus,
    String cacheReason,
    List<String> phaseOrder,
    List<LifecycleHandlerDeclaration> plannedHandlers,
    List<HandlerAttempt> attemptedHandlers,
    List<HandlerAttempt> successfulHandlers,
    List<FailedHandler> failedHandlers,
    List<ContextDirectory> contextDirectories) {
  public LifecycleExecutionReport {
    phaseOrder = List.copyOf(phaseOrder);
    plannedHandlers = List.copyOf(plannedHandlers);
    attemptedHandlers = List.copyOf(attemptedHandlers);
    successfulHandlers = List.copyOf(successfulHandlers);
    failedHandlers = List.copyOf(failedHandlers);
    contextDirectories = List.copyOf(contextDirectories);
  }

  public record HandlerAttempt(String phase, String modId, String className, String methodName) {}

  public record FailedHandler(
      String phase, String modId, String className, String methodName, String reason) {}

  public record ContextDirectory(
      String modId,
      String configDirectory,
      String dataDirectory,
      String cacheDirectory,
      String generatedDirectory) {}

  public static final String STATE_PLANNED = "planned";
  public static final String STATE_EXECUTED = "executed";
}
