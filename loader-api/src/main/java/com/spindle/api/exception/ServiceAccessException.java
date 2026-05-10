package com.spindle.api.exception;

/** Raised when a mod uses the Runtime API-0 service surface in an unsupported way. */
public final class ServiceAccessException extends SpindleApiException {
  private final String modId;
  private final String serviceId;

  public ServiceAccessException(String modId, String serviceId, String message) {
    super(message);
    this.modId = modId;
    this.serviceId = serviceId;
  }

  public ServiceAccessException(String modId, String serviceId, String message, Throwable cause) {
    super(message, cause);
    this.modId = modId;
    this.serviceId = serviceId;
  }

  public String modId() {
    return modId;
  }

  public String serviceId() {
    return serviceId;
  }
}
