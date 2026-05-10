package com.spindle.api.exception;

/** Raised when a mod uses the Runtime API-0 config surface in an unsupported way. */
public final class ConfigAccessException extends SpindleApiException {
  private final String modId;
  private final String key;

  public ConfigAccessException(String modId, String key, String message) {
    super(message);
    this.modId = modId;
    this.key = key;
  }

  public ConfigAccessException(String modId, String key, String message, Throwable cause) {
    super(message, cause);
    this.modId = modId;
    this.key = key;
  }

  public String modId() {
    return modId;
  }

  public String key() {
    return key;
  }
}
