package com.spindle.api.exception;

/** Raised when a mod calls a capability-gated runtime API without the required grant. */
public final class CapabilityDeniedException extends SpindleApiException {
  private final String modId;
  private final String capability;
  private final String methodName;

  public CapabilityDeniedException(
      String modId, String capability, String methodName, String message) {
    super(message);
    this.modId = modId;
    this.capability = capability;
    this.methodName = methodName;
  }

  public String modId() {
    return modId;
  }

  public String capability() {
    return capability;
  }

  public String methodName() {
    return methodName;
  }
}
